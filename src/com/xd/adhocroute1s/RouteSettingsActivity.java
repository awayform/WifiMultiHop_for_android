package com.xd.adhocroute1s;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

import com.googlecode.android.wifi.tether.R;
import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.route.IPEditPreference;
import com.xd.adhocroute1s.route.InterfaceEditPreference;
import com.xd.adhocroute1s.route.NetIPEditPreference;

public class RouteSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private CheckBoxPreference openDynCheckGateway;
	private CheckBoxPreference openStaticGateway;
	private CheckBoxPreference openNat;
	
	private NetIPEditPreference natSubnet;
	private IPEditPreference natIp;
	private InterfaceEditPreference natInterface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.routepreferences);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void init() {
		natSubnet = (NetIPEditPreference)findPreference("natsubnet");
		natIp = (IPEditPreference)findPreference("natip");
		natInterface = (InterfaceEditPreference)findPreference("natinterface");
		
		openNat = (CheckBoxPreference)findPreference("open_nat");
		openDynCheckGateway = (CheckBoxPreference)findPreference("is_dyncheck_gateway_enabled");
		openStaticGateway = (CheckBoxPreference)findPreference("is_static_gateway_enabled");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// 静态设置为网关和动态设置为网关只能选一种方式
		if (key.equals("is_dyncheck_gateway_enabled")) {
			if (sharedPreferences.getBoolean("is_dyncheck_gateway_enabled", false)) {
				openStaticGateway.setChecked(false);
			}
		} else if(key.equals("is_static_gateway_enabled")) {
			if (sharedPreferences.getBoolean("is_static_gateway_enabled", false)) {
				openDynCheckGateway.setChecked(false);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (openNat.isChecked()) {
			if (natInterface.getText().trim().isEmpty() && natIp.getText().trim().isEmpty() && natSubnet.getText().trim().isEmpty()) {
				((TetherApplication)getApplicationContext()).adhocRouteApp.showToastMsg(R.string.toast_nat_param_error);
			} else {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}
}
