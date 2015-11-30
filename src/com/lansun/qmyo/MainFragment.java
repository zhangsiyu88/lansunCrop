package com.lansun.qmyo;

import java.util.ArrayList;

import com.amap.api.mapcore2d.fa;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;

import com.lansun.qmyo.R.drawable;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.FoundFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.utils.gooview.GooViewListener;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MainViewPager;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;




public class MainFragment extends Fragment {
	
	public LayoutInflater inflater;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private  Activity activity;
	private int mPosition = Integer.MAX_VALUE;
	
	public MainFragment(int position){
		this.mPosition = position ;
	}
	public MainFragment(){
		
	}
	
	
	@InjectAll
	Views v;
	private MainViewPager vp_mainfrag;
	private ArrayList<BaseFragment> fragList;
	private TextView point;
	private boolean  visiable;
	private MainFragmentBroadCastReceiver broadCastReceiver;
	private IntentFilter filter;
	class Views {
		@InjectBinder(method = "click", listeners = OnClick.class)
		private RelativeLayout fl_home_top_menu, rl_top_r_top_menu, rl_bg,rl_top_bg;
		
		@InjectBinder(method = "click", listeners = OnClick.class)
		private View bottom_secretary, bottom_found, bottom_mine,bottom_home;
		private TextView tv_home_icon, tv_secretary_icon, tv_found_icon, tv_mine_icon,
		                 tv_home_experience,tv_top_home_experience;
		private RecyclingImageView iv_home_icon,iv_secretary_icon, iv_found_icon, iv_mine_icon;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		inflater = LayoutInflater.from(getActivity());
    }
	
	/**
	 * 加载时最先运行
	 * 首先拿到查询的参数,这个参数是从下一个页面传递过来的主要为了保存和搜索
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager=getChildFragmentManager();
		
		broadCastReceiver = new MainFragmentBroadCastReceiver();
		System.out.println("MainFragment中注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.ChangeTheLGPStatus");
		getActivity().registerReceiver(broadCastReceiver, filter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mainfrag, container,false);
		vp_mainfrag = (MainViewPager) rootView.findViewById(R.id.vp_mainfrag);
		point = (TextView) rootView.findViewById(R.id.point);
		Handler_Inject.injectFragment(this, rootView);
		initView(rootView);
		initFrag();
		
		if(mPosition != Integer.MAX_VALUE){
			//vp_mainfrag.setCurrentItem(mPosition);
			
			//底部导航栏的button颜色对应变换
			switch (mPosition) {
			case 0:
				click(v.bottom_home);
				break;
			case 1:
				click(v.bottom_secretary);
				break;
			case 2:
				click(v.bottom_found);
				break;
			case 3:
				click(v.bottom_mine);
				break;
			}
		}
		return rootView;
	}
	
	/**
	 * onCreatView中调用此方法
	 */
	private void initView(View rootView) {
//			//1.获取消息页发来的广播，根据广播内容得知底部的小绿点是否显示
//		
//			//2.若显示需给其设置上触摸滑动监听
//	    if(true){
//	    	visiable = point.getVisibility()==0;
//	    }
//				
//		if (visiable) {
//			point.setText("");
//			point.setTag(0);
//			
//			/*
//			 * 初始化监听者，方便对圆形按钮进行GooViewListener
//			 * 之随意在这一步就进行初始化，谁拿到数据谁进行初始化任务，另外从时效原则上来说也是必须的
//			 */
//			GooViewListener mGooListener = new GooViewListener(activity, point) {
//				@Override
//				public void onDisappear(PointF mDragCenter) {
//					super.onDisappear(mDragCenter);
//					activity.runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							CustomToast.show(activity, "提示", "所有消息置为已读");
//						}
//					});
//					/*Utils.showToast(mContext,"Cheers! We have get rid of it!");*/
//				}
//				@Override
//				public void onReset(boolean isOutOfRange) {
//					super.onReset(isOutOfRange);
//					/*Utils.showToast(mContext,isOutOfRange ? "Are you regret?" : "Try again!");*/
//				}
//			};
//			//为这个绿色小点设置触摸监听,为了使RedCircleButton被触摸时进行属于自己的动画表示
//			point.setOnTouchListener(mGooListener);
//		}
		
		
		
		}
	
