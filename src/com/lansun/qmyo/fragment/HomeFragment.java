package com.lansun.qmyo.fragment;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.PullToRefreshView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.google.gson.Gson;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;
import com.lansun.qmyo.GrabRedPackActivity;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.HomeListAdapter;
import com.lansun.qmyo.adapter.HomePagerAdapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.HomeAdPhotoData;
import com.lansun.qmyo.domain.HomePosterList;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.newbrand.NewBrandFragment;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.listener.ToLoginListener;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.AnimUtils;
import com.lansun.qmyo.utils.CustomDialog;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.FixedSpeedScroller;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.EightPartActivityAdapter;
import com.lansun.qmyo.view.CloudView;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExperienceDialog;
import com.lansun.qmyo.view.HomeAdViewPager;
import com.lansun.qmyo.view.LoginDialog;
import com.lansun.qmyo.view.MyMesureSelfViewPager;
import com.lansun.qmyo.view.UpdateAppVersionDialog;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lansun.qmyo.view.UpdateAppVersionDialog.OnConfirmListener;
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
	/*private SearchAdapter adapter;*/
	private EightPartActivityAdapter adapter;
	private InternalHandler handler = new InternalHandler();
	private ImageView iv_point2;
	private ImageView iv_point, iv_home_ad;
	private View ll_activity_home_new, ll_activity_home_yhq;// 新品曝光
	private boolean isChina = true;
	public boolean mFromBankCardFragment = false;
	private boolean onlyOne = true;
	private boolean hasFooterview;
	private String nextPageUrl = "";
	private boolean isDeleteShopData = false;
	private View searchView;
	private static int currY;
	private static int hiddenY;
	
//	private Handler uiHandler=new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			switch (msg.what) {
//			case 10:
//				ToggleDialogByUserStatus();
//				break;
//			}
//		}

	/**
	 * LoonAndroid框架规定，上拉和下拉刷新只能针对ListView进行设置，其他的View类型不可识别
	 */
	/*@InjectView(binders = { @InjectBinder(listeners ={ OnItemClick.class }, method = "itemClick")}, pull = true) 
	private  MyListView lv_home_list;*/

	@InjectView
	private  TestMyListView lv_home_list;

	class Views {
		@InjectBinder(method = "click", listeners = OnClick.class)
		private RelativeLayout fl_home_top_menu, rl_top_r_top_menu, rl_bg,rl_top_bg;
		@InjectBinder(method = "click", listeners = OnClick.class)
		private View bottom_secretary, bottom_found, bottom_mine,
		iv_home_location, iv_top_location, ll_search;
		private TextView tv_home_location, tv_home_top_location;
		private RecyclingImageView iv_home_icon;
		private TextView tv_home_icon, tv_home_experience,
		tv_top_home_experience;
		private View iv_card, iv_top_card;
		private View snow_view;
		@InjectBinder(method = "click", listeners = OnClick.class)
		private EditText et_home_search;
	}

	private View tv_home_search;

	private boolean isPlay = false;
	
	private ArrayList<HashMap<String, Object>> shopDataList = new ArrayList<HashMap<String, Object>>();
	private PackageManager manager;
	public HomeFragment(boolean mIsFirstEnter) {
		mFromBankCardFragment = mIsFirstEnter;
	}

    public HomeFragment() {
		//Log.i("token", App.app.getData("access_token"));	
	}

	@Override
	public void onResume() {
		if(v.snow_view!=null){
			v.snow_view.setVisibility(View.VISIBLE);
		}
		//v.rl_top_r_top_menu.setVisibility(View.GONE);
		justComeBackFromHome = true;
		
		
		if(searchView!=null){
			justComeBackFromHome = true;
			//currY = getLocation(searchView);
			if (hiddenY > currY + 66){
				//AnimUtils.startTopInAnim(activity, v.fl_home_top_menu);
				//v.fl_home_top_menu.setVisibility(View.VISIBLE);
			} else {
				//startTopOutAnim(activity, v.fl_home_top_menu);
				//v.fl_home_top_menu.setVisibility(View.GONE);
				LogUtils.toDebugLog("回到后台测试", "重新进入首页界面");
				//v.fl_home_top_menu.setVisibility(View.GONE);
			}
		}
		//v.fl_home_top_menu.setVisibility(View.GONE);
		
		v.iv_home_icon.setPressed(true);//底部的首页定位button
		
		//isPlay = false;
		
		
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green1));

//		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

		
		/**
		 * 该方法：(InputMethodManager) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		 * 通过获取到的软键盘管理器 操作着 软键盘本身的显示和隐藏
		 * 实质上和 
		 * activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);方法去操作界面的效果是一致
		 * getWindow().setSoftInputMode是通过设置针对输入法展示情况的模式属性，来直接地影响着 界面上的控件，但又间接地影响着 软键盘的展示
		 */
