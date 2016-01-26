package com.lansun.qmyo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.blurry.Blurry;

import com.amap.api.mapcore2d.fa;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_File;
import com.android.pc.util.Handler_Inject;

import com.lansun.qmyo.R.drawable;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.FoundFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.utils.LogUtils;
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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;




public class MainFragment extends Fragment {
	
	public LayoutInflater inflater;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private  Activity activity;
	private int mPosition = Integer.MAX_VALUE;
	
	public Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				activity.sendBroadcast(new Intent("com.lansun.qmyo.checkMySecretary"));
				System.out.println("handlerde方法中点击私人秘书按钮时，MainFragment的  发送    提示检测私人秘书信息的广播了");
				break;
			case 10:
				activity.sendBroadcast(new Intent("com.lansun.qmyo.showFirstCommitSecretaryAskGuide"));
				System.out.println("handlerde方法中，MainFragment  发送    私人秘书信息页在第一次提问成功后弹引导页的广播了");
				break;
			}
		}

	};
	
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
	//private LinearLayout bottom;
	private RelativeLayout bottom;
	private LinearLayout bottom_menu;
	private View rootView;
	class Views {
		@InjectBinder(method = "click", listeners = OnClick.class)
		private RelativeLayout fl_home_top_menu, rl_top_r_top_menu, rl_bg,rl_top_bg;
		
		//执行click的操作
		@InjectBinder(method = "click", listeners = OnClick.class)
		private View bottom_secretary, bottom_found, bottom_mine,bottom_home;
		private TextView tv_home_icon, tv_secretary_icon, tv_found_icon, tv_mine_icon,
		                 tv_home_experience,tv_top_home_experience;
		private RecyclingImageView iv_home_icon,iv_secretary_icon, iv_found_icon, iv_mine_icon;
		
		private View line_bottom;
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
		
		 //进入活动详情时，就需要做好后面用户发表评论的准备工作了
		new Thread(new Runnable() {
			@Override
			public void run() {
				/**
				 * 此处第一步将db文件写入到内存缓冲区处
				 */
				try {
					/*if (!new File(activity.getCacheDir().getPath() +File.separator +"qmyo_sensitive.db").exists()) {
						Handler_File.writeFile(activity.getCacheDir().getPath()+File.separator+ "qmyo_sensitive.db", getResources().getAssets()
								.open("qmyo_sensitive.db"));
					}*/
					LogUtils.toDebugLog("数据库的路径", "run()方法    将敏感词写入到我们的内存上存起来");
					Handler_File.writeFile(activity.getCacheDir().getPath()+File.separator+ "qmyo_sensitive_new.db", getResources().getAssets()
							.open("qmyo_sensitive_new.db"));
				} catch (IOException e) {
					//读写异常
				}
			}
		}).start();
		
		if(savedInstanceState!= null){
			//vp_mainfrag = savedInstanceState.getParcelable("vp_mainfrag");
			/*
			 * 在退出后台时，并未进行下面数据的保存
			 * rootView = savedInstanceState.getParcelable("rootView");
				fragList = savedInstanceState.getParcelable("fragList");
				manager = savedInstanceState.getParcelable("manager");
				transaction = savedInstanceState.getParcelable("transaction");*/
		}
		manager=getChildFragmentManager();
		broadCastReceiver = new MainFragmentBroadCastReceiver();
		System.out.println("MainFragment中注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.ChangeTheLGPStatus");
		filter.addAction("com.lansun.qmyo.checkMySecretary");
		filter.addAction("com.lansun.qmyo.DeleteTheLGPStatus");
		filter.addAction("com.lansun.qmyo.hideTheBottomMenu");
		filter.addAction("com.lansun.qmyo.recoverTheBottomMenu");
		getActivity().registerReceiver(broadCastReceiver, filter);
		
		
		if(getArguments() != null){
		    LogUtils.toDebugLog("catch", "拿到了从task页面传过来的标示了");
			if(getArguments().getString("firstCommitAsk")=="true"){
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						handler.sendEmptyMessage(10);
					}
				}, 500);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_mainfrag, container,false);
		vp_mainfrag = (MainViewPager) rootView.findViewById(R.id.vp_mainfrag);
		point = (TextView) rootView.findViewById(R.id.point);
		bottom_menu =  (LinearLayout)rootView.findViewById(R.id.bottom_menu);
		bottom =  (RelativeLayout)rootView.findViewById(R.id.bottom);
		
		Handler_Inject.injectFragment(this, rootView);
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		initView(rootView);
		initFrag();
		
		/**
		 * 由其他页面跳转至主界面的四大页面中的指定页面时，执行下面的操作
		 */
		if(mPosition != Integer.MAX_VALUE){  
			//vp_mainfrag.setCurrentItem(mPosition);
			
			//底部导航栏的button颜色对应变换
			switch (mPosition) {
			case 0:
				click(v.bottom_home);
				break;
			case 1:
				click(v.bottom_secretary);
				/*
				 * 点击私人秘书页面时，发送广播通知秘书页进行访问，判断秘书信息是否已录入
				 */
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						handler.sendEmptyMessage(0);
					}
				}, 500);
				
				

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
		Blurry.delete((ViewGroup)rootView);
		
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
			/*
			 * 点击私人秘书页面时，发送广播通知秘书页进行访问，判断秘书信息是否已录入
			 */
			activity.sendBroadcast(new Intent("com.lansun.qmyo.checkMySecretary"));
			System.out.println("常规的click()方法中点击私人秘书按钮时，MainFragment的  发送    提示检测私人秘书信息的广播了");
			
			
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
				
			}else if(intent.getAction().equals("com.lansun.qmyo.checkMySecretary")){
				
				/*Blurry.with(getActivity())
	            .radius(25)
	            .sampling(2)
	            .async()
	            .animate(500)
	            .onto((ViewGroup)rootView);*/
				
			}else if(intent.getAction().equals("com.lansun.qmyo.hideTheBottomMenu")){
			
/*				AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
				alpha.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						Blurry.with(getActivity())
						.radius(25)
						.sampling(2)
						.async()
						.animate(500)
						.onto((ViewGroup)rootView);
						v.bottom_found.setVisibility(View.INVISIBLE);
						v.bottom_home.setVisibility(View.INVISIBLE);
						v.bottom_mine.setVisibility(View.INVISIBLE);
						v.bottom_secretary.setVisibility(View.INVISIBLE);
						v.line_bottom.setVisibility(View.INVISIBLE);
					}
				});
		        alpha.setDuration(500);
		        v.bottom_found.startAnimation(alpha);
		        v.bottom_home.startAnimation(alpha);
		        v.bottom_mine.startAnimation(alpha);
		        v.bottom_secretary.startAnimation(alpha);
		        v.line_bottom.startAnimation(alpha);*/
				
				
				Blurry.with(getActivity())
				.radius(25)
				.sampling(2)
				.async()
				.animate(10)
				.onto((ViewGroup)rootView);
				System.out.println("首页收到模糊整体背景的广播了");
				
		}else if(intent.getAction().equals("com.lansun.qmyo.recoverTheBottomMenu")){
//			AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
//	        alpha.setDuration(300);
//	        bottom_menu.startAnimation(alpha);
//			//bottom_menu.setVisibility(View.VISIBLE);
			
			Blurry.delete((ViewGroup)rootView);
			System.out.println("首页收到解除之前模糊整体背景的广播了");
/*			v.bottom_home.setVisibility(View.VISIBLE);
			v.bottom_found.setVisibility(View.VISIBLE);
			v.bottom_mine.setVisibility(View.VISIBLE);
			v.bottom_secretary.setVisibility(View.VISIBLE);
			v.line_bottom.setVisibility(View.VISIBLE);*/
	}
		}
	 }

		@Override
		public void onDestroy() {
			getActivity().unregisterReceiver(broadCastReceiver);
			super.onDestroy();
		}
		/**
		 * 存储当前控件被清除前那一瞬希望保留的内容
		 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
		 */
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			
			//outState.putParcelable("vp_mainfrag", vp_mainfrag);
			
			/* 
			 * 下面的变量都没有实现Praceable接口，不可被序列化保存和传递
			 * 
			 * outState.putParcelable("rootView", (Parcelable) rootView);
			outState.putParcelable("fragList", (Parcelable)fragList);
			outState.putParcelable("manager", (Parcelable)manager);
			outState.putParcelable("transaction", (Parcelable)transaction);*/
		}
}
