package com.xd.wifimultihop.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.xd.wifimultihop.R;

/**
 * 通用对话框
 * 
 */
public class CommonDialog extends Dialog implements android.view.View.OnClickListener {

    private TextView messageView;
    private TextView titleView;
    private Button positiveButton;
    private Button negativeButton;
    private View devider;
    private OnClickListener clickListener;

    public CommonDialog(Context context) {
        super(context);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.base_dialog);
        titleView = (TextView) findViewById(R.id.title);
        messageView = (TextView) findViewById(R.id.message);
        positiveButton = (Button) findViewById(R.id.ok);
        positiveButton.setOnClickListener(this);
        negativeButton = (Button) findViewById(R.id.cancel);
        negativeButton.setOnClickListener(this);
        
        devider = findViewById(R.id.devider);
        
        View closeView = findViewById(R.id.close);
        closeView.setOnClickListener(this);
    }

    public void setTitle(int resId) {
        titleView.setText(resId);
    }

    public void setTitle(CharSequence text) {
        titleView.setText(text);
    }
    
    public void showPositiveButton(boolean isShow) {
        if (isShow) {
            devider.setVisibility(View.VISIBLE);
            positiveButton.setVisibility(View.VISIBLE);
        } else {
            devider.setVisibility(View.GONE);
            positiveButton.setVisibility(View.GONE);
        }
    }
    
    public void setPositiveButton(CharSequence text) {
    	positiveButton.setText(text);
    }
    
    public void showNegitiveButton(boolean isShow) {
        if (isShow) {
            devider.setVisibility(View.VISIBLE);
            negativeButton.setVisibility(View.VISIBLE);
        } else {
            devider.setVisibility(View.GONE);
            negativeButton.setVisibility(View.GONE);
        }
    }
    public void setNegativeButton(CharSequence text) {
    	negativeButton.setText(text);
    }

    public void setMessage(int resId) {
        messageView.setText(resId);
    }

    public void setMessage(CharSequence text) {
        messageView.setText(text);
    }

    public void setOnClickListener(DialogInterface.OnClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            int which = DialogInterface.BUTTON_POSITIVE;
            switch (v.getId()) {
                case R.id.ok:
                    which = DialogInterface.BUTTON_POSITIVE;
                    break;
                case R.id.close:
                case R.id.cancel:
                    which = DialogInterface.BUTTON_NEGATIVE;
                    break;
                default:
                    break;
            }
            clickListener.onClick(this, which);
        }
        dismiss();
    }
}