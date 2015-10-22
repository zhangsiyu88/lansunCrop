package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
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
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lansun.qmyo.view.ViewRight;
import com.lansun.qmyo.R;

public class ActivityFragment extends BaseFragment {

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
	private Service nearService;

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
	private Intelligent sxintelligent;

	private String position = "nearby";

	private String intelligentStr;
	private String type;

	private View tv_found_secretary;

	private boolean isPosition;

	class Views {
		private View iv_card;
		private TextView tv_activity_title, tv_home_experience;
		private ExpandTabView expandtab_view;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private RelativeLayout rl_bg;
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

	@InjectInit
	private void init() {
		secretaryTitle = getResources().getStringArray(R.array.secretary_title);
		secretaryhint = getResources().getStringArray(R.array.secretary_hint);
		int type = getArguments().getInt("type");
		
		Log.i("type", "这次的type的值为"+ type);
		
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

		/*注意：
		 * progress_container就是所有的ListView所在的，那个最大的布局界面上
		 * 这样就可以巧妙的将那个Gif的动画优先设置到progress_container中，然后再将ListView对象放入进去
		 * 
		 */
		progress_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				initData();
			}
		});
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.rl_bg:// 进入我的银行卡
			Fragment fragment;
			fragment = new MineBankcardFragment();
			//new MineBankcardFragment();
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		}
	}

	private void itemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
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
		
		holder_button.clear();//holder_button是一个HashMap
		mViewArray.clear();//mViewArray 为 new HashMap<Integer, View>()
		v.expandtab_view.removeAllViews();
		
		setProgress(lv_activity_list);
		startProgress();

		
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
			
			
			// 附近 固定
			InternetConfig config1 = new InternetConfig();
			config1.setKey(1);
			
			Log.i("选择城市的code为:", App.app.getData("select_cityCode"));
			
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_DISTRICT
					+ App.app.getData("select_cityCode"), config1, this);
			
			
			// 智能排序
			InternetConfig config2 = new InternetConfig();
			config2.setKey(2);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT,config2, this);
			
			
			// 筛选
			InternetConfig config3 = new InternetConfig();
			config3.setKey(3);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SCREENING,config3, this);
		}

		emptyView = inflater.inflate(R.layout.activity_search_empty, null);
		tv_found_secretary = emptyView.findViewById(R.id.tv_found_secretary);
		tv_found_secretary.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SecretaryDetailFragment fragment = new SecretaryDetailFragment();
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
		
		loadActivityList();//-->加载活动的列表
		
		
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
						ActivityFragment.this.position = nearService.getData().get(parentId).getKey()+ "";
					} else if (nearService.getData().get(parentId).getItems()
							.get(position) != null) {
						ActivityFragment.this.position = nearService.getData()
								.get(parentId).getItems().get(position)
								.getKey();
					}
				} else {
					ActivityFragment.this.position = nearService.getData()
							.get(parentId).getItems().get(position).getKey();
				}
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
				}
				loadActivityList();
				onRefresh(viewLeft2, showText);
			}
		});

		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText, int position) {
				type = sxintelligent.getData().get(position).getKey();
				shopDataList.clear();
				loadActivityList();
				onRefresh(viewRight, showText);
			}
		});
	}

	
	
	
	/**
	 * 加载活动列表,最后走的都是refreshCurrentList,只不过携带的参数不同
	 */
	private void loadActivityList() {
		// 活动列表
		refreshParams = new LinkedHashMap<>();
		if (getCurrentCity()[0].equals(getSelectCity()[0])) {
			isPosition = true;
			Log.i("ActivityFragment的loadActivityList()方法中未获取列表上交的location值: ",GlobalValue.gps.toString());
			refreshParams.put("location", GlobalValue.gps.toString());//问题在这儿!这个location 拼接在了服务器访问接口里面
		}
		
		try {
			refreshParams.put("poistion",URLEncoder.encode(ActivityFragment.this.position, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

			if (IsNew) {// 新品曝光 固定为NEW
				refreshParams.put("type", "new");
				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("service", HODLER_TYPE);
			} else {
				if (!TextUtils.isEmpty(type)) {
					refreshParams.put("type", type);
				}
				refreshParams.put("service", HODLER_TYPE);
			}
		
		if (!TextUtils.isEmpty(intelligentStr)) {//目前貌似只有 智能排序 是可产生点击结果的,并且是展示 所有活动 的操作请求
			refreshParams.put("intelligent", intelligentStr);
		}
		
		
		//首先来获取到所有活动的列表,展示在litView上
		refreshParams.put("site", getSelectCity()[0]);
		/*refreshParams.put("site", "310000");//首先默认是在上海 310000*/	
		refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
		refreshKey = 4;
		refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_activity_list);
	}

	
	
	/**
	 * 所有网络访问的结果处理
	 * @param r
	 */
	@InjectHttp
	private void result(ResponseEntity r) {
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();

		if (r.getStatus() == FastHttp.result_ok) {
			if (mViewArray.size() >= 3 && activityList != null) {
				//endProgress();   //-->这玩意保证了gif动画的结束,但在这儿endProcess也太快了!
			}
			String name;
			
			switch (r.getKey()) {
			case 0:// 全部
				
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
				Log.i("附近的数据", "拿到附近的数据!");
				
				nearService = Handler_Json.JsonToBean(Service.class,
						r.getContentAsString());
				name = nearService.getName();
				if (name == null) {
					name = nearService.getData().get(0).getName();
				}
				ArrayList<String> nearGroup = new ArrayList<String>();
				SparseArray<LinkedList<String>> allNearChild = new SparseArray<LinkedList<String>>();
				for (int j = 0; j < nearService.getData().size(); j++) {
					LinkedList<String> chind = new LinkedList<String>();
					nearGroup.add(nearService.getData().get(j).getName());
					ArrayList<ServiceDataItem> items = nearService.getData().get(j).getItems();
					if (items != null) {
						for (ServiceDataItem item : items) {
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
				Log.i("智能排序的数据", "拿到智能排序的数据!");
				intelligent = Handler_Json.JsonToBean(Intelligent.class,r.getContentAsString());
				
				name = intelligent.getName();
				ArrayList<String> sortGroup = new ArrayList<String>();
				ArrayList<Data> sortData = intelligent.getData();
				for (Data d : sortData) {
					sortGroup.add(d.getName());
				}
				holder_button.put(2, name);
				viewMiddle.setItems(sortGroup);
				mViewArray.put(2, viewMiddle);
				break;

			case 3:// 筛选
				Log.i("筛选的数据", "拿到筛选的数据!");
				
				sxintelligent = Handler_Json.JsonToBean(Intelligent.class,r.getContentAsString());
				
				name = sxintelligent.getName();
				ArrayList<String> iconGroup = new ArrayList<String>();
				ArrayList<String> sxGroup = new ArrayList<String>();
				ArrayList<Data> sxData = sxintelligent.getData();
				for (Data d : sxData) {
					sxGroup.add(d.getName());
					iconGroup.add(d.getKey());
				}
				holder_button.put(3, name);
				viewRight.setICons(iconGroup);
				viewRight.setItems(sxGroup);
				mViewArray.put(3, viewRight);
				break;
			
				
				
			case 4:// 活动列表
				//TODO
				Log.i("进入界面时,能不能拿到数据","能拿到!但不展示出来是个问题!");
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
						
						Log.i("烦死了", "分明能走到setAdapter这儿啊!!!");
						
						endProgress();//当ListView链接上适配器时,我们需要将gif的动画关掉
						
					} else {
						//adapter并不为空时
						activityAdapter.notifyDataSetChanged();
					}
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					
				} else {//因为上拉前去获取到的数据为null，此时需要将之前的值保留住并展示
					//lv_activity_list.setAdapter(null);
					activityAdapter.notifyDataSetChanged();
					
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
			
			
	 } else {//如果服务器没有返回需要值回来，提示为 网络错误信息
			progress_text.setText(R.string.net_error_refresh);
		}
		
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
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
					
					lv_activity_list.addFooterView(emptyView);
					
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
					
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
								ActivityFragment.this.position, "utf-8"));
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
							ActivityFragment.this.position, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				refreshParams.put("service", HODLER_TYPE);
				
				isDownChange = true;//下拉更新的标志
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_activity_list);
				
				/*lv_activity_list.removeView(emptyView);//不能忘了去除底部的emptyView
				lv_activity_list.invalidate();*/
				
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
	/*	v.expandtab_view.onPressBack();
		shopDataList.clear();
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

	
}