//		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(lv_home_list.getWindowToken(), 0); 
		
		/*v.rl_bg.setPressed(true);
		v.rl_top_bg.setPressed(true);*/
		super.onResume();
	}

	@Override
	public void onPause() {
		
//		PullToRefreshManager.getInstance().headerUnable();
		//v.fl_home_top_menu.setVisibility(View.GONE);
		LogUtils.toDebugLog("回到后台测试", "离开首页界面");
		/*if (adapter != null) {                  //adapter若置为空，在退出后台时，再进去页面进行刷新，会导致刷新后至顶部 
			adapter = null;
		}*/ 
		v.snow_view.setVisibility(View.INVISIBLE);
		
		if (promoteAdapter != null) {
			promoteAdapter = null;
		}
		//isPlay = false;
		v.iv_home_icon.setPressed(true);
		v.tv_home_icon.setTextColor(getResources().getColor(R.color.app_green1));

		/*v.rl_bg.setPressed(true);
		v.rl_top_bg.setPressed(true);*/
		super.onPause();
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
			HashMap<String, Object> data = shopDataList.get(arg2 - 1);                     //添加了头的ListView，注意位置的偏移

			fragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("shopId", data.get("shopId").toString());
			args.putString("activityId", data.get("activityId").toString());

			String  shopId = args.getString("shopId");
			String  activityId = args.getString("activityId");
			Log.i("你点的位置上的Item","门店Id: "+shopId +"活动Id: "+activityId);
			fragment.setArguments(args);
		} else {
			HomePromoteData data = promoteList.getData().get(arg2);//----------------------这儿也需要注意，将来位置需要替换
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
		
		broadCastReceiver = new HomeRefreshBroadCastReceiver();
		System.out.println("首页 注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.refreshHome");
		filter.addAction("com.lansun.qmyo.refreshTheIcon");
		filter.addAction("com.lansun.qmyo.ChangeTheLGPStatus");
		filter.addAction("com.lansun.qmyo.refreshHomeList");
		getActivity().registerReceiver(broadCastReceiver, filter);
		
		
		/**
		 * 判断是否需要进行版本的更新
		 */
		PackageInfo info = null;
		manager = App.app.getPackageManager();
		try {
		  info = manager.getPackageInfo(App.app.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		  e.printStackTrace();
		}
		InternetConfig config = new InternetConfig();
		config.setKey(10);
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
//		params.put("key", "Android");
//		params.put("version", "1");
		//FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION + info.versionCode , config, this);
//		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION+"?key=Android&version="+1 ,params, config, this);
		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION + "?key=Android&version="+info.versionCode , config, this);
//		FastHttpHander.ajaxGet("http://api.andrew.qmyo.net/version/info/?key=Android&version=12", config, this);
		
		
		intent = new Intent("com.lansun.qmyo.fragment.newbrand");
		
		Log.e("token",App.app.getData("access_token"));
		if(GlobalValue.gps!=null){
			Log.e("gps",""+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
		}
		
		LayoutInflater inflater  = LayoutInflater.from(activity);
//		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		
		
		rootView = inflater.inflate(R.layout.activity_home_old, null, false);  //填充其home界面，由于activity_home.xml已被修改为了含ScrollView的界面，故使用原有的xml布局
		
		/*head =  rootView.findViewById(R.id.head_banner);*/
		Handler_Inject.injectFragment(this, rootView);//当前的fragment里面使用 自动去注入组件
		
		
		lv_home_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {
				
				if(position-2 >= shopDataList.size()){
					return;
				}
				ActivityDetailFragment fragment = new ActivityDetailFragment();
				Bundle args = new Bundle();
				args.putString("activityId",
						shopDataList.get(position-2).get("activityId").toString());
				args.putString("shopId", shopDataList.get(position-2).get("shopId")
						.toString());
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
				
				
			}
		});
		lv_home_list.setNoHeader(true);
		lv_home_list.onLoadMoreOverFished();
		lv_home_list.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefreshing() {
				
			}
			@Override
			public void onLoadingMore() {
				if (list != null) {
					if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();
						CustomToast.show(activity, R.string.reach_bottom, R.string.collect_more_superise);
						lv_home_list.onLoadMoreOverFished();
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
					/*refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
					refreshParams.put("site", getSelectCity()[0]);
					refreshParams.put("intelligent", "home");
					refreshCurrentList(refreshUrl, refreshParams,1, lv_home_list);
					PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
				}
			}
		});
		
		
		refresh_footer = inflater.inflate(R.layout.refresh_footer, null);

		//根据用户状态来判断是否弹出Dialog,为了保证模糊的背景为正常的首页界面图像，故现在将其放到首页轮播图加载出来之后，才判断弹窗
//		ToggleDialogByUserStatus();
		
		super.onCreate(savedInstanceState);
	}

	private void ToggleDialogByUserStatus() {
		if (TextUtils.isEmpty(App.app.getData("exp_secret"))
				&& TextUtils.isEmpty(App.app.getData("secret"))
				&& !GlobalValue.isFirst){
			ExperienceDialog dialog = new ExperienceDialog(activity);//这么个体验的对话框，需要单独在其内部设置点击响应事件
			//进来首先就弹出对话框
			dialog.setOnConfirmListener(new com.lansun.qmyo.view.ExperienceDialog.OnConfirmListener() {
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
			dialog.show(getActivity().getFragmentManager(), "experience");
//			dialog.show(getActivity().getSupportFragmentManager(), "experience");
		}
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/*activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);*/
//		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
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
		

		return rootView;
	}
	boolean canscoll = false;
	
	/*private ArrayList<HomeAdPhotoData> homeAdPhotoList = new ArrayList<HomeAdPhotoData>();
	public void setDataIntoHomeAdPhotoList(){
		HomeAdPhotoData homeAdPhotoData1 = new HomeAdPhotoData();
    	homeAdPhotoData1.setPhoto("http://lansuntest.qiniudn.com/201507201746_143738561397");
    	HomeAdPhotoData homeAdPhotoData2 = new HomeAdPhotoData();
    	homeAdPhotoData2.setPhoto("http://lansuntest.qiniudn.com/201507201746_143738561397");
    	homeAdPhotoList.add(homeAdPhotoData1);
    	homeAdPhotoList.add(homeAdPhotoData2);
	}*/
	

	@InjectInit
	private void init() {
		//模拟图片的数据源
		/*setDataIntoHomeAdPhotoList();*/
	
		
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
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_green);
				v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_green);

				v.tv_home_experience.setVisibility(View.VISIBLE);
				v.tv_top_home_experience.setVisibility(View.VISIBLE);
				v.iv_card.setVisibility(View.GONE);
				v.iv_top_card.setVisibility(View.GONE);

			} else {
				if(App.app.getData("isEmbrassStatus").equals("true")){//当判断为尴尬用户状态时，强制要求其进入登录注册页，要么使用其他可使用号码进行登录，要么再使用原有号码登录后再跳至搜索银行卡页
					/*SearchBankCardFragment searchBankCardFragment = new SearchBankCardFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean("isFromRegisterAndHaveNothing",true);//借用这个isFromRegisterAndHaveNothing的标签进行搜索的设置
					searchBankCardFragment.setArguments(bundle);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(searchBankCardFragment);
					EventBus.getDefault().post(fEntity);*/
					RegisterFragment registerFragment = new RegisterFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean("isFromRegisterAndHaveNothingThenGoToRegister",true);//借用这个isFromRegisterAndHaveNothing的标签进行搜索的设置
					registerFragment.setArguments(bundle);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(registerFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}else{
					//如果不是体验用户,即我已是注册登录用户
					v.iv_card.setVisibility(View.VISIBLE);//原本右边银行卡可见
					v.iv_top_card.setVisibility(View.VISIBLE);//滑动出现的右边银行卡可见
					v.tv_home_experience.setVisibility(View.GONE);//原本  体验不可见
					v.tv_top_home_experience.setVisibility(View.GONE);//滑动出现的右边 体验二字 不可见
					v.rl_bg.setPressed(false);
					v.rl_bg.setBackgroundResource(R.drawable.circle_background_gray);
					v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_gray);
					v.rl_top_bg.setPressed(false);
				}
			}

			if (!TextUtils.isEmpty(getSelectCity()[0])) {//设置左上角的城市名称
				v.tv_home_location.setText(getSelectCity()[1]);
				v.tv_home_top_location.setText(getSelectCity()[1]);
			}

			head = inflater.inflate(R.layout.activity_home_banner, null);
			pointSets = (LinearLayout) head.findViewById(R.id.ll_point_sets);
			
			/*尝试在head被充起来的瞬间就将其放到ListView的头上*/
			lv_home_list.addHeaderView(head, null, true);

			ViewTreeObserver vto = head.getViewTreeObserver();
			vto.addOnScrollChangedListener(new OnScrollChangedListener() {


				@Override
				public void onScrollChanged() {
					
					int[] location = new int[2];
					v.iv_home_location.getLocationInWindow(location);
					
					hiddenY = location[1] + v.iv_home_location.getHeight();//hiddenY Y轴方向上的坐标值

					/**
					 * View searchView = lv_home_list.findViewById(R.id.ll_search);
					 * */

					searchView = head.findViewById(R.id.ll_search);
					searchView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							SearchFragment fragment = new SearchFragment();
							FragmentEntity event = new FragmentEntity();
							event.setFragment(fragment);
							EventBus.getDefault().post(event);
						}
					});
					
					currY = getLocation(searchView);
					if (currY == 0) {
						return;
					}
					if (hiddenY > currY + 66) {//隐藏起来的HiddenY已经很多了，此时需要展示出上面隐藏的内容
						//LogUtils.toDebugLog("isPlay", "isPlay的值为： "+isPlay);
						if (isPlay) {
							isPlay = false;
							LogUtils.toDebugLog("isPlay", "isPlay的值为： "+isPlay);
							Animation search_top_in = AnimationUtils.loadAnimation(activity, R.anim.home_top_in);
							
							AnimUtils.startTopInAnim(activity,v.fl_home_top_menu);
							v.rl_top_r_top_menu.setVisibility(View.GONE);
						}else{//从后台回来，isPlay == false
							if(justComeBackFromHome){
								//仅仅保持当前的样式不动
								isPlay =false;
								justComeBackFromHome=false;
							}
							
							
						}
					} else {
						//LogUtils.toDebugLog("isPlay", "isPlay的值为： "+isPlay);
						
						if (!isPlay) {//顶部搜索栏  非展示  状态
							
							if(justComeBackFromHome){//只对 刚从后台回来和刚进入app时，这一下不做动画操作
								//startTopOutAnim(activity, v.fl_home_top_menu);
								isPlay = true;
								v.rl_top_r_top_menu.setVisibility(View.VISIBLE);
								justComeBackFromHome = false;
							}else{
								isPlay = true;
								startTopOutAnim(activity, v.fl_home_top_menu);
								v.rl_top_r_top_menu.setVisibility(View.VISIBLE);
							}
						}else{//顶部搜索栏  展示  状态
							if(justComeBackFromHome){//刚从后台回来
								//isPlay = false;
								//startTopOutAnim(activity, v.fl_home_top_menu);
								v.rl_top_r_top_menu.setVisibility(View.VISIBLE);
								justComeBackFromHome = false;
							}
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

			
			/**
			 * 下面两个点获取不正常
			 */
			/*iv_point = (ImageView) lv_home_list.findViewById(R.id.iv_point);
			iv_point2 = (ImageView) lv_home_list.findViewById(R.id.iv_point2);*/

			iv_point = (ImageView) head.findViewById(R.id.iv_point);
			iv_point2 = (ImageView) head.findViewById(R.id.iv_point2);
			iv_homead_point = (ImageView) head.findViewById(R.id.iv_homead_point);
			iv_homead_point2 = (ImageView) head.findViewById(R.id.iv_homead_point2);
			iv_homead_point3 = (ImageView) head.findViewById(R.id.iv_homead_point3);
			
			
			
			
			/**
			 * 为保证八大板块的展示效果不是那么容易丢失，故在此提升其绘制展示的顺序，
			 * 一定程度上减少丢失的可能性
			 */

			//放了个ViewPager的头在ListView上面
			/*final ViewPager banner = (ViewPager) lv_home_list.findViewById(R.id.vp_home_pager);*/
			
			
			final ViewPager banner = (ViewPager) head.findViewById(R.id.vp_home_pager);
			
			//final MyMesureSelfViewPager banner = (MyMesureSelfViewPager) head.findViewById(R.id.vp_home_pager);
			

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
			 * 八大板块的ViewPager   -->此处易丢失
			 * 
			 * 这个HomePagerAdapter真是别有洞天啊！里面设计了一系列的点击跳转操作和事件的监听
			 */
			HomePagerAdapter adapter = new HomePagerAdapter(activity);//这个ViewPager的Adapter中含有大量操作方法  //TODO
			if (!isChina) {
				iv_point2.setVisibility(View.GONE);
			}
			adapter.setIsChina(isChina);
			banner.setAdapter(adapter); //banner是ListView里面的上部的ViewPager
			banner.setOnPageChangeListener(pageChangeListener);
			
			
			
			
			

			InternetConfig config = new InternetConfig();
			config.setKey(0);
			/**
			 * 去获取数据内容,暂关闭依据定位地点获取数据的方法
			 FastHttpHander.ajaxGet(GlobalValue.URL_HOME_AD + getSelectCity()[0], config, this);*/
			
			/* 去拿头部的广告图片-->>
			 */
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
				refreshParams.put("location", String.valueOf(GlobalValue.gps.getWgLat()+","+ GlobalValue.gps.getWgLon()));
				/* refreshParams.put("site", getSelectCity()[0]);*/

				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("intelligent", "home");
				isChina = true;
			}
			refreshKey = 1;
			
			//列表也有可能展示不出来，导致下方代码无法执行下去，便出现了八大板块页面无法展示的问题
			refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_home_list);//这个地方需要进行去服务器访问数据，                                                           setKey为1      
		}
		
		


	}
	
	/**
	 * 给首页轮播大图上的绿色小点设置对应的颜色
	 * @param position
	 */
	private void changeHomeAdPointColor(int position) {
		   pointSets.removeAllViews();
		   int selectPosition = position % homePhotoList.size();
		   
		    for (int i = 0; i < homePhotoList.size(); i++) {  
		    	RecyclingImageView pointImageView = new RecyclingImageView(activity);  
		    	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(14,14);
		    	layoutParams.leftMargin = 8;
		    	pointImageView.setLayoutParams(layoutParams);  
		    	if(i==selectPosition){
		    		pointImageView.setBackgroundResource(R.drawable.oval_select);  
		    	}else{
		    		pointImageView.setBackgroundResource(R.drawable.oval_nomal);  
		    	}
		        pointSets .addView(pointImageView);  
		   } 
		
		
//		    if (position%3 == 0) {
//			iv_homead_point.setBackgroundResource(R.drawable.oval_select);
//			iv_homead_point2.setBackgroundResource(R.drawable.oval_nomal);
//			iv_homead_point3.setBackgroundResource(R.drawable.oval_nomal);
//		}
//		if (position%3 == 1) {
//			iv_homead_point2.setBackgroundResource(R.drawable.oval_select);
//			iv_homead_point.setBackgroundResource(R.drawable.oval_nomal);
//			iv_homead_point3.setBackgroundResource(R.drawable.oval_nomal);
//		}
//		if (position%3 == 2) {
//			iv_homead_point3.setBackgroundResource(R.drawable.oval_select);
//			iv_homead_point.setBackgroundResource(R.drawable.oval_nomal);
//			iv_homead_point2.setBackgroundResource(R.drawable.oval_nomal);
//		}
//		//选中的小圆点，注意：编号从0开始，size-1结束
//		int selectPosition = position % homePhotoList.size();
//		
//		for(int j= 0;j<homePhotoList.size();j++){
//			if(j==selectPosition){
//				//此point下的小圆点为 选中 状态  //TODO
//			}else{
//				//剩余的小圆点为 非选中 状态
//			}
//		}
		
		
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
			//1.页面滑动时取消ViewPager内部View的点击事件
			
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
	//private ScrollView mScrollView;
	private View refresh_footer;
	private ServiceAllBiz biz;
	protected Intent intent;
	private ImageView iv_homead_point;
	private ImageView iv_homead_point2;
	private ImageView iv_homead_point3;
	private HomeAdViewPager vp_home_ad;
//	private JazzyViewPager vp_home_ad;
	private HomeRefreshBroadCastReceiver broadCastReceiver;
	private IntentFilter filter;
	private FixedSpeedScroller mScroller;
	/*
	 * 标签属性： 标示 首页刚刚从后台回来
	 */
	private boolean justComeBackFromHome = false;
	private ArrayList<HashMap<String, String>> homePhotoList = new  ArrayList<HashMap<String, String>>();
	private LinearLayout pointSets;

	public int getLocation(View v) {
		int[] loc = new int[4];
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		loc[0] = location[0];
		loc[1] = location[1];
		return loc[1]; //只需要返回Y轴方向的坐标
	}

	/*
	    静态的 方法
	 */
	public static void startTopOutAnim(Context activity, View v) {
		Animation search_top_in = AnimationUtils.loadAnimation(activity,
				R.anim.home_top_out);
		//search_top_in.setDuration(1000);
		v.setAnimation(search_top_in);
		v.startAnimation(search_top_in);
		v.setVisibility(View.GONE);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		
//		PullToRefreshManager.getInstance().headerUnable();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
//		PullToRefreshManager.getInstance().footerEnable();

		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0://拿到顶头上的图
//				try {
//					//if(isFirstRequest){//当我们在init()中是第一次去访问的时候,我们就先将获取到的数据(其实压根没数据)展示出来,把图放上去
//					//在init中发送了个请求,先将listView的头部加载出来显示,实际上这里的shopDataList中是空数据
//					//防止fragment回来时被发现shopDataList中还有值,那么会先出现刚刚回来展示的数据居然还多于后面刷新后的数据列表
//					//shopDataList.clear();
//
//					Log.i("TAGTAGTAGTAGTAG", "前去服务器已经拿回了值");
//					JSONObject obj = new JSONObject(r.getContentAsString());
//					photoUrl = obj.get("photo").toString();
//					
//					iv_home_ad.setVisibility(View.VISIBLE);
//					vp_home_ad = (ViewPager) head.findViewById(R.id.vp_home_ad);
//					vp_home_ad.setVisibility(View.GONE);
//					loadPhoto(photoUrl, iv_home_ad);
//					
//						//目前返回的只是一个对象,下面是针对数组使用的
//						//ArrayList<HomeAdPhotoData> photoList = new ArrayList<HomeAdPhotoData>();
//					
//						//adapter = null;
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				
//				endProgress();
				
				
				
				//拿到头部的几张图片的数据
				HomePosterList photoList = Handler_Json.JsonToBean(HomePosterList.class,r.getContentAsString());//TODO
				
				LogUtils.toDebugLog("photoList.toString()", "r.getContentAsString() :"+r.getContentAsString());
				/*Gson gson = new Gson();
				HomePosterList photoList = gson.fromJson(r.getContentAsString(), HomePosterList.class);*/
				
				LogUtils.toDebugLog("photoList.toString()", "photoList.toString()  :  "+photoList.toString());
				
				HashMap<String, String> hashMap = new HashMap<String,String>();
				   for(HomeAdPhotoData data: photoList.getData()){
					    hashMap = new HashMap<String,String>();
					    hashMap.put("photoDataWebViewUrl", data.getUrl());
				    	hashMap.put("photoDataPhotoUrl",data.getPhoto()); 
				    	hashMap.put("photoDataTag",String.valueOf(data.getTag())); 
				    	hashMap.put("photoDataActivityId",data.getActivity_id()); 
				    	hashMap.put("photoDataShopId",data.getShop_id()); 
				    	LogUtils.toDebugLog("photoDataPhotoUrl", "photoDataPhotoUrl的值为: "+data.getPhoto());
				    	LogUtils.toDebugLog("photoDataTag", "photoDataTag的值为: "+data.getTag());
				    	homePhotoList.add(hashMap);
				    }
				    endProgress();
				    
				    
				    //拿到数据后，进行viewpager的负载问题
				    vp_home_ad = (HomeAdViewPager) head.findViewById(R.id.vp_home_ad);
				    
				    /*vp_home_ad = (JazzyViewPager) head.findViewById(R.id.vp_home_ad);
					vp_home_ad.setTransitionEffect(TransitionEffect.CubeIn);*/
				    
				    
				    if(homePhotoList.size()>1){
				    	vp_home_ad.setPagingEnabled(true);
					}else{
						vp_home_ad.setPagingEnabled(false);
					}
				    
					//1.给ViewPager设置上资源适配器
			/*		MyHomeAdPagerAdapter homeAdPagerAdapter = new MyHomeAdPagerAdapter(homeAdPhotoList);*/		
					MyHomeAdPagerAdapter homeAdPagerAdapter = new MyHomeAdPagerAdapter(homePhotoList);
					//2.给Viewpager设置上页面切换的监听器
					OnPageChangeListener homeAdPageChangeListener = new OnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							//NTD1.改变对应的小圆点的颜色
							if(homePhotoList.size()>1){
								changeHomeAdPointColor(position);
							}else{
								pointSets.setVisibility(View.INVISIBLE);
							}
						}
						@Override
						public void onPageScrolled(int arg0, float arg1, int arg2) {
						}
						@Override
						public void onPageScrollStateChanged(int arg0) {
						}
					};
					
					
					//3.给Viewpager设置上轮播的无限循环效果
					//4.给Viewpager设置上页面的点击效果        -->在Adapter中，对View进行设置
					
					vp_home_ad.setAdapter(homeAdPagerAdapter);
