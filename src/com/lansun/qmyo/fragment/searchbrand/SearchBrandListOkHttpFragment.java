package com.lansun.qmyo.fragment.searchbrand;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
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
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.base.BackHandedFragment;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.position.City;
import com.lansun.qmyo.domain.position.Position;
import com.lansun.qmyo.domain.screening.DataScrolling;
import com.lansun.qmyo.domain.screening.Type;
import com.lansun.qmyo.domain.service.ServiceData;
import com.lansun.qmyo.domain.service.ServiceRoot;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.SearchFragment;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.EightPartActivityAdapter;
import com.lansun.qmyo.view.ActivityMyListView;
import com.lansun.qmyo.view.ActivityMyListView.OnRefreshListener;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lansun.qmyo.view.ViewRight;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@SuppressLint("InflateParams") public class SearchBrandListOkHttpFragment extends BackHandedFragment implements OnClickListener,TextWatcher{
	private final String TAG=SearchBrandListOkHttpFragment.class.getSimpleName();
	private String HODLER_TYPE = "000000";
	private boolean isPull = false;
	private boolean first;
	private ActivityList list = new ActivityList();
	public static String defaultVisitUrl = GlobalValue.URL_ALL_ACTIVITY;
	/*private SearchAdapter searchBankcardAdapter;*/
	private EightPartActivityAdapter searchBankcardAdapter;

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
	private ServiceRoot root;
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
	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();
	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();
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
	private ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();

	/*@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), down = true, pull = true)
	private MyListView lv_search_content;*/
	
	@InjectView
	private ActivityMyListView lv_search_content;
	protected boolean allready;
	private boolean justFirstClick = true;
	private boolean isFromNoNetworkViewTip = false;
	
	/**
	 * 处理okhttp的网络返回请求
	 */
	private Handler handleOkhttp=new Handler(){

		private long insertDataCurrentTimeMillis;

		public void handleMessage(android.os.Message msg) {  //TODO
			lv_search_content.setVisibility(View.VISIBLE);
			
			switch (msg.what) {
			case 0:
				break;
			case 1:
				endProgress();
				lv_search_content.setVisibility(View.VISIBLE);
				lv_search_content.onLoadMoreFished();
				lv_search_content.onRefreshFinshed(true);
				lv_search_content.setNoHeader(false);
				
				if(cPd!=null){
					cPd.dismiss();
					cPd = null;
				}
				if(searchBankcardAdapter == null){
					/*searchBankcardAdapter = new SearchAdapter(lv_search_content,datas, R.layout.activity_search_item);*/
					searchBankcardAdapter = new EightPartActivityAdapter(activity,datas);
					
					if(list.getData().size()<10){
						try{
							lv_search_content.removeFooterView(emptyView);
							lv_search_content.removeFooterView(noNetworkView);
						}catch(Exception e ){
							
						}
//						lv_search_content.addFooterView(emptyView);
//						lv_search_content.setAdapter(searchBankcardAdapter);
//						CustomToast.show(activity, "到底啦！", "该关键词下暂时只有这么多内容");
//						lv_search_content.onLoadMoreOverFished();
						
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司,加上小秘书提示图
							lv_search_content.setNoHeader(true);//一进来少于10条，则无法刷新操作
				            lv_search_content.onLoadMoreOverFished();
				            lv_search_content.addFooterView(emptyView);
				          }else{
				            //DO-OP
				          }
						lv_search_content.setAdapter(searchBankcardAdapter);
						
						
						insertDataCurrentTimeMillis = System.currentTimeMillis();
						Log.d("全局搜索测试", "数据放到界面上的时间点 "+insertDataCurrentTimeMillis);
						Log.d("全局搜索测试", "数据放到界面上的时间差 "+(insertDataCurrentTimeMillis - getRespCurrentTimeMillis));
						
					}else{													//不等于list的Data数组的值大于等于10时，正常刷新
						try{
							lv_search_content.removeFooterView(emptyView);
							lv_search_content.removeFooterView(noNetworkView);
						}catch(Exception e ){
						}finally{
							lv_search_content.setAdapter(searchBankcardAdapter);
							insertDataCurrentTimeMillis = System.currentTimeMillis();
							Log.d("全局搜索测试", "数据放到界面上的时间点 "+insertDataCurrentTimeMillis);
							Log.d("全局搜索测试", "数据放到界面上的时间差 "+(insertDataCurrentTimeMillis - getRespCurrentTimeMillis));
							/*endProgress();*/
						}
					}

					
				}else{//searchBankcardAdapter已经存在
					if(list.getData().size()<10){
						try{
							lv_search_content.removeFooterView(emptyView);
							lv_search_content.removeFooterView(noNetworkView);
						}catch(Exception e ){
							
						}
//						lv_search_content.addFooterView(emptyView);
//						CustomToast.show(activity, "到底啦！", "该关键词下暂时只有这么多内容");
//						lv_search_content.onLoadMoreOverFished();
						
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司,加上小秘书提示图
				            lv_search_content.onLoadMoreOverFished();
				            lv_search_content.addFooterView(emptyView);
				          }else{
				            //DO-OP
				          }
 					searchBankcardAdapter.notifyDataSetChanged();
					}else{
						try{
							lv_search_content.removeFooterView(emptyView);
							lv_search_content.removeFooterView(noNetworkView);
						}catch(Exception e ){
						}finally{
							searchBankcardAdapter.notifyDataSetChanged();
						}
					}
				}
				
				first_enter = Integer.MAX_VALUE;
				
				
//				PullToRefreshManager.getInstance().footerEnable();
//				PullToRefreshManager.getInstance().headerEnable();
//				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//				PullToRefreshManager.getInstance().onFooterRefreshComplete();

				break;
			case 2:
				
				LogUtils.toDebugLog("Search", "搜索为空 ，底部应该出现小猫头鹰");
				
				lv_search_content.setNoHeader(true);
				
				endProgress();
				if(cPd!=null){
					cPd.dismiss();
					cPd = null;
				}
				//setEmptityView(first,1);
				//emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
				lv_search_content.setVisibility(View.VISIBLE);
				//进来第一次搜索即为 空
				if(searchBankcardAdapter==null){
					/*searchBankcardAdapter = new SearchAdapter(lv_search_content,datas, R.layout.activity_search_item);*/
					searchBankcardAdapter = new EightPartActivityAdapter(activity,datas);
					//1.
					//lv_search_content.setAdapter(searchBankcardAdapter);
					try{
						lv_search_content.removeFooterView(emptyView);
					}catch(Exception exception ){
						//lv_search_content.addFooterView(emptyView);
					}
					lv_search_content.addFooterView(emptyView);
					lv_search_content.onLoadMoreOverFished();
					
					//2.
					lv_search_content.setAdapter(searchBankcardAdapter);
				}else{//之前的搜索是有值的，只不过接下来的搜索报空了而已
					
					try{
						lv_search_content.removeFooterView(emptyView);
					}catch(Exception exception ){
						//lv_search_content.addFooterView(emptyView);
					}
					lv_search_content.addFooterView(emptyView);
					lv_search_content.onLoadMoreOverFished();
					lv_search_content.setAdapter(searchBankcardAdapter);
					//searchBankcardAdapter.notifyDataSetChanged();
				}
				
//				PullToRefreshManager.getInstance().footerUnable();//拒绝此时的上拉和下拉操作
//				PullToRefreshManager.getInstance().headerUnable();
				break;
			case 3:
				setFirstValue();
				break;
			case 4:
				setFirstValue();
				break;
			case 5:
				setFirstValue();
				break;
			case 6:
				setFirstValue();
				break;
			case 7:
				setFirstValue();
				break;
			case 8:
				setFirstValue();
				break;
			case 9:
				setFirstValue();
				break;
			case 10:
				setFirstValue();
				break;
				
			case 11:
				//无网络的时候进行的操作//TODO
				//CustomToast.show(activity, "提示", "断网 ");
				progress_text.setText(R.string.net_error_refresh);
				lv_search_content.setVisibility(View.VISIBLE);
				
				//针对在断网后再次点击上部筛选栏的自己菜单时，做出的重复添加无效的 noNetworkView界面操作
				try{
					lv_search_content.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				try{
					lv_search_content.removeFooterView(emptyView);
				}catch(Exception e ){
				}

				if(cPd!=null){//断网情况下，且还拥有了cPd，表明其走到了loadActivityList，表示之前成功使用过筛选栏进行列表选择过， 实际上是访问不到数据的 
					cPd.dismiss();
					cPd = null;

					//setNetworkView();
					noNetworkView = setNetworkView();
					lv_search_content.addFooterView(noNetworkView);
					//searchBankcardAdapter.notifyDataSetChanged(); //需删除此代码
					
					//此时可断开上拉的操作
//					PullToRefreshManager.getInstance().footerUnable();
//					PullToRefreshManager.getInstance().headerUnable();

				}else{//注意下面的两个判断的安放顺序//TODO
					if(isFromNoNetworkViewTip){//由ListView添加上的footerview画面点击产生的效果
						//筛选栏的点击在无网的状态下，点击提示画面，进行尝试联网操作，但依旧是返回统一的检查网络的提示画面
						/*	ImageView iv_gif_loadingprogress = (ImageView) noNetworkView.findViewById(R.id.iv_gif_loadingprogress);
				    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();*/
						noNetworkView = setNetworkView();
						lv_search_content.addFooterView(noNetworkView);
//						PullToRefreshManager.getInstance().footerUnable();
//						PullToRefreshManager.getInstance().headerUnable();
						isFromNoNetworkViewTip = false;
						return;
					}
					justFirstClick = true;
					/*if(justFirstClick){//针对 一进来就是无网状态，此时点击container会进行initData()的操作，此时点击一次后，justFirstClick=false，但是为了来网络时点击有效，那么很明显，不可禁掉点击监听，但可以禁掉 点击响应后的操作
						lv_activity_list.addFooterView(noNetworkView);
						PullToRefreshManager.getInstance().footerUnable();
						justFirstClick = false;
					}*/
				}
				break;
			}
			if (holder_button.size() == 4) {
				if (!allready) {
					expandtab_view.setValue(holder_button, mViewArray);//此处出现过NullPointerException，待检测
					allready=true;
				}
			}
		};
	};
	protected String position_bussness="nearby";
	private TextView tv_found_secretary;
	private ServiceAllBiz biz;
	private boolean isShowFromInitData;
	private boolean isShowDialog;
	private CustomDialogProgress cPd;
	private boolean isDownRefresh;
	private String encodeQuery;
	private RelativeLayout noNetworkView;
	private InputMethodManager imm;
	private int times = 0;
	private RequestQueue queue = Volley.newRequestQueue(App.app);
	
	private long sendReqCurrentTimeMillis;
	private long getRespCurrentTimeMillis;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		isShowFromInitData = true;
		emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_empty, null);
		tv_found_secretary = (TextView) emptyView.findViewById(R.id.tv_found_secretary);
		tv_found_secretary.setVisibility(View.VISIBLE);

		tv_found_secretary.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try{
					/*SecretaryFragment homeFragment = new SecretaryFragment();*/
					MainFragment fragment=new MainFragment(1);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(fragment);
					EventBus.getDefault().post(fEntity);
				}catch(Exception e){
					Toast.makeText(activity, "小迈出了个小差", Toast.LENGTH_SHORT).show();
					
					MainFragment fragment=new MainFragment(1);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(fragment);
					EventBus.getDefault().post(fEntity);
				}
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
		/**
		 * 此处主要用于12小时更新一次最新的banner
		 */
		String time=App.app.getData("in_this_fragment_time");
		if (!"".equals(time)) {
			long oldtime=Long.valueOf(time);
			long curr_time=System.currentTimeMillis();
			int ours=(int) ((curr_time-oldtime)/1000/60/60);
			if (ours>11) {
				getAllServer();
			}else {
				setFirstValue();
			}
		}else {
			getAllServer();
			App.app.setData("in_this_fragment_time",String.valueOf(System.currentTimeMillis()));
		}
		
		/*if("".equals(App.app.getData(App.TAGS[0]))|| "".equals(App.app.getData(App.TAGS[1]))||
				 "".equals(App.app.getData(App.TAGS[2]))|| "".equals(App.app.getData(App.TAGS[3]))||
				 "".equals(App.app.getData(App.TAGS[4]))|| "".equals(App.app.getData(App.TAGS[5]))||
				 "".equals(App.app.getData(App.TAGS[6]))|| "".equals(App.app.getData(App.TAGS[7]))){  //之前本地json被清掉之后，重新访问网络
			getAllServer();
		}*/
		
		//到此结束
		
		/**
		 * 获取商圈选择访问
		 */
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
		/**
		 * 智能排序
		 */
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

		/**
		 * 筛选
		 */
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
	/**
	 * 页面跳转
	 * @param query
	 */
	private void startSearch(String query) {
		SearchFragment fragment = new SearchFragment();
		Bundle args = new Bundle();
		args.putString("query", query);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	private void initView( View view){
		//拿到searchFragment送来的搜素关键字
		initData();
		
		iv_activity_back=(RecyclingImageView)view.findViewById(R.id.iv_activity_back);
		del_search_content=(ImageView)view.findViewById(R.id.del_search_content);
		et_home_search=(EditText)view.findViewById(R.id.et_home_search);
		
		et_home_search.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (TextUtils.isEmpty(et_home_search.getText().toString())) {
						startSearch("");
					}
					//不要被方法名误导，此方法只是进行了页面的跳转，实则此页面内部并无 右上角 的 搜索 操作
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

		lv_search_content=(ActivityMyListView)view.findViewById(R.id.lv_search_content); 

		//初始化完成之后去请求网络获取所有数据
		if(isShowFromInitData){//设计isShowFromInitData标签的原因是： 只有第一次进来时，才会有container的监听，在container没有消失的前提下，都只会走initData()方法间接地操作loadActivityList()
			//而筛选栏是直接的进行loadActivityList()的操作，那么在一进入页面就断网的环境下，是没有机会点击筛选栏的，自然也无法 弹出那个customDialogProgress
			isShowDialog = false;
		}
		//这才是真实的搜索操作
		startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,"nearby","intelligent","all",GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
		sendReqCurrentTimeMillis = System.currentTimeMillis();
		Log.d("全局搜索测试", "搜索发送请求的时间点 "+sendReqCurrentTimeMillis);
		
		search_tv_cancle=(TextView)view.findViewById(R.id.search_tv_cancle);
		search_tv_cancle.setOnClickListener(this);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		
		View rootView = inflater.inflate(R.layout.activity_search_content, container,false);
		rootView.setBackgroundColor(Color.argb(255, 235, 235, 235));
		initView(rootView);

		//当前页面根据键盘的问题自动判断是否将布局进行重新编排 ，以保证键盘不会将界面盖住
//		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
//				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); 
		
//		imm = (InputMethodManager) getActivity()
//				.getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		Handler_Inject.injectFragment(this, rootView);//Handler_Inject就会去调用invoke，invoke中会调用Inject_View,而Inject_View中又会调用applyTo()
		//这里面就寻找并定位到ListView对象，并且涉及到搜索startSearch(),其中startSearch()中就需要将listView和progress挂上钩，言即需要将ListView找到并和progress挂上钩
		//init();
		
		
		lv_search_content.onLoadMoreOverFished();
		lv_search_content.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefreshing() {
				isShowDialog=false;
				
				startSearch(defaultVisitUrl,getSelectCity()[0], 
						HODLER_TYPE, position_bussness, intelligentStr, type,
						GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(), query);
				searchBankcardAdapter = null;
//				PullToRefreshManager.getInstance().footerEnable();
				lv_search_content.removeFooterView(emptyView);
			}
			
			@Override
			public void onLoadingMore() {
				isShowDialog=false;
				if (list != null) {
					if ("null".equals(String.valueOf(list.getNext_page_url()))||
							TextUtils.isEmpty(String.valueOf(list.getNext_page_url()))) {
						try{
							//若之前的隶属于View对象是有emptyView对象的，先将其移除掉
							lv_search_content.removeFooterView(emptyView);
						}catch(Exception e ){
							
						}finally{
						}
//						lv_search_content.addFooterView(emptyView);
//						lv_search_content.onLoadMoreOverFished();
						
					  if(times == 0){
						lv_search_content.addFooterView(emptyView);
			              lv_search_content.onLoadMoreOverFished();
			              CustomToast.show(activity, R.string.reach_bottom, R.string.not_more);
			              times++;
			            }else{
			              lv_search_content.onLoadMoreOverFished();
			            }

//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
//						CustomToast.show(activity, "到底啦！", "该关键词下暂时只有这么多内容");
					} else {
						String nextPageUrl = list.getNext_page_url();
						lv_search_content.setNoHeader(true);
//						startSearchNext(nextPageUrl);
						startSearchNextByVolley(nextPageUrl);
						isPull = true;
					}
				}
			}
				
		});
		getTitleBanner();
		setListener();
		return rootView;
	}

	protected void startSearchNextByVolley(String nextPageUrl) {
		
		String url = nextPageUrl;
		
		//根据给定的URL新建一个请求
		StringRequest stringRequest = new StringRequest(Method.GET, url,new Listener<String>() {

			@Override
			public void onResponse(String response) {
				//此时是加载更多。。。，故adapter不为空
		    	LogUtils.toDebugLog("loading", "加载更多成功");
		    	Gson gson=new Gson();
				list = gson.fromJson(response, ActivityList.class);
				if(isPull){
					isPull = false;
				}else{
					datas = new ArrayList<HashMap<String, Object>>();//重新new出来一个新的list
				}
				if (list.getData() != null && !list.getData().toString().equals("[]") ){
					for (ActivityListData data : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("tv_search_activity_name", data.getShop().getName());
						map.put("tv_search_activity_distance", data.getShop().getDistance());
						map.put("tv_search_activity_desc", data.getActivity().getName());
						map.put("iv_search_activity_head", data.getActivity().getPhoto());
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						datas.add(map);
					}
					handleOkhttp.sendEmptyMessage(1);
					return;
				}
				if(list.getData().toString().equals("[]")){
					handleOkhttp.sendEmptyMessage(2);            //返回的数据解析后为空时 
					return;
				}
			} 	
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				LogUtils.toDebugLog("loading", "加载更多失效");
			}
		}){
			@Override  
		   public Map<String, String> getHeaders() throws AuthFailureError  
		   {  
				//super.getHeaders();
				Map<String, String> headers = new HashMap<String, String>();  
				headers.put("Charset", "UTF-8");  
				headers.put("Content-Type", "application/x-javascript");  
				headers.put("Accept-Encoding", "gzip,deflate");  
				headers.put("Authorization", "Bearer "+App.app.getData("access_token"));  
				return headers;  
		   } 
		};
		stringRequest.setTag("loadingMore");
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(20*1000, 1, (float) 2.0));
		
		// 把这个请求加入请求队列
		queue.add(stringRequest);
