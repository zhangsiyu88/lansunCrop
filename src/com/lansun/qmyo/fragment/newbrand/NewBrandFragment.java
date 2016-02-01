package com.lansun.qmyo.fragment.newbrand;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.biz.AllNewDataBiz;
import com.lansun.qmyo.biz.IntelligentBiz;
import com.lansun.qmyo.biz.PositionBiz;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.position.City;
import com.lansun.qmyo.domain.position.Position;
import com.lansun.qmyo.domain.service.ServiceData;
import com.lansun.qmyo.domain.service.ServiceRoot;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.MineBankcardFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.listener.RequestCallBack;
//import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.DataUtils;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.EightPartActivityAdapter;
import com.lansun.qmyo.view.ActivityMyListView;
import com.lansun.qmyo.view.ActivityMyListView.OnRefreshListener;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
/**
 * time 2015-12-03
 * 
 * @author Yeun
 * 
 * 此版块为新品曝光栏目
 */
public class NewBrandFragment extends BaseFragment{
	private Gson gson;
	private ServiceAllBiz biz;
	private String[] jsons=new String[8];
	private Map<String, String> map=new HashMap<String, String>();
	private String HODLER_TYPE="000000";
	private View tv_found_secretary;
	private boolean isPosition;
	private boolean justFirstClick = true;
	private boolean isFromNoNetworkViewTip = false;
	
	@InjectAll
	Views v;
	private View loadView;
	private ViewLeft viewLeft;
	private ViewLeft viewLeft2;
	private ViewMiddle viewMiddle;
	//服务板块
	private ServiceRoot root;
	/**
	 * 全部服务信息
	 */
	private Position nearService;

	/**
	 * 智能排序
	 */
	private Intelligent intelligent;
	private String position_bussness = "nearby";
	private String intelligentStr="intelligent";

	//承载下拉控件和数据的
	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();
	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();

	private ArrayList<HashMap<String, Object>> shopDataList = new ArrayList<HashMap<String, Object>>();
	private ActivityList activityList  = new ActivityList() ;
	
	/*private SearchAdapter activityAdapter;*/
	private EightPartActivityAdapter activityAdapter;

	@InjectView
	private ActivityMyListView lv_activity_list;
	
/*	@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), down = true, pull = true)
	private MyListView lv_activity_list;*/	
	
	//添加一个搜索为空的view层
	private View emptyView;
	protected String name;
	private String title;
	protected boolean allready;
	protected boolean isRemove=true;
	class Views {
		private View expandTabViewButtomLine;
		private View header;
		private View iv_card;
		private TextView tv_activity_title, tv_home_experience;
		private ExpandTabView expandtab_view;
		private RelativeLayout rl_bg;
	}
	/**
	 * 工作线程发送消息handler轮训消息并处理
	 */
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				lv_activity_list.onLoadMoreFished();
				lv_activity_list.onRefreshFinshed(true);
				lv_activity_list.setNoHeader(false);
				
//				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				
				endProgress();
				lv_activity_list.setVisibility(View.VISIBLE);
				
