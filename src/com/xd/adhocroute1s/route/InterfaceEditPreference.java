package com.xd.adhocroute1s.route;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.googlecode.android.wifi.tether.TetherApplication;
import com.xd.adhocroute1s.AdhocRouteApp;
import com.xd.wifimultihop.R;

/**
 * 支持判断网卡是否存在
 * @author qhyuan1992
 *
 */
public class InterfaceEditPreference extends EditTextPreference {
	public InterfaceEditPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public InterfaceEditPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public InterfaceEditPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean interfaceCheck(String text) {
        if (text != null && !text.isEmpty()) {
        	return ((TetherApplication)getContext().getApplicationContext()).coretask.networkInterfaceExists(text);
        }
        return false;
    }
	public  boolean validate(String addr) {
		return interfaceCheck(addr);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String addr = getEditText().getText().toString();
			if (!validate(addr)) {
				((TetherApplication)getContext().getApplicationContext()).adhocRouteApp.showToastMsg(R.string.toast_interface_set_not_exist);
				positiveResult = false;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}
