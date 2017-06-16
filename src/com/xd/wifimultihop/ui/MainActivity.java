package com.xd.wifimultihop.ui;

import java.util.ArrayList;
import java.util.List;

import android.R.drawable;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.googlecode.android.wifi.tether.AccessControlActivity;
import com.googlecode.android.wifi.tether.LogActivity;
import com.googlecode.android.wifi.tether.R;
import com.googlecode.android.wifi.tether.SetupActivity;
import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.RouteSettingsActivity;

public class MainActivity extends Activity {
	private static final int MENU_SETUP = 0;
	private static final int MENU_ROUTE = 1;
	private static final int MENU_LOG = 2;
	private static final int MENU_ABOUT = 3;
	private static final int MENU_ACCESS = 4;
	
	private FrameLayout container;
	private FragmentTransaction ft;
	private RadioGroup rg;
	private List<Fragment> addedFragments = new ArrayList<Fragment>();

	private Fragment buildNetFragment;
	private Fragment routeFragment;
	private Fragment businessFragment;
	
	private TetherApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		application = (TetherApplication)getApplication();
		container = (FrameLayout)findViewById(R.id.container);
		ft = getFragmentManager().beginTransaction();
		if (buildNetFragment == null) {
			buildNetFragment = new BuildNetFragment();
		}
		if (routeFragment == null) {
			routeFragment = new RouteFragment();
			
		}
		if (businessFragment == null) {
			businessFragment = new BusinessFragment();
		}
		// 默认显示的是firstPage
		ft.replace(R.id.container, buildNetFragment).commit();
		addedFragments.add(buildNetFragment);
		
		rg = (RadioGroup) findViewById(R.id.main_tab);
		rg.check(R.id.radio_button_buildnet);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_button_buildnet:
					
					getFragmentManager().beginTransaction().hide(routeFragment).commit();
					getFragmentManager().beginTransaction().hide(businessFragment).commit();
					if (buildNetFragment == null) {
						buildNetFragment = new BuildNetFragment();
						getFragmentManager().beginTransaction().add(R.id.container, buildNetFragment).commit();
						addedFragments.add(buildNetFragment);
					}else {
						if (!addedFragments.contains(buildNetFragment)) {
							getFragmentManager().beginTransaction().add(R.id.container, buildNetFragment);
							addedFragments.add(buildNetFragment);// 自己判断是否已添加
						}
						getFragmentManager().beginTransaction().show(buildNetFragment).commit();
					}
					break;
				case R.id.radio_button_route:
					getFragmentManager().beginTransaction().hide(buildNetFragment).commit();
					getFragmentManager().beginTransaction().hide(businessFragment).commit();
					if (routeFragment == null) {
						routeFragment = new RouteFragment();
						getFragmentManager().beginTransaction().add(R.id.container, routeFragment).commit();
						addedFragments.add(routeFragment);
					}else {
						FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
						if (!addedFragments.contains(routeFragment)) {
							fTransaction.add(R.id.container, routeFragment);
							addedFragments.add(routeFragment);// 自己判断是否已添加
						}
						fTransaction.show(routeFragment).commit();
					}
					break;
				case R.id.radio_button_service:
					getFragmentManager().beginTransaction().hide(buildNetFragment).commit();
					getFragmentManager().beginTransaction().hide(routeFragment).commit();
					if (businessFragment == null) {
						businessFragment = new BusinessFragment();
						getFragmentManager().beginTransaction().add(R.id.container, businessFragment).commit();
						addedFragments.add(businessFragment);
					}else {
						FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
						if (!addedFragments.contains(businessFragment)) {
							fTransaction.add(R.id.container, businessFragment);
							addedFragments.add(businessFragment);// 自己判断是否已添加
						}
						fTransaction.show(businessFragment).commit();
					}
					break;
				
				}
				
			}
		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean supRetVal = super.onCreateOptionsMenu(menu);
    	SubMenu setup = menu.addSubMenu(0, MENU_SETUP, 0, "网络设置");
    	setup.setIcon(drawable.ic_menu_preferences);
    	
    	SubMenu route = menu.addSubMenu(0, MENU_ROUTE, 0, "路由设置");
    	route.setIcon(drawable.ic_menu_preferences);
    	
    	
    	if (this.application.accessControlSupported) { 
    		SubMenu accessctr = menu.addSubMenu(0, MENU_ACCESS, 0, getString(R.string.main_activity_accesscontrol));
    		accessctr.setIcon(drawable.ic_menu_edit);   
    	}
    	SubMenu log = menu.addSubMenu(0, MENU_LOG, 0, getString(R.string.main_activity_showlog));
    	log.setIcon(drawable.ic_menu_agenda);
    	SubMenu about = menu.addSubMenu(0, MENU_ABOUT, 0, getString(R.string.main_activity_about));
    	about.setIcon(drawable.ic_menu_info_details); 
    	return supRetVal;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
		
    	boolean supRetVal = super.onOptionsItemSelected(menuItem);
    	switch (menuItem.getItemId()) {
	    	case MENU_SETUP :
		        startActivityForResult(new Intent(
		        		MainActivity.this, SetupActivity.class), 0);
		    break;
	    	case MENU_ROUTE :
	    		Intent intent = new Intent(this, RouteSettingsActivity.class);
				this.startActivity(intent); 		
		    break;
	    	case MENU_LOG :
		        startActivityForResult(new Intent(
		        		MainActivity.this, LogActivity.class), 0);
		    break;
	    	case MENU_ACCESS :
		        startActivityForResult(new Intent(
		        		MainActivity.this, AccessControlActivity.class), 0);   		
		    break;
	    	case MENU_ABOUT :
	    		CommonDialog dialog = new CommonDialog(this);
				dialog.setTitle("关于软件");
				dialog.setMessage("本软件由是通过Android的wifi进行Ad-Hoc组网实现多跳功能，可以支持上层语音视频等业务。\n\n"
						+ "联系：西安电子科技大学ISN实验室，老科A614");
				dialog.showNegitiveButton(false);
				dialog.showPositiveButton(false);
				dialog.show();
		    break;
		    
		    
    	}
    	return supRetVal;
    } 
    
}
