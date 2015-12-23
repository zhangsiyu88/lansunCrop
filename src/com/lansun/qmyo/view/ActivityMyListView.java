package com.lansun.qmyo.view;

import java.text.SimpleDateFormat;
import java.util.Date;


import com.lansun.qmyo.R;
import com.lansun.qmyo.utils.LogUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.socialize.utils.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMyListView extends ListView  {//implements OnScrollListener
	private GestureDetector mGestureDetector;
	private int downY = -1;
	private View header;
	private int headerHeight;
	private View secondHeader;// 记录轮播图这个View
	private View mFirstItem;
	private boolean isLastItemDisplay = true;
	private OnRefreshListener mListener;// 记录外界设置的Listener
	private static final int PULLDOWN_REFRESH = 1;// 下拉刷新
	private static final int RELEASE_REFRESH = 2;// 松开刷新
	private static final int REFRESHING = 3;// 正在刷新
	
	private static final int PULLUP_REFRESH = 4;// 下拉加载
	private static final int RELEASE_LOADING = 5;// 松开加载
	private static final int LOADING = 6;// 正在加载
	
	
	
	
	private static int CURRENT_STATE = PULLDOWN_REFRESH;// 当前刷新
	@ViewInject(R.id.iv_refresh_state)
	private TextView iv_refresh_state;// 下拉刷新状态
	@ViewInject(R.id.iv_refresh_arrow)
	private ImageView iv_refresh_arrow;// 下拉刷新箭头
	@ViewInject(R.id.pb_refresh_progress)
	private ProgressBar pb_refresh_progress;// 下拉刷新进度条
	@ViewInject(R.id.tv_refresh_time)
	private TextView tv_refresh_time;// 更新时间
	
	
	@ViewInject(R.id.tv_loadingmore)
	private TextView tv_loadingmore;// 上拉加载刷新文字
	@ViewInject(R.id.iv_loadingarrow)
	private ImageView iv_loadingarrow;// 上拉加载的箭头
	@ViewInject(R.id.pb_loading)
	private ProgressBar pb_loading;// 下拉刷新进度条
	
	
	@ViewInject(R.id.iv_open_eyes)
	private ImageView iv_open_eyes;
	@ViewInject(R.id.iv_close_eyes)
	private ImageView iv_close_eyes;
	

	private Handler handler = new Handler();
	private RotateAnimation up;
	private RotateAnimation down;
	private View footer;
	private int footerHeight;
	
	View.OnTouchListener mGestureListener;
	private boolean downRefresh;
	private boolean upLoading;
	private boolean mNoHeader = false;

	public ActivityMyListView(Context context) {
		super(context);
//		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		initHeader();
		initFooter();
	}
	
	public ActivityMyListView(Context context,boolean isNoHaeader) {
		super(context);
//		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		initHeader();
		initFooter();
		this.mNoHeader = isNoHaeader;
	}

	@SuppressWarnings("deprecation")
	public ActivityMyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		initHeader();
		initFooter();
		
	}

	public ActivityMyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		initHeader();
		initFooter();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);//由原始监听器来解决问题，不使用另外写的手势监听
		
//		switch (ev.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				if(CURRENT_STATE==PULLDOWN_REFRESH){
//				     return true;
//				}
//			break;
//		}
//		return false;
	}

//	class YScrollDetector extends SimpleOnGestureListener {
//		@Override
//		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//			if (distanceY != 0 && distanceX != 0) {
//			}
//			
//			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
//				requestDisallowInterceptTouchEvent(true);
//				return true;
//			}
//			return false;
//		}
//
//	}
//	
//	//ListView本身还实现了一个滚动监听
//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
//	}
//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		
//		
//	}

	/*@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}*/
	

	public void addSecondHeader(View view) {
		this.addHeaderView(view);
		this.secondHeader = view;
	}

	public void setNoHeader(boolean isNoHeader){
		if(isNoHeader){
			mNoHeader = true;
		}else{
			mNoHeader = false;
		}
	}
	private void initFooter() {
		footer = View.inflate(getContext(), R.layout.refresh_test_footer, null);
		
		ViewUtils.inject(this, footer);
		
		footer.measure(0, 0);
		footerHeight = footer.getMeasuredHeight();
		/*footer.setPadding(0, -footerHeight, 0, 0);*/
		
		footer.setPadding(0, 0, 0, 0);
		
		this.addFooterView(footer);
		this.setOnScrollListener(new MyOnScrollListener());
	}

	private void initHeader() {
		header = View.inflate(getContext(), R.layout.refresh_test_header, null);
		
		ViewUtils.inject(this, header);
		
		// 测量头布局
		header.measure(0, 0);
		// 获取头布局的高度
		headerHeight = header.getMeasuredHeight();
		// 隐藏头布局
		header.setPadding(0, -headerHeight, 0, 0);
		this.addHeaderView(header);
		iv_refresh_state.setText("下拉刷新");

		up = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		up.setDuration(500);
		up.setFillAfter(true);
		down = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down.setDuration(500);
		down.setFillAfter(true);
		
//		test();
	}

	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			
