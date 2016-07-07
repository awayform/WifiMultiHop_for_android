package com.xd.adhocroute1s.nativehelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.util.Log;

/**
 * 对NativeTask的进一步封装<br>
 * 封装了与设备已经执行进程相关的一些函数
 * @author qhyuan1992
 *
 */
public class CoreTask {
	public static final String TAG = "AdhocRoute -> CoreTask";
	// <进程文件绝对路径，启动进程命令>
	// 用来缓存，避免每次都去取文件，除非发现一个新的进程未被包含在里面
	private Hashtable<String,String> runningProcesses = new Hashtable<String,String>();

    public boolean hasRootPermission() {
    	boolean rooted = true;
		try {
			File su = new File("/system/bin/su");
			if (su.exists() == false) {
				su = new File("/system/xbin/su");
				if (su.exists() == false) {
					rooted = false;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Can't obtain root - Here is what I know: "+e.getMessage());
			rooted = false;
		}
		return rooted;
    }

    public boolean networkInterfaceExists(String device) {
    	if (device == null || device.equals(""))
    		return false;
    	for (String line : readLinesFromFile("/proc/net/dev")) {
    		if (line.startsWith(device)) {
    			if(line.substring(0, line.indexOf(":")).trim().equals(device)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public static void setFirstDns(String dns) {
    	CoreTask.runRootCommand("setprop net.dns1 " + dns);
    }
    
    public static void setSecondDns(String dns) {
    	CoreTask.runRootCommand("setprop net.dns2 " + dns);
    }
    // 启动进程
    public static boolean startProcess (String proc) {
    	return runRootCommand(proc);
    }

    /*
     * 好像有问题
     */
    @Deprecated
    public static boolean killProcessNative(String procName) {
		int returncode = NativeTask.killProcess(2, procName);
		if (returncode == 0) {
			return true;
		}
		return false;
	}

    // olsrd
    public boolean killProcess(String processName) {
    	boolean killSuccess = false;
    	List<String> targetIDList = new ArrayList<String>();
		for (String procFilePath : runningProcesses.keySet()) {
			String procStartCmd = runningProcesses.get(procFilePath);
			if (procStartCmd.contains(processName)) {
				String targetID = procFilePath.substring(procFilePath.lastIndexOf("/") + 1);
				targetIDList.add(targetID);
			}
		}
		
		for (String targetID : targetIDList) {
			if (killProcessByPid(targetID)) {
				killSuccess = true;
				runningProcesses.remove(targetID);
			}
		}
		return killSuccess;
    }

    public boolean killProcessByPid(String procID) {
    	return CoreTask.runRootCommand("kill -2 " + procID);
    }

    public boolean isProcessRunning(String processName) {
		boolean processIsRunning = false;
		Hashtable<String,String> tmpRunningProcesses = new Hashtable<String,String>();
		File procDir = new File("/proc");
		FilenameFilter filter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            try {
	                Integer.parseInt(name);
	            } catch (NumberFormatException ex) {
	                return false;
	            }
	            return true;
	        }
	    };
		File[] processes = procDir.listFiles(filter);
		// 循环遍历所有的进程
		for (File process : processes) {
			String cmdLine = "";
			// Checking if this is a already known process
			if (this.runningProcesses.containsKey(process.getAbsoluteFile().toString())) {
				cmdLine = this.runningProcesses.get(process.getAbsoluteFile().toString());
			}
			else {
				// 读取到进程启动命令
				ArrayList<String> cmdlineContent = this.readLinesFromFile(process.getAbsoluteFile()+"/cmdline");
				if (cmdlineContent != null && cmdlineContent.size() > 0) {
					cmdLine = cmdlineContent.get(0);
				}
			}
			// Adding to tmp-Hashtable
			tmpRunningProcesses.put(process.getAbsoluteFile().toString(), cmdLine);
			// Checking if processName matches
			if (cmdLine.contains(processName)) {
				processIsRunning = true;
			}
		}
		// Overwriting runningProcesses
		this.runningProcesses = tmpRunningProcesses;
		return processIsRunning;
	}

	public boolean isNatEnabled() {
    	ArrayList<String> lines = readLinesFromFile("/proc/sys/net/ipv4/ip_forward");
    	return lines.contains("1");
    }
    
	/**
	 * 开启NAT功能
	 * 1，可以写文件，但要修改获取权限
	 * 2.roots执行命令
	 */
    public void setNatEnabled() {
    	CoreTask.runRootCommand("echo \"1\" > /proc/sys/net/ipv4/ip_forward");
    	//this.writeLineToFile("/proc/sys/net/ipv4/ip_forward", "1"); // error
    }

    public void addNatWithSrcAndIP(String src, String ipTarget) {
    	setNatWithSrcAndIP(src, ipTarget, true);
    }
    public void delNatWithSrcAndIP(String netRange, String ipTarget) {
    	setNatWithSrcAndIP(netRange, ipTarget, false);
    }
    
    public void addNatWithSrcAndInterface(String src, String inface) {
    	setNatWithSrcAndInterface(src, inface, true);
    }
    public void delNatWithSrcAndInterface(String src, String inface) {
    	setNatWithSrcAndInterface(src, inface, false);
    }
    
    public void addNatWithSrc(String src) {
    	setNatWithSrc(src, true);
    }
    public void delNatWithSrc(String src) {
    	setNatWithSrc(src, false);
    }
    
    public void addNatWithInterface(String inface) {
    	setNatWithInterface(inface, true);
    }
    public void delNatWithInterface(String inface) {
    	setNatWithInterface(inface, false);
    }
    
    public void addNat() {
    	setNat(true);
    }
    public void delNat() {
    	setNat(false);
    }

    public void delAllNat() {
    	String natdeleAllCmd = "iptables -t nat -F POSTROUTING";
    	CoreTask.runRootCommand(natdeleAllCmd);
    }
    
    private void setNatWithSrcAndIP(String src, String ipTarget, boolean isAdd) {
    	String natCmd = "";
    	if (isAdd) {
    		natCmd = "iptables -t nat -A POSTROUTING -s " + src + " -j SNAT --to-source " + ipTarget;
    	} else {
    		natCmd = "iptables -t nat -D POSTROUTING -s " + src + " -j SNAT --to-source " + ipTarget;
    	}
    	CoreTask.runRootCommand(natCmd);
    }
    
    private void setNatWithSrcAndInterface(String src, String inface, boolean isAdd) {
    	String natCmd = "";
    	if (isAdd) {
    		natCmd = "iptables -t nat -A POSTROUTING -o " + inface + " -s " + src + " -j MASQUERADE";
    	} else {
    		natCmd = "iptables -t nat -D POSTROUTING -o " + inface + " -s " + src + " -j MASQUERADE";
    	}
    	CoreTask.runRootCommand(natCmd);
    }
    
    private void setNatWithSrc(String src, boolean isAdd) {
    	String natCmd = "";
    	if (isAdd) {
    		natCmd = "iptables -t nat -A POSTROUTING -s " + src + " -j MASQUERADE";
    	} else {
    		natCmd = "iptables -t nat -D POSTROUTING -s " + src + " -j MASQUERADE";
    	}
    	CoreTask.runRootCommand(natCmd);
    }
    
    private void setNatWithInterface(String inface, boolean isAdd) {
    	String natCmd = "";
    	if (isAdd) {
    		natCmd = "iptables -t nat -A POSTROUTING -o " + inface +" -j MASQUERADE";
    	} else {
    		natCmd = "iptables -t nat -D POSTROUTING -o " + inface +" -j MASQUERADE";
    	}
    	CoreTask.runRootCommand(natCmd);
    }
    
    private void setNat(boolean isAdd) {
    	String natCmd = "";
    	if (isAdd) {
    		natCmd = "iptables -t nat -A POSTROUTING -j MASQUERADE";
    	} else {
    		natCmd = "iptables -t nat -D POSTROUTING -j MASQUERADE";
    	}
    	CoreTask.runRootCommand(natCmd);
    }

    public static boolean runRootCommand(String command) {
		Log.d(TAG, "Root-Command ==> su -c \""+command+"\"");
		int returncode = NativeTask.runCommand("su -c \""+command+"\"");
		if (returncode == 0) {
			return true;
		}
		Log.d(TAG, "Root-Command error, return code: " + returncode);
		return false;
	}

	public static boolean runStandardCommand(String command) {
		Log.d(TAG, "Standard-Command ==> \""+command+"\"");
		int returncode = NativeTask.runCommand(command);
		if (returncode == 0) {
			return true;
		}
		Log.d(TAG, "Standard-Command error, return code: " + returncode);
		return false;
	}

	public ArrayList<String> readLinesFromFile(String filename) {
		String line = null;
		BufferedReader br = null;
		InputStream ins = null;
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(filename);
		if (file.canRead() == false)
			return lines;
		try {
			ins = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(ins), 8192);
			while((line = br.readLine())!=null) {
				lines.add(line.trim());
			}
		} catch (Exception e) {
			Log.d(TAG, "Unexpected error - Here is what I know: "+e.getMessage());
		}
		finally {
			try {
				ins.close();
				br.close();
			} catch (Exception e) {
				// Nothing.
			}
		}
		return lines;
	}

	public boolean writeLineToFile(String filename, String lines) {
		OutputStream out = null;
		boolean returnStatus = false;
		Log.d(TAG, "Writing " + lines.length() + " bytes to file: " + filename);
		try {
			out = new FileOutputStream(filename);
	    	out.write(lines.getBytes());
	    	out.flush();
		} catch (Exception e) {
			Log.d(TAG, "Unexpected error - Here is what I know: "+e.getMessage());
		}
		finally {
	    	try {
	    		if (out != null)
	    			out.close();
	    		returnStatus = true;
			} catch (IOException e) {
				returnStatus = false;
			}
		}
		return returnStatus;
	}
	
	/**
	 * 修改文件权限
	 * @param file
	 * @param mode
	 * @return
	 */
	public boolean chmod(String file, String mode) {
		if (NativeTask.runCommand("chmod "+ mode + " " + file) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 获取内核版本
	 * @return
	 */
	public String getKernelVersion() {
        ArrayList<String> lines = readLinesFromFile("/proc/version");
        String version = lines.get(0).split(" ")[2];
        Log.d(TAG, "Kernel version: " + version);
        return version;
    }

    public boolean isBusyboxInstalled() {
    	if ((new File("/system/bin/busybox")).exists() == false) {
	    	if ((new File("/system/xbin/busybox")).exists() == false) {
	    		return false;
	    	}
    	}
    	return true;
    }
    
    /*
     * This method check if needed binary for the rooting-fix are present
     */
    public boolean isRoutefixSupported() {
    	if ((new File("/system/bin/ip")).exists()) {
    		return true;
    	}
    	return false;
    }
    
    public String getProp(String property) {
    	return NativeTask.getProp(property);
    }
    
    public long[] getDataTraffic(String device) {
    	// Returns traffic usage for all interfaces starting with 'device'.
    	long [] dataCount = new long[] {0, 0};
    	if (device == null || device.equals(""))
    		return dataCount;
    	for (String line : readLinesFromFile("/proc/net/dev")) {
    		if (line == null || line.startsWith(device) == false)
    			continue;
    		line = line.replace(':', ' ');
    		String[] values = line.split(" +");
    		dataCount[0] += Long.parseLong(values[1]);
    		dataCount[1] += Long.parseLong(values[9]);
    	}
    	return dataCount;
    }
    
    public long getModifiedDate(String filename) {
    	File file = new File(filename);
    	if (file.exists() == false) {
    		return -1;
    	}
    	return file.lastModified();
    }
}