	/**
	 * onCreatView中调用此方法
	 */
	private void initFrag(){
		fragList = new ArrayList<BaseFragment>();
		fragList.add(new HomeFragment());
		fragList.add(new SecretaryFragment());
		fragList.add(new FoundFragment());
		fragList.add(new MineFragment());
		
		vp_mainfrag.setOffscreenPageLimit(4);
		vp_mainfrag.setAdapter(new MyFragAdapter(manager));
		//vp_mainfrag.setAdapter(new MyFragAdapter(manager));
		vp_mainfrag.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	/**
	 * 以注解的形式拿到所有的xml布局中的控件
	 */
	@InjectInit
	private void init() {
		/* 这个方法会使得键盘弹起
		 * InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/
		initData();
		//刚进来时，需要将第一个button设置为  着色状态
		//v.iv_home_icon.setPressed(true);//底部的首页定位button
		v.iv_home_icon.setBackgroundResource(R.drawable.bottom_press_1);
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green1));
	}
	private void initData() {
		
	}
	
	private void click(View view) {
		/*EventBus bus = EventBus.getDefault();
		Fragment fragment = null;
		FragmentEntity entity = new FragmentEntity();*/
		switch (view.getId()) {
		case R.id.bottom_home:
			//vp_mainfrag.setCurrentItem(0);
			
			vp_mainfrag.setCurrentItem(0,false);
			//setBottomIconColor(v.iv_home_icon,v.iv_secretary_icon, v.iv_found_icon, v.iv_mine_icon);
			setBottomTextColor(v.tv_home_icon,v.tv_secretary_icon, v.tv_found_icon, v.tv_mine_icon);
			setBottomIconColorForced(v.iv_home_icon,v.iv_secretary_icon, v.iv_found_icon, v.iv_mine_icon,
					R.drawable.bottom_press_1,R.drawable.bottom_2,R.drawable.bottom_3,R.drawable.bottom_4);
			
			break;
		case R.id.bottom_secretary:
			//vp_mainfrag.setCurrentItem(1);
			vp_mainfrag.setCurrentItem(1,false);
			//setBottomIconColor(v.iv_secretary_icon,v.iv_home_icon, v.iv_found_icon, v.iv_mine_icon);
			setBottomTextColor(v.tv_secretary_icon,v.tv_home_icon, v.tv_found_icon, v.tv_mine_icon);
			setBottomIconColorForced(v.iv_secretary_icon,v.iv_home_icon, v.iv_found_icon, v.iv_mine_icon,
					R.drawable.bottom_press_2,R.drawable.bottom_1,R.drawable.bottom_3,R.drawable.bottom_4);
			break;
		case R.id.bottom_found:
			//vp_mainfrag.setCurrentItem(2);
			vp_mainfrag.setCurrentItem(2,false);
			//setBottomIconColor(v.iv_found_icon,v.iv_secretary_icon,v.iv_home_icon, v.iv_mine_icon);
			setBottomTextColor(v.tv_found_icon,v.tv_secretary_icon, v.tv_home_icon, v.tv_mine_icon);
			setBottomIconColorForced(v.iv_found_icon,v.iv_secretary_icon,v.iv_home_icon, v.iv_mine_icon,
					R.drawable.bottom_press_3,R.drawable.bottom_2,R.drawable.bottom_1,R.drawable.bottom_4);
			break;
		case R.id.bottom_mine:
			//vp_mainfrag.setCurrentItem(3);
			vp_mainfrag.setCurrentItem(3,false);
			//setBottomIconColor(v.iv_mine_icon,v.iv_home_icon, v.iv_found_icon, v.iv_secretary_icon);
			setBottomTextColor(v.tv_mine_icon,v.tv_secretary_icon, v.tv_found_icon,v.tv_home_icon);
			setBottomIconColorForced(v.iv_mine_icon,v.iv_home_icon, v.iv_found_icon, v.iv_secretary_icon,
					R.drawable.bottom_press_4,R.drawable.bottom_1,R.drawable.bottom_3,R.drawable.bottom_2);
			break;
		}
		/*if (fragment != null) {
			entity.setFragment(fragment);
			bus.post(entity);
		}*/
	}
	
	private void setBottomTextColor(TextView v1, TextView v2,TextView v3, TextView v4) {
		((TextView) v1).setTextColor(getResources().getColor(R.color.app_green1));
		((TextView) v2).setTextColor(getResources().getColor(R.color.text_nomal));
		((TextView) v3).setTextColor(getResources().getColor(R.color.text_nomal));
		((TextView) v4).setTextColor(getResources().getColor(R.color.text_nomal));
	}
	private void setBottomIconColor(View v1,View v2,View v3,View v4) {
		v2.setPressed(false);
		v3.setPressed(false);
		v4.setPressed(false);
		v1.setPressed(true);
	}
	/**
	 * 强制设置图标的背景图片
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param v1ResourceId
	 * @param v2ResourceId
	 * @param v3ResourceId
	 * @param v4ResourceId
	 */
	private void setBottomIconColorForced(View v1,View v2,View v3,View v4,
			 int v1ResourceId,int v2ResourceId,int v3ResourceId,int v4ResourceId) {
		v1.setBackgroundResource(v1ResourceId);
		v2.setBackgroundResource(v2ResourceId);
		v3.setBackgroundResource(v3ResourceId);
		v4.setBackgroundResource(v4ResourceId);
	}

	
	class MyFragAdapter extends FragmentPagerAdapter{

		public MyFragAdapter(FragmentManager fm) {
			super(fm);
		}
		
		
	/*	@SuppressWarnings("deprecation")
		@Override
		public Object instantiateItem(View container, int position) {
			
			destroyItem(container, position, fragList.get(position));
			return fragList.get(position);
			//return super.instantiateItem(container, position);
		}*/
		
		
		@Override
		public Fragment getItem(int position) {
			return fragList.get(position);
		}
		@Override
		public int getCount() {
			return fragList.size();
		}
		
//		@Override
//		public void destroyItem(ViewGroup container, int position, Object object) {
//			super.destroyItem(container, position, object);
//		}
	}
	
	
	class  MyFragStatusAdapter extends  FragmentStatePagerAdapter{

		public MyFragStatusAdapter(FragmentManager fm) {
			super(fm);
		}
		
		

		@Override
		public Fragment getItem(int position) {
			return fragList.get(position);
		}

		@Override
		public int getCount() {
			return fragList.size();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, fragList.get(position));
		}
	}
	
	
	
