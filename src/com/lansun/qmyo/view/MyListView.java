package com.lansun.qmyo.view;

import com.umeng.socialize.utils.Log;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class MyListView extends ListView implements OnScrollListener {
	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	public MyListView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	@SuppressWarnings("deprecation")
	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		/*return super.onInterceptTouchEvent(ev)&& mGestureDetector.onTouchEvent(ev);*/
		return super.onInterceptTouchEvent(ev);//由原始监听器来解决问题，不使用另外写的手势监听
	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (distanceY != 0 && distanceX != 0) {
			}
			
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				requestDisallowInterceptTouchEvent(true);
				return true;
			}
			return false;
		}

	}
	
	//ListView本身还实现了一个滚动监听
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
}
