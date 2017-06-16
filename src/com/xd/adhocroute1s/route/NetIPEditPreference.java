package com.xd.adhocroute1s.route;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.googlecode.android.wifi.tether.R;
import com.googlecode.android.wifi.tether.TetherApplication;
/**
 * 支持判断是否是合法的用斜杠和数字表示的子网号
 * @author qhyuan1992
 *
 */
public class NetIPEditPreference extends EditTextPreference {
	public NetIPEditPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public NetIPEditPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public NetIPEditPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		editText.setKeyListener(DigitsKeyListener.getInstance("0123456789./"));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean netIPCheck(String text) {
		if (text.isEmpty()) return true;
        if (text != null && !text.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)"
                    + "/\\d+$";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
	public  boolean validate(String addr) {
		return netIPCheck(addr);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			String addr = getEditText().getText().toString();
			if (!validate(addr)) {
				((TetherApplication)getContext().getApplicationContext()).adhocRouteApp.showToastMsg(R.string.toast_setting_preference_format_error);
				positiveResult = false;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}