	/**
	 * 继承自PagerAdapter的MyAdapter
	 */
	class MyAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return fragList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			/**
			 * 创建view
			 * if(position==1){
			 * 	view.inflage
			 * if(position==2){
			 * 	view.inflage
			 * 
			 * 更新view
			 * if(position==1){
			 * 	更新ui
			 * if(position==2){
			 * 	更新UI
			 * 
			 * BasePager pager = pagers.get(position);
			 * pager.initData();
			 * container.addView(pager.rootview);
			 * return pager.rootview;
			 */
			BaseFragment frag = fragList.get(position);
		    container.addView(frag.getView());
			return frag.getView();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
	
	
	class MyOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			
		}

		@Override
		public void onPageSelected(int position) {
			// 避免ViewPager的预加载功能导致用户浪费流量，在这初始化数据 
			/*fragList.get(position).initData();*/
			
			//fragList.get(position).getView();
			if(position==0){
				setBottomTextColor(v.tv_home_icon,v.tv_secretary_icon, v.tv_found_icon, v.tv_mine_icon);
				setBottomIconColor(v.iv_home_icon,v.iv_secretary_icon, v.iv_found_icon, v.iv_mine_icon);
			}else if(position==1){
				setBottomIconColor(v.iv_secretary_icon,v.iv_home_icon, v.iv_found_icon, v.iv_mine_icon);
				setBottomTextColor(v.tv_secretary_icon,v.tv_home_icon, v.tv_found_icon, v.tv_mine_icon);
			}else if(position==2){
				setBottomIconColor(v.iv_found_icon,v.iv_secretary_icon,v.iv_home_icon, v.iv_mine_icon);
				setBottomTextColor(v.tv_found_icon,v.tv_secretary_icon, v.tv_home_icon, v.tv_mine_icon);
			}else if(position==3){
				setBottomIconColor(v.iv_mine_icon,v.iv_home_icon, v.iv_found_icon, v.iv_secretary_icon);
				setBottomTextColor(v.tv_mine_icon,v.tv_secretary_icon, v.tv_found_icon,v.tv_home_icon);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			
		}
		
	}
	
class MainFragmentBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.ChangeTheLGPStatus")){
				
				
			System.out.println("首页收到更新绿色小圆点的广播了");
			//1.获取消息页发来的广播，根据广播内容得知底部的小绿点是否显示
				
			//2.若显示需给其设置上触摸滑动监听
		    if(true){
		    	/*visiable = point.getVisibility()==0;*/
		    	visiable = true;
		    	point.setVisibility(View.VISIBLE);
		    }
					
			if (visiable) {
				point.setText("");
				point.setTag(0);
				
				/*
				 * 初始化监听者，方便对圆形按钮进行GooViewListener
				 * 之随意在这一步就进行初始化，谁拿到数据谁进行初始化任务，另外从时效原则上来说也是必须的
				 */
				GooViewListener mGooListener = new GooViewListener(activity, point) {
					@Override
					public void onDisappear(PointF mDragCenter) {
						super.onDisappear(mDragCenter);
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								CustomToast.show(activity, "提示", "所有消息置为已读");
							}
						});
						/*Utils.showToast(mContext,"Cheers! We have get rid of it!");*/
					}
					@Override
					public void onReset(boolean isOutOfRange) {
						super.onReset(isOutOfRange);
						/*Utils.showToast(mContext,isOutOfRange ? "Are you regret?" : "Try again!");*/
					}
				};
				//为这个绿色小点设置触摸监听,为了使RedCircleButton被触摸时进行属于自己的动画表示
				point.setOnTouchListener(mGooListener);
			}
				
				
			}else if(intent.getAction().equals("com.lansun.qmyo.DeleteTheLGPStatus")){
				
				point.setVisibility(View.GONE);//将底部的小绿点关掉不显示
				
			}
		}
	 }

		@Override
		public void onDestroy() {
			getActivity().unregisterReceiver(broadCastReceiver);
			super.onDestroy();
		}
}
