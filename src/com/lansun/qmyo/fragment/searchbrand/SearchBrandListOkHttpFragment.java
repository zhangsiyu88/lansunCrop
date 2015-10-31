package com.lansun.qmyo.fragment.searchbrand;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.domain.position.City;
import com.lansun.qmyo.domain.position.Position;
import com.lansun.qmyo.domain.screening.DataScrolling;
import com.lansun.qmyo.domain.screening.Type;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.ActivityFragment;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.SearchBankCardFragment;
import com.lansun.qmyo.fragment.SearchFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;

import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lansun.qmyo.view.ViewRight;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class SearchBrandListOkHttpFragment extends BaseFragment implements OnClickListener,TextWatcher{
	private String HODLER_TYPE = "000000";
	private boolean isPull = false;
	private boolean first;
	private ActivityList list;
	public static String defaultVisitUrl = GlobalValue.URL_ALL_ACTIVITY;
	private SearchAdapter searchBankcardAdapter;
	
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

	private String intelligentStr="intelligent";
	private String type;
	private ViewLeft viewLeft2;
	private ViewMiddle viewMiddle;
	private ViewLeft viewLeft;
	private ViewRight viewRight;
	private HashMap<Integer, View> mViewArray = new HashMap<>();
	private HashMap<Integer, String> holder_button = new HashMap<>();
	private String query;
	private EditText et_home_search;
	private ExpandTabView expandtab_view;
	
	private RecyclingImageView iv_activity_back;
	private View emptyView;
	//	private ActivityList list;
	private ImageView del_search_content;
	private StringBuffer typeSb = new StringBuffer();
	private TextView search_tv_cancle;
	private String name;
	private boolean isExpandTag;
	private ArrayList<HashMap<String, Object>> datas;
	
	//TODO
	@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), down = true, pull = true)
	private MyListView lv_search_content;
	