//					vp_home_ad.setOnTouchListener(new MyOnTouchListener());
					
					/**
					 * 利用Android的反射机制，试图减缓图片的滑动速度
					 * 但却减缓了用户手指在滑动时的效果
					 * 故暂时将其禁掉
					 */
					try {
						Field mField = ViewPager.class.getDeclaredField("mScroller");
						mField.setAccessible(true);//允许暴力反射
						mScroller = new FixedSpeedScroller(vp_home_ad.getContext(),new LinearInterpolator());
						mScroller.setmDuration(450);
						mField.set(vp_home_ad, mScroller);
					} catch (Exception e) {
						e.printStackTrace();
					}
					mScroller.setmDuration(450);
					vp_home_ad.setOnPageChangeListener(homeAdPageChangeListener);
					
					if(homePhotoList.size()>1){
						handler.removeCallbacksAndMessages(null);
						handler.postDelayed(new InternalTask(), 5000);
					}
					vp_home_ad.setCurrentItem(1000*3);
					
					
					//dataList为空
					for(HashMap<String, Object> shopData: shopDataList){
						System.out.println(shopData.toString());
					}
					/*adapter = new SearchAdapter(lv_home_list, shopDataList, R.layout.activity_search_item);*/
					adapter = new EightPartActivityAdapter(activity, shopDataList);
					lv_home_list.setAdapter(adapter);
					endProgress();
