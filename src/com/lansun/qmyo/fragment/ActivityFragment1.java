package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.google.gson.Gson;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.domain.position.City;
import com.lansun.qmyo.domain.position.Position;
import com.lansun.qmyo.domain.screening.DataScrolling;
import com.lansun.qmyo.domain.screening.Type;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lansun.qmyo.view.ViewRight;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;

public class ActivityFragment1 extends BaseFragment {

	private String HODLER_TYPE;

	@InjectView
	private View expandTabViewButtomLine;

	@InjectAll
	Views v;
	private View loadView;

	private ViewLeft viewLeft;

	private ViewLeft viewLeft2;

	private ViewMiddle viewMiddle;

	private ViewRight viewRight;

	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();

	int index = 0;
	private ActivityList activityList;
	private SearchAdapter activityAdapter;
	private ArrayList<HashMap<String, Object>> shopDataList = new ArrayList<HashMap<String, Object>>();

	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();

	@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), down = true, pull = true)
	private MyListView lv_activity_list;

	/**
	 * 是否为新品曝光
	 */
	private boolean IsNew;
	/**
	 * 全部服务信息
	 */
	private Position nearService;

	/**
	 * 智能排序
	 */
	private Intelligent intelligent;

	/**
	 * 全部的信息
	 */
	private Service AllService;

	/**
	 * 筛选
	 */
	private Type sxintelligent;

	private String position_bussness = "nearby";

	private String intelligentStr="intelligent";
	private String type;
	private int initType;

	private View tv_found_secretary;

	private boolean isPosition;
	private boolean justFirstClick = true;
	private boolean isFromNoNetworkViewTip = false;
	/*private boolean spyJustFirstClick = false;*/
	
	class Views {
		private View iv_card;
		private TextView tv_activity_title, tv_home_experience;
		private ExpandTabView expandtab_view;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private RelativeLayout rl_bg;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View customdialogprogress;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_activity, null);
		
		Handler_Inject.injectFragment(this, rootView);//Handler_Inject就会去调用invoke，invoke中会调用Inject_View,而Inject_View中又会调用applyTo()

		return rootView;
	}

	private String[] secretaryTitle;
	private String[] secretaryhint;
	private int[][] secretaryImages = new int[][] {
			{ R.drawable.details_figure01, R.drawable.details_figure02,
				R.drawable.details_figure03, R.drawable.details_figure04,
				R.drawable.details_figure05, R.drawable.details_cannot },
				{ R.drawable.details_shopping01, R.drawable.details_shopping02,
					R.drawable.details_shopping03,
					R.drawable.details_shopping04, R.drawable.details_cannot },
					{ R.drawable.details_party01, R.drawable.details_party02,
						R.drawable.details_party03, R.drawable.details_party04,
						R.drawable.details_cannot },
						{ R.drawable.details_life01, R.drawable.details_life02,
							R.drawable.details_life03, R.drawable.details_life04,
							R.drawable.details_cannot },
							{ R.drawable.details_abroad01, R.drawable.details_abroad02,
								R.drawable.details_cannot },
								{ R.drawable.details_financial01, R.drawable.details_financial02,
									R.drawable.details_cannot },
									{ R.drawable.details_card01, R.drawable.details_cannot } };
	private String typeStr;
	private int secretaryPosition;

	private View emptyView;
	private boolean isDownChange  = false;
	private boolean mIsHasChangeTheBankcardInMineBankcardPage = false;
	private CustomDialogProgress cPd = null;

	
	private long districtToRequestCurrentTimeMillis;
	private long intelligentToRequestCurrentTimeMillis;
	private long screenToRequestCurrentTimeMillis;
	private long eightPartToRequestCurrentTimeMillis;
	private long serviceToRequesturrentTimeMillis;
	private long eightPartToResponseCurrentTimeMillis;
	private long districtToResponseCurrentTimeMillis;
	private long intelligentToResponseCurrentTimeMillis;
	private long screenToResponseCurrentTimeMillis;
	private long serviceToResponseCurrentTimeMillis;

	private boolean isShowDialog = false;

	private boolean isShowFromInitData = false;

	private RelativeLayout noNetworkView;
	
	

	@InjectInit
	private void init() {
		secretaryTitle = getResources().getStringArray(R.array.secretary_title);
		secretaryhint = getResources().getStringArray(R.array.secretary_hint);
		int type = getArguments().getInt("type");
		initType = type;
		Log.i("type", "这次的type的值为"+ type);
		
		
		mIsHasChangeTheBankcardInMineBankcardPage = getArguments().getBoolean("isHasChangeTheBankcardInMineBankcardPage");
		Log.d("mIsHasChangeTheBankcardInMineBankcardPage", "这次的mIsHasChangeTheBankcardInMineBankcardPage的值为"+ mIsHasChangeTheBankcardInMineBankcardPage);

		if (type != 0) {
			switch (type) {
			case R.string.shopping_carnival:
				typeStr = "shopping";
				secretaryPosition = 1;
				HODLER_TYPE = "100000";
				break;
			case R.string.travel_holiday:
				typeStr = "travel";
				secretaryPosition = 0;
				HODLER_TYPE = "200000";
				break;
			case R.string.dining:
				typeStr = "party";
				secretaryPosition = 2;
				HODLER_TYPE = "300000";
				break;
			case R.string.courtesy_car:
				typeStr = "shopping";
				secretaryPosition = 1;
				HODLER_TYPE = "400000";
				break;
			case R.string.life_service:
				typeStr = "life";
				secretaryPosition = 3;
				HODLER_TYPE = "600000";
				break;
			case R.string.entertainment:
				secretaryPosition = 3;
				typeStr = "life";
				HODLER_TYPE = "500000";
				break;
			case R.string.integral:
				typeStr = "card";
				secretaryPosition = 6;
				HODLER_TYPE = "700000";
				break;
			case R.string.investment:
				secretaryPosition = 5;
				typeStr = "investment";
				HODLER_TYPE = "800000";
				break;
			}
			if (getArguments() != null) {
				IsNew = getArguments().getBoolean("IsNew");
				if (IsNew) {
					HODLER_TYPE = "000000";
				}

			}
			initData();//---------------------功能：
			/*spyJustFirstClick = true;*/
			justFirstClick = false;
					
			v.tv_activity_title.setText(type);
			System.out.println(v.tv_activity_title.getText());

		} else {
			v.tv_activity_title.setText("未知");
		}

		if ("true".equals(App.app.getData("isExperience"))) {
			v.rl_bg.setPressed(true);
			v.tv_home_experience.setVisibility(View.VISIBLE);
			v.iv_card.setVisibility(View.GONE);
		} else {
			v.iv_card.setVisibility(View.VISIBLE);
			v.tv_home_experience.setVisibility(View.GONE);
			v.rl_bg.setPressed(false);//含有“体验”两个字的灰色按钮隐藏掉
		}

		//TODO
		/*注意：
		 * progress_container就是所有的ListView所在的，那个最大的布局界面上
		 * 这样就可以巧妙的将那个Gif的动画优先设置到progress_container中，然后再将ListView对象放入进去
		 */
		progress_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if(justFirstClick){
					/*spyJustFirstClick = true;*/
					justFirstClick = false;
					initData();
				}
			}
		});
		
		TextView messageTextView = (TextView) v.customdialogprogress.findViewById(R.id.messageText);
		ImageView iv_gif_loadingprogress = (ImageView)  v.customdialogprogress.findViewById(R.id.iv_gif_loadingprogress);
    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();
		messageTextView.setText("请检查网络连接，确保联网后进入页面");
		v.customdialogprogress.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(!justFirstClick){
					isFromNoNetworkViewTip = true;
					refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+
				HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type="+ActivityFragment1.this.type+
				"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"&query="+"",
				null, refreshKey,lv_activity_list);
				}
			}
		});
		
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.rl_bg:// 进入我的银行卡
			Fragment fragment =  new MineBankcardFragment();
			Bundle bundle = new Bundle();
			if(getArguments().getBoolean("IsNew")){
				bundle.putBoolean("isFromNewPart", true);
				fragment.setArguments(bundle);
				/*CustomToast.show(activity, "来自于新品曝光板块", "下面进入我的银行卡页");*/
			}else{
				fragment.setArguments(bundle);
				bundle.putBoolean("isFromEightPart", true);
				bundle.putInt("type",initType);
				/*CustomToast.show(activity, "来自于八大板块页", "下面进入我的银行卡页");*/
			}
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		/*case R.id.customdialogprogress:
			TextView messageTextView = (TextView) v.customdialogprogress.findViewById(R.id.messageText);
			ImageView iv_gif_loadingprogress = (ImageView)  v.customdialogprogress.findViewById(R.id.iv_gif_loadingprogress);
	    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();
			messageTextView.setText("请检查网络连接，确保联网后进入页面");
			v.customdialogprogress.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(!justFirstClick){
						isFromNoNetworkViewTip = true;
						refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+
					HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type="+type+
					"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"&query="+"",
					null, refreshKey,lv_activity_list);
					}
				}
			});
			
			break;*/
		}
	}

	private void itemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (shopDataList.size() > 0) {
			HashMap<String, Object> item = shopDataList.get(position);
			String activityId = item.get("activityId").toString();
			String shopId = item.get("shopId").toString();
			EventBus bus = EventBus.getDefault();
			FragmentEntity event = new FragmentEntity();
			ActivityDetailFragment fragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("activityId", activityId);
			args.putString("shopId", shopId);
			fragment.setArguments(args);
			event.setFragment(fragment);
			bus.post(event);
		}
	}


	/**
	 * init()中被调用
	 * 一个功能强大的方法
	 */
	@SuppressLint("InflateParams")
	private void initData() {

		isShowFromInitData = true;
		holder_button.clear();//holder_button是一个HashMap
		mViewArray.clear();//mViewArray 为 new HashMap<Integer, View>()
		v.expandtab_view.removeAllViews();

		setProgress(lv_activity_list);
		startProgress();
		/*isShowDialog  = true;*/


		viewLeft = new ViewLeft(activity);
		viewLeft2 = new ViewLeft(activity);
		viewMiddle = new ViewMiddle(activity);
		viewRight = new ViewRight(activity);

		//判断Activity的类型是不是新品的内容，新内容时只展示三个模块，若为其他的八个板块时，那么需要的是展示四个TextView
		if (IsNew) {

			InternetConfig config = new InternetConfig();
			config.setKey(0);
			//---->调整接口连接器,此处需要将Token加入到config里面的head中去
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);

			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SERVICE
					+ HODLER_TYPE, config, this);
			// 附近 固定
			InternetConfig config1 = new InternetConfig();
			config1.setKey(1);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_DISTRICT
					+ App.app.getData("select_cityCode"), config1, this);
			// 智能排序
			InternetConfig config2 = new InternetConfig();
			config2.setKey(2);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT,
					config2, this);
		} else {//八大板块中任意一个点击进入后的list
			// 服务板块
			InternetConfig config = new InternetConfig();
			config.setKey(0);

			//---->调整接口连接器,此处需要将Token加入到config里面的head中去
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);

			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SERVICE
					+ HODLER_TYPE, config, this);
			
			
			serviceToRequesturrentTimeMillis = System.currentTimeMillis();
			Log.d("八大板块测试", "准备前往获取板块服务列表的数据 "+serviceToRequesturrentTimeMillis);

			// 附近 固定
			InternetConfig config1 = new InternetConfig();
			config1.setKey(1);

			Log.i("选择城市的code为:", App.app.getData("select_cityCode"));

			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_DISTRICT
					+ App.app.getData("select_cityCode"), config1, this);
			
			
			districtToRequestCurrentTimeMillis = System.currentTimeMillis();
			Log.d("八大板块测试", "准备前往获取商圈的数据 "+districtToRequestCurrentTimeMillis);


			// 智能排序
			InternetConfig config2 = new InternetConfig();
			config2.setKey(2);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT,config2, this);
			
			intelligentToRequestCurrentTimeMillis = System.currentTimeMillis();
			Log.d("八大板块测试", "准备前往获取智能排序的数据 "+intelligentToRequestCurrentTimeMillis);


			// 筛选
			InternetConfig config3 = new InternetConfig();
			config3.setKey(3);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SCREENING,config3, this);
			
			screenToRequestCurrentTimeMillis = System.currentTimeMillis();
			Log.d("八大板块测试", "准备前往获取筛选的数据 "+screenToRequestCurrentTimeMillis);
		}

		emptyView = inflater.inflate(R.layout.activity_search_empty, null);
		tv_found_secretary = emptyView.findViewById(R.id.tv_found_secretary);
		tv_found_secretary.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*SecretaryDetailFragment fragment = new SecretaryDetailFragment();*/
				/*SecretaryFragment fragment = new SecretaryFragment();*/
				MainFragment fragment = new MainFragment(1);
				
				Bundle args = new Bundle();
				args.putString("type", typeStr);
				args.putString("title", secretaryTitle[secretaryPosition]);
				args.putString("hint", secretaryhint[secretaryPosition]);
				args.putIntArray("images", secretaryImages[secretaryPosition]);
				fragment.setArguments(args);

				// type = arguments.getString("type");
				// title = arguments.getString("title");
				// hint = arguments.getString("hint");
				// images = arguments.getIntArray("images");
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);

			}
		});

		
		
		
		
		//当服务器返回的json中的nextpage_url为空的时候，ListView加上尾部的View操作
		//lv_activity_list.addFooterView(emptyView);

		initListener();//-->给上面四个TextView每个设置上选择的监听

		if(isShowFromInitData){//设计isShowFromInitData标签的原因是： 只有第一次进来时，才会有container的监听，在container没有消失的前提下，都只会走initData()方法间接地操作loadActivityList()
			                   //而筛选栏是直接的进行loadActivityList()的操作，那么在一进入页面就断网的环境下，是没有机会点击筛选栏的，自然也无法 弹出那个customDialogProgress
			isShowDialog = false;
		}
		
		loadActivityList();//-->加载活动的列表
		
		isShowDialog = true;
		


	}

	/**
	 * 对标题栏下面的四个分点击TextView模块设置对应的监听者
	 */
	private void initListener() {

		//智能排序模块
		viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {

			@Override
			public void getValue(String distance, String showText, int position) {
				intelligentStr = intelligent.getData().get(position).getKey();
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				PullToRefreshManager.getInstance().footerEnable();
				loadActivityList();
				onRefresh(viewMiddle, showText);
			}
		});

		//所有服务模块
		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {
			@Override
			public void getValue(String showText, int parentId, int position) {
				if (AllService.getData().get(parentId).getItems() == null) {
					onRefresh(viewLeft, showText);
					HODLER_TYPE = AllService.getData().get(parentId).getKey()
							+ "";
				} else if (AllService.getData().get(parentId).getItems()
						.get(position) != null) {
					HODLER_TYPE = AllService.getData().get(parentId).getItems()
							.get(position).getKey();
				}
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				PullToRefreshManager.getInstance().footerEnable();
				loadActivityList();
				onRefresh(viewLeft, showText);
			}
		});

		//附近服务模块
		viewLeft2.setOnSelectListener(new ViewLeft.OnSelectListener() {

			@Override
			public void getValue(String showText, int parentId, int position) {

				if (parentId == 0) {

					if (nearService.getData().get(parentId).getItems() == null) {
						onRefresh(viewLeft, showText);
						position_bussness = nearService.getData().get(parentId).getItems().get(position).getKey()+ "";
					} else if (nearService.getData().get(parentId).getItems()
							.get(position) != null) {
						position_bussness = nearService.getData()
								.get(parentId).getItems().get(position)
								.getKey();
					}
				} else {
					position_bussness = nearService.getData()
							.get(parentId).getItems().get(position).getKey();
				}
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				PullToRefreshManager.getInstance().footerEnable();
				loadActivityList();
				onRefresh(viewLeft2, showText);
			}
		});

		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText, int position) {
				type = sxintelligent.getData().get(position).getKey();
				shopDataList.clear();
				
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				PullToRefreshManager.getInstance().footerEnable();
				loadActivityList();
				onRefresh(viewRight, showText);
			}
		});
	}




	/**
	 * 加载活动列表,最后走的都是refreshCurrentList,只不过携带的参数不同
	 */
	private void loadActivityList() {
		
		if (isShowDialog){
			if(cPd == null ){
				/*GifMovieView loading_gif = (GifMovieView) progress.findViewById(R.id.loading_gif);
			loading_gif.setMovieResource(R.drawable.loading);
			pd = new ProgressDialog(activity);
			pd.setMessage("小迈努力加载中... ...");
			LayoutParams layoutParams = new LayoutParams(getActivity());
			pd.getWindow().addContentView(loading_gif, arg1)
			pd.setContentView(loading_gif);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);*/
				
				/*cPd = new CustomDialogProgress(activity);*/
				Log.d("dialog","生成新的dialog！");
				/*CustomDialogProgress.createDialog(App.app.getApplicationContext());*/
				//cPd.customDialogProgress(App.app.getApplicationContext());
				/*cPd.setMessage("刷新中");*/
				/*cPd.setCancelable(false);*/
				
				cPd = CustomDialogProgress.createDialog(activity);
				cPd.setCanceledOnTouchOutside(false);
				
				cPd.show();
			}else{
				cPd.show();
			}
		}
		
		// 活动列表
		refreshParams = new LinkedHashMap<>();
		if (getCurrentCity()[0].equals(getSelectCity()[0])) {
			isPosition = true;
			refreshParams.put("location", GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());//问题在这儿!这个location 拼接在了服务器访问接口里面
		}
		refreshParams.put("poistion",position_bussness);
		Log.e("position", "position_bussness="+position_bussness);
		if (IsNew) {// 新品曝光 固定为NEW
			refreshParams.put("type", "new");
			refreshParams.put("site", getSelectCity()[0]);
			refreshParams.put("service", HODLER_TYPE);
		} else {
			if (!TextUtils.isEmpty(type)) {
				refreshParams.put("type", type);
			}else {
				refreshParams.put("type", "");
			}
			refreshParams.put("service", HODLER_TYPE);
		}

		//		if (!TextUtils.isEmpty(intelligentStr)) {//目前貌似只有 智能排序 是可产生点击结果的,并且是展示 所有活动 的操作请求
		//		}
		refreshParams.put("intelligent", intelligentStr);
		//首先来获取到所有活动的列表,展示在litView上
		refreshParams.put("site", getSelectCity()[0]);
		/*refreshParams.put("site", "310000");//首先默认是在上海 310000*/	
		refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
		refreshKey = 4;
		
		eightPartToRequestCurrentTimeMillis = System.currentTimeMillis();
		Log.d("八大板块测试", "八大板块页面准备前去访问数据： "+eightPartToRequestCurrentTimeMillis);
		
		if (IsNew) {
			refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type=new"+"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),null, refreshKey,lv_activity_list);
		}else {
			refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type="+type+"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"&query="+"",null, refreshKey,lv_activity_list);
		}
	}



	/**
	 * 所有网络访问的结果处理
	 * @param r
	 */
	@InjectHttp
	private void result(ResponseEntity r) {
		PullToRefreshManager.getInstance().footerEnable();
		PullToRefreshManager.getInstance().headerEnable();
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
		

		if (r.getStatus() == FastHttp.result_ok) {
			if (mViewArray.size() >= 3 && activityList != null) {
				//endProgress();   //-->这玩意保证了gif动画的结束,但在这儿endProcess也太快了!
			}
			String name;

			switch (r.getKey()) {
			case 0:// 全部
				
				serviceToResponseCurrentTimeMillis = System.currentTimeMillis();
				Log.d("八大板块测试", "已获取板块服务列表的数据 "+serviceToResponseCurrentTimeMillis);
				Log.d("八大板块测试", "获取板块服务的数据等待时间为： "+(serviceToResponseCurrentTimeMillis-serviceToRequesturrentTimeMillis));
				
				
				Log.i("全部的数据", "拿到最左边列表的展示的全部数据!");
				AllService = Handler_Json.JsonToBean(Service.class,
						r.getContentAsString());
				name = AllService.getName();
				if (name == null) {
					name = AllService.getData().get(0).getName();
				}
				ArrayList<String> allGroup = new ArrayList<String>();
				SparseArray<LinkedList<String>> allChild = new SparseArray<LinkedList<String>>();
				for (int j = 0; j < AllService.getData().size(); j++) {
					LinkedList<String> chind = new LinkedList<String>();
					allGroup.add(AllService.getData().get(j).getName());
					ArrayList<ServiceDataItem> items = AllService.getData()
							.get(j).getItems();
					if (items != null) {
						for (ServiceDataItem item : items) {
							chind.add(item.getName());
						}
					}
					allChild.put(j, chind);
				}
				holder_button.put(0, name);
				viewLeft.setGroups(allGroup);
				viewLeft.setChildren(allChild);
				mViewArray.put(0, viewLeft);
				break;
			case 1:// 附近
				
				districtToResponseCurrentTimeMillis = System.currentTimeMillis();
				Log.d("八大板块测试", "已获取商圈的数据 "+districtToResponseCurrentTimeMillis);
				Log.d("八大板块测试", "获取商圈的数据等待时间为： "+(districtToResponseCurrentTimeMillis-districtToRequestCurrentTimeMillis));
				
				
				Log.i("附近的数据", "拿到附近的数据!");
				Gson gson=new Gson();
				nearService = gson.fromJson(r.getContentAsString(),Position.class);
				name = nearService.getName();
				if (name == null) {
					name = nearService.getData().get(0).getName();
				}
				ArrayList<String> nearGroup = new ArrayList<String>();
				SparseArray<LinkedList<String>> allNearChild = new SparseArray<LinkedList<String>>();
				for (int j = 0; j < nearService.getData().size(); j++) {
					LinkedList<String> chind = new LinkedList<String>();
					nearGroup.add(nearService.getData().get(j).getName());
					List<City> items = nearService.getData()
							.get(j).getItems();
					if (items != null) {
						for (City item : items) {
							chind.add(item.getName());
						}
					}
					allNearChild.put(j, chind);
				}
				holder_button.put(1, name);
				viewLeft2.setGroups(nearGroup);
				viewLeft2.setChildren(allNearChild);
				mViewArray.put(1, viewLeft2);
				break;
			case 2:// 智能排序
				
				intelligentToResponseCurrentTimeMillis = System.currentTimeMillis();
				Log.d("八大板块测试", "已获取智能排序的数据 "+intelligentToResponseCurrentTimeMillis);
				Log.d("八大板块测试", "获取智能排序的数据等待时间为： "+(intelligentToResponseCurrentTimeMillis-intelligentToRequestCurrentTimeMillis));
				
				
				Log.i("智能排序的数据", "拿到智能排序的数据!");
				intelligent = Handler_Json.JsonToBean(Intelligent.class,r.getContentAsString());

				name = intelligent.getName();
				ArrayList<String> sortGroup = new ArrayList<String>();
				ArrayList<Data> sortData = intelligent.getData();
				for (Data d : sortData) {
					if (IsNew) {
						if (!"银行卡优先".equals(d.getName().toString().trim())) {
							sortGroup.add(d.getName());
						}
					}else {
						sortGroup.add(d.getName());
					}
				}
				holder_button.put(2, name);
				viewMiddle.setItems(sortGroup);
				mViewArray.put(2, viewMiddle);
				break;

			case 3:// 筛选
				
				screenToResponseCurrentTimeMillis = System.currentTimeMillis();
				Log.d("八大板块测试", "已获取筛选排序的数据 "+screenToResponseCurrentTimeMillis);
				Log.d("八大板块测试", "获取筛选排序的数据等待时间为 ："+(screenToResponseCurrentTimeMillis-screenToRequestCurrentTimeMillis));
				
				sxintelligent = Handler_Json.JsonToBean(Type.class,r.getContentAsString());
				name = sxintelligent.getName();
				ArrayList<String> iconGroup = new ArrayList<String>();
				ArrayList<String> sxGroup = new ArrayList<String>();
				List<DataScrolling> sxData = sxintelligent.getData();
				for (DataScrolling d : sxData) {
					sxGroup.add(d.getName());
					iconGroup.add(d.getKey());
				}
				holder_button.put(3, name);
				viewRight.setICons(iconGroup);
				viewRight.setItems(sxGroup);
				mViewArray.put(3, viewRight);


				//				
				//				sxintelligent = Handler_Json.JsonToBean(Intelligent.class,r.getContentAsString());
				//				
				//				name = sxintelligent.getName();
				//				ArrayList<String> iconGroup = new ArrayList<String>();
				//				ArrayList<String> sxGroup = new ArrayList<String>();
				//				ArrayList<Data> sxData = sxintelligent.getData();
				//				for (Data d : sxData) {
				//					sxGroup.add(d.getName());
				//					iconGroup.add(d.getKey());
				//				}
				//				holder_button.put(3, name);
				//				viewRight.setICons(iconGroup);
				//				viewRight.setItems(sxGroup);
				//				mViewArray.put(3, viewRight);
				break;
			case 4:// 活动列表
				//TODO
				eightPartToResponseCurrentTimeMillis = System.currentTimeMillis();
				Log.d("八大板块测试", "八大板块页面去访问数据现已拿到数据内容的时间：  "+ eightPartToResponseCurrentTimeMillis);
				
				/*Log.d("八大板块测试", "八大板块页面去访问数据现已拿到数据内容的时间和请求数据的时间差为：  "+(eightPartToResponseCurrentTimeMillis-eightPartToRequestCurrentTimeMillis));*/
				Log.d("八大板块测试", "等待服务器回复数据的时间：  "+(eightPartToResponseCurrentTimeMillis-eightPartToRequestCurrentTimeMillis));
				
				if(cPd!=null){
					cPd.dismiss();
					cPd = null;
				}
				
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
					
				//从服务器端拿到数据
				activityList = Handler_Json.JsonToBean(ActivityList.class,
						r.getContentAsString());

				if (activityList.getData() != null) {//服务器返回回来的数据中的Data不为null

					if(isDownChange){//下拉刷新时,需要将数据重新获取,即将shopDataList清空掉
						shopDataList.clear();
						isDownChange = false;
					}

					//无论activityList是否为空 都应该在后面进行拼接上去,shopList不需要删除掉
					//shopDataList.clear();

					if (activityAdapter != null) {
						activityAdapter.notifyDataSetChanged();
					}

					for (ActivityListData data : activityList.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("tv_search_activity_name", data.getShop().getName());
						map.put("tv_search_activity_distance", data.getShop().getDistance());
						map.put("tv_search_activity_desc", data.getActivity().getName());
						map.put("iv_search_activity_head", data.getActivity().getPhoto());
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						shopDataList.add(map);
					}

					if (activityAdapter == null) {
						activityAdapter = new SearchAdapter(lv_activity_list,
								shopDataList, R.layout.activity_search_item);

						lv_activity_list.setAdapter(activityAdapter);
						expandTabViewButtomLine.setVisibility(View.VISIBLE);//当拿到数据加载到ListView上后，再将下面的Line线条展示出来
						
						long linkToAdapterCurrentTimeMillis = System.currentTimeMillis();
						Log.d("八大板块测试", "八大板块页面去访问数据现已拿到数据内容，已连接上适配器： "+linkToAdapterCurrentTimeMillis);
						/*Log.d("八大板块测试", "八大板块页面去访问数据现已拿到数据展示到界面上与获取数据的时间差：  "+(linkToAdapterCurrentTimeMillis-eightPartToResponseCurrentTimeMillis));*/
						Log.d("八大板块测试", "展示花费时间：  "+(linkToAdapterCurrentTimeMillis-eightPartToResponseCurrentTimeMillis));
						
						if(activityList.getData().size()<10){
							lv_activity_list.addFooterView(emptyView);
						}
						endProgress();//当ListView链接上适配器时,我们需要将gif的动画关掉

					} else {
						Log.d("八大板块测试", "八大板块页面去访问数据现已拿到数据内容，已连接上适配器(适配器复用)： "+System.currentTimeMillis());
						
						//adapter并不为空时
						activityAdapter.notifyDataSetChanged();
						if(activityList.getData().size()<10){
							lv_activity_list.addFooterView(emptyView);
						}
						/*activityAdapter.notifyDataSetChanged();*/
					}
					
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();

				} else {//因为上拉前去获取到的数据为null，此时需要将之前的值保留住并展示
					//lv_activity_list.setAdapter(null);
					activityAdapter.notifyDataSetChanged();
					lv_activity_list.addFooterView(emptyView);
					

					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();

				}
				//endProgress();
				break;

			}
			if (r.getKey() < 4) {
				progress_text.setText("正在搜索幸运中");
				if (IsNew) {
					if (holder_button.size() == 3) {
						v.expandtab_view.setValue(holder_button, mViewArray);
					}
				} else {
					if (holder_button.size() == 4) {
						v.expandtab_view.setValue(holder_button, mViewArray);
					}
				}
			}//r.getKey() < 4 的if


		} else {//如果服务器没有返回需要值回来 ---->网络未连接上外部网络//TODO
			progress_text.setText(R.string.net_error_refresh);
			//针对在断网后再次点击上部筛选栏的自己菜单时，做出的重复添加无效的 noNetworkView界面操作
		   /*try{
				lv_activity_list.removeFooterView(noNetworkView);
				v.customdialogprogress.setVisibility(View.VISIBLE);
			}catch(Exception e ){
			}*/
			
			lv_activity_list.setVisibility(View.GONE);
			v.customdialogprogress.setVisibility(View.VISIBLE);
			
			if(cPd!=null){//断网情况下，且还拥有了cPd，表明其走到了loadActivityList，表示之前成功使用过筛选栏进行列表选择过， 实际上是访问不到数据的 
				cPd.dismiss();
				cPd = null;
			    lv_activity_list.setVisibility(View.GONE);
				v.customdialogprogress.setVisibility(View.VISIBLE);
				/*setNetworkView();
				noNetworkView = setNetworkView();
				lv_activity_list.addFooterView(noNetworkView);*/
				
				
				//此时可断开上拉的操作
				PullToRefreshManager.getInstance().footerUnable();
				PullToRefreshManager.getInstance().headerUnable();
				
			}else{//注意下面的两个判断的安放顺序
				  lv_activity_list.setVisibility(View.GONE);
				  v.customdialogprogress.setVisibility(View.VISIBLE);
				
				if(isFromNoNetworkViewTip){//由ListView添加上的footerview画面点击产生的效果
					//筛选栏的点击在无网的状态下，点击提示画面，进行尝试联网操作，但依旧是返回统一的检查网络的提示画面
				/*	ImageView iv_gif_loadingprogress = (ImageView) noNetworkView.findViewById(R.id.iv_gif_loadingprogress);
			    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();*/
					lv_activity_list.setVisibility(View.GONE);
					v.customdialogprogress.setVisibility(View.VISIBLE);
					
					/*noNetworkView = setNetworkView();
					lv_activity_list.addFooterView(noNetworkView);
					PullToRefreshManager.getInstance().footerUnable();
					PullToRefreshManager.getInstance().headerUnable();*/
					/*isFromNoNetworkViewTip = false;*/
					return;
				}
				
				if(!justFirstClick){//针对 一进来就是无网状态，此时点击container会进行initData()的操作，此时点击一次后，justFirstClick=false，但是为了来网络时点击有效，那么很明显，不可禁掉点击监听，但可以禁掉 点击响应后的操作
					
					justFirstClick = true;
				}
			}
			lv_activity_list.setVisibility(View.GONE);
			v.customdialogprogress.setVisibility(View.VISIBLE);
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private RelativeLayout setNetworkView() {
				noNetworkView = (RelativeLayout) inflater.inflate(R.layout.customdialogprogress, null);
				TextView messageTextView = (TextView) noNetworkView.findViewById(R.id.messageText);
				ImageView iv_gif_loadingprogress = (ImageView) noNetworkView.findViewById(R.id.iv_gif_loadingprogress);
		    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();
				messageTextView.setText("请检查网络连接，确保联网后进入页面");
				noNetworkView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if(!justFirstClick){
							isFromNoNetworkViewTip = true;
							refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+
						HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type="+type+
						"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"&query="+"",
						null, refreshKey,lv_activity_list);
						}
					}
				});
				return noNetworkView;
	}

	/**
	 * 对上拉和下拉操作的处理
	 * @param type
	 */
	@InjectPullRefresh
	public void call(int type) {
		Log.i("上拉或者下拉的动作是否可以识别?", "说明动作能够被系统识别");
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			Log.i("上拉动作是否可以识别?", "说明上拉动作能够被识别");
			if (activityList != null) {
				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
				if (activityList.getNext_page_url()== "null"||TextUtils.isEmpty(activityList.getNext_page_url())) {

					
				   //当获取到的活动列表的数目少于10条时，顺带给listView 底部加上一个emptyView
					
					try{
						lv_activity_list.removeFooterView(emptyView);
					}catch(Exception e ){
						
					}
					//当我是在无网络环境的状态下，并且已经展示出网络获取失败的界面效果下，此时在进行
					//...在添加上无望的操作时，我们可以将其上拉的操作禁止掉
					
					
					lv_activity_list.addFooterView(emptyView);
					
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, "到底啦！", "小迈会加油搜集更多惊喜哦");

				} else {
					/* 下面这一步代码应该耽误了半天时间
					 * PullToRefreshManager.getInstance().onFooterRefreshComplete();
					 */

					refreshParams = new LinkedHashMap<>();
					refreshParams.put("location",GlobalValue.gps.toString());
					//定位成功后,则将location拼接到refreshParams中,但目前暂时将其关闭,对外不打开
					if (isPosition = true) {
						refreshParams.put("location",GlobalValue.gps.toString());
					}
					// params.put("distance", distance);
					refreshParams.put("site", getSelectCity()[0]);
					/*refreshParams.put("site", "310000");//默认只有上海城市*/	

					try {
						refreshParams.put("poistion", URLEncoder.encode(
								position_bussness, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					refreshParams.put("service", HODLER_TYPE);

					//更新当前页面的下一个页面时,前面的数据不应该被取消掉,应该拼接在后面
					refreshCurrentList(activityList.getNext_page_url(),refreshParams, 4, lv_activity_list);
				}
			}
			break;
		case InjectView.DOWN:
			if (activityList != null) {
				refreshParams = new LinkedHashMap<>();

				if (isPosition) {
					refreshParams.put("location", GlobalValue.gps.toString());
				}

				if (IsNew) {
					refreshParams.put("type", "new");
				}

				//refreshParams.put("distance", distance);
				refreshParams.put("site", getSelectCity()[0]);
				/*refreshParams.put("site", "310000");//默认先只跑上海市版本*/	

				try {
					refreshParams.put("poistion", URLEncoder.encode(
							position_bussness, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				refreshParams.put("service", HODLER_TYPE);

				isDownChange = true;//下拉更新的标志
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_activity_list);

				lv_activity_list.removeFooterView(emptyView);//不能忘了去除底部的emptyView
				/*lv_activity_list.invalidate();*/

			}else{
				CustomToast.show(activity, "ti", "activityList == null");
			}
			break;
		}
	}

	@Override
	public void onResume() {
		/*if (activityAdapter != null) {
			activityAdapter = null;
			shopDataList.clear();
		}*/
		super.onResume();

	}

	@Override
	public void onPause() {
		//下面的三句代码起到了当离开当前Fragment页面时，重置数据容器，提供下次访问展示的作用
		v.expandtab_view.onPressBack();
		/*shopDataList.clear();
		activityAdapter = null;*/
		super.onPause();
	}

	/**
	 * 在上方所有四个按钮设置的的监听者里面都一一设置了一个刷新的操作
	 * 对点击完成后,TextView里面本身的文字内容进行了刷新操作
	 * @param view
	 * @param showText
	 */
	private void onRefresh(View view, String showText) {
		v.expandtab_view.onPressBack();
		int position = getPositon(view);
		if (position >= 0&& !v.expandtab_view.getTitle(position).equals(showText)) {
			v.expandtab_view.setTitle(showText, position);
		}
	}

	private int getPositon(View tView) {
		for (int i = 0; i < mViewArray.size(); i++) {
			if (mViewArray.get(i) == tView) {
				return i;
			}
		}
		return -1;
	}

	
	@Override/*@InjectMethod(@InjectListener(ids = 2131296342, listeners = OnClick.class))*/
	protected void back() {
		if(mIsHasChangeTheBankcardInMineBankcardPage){
			Log.d("", "");
			HomeFragment homeFragment = new HomeFragment();
			FragmentEntity fEntity = new FragmentEntity();
			fEntity.setFragment(homeFragment);
			EventBus.getDefault().post(fEntity);
			return;
		}
		super.back();
	}

}
