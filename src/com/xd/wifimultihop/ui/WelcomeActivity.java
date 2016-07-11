package com.xd.wifimultihop.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.android.wifi.tether.R;

public class WelcomeActivity extends Activity implements AnimationListener {
	private ImageView imageView = null;
	private TextView welcomeText = null;
	private TextView welcomeTitleText = null;
	private Animation alphaAnimation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		imageView = (ImageView) findViewById(R.id.welcome_image_view);
		welcomeText = (TextView) findViewById(R.id.welcome_text);
		welcomeTitleText = (TextView) findViewById(R.id.welcome_title);
		alphaAnimation = AnimationUtils.loadAnimation(this,
				R.anim.welcome_alpha);
		welcomeText.setAnimation(alphaAnimation);
		welcomeTitleText.setAnimation(alphaAnimation);
		alphaAnimation.setFillEnabled(true);
		alphaAnimation.setFillAfter(true);
		imageView.setAnimation(alphaAnimation);
		alphaAnimation.setAnimationListener(this);
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return false;
	}
}