//					PullToRefreshManager.getInstance().headerUnable();
					
					
					//1.当界面出现图片时，才进行随机银行卡的弹窗操作
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(10);
							//ToggleDialogByUserStatus();
						}
					}, 200);
					
				//loadPhoto(photoUrl, iv_home_ad);
				break;
			case 1:// 极文列表
				
				//LogUtils.toDebugLog("AppUpdate", "result(1)中决定是否弹出弹窗： GlobalValue.isWaitingForUpdateApp: "+GlobalValue.isWaitingForUpdateApp);
				
				/**
				 * 当App.app.getData("toUpdateApp")为空时，才会去进行弹窗的判断，
				 * 否则无需进行下面的判断，
				 * 避免在App启动的当前一次效用内，用户一打开app首次拒绝了更新要求后，当使用过程中刷新HomeFragment时，造成重复的弹窗更新提醒，这样体验很差
				 * 故而，在当前次的使用过程中，需保持弹出一次即可，正常退出后，下次开启，可再弹框提醒
				 */
				if(TextUtils.isEmpty(App.app.getData("toUpdateApp"))){//App.app.getData("toUpdateApp")为空的时候，表明是第一次开启App
					if(GlobalValue.isWaitingForUpdateApp){//根据case10的访问结果，来判断是否需要弹出对话框
						UpdateAppVersionDialog dialog = new UpdateAppVersionDialog();//这么个体验的对话框，需要单独在其内部设置点击响应事件
						//进来首先就弹出对话框
						dialog.setOnConfirmListener(new OnConfirmListener(){
							@Override
							public void confirm() {
								//CustomToast.show(activity, "看好咯", "我要下载咯");
							}
						});
						dialog.show(getActivity().getFragmentManager(), "update");
						GlobalValue.isWaitingForUpdateApp =  false;
					}
					App.app.setData("toUpdateApp","NotBlank");
				}
				
				
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
//							PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//							PullToRefreshManager.getInstance().onFooterRefreshComplete();
							promoteAdapter.notifyDataSetChanged();
						}
					} else {//promoteList.getData() == null 若没有获取到值，可将其当前的listview连接的适配器设置为空
						lv_home_list.setAdapter(null);
					}

				} else {//如果选择的城市是China的城市,换句话说,常规进来的界面是走下面的代码的
					
					LogUtils.toDebugLog("result", "result(1)中拿到首页下面的HomeList内容了");
					lv_home_list.onLoadMoreFished();
					lv_home_list.onRefreshFinshed(true);
					
					list = Handler_Json.JsonToBean(ActivityList.class,r.getContentAsString());
					
					if(isDeleteShopData){
						shopDataList.clear();
						isDeleteShopData=!isDeleteShopData;
					}
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
							/*adapter = new SearchAdapter(lv_home_list,shopDataList, R.layout.activity_search_item);*/
							adapter = new EightPartActivityAdapter(activity, shopDataList);
							lv_home_list.setAdapter(adapter);

						} else {
							try{
								lv_home_list.removeFooterView(refresh_footer);
							}catch(Exception e){
								e.printStackTrace();
							}
							//TODO
							adapter.notifyDataSetChanged();
//							PullToRefreshManager.getInstance().headerUnable();
							
							/*adapter.notifyAll();*/
							//lv_home_list.setAdapter(adapter); //当ScrollView包裹ListView时，少了这段代码，仅仅只有新刷出来的数据是拥有标签的
						}
						//上面的图还是要加载上去的
						//loadPhoto(photoUrl, iv_home_ad);
