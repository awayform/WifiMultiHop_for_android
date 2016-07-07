package com.xd.adhocroute1s.route;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 用来配置路由的配置文件
 * @author qhyuan1992
 *
 */
public class RouteConfig {
	// 基本配置
	private String baseConfiguration;
	// 额外增加的
	private Vector<RouteConfig.SimpleInfo> simpleInfos;
	private Vector<RouteConfig.ComplexInfo> complexInfos;
	
	public RouteConfig() {
		baseConfiguration = "";
		simpleInfos = new Vector<RouteConfig.SimpleInfo>();
		complexInfos = new Vector<RouteConfig.ComplexInfo>();
	}
	public RouteConfig(String baseConfiguration) throws IOException, FileNotFoundException {
		this(new FileInputStream(baseConfiguration));
	}
	
	public RouteConfig(InputStream base) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader buf = new BufferedReader(new InputStreamReader(base));		
		if (buf != null) {
			String line;
			while ((line = buf.readLine()) != null) {
				builder.append(line);
				builder.append("\n");
			}
			buf.close();
		}
		baseConfiguration = new String(builder);
		simpleInfos = new Vector<RouteConfig.SimpleInfo>();
		complexInfos = new Vector<RouteConfig.ComplexInfo>();
	}
	
	public void addSimpleConfigInfo(RouteConfig.SimpleInfo configInfo) {
		simpleInfos.add(configInfo);
	}
	
	public void addComplexConfigInfo(RouteConfig.ComplexInfo configInfo) {
		complexInfos.add(configInfo);
	}
	
	// 基本的配置 + SharedPreference里面的配置
	public String toString() {
		StringBuilder builder = new StringBuilder(baseConfiguration);
		// 增加简单配置信息
		for (RouteConfig.SimpleInfo s : simpleInfos) {
			builder.append(s.toString());
		}
		
		// 增加复杂配置信息
		for (RouteConfig.ComplexInfo s : complexInfos) {
			builder.append(s.toString());
		}
		return new String(builder);
	}
	
	public void write(OutputStream out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		if (writer != null) {
			writer.write(toString());
			writer.flush();
			writer.close();
		}
	}
	
	/**
	 * 较简单的配置形式的抽象，一般是键值对，抽象形式为<br>
	 * XXXX XXXX<br>
	 * 命名如下<br>
	 * paramKey paramValue<br>
	 * @author qhyuan1992
	 *
	 */
	public static class SimpleInfo {
		protected Map<String, String> parameterMap;
		public SimpleInfo () {
			parameterMap = new HashMap<String, String>();
		}
		public void addParamKeyValue(String key, String value) {
			parameterMap.put(key, value);
		}
		public void setIpVersion(int ipVersion) {
			addParamKeyValue("IpVersion", String.valueOf(ipVersion));
		}
		
		public void setOlsrPort(int port) {
			addParamKeyValue("OlsrPort", String.valueOf(port));
		}
		
		public void setTosValue(int tosValue) {
			addParamKeyValue("TosValue", String.valueOf(tosValue));
		}
		
		public void setWillingness(int willingness) {
			addParamKeyValue("Willingness", String.valueOf(willingness));
		}
		
		public void setAllowNoIntEnabled(boolean allowNoInt) {
			addParamKeyValue("AllowNoInt", allowNoInt ? "yes" : "no");
		}
		
		public void setLinkQualityLevel(int linkQualityLevel) {
			addParamKeyValue("LinkQualityLevel", String.valueOf(linkQualityLevel));
		}
		
		public void setLinkQualityAlgorithm(String linkQualityAlgorithm) {
			addParamKeyValue("LinkQualityAlgorithm", "\"" + linkQualityAlgorithm + "\"");
		}
		
		public void setLinkQualityAging(float linkQualityAging) {
			addParamKeyValue("LinkQualityAging", String.valueOf(linkQualityAging));
		}
		
		public void setLinkQualityFishEyeEnabled(boolean linkQualityFishEyeEnabled) {
			addParamKeyValue("LinkQualityFishEye", linkQualityFishEyeEnabled ? "1" : "0");
		}
		
		public void setUseHysteresis(boolean useHysteresis) {
			addParamKeyValue("UseHysteresis", useHysteresis ? "yes" : "no");
		}
		
		public void setPollrate(float seconds) {
			addParamKeyValue("Pollrate", String.valueOf(seconds));
		}
		
		public void setNicChangePollrate(float seconds) {
			addParamKeyValue("NicChgsPollInt", String.valueOf(seconds));
		}
		
		public void setTcRedundancy(int tcRedundancy) {
			addParamKeyValue("TcRedundancy", String.valueOf(tcRedundancy));
		}
		
		public void setNatThreshold(float natThreshold) {
			addParamKeyValue("NatThreshold", String.valueOf(natThreshold));
		}
		
		public void setMprCoverage(int mprCoverage) {
			addParamKeyValue("MprCoverage", String.valueOf(mprCoverage));
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> e : parameterMap.entrySet()) {
				builder.append(e.getKey() + " " + e.getValue() + "\n");
			}
			return new String(builder);
		}
	}
	
	/**
	 * 较复杂的配置形式的抽象,抽象形式如下：<br>
	 * XXXX XXXX<br>
	 * {<br>
	 *     XXXX XXXX XXXX<br>
	 * }<br>
	 * 
	 * 命名如下<br>
	 * header headerInfo<br>
	 * {<br>
	 *     paramType paramKey paramValue<br>
	 * }<br>
	 * 
	 * @author qhyuan1992
	 *
	 */
	public static class ComplexInfo {
		// 后面跟上键值对， 比如 host 127.0.0.1
		protected Map<String, String> parameterMap;
		// 可以试试LoadPlugin
		protected String header;
		// LoadPlugin后面跟的路径
		protected String headInfo;
		// 标识，比如 PlParam，可以没有
		protected String paramType;
		
		public ComplexInfo(String header, String headInfo, String paramType) {
			this.header = header;
			this.headInfo = headInfo;
			this.paramType = paramType;
			parameterMap = new HashMap<String,String>();
		}
		
		public void addParamKeyValue(String key, String value) {
			parameterMap.put(key, value);
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (!header.equals("")) {
				builder.append(header);
			}
			if (!headInfo.equals("")) {
				builder.append(" ");
				builder.append("\"" + headInfo + "\"\n");
			}
			builder.append("{\n");
			for (Map.Entry<String, String> e : parameterMap.entrySet()) {
				builder.append("\t" + paramType + " \"" + e.getKey() + "\" \"" + e.getValue() + "\"\n");
			}
			builder.append("}\n");
			return new String(builder);
		}
	}
	
	/**
	 * Hna消息
	 * @author qhyuan1992
	 * @see com.xd.adhocroute1s.route.RouteConfig.ComplexInfo
	 */
	public static class HnaInfo extends ComplexInfo {
		public HnaInfo() {
			super("Hna4", "", "");
		}
		
		public void addWan(String wanSubnet, String wanMask) {
			addParamKeyValue(wanSubnet, wanMask);
		}
		// Hna的具体信息在格式上和其他配置不一样，于是要重写toString方法
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (!header.equals("")) {
				builder.append(header);
			}
			if (!headInfo.equals("")) {
				builder.append(" ");
				builder.append("\"" + headInfo + "\"");
			}
			builder.append("{\n");
			for (Map.Entry<String, String> e : parameterMap.entrySet()) {
				builder.append("\t" + paramType + " " + e.getKey() + " " + e.getValue() + "\n");
			}
			builder.append("}\n");
			return new String(builder);
		}
	}
	
	/**
	 * 加载插件
	 * @author qhyuan1992
	 * @see com.xd.adhocroute1s.route.RouteConfig.ComplexInfo
	 */
	public static class PluginInfo extends ComplexInfo{
		public PluginInfo(String pluginPath) {
			super("LoadPlugin", pluginPath, "PlParam");
		}
	}

	/**
	 * 声明网卡信息
	 * @author qhyuan1992
	 * @see com.xd.adhocroute1s.route.RouteConfig.ComplexInfo
	 */
	public static class InterfaceInfo extends ComplexInfo{
		public InterfaceInfo(String infaceName) {
			super("Interface", infaceName, "");
		}
		
		public void setHelloInterval(float seconds) {
			addParamKeyValue("HelloInterval", String.valueOf(seconds));
		}
		public void setHelloValidityTime(float seconds) {
			addParamKeyValue("HelloValidityTime", String.valueOf(seconds));
		}
		
		public void setTcInterval(float seconds) {
			addParamKeyValue("TcInterval", String.valueOf(seconds));
		}
		public void setTcValidityTime(float seconds) {
			addParamKeyValue("TcValidityTime", String.valueOf(seconds));
		}
		
		public void setMidInterval(float seconds) {
			addParamKeyValue("MidInterval", String.valueOf(seconds));
		}
		public void setMidValidityTime(float seconds) {
			addParamKeyValue("MidValidityTime", String.valueOf(seconds));
		}
		
		public void setHnaInterval(float seconds) {
			addParamKeyValue("HnaInterval", String.valueOf(seconds));
		}
		public void setHnaValidityTime(float seconds) {
			addParamKeyValue("HnaValidityTime", String.valueOf(seconds));
		}
		
		// 网卡的具体信息在格式上和其他配置不一样，于是要重写toString方法
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (header != null && headInfo != null) {
				builder.append(header);
				builder.append(" ");
				builder.append("\"" + headInfo + "\"");
			}
			builder.append("{\n");
			for (Map.Entry<String, String> e : parameterMap.entrySet()) {
				builder.append("\t" + paramType + e.getKey() + " " + e.getValue() + "\n");
			}
			builder.append("}\n");
			return new String(builder);
		}
	}
}
