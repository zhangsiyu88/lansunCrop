package com.lansun.qmyo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class PopRelativeLayout extends RelativeLayout {

	@SuppressLint("NewApi") public PopRelativeLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public PopRelativeLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PopRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PopRelativeLayout(Context context) {
		super(context);
	}
	
	

}
