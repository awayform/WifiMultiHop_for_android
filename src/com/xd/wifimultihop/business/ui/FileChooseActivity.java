package com.xd.wifimultihop.business.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.android.wifi.tether.R;
import com.xd.wifimultihop.business.app.Constants;

public class FileChooseActivity extends Activity {

	private class FileChooseAdapter extends BaseAdapter {

		private LayoutInflater layout;
		private ArrayList<File> mlist;

		public FileChooseAdapter(Context context, ArrayList<File> list) {
			layout = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mlist = list;
		}

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public File getItem(int position) {
			return mlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = layout.inflate(R.layout.listitem_file, fileListView,
						false);
			} else {
				view = convertView;
			}
			File file = getItem(position);
			TextView text = (TextView) view.findViewById(R.id.itemText);
			text.setText(file.getName());
			ImageView image = (ImageView) view.findViewById(R.id.itemImage);
			if (file.isFile()) {
				image.setImageResource(R.drawable.file);
			} else {
				image.setImageResource(R.drawable.folder);
			}
			return view;
		}

	}

	// private static final String TAG = "FileChooseActivity";
	private File root;
	private TextView pathText;
	private ListView fileListView;
	private ArrayList<File> curFileList;
	private File curFile;
	private FileChooseAdapter mAdapter;

	private HashMap<File, Integer> returnPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_file_choose);
		returnPos = new HashMap<File, Integer>();
		pathText = (TextView) findViewById(R.id.textPath);
		fileListView = (ListView) findViewById(R.id.listFile);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			root = Environment.getExternalStorageDirectory();
			pathText.setText(root.toString());
			curFileList = new ArrayList<File>();
			curFile = root;
			updateFileList(curFile);
			mAdapter = new FileChooseAdapter(this, curFileList);
			fileListView.setAdapter(mAdapter);
			fileListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					File file = ((FileChooseAdapter) parent.getAdapter())
							.getItem(position);
					if (file.isDirectory()) {
						returnPos.put(file.getParentFile(),
								Integer.valueOf(fileListView
										.getFirstVisiblePosition()));
						curFile = file;
						updateFileList(curFile);
						pathText.setText(curFile.toString());
						mAdapter.notifyDataSetChanged();
					} else {
						Intent intent = new Intent(FileChooseActivity.this,
								DialNoActivity.class);
						intent.putExtra(Constants.EXTRA_TYPE,
								Constants.FILE_REQ);
						intent.putExtra(Constants.EXTRA_FILEPATH,
								file.toString());
						intent.putExtra(Constants.EXTRA_FILENAME,
								file.getName());
						intent.putExtra(Constants.EXTRA_FILESIZE, file.length());
						startActivity(intent);
					}
				}
			});
		} else {
			pathText.setText("null");
			Toast.makeText(this, "没有可用的SD卡或手机存储空间", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 没有SD Card
			if (curFile == null) return super.onKeyDown(keyCode, event);
			if (!curFile.toString().equals(root.toString())) {
				curFile = curFile.getParentFile();
				updateFileList(curFile);
				pathText.setText(curFile.toString());
				mAdapter.notifyDataSetChanged();
				fileListView.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						int pos = returnPos.get(curFile).intValue();
						fileListView.setSelection(pos);
					}
				});

				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void updateFileList(File currentDir) {
		File[] files = currentDir.listFiles();
		curFileList.clear();
		for (int i = 0; i < files.length; i++) {
			curFileList.add(files[i]);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {  
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    }
		return super.onOptionsItemSelected(item);
	}
}