				if(cPd!=null){
					cPd.dismiss();
					cPd = null;
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
					isRemove = true;
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				
				/**
				 * 注意下面添加的判断条件，activityList.getData().toString()居然是个“[]”
				 */
				if (activityList.getData() != null && activityList.getData().toString()!= "[]") {//服务器返回回来的数据中的Data不为null
					if (!isRemove) {
						try{
							lv_activity_list.removeFooterView(emptyView);
						}catch(Exception e){
					  }
						isRemove=true;
					}
					if(isDownChange){//下拉刷新时,需要将数据重新获取,即将shopDataList清空掉
						shopDataList.clear();
						isDownChange = false;
					}
					HashMap<String, Object> map = new HashMap<String, Object>();
					/*shopDataList =  new ArrayList<HashMap<String, Object>>();*/
					LogUtils.toDebugLog("activityList", "加载的列表的第一个 :  "+activityList.getData().get(0).toString());
					
					for (ActivityListData data : activityList.getData()) {
						map = new HashMap<String, Object>();
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
						/*activityAdapter = new SearchAdapter(lv_activity_list,
								shopDataList, R.layout.activity_search_item);*/
						
						activityAdapter = new EightPartActivityAdapter(activity,shopDataList);
						lv_activity_list.setAdapter(activityAdapter);
						
						if(activityList.getData().size()<10){
							if (isRemove) {
//								lv_activity_list.addFooterView(emptyView);
//								CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");
//								lv_activity_list.onLoadMoreOverFished();
								
								if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
									lv_activity_list.setNoHeader(true);
									lv_activity_list.onLoadMoreOverFished();
									lv_activity_list.addFooterView(emptyView);
									//CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");
						          }else{
						            //DO-OP
						          }
								
								/*LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
								params.weight=1;
								lv_activity_list.setLayoutParams(params);*/
								
								isRemove=false;
							}
						}else {
							if (!isRemove) {
								try{
									lv_activity_list.removeFooterView(emptyView);
								}catch(Exception e){
									
								}
								isRemove=true;
							}
						}
					} else {
						//adapter 已经存在时，
						//activityAdapter.notifyDataSetChanged();
						
						if(activityList.getData().size()<10){
							if (isRemove) {
//								lv_activity_list.addFooterView(emptyView);
//								CustomToast.show(activity, "到底啦！", "小迈会加油搜索更多惊喜的！");
//								lv_activity_list.onLoadMoreOverFished();
								
								if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
									lv_activity_list.setNoHeader(true);
									lv_activity_list.onLoadMoreOverFished();
									lv_activity_list.addFooterView(emptyView);
									CustomToast.show(activity, R.string.reach_bottom, R.string.collect_more_superise);
						          }else{
						            //DO-OP
						          }
								
								isRemove=false;
							}
						}else {
							if (!isRemove) {
								try{
									lv_activity_list.removeFooterView(emptyView);
									isRemove=true;
								}catch(Exception e){
								}
							}
						}
						
						//adapter并不为空时
						activityAdapter.notifyDataSetChanged();
						
						/*activityAdapter.notifyDataSetChanged();*/
					}
					first_enter = Integer.MAX_VALUE;

				} else {//因为上拉前去获取到的数据为null，此时需要将之前的值保留住并展示
					lv_activity_list.setAdapter(null);
					
					lv_activity_list.setNoHeader(true);
					
					if(activityAdapter!= null){
						activityAdapter.notifyDataSetChanged();
					}
					
					if (isRemove){
						lv_activity_list.addFooterView(emptyView);
						isRemove=false;
					}
					lv_activity_list.onLoadMoreOverFished();
				}
				break;
			case 2:
				setFirstValue();
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
			case 10:  //无网络的时候进行的操作//TODO
				//CustomToast.show(activity, "提示", "断网 ");
				progress_text.setText(R.string.net_error_refresh);
				lv_activity_list.onLoadMoreOverFished();
				lv_activity_list.setNoHeader(true);
				
				lv_activity_list.setVisibility(View.VISIBLE);
				
				//针对在断网后再次点击上部筛选栏的自己菜单时，做出的重复添加无效的 noNetworkView界面操作
				try{
					lv_activity_list.removeFooterView(noNetworkView);
				}catch(Exception e ){
				}
				try{
					lv_activity_list.removeFooterView(emptyView);
				}catch(Exception e ){
				}

				if(cPd!=null){//断网情况下，且还拥有了cPd，表明其走到了loadActivityList，表示之前成功使用过筛选栏进行列表选择过， 实际上是访问不到数据的 
					cPd.dismiss();
					cPd = null;

					//setNetworkView();
					noNetworkView = setNetworkView();
					
					lv_activity_list.addFooterView(noNetworkView);
					activityAdapter.notifyDataSetChanged();
					
					//此时可断开上拉的操作
//					PullToRefreshManager.getInstance().footerUnable();
//					PullToRefreshManager.getInstance().headerUnable();

				}else{//注意下面的两个判断的安放顺序//TODO
					
					if(isFromNoNetworkViewTip){//由ListView添加上的footerview画面点击产生的效果
						//筛选栏的点击在无网的状态下，点击提示画面，进行尝试联网操作，但依旧是返回统一的检查网络的提示画面
						/*	ImageView iv_gif_loadingprogress = (ImageView) noNetworkView.findViewById(R.id.iv_gif_loadingprogress);
				    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();*/
						noNetworkView = setNetworkView();
						lv_activity_list.addFooterView(noNetworkView);
						lv_activity_list.onLoadMoreOverFished();
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
			if (holder_button.size() == 3) {
				if (!allready) {
					v.expandtab_view.setValue(holder_button, mViewArray);
					allready=true;
				}
			}
		}
	};
	private AllNewDataBiz biz_all;
	private ProgressDialog dialog;
	private boolean isDownChange;
	public boolean isSend;
	private boolean isShowDialog;
	private CustomDialogProgress cPd;
	private IntentFilter filter;
	private NewBrandRefreshBroadCastReceiver broadCastReceiver;
	private RelativeLayout noNetworkView;
	private int times = 0;
	private RequestQueue queue;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.toDebugLog("token", "NewBrand中的token： "+App.app.getData("access_token"));
		
		
		broadCastReceiver = new NewBrandRefreshBroadCastReceiver();
		System.out.println("注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.refreshTheIcon");
		getActivity().registerReceiver(broadCastReceiver, filter);
		
		
		biz_all = new AllNewDataBiz();
		gson = new Gson();
		viewLeft=new ViewLeft(getActivity());
		viewLeft2=new ViewLeft(getActivity());
		viewMiddle=new ViewMiddle(getActivity());
		
		//从本地或网络上获取筛选栏的值
		getAllBannerContentFromNetOrLocal();//详见下方的封装起来的方法
	}

	/**
	 * 从本地或网络上获取筛选栏得值
	 */
	public void getAllBannerContentFromNetOrLocal() {
		
		//导航栏5小时更新一次
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
			App.app.setData("in_this_fragment_time",String.valueOf(System.currentTimeMillis()));
		}*/
		
		PositionBiz pBiz=new PositionBiz();
		pBiz.getPostion(App.app.getData("select_cityCode"), new RequestCallBack() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					nearService = gson.fromJson(response.body().string(),Position.class);
					//					handleOk.sendEmptyMessage(8);
					name = nearService.getName();
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
					handleOk.sendEmptyMessage(2);
				}
			}
			@Override
			public void onFailure(Request request, IOException exception) {
				

			}
		});
		IntelligentBiz intelligentBiz=new IntelligentBiz();
		intelligentBiz.getIntelligent(new RequestCallBack() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String json=response.body().string();
					intelligent = gson.fromJson(json,Intelligent.class);
					//					handleOk.sendEmptyMessage(9);
					name = intelligent.getName();
					ArrayList<String> sortGroup = new ArrayList<String>();
					ArrayList<Data> sortData = intelligent.getData();
					for (Data d : sortData) {
						if (!"银行卡优先".equals(d.getName().toString().trim())) {
							sortGroup.add(d.getName());
						}
					}
					holder_button.put(2, name);
					viewMiddle.setItems(sortGroup);
					mViewArray.put(2, viewMiddle);
					handleOk.sendEmptyMessage(3);
				}
			}
			@Override
			public void onFailure(Request request, IOException exception) {

			}
		});
	}
	
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		//注册广播
		View view=inflater.inflate(R.layout.activity_activity, container,false);
		Handler_Inject.injectFragment(this, view);//Handler_Inject就会去调用invoke，invoke中会调用Inject_View,而Inject_View中又会调用applyTo()
		initView(view);
		initListener();
		
		lv_activity_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {
				if(position-1 >= shopDataList.size()||position-1<0){
					return;
				}
				
				v.expandtab_view.onPressBack();//将筛选栏关闭掉
				LogUtils.toDebugLog("close", "关闭ExpandTabView");
				
				/*ActivityDetailFragment fragment = new ActivityDetailFragment();*/
				
				ActivityDetailFragment fragment = new ActivityDetailFragment(v.expandtab_view);
				Bundle args = new Bundle();
				args.putString("activityId",
						shopDataList.get(position-1).get("activityId").toString());
				args.putString("shopId", shopDataList.get(position-1).get("shopId")
						.toString());
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
				
				
			}
		});
		lv_activity_list.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefreshing() {
				isShowDialog=false;
				if (activityList != null) {
					isDownChange = true;//下拉更新的标志
					first_enter = 0;
					startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
					lv_activity_list.removeFooterView(emptyView);//不能忘了去除底部的emptyView
				}else{
					CustomToast.show(activity, "提示", "activityList == null");
				}
			}
			
			
			
			/**
			 * 加载更多的操作，现在修改为Volley的网络访问，力求当筛选栏被点击后，此时加载更多的网络请求被终止掉cancel掉（换句话说，不再执行onResponse()和onErrorResponse()）
			 */
			@Override
			public void onLoadingMore() {
				isShowDialog=false;
				if (activityList != null) {
					/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
					if (activityList.getNext_page_url()== "null"||TextUtils.isEmpty(activityList.getNext_page_url())) {
						try{
							lv_activity_list.removeFooterView(emptyView);
						}catch(Exception e ){

						}
						
//						lv_activity_list.addFooterView(emptyView);
//						lv_activity_list.onLoadMoreOverFished();
//						CustomToast.show(activity, "到底啦！", "小迈会加油搜集更多惊喜哦");
						
						if(times == 0){
				              lv_activity_list.onLoadMoreOverFished();
				              lv_activity_list.addFooterView(emptyView);
				              CustomToast.show(activity, R.string.reach_bottom, R.string.collect_more_superise);
				              times++;
				            }else{
				              lv_activity_list.addFooterView(emptyView);
				              lv_activity_list.onLoadMoreOverFished();
				            }
						
						
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
						
					} else {
						//下面的startSearchData()注意与 删选栏点击的startSearchData() 区分开来 
//						startSearchData(activityList.getNext_page_url());
						startSearchDataByVolley(activityList.getNext_page_url());
						
						lv_activity_list.setNoHeader(true);
					}
				}
			}
		});
		
		
		if(App.app.getData("gpsIsNotAccurate").equals("true")){
			
			DialogUtil.createTipAlertDialog(activity,
					"您还未开启精确定位哦\n\r请前往应用权限页开启",
					new DialogUtil.TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							  Intent localIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
					          localIntent.setData(Uri.fromParts("package", "com.lansun.qmyo", null));
					          activity.startActivity(localIntent);//前往权限设置的页面
					          
					          if(App.app.getData("firstEnter").isEmpty()){
									App.app.setData("gpsIsNotAccurate","");//将gps的提醒标签置为空
									App.app.setData("firstEnter","notblank");//但此时已不是第一次进入
								}
					          dialog.dismiss();
						}

						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							
						}
					});
			}
		return view;
	}

	/**
	 * 对标题栏下面的四个分点击TextView模块设置对应的监听者
	 */
	private void initListener() {

		
		
		//智能排序模块
		viewMiddle.setOnSelectListener(new ViewMiddle.OnSelectListener() {

			@Override
			public void getValue(String distance, String showText, int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
				
				intelligentStr = intelligent.getData().get(position).getKey();
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
					activityAdapter = null;
				}
				isShowDialog=true;
				endProgress();
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				onRefresh(viewMiddle, showText);
			}
		});

		//所有服务模块
		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {
			@Override
			public void getValue(String showText, int parentId, int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
				if (root.getData().get(parentId).getData()== null) {
					onRefresh(viewLeft, showText);
					HODLER_TYPE = root.getData().get(parentId).getKey()+ "";
				} else if (root.getData().get(parentId).getData()
						.get(position) != null) {
					HODLER_TYPE = root.getData().get(parentId).getData()
							.get(position).getKey();
				}
				shopDataList.clear();
				if (activityAdapter != null) {
					activityAdapter.notifyDataSetChanged();
					activityAdapter = null;
				}
				isShowDialog=true;
				endProgress();
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				onRefresh(viewLeft, showText);
			}
		});

		//附近服务模块
		viewLeft2.setOnSelectListener(new ViewLeft.OnSelectListener() {

			@Override
			public void getValue(String showText, int parentId, int position) {
				if(queue!=null){
					queue.cancelAll("loadingMore");//取消
				}
				LogUtils.toDebugLog("cancel", "取消之前加载更多页面的响应");
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
					activityAdapter = null;
				}
				isShowDialog=true;
				endProgress();
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				onRefresh(viewLeft2, showText);
			}
		});
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
	
	private void initView(View view) {
	    //一进入界面时，就进行上拉和下拉刷新头的准备工作
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
		
		/**
		 * 大前提： App.app.getData("LastRefreshTokenTime")不为 空
		 */
		if(App.app.getData("LastRefreshTokenTime")!=null&&
				!App.app.getData("LastRefreshTokenTime").equals("")&&
				!TextUtils.isEmpty(App.app.getData("LastRefreshTokenTime"))){
		//去获取列表
		long LastRefreshTokenTime = Long.valueOf(App.app.getData("LastRefreshTokenTime"));
		LogUtils.toDebugLog("LastRefreshTokenTime", "上次最近更新token服务的时刻： "+DataUtils.dataConvert(LastRefreshTokenTime));
		LogUtils.toDebugLog("LastRefreshTokenTime", "两次更新token的时间差"+((System.currentTimeMillis()-LastRefreshTokenTime))/1000/60);
		
		if((System.currentTimeMillis()-LastRefreshTokenTime)>10*60*1000){
			//进行刷新token的操作
			HttpUtils httpUtils = new HttpUtils();
			com.lidroid.xutils.http.callback.RequestCallBack<String> requestCallBack = new com.lidroid.xutils.http.callback.RequestCallBack<String>() {
				@Override
				public void onFailure(HttpException e, String result) {
				}
				@Override
				public void onSuccess(ResponseInfo<String> result) {
					Token token = Handler_Json.JsonToBean(Token.class,result.toString());
					App.app.setData("access_token", token.getToken());
					App.app.setData("LastRefreshTokenTime",String.valueOf(System.currentTimeMillis()));
					
					LogUtils.toDebugLog("LastRefreshTokenTime", 
							"此次最近更新token服务的时刻： "+DataUtils.dataConvert(Long.valueOf(App.app.getData("LastRefreshTokenTime"))));
					LogUtils.toDebugLog("LastRefreshTokenTime", "在NewBrandFragment中令牌更新操作成功！");
					
//					InternetConfig config = new InternetConfig();
//					config.setKey(key);
//					HashMap<String, Object> head = new HashMap<>();
//					head.put("Authorization", "Bearer " + App.app.getData("access_token"));
//					
//					config.setHead(head);
//					FastHttpHander.ajaxGet(url, params, config, this);
					startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
//					if (view != null) {
//						setProgress(view);
//						startProgress();
//					}
					return;
				}
			};
			//httpUtils.send(HttpMethod.GET, GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("secret"), null,requestCallBack );
			
			if(isExperience()){//体验用户的情况下，是需要使用临时Exp_Secret去更新Token
				if(App.app.getData("exp_secret")!=""){
					httpUtils.send(HttpMethod.GET, GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("exp_secret"), null,requestCallBack );
				}
			}else{//非体验用户（登陆用户），是需要使用正式的Secret去更新Token
				httpUtils.send(HttpMethod.GET, GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("secret"), null,requestCallBack );
			}
		}
	}
		startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
		
		
		
		
		v.tv_activity_title.setText("新品曝光");
		
		if ("true".equals(App.app.getData("isExperience"))) {
			v.tv_home_experience.setVisibility(View.VISIBLE);
			v.iv_card.setVisibility(View.GONE);
			v.rl_bg.setBackgroundResource(R.drawable.circle_background_green);
		} else {
			v.iv_card.setVisibility(View.VISIBLE);
			v.tv_home_experience.setVisibility(View.GONE);
		}
		v.rl_bg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentEntity entity=new FragmentEntity();
				Fragment fragment=new MineBankcardFragment();
				Bundle bundle=new Bundle();
				bundle.putBoolean("isFromNewPart", true);
				fragment.setArguments(bundle);
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
			}
		});
		emptyView = inflater.inflate(R.layout.activity_search_empty, null);
		tv_found_secretary = emptyView.findViewById(R.id.tv_found_secretary);
		tv_found_secretary.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*SecretaryFragment fragment = new SecretaryFragment();*/
				MainFragment fragment=new MainFragment(1);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
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
					
					/*refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&service="+
							HODLER_TYPE+"&position="+position_bussness+"&intelligent="+intelligentStr+"&type="+"new"+
							"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"&query="+"",
							null, refreshKey,lv_activity_list);*/
					startSearchData(GlobalValue.URL_ALL_ACTIVITY,
							App.app.getData("select_cityCode"),
							HODLER_TYPE,position_bussness,intelligentStr,
							GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
					justFirstClick = false;
				}
			}
		});
		return noNetworkView;
	}
	
	
	/**
	 * 启动搜索带全部参数
	 * @param base_url
	 * @param site
	 * @param service
	 * @param position
	 * @param intelligent
	 * @param location
	 */
	private void startSearchData(String base_url,String site, String service, String position,//TODO
			String intelligent, String location) {
		
		this.first_enter =0;//保证了每次新的关键字搜索时都拥有 是否为第一次加载的 判断标签
		this.times  = 0;
		lv_activity_list.setNoHeader(false);
		
		if (isShowDialog){
			if(cPd == null ){
				cPd = CustomDialogProgress.createDialog(activity);
				cPd.setCanceledOnTouchOutside(false);
				
				lv_activity_list.setVisibility(View.INVISIBLE);
				cPd.show();
			}else{
				cPd.show();
			}
		}
		setProgress(lv_activity_list);
		startProgress();
		String url=base_url+"site="+site+"&service="+service+"&position="+position+"&intelligent="+intelligent+"&location="+location+"&type=new";
		biz_all.getAllNewData(url,new RequestCallBack() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String json=response.body().string();
					activityList=gson.fromJson(json, ActivityList.class);
					handleOk.sendEmptyMessage(1);
					/*if(justFirstClick){//已经拿到数据的情况下
						justFirstClick = !justFirstClick;
					}*/
				}
			}
			@Override
			public void onFailure(Request request, IOException exception) {
				handleOk.sendEmptyMessage(10);
				if(!justFirstClick){//未拿到数据的时候，要让点击操作有响应，故在此 开放标志信号justFirstClick
					justFirstClick = !justFirstClick;
				}
			}
		});
	}
	
	/**
	 * 上拉刷新url服务端已经拼接
	 * @param base_url
	 */
	private void startSearchData(String base_url) {
		biz_all.getAllNewData(base_url,new RequestCallBack() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String json=response.body().string();
					activityList=gson.fromJson(json, ActivityList.class);
					handleOk.sendEmptyMessage(1);
				}
			}
			@Override
			public void onFailure(Request request, IOException exception) {

			}
		});
	}
	@SuppressWarnings("deprecation")
	protected void startSearchDataByVolley(String next_page_url) {
		queue = Volley.newRequestQueue(App.app);
		String url = next_page_url;
		
		
//		addHeader("Authorization", "Bearer "+App.app.getData("access_token"))
		 
		//根据给定的URL新建一个请求
		StringRequest stringRequest = new StringRequest(Method.GET, url,new Listener<String>() {

			@Override
			public void onResponse(String response) {
				 // 在这里处理请求得到的String类型的响应
		    	LogUtils.toDebugLog("loading", "加载更多成功");
				String json = (String)response;
				activityList=gson.fromJson(json, ActivityList.class);
				handleOk.sendEmptyMessage(1);
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
		// 把这个请求加入请求队列
		queue.add(stringRequest);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 这个地方要写v.expandtab_view.onPressBack();否则下拉控件会有问题
	 */
	@Override
	public void onPause() {
		super.onPause();
		v.expandtab_view.onPressBack();
		/*shopDataList.clear();
		activityAdapter = null;*/
		
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
		
	}
	/**
	 * 对上拉和下拉操作的处理
	 * @param type
	 */
//	@InjectPullRefresh
//	public void call(int type) {
//		// 这里的type来判断是否是下拉还是上拉
//		switch (type) {
//		case InjectView.PULL:
//			isShowDialog=false;
//			if (activityList != null) {
//				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
//				if (activityList.getNext_page_url()== "null"||TextUtils.isEmpty(activityList.getNext_page_url())) {
//					try{
//						lv_activity_list.removeFooterView(emptyView);
//					}catch(Exception e ){
//
//					}
//					lv_activity_list.addFooterView(emptyView);
//
////					PullToRefreshManager.getInstance().onFooterRefreshComplete();
////					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
//					CustomToast.show(activity, "到底啦！", "小迈会加油搜集更多惊喜哦");
//				} else {
//					startSearchData(activityList.getNext_page_url());
//				}
//			}
//			break;
//		case InjectView.DOWN:
//			isShowDialog=false;
//			if (activityList != null) {
//				isDownChange = true;//下拉更新的标志
//				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
//				lv_activity_list.removeFooterView(emptyView);//不能忘了去除底部的emptyView
//			}else{
//				CustomToast.show(activity, "提示", "activityList == null");
//			}
//			break;
//		}
//	}
	/**
	 * item的点击事件
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	/**private void itemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
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
	}*/
	
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
					handleOk.sendEmptyMessage(2);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(2);
		}
		if ("".equals(App.app.getData(App.TAGS[1]))) {
			biz.getAllService("200000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[1],json);
					handleOk.sendEmptyMessage(3);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(3);
		}
		if ("".equals(App.app.getData(App.TAGS[2]))) {
			biz.getAllService("300000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[2],json);
					handleOk.sendEmptyMessage(4);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(4);
		}
		if ("".equals(App.app.getData(App.TAGS[3]))) {
			biz.getAllService("400000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[3],json);
					handleOk.sendEmptyMessage(5);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(5);
		}
		if ("".equals(App.app.getData(App.TAGS[4]))) {
			biz.getAllService("500000",new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[4],json);
					handleOk.sendEmptyMessage(6);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(6);
		}
		if ("".equals(App.app.getData(App.TAGS[5]))) {
			biz.getAllService("600000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[5],json);
					handleOk.sendEmptyMessage(7);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(7);
		}
		if ("".equals(App.app.getData(App.TAGS[6]))) {
			biz.getAllService("700000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[6],json);
					handleOk.sendEmptyMessage(8);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(8);
		}
		if ("".equals(App.app.getData(App.TAGS[7]))) {
			biz.getAllService("800000", new RequestCallBack() {
				@Override
				public void onResponse(Response response) throws IOException {
					String json=response.body().string();
					App.app.setData(App.TAGS[7],json);
					handleOk.sendEmptyMessage(9);
				}
				@Override
				public void onFailure(Request request, IOException exception) {

				}
			});
		}else {
			handleOk.sendEmptyMessage(9);
		}
	}
	private void setFirstValue() {
		if (!"".equals(App.app.getData(App.TAGS[0]))&&!"".equals(App.app.getData(App.TAGS[1]))&&!"".equals(App.app.getData(App.TAGS[2]))&&!"".equals(App.app.getData(App.TAGS[3]))&&!"".equals(App.app.getData(App.TAGS[4]))&&!"".equals(App.app.getData(App.TAGS[5]))&&!"".equals(App.app.getData(App.TAGS[6]))&&!"".equals(App.app.getData(App.TAGS[7]))) {
			String allJson="{name: 全部,data:[{name: 全部,key: 000000},"+App.app.getData(App.TAGS[0])+","+App.app.getData(App.TAGS[1])+","+App.app.getData(App.TAGS[2])+","+App.app.getData(App.TAGS[3])+","+App.app.getData(App.TAGS[4])+","+App.app.getData(App.TAGS[5])+","+App.app.getData(App.TAGS[6])+","+App.app.getData(App.TAGS[7])+"]}";
			Gson gson=new Gson();
			root = gson.fromJson(allJson, ServiceRoot.class);
			
			if(root.getName()==null){
				getAllServer();
				return;
			}
			name = root.getName();
			
			ArrayList<String> allGroup = new ArrayList<String>();
			SparseArray<LinkedList<String>> allChild = new SparseArray<LinkedList<String>>();
			for (int j = 0; j < root.getData().size(); j++) {
				LinkedList<String> chind = new LinkedList<String>();
				
				if(root.getData()==null){
					getAllServer();//重新去获取新品曝光筛选栏服务部分的内容
					return;  //结束下面的执行代码
				}
				
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
	}
	
	@Override
	public void onStop() {
//		PullToRefreshManager.getInstance().headerUnable();
		super.onStop();
	}
	@Override
	public void onDestroy() {
//		PullToRefreshManager.getInstance().headerUnable();
	    getActivity().unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
	
     class NewBrandRefreshBroadCastReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.refreshTheIcon")){
				System.out.println("新品曝光板块刷新页面 收到刷新Icon的广播了");
				v.iv_card.setVisibility(View.VISIBLE);
				v.tv_home_experience.setVisibility(View.GONE);
				v.rl_bg.setBackgroundResource(R.drawable.circle_background_gray);
			
			}
		}
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
 	 						startSearchData(GlobalValue.URL_ALL_ACTIVITY,
 	 								App.app.getData("select_cityCode"),HODLER_TYPE,
 	 								position_bussness,intelligentStr,
 	 								GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
 	 						
 	 						getAllBannerContentFromNetOrLocal();
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
     
}