/*	@InjectAll
	Views v;
	
	class Views {
		private View iv_card;
		private TextView tv_activity_title, tv_home_experience;
		private ExpandTabView expandtab_view;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private RelativeLayout rl_bg;
	}*/
	
	private Handler handleOkhttp=new Handler(){

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (holder_button.size() == 4) {
					expandtab_view.setValue(holder_button,mViewArray);
				}
				break;
			case 4:
				endProgress();
				if(searchBankcardAdapter == null){
					searchBankcardAdapter = new SearchAdapter(lv_search_content,datas, R.layout.activity_search_item);
					try{
						Log.i("","企图remove掉ListView中的尾布局");
						
						lv_search_content.removeFooterView(emptyView);
						Log.i("","remove掉ListView中的尾布局成功！");
						
					}catch(Exception e ){
//						CustomToast.show(activity, "出异常了", "异常已被抓！");
					
					}finally{
						Log.i("","remove掉ListView中的尾布局失败，抓住异常，还是展示获取的数据，但尾部跟上了emptyView！");
						lv_search_content.setAdapter(searchBankcardAdapter);
						endProgress();
					}
					
				}else{
					searchBankcardAdapter.notifyDataSetChanged();
				}
				PullToRefreshManager.getInstance().footerEnable();//拒绝此时的上拉和下拉操作
				PullToRefreshManager.getInstance().headerEnable();
				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				
				break;
			case 5:
				//endProgress();
				//setEmptityView(first,1);
				Log.i("case为5时，能否走到下面这一步？", "返回数据解析为空，故而在listView尾部添加emptyView");
				//emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
				lv_search_content.addFooterView(emptyView);
				
				lv_search_content.setAdapter(null);
				
				PullToRefreshManager.getInstance().footerUnable();//拒绝此时的上拉和下拉操作
				PullToRefreshManager.getInstance().headerUnable();
				break;
			}
		};
	};
	protected String position_bussness="nearby";
	private TextView tv_found_secretary;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_empty, null);
		tv_found_secretary = (TextView) emptyView.findViewById(R.id.tv_found_secretary);
		tv_found_secretary.setVisibility(View.VISIBLE);
		
		tv_found_secretary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SecretaryFragment homeFragment = new SecretaryFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(homeFragment);
				EventBus.getDefault().post(fEntity);
			}
		});
		super.onCreate(savedInstanceState);
	}
	private void initData() {
		if (getArguments()!=null) {
			query=getArguments().getString("query");
		}
		holder_button.clear();
		mViewArray.clear();
	}

	private void getTitleBanner() {
		viewLeft = new ViewLeft(activity);
		viewLeft2 = new ViewLeft(activity);
		viewMiddle = new ViewMiddle(activity);
		viewRight = new ViewRight(activity);
		
		
		//按服务类型进行范文
		OkHttp.asyncGet(GlobalValue.URL_SEARCH_HOLDER_SERVICE+"000000","Authorization","Bearer "+App.app.getData("access_token"),null,new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				Gson gson=new Gson();
				AllService = gson.fromJson(response.body().string(),Service.class);
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
				handleOkhttp.sendEmptyMessage(0);
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {

			}
		});
		
		//按商圈选择访问
		OkHttp.asyncGet(GlobalValue.URL_SEARCH_HOLDER_DISTRICT+App.app.getData("select_cityCode"),
				"Content-Type", "application/json", null, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				Gson gson=new Gson();
				nearService = gson.fromJson(response.body().string(), Position.class);
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
				handleOkhttp.sendEmptyMessage(0);
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {

			}
		});
		OkHttp.asyncGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT, "Content-Type", "application/json", null, new Callback() {

			@Override
			public void onResponse(Response response) throws IOException {
				Gson gson=new Gson();
				intelligent = gson.fromJson(response.body().string(), Intelligent.class);
				name = intelligent.getName();
				ArrayList<String> sortGroup = new ArrayList<String>();
				ArrayList<com.lansun.qmyo.domain.Data> sortData = intelligent.getData();
				for (com.lansun.qmyo.domain.Data d : sortData) {
					sortGroup.add(d.getName());
				}
				holder_button.put(2, name);
				viewMiddle.setItems(sortGroup);
				mViewArray.put(2, viewMiddle);
				handleOkhttp.sendEmptyMessage(0);
			}

			@Override
			public void onFailure(Request arg0, IOException arg1) {
			}
		});
		
		//去获取需要
		OkHttp.asyncGet(GlobalValue.URL_SEARCH_HOLDER_SCREENING, "Content-Type", "application/json", null, new Callback(){
			@Override
			public void onFailure(Request arg0, IOException arg1) {
			}
			@Override
			public void onResponse(Response response) throws IOException {
				Gson gson=new Gson();
				sxintelligent = gson.fromJson(response.body().string(), Type.class);
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
				handleOkhttp.sendEmptyMessage(0);
			}

		});
		setOnPullListeners();
	}
	private void startSearch(String query) {
		SearchFragment fragment = new SearchFragment();
		Bundle args = new Bundle();
		args.putString("query", query);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}
	
	
	
	private void initView( View view) {
		initData();
		
		iv_activity_back=(RecyclingImageView)view.findViewById(R.id.iv_activity_back);
		del_search_content=(ImageView)view.findViewById(R.id.del_search_content);
		et_home_search=(EditText)view.findViewById(R.id.et_home_search);
		
		et_home_search.setFocusable(false);//使其丧失焦点，即不让键盘自动弹起
		
		
		et_home_search.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (TextUtils.isEmpty(et_home_search.getText().toString())) {
						startSearch("");
					}
					startSearch(et_home_search.getText().toString().trim());
				}
			}
		});
		del_search_content.setOnClickListener(this);
		if (query!=null) {
			et_home_search.setText(query);
			del_search_content.setVisibility(View.VISIBLE);
			first=true;
		}else {
			et_home_search.setHint(R.string.please_enter_search_brand);
		}
		et_home_search.addTextChangedListener(this);
		expandtab_view=(ExpandTabView)view.findViewById(R.id.expandtab_view);
		expandtab_view.removeAllViews();//ExpandTab终于上场了！
		
		
		lv_search_content=(MyListView)view.findViewById(R.id.lv_search_content); //TODO	
		
		//初始化完成之后去请求网络获取所有数据

		startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,"nearby","intelligent","all",GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
		search_tv_cancle=(TextView)view.findViewById(R.id.search_tv_cancle);
		search_tv_cancle.setOnClickListener(this);
	}
	
	
	@InjectInit
	private void init() {
		
		
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		//TODO
	    View rootView = inflater.inflate(R.layout.activity_search_content, null);
	    rootView.setBackgroundColor(Color.argb(255, 235, 235, 235));
	    initView(rootView);
	    
	    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	    
	    Handler_Inject.injectFragment(this, rootView);//Handler_Inject就会去调用invoke，invoke中会调用Inject_View,而Inject_View中又会调用applyTo()
		
	   //这里面就寻找并定位到ListView对象，并且涉及到搜索startSearch(),其中startSearch()中就需要将listView和progress挂上钩，言即需要将ListView找到并和progress挂上钩
		//init();
	    getTitleBanner();
		setListener();
		return rootView;
	}
	
	
	/*
	 * listView 对象设置上点击事件
	 */
	private void setListener() {
		lv_search_content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				HashMap<String, Object> data = datas.get(position);
				String activityId = data.get("activityId").toString();
				String shopId = data.get("shopId").toString();
				ActivityDetailFragment fragment = new ActivityDetailFragment();
				Bundle args = new Bundle();
				args.putString("activityId", activityId);
				args.putString("shopId", shopId);
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});
	}
	/**
	 * 搜索内容监听
	 * 
	 * @param s
	 * @param start
	 * @param before
	 * @param count
	 */
	private void changeText(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			search_tv_cancle.setText(R.string.cancle);
			search_tv_cancle.setTextColor(Color.parseColor("#939393"));
			et_home_search.setHint(R.string.please_enter_search_brand);
		} else {
			search_tv_cancle.setText(R.string.search);
			search_tv_cancle.setTextColor(getResources().getColor(
					R.color.app_green1));
		}
	}
	/**
	 * 搜索为空的时候添加
	 */
	public void setEmptityView(Boolean isFirst,int visiable){
		if (isFirst) {
			emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_empty, null);
			emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
		if (visiable==1) {
			((ViewGroup) lv_search_content.getParent()).addView(emptyView);
		}else {
			((ViewGroup) lv_search_content.getParent()).removeView(emptyView);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_tv_cancle:
			if (!first) {
				first=true;
			}
			if (getString(R.string.cancle).equals(search_tv_cancle.getText())) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
				back();
			} else {
				query = et_home_search.getText().toString().trim();
				
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				
				
				//				startSearch();
				setEmptityView(first, 0);
			}
			break;
			// 大× 删除编辑框内容
		case R.id.del_search_content:
			et_home_search.setText("");
			getFragmentManager().popBackStack();
			break;
		case R.id.iv_activity_back:
			getFragmentManager().popBackStack();
			break;
		}
	}
	/**
	 * 开启搜索
	 * 
	 * @param string
	 */
	private void startSearch(String visitUrl, String site,String service,String positon,String intelligent,String type,String location,String query) {
		
		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
		
		setProgress(lv_search_content);
		startProgress();
		progress_text.setText("正在搜索幸运中");
		OkHttp.asyncGet(visitUrl+"site="+site+"&service="+service+"&position="+positon+"&intelligent="+intelligent+"&type="
		+type+"&location="+location+"&query="+query, "Authorization", "Bearer "+App.app.getData("access_token"), null, new Callback() {
			
			@Override
			public void onResponse(Response response) throws IOException {
			
				Gson gson=new Gson();
				list = gson.fromJson(response.body().string(), ActivityList.class);
				
				System.out.println("网络返回的数据 转化为 list后的对象为： "+list.getData().toString());
				//TODO
				if(isPull){
					isPull = false;
				}else{
					datas = new ArrayList<>();//重新new出来一个新的list
				}
				
				if (list.getData() != null && !list.getData().toString().equals("[]") ){
					for (ActivityListData data : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("tv_search_activity_name", data.getShop()
								.getName());
						map.put("tv_search_activity_distance", data.getShop()
								.getDistance());
						map.put("tv_search_activity_desc", data.getActivity()
								.getName());
						map.put("iv_search_activity_head", data.getActivity()
								.getPhoto());
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						datas.add(map);
					}
					handleOkhttp.sendEmptyMessage(4);
					
					return;
				}
				
				if(list.getData().toString().equals("[]")){
					handleOkhttp.sendEmptyMessage(5);//返回的数据解析后为空时
					Log.i("能否走到下面这一步？", "返回数据解析为空，故而在listView尾部添加emptyView");
					return;
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {

			}
		});
	}
	
	
	private void setOnPullListeners() {
		viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText,
					int position) {
				intelligentStr = intelligent.getData()
						.get(position).getKey();
				searchBankcardAdapter = null;//上面四个板块点击之前需要进行  设置为空的操作，为了将前面的页面数据给我清掉
				/*lv_search_content.removeView(emptyView);*/
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewMiddle, showText);
			}

		});

		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener(){
			@Override
			public void getValue(String showText, int parendId,
					int position) {
				if (AllService.getData().get(parendId).getItems() == null) {
					onRefresh(viewLeft, showText);
					HODLER_TYPE = AllService.getData()
							.get(parendId).getKey()
							+ "";
				} else if (AllService.getData().get(parendId)
						.getItems().get(position) != null) {
					
					HODLER_TYPE = AllService.getData().get(parendId).getItems().get(position).getKey();
					
					query=AllService.getData().get(parendId).getItems().get(position).getName();
//					onRefreshEd(AllService.getData()
//							.get(parendId).getItems().get(position)
//							.getName());
				}
				searchBankcardAdapter = null;
				
				/*lv_search_content.removeView(emptyView);*/
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewLeft, showText);
			}
		});
		
		viewLeft2.setOnSelectListener(new ViewLeft.OnSelectListener() {
			@Override
			public void getValue(String showText, int parendId,int position) {
				if (parendId == 0) {
					if (nearService.getData().get(parendId).getItems() == null) {
						onRefresh(viewLeft, showText);
						position_bussness = nearService.getData().get(parendId).getItems().get(position).getKey()+ "";
						Log.e("position", "=="+position_bussness);
					} else if (nearService.getData()
							.get(parendId).getItems()
							.get(position) != null) {
						position_bussness= nearService
								.getData().get(parendId)
								.getItems().get(position)
								.getKey();
						Log.e("position", "=="+position_bussness);
					}
				} else {
					position_bussness= nearService
							.getData().get(parendId)
							.getItems().get(position)
							.getKey();
					Log.e("position", "=="+position_bussness);
				}
				searchBankcardAdapter = null;
				/*lv_search_content.removeView(emptyView);*/
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewLeft2, showText);
			}
		});

		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText,int position) {
				
				type = sxintelligent.getData().get(position).getKey();
				if ("all".equals(type)) {
					typeSb.delete(0, typeSb.length());
				}
				if (!typeSb.toString().contains(type)) {
					typeSb.append(type + ",");
				}
				searchBankcardAdapter = null;
				/*lv_search_content.removeView(emptyView);*/
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewRight, showText);

			}
		});
	}
	protected void onRefreshEd(String name) {
		et_home_search.setText(name);
	}
	private void onRefresh(View view, String showText) {
		expandtab_view.onPressBack();
		int position = getPositon(view);
		if (position >= 0
				&& !expandtab_view.getTitle(position).equals(showText)) {
			expandtab_view.setTitle(showText, position);
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
	@Override
	public void onDestroy() {
		expandtab_view.onPressBack();
		super.onDestroy();
	}
	@Override
	public void afterTextChanged(Editable s) {

	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		changeText(s, start, before, count);
	}
	
	
	
	
	
	/**
	 * 对上拉和下拉操作的处理
	 * @param type
	 */
	@InjectPullRefresh
	public void call(int type1) {
		Log.i("上拉或者下拉的动作是否可以识别?", "说明动作能够被系统识别");
		// 这里的type来判断是否是下拉还是上拉
		switch (type1) {
		case InjectView.PULL:
			Log.i("上拉动作是否可以识别?", "说明上拉动作能够被识别");
			/*CustomToast.show(activity, "上拉操作", "现在进行上拉操作！");*/
			if (list != null) {
				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
				  if (list.getNext_page_url()== "null"||TextUtils.isEmpty(list.getNext_page_url())) {
					
					//lv_search_content.addFooterView(emptyView);
					
					 //setEmptityView(true, 1);
					  
					 lv_search_content.addFooterView(emptyView);
					
					Log.e("Tag","应该已经添加了空的View");
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, "到底啦！", "该关键词下暂时只有这么多内容");
					
				} else {
					String nextPageUrl = list.getNext_page_url();
					/*startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,"nearby","intelligent","all",GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);*/
					
					startSearch(nextPageUrl,getSelectCity()[0], HODLER_TYPE, position_bussness, intelligentStr, type, GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(), query);
					isPull = true;
					
				}
			}
			break;
			
			
		case InjectView.DOWN://去做刷新操作
		
			startSearch(defaultVisitUrl,getSelectCity()[0], HODLER_TYPE, position_bussness, intelligentStr, type, GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(), query);
			searchBankcardAdapter = null;
				//暂时为空
			/*CustomToast.show(activity, "下拉操作", "现在进行下拉操作！");*/
			PullToRefreshManager.getInstance().footerEnable();
			lv_search_content.removeFooterView(emptyView);
			break;
		}
	}
}