//						PullToRefreshManager.getInstance().headerUnable();
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						isFirstRequest = false;
					}
				}
				break;
			case 10:
				LogUtils.toDebugLog("case10", r.getContentAsString().toString());
				
				if(r.getContentAsString().contains("false")){//服务器的回复为{data："false"},表示不需要等待更新
					GlobalValue.isWaitingForUpdateApp = false;
					LogUtils.toDebugLog("更新版本的弹框", "result(10)中决定版本的弹框GlobalValue.isWaitingForUpdateApp："+GlobalValue.isWaitingForUpdateApp);
				}else{
					GlobalValue.isWaitingForUpdateApp = true;
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}
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
//   @InjectPullRefresh
//	public void call(int type) {
//		// 首页是没有下拉加载的
//		switch (type) {
//		case InjectView.PULL:
//			if (list != null) {
//				if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
////					PullToRefreshManager.getInstance().onFooterRefreshComplete();
////					PullToRefreshManager.getInstance().footerUnable();
//					CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");
//
//				} else {
//					refreshParams = new LinkedHashMap<>();
//					if (isChina) {//如果是国内活动，需要进行刷新操作
//						refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
//						refreshParams.put("site", getSelectCity()[0]);
//						refreshParams.put("intelligent", "home");
//					} else {
//						refreshParams = null;
//						refreshUrl = String.format(GlobalValue.URL_ARTICLE_PROMOTE,getSelectCity()[0]);
//					}
//					refreshCurrentList(list.getNext_page_url(), refreshParams,1, lv_home_list);
//				}
//			}else{//针对第一次进来用户急于去刷新操作，但此时的list对象是为空的
//				CustomToast.show(activity, "让网速飞一会儿，biu~biu~", "总裁大大，请给我一首歌的时间");
//				/*refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
//				refreshParams.put("site", getSelectCity()[0]);
//				refreshParams.put("intelligent", "home");
//				refreshCurrentList(refreshUrl, refreshParams,1, lv_home_list);
//				PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
//
//			}
//			break;
//		}
//	}
   class MyHomeAdPagerAdapter extends PagerAdapter {
		private Context context;
		/*private ArrayList<HomeAdPhotoData> mList;//传递进入数据源*/		
		
		private ArrayList<HashMap<String,String>> mList;//传递进入数据源
		private int adPhotoNum;

		public MyHomeAdPagerAdapter(ArrayList<HashMap<String,String>> homePhotoList) {//TODO
			this.mList = homePhotoList;
			this.adPhotoNum = homePhotoList.size();
		}
		@Override
		public int getCount() {
			/*return mList.size();*/
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView imageView = new ImageView(activity);
			imageView.setBackgroundResource(R.drawable.default_details);
			
			imageView.setScaleType(ScaleType.CENTER_CROP);
			
			/* 暂时将 根据uri加载图片的方法停掉，但以后需打开
			 * loadPhoto(mList.get(position).getPhoto(), imageView);//把图片放到container中的ImageView控件上
			 */
			
//			if(position%adPhotoNum==0){
//				imageView.setBackgroundResource(R.drawable.home_ad_poster1);
//			}else if(position%adPhotoNum==1){
//				imageView.setBackgroundResource(R.drawable.home_ad_poster2);
//			}else if(position%adPhotoNum==2){
//				imageView.setBackgroundResource(R.drawable.home_ad_poster3);
//			}
			
			loadPhoto(mList.get(position%adPhotoNum).get("photoDataPhotoUrl"), imageView);
			
			/*
			 * 以后的服务器返回回来的数据需要拥有这张图外接的连接地址，目前暂时将其设置为点击第二张图片进入私人秘书页
			   imageView.setOnTouchListener(new MyOnTouchListener());
			   touch事件很恶心地拦截掉了点击事件的DOWN操作*/
			imageView.setOnTouchListener(new MyOnTouchListener());
			
			imageView.setOnClickListener(new MyOnClickListener(position));
			
			container.addView(imageView);
			/* 针对特殊效果的JazzyViewPager调用的方法
			 * vp_home_ad.setObjectForPosition(imageView, position);
			 */
			return imageView;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
  
   /**
    * 触摸事件未被使用，由于其DOWN操作和点击事件产生
    * @author Yeun.Zhang
    *
    */
	@SuppressLint("ClickableViewAccessibility") 
	class MyOnTouchListener implements OnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handler.removeCallbacksAndMessages(null);
				handler.removeMessages(0);
				LogUtils.toDebugLog("点击", "onTouch中的点击");
				break;
				
			case MotionEvent.ACTION_CANCEL:
				if(homePhotoList.size()>1){
					handler.postDelayed(new InternalTask(), 5000);
					LogUtils.toDebugLog("点击", "onTouch中的ACTION_CANCEL");
				}
				break;
			case MotionEvent.ACTION_UP:
				if(homePhotoList.size()>1){
					handler.postDelayed(new InternalTask(), 5000);
					LogUtils.toDebugLog("点击", "onTouch中的ACTION_UP");
				}
				break;
			}
			/*return true;*/
			return false;
		}
	}
	class InternalTask implements Runnable{
		@Override
		public void run() {
			handler.sendEmptyMessage(0);
		}
	}
	
	@SuppressLint("HandlerLeak")
	class InternalHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what){
				case 10:
					ToggleDialogByUserStatus();
					break;	
				default:
					/**
					 * mScroller.setmDuration(3000);
					 * */
					/*int nextIndex = (vp_home_ad.getCurrentItem()+1)%homeAdPhotoList.size();*/
					int nextIndex = vp_home_ad.getCurrentItem()+1;
					//vp_home_ad.setFadingEdgeLength(100);
					vp_home_ad.setCurrentItem(nextIndex);
					handler.postDelayed(new InternalTask(), 5000);//自己给自己发信息
				    break;
			}
		}
	}
	
	//页面图片的点击事件
	class MyOnClickListener implements OnClickListener{
		public int mPosition ;
		private Bundle bundleToGrab;
		public MyOnClickListener(int position){
			this.mPosition = position;
		}
		@Override
		public void onClick(View v) {
			LogUtils.toDebugLog("点击", "点击监听中的点击事件");
			
//			if(mPosition%3 == 0){
//				MainFragment mineFragment = new MainFragment(1);
//				FragmentEntity fEntity = new FragmentEntity();
//				fEntity.setFragment(mineFragment);
//				EventBus.getDefault().post(fEntity);
//			}else if(mPosition%3 == 1){
//				ActivityFragment activityFragment = new ActivityFragment();
//				Bundle args = new Bundle();
//				args.putInt("type", R.string.investment);
//				activityFragment.setArguments(args);
//				FragmentEntity fEntity = new FragmentEntity();
//				fEntity.setFragment(activityFragment);
//				EventBus.getDefault().post(fEntity);
//			}else if(mPosition%3 == 2){
//				ActivityFragment activityFragment = new ActivityFragment();
//				Bundle args = new Bundle();
//				args.putInt("type", R.string.integral);
//				activityFragment.setArguments(args);
//				FragmentEntity fEntity = new FragmentEntity();
//				fEntity.setFragment(activityFragment);
//				EventBus.getDefault().post(fEntity);
//			}
			
			
		 /* hashMap.put("photoDataPhotoUrl",String.valueOf(data.getPhoto())); 
	    	hashMap.put("photoDataTag",String.valueOf(data.getTag())); 
	    	hashMap.put("photoDataActivityId",String.valueOf(data.getActivity_id())); 
	    	hashMap.put("photoDataShopId",""+String.valueOf(data.getShop_id())); */
			
			
			
//			板块为必填的0-9之间的数字。0为新品曝光，1-8代表app首页下的八大模块位置，9代表具体的活动，活动id与商店id需同时有且此时板块只能为9，
//			板块为0-8时，后面的活动id与门店id不写，10私人小秘书，11优惠券，数字之间用逗号(英文字符)隔开。例如跳转购物狂欢模块，只需填写1，
//			跳转参某个活动的门店应写：9,1246,5786
			
			int tag = Integer.valueOf(homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataTag"));
			LogUtils.toDebugLog("photoDataTag", "photoDataTag的值为： "+tag);
			LogUtils.toDebugLog("photoDataTag", "photoDataTag的值为： "+homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataTag"));
			
			if("".equals(homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataTag"))||
					homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataTag")==null){
				//NO-OP
			}else{
				Fragment fragment =new Fragment();
				Bundle bundle = new Bundle();
				
				switch(tag){
				case 0://新品曝光
					fragment = new NewBrandFragment();
					bundle.putString("type", "new");
					fragment.setArguments(bundle);	
					break;
					
				case 1://购物狂欢
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.shopping_carnival);
					fragment.setArguments(bundle);	
					break;
				case 2://美食餐饮
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.dining);
					fragment.setArguments(bundle);	
					break;
				case 3://积分兑换
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.integral);
					fragment.setArguments(bundle);	
					break;
				case 4://投资理财
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.investment);
					fragment.setArguments(bundle);	
					break;
				case 5://生活服务
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.life_service);
					fragment.setArguments(bundle);	
					break;
				case 6://休闲娱乐
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.entertainment);
					fragment.setArguments(bundle);	
					break;
				case 7://旅游度假
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.travel_holiday);
					fragment.setArguments(bundle);	
					break;
				case 8://汽车礼遇
					fragment = new ActivityFragment();
					bundle.putInt("type", R.string.courtesy_car);
					fragment.setArguments(bundle);	
					break;
					
				case 9://具体活动页面
					String activity_id = homePhotoList.get(mPosition % homePhotoList.size()).get("photoDataActivityId");
					String shop_id = homePhotoList.get(mPosition % homePhotoList.size()).get("photoDataShopId");
					fragment = new ActivityDetailFragment();
					bundle.putString("activityId", activity_id); 
					bundle.putString("shopId", shop_id);
					/*bundle.putString("activityId", 6643+""); 
					bundle.putString("shopId", 27246+"");*/
					fragment.setArguments(bundle);
					break;
				case 10://私人秘书页
					fragment = new MainFragment(1);
					break;
					
				case 11://优惠券页
					break;
					
					
				case 12:
					break;
					//NO-OP
				case 13:
					//NO-OP
					break;
				case 14:
					//NO-OP
					break;
				case 21:
					fragment = new PromoteDetailFragment();
					bundle.putString("loadUrl", homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataWebViewUrl"));
					fragment.setArguments(bundle);	
					break;
				case 31:
					//1.1 向服务器发送获取随机码的请求（或者在 webView界面进行请求），顺带上传一个 用户信息
//					fragment = new GrabRedPackFragment();
//					bundle.putString("loadUrl", homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataWebViewUrl"));
//					fragment.setArguments(bundle);
					GrabRedPackActivity grabRedPackActivity = new GrabRedPackActivity((ToLoginListener)activity);
					Intent intentToGrab = new Intent(activity,grabRedPackActivity.getClass());
					bundleToGrab = new Bundle();
//					bundleToGrab.putParcelable("activity", (Parcelable)activity);
					bundleToGrab.putString("loadUrl", homePhotoList.get(mPosition%homePhotoList.size()).get("photoDataWebViewUrl"));
					intentToGrab.putExtras(bundleToGrab);
					activity.startActivity(intentToGrab);
//					startActivityForResult(intentToGrab, 6666);
//					getActivity().overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
//					break;
					return;
				default:
					break;
				}
				FragmentEntity fragmentEntity = new FragmentEntity();
				fragmentEntity.setFragment(fragment);
				EventBus.getDefault().post(fragmentEntity);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
	
	 class HomeRefreshBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.refreshHome")){
				System.out.println("首页收到局部刷新的广播了");
				refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
				refreshKey = 1;
				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("intelligent", "home");
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_home_list);
				
				v.iv_card.setVisibility(View.VISIBLE);//原本右边银行卡可见
				v.iv_top_card.setVisibility(View.VISIBLE);//滑动出现的右边银行卡可见
				v.tv_home_experience.setVisibility(View.GONE);//原本  体验不可见
				v.tv_top_home_experience.setVisibility(View.GONE);//滑动出现的右边 体验二字 不可见
				v.rl_bg.setPressed(false);
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_gray);
				v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_gray);
				v.rl_top_bg.setPressed(false);
				
				/*adapter = null;*/
				isDeleteShopData = true;
				/*
				 * shopDataList.clear();
				   adapter.notifyDataSetChanged();
				 */
			}
			if(intent.getAction().equals("com.lansun.qmyo.refreshTheIcon")){
				v.iv_card.setVisibility(View.VISIBLE);//原本右边银行卡可见
				v.iv_top_card.setVisibility(View.VISIBLE);//滑动出现的右边银行卡可见
				v.tv_home_experience.setVisibility(View.GONE);//原本  体验不可见
				v.tv_top_home_experience.setVisibility(View.GONE);//滑动出现的右边 体验二字 不可见
				v.rl_bg.setPressed(false);
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_gray);
				v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_gray);
				v.rl_top_bg.setPressed(false);
				System.out.println("首页收到刷新Icon的广播了");
				
			}
			if(intent.getAction().equals("com.lansun.qmyo.refreshHomeList")){
				
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_home_list);
				
				v.iv_card.setVisibility(View.GONE);//原本右边银行卡不可见
				v.iv_top_card.setVisibility(View.GONE);//滑动出现的右边银行卡不可见
				v.tv_home_experience.setVisibility(View.VISIBLE);//原本  体验可见
				v.tv_top_home_experience.setVisibility(View.VISIBLE);//滑动出现的右边 体验二字 可见
				v.rl_bg.setPressed(false);
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_green);
				v.rl_top_bg.setBackgroundResource(R.drawable.circle_background_green);
				v.rl_top_bg.setPressed(false);
				System.out.println("首页收到来自ExperienceDialog页面的广播了");
			}
			
		}
	 }
	 
	 @Override
	protected void setProgress(View view) {
		 if (progress != null) {
				return;
			}
			loadView = view;
			LayoutParams lp = view.getLayoutParams();
			ViewParent parent = view.getParent();
			FrameLayout container = new FrameLayout(activity);
			ViewGroup group = (ViewGroup) parent;
			int index = group.indexOfChild(view);
			group.removeView(view);
			group.addView(container, index, lp);
			container.addView(view);
			if (inflater != null) {
				progress = inflater.inflate(R.layout.fragment_progress, null);
				progress_container = (LinearLayout) progress
						.findViewById(R.id.progress_container);

				progress_text = (TextView) progress.findViewById(R.id.progress_text);//动态猫头鹰底部显示：内容正在加载中
				progress_container.setOnClickListener(new OnClickListener() {

					private int justOneTimes;

					@Override
					public void onClick(View arg0) {
						try{
							//执行两个网络访问各一次
							
							if(justOneTimes==0){
								//1.1顶部广告的内容
								InternetConfig config = new InternetConfig();
								config.setKey(0);
								FastHttpHander.ajaxGet(GlobalValue.URL_HOME_AD + 310000, config, HomeFragment.this);//已去访问头部的数据
								
								//1.2 拿到首页底部列表的内容
								refreshCurrentList(refreshUrl, refreshParams, refreshKey,loadView);
								justOneTimes++;
							}
							
						}catch(Exception e){
							App.app.startActivity(new Intent(App.app,MainActivity.class));
						}
						
					}
				});
				GifMovieView loading_gif = (GifMovieView) progress.findViewById(R.id.loading_gif);
				loading_gif.setMovieResource(R.drawable.loading);
				container.addView(progress);
				progress_container.setTag(view);
				view.setVisibility(View.GONE);
			}
			group.invalidate();
		 
		 
		//super.setProgress(view);
	}

}
