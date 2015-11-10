package com.lansun.qmyo.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.HomeListAdapter;
import com.lansun.qmyo.adapter.HomePagerAdapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.newbrand.NewBrandFragment;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.AnimUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExperienceDialog;
import com.lansun.qmyo.view.ExperienceDialog.OnConfirmListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * 主界面
 * 
 * @author Yeun
 * 
 */
@SuppressLint("InflateParams") public class HomeFragment extends BaseFragment {

	@InjectAll
	Views v;
	private View head;
	private SearchAdapter adapter;
	private ImageView iv_point2;
	private ImageView iv_point, iv_home_ad;
	private View ll_activity_home_new, ll_activity_home_yhq;// 新品曝光
	private boolean isChina = true;
	public boolean mFromBankCardFragment = false;
	private boolean onlyOne = true;
	private boolean hasFooterview;
	private String nextPageUrl = "";

	/**
	 * LoonAndroid框架规定，上拉和下拉刷新只能针对ListView进行设置，其他的View类型不可识别
	 */
	@InjectView(binders = { @InjectBinder(listeners ={ OnItemClick.class }, method = "itemClick")})  //, pull = true
	private ListView lv_home_list;

	/* @InjectView(pull = true)
	private ScrollView  sv_homefrag;*/


	@InjectView
	private ScrollView sv_homefrag;

	/*@InjectView(pull = true)
	private  LinearLayout ll_homefrag;*/



	class Views {
		@InjectBinder(method = "click", listeners = OnClick.class)
		private RelativeLayout fl_home_top_menu, rl_top_r_top_menu, rl_bg,
		rl_top_bg;
		@InjectBinder(method = "click", listeners = OnClick.class)
		private View bottom_secretary, bottom_found, bottom_mine,
		iv_home_location, iv_top_location, ll_search;
		private TextView tv_home_location, tv_home_top_location;
		private RecyclingImageView iv_home_icon;
		private TextView tv_home_icon, tv_home_experience,
		tv_top_home_experience;
		private View iv_card, iv_top_card;
		@InjectBinder(method = "click", listeners = OnClick.class)
		private EditText et_home_search;
	}

	private View tv_home_search;

	private boolean isPlay = false;

	private ArrayList<HashMap<String, Object>> shopDataList = new ArrayList<HashMap<String, Object>>();
	public HomeFragment(boolean mIsFirstEnter) {
		mFromBankCardFragment = mIsFirstEnter;
	}

    public HomeFragment() {
		//Log.i("token", App.app.getData("access_token"));	
	}

