package com.lansun.qmyo.fragment;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
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
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.StoreAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.domain.Shop;
import com.lansun.qmyo.domain.StoreList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;

import com.lansun.qmyo.view.ViewRight;
import com.lansun.qmyo.R;

/**
 * 门店列表
 * 
 * @author bhxx
 * 
 */
public class StoreListFragment extends BaseFragment {

	private ViewLeft viewLeft;
	private ViewRight viewRight;
	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();
	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();
	private StoreAdapter adapter;

	@InjectAll
	Views v;
	private String activityId;
	private ArrayList<HashMap<String, String>> dataList;

	/**
	 * 全部服务信息
	 */
	private Service nearService;

	/**
	 * 筛选
	 */
	private Intelligent sxintelligent;
	protected String position;
	private String type = "recommend";

	/*@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)*/
	
	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, pull = true)
	private MyListView lv_stores_content;
	private StoreList list;
	private String shopId;
	private boolean isPull = false;
	private boolean isPosition;
	private View emptyView;
	private TextView tv_found_secretary;
	private RelativeLayout activity_search_empty_storelist;

	class Views {
		private View fl_comments_right_iv, tv_activity_shared;
		private ExpandTabView expandtab_view;
		private TextView tv_activity_title;
		private RelativeLayout activity_search_empty_storelist;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_store_list, null);
		activity_search_empty_storelist = (RelativeLayout) rootView.findViewById(R.id.activity_search_empty_storelist);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	/**
	 * 门店列表跳转
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void itemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Shop shop = list.getData().get(arg2);
		StoreDetailFragment fragment = new StoreDetailFragment();
		Bundle args = new Bundle();
		args.putString("shopId", shop.getId() + "");
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		
		emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_empty_storelist1, null);
		
		super.onCreate(savedInstanceState);
	}
	
	
	
	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initData();
		if (getArguments() != null) {

			int type = getArguments().getInt("type");
			activityId = getArguments().getString("activityId");
			shopId = getArguments().getString("shopId");
			if (type == 0) {
				// 所有分店
				v.tv_activity_title.setText(getString(R.string.check_all));
			} else {

				v.tv_activity_title.setText(getString(R.string.join_activity_shop));
			}
		}
		refreshUrl = String.format(GlobalValue.URL_ACTIVITY_SHOPS, activityId+ "");
		
		refreshKey = 2;
		
		refreshParams = new LinkedHashMap<String, String>();
		refreshParams.put("location",GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
		
		/*refreshParams.put("site","310000");*/
		refreshParams.put("site",App.app.getData("cityCode"));
		
		refreshParams.put("shop_id",shopId);
		refreshParams.put("position","nearby");
		refreshParams.put("intelligent","recommend");
		
		refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_stores_content);
	}

	private void initData() {
		holder_button.clear();
		mViewArray.clear();
		v.expandtab_view.removeAllViews();
		
		// 附近 固定
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		FastHttpHander.ajaxGet(
				GlobalValue.URL_SEARCH_HOLDER_DISTRICT
						+ App.app.getData("select_cityCode"), config, this);//获取附近的商圈数据

		// 智能排序
		InternetConfig config1 = new InternetConfig();
		config1.setKey(1);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT,config1, this);
		
		
		/*// 筛选
		InternetConfig config1 = new InternetConfig();
		config1.setKey(1);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SCREENING,config1, this);//获取筛选的类型*/
	}

	private void onRefresh(View view, String showText) {

		v.expandtab_view.onPressBack();
		int position = getPositon(view);
		if (position >= 0
				&& !v.expandtab_view.getTitle(position).equals(showText)) {
			v.expandtab_view.setTitle(showText, position);
		}
	}

	@Override
	public void onPause() {
		v.expandtab_view.onPressBack();
		/*dataList.clear();
		adapter = null;*/
		super.onPause();
	}

	private int getPositon(View tView) {
		for (int i = 0; i < mViewArray.size(); i++) {
			if (mViewArray.get(i) == tView) {
				return i;
			}
		}
		return -1;
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			
			endProgress();
			
			PullToRefreshManager.getInstance().onFooterRefreshComplete();
			/*dataList = new ArrayList<HashMap<String, String>>();*/
			String name;
			switch (r.getKey()) {
  
			case 0:// 全部  左边的内容拿到的是距离和商圈的信息
				viewLeft = new ViewLeft(activity);
				nearService = Handler_Json.JsonToBean(Service.class,r.getContentAsString());
				
				name = nearService.getName();
				if (name == null) {
					name = nearService.getData().get(0).getName();
				}
				ArrayList<String> nearGroup = new ArrayList<String>();
				
				//SparseArray,骚气的新数据结构
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
				
				holder_button.put(0, name);//设置左侧的栏目名 
				
				viewLeft.setGroups(nearGroup);
				viewLeft.setChildren(allNearChild);
				mViewArray.put(0, viewLeft);
				break;
			case 1:// 筛选类型
				viewRight = new ViewRight(activity);
				sxintelligent = Handler_Json.JsonToBean(Intelligent.class,
						r.getContentAsString());
				name = sxintelligent.getName();
				ArrayList<String> iconGroup = new ArrayList<String>();
				ArrayList<String> sxGroup = new ArrayList<String>();
				ArrayList<Data> sxData = sxintelligent.getData();
				for (Data d : sxData) {
					sxGroup.add(d.getName());
					iconGroup.add(d.getKey());
				}
				holder_button.put(1, name);//设置右侧的栏目名 
				viewRight.setICons(iconGroup);
				viewRight.setItems(sxGroup);
				mViewArray.put(1, viewRight);
				break;

			case 2:
				Log.i("r.getContentAsString()","参与此活动的门店类型"+r.getContentAsString());
				list = Handler_Json.JsonToBean(StoreList.class,r.getContentAsString());
				
				if(isPull){
					isPull = false;
				}else{
					dataList = new ArrayList<HashMap<String, String>>();
				}
				
				if (list.getData() != null ) {
					for (Shop s : list.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("tv_store_item_name", s.getName());
						map.put("tv_store_item_num", s.getAttention() + "");
						map.put("tv_store_item_distance", s.getDistance());
						map.put("rb_store_item", s.getEvaluate() + "");
						Log.i("每个塞进来的map值为：",map.toString());
						dataList.add(map);
					}
					if (adapter == null) {
						adapter = new StoreAdapter(lv_stores_content, dataList,R.layout.activity_store_item);
						
						try{
							lv_stores_content.setAdapter(adapter);//先将adapter和ListView挂上钩
							lv_stores_content.removeFooterView(emptyView);
						}catch(Exception e ){
						}
						lv_stores_content.setAdapter(adapter);
						Log.i("","走到此处证明已经拿到服务器返回的数据，已经将adapter和ListView控件塞到一起了,new StoreAdapter");
						
					} else {
						/*Log.i("","走到此处证明已经拿到服务器返回的数据，已经将adapter和ListView控件塞到一起了,notifyDataSetChanged");*/
						/*adapter = new StoreAdapter(lv_stores_content, dataList,R.layout.activity_store_item);*/
						
						adapter.notifyDataSetChanged();
						/*lv_stores_content.setAdapter(adapter);*/
					
					}
					/*if(list.getData().size()<10){
						lv_stores_content.addFooterView(emptyView);
					}*/
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					
				} else {//list.getData()为 空
					
					try{
						lv_stores_content.removeFooterView(emptyView);
						/*v.activity_search_empty_storelist.setVisibility(View.INVISIBLE);*/
					}catch(Exception e ){
					}
					lv_stores_content.addFooterView(emptyView);
					/*activity_search_empty_storelist.setVisibility(View.VISIBLE);*/
					lv_stores_content.setAdapter(null);
					/*adapter.notifyDataSetChanged();*/
					
					
				}
				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				break;
			}
			
			
			
			//expandtab_view中的左边的viewLeft内容
			if (holder_button.size() == 2) {
				v.expandtab_view.setValue(holder_button, mViewArray);
				//左侧栏目监听
				viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {

					@Override
					public void getValue(String showText, int parentId,
							int position) {

						if (parentId == 0) {

							if (nearService.getData().get(parentId).getItems() == null) {
								onRefresh(viewLeft, showText);
								StoreListFragment.this.position = nearService.getData().get(parentId).getKey()+ "";
							} else if (nearService.getData().get(parentId)
									.getItems().get(position) != null) {
								StoreListFragment.this.position = nearService.getData().get(parentId).getItems().get(position).getKey();
							}
						} else {
							StoreListFragment.this.position = nearService.getData().get(parentId).getItems().get(position).getKey();
							 
						}
						dataList.clear();
						 if (adapter != null) {
							 adapter.notifyDataSetChanged();
						 }
						 loadActivityList();
						 onRefresh(viewLeft, showText);
					}
				});
				//右侧栏目监听
				viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {

					@Override
					public void getValue(String distance, String showText,
							int position) {
						type = sxintelligent.getData().get(position).getKey();
						dataList.clear();
						loadActivityList();
						onRefresh(viewRight, showText);
					}
				});
			}
		} else {
			PullToRefreshManager.getInstance().onHeaderRefreshComplete();
			PullToRefreshManager.getInstance().onFooterRefreshComplete();
			progress_text.setText(R.string.net_error_refresh);
		}
	}



	@InjectPullRefresh
	private void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url().contains("null")||list.getNext_page_url() == null) {
					
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					
					/*try{
						lv_stores_content.removeFooterView(emptyView);
					}catch(Exception e ){
					}
					lv_stores_content.addFooterView(emptyView);*/
					
					CustomToast.show(activity, "到底啦！", "涉及此活动的门店只有这么多");
				} else {
					refreshCurrentList(list.getNext_page_url(), null,refreshKey, lv_stores_content);
					isPull = true;
				}
			}
			break;
			
		case InjectView.DOWN:
			if (list != null) {
				refreshCurrentList(refreshUrl, null, refreshKey,
						lv_stores_content);
			}
			break;
		}
	}

	/**
	 * 加载活动列表,最后走的都是refreshCurrentList,只不过携带的参数不同
	 */
	private void loadActivityList() {
		
		
	/** 参考页面初始化中的内容
	 * 	refreshUrl = String.format(GlobalValue.URL_ACTIVITY_SHOPS, activityId+ "");
		refreshKey = 2;
		refreshParams = new LinkedHashMap<String, String>();
		refreshParams.put("location",GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
		refreshParams.put("site","310000");
		refreshParams.put("site",App.app.getData("cityCode"));
		refreshParams.put("shop_id",shopId);
		refreshParams.put("position","nearby");
		refreshParams.put("intelligent","recommend");
		refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_stores_content);*/
		
		// 活动列表
		refreshParams = new LinkedHashMap<>();
		if (getCurrentCity()[0].equals(getSelectCity()[0])) {
			isPosition = true;
			refreshParams.put("location", GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());//问题在这儿!这个location 拼接在了服务器访问接口里面
		}
		
		
		refreshParams.put("site", getSelectCity()[0]);
	    refreshParams.put("poistion",position);
	    refreshParams.put("intelligent", type);
		/*refreshParams.put("type", type);//选中的类型*/		
		refreshParams.put("shop_id",shopId);
		
		refreshUrl =  String.format(GlobalValue.URL_ACTIVITY_SHOPS, activityId+ "");
		refreshKey = 2;
		
		/*refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_stores_content);*/
		
		Log.i("", "下面进行刷新列表的操作了！");
		refreshCurrentList(refreshUrl+"site="+getSelectCity()[0]+"&shop_id="+shopId+"&position="+position+"&intelligent="+type+"&location="+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon()+"",
				null, refreshKey,lv_stores_content);
	}

	
	
	
}
