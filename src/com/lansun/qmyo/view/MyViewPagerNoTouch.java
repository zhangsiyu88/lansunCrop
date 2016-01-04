package com.lansun.qmyo.view;

import com.lansun.qmyo.view.MyListView.YScrollDetector;
import com.umeng.socialize.utils.Log;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.AbsListView;

public class MyViewPagerNoTouch extends ViewPager {

	public MyViewPagerNoTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	public MyViewPagerNoTouch(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		
//		return super.onInterceptTouchEvent(ev)&& mGestureDetector.onTouchEvent(ev);
//	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
			
			if (distanceY != 0 && distanceX != 0) {
				/*requestDisallowInterceptTouchEvent(false);*/
				
				requestDisallowInterceptTouchEvent(true);//请求父控件不要拦截触摸操作                                                    //------------->by Yeun 11.13
			}
			
			
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				requestDisallowInterceptTouchEvent(false);
				Log.d("sliding","MyViewPager中的纵向位移大于横向位移");
				return false;
			}																			   //------------->by Yeun 11.13
			if (Math.abs(distanceY) <= Math.abs(distanceX)) {
				requestDisallowInterceptTouchEvent(true );
				Log.d("sliding","MyViewPager中的横向位移大于纵向位移");
				return true;
			}
			return true;
		}

	}
  
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return false;
	}
	
}