	@Override
	public void onResume() {
		// imm.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus()
		// .getWindowToken(), 0);



		v.iv_home_icon.setPressed(true);//底部的首页定位button
		isPlay = false;
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green1));

		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);


		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(sv_homefrag.getWindowToken(), 0); 
		/*v.rl_bg.setPressed(true);
		v.rl_top_bg.setPressed(true);*/
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (adapter != null) {
			adapter = null;
		}
		if (promoteAdapter != null) {
			promoteAdapter = null;
		}
		isPlay = false;
		v.iv_home_icon.setPressed(true);
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green1));

		/*v.rl_bg.setPressed(true);
		v.rl_top_bg.setPressed(true);*/
	}

	/**
	 * 打开极文url
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void itemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {//args2 == position

		Fragment fragment;
		if (isChina) {//--> 选择是中国的地域情况下,打开活动详情页面
			/*HashMap<String, Object> data = shopDataList.get(arg2 - 1);*/
			//arg2的参数应为position，上步故意将位置向下偏移
			HashMap<String, Object> data = shopDataList.get(arg2);

			fragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("shopId", data.get("shopId").toString());
			args.putString("activityId", data.get("activityId").toString());

			String  shopId = args.getString("shopId");
			String  activityId = args.getString("activityId");
			Log.i("你点的位置上的Item","门店Id: "+shopId +"活动Id: "+activityId );
			fragment.setArguments(args);
		} else {
			HomePromoteData data = promoteList.getData().get(arg2 - 1);//----------------------这儿也需要注意，将来位置需要替换
			fragment = new PromoteDetailFragment();
			Bundle args = new Bundle();
			args.putSerializable("promote", data);
			fragment.setArguments(args);
		}
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		intent = new Intent("com.lansun.qmyo.fragment.newbrand");
		LayoutInflater inflater  = LayoutInflater.from(activity);
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		rootView = inflater.inflate(R.layout.activity_home, null, false);
		head =  rootView.findViewById(R.id.head_banner);
		Handler_Inject.injectFragment(this, rootView);//当前的fragment里面使用 自动去注入组件

		refresh_footer = inflater.inflate(R.layout.refresh_footer, null);

		sv_homefrag.scrollTo(0, 0);
		sv_homefrag.setSmoothScrollingEnabled(true);

		/*sv_homefrag.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new GetDataTask().execute();				
			}
		});*/
		/*sv_homefrag.setOnPullEventListener(new OnPullEventListener<ScrollView>(){

			@Override
			public void onPullEvent(PullToRefreshBase<ScrollView> refreshView,
					State state, Mode direction) {
				new GetDataTask().execute();
			}
		});*/

		
	
		//TODO 首页刷新的位置监听
		sv_homefrag.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:

					break;
				case MotionEvent.ACTION_MOVE:
					int scrollY = v.getScrollY();
					int height = v.getHeight();
					int measuredHeight = sv_homefrag.getChildAt(0).getMeasuredHeight();
					//Log.i("滑动中", "measuredHeight = sv_homefrag.getChildAt(1).getMeasuredHeight(): "+sv_homefrag.getChildAt(1).getMeasuredHeight());

					View childAt1 = sv_homefrag.getChildAt(0);
					int childAt1Height1 = childAt1.getLayoutParams().height;
					if(scrollY+height == measuredHeight){ // 滑出屏幕外的高度+ 当前距离屏幕的高度 =sv_homefag的实测高度 
						/*CustomToast.show(activity, "滑到底了", "到底了!");*/
						/*sv_homefrag.setOnTouchListener(null);*/
						if (list != null) {
							if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
								CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");
								sv_homefrag.setOnTouchListener(null);
								try{
									lv_home_list.removeFooterView(refresh_footer);
								}catch(Exception e){
									e.printStackTrace();
								}
								//在最后一页刷不出数据时，需要将底部的footerview去除掉
								lv_home_list.setAdapter(adapter);
								setListViewHeightBasedOnChildren(lv_home_list);

							} else {//下一页仍然拥有数据
								
								/*if(onlyOne){*/
									//只给ListView加一次FooterView
									/*onlyOne = false;*/
								/*	onlyOne = true;*/
								
								if(list.getNext_page_url()==nextPageUrl){
									
								}else{
									try{
										lv_home_list.removeFooterView(refresh_footer);
										//hasFooterview = false;
									}catch(Exception e){
									}
									
									lv_home_list.addFooterView(refresh_footer);
									//hasFooterview = true ;
									/*adapter.notifyDataSetChanged();*/		
									//setListViewHeightBasedOnChildrenByDataList(lv_home_list,shopDataList); 
									setListViewHeightBasedOnChildren(lv_home_list);
									
									refreshParams = new LinkedHashMap<>();
									if (isChina) {//如果是国内活动，需要进行刷新操作
										refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
										refreshParams.put("site", getSelectCity()[0]);
										refreshParams.put("intelligent", "home");
									} else {
										refreshParams = null;
										refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,getSelectCity()[0]);
									}
									
									refreshCurrentList(list.getNext_page_url(), refreshParams,1, lv_home_list);
									CustomToast.show(activity, "努力加载中", "总裁大大,稍等哟");
								    nextPageUrl = list.getNext_page_url();
								}
								
//									if(list.getNext_page_url()==nextPageUrl){
//										 //DoNothing!
//									}else{
//										refreshCurrentList(list.getNext_page_url(), refreshParams,1, lv_home_list);
//										CustomToast.show(activity, "努力加载中", "总裁大大,稍等哟");
//										nextPageUrl = list.getNext_page_url();
//									}
								}
							/*}*/
						}
					}
					break;
				case MotionEvent.ACTION_UP:

					break;	
				}



				return false;
			}
		});

		/*mScrollView = sv_homefrag.getRefreshableView();*/
		if (TextUtils.isEmpty(App.app.getData("exp_secret"))
				&& TextUtils.isEmpty(App.app.getData("secret"))
				&& !GlobalValue.isFirst){
			ExperienceDialog dialog = new ExperienceDialog();//这么个体验的对话框，需要单独在其内部设置点击响应事件
			//进来首先就弹出对话框
			dialog.setOnConfirmListener(new OnConfirmListener() {
				@Override
				public void confirm() {
					v.rl_bg.setPressed(true);//这是“体验”二字后面的背景绿色和灰色选择器，那么为了取消点击效果，则在此将选择器设置为  点击和非点击都为 统一效果
					v.rl_top_bg.setPressed(true);
					v.tv_home_experience.setVisibility(View.VISIBLE);
					v.tv_top_home_experience.setVisibility(View.VISIBLE);
					v.iv_card.setVisibility(View.GONE);
					v.iv_top_card.setVisibility(View.GONE);
				}
			});
			dialog.show(getFragmentManager(), "experience");
		}
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green2));

		if(mFromBankCardFragment){
			v.rl_bg.setPressed(true);//这是“体验”二字后面的背景绿色和灰色选择器，那么为了取消点击效果，则在此将选择器设置为  点击和非点击都为 统一效果
			v.rl_top_bg.setPressed(true);
			v.tv_home_experience.setVisibility(View.VISIBLE);
			v.tv_top_home_experience.setVisibility(View.VISIBLE);
			v.iv_card.setVisibility(View.GONE);
			v.iv_top_card.setVisibility(View.GONE);
			mFromBankCardFragment = false;//使者用完后，要马上清除
		}


		/*final SwipeRefreshLayout refreshlayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		//初始化刷新头
		final TextView tv = (TextView)refreshlayout.findViewById(R.id.textView1);

		Log.i("", refreshlayout==null?"为空":"不为空");
        //设置刷新时动画的颜色，可以设置4个
		refreshlayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
		refreshlayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                tv.setText("正在刷新");

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        tv.setText("刷新完成");
                        refreshlayout.setRefreshing(false);
                    }
                }, 6000);
            }
        });*/



		return rootView;
	}
	boolean canscoll = false;

	@InjectInit
	private void init() {
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green2));
		v.et_home_search.setFocusable(false);
		if (TextUtils.isEmpty(App.app.getData("access_token"))/*没有拿到access_Token,isFirst也为true的时候，则跳至体验搜索页*/
				&& GlobalValue.isFirst) {
			ExperienceSearchFragment fragment = new ExperienceSearchFragment();
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			return;

		} else {//可能我有access_token,可能我不是第一次进来
			v.et_home_search.setHint(R.string.please_enter_search_brand);
			
			if ("true".equals(App.app.getData("isExperience"))) {//我拿到了exp_sercret并且也拿到了转换后的access_token ,而且又添加了某张银行卡，那么我就成为了体验用户
				v.rl_bg.setPressed(true);
				v.rl_top_bg.setPressed(true);

				//强行将体验字体后面的背景设置为 绿色！
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_gray);
				v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_gray);

				v.tv_home_experience.setVisibility(View.VISIBLE);
				v.tv_top_home_experience.setVisibility(View.VISIBLE);
				v.iv_card.setVisibility(View.GONE);
				v.iv_top_card.setVisibility(View.GONE);

			} else {
				if(App.app.getData("isEmbrassStatus").equals("true")){
					SearchBankCardFragment searchBankCardFragment = new SearchBankCardFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean("isFromRegisterAndHaveNothing",true);//借用这个isFromRegisterAndHaveNothing的标签进行搜索的设置
					searchBankCardFragment.setArguments(bundle);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(searchBankCardFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}else{
					//如果不是体验用户,即我已是注册登录用户
					v.iv_card.setVisibility(View.VISIBLE);//原本右边银行卡可见
					v.iv_top_card.setVisibility(View.VISIBLE);//滑动出现的右边银行卡可见
					v.tv_home_experience.setVisibility(View.GONE);//原本  体验不可见
					v.tv_top_home_experience.setVisibility(View.GONE);//滑动出现的右边 体验二字 不可见
					v.rl_bg.setPressed(false);
					v.rl_top_bg.setPressed(false);
				}
			}

			if (!TextUtils.isEmpty(getSelectCity()[0])) {//设置左上角的城市名称
				v.tv_home_location.setText(getSelectCity()[1]);
				v.tv_home_top_location.setText(getSelectCity()[1]);
			}

			/*head = inflater.inflate(R.layout.activity_home_banner, null);*/


			/*尝试在head被充起来的瞬间就将其放到ListView的头上*/
			/*lv_home_list.addHeaderView(head, null, true);*/




			Log.i("","head的值为： "+head.toString());


			ViewTreeObserver vto = head.getViewTreeObserver();
			vto.addOnScrollChangedListener(new OnScrollChangedListener() {

				@Override
				public void onScrollChanged() {

					int[] location = new int[2];
					v.iv_home_location.getLocationInWindow(location);
					int hiddenY = location[1] + v.iv_home_location.getHeight();

					/**
					 * View searchView = lv_home_list.findViewById(R.id.ll_search);
					 * */

					View searchView = head.findViewById(R.id.ll_search);//在head这个View中拿到对应的搜索栏对象
					searchView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							SearchFragment fragment = new SearchFragment();
							FragmentEntity event = new FragmentEntity();
							event.setFragment(fragment);
							EventBus.getDefault().post(event);
						}
					});
					int currY = getLocation(searchView);
					if (currY == 0) {
						return;
					}
					if (hiddenY > currY + 66) {
						if (isPlay) {
							isPlay = false;
							Animation search_top_in = AnimationUtils.loadAnimation(activity, R.anim.home_top_in);
							AnimUtils.startTopInAnim(activity,v.fl_home_top_menu);

							v.rl_top_r_top_menu.setVisibility(View.GONE);
						}
					} else {
						if (!isPlay) {
							isPlay = true;
							startTopOutAnim(activity, v.fl_home_top_menu);
							v.rl_top_r_top_menu.setVisibility(View.VISIBLE);
						}
					}
				}
			});



			/* 优惠券的点击监听
			 */
			ll_activity_home_yhq = head.findViewById(R.id.ll_activity_home_yhq);
			ll_activity_home_yhq.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Fragment fragment = null;
					//所有的标示都是由user来表示
					if (GlobalValue.user == null) {
						/*fragment = new RegisterFragment();*/
						fragment = new MineCouponsFragment();
					} else {
						fragment = new MineCouponsFragment();
					}
					if (fragment != null) {
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
				}
			});

			/* 新品曝光的点击监听
			 */
			ll_activity_home_new = head.findViewById(R.id.ll_activity_home_new);
			ll_activity_home_new.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Fragment fragment=new NewBrandFragment();
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
			});


			iv_home_ad = (ImageView) head.findViewById(R.id.iv_home_ad);

			/* search栏的点击监听
			 */
			tv_home_search = head.findViewById(R.id.tv_home_search);
			tv_home_search.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					SearchFragment fragment = new SearchFragment();
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
			});


			InternetConfig config = new InternetConfig();
			config.setKey(0);
			/**
			 * 去获取数据内容,暂关闭依据定位地点获取数据的方法
			 FastHttpHander.ajaxGet(GlobalValue.URL_HOME_AD + getSelectCity()[0], config, this);*/
			Log.i("TAGTAGTAGTAGTAG", "准备向服务器发起请求，等待回执");
			FastHttpHander.ajaxGet(GlobalValue.URL_HOME_AD + 310000, config, this);//已去访问头部的数据
			Log.i("TAGTAGTAGTAGTAG", "已经向服务器发起请求，等待回执");
			//定值去访问页面顶头上的图


			refreshParams = new LinkedHashMap<String, String>();

			if (Character.isLetter(getSelectCity()[0].charAt(0))) {//-->目前是走不到的
				isChina = false;
				refreshParams = null;

				/*暂时关闭依据定位地点刷新底部列表的操作
				 * refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,getSelectCity()[0]);*/
				refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,310000);
				//当是极文的时候,需要加入个footerView
				View footView = inflater.inflate(R.layout.home_item_bottom,null);
				lv_home_list.addFooterView(footView);

			} else {
				refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
				/*refreshParams.put("location", "31.293688,121.524448");*/
				refreshParams.put("location", String.valueOf(GlobalValue.gps.getWgLat()+","+ GlobalValue.gps.getWgLon()));
				/* refreshParams.put("site", getSelectCity()[0]);*/
				//-----注意：我在这个地方手动设置了刷新列表的地点参数，需要注意在后面将其改回来
				/*refreshParams.put("site", "310000");*/ 

				refreshParams.put("site", getSelectCity()[0]);
				/*refreshParams.put("intelligent", "recommend");*/
				refreshParams.put("intelligent", "home");
				isChina = true;
			}
			refreshKey = 1;
			//刷新当前的ListView，展示List条目内容：提交get请求
			refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_home_list);//这个地方需要进行去服务器访问数据，                                                           setKey为1      

			/*移至head从xml被填充后就立马addHeaderView进去
			 *lv_home_list.addHeaderView(head, null, true);*/

			/**
			 * 下面两个点获取不正常
			 */
			/*iv_point = (ImageView) lv_home_list.findViewById(R.id.iv_point);
			iv_point2 = (ImageView) lv_home_list.findViewById(R.id.iv_point2);*/

			iv_point = (ImageView) head.findViewById(R.id.iv_point);
			iv_point2 = (ImageView) head.findViewById(R.id.iv_point2);

			//放了个ViewPager的头在ListView上面
			/*final ViewPager banner = (ViewPager) lv_home_list.findViewById(R.id.vp_home_pager);*/
			final ViewPager banner = (ViewPager) head.findViewById(R.id.vp_home_pager);

			// banner.setOnTouchListener(new OnTouchListener() {
			// @Override
			// public boolean onTouch(View v, MotionEvent event) {
			// if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			// canscoll = true;
			// } else {
			// canscoll = false;
			// }
			// return false;
			// }
			// });
			// lv_home_list.setOnTouchListener(new OnTouchListener() {
			// @Override
			// public boolean onTouch(View v, MotionEvent event) {
			// if (event.getAction() == MotionEvent.ACTION_UP) {
			// canscoll = false;
			// }
			// return canscoll;
			// }
			// });


			/*
			 * 这个HomePagerAdapter真是别有洞天啊！里面设计了一系列的点击跳转操作和事件的监听
			 */
			HomePagerAdapter adapter = new HomePagerAdapter(activity);//这个ViewPager的Adapter中含有大量操作方法  //TODO
			if (!isChina) {
				iv_point2.setVisibility(View.GONE);
			}
			adapter.setIsChina(isChina);
			banner.setAdapter(adapter); //banner是ListView里面的上部的ViewPager
			banner.setOnPageChangeListener(pageChangeListener);
		}

	}

	private void changePointColor(int position) {
		if (position == 0) {
			iv_point.setBackgroundResource(R.drawable.oval_select);
			iv_point2.setBackgroundResource(R.drawable.oval_nomal);
		}
		if (position == 1) {
			iv_point.setBackgroundResource(R.drawable.oval_nomal);
			iv_point2.setBackgroundResource(R.drawable.oval_select);
		}
	}

	OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int position) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			changePointColor(position);
		}

	};

	private ActivityList list;
	private HomePromote promoteList;
	private HomeListAdapter promoteAdapter;
	private String photoUrl;
	private boolean isFirstRequest = true;
	private View rootView;
	private ScrollView mScrollView;
	private View refresh_footer;
	private ServiceAllBiz biz;
	protected Intent intent;

	public int getLocation(View v) {
		int[] loc = new int[4];
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		loc[0] = location[0];
		loc[1] = location[1];
		return loc[1];
	}

	/*
	    静态的 方法
	 */
	public static void startTopOutAnim(Context activity, View v) {
		Animation search_top_in = AnimationUtils.loadAnimation(activity,
				R.anim.home_top_out);
		v.setAnimation(search_top_in);
		v.startAnimation(search_top_in);
		v.setVisibility(View.GONE);
	}

	@InjectHttp
	private void result(ResponseEntity r) {

		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/


		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0://拿到顶头上的图
				try {
					//if(isFirstRequest){//当我们在init()中是第一次去访问的时候,我们就先将获取到的数据(其实压根没数据)展示出来,把图放上去
					//在init中发送了个请求,先将listView的头部加载出来显示,实际上这里的shopDataList中是空数据
					//防止fragment回来时被发现shopDataList中还有值,那么会先出现刚刚回来展示的数据居然还多于后面刷新后的数据列表
					//shopDataList.clear();

					Log.i("TAGTAGTAGTAGTAG", "前去服务器已经拿回了值");
					JSONObject obj = new JSONObject(r.getContentAsString());
					photoUrl = obj.get("photo").toString();
					//loadPhoto(photoUrl, iv_home_ad);

					/*	for(HashMap<String, Object> shopData: shopDataList){
						System.out.println(shopData.toString());
					}

					adapter = new SearchAdapter(lv_home_list, shopDataList, R.layout.activity_search_item);
					lv_home_list.setAdapter(adapter);
					adapter = null;*/

					//}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				loadPhoto(photoUrl, iv_home_ad);


				break;
			case 1:// 极文列表

				if (!isChina) {
					promoteList = Handler_Json.JsonToBean(HomePromote.class,r.getContentAsString());

					if (promoteList.getData() != null) {
						ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
						for (HomePromoteData data : promoteList.getData()) {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("iv_home_item_head", data.getPhoto());
							map.put("tv_home_item_title", data.getTag());
							map.put("tv_home_item_desc", data.getName());
							dataList.add(map);
						}
						/**
						 * 拿到数据后开始进行对ListView的每一个条目的设置
						 */
						if (promoteAdapter == null) {//第一次进入,Adapter为null,将其设置到ListView对象上去
							promoteAdapter = new HomeListAdapter(lv_home_list,
									dataList, R.layout.home_item);

							lv_home_list.setAdapter(promoteAdapter);

						} else {

							PullToRefreshManager.getInstance().onHeaderRefreshComplete();
							PullToRefreshManager.getInstance().onFooterRefreshComplete();
							promoteAdapter.notifyDataSetChanged();
						}
					} else {//promoteList.getData() == null 若没有获取到值，可将其当前的listview连接的适配器设置为空
						lv_home_list.setAdapter(null);
					}

				} else {//如果选择的城市是China的城市,换句话说,常规进来的界面是走下面的代码的

					list = Handler_Json.JsonToBean(ActivityList.class,r.getContentAsString());

					if (list.getData() != null) {
						for (ActivityListData data : list.getData()) {
							HashMap<String, Object> map = new HashMap<String, Object>();

							map.put("tv_search_activity_name", data.getShop().getName());

							map.put("tv_search_activity_distance", data.getShop().getDistance());

							map.put("tv_search_activity_desc", data.getActivity().getName());

							map.put("iv_search_activity_head", data.getActivity().getPhoto());

							map.put("activityId", data.getActivity().getId());
							map.put("shopId", data.getShop().getId());
							map.put("tv_search_tag", data.getActivity().getTag());

							map.put("icons", data.getActivity().getCategory());//拿到category的icons列表

							shopDataList.add(map);//店铺的数据源List
						}
						if (adapter == null) {
							adapter = new SearchAdapter(lv_home_list,shopDataList, R.layout.activity_search_item);
							lv_home_list.setAdapter(adapter);

							setListViewHeightBasedOnChildren(lv_home_list);   
							//setListViewHeightBasedOnChildrenByDataList(lv_home_list, shopDataList);

						} else {
							try{
								lv_home_list.removeFooterView(refresh_footer);
							}catch(Exception e){
								e.printStackTrace();
							}
							//TODO
							adapter.notifyDataSetChanged();
							/*adapter.notifyAll();*/
							lv_home_list.setAdapter(adapter); //少了这段代码，仅仅只有新刷出来的数据是拥有标签的
							
							setListViewHeightBasedOnChildren(lv_home_list);  
							//setListViewHeightBasedOnChildrenByDataList(lv_home_list, shopDataList);
						}
						//上面的图还是要加载上去的
						/*loadPhoto(photoUrl, iv_home_ad);*/
						isFirstRequest = false;


						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						PullToRefreshManager.getInstance().footerUnable();


					}
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}
	}

	/**
	 * 动态自动测量ListView的高度
	 * @param listView
	 */
	private void setListViewHeightBasedOnChildren(ListView listView ) {

		ListAdapter lAdapter = listView.getAdapter(); 
		// 获取ListView对应的Adapter  
		if (lAdapter == null) {   
			return;   
		}   

		int totalHeight = 0;   
		for (int i = 0, len = lAdapter.getCount(); i < len; i++) {   
			// listAdapter.getCount()返回数据项的数目   
			View listItem = lAdapter.getView(i, null, listView);   
			// 计算子项View 的宽高   
		    listItem.measure(0, 0);  
			// 统计所有子项的总高度   
			totalHeight += listItem.getMeasuredHeight();    
		}   

		ViewGroup.LayoutParams params = listView.getLayoutParams();   
		// listView.getDividerHeight()获取子项间分隔符占用的高度   
		// params.height最后得到整个ListView完整显示需要的高度   
		/*params.height = totalHeight+ (listView.getDividerHeight() * (lAdapter.getCount() - 1) + refresh_footer.getMeasuredHeight());   */
	    params.height = totalHeight+ (listView.getDividerHeight() * (lAdapter.getCount() - 1));   
	    
		listView.setLayoutParams(params);  
	}
	
	/**
	 * 动态自动测量ListView的高度
	 * @param listView
	 */
	private void setListViewHeightBasedOnChildrenByDataList(ListView listView, List dataList ) {

		ListAdapter lAdapter = listView.getAdapter(); 
		int totalHeight = 0;   
		int len = dataList.size(); 
		// listAdapter.getCount()返回数据项的数目   
		View listItem = lAdapter.getView(0, null, listView);   
		// 计算子项View 的宽高   
		listItem.measure(0, 0);    
		// 统计所有子项的总高度   
		totalHeight = listItem.getMeasuredHeight() * len;
		ViewGroup.LayoutParams params = listView.getLayoutParams();   
		if(hasFooterview){
			params.height = totalHeight+ (listView.getDividerHeight() *len-1)+refresh_footer.getMeasuredHeight();   
		}else{
			params.height = totalHeight+ (listView.getDividerHeight() *len-1);   
		}
		listView.setLayoutParams(params);  
	}

	public void click(View v) {
		EventBus bus = EventBus.getDefault();
		Fragment fragment = null;
		FragmentEntity entity = new FragmentEntity();
		switch (v.getId()) {
		case R.id.bottom_secretary:
			fragment = new SecretaryFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
			break;
		case R.id.bottom_mine:
			fragment = new MineFragment();
			break;
		case R.id.iv_home_location:// 进入地理定位
			fragment = new GpsFragment();
			break;
		case R.id.iv_top_location:// 进入地理定位
			fragment = new GpsFragment();
			break;
		case R.id.rl_top_bg:// 进入我的银行卡
			fragment = new MineBankcardFragment();
			if ("true".equals(App.app.getData("isExperience"))) {
				v.setBackgroundResource(R.drawable.circle_background_green);
			}
			boolean isFromHome1 = true;
			Bundle bundle1 = new Bundle();
			bundle1.putBoolean("isFromHome", isFromHome1);
			fragment.setArguments(bundle1);
			break;

		case R.id.rl_bg:// 进入我的银行卡
			fragment = new MineBankcardFragment();
			boolean isFromHome2 = true;
			Bundle bundle2 = new Bundle();
			bundle2.putBoolean("isFromHome", isFromHome2);
			fragment.setArguments(bundle2);

			if ("true".equals(App.app.getData("isExperience"))) {
				v.setBackgroundResource(R.drawable.circle_background_green);
			}
			break;
		case R.id.tv_home_top_location:// 进入城市切换界面
			fragment = new GpsFragment();
			break;
		case R.id.ll_activity_home_yhq:// 优惠券
			fragment = new MineCouponsFragment();
			break;
		case R.id.ll_search:
			fragment = new SearchFragment();
			break;
		case R.id.et_home_search:
			fragment = new SearchFragment();
			break;
		}
		if (fragment != null) {
			entity.setFragment(fragment);
			bus.post(entity);
		}

	}

	/**
	 * 
	 * @param type
	 */
	/*	@InjectPullRefresh
	public void call(int type) {
		// 首页是没有下拉加载的
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();
					CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");

				} else {
					refreshParams = new LinkedHashMap<>();
					if (isChina) {//如果是国内活动，需要进行刷新操作
						refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
						refreshParams.put("site", getSelectCity()[0]);
						refreshParams.put("intelligent", "home");
					} else {
						refreshParams = null;
						refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,getSelectCity()[0]);
					}
					refreshCurrentList(list.getNext_page_url(), refreshParams,1, lv_home_list);
				}
			}else{//针对第一次进来用户急于去刷新操作，但此时的list对象是为空的
				CustomToast.show(activity, "让网速飞一会儿，biu~biu~", "总裁大大，请给我一首歌的时间");
				refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("intelligent", "home");
				refreshCurrentList(refreshUrl, refreshParams,1, lv_home_list);
				PullToRefreshManager.getInstance().onFooterRefreshComplete();

			}
			break;
		}
	}
	 */

	/**
	 * 下面的方法原本是针对PullTorefresh进行的滑动监听的
	 * @author Yeun.Zhang
	 *
	 */
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {


				Thread.sleep(4000);


			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// Do some stuff here
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();
					CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");

					/*sv_homefrag.setOnPullEventListener(null);
					sv_homefrag.setPullToRefreshEnabled(false);
					sv_homefrag.setPullToRefreshOverScrollEnabled(false);*/


				} else {
					refreshParams = new LinkedHashMap<>();
					if (isChina) {//如果是国内活动，需要进行刷新操作
						refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
						refreshParams.put("site", getSelectCity()[0]);
						refreshParams.put("intelligent", "home");
					} else {
						refreshParams = null;
						refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,getSelectCity()[0]);
					}
					refreshCurrentList(list.getNext_page_url(), refreshParams,1, lv_home_list);
				}
			}else{//针对第一次进来用户急于去刷新操作，但此时的list对象是为空的
				CustomToast.show(activity, "让网速飞一会儿，biu~biu~", "总裁大大，请给我一首歌的时间");
				refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("intelligent", "home");
				refreshCurrentList(refreshUrl, refreshParams,1, lv_home_list);

				/*PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
			}
			// Call onRefreshComplete when the list has been refreshed.
			/*sv_homefrag.onRefreshComplete();*/

			super.onPostExecute(result);
		}
	}
}
