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
 * 支持判断是否是合法的掩码地址
 * @author qhyuan1992
 *
 */
public class MaskEditPreference extends EditTextPreference {
	public MaskEditPreference(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public MaskEditPreference(Context context, AttributeSet attrs) { super(context, attrs); }
	public MaskEditPreference(Context context) { super(context); }

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public boolean maskCheck(String text) {
		if (text.isEmpty()) return true;
        if (text != null && !text.isEmpty()) {
            String regex = "(254|252|248|240|224|192|128|0)\\.0\\.0\\.0|"     
            			+ "255\\.(254|252|248|240|224|192|128|0)\\.0\\.0|"
            			+ "255\\.255\\.(254|252|248|240|224|192|128|0)\\.0|"
            			+ "255\\.255\\.255\\.(254|252|248|240|224|192|128|0)";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
	public  boolean validate(String addr) {
		return maskCheck(addr);
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
