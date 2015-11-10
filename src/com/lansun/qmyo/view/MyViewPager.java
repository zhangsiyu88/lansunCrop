package com.lansun.qmyo.view;

import com.lansun.qmyo.view.MyListView.YScrollDetector;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.AbsListView;

public class MyViewPager extends ViewPager {

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	public MyViewPager(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		return super.onInterceptTouchEvent(ev)&& mGestureDetector.onTouchEvent(ev);
	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (distanceY != 0 && distanceX != 0) {
				requestDisallowInterceptTouchEvent(false);
			}
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				requestDisallowInterceptTouchEvent(false);
				return false;
			}
			return true;
		}

	}

}