//		queue.start();
	}
	/*
	 * listView 对象设置上点击事件
	 */
	private void setListener() {
		lv_search_content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				if(position-1 >= datas.size()){
					LogUtils.toDebugLog("ListView的点击监听","点击了，但就不响应");
				}else{
					HashMap<String, Object> data = datas.get(position-1);
					String activityId = data.get("activityId").toString();
					String shopId = data.get("shopId").toString();
					ActivityDetailFragment fragment = new ActivityDetailFragment(expandtab_view);
					Bundle args = new Bundle();
					args.putString("activityId", activityId);
					args.putString("shopId", shopId);
					fragment.setArguments(args);
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
			}
		});
	}
	
	private RelativeLayout setNetworkView() {
		LogUtils.toDebugLog("设置NoNetView", "设置NoNetView");
		
		noNetworkView = (RelativeLayout) inflater.inflate(R.layout.customdialogprogress1, null);
		TextView messageTextView = (TextView) noNetworkView.findViewById(R.id.messageText);
		ImageView iv_gif_loadingprogress = (ImageView) noNetworkView.findViewById(R.id.iv_gif_loadingprogress);
		((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();
		messageTextView.setText("请检查网络连接，确保联网后进入页面");
		noNetworkView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view ) {//TODO
				if(justFirstClick){
					isFromNoNetworkViewTip = true;
					/*startSearchData(GlobalValue.URL_ALL_ACTIVITY,
							App.app.getData("select_cityCode"),
							HODLER_TYPE,position_bussness,intelligentStr,
							GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());*/
					
					startSearch(defaultVisitUrl,getSelectCity()[0],
								HODLER_TYPE,"nearby","intelligent","all",
								GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),
								query);
					
					justFirstClick = false;
				}
			}
		});
		return noNetworkView;
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
			search_tv_cancle.setTextColor(getResources().getColor(R.color.app_green1));
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
//				InputMethodManager imm = (InputMethodManager) getActivity()
//						.getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
				
				back();//执行的是当前的back方法，即重写的方法，这才是关键代码！
				
				
			} else {
				query = et_home_search.getText().toString().trim();

				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//				startSearch();
				setEmptityView(first, 0);
			}
			break;
			// 大× 删除编辑框内容
		case R.id.del_search_content:
			activity.sendBroadcast(new Intent("com.lansun.qmyo.toggleSoftKeyboard"));
			LogUtils.toDebugLog("broadcast", "SearchBrandListOkHttpFragment  发送弹起键盘的广播");
