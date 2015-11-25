package com.lansun.qmyo.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MainViewPager extends ViewPager {


	/*private boolean isPagingEnabled = true;*/   
	private boolean isPagingEnabled = false;
	 
	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MainViewPager(Context context) {
		super(context);
	}

	 @Override
	    public boolean onTouchEvent(MotionEvent event) {
	        /*return this.isPagingEnabled && super.onTouchEvent(event);*/
		 return true;
	    }
	 
	    @Override
	    public boolean onInterceptTouchEvent(MotionEvent event) {
	        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
	        /*return true;*/
	    }
	 
	    public void setPagingEnabled(boolean b) {
	        this.isPagingEnabled = b;
	    }
}