//			if(CURRENT_STATE==PULLDOWN_REFRESH){
//			     return true;
//				}
//			
			if(CURRENT_STATE==REFRESHING){
				break;
				//return true;
			}
//			if(CURRENT_STATE==RELEASE_REFRESH){
//				return true;
//			}
			
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			/* 这段代码实际上保证了在move监听时，要求其头部的轮播图必须显示，否则触摸事件无效，不会产生刷新作用
			 * if (!isSecondHeaderDisplay()) {
				// 只有轮播图完全显示时，才处理下拉刷新动作
				break;
			}*/
			if(mNoHeader){
				break;
			}
			
			if(!isFirstItemDisplay()){//如果ListView中的第一个条目没有被展示出来时，不响应触摸的事件
				break;
			}
			
//			if(!isFirstItemDisplay()){
//				downRefresh = false;
//			}else{
//				downRefresh = true;
//				//upLoading = false;
//			}
//			
//			if(!isLastItemDisplay){               //最后一个条目未显示时，取消所有的滑动响应        //TODO
//				upLoading = false;
//			}else{								  //最后一个条目显示，开始响应手指上滑的操作
//				upLoading = true;
//			}
			
			if (CURRENT_STATE == REFRESHING) {
				// 正在刷新时，不能再处理刷新动作
				//return true;
				break;
			}

			if (downY == -1) {// 如果down事件丢失，重新赋值
				downY = (int) ev.getY();
			}
			int moveY = (int) ev.getY();
			
			int diffY = moveY - downY;// 手指移动的距离
			
			LogUtils.toDebugLog("touchDistance", "滑动距离为："+diffY);
			
			if (diffY > 0 ) {//&&downRefresh
				upLoading = false;
				int topPadding = diffY/3 - headerHeight;
				if (topPadding >= 0 && CURRENT_STATE != RELEASE_REFRESH) {
					
					System.out.println("进入松开刷新状态");
					
					CURRENT_STATE = RELEASE_REFRESH;
					updateRefreshState(CURRENT_STATE);
				} else if (topPadding < 0 && CURRENT_STATE != PULLDOWN_REFRESH) {
					
					System.out.println("进入下拉刷新状态");
					
					CURRENT_STATE = PULLDOWN_REFRESH;
					updateRefreshState(CURRENT_STATE);
				}
				header.setPadding(0, topPadding, 0, 0);
				
				//注意：  下面这段代码要取消掉，避免ListView的OnItemClickListener和下拉触摸中的DOWN事件 被混淆
				/*return true;// 消费事件*/				
				
			}
			
//			else if(diffY < 0&& upLoading){													//注意：此处是手指上滑引发的触摸事件
//				downRefresh = false;
//				LogUtils.toDebugLog("touchDistance", "走到上滑事件中来");
//				int bottomPadding = diffY + footerHeight;
//				if(bottomPadding > 0 && CURRENT_STATE != PULLUP_REFRESH){  //底部还没有展示出来，仍在上拉加载的提示效果中
//					System.out.println("提示仍是上拉加载更多的过程");
//					
//					CURRENT_STATE = PULLUP_REFRESH;
//					updateRefreshState(CURRENT_STATE);
//					
//				}else if(bottomPadding<0 && CURRENT_STATE != RELEASE_LOADING){//将整个底部已经展示出来
//					System.out.println("提示为松开加载更多的过程");
//					
//					CURRENT_STATE = RELEASE_LOADING;
//					updateRefreshState(CURRENT_STATE);
//				}
//				footer.setPadding(0,0,0, bottomPadding);
//				return true;
//			}
			break;
		case MotionEvent.ACTION_UP:
			downY = -1;
			if (CURRENT_STATE == RELEASE_REFRESH) {
				
				CURRENT_STATE = REFRESHING;
				System.out.println("进入正在刷新状态");
				header.setPadding(0, 0, 0, 0);// 正在刷新状态
				updateRefreshState(CURRENT_STATE);
				/* 重复了刷新操作*/
				  if (mListener != null) {
					  
					/*
					 * 虽然强制将尾部监听去访问服务器的操作屏蔽掉(isLoadMore置为true)，
					 * 但当刷新操作完成的时候，执行isLoadMoreFished()，此方法中将会重新开启底部的监听操作(isLoadMore重新置回为false) 
					 * 
					 * 操作缘由：否则会造成刷新出来的列表和之前列表的下一页数据串在一起进行展示
					 */
					setAllowTailLoad(false);
					
					mListener.onRefreshing();
					//此处注掉后，之所以不影响后面的重新刷新操作，是因为在onRefreshFinished()中执行了状态回归的： CURRENT_STATE = PULLDOWN_REFRESH;
					//CURRENT_STATE = PULLDOWN_REFRESH;
				}
			} else if (CURRENT_STATE == PULLDOWN_REFRESH) {
				header.setPadding(0, -headerHeight, 0, 0);// 隐藏头布局
				
			}