//			et_home_search.setText("");此处可能涉及到焦点获取，键盘欲弹起的标签位
			getFragmentManager().popBackStack();//纳入最大的宿主activity的fragments集合的框架中
			break;
			
		/*case R.id.iv_activity_back:
			getFragmentManager().popBackStack();
			break;*/
		}
	}
	/**
	 * 正常点击开启的搜索
	 * 开启搜索
	 * 
	 * @param string
	 */
	private void startSearch(String visitUrl, String site,String service,String positon,String intelligent,String type,String location,String query) {
		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
		
		this.times  = 0;
		this.first_enter = 0;
		
		if (isShowDialog){
			if(cPd == null ){
				cPd = CustomDialogProgress.createDialog(activity);
				
				/*cPd.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						lv_search_content.setVisibility(View.INVISIBLE);
						if (allready) {
						}
					}
				});*/
				
				lv_search_content.setVisibility(View.INVISIBLE);
				cPd.setCanceledOnTouchOutside(false);
				cPd.show();
			}else{
				cPd.show();
			}
		}else {
			setProgress(lv_search_content);
			startProgress();
			progress_text.setText("正在搜索幸运中");
		}
		try {
			encodeQuery = URLEncoder.encode(query, "utf-8");
			Log.d("utf-8", encodeQuery);
			Log.d("utf-8", query);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		/*refreshParams.put("query", URLEncoder.encode(query, "utf-8"));
		Log.d("utf-8", query);*/
		
		OkHttp.asyncGet(visitUrl+"site="+site+"&service="+service+"&position="+positon+"&intelligent="+intelligent+"&type="
				+type+"&location="+location+"&query="+query, "Authorization", "Bearer "+App.app.getData("access_token"), "SearchBrandListOkHttpFragment", new Callback() {
			
			@Override
			public void onResponse(Response response) throws IOException {
				getRespCurrentTimeMillis = System.currentTimeMillis();
				Log.d("全局搜索测试", "服务器针对请求返回数据的时间点 "+getRespCurrentTimeMillis);
				Log.d("全局搜索测试", "发送与返回服务的时间差 "+(getRespCurrentTimeMillis - sendReqCurrentTimeMillis));
				
				
				Gson gson=new Gson();
				list = gson.fromJson(response.body().string(), ActivityList.class);
				
				/*list = Handler_Json.JsonToBean(ActivityList.class, response.body().string());*/
				if(isPull){
					isPull = false;
				}else{
					datas.clear();//重新new出来一个新的list
				}

				if(list!=null){//list只有Gson解析成功后，才可以完成
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
						
						/* 下面这一步，试图在setAdapter之前将emptyView塞入到ListView控件上，但此处报错
						 * if(list.getData().size()<10){
						lv_search_content.addFooterView(emptyView);
						Log.i("","在此处添加上了尾部EmptyView");
					}*/
						
						handleOkhttp.sendEmptyMessage(1);//TODO    返回的list不为空的时候，发送1号消息
						return;
					}
					
					
					if(list.getData()!= null){//list虽不为空，但是list.getData可能为空
						LogUtils.toDebugLog("Search", "底部将出现小猫头鹰");
						if(list.getData().toString().equals("[]")||(list.getData().toString().contains("[")&&list.getData().toString().contains("]"))){
							LogUtils.toDebugLog("Search", "handleOkhttp.sendEmptyMessage(2);");
							handleOkhttp.sendEmptyMessage(2);//返回的数据解析后为空时    
							return;
						}
					}else{//针对“泊车优惠”的这种情况，list不为空，但list.getData()却是为null值，这么做是为了避免list。getData()出现空指针异常，导致程序奔溃的补救措施
						LogUtils.toDebugLog("Search", "底部将出现小猫头鹰");
						handleOkhttp.sendEmptyMessage(2);
					}
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {
				handleOkhttp.sendEmptyMessage(11);
				if(!justFirstClick){//未拿到数据的时候，要让点击操作有响应，故在此 开放标志信号justFirstClick
					justFirstClick = !justFirstClick;
				}
			}
		});
	}
	/**
	 * 进行上拉时的搜索
	 * 开启搜索
	 * 
	 * @param string
	 */
	private void startSearchNext(String visitUrl) {
		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
		if (isShowDialog){
			if(cPd == null ){
				cPd = CustomDialogProgress.createDialog(activity);
				/*cPd.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						lv_search_content.setVisibility(View.INVISIBLE);
						if (allready) {
							
						}
					}
				});*/
				lv_search_content.setVisibility(View.INVISIBLE);
				cPd.setCanceledOnTouchOutside(false);
				cPd.show();
			}else{
				cPd.show();
			}
		}else {
			setProgress(lv_search_content);
			startProgress();
			progress_text.setText("正在搜索幸运中");
		}
		OkHttp.asyncGet(visitUrl, "Authorization", "Bearer "+App.app.getData("access_token"), "SearchBrandListOkHttpFragment", new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				Gson gson=new Gson();
				list = gson.fromJson(response.body().string(), ActivityList.class);
				if(isPull){
					isPull = false;
				}else{
					datas = new ArrayList<HashMap<String, Object>>();//重新new出来一个新的list
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
					
					/* 下面这一步，试图在setAdapter之前将emptyView塞入到ListView控件上，但此处报错
					 * if(list.getData().size()<10){
						lv_search_content.addFooterView(emptyView);
						Log.i("","在此处添加上了尾部EmptyView");
					}*/
					
					handleOkhttp.sendEmptyMessage(1);
					return;
				}
				
				if(list.getData().toString().equals("[]")){
					handleOkhttp.sendEmptyMessage(2);            //返回的数据解析后为空时 
					return;
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {
				
			}
		});
	}

	/**
	 * 设置筛选栏的选择监听
	 */
	private void setOnPullListeners() {
		viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText,
					int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
				intelligentStr = intelligent.getData()
						.get(position).getKey();
				//				searchBankcardAdapter = null;//上面四个板块点击之前需要进行  设置为空的操作，为了将前面的页面数据给我清掉
				OkHttp.okHttpClient.cancel(TAG);
				
				//searchBankcardAdapter=null;
				datas.clear();
				if (searchBankcardAdapter != null) {
					searchBankcardAdapter.notifyDataSetChanged();
				}
				
				/*lv_search_content.removeView(emptyView);*/
				if (!isShowDialog) {
					endProgress();
				}
				isShowDialog=true;
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewMiddle, showText);
			}
		});

		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener(){
			@Override
			public void getValue(String showText, int parendId,int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
				if (root.getData().get(parendId).getData() == null) {
					onRefresh(viewLeft, showText);
					HODLER_TYPE = root.getData().get(parendId).getKey()+ "";
				} else if (root.getData().get(parendId)
						.getData().get(position) != null) {

					HODLER_TYPE = root.getData().get(parendId).getData().get(position).getKey();
					//					query=root.getData().get(parendId).getData().get(position).getName();
				}
				OkHttp.okHttpClient.cancel(TAG);
				
				//searchBankcardAdapter=null;
				datas.clear();
				if (searchBankcardAdapter != null) {
					searchBankcardAdapter.notifyDataSetChanged();
				}
				if (!isShowDialog) {
					endProgress();
				}
				isShowDialog=true;
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewLeft, showText);
			}
		});

		viewLeft2.setOnSelectListener(new ViewLeft.OnSelectListener() {
			@Override
			public void getValue(String showText, int parendId,int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
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
				OkHttp.okHttpClient.cancel(TAG);
				
				//searchBankcardAdapter=null;
				datas.clear();
				if (searchBankcardAdapter != null) {
					searchBankcardAdapter.notifyDataSetChanged();
				}
				if (!isShowDialog) {
					endProgress();
				}
				isShowDialog=true;
				startSearch(defaultVisitUrl,getSelectCity()[0],HODLER_TYPE,position_bussness,intelligentStr,type,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),query);
				onRefresh(viewLeft2, showText);
			}
		});

		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText,int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
				type = sxintelligent.getData().get(position).getKey();
				if ("all".equals(type)) {
					typeSb.delete(0, typeSb.length());
				}
				if (!typeSb.toString().contains(type)) {
					typeSb.append(type + ",");
				}
				OkHttp.okHttpClient.cancel(TAG);
				
				//searchBankcardAdapter=null;
				
				datas.clear();
				if (searchBankcardAdapter != null) {
					searchBankcardAdapter.notifyDataSetChanged();
				}
				if (!isShowDialog) {
					endProgress();
				}
				isShowDialog=true;
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
//		PullToRefreshManager.getInstance().headerUnable();
		
		
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
			isShowDialog=false;
			/*CustomToast.show(activity, "上拉操作", "现在进行上拉操作！");*/
			if (list != null) {
				if ("null".equals(String.valueOf(list.getNext_page_url()))||TextUtils.isEmpty(String.valueOf(list.getNext_page_url()))) {
					try{
						//若之前的隶属于View对象是有emptyView对象的，先将其移除掉
						lv_search_content.removeFooterView(emptyView);
					}catch(Exception e ){
						
					}finally{
					}
					lv_search_content.addFooterView(emptyView);

//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
//					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, R.string.reach_bottom, R.string.not_more);
				} else {
					String nextPageUrl = list.getNext_page_url();
					startSearchNext(nextPageUrl);
					isPull = true;
				}
			}
			break;
		case InjectView.DOWN://去做刷新操作
			isShowDialog=false;
			
			startSearch(defaultVisitUrl,getSelectCity()[0], 
					HODLER_TYPE, position_bussness, intelligentStr, type,
					GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(), query);
			searchBankcardAdapter = null;
			//暂时为空
			/*CustomToast.show(activity, "下拉操作", "现在进行下拉操作！");*/
