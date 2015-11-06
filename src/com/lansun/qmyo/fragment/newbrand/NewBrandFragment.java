package com.lansun.qmyo.fragment.newbrand;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
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
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
/**
 * time 2015-11-5
 * @author Guzman.Geng
 * 此版块为新品曝光栏目
 */
public class NewBrandFragment extends BaseFragment{
	private Gson gson;
	private ServiceAllBiz biz;
	private String[] jsons=new String[8];
	private Map<String, String> map=new HashMap<>();
	private String HODLER_TYPE="000000";
	private View tv_found_secretary;
	private boolean isPosition;
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
	private ActivityList activityList;
	private SearchAdapter activityAdapter;

	@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), down = true, pull = true)
	private MyListView lv_activity_list;
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
				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				endProgress();
				if (activityList.getData() != null) {//服务器返回回来的数据中的Data不为null
					if (!isRemove) {
						lv_activity_list.removeFooterView(emptyView);
						isRemove=true;
					}
					if(isDownChange){//下拉刷新时,需要将数据重新获取,即将shopDataList清空掉
						shopDataList.clear();
						isDownChange = false;
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
						if(activityList.getData().size()<10){
							if (isRemove) {
								lv_activity_list.addFooterView(emptyView);
								LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
								params.weight=1;
								lv_activity_list.setLayoutParams(params);
								isRemove=false;
							}
						}else {
							if (!isRemove) {
								lv_activity_list.removeFooterView(emptyView);
								isRemove=true;
							}
						}
					} else {
						//adapter并不为空时
						activityAdapter.notifyDataSetChanged();
						if(activityList.getData().size()<10){
							if (isRemove) {
								lv_activity_list.addFooterView(emptyView);
								isRemove=false;
							}
						}else {
							if (!isRemove) {
								lv_activity_list.removeFooterView(emptyView);
								isRemove=true;
							}
						}
						/*activityAdapter.notifyDataSetChanged();*/
					}
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();

				} else {//因为上拉前去获取到的数据为null，此时需要将之前的值保留住并展示
					lv_activity_list.setAdapter(null);
					activityAdapter.notifyDataSetChanged();
					if (isRemove) {
						lv_activity_list.addFooterView(emptyView);
						isRemove=false;
					}
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		biz_all = new AllNewDataBiz();
		gson = new Gson();
		viewLeft=new ViewLeft(getActivity());
		viewLeft2=new ViewLeft(getActivity());
		viewMiddle=new ViewMiddle(getActivity());
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
				intelligentStr = intelligent.getData().get(position).getKey();
				shopDataList.clear();
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				onRefresh(viewMiddle, showText);
			}
		});

		//所有服务模块
		viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {
			@Override
			public void getValue(String showText, int parentId, int position) {
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
				}
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
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
		startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
		v.tv_activity_title.setText("新品曝光");
		
		if ("true".equals(App.app.getData("isExperience"))) {
			v.rl_bg.setPressed(true);
			v.tv_home_experience.setVisibility(View.VISIBLE);
			v.iv_card.setVisibility(View.GONE);
		} else {
			v.iv_card.setVisibility(View.VISIBLE);
			v.tv_home_experience.setVisibility(View.GONE);
			v.rl_bg.setPressed(false);//含有“体验”两个字的灰色按钮隐藏掉
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
				SecretaryFragment fragment = new SecretaryFragment();
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});
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
	private void startSearchData(String base_url,String site, String service, String position,
			String intelligent, String location) {
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
				}
			}
			@Override
			public void onFailure(Request request, IOException exception) {

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
	/**
	 * 这个地方要写v.expandtab_view.onPressBack();否则下拉控件会有问题
	 */
	@Override
	public void onPause() {
		super.onPause();
		v.expandtab_view.onPressBack();
		shopDataList.clear();
		activityAdapter = null;
	}
	/**
	 * 对上拉和下拉操作的处理
	 * @param type
	 */
	@InjectPullRefresh
	public void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (activityList != null) {
				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
				if (activityList.getNext_page_url()== "null"||TextUtils.isEmpty(activityList.getNext_page_url())) {
					try{
						lv_activity_list.removeFooterView(emptyView);
					}catch(Exception e ){

					}
					lv_activity_list.addFooterView(emptyView);

					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, "到底啦！", "小迈会加油搜集更多惊喜哦");
				} else {
					startSearchData(activityList.getNext_page_url());
				}
			}
			break;
		case InjectView.DOWN:
			if (activityList != null) {
				isDownChange = true;//下拉更新的标志
				startSearchData(GlobalValue.URL_ALL_ACTIVITY,App.app.getData("select_cityCode"),HODLER_TYPE,position_bussness,intelligentStr,GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				lv_activity_list.removeFooterView(emptyView);//不能忘了去除底部的emptyView
			}else{
				CustomToast.show(activity, "ti", "activityList == null");
			}
			break;
		}
	}
	/**
	 * item的点击事件
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
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
}