//			else if (CURRENT_STATE == RELEASE_LOADING) {
//				CURRENT_STATE = LOADING;
//				System.out.println("进入正在加载状态");
//				
//				footer.setPadding(0, 0, 0, 0);// 正在加载状态
//				updateRefreshState(CURRENT_STATE);
//				 if (mListener != null) {
//						mListener.onLoadingMore();//出现加载更多后，立即将其缩回
//						CURRENT_STATE = PULLUP_REFRESH;
//					}
//				
//			}else if(CURRENT_STATE == PULLUP_REFRESH){
//				footer.setPadding(0, -footerHeight, 0, 0);//隐藏尾
//			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}



	// 对外提供接口
	public interface OnRefreshListener {
		void onRefreshing();
		void onLoadingMore();
	}

	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mListener = listener;
	}

	// 下拉刷新完成，回归的初始状态
	public void onRefreshFinshed(boolean isSuccess) {
		
		//在此处，对当前状态重置为等待下拉刷新的状态，保证了状态回归
		CURRENT_STATE = PULLDOWN_REFRESH;
		
		header.setPadding(0, -headerHeight, 0, 0);
		iv_refresh_state.setText("下拉刷新");
		iv_refresh_arrow.setVisibility(View.VISIBLE);
		pb_refresh_progress.setVisibility(View.INVISIBLE);
		if (isSuccess) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String date = format.format(new Date()).toString();
			tv_refresh_time.setText("最后刷新时间:" + date);
		} else {
			Toast.makeText(getContext(), "刷新数据失败", 0).show();
		}
	}

	private void updateRefreshState(int currentState) {
		switch (currentState) {
		case PULLDOWN_REFRESH:
			iv_refresh_arrow.startAnimation(down);
			iv_refresh_state.setText("下拉刷新");
			iv_refresh_arrow.setVisibility(View.VISIBLE);
			pb_refresh_progress.setVisibility(View.INVISIBLE);
			setEyesClose();
			break;
		case RELEASE_REFRESH:
			setEyesOpen();
			iv_refresh_arrow.startAnimation(up);
			iv_refresh_state.setText("松开刷新");
			break;
		case REFRESHING:
			iv_refresh_arrow.clearAnimation();
			iv_refresh_arrow.setVisibility(View.INVISIBLE);
			pb_refresh_progress.setVisibility(View.VISIBLE);
			iv_refresh_state.setText("正在刷新");
			break;
			
		case PULLUP_REFRESH:
			iv_loadingarrow.startAnimation(down);
			tv_loadingmore.setText("上拉加载更多");
			iv_loadingarrow.setVisibility(View.VISIBLE);
			pb_loading.setVisibility(View.GONE);
			
			break;
		case RELEASE_LOADING:
			iv_loadingarrow.startAnimation(up);
			tv_loadingmore.setText("松开加载更多");
			pb_loading.setVisibility(View.GONE);
			break;
			
		case LOADING:
			iv_loadingarrow.clearAnimation();
			iv_loadingarrow.setVisibility(View.INVISIBLE);
			pb_loading.setVisibility(View.VISIBLE);
			tv_loadingmore.setText("加载更多中");
			break;

			
			
		default:
			break;
		}
	}

	

	/**
	 * 适用于带轮播图的ListView对象
	 * @return
	 */
	private boolean isSecondHeaderDisplay() {
		int[] location = new int[2];// 用来存储X轴和Y轴
		this.getLocationOnScreen(location);
		int listViewLocY = location[1];// 获取listView的Y轴
		secondHeader.getLocationOnScreen(location);
		// 获取secondHeader的Y轴
		int secondHeaderLocY = location[1];
		return secondHeaderLocY >= listViewLocY;
	}
	
	
	
	private boolean isLoadMore = false;
	
	private View firstItem;
	private View lastItem;
	
	public void onLoadMoreFished(){
		isLoadMore = false;
		//footer.setPadding(0, -footerHeight, 0, 0);// 隐藏加载更多布局
		footer.setPadding(0, 0, 0, 0);//结束后仍然为零
		
	}
	
	public void onLoadMoreOverFished(){
		/*isLoadMore = false;*/
		isLoadMore = true;
		footer.setPadding(0, -footerHeight, 0, 0);// 隐藏加载更多布局
		
	}
	
	/**
	 * 设置是否允许底部监听滑动到底部，进行加载下一页的操作
	 * @param b
	 * b = true 设置尾部可监听，并执行加载操作                    -->isLoadMore = false;
	 * b = false 尾部监听，但不执行请求下一页的加载操作   -->isLoadMore = true;
	 */
	public void setAllowTailLoad(boolean b){
		isLoadMore = !b;
	}
	
	class MyOnScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (OnScrollListener.SCROLL_STATE_IDLE == scrollState|| 
					OnScrollListener.SCROLL_STATE_FLING == scrollState) {
				// 判断当前Listview最下面一条是不是最后一条数据
				if(getLastVisiblePosition()==getCount()-1&&!isLoadMore){
					System.out.println("加载更多");
					isLoadMore = true;
					
					//footer.setPadding(0, 0, 0, 0);
					
					
//					setSelection(getCount());
					//强行将其只显示在footerView上面的一个Item上，即是数据列表的最后一个Item
					setSelection(getCount());
					
					if(mListener!=null){
						mListener.onLoadingMore();
					}
					
//					isLastItemDisplay = true;
				}else{
					System.out.println("滑动中");
				}
				
//				if(getLastVisiblePosition()!=getCount()-1&&!isLoadMore){
//					isLastItemDisplay = false;
//				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	}

	/**
	 * 检测ListView的第一个条目是否显示出来
	 * @return
	 */
	private boolean isFirstItemDisplay() {
		int[] location = new int[2];// 用来存储X轴和Y轴
		this.getLocationOnScreen(location);
		int listViewLocY = location[1];// 获取listView的Y轴
		//LogUtils.toDebugLog("location", "listViewLocY:"+location[1]);
		
		/*View itemAtPosition = (View) getItemAtPosition(0);
		itemAtPosition.getLocationOnScreen(location);*/
		
		firstItem = getChildAt(0);
		firstItem.getLocationOnScreen(location);
		
		// 获取secondHeader的Y轴
		int firstItemLocY = location[1];
		//LogUtils.toDebugLog("location","firstItemLocY:"+ location[1]);
		return firstItemLocY >= listViewLocY;
	}
	
	
	
	private boolean isLastItemDisplay() {   //TODO
		int[] location = new int[2];// 用来存储X轴和Y轴
		int lastItemLocY ;
		//1.定位最后一个Item在屏幕中的位置
		if(getLastVisiblePosition() == getCount()-1){
			lastItem = this.getChildAt(getCount()-2);
			/*lastItem.getLocationOnScreen(location);*/
			lastItem.getLocationInWindow(location);
			lastItemLocY = location[1];
			
		}else{
			lastItemLocY = 1280;
		}
		//LogUtils.toDebugLog("location","lastItemLocY:"+ lastItemLocY);
		
		//2.定位ListView最底部在屏幕的位置
		this.getLocationOnScreen(location);
		int listViewLocY = location[1];// 获取listView的Y轴
		
		//LogUtils.toDebugLog("location", "listViewLocY:"+location[1]);
		
		//3.比较两者的位置差，当最后一个显示的时候，需要将
		
		return lastItemLocY > 1000;
	}
	
	
	/*public void addFirstItem(View firstItem) {
		this.mFirstItem = firstItem;
		int[] location = new int[2];// 用来存储X轴和Y轴
		firstItem.getLocationOnScreen(location);
		LogUtils.toDebugLog("location", location[1]+"  传入进来的location");
	}*/
	
	/**
	 * 刷新头左部图片
	 */
	public void  setEyesOpen(){//睁眼可见
		iv_close_eyes.setVisibility(View.GONE);
		iv_open_eyes.setVisibility(View.VISIBLE);
	}
	private void setEyesClose(){
		iv_close_eyes.setVisibility(View.VISIBLE);
		iv_open_eyes.setVisibility(View.GONE);
	}
}