//			PullToRefreshManager.getInstance().footerEnable();
			lv_search_content.removeFooterView(emptyView);
			break;
		}
	}


	/**
	 * 获得8大板块的头部导航
	 */
	private void getAllServer() {
		biz=new ServiceAllBiz();
		if ("".equals(App.app.getData(App.TAGS[0]))) {
			biz.getAllService("100000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[0],json);
					handleOkhttp.sendEmptyMessage(3);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(3);
		}
		if ("".equals(App.app.getData(App.TAGS[1]))) {
			biz.getAllService("200000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[1],json);
					handleOkhttp.sendEmptyMessage(4);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(4);
		}
		if ("".equals(App.app.getData(App.TAGS[2]))) {
			biz.getAllService("300000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[2],json);
					handleOkhttp.sendEmptyMessage(5);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(5);
		}
		if ("".equals(App.app.getData(App.TAGS[3]))) {
			biz.getAllService("400000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[3],json);
					handleOkhttp.sendEmptyMessage(6);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(6);
		}
		if ("".equals(App.app.getData(App.TAGS[4]))) {
			biz.getAllService("500000",new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[4],json);
					handleOkhttp.sendEmptyMessage(7);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(7);
		}
		if ("".equals(App.app.getData(App.TAGS[5]))) {
			biz.getAllService("600000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[5],json);
					handleOkhttp.sendEmptyMessage(8);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(8);
		}
		if ("".equals(App.app.getData(App.TAGS[6]))) {
			biz.getAllService("700000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[6],json);
					handleOkhttp.sendEmptyMessage(9);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(9);
		}
		if ("".equals(App.app.getData(App.TAGS[7]))) {
			biz.getAllService("800000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[7],json);
					handleOkhttp.sendEmptyMessage(10);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOkhttp.sendEmptyMessage(10);
		}
	}
	/**
	 * 加载本地导航资源
	 */
	private void setFirstValue() {
		if (!"".equals(App.app.getData(App.TAGS[0]))&&!"".equals(App.app.getData(App.TAGS[1]))&&!"".equals(App.app.getData(App.TAGS[2]))&&!"".equals(App.app.getData(App.TAGS[3]))&&!"".equals(App.app.getData(App.TAGS[4]))&&!"".equals(App.app.getData(App.TAGS[5]))&&!"".equals(App.app.getData(App.TAGS[6]))&&!"".equals(App.app.getData(App.TAGS[7]))) {
			String allJson="{name: 全部,data:[{name: 全部,key: 000000},"+App.app.getData(App.TAGS[0])+","+App.app.getData(App.TAGS[1])+","+App.app.getData(App.TAGS[2])+","+App.app.getData(App.TAGS[3])+","+App.app.getData(App.TAGS[4])+","+App.app.getData(App.TAGS[5])+","+App.app.getData(App.TAGS[6])+","+App.app.getData(App.TAGS[7])+"]}";
			Gson gson=new Gson();
			root = gson.fromJson(allJson, ServiceRoot.class);
			name = root.getName();
			ArrayList<String> allGroup = new ArrayList<String>();
			SparseArray<LinkedList<String>> allChild = new SparseArray<LinkedList<String>>();
			for (int j = 0; j < root.getData().size(); j++) {
				LinkedList<String> chind = new LinkedList<String>();
				allGroup.add(root.getData().get(j).getName());
				List<ServiceData> items =root.getData().get(j).getData();
				if (items != null) {
					for (ServiceData item : items) {
						chind.add(item.getName());
					}
				}
				allChild.put(j, chind);
			}
			holder_button.put(0, name);
			viewLeft.setGroups(allGroup);
			viewLeft.setChildren(allChild);
			mViewArray.put(0, viewLeft);
		}
	};
	
	@Override
	public void onStop() {
//		PullToRefreshManager.getInstance().headerUnable();
		super.onStop();
	}
	
	
	  /**
     * 重写BaseFragment中的setProgress的方法，重点是改变progress_container这里的点击事件
     */
    @Override
    protected void setProgress(View view) {
		if (progress != null) {
			return;
		}
		loadView = view;
		LayoutParams lp = (LayoutParams) view.getLayoutParams();
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

				@Override
				public void onClick(View arg0) {
					try{
						//refreshCurrentList(refreshUrl, refreshParams, refreshKey,loadView);
						if(justFirstClick){//一旦点击一次后，即下次点击不产生实际效果，避免列表重复加载的情况   （即只是第一次点击的时候才有效）
							justFirstClick = false;
							//initData();
							
							progress_text.setText("内容正在加载中...");
	 						
	 						startSearch(defaultVisitUrl,getSelectCity()[0],
	 								HODLER_TYPE,"nearby","intelligent","all",
	 								GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon(),
	 								query);
	 						getTitleBanner();
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
	}
    
    
	@Override//	@InjectMethod(@InjectListener(ids = 2131296340, listeners = OnClick.class))
	protected void back() {
		activity.sendBroadcast(new Intent("com.lansun.qmyo.toggleSoftKeyboard"));
		LogUtils.toDebugLog("broadcast", "SearchBrandListOkHttpFragment  发送弹起键盘的广播");
		super.back();
	}
	@Override
	public boolean onBackPressed() {
		back();
		return true;
	}
	
}
