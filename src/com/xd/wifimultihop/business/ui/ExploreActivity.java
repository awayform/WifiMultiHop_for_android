package com.xd.wifimultihop.business.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.android.wifi.tether.R;


public class ExploreActivity extends Activity {

	protected static final int LOAD_SUCESS = 0x08;
	private WebSettings webSettings;
	private WebView webView;
	private ProgressDialog dialog;
	private Button exploreBtn;
	private EditText exploreEt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore);
		webView = (WebView) findViewById(R.id.wv);
		exploreBtn = (Button) findViewById(R.id.btn_explore);
		exploreEt = (EditText) findViewById(R.id.et_explore);
		exploreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				webView.loadUrl(exploreEt.getText().toString().trim());
			}
		});
		// zoomCtl = (ZoomControls) findViewById(R.id.zoom);
		// webView.getSettings().setBlockNetworkImage(true);
		webSettings = webView.getSettings();
		// webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// webSettings.setSupportZoom(true);
		webSettings.setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		/*dialog = new ProgressDialog(this);
		dialog.setTitle("浏览网页");
		dialog.setMessage("正在加载...");
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();*/
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				//
//				dialog.dismiss();
				super.onPageFinished(view, url);
			}
		});
		
	}
		
}