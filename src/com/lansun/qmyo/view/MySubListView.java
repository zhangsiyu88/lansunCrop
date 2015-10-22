package com.lansun.qmyo.view;

import com.android.pc.ioc.image.OnScrollLoaderListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * 固定宽高解决listview显示不全和滑动问题
 * 
 * @author bhxx
 * 
 */
public class MySubListView extends ListView {
	private int mLastMotionY;
	private ScrollView parentScrollView;
	private ListView parentListView;

	public MySubListView(Context context) {
		super(context);
	}

	@SuppressWarnings("deprecation")
	public MySubListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MySubListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setParentScrollView(ScrollView parentScrollView) {
		this.parentScrollView = parentScrollView;
	}

	public void setParentScrollView(ListView parentListView) {
		this.parentListView = parentListView;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:

			setParentScrollAble(true);
		case MotionEvent.ACTION_MOVE:
			setParentScrollAble(true);
			break;
		case MotionEvent.ACTION_UP:

		case MotionEvent.ACTION_CANCEL:
			setParentScrollAble(true);
			break;
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * @param flag
	 */
	private void setParentScrollAble(boolean flag) {
		if (parentScrollView != null) {
			parentScrollView.requestDisallowInterceptTouchEvent(!flag);
		}
		if (parentListView != null) {
			parentListView.requestDisallowInterceptTouchEvent(!flag);
		}
	}

	private int maxHeight;

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

}