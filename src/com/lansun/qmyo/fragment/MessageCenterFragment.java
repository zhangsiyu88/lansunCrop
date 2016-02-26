package com.lansun.qmyo.fragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
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
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.lansun.qmyo.adapter.MessageAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.base.BackHandedFragment;
import com.lansun.qmyo.domain.MessageData;
import com.lansun.qmyo.domain.MessageList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.NotifyUtils;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.swipe.SwipeListMineMessageAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineMessageAdapter.ItemClickCallback;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lansun.qmyo.R;

public class MessageCenterFragment extends BackHandedFragment implements ItemClickCallback {
	private MessageList list;
	
	/*private MessageAdapter adapter;*/
	private SwipeListMineMessageAdapter adapter;
	private int times = 0;
	private CustomDialogProgress cPd = null;
	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
	private int index;
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_message_activity, tv_message_maijie , maijie_point, activity_point,tv_all_message_isread;
		private View no_data;
		
	}
	@InjectView
	private TestMyListView lv_message_list;
	private boolean isShowDialog;
	/**
	 * 整页未读消息中的   已读消息列表
	 */
	private ArrayList<HashMap<String, String>> hasReadList = new ArrayList<HashMap<String, String>>();
	
	/**
	 * 整页消息列表中的 未读消息列表
	 */
	private ArrayList<HashMap<String, String>> hasUnreadListInTotalPage = new ArrayList<HashMap<String, String>>();
	
	
	
	private int activityMessageCounts;

	private int maijieMessageCounts;

	private String shopId;

	private String activityId;

	private boolean tag_inReadedList;
    /**
     * 信息的编号
     */
	private String id;

	private MessageCenterBroadcast broadCastReceiver;
	
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		broadCastReceiver = new MessageCenterBroadcast();
		System.out.println("MessageCenterFragment中注册广播 ing");
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.message");
		filter.addAction("com.lansun.qmyo.notifyMessageCenterList");
		getActivity().registerReceiver(broadCastReceiver, filter);
	}
	@Override
	public void onDestroy() {
		activity.unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_message_center, null);
		Handler_Inject.injectFragment(this, rootView);
		
		
		showCounts(v.activity_point, Integer.valueOf(NotifyUtils.getInstance().getActivityMessageCounts()));
		activityMessageCounts = Integer.valueOf(NotifyUtils.getInstance().getActivityMessageCounts());
		showCounts(v.maijie_point, Integer.valueOf(NotifyUtils.getInstance().getMaijeMessageCounts()));
		maijieMessageCounts = Integer.valueOf(NotifyUtils.getInstance().getMaijeMessageCounts());
		
		//一旦进入进来后的，就发送广播将小绿点给取消掉
		/*activity.sendBroadcast(new Intent("com.lansun.qmyo.DeleteTheLGPStatus"));
		LogUtils.toDebugLog("infos", "消息中心一打开，广播通知小绿点消失");*/
		if(activityMessageCounts>0||maijieMessageCounts>0||Integer.valueOf(NotifyUtils.getInstance().getSecretaryMessageCounts())>0){
			v.tv_all_message_isread.setVisibility(View.VISIBLE);
			v.tv_all_message_isread.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					DialogUtil.createTipAlertDialog(getActivity(),
							R.string.total_read, new TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							InternetConfig config = new InternetConfig();
							config.setKey(3);
							HashMap<String, Object> head = new HashMap<>();
							head.put("Authorization", "Bearer " + App.app.getData("access_token"));
							config.setHead(head);
							FastHttpHander.ajaxGet(GlobalValue.URL_TOTAL_MESSAGE_READ, null, config, MessageCenterFragment.this);
						}
						@Override
						public void onNegativeButtonClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				}
			});
		}
		
		
		lv_message_list.setNoHeader(true);
		lv_message_list.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefreshing() {
				//NO-OP
			}
			
			@Override
			public void onLoadingMore() {
				if (list != null) {
					if (TextUtils.isEmpty(list.getNext_page_url())||list.getData()==null||list.getNext_page_url()=="null") {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						
						if(times ==0){
							CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
							lv_message_list.onLoadMoreOverFished();
							times++;
						}else{
							lv_message_list.onLoadMoreOverFished();
							
						}
						
					} else {
						refreshCurrentList(list.getNext_page_url(), null, 0, lv_message_list);
						lv_message_list.onLoadMoreFished();//需要将isLoadMore置为false
					}
				}else{
					CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
					lv_message_list.onLoadMoreOverFished();
				}
			}
		});
		
		return rootView;
	}

	@InjectInit
	private void init() {
		refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey, lv_message_list);
	}
	
	private void click(View view) {
//		NotifyUtils.sendNotifictionCounts();
		lv_message_list.setVisibility(View.INVISIBLE);
		dataList.clear();
		adapter = null;
		lv_message_list.setAdapter(null);
		lv_message_list.onLoadMoreOverFished();
		times = 0;
		first_enter = 0;
		switch (view.getId()) {
		case R.id.tv_message_activity:// TODO 活动
			refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
			changeTextColor(v.tv_message_activity);
			index=0;
			isShowDialog = true;
			break;
		case R.id.tv_message_maijie:// TODO 迈界
			refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.maijie;
			changeTextColor(v.tv_message_maijie);
			index=1;
			isShowDialog = true;
			break;
		}
		setProgress(lv_message_list);
		startProgress();
		refreshCurrentList(refreshUrl, null, refreshKey, lv_message_list);
		if (isShowDialog){
			if(cPd == null ){
				Log.d("dialog","生成新的dialog！");
				cPd = CustomDialogProgress.createDialog(activity);
				cPd.setCanceledOnTouchOutside(false);
				cPd.show();
			}else{
				cPd.show();
			}
		}
	}

	private void changeTextColor(TextView tv) {
		v.tv_message_maijie.setTextColor(getResources().getColor(
				R.color.app_grey5));
		v.tv_message_activity.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			if(cPd!=null){
				cPd.dismiss();
				cPd = null;
			}
			switch (r.getKey()) {
			case 0:
				endProgress();
				
				list = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				if(list!=null){
					if (list.getData() != null) {
						v.no_data.setVisibility(View.GONE);
						HashMap<String, String> map = new HashMap<String, String>();
						
						for (MessageData data : list.getData()) {
							map = new HashMap<String, String>();
							map.put("id", data.getId());
							map.put("activity_id", data.getActivity_id()+"");
							map.put("shop_id", data.getShop_id()+"");
							map.put("tv_message_item_name", data.getTitle());
							map.put("tv_message_item_desc", data.getContent());
							
							String historyTime = "";
							if(data.getTime().equals(null)){
								historyTime = "时间格式异常";
							}else{
									long time = Handler_Time.getInstance(data.getTime()).getTimeInMillis();
									
									if ((System.currentTimeMillis() - time)/ (1000 * 24 * 60 * 60) > 1) {
										Handler_Time instance = Handler_Time.getInstance(data.getTime());
										historyTime = instance.getYear() + "-"
												+ instance.getMonth() + "-"
												+ instance.getDay();
									} else {
										historyTime = getHistoryTime(System.currentTimeMillis() - time);
									}
							}
							map.put("tv_search_activity_time", historyTime);
							map.put("tv_message_item_count", data.getIs_read() + "");
							dataList.add(map);
							
							//对dataList中的信息数据进行未读信息的筛选并提取出来
							for(int j= 0;j<dataList.size();j++){
								HashMap<String, String> hashMap = dataList.get(j);
								if(hashMap.get("tv_message_item_count").equals("0")){//表示此条消息为 未读
									hasUnreadListInTotalPage.add(hashMap);
								}
							}
							
						}
						
						if (adapter == null) {
							/*adapter = new MessageAdapter(lv_message_list, dataList,R.layout.manager_group_list_item_parent);*/
							adapter = new SwipeListMineMessageAdapter(activity, dataList);
							adapter.setItemClickCallback(this);
							lv_message_list.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
						if(list.getData().size()<10){
							if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
					            lv_message_list.onLoadMoreOverFished();
					          }else{
					            //DO-OP
					          }
//							times++;
//							lv_message_list.onLoadMoreOverFished();
//							CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
						}
						this.first_enter = Integer.MAX_VALUE;
						lv_message_list.setVisibility(View.VISIBLE);
						lv_message_list.onLoadMoreFished();
						
					} else {//list.getData() == null
						lv_message_list.setAdapter(null);
						lv_message_list.onLoadMoreOverFished();
						lv_message_list.setVisibility(View.INVISIBLE);
						v.no_data.setVisibility(View.VISIBLE);
					}
				}else{//list==null
					v.no_data.setVisibility(View.VISIBLE);
					lv_message_list.onLoadMoreOverFished();
				}
				break;
				
			case 2:
				LogUtils.toDebugLog("log", r.getContentAsString());
				break;
			case 3:
				if(r.getContentAsString().contains("true")){
					v.tv_all_message_isread.setVisibility(View.GONE);
					activity.sendBroadcast(new Intent("com.lansun.qmyo.notifyMessageCenterList"));
				}
				LogUtils.toDebugLog("log", r.getContentAsString());
				break;
			}
		}
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}


	
	private String getHistoryTime(long mss) {
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		String time = "";
		if (minutes == 0) {
			time = "刚刚";
			return time;
		}
		if (minutes != 0) {
			time = minutes + "分" + time;
		}
		if (hours != 0) {
			time = hours + "小时";
		}
		return time + "前";
	}

	/**
	 * 重写在Adapter中设置的点击产生的实际操作，再次回调中实现
	 * @param position
	 */
	@Override
	public void listItemClick(int position) {
		
		
		if (index==0) {//活动消息
			
			/*bundle.putString("shopId", list.getData().get(position).getShop_id()+"");
			bundle.putString("activityId", list.getData().get(position).getActivity_id()+"");*/
			
			id = dataList.get(position).get("id");
			shopId = dataList.get(position).get("shop_id");
			activityId = dataList.get(position).get("activity_id");
			
			/*
			 * 获取此列是否为整个页面消息中的未读条目
			 * 
			 * 要求在原始的dataList中为未读的消息
			 * 还得在存取出来的
			 */
			tag_inReadedList = false;//默认: 认为是不在已读列表中
			
			//判断当前活动是否在已读列表中
			
			
			if(dataList.get(position).get("tv_message_item_count").equals("1")){
				jump2ActivityDetailFragment(position);
				
			}else{//表明此条目为未读的消息
				//此处需访问更改状态接口：HttpRequest
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
				params.put("type", "activity");
				params.put("message_id", id);
				params.put("activity_id", "");
				params.put("shop_id", "");
//				params.put("activity_id", activityId);
//				params.put("shop_id", shopId);
				refreshCurrentList(GlobalValue.URL_USER_MESSAGE+"/info?", params, 2, lv_message_list);
				--activityMessageCounts;
				
				showCounts(v.activity_point, activityMessageCounts);
				
				jump2ActivityDetailFragment(position);
				dataList.get(position).put("tv_message_item_count", String.valueOf(1));
			}	
//				for(int i=0;i<hasReadList.size();i++){//且存在于
//					if(hasReadList.get(i).get("id").equals(Integer.valueOf(id))){
//						//表示为存在已读的列表中
//						LogUtils.toDebugLog("message", "存在于已读列表中");
//						tag_inReadedList = true;
//						break;
//					}
//				}                                                                
//				//0.和已被点击的列表进行比对
//				
//				//0.1.dataList中未被点击的，在点击后，添加进入已被点击过得列表中
//				
//				//0.2.比对后发现存在于已被点击的过得列表中，那么做 消息数目减1 的操作
//				
//				
//				
//				if(!tag_inReadedList){//是未读的信息，并且不在已读列表中
//					LogUtils.toDebugLog("message", "这次点击之前不存在于 已读列表中，现在已被点击打开，需置为已读过状态");
//					HashMap<String, String> hashMap = new HashMap<String, String>();
//					hashMap.put("shopId", dataList.get(position).get("shop_id"));
//					hashMap.put("activity_id", dataList.get(position).get("activity_id"));
//					hashMap.put("id", dataList.get(position).get("id"));
//					hasReadList.add(hashMap);//将此条信息塞入到 已经读过的列表中
//					--activityMessageCounts;
//					v.activity_point.setText(String.valueOf(activityMessageCounts));
//					
//					//此处需访问更改状态接口：HttpRequest
//					LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
//					params.put("type", "activity");
//					params.put("message_id", id);
//					refreshCurrentList(GlobalValue.URL_USER_MESSAGE+"/info?", params, 2, lv_message_list);
//				}
			
			
		}else {//迈界消息
			/* 暂时不做点击处理*/
			
//			id = dataList.get(position).get("id");
//			tag_inReadedList = false;//默认: 认为是不在已读列表中
//			
//			//判断当前活动是否在已读列表中
//			//表明此条目为未读的消息
//			if(dataList.get(position).get("tv_message_item_count").equals("0")){//当前这个position上的拥有未读标签，
//				for(int i=0;i<hasReadList.size();i++){
//					if(hasReadList.get(i).get("id").equals(Integer.valueOf(id))){
//						//表示为存在已读的列表中
//						LogUtils.toDebugLog("message", "存在于已读列表中");
//						tag_inReadedList = true;
//						break;
//					}
//				}                                                                
//			}
//			//0.和已被点击的列表进行比对
//			
//			//0.1.dataList中未被点击的，在点击后，添加进入已被点击过得列表中
//			
//			//0.2.比对后发现存在于已被点击的过得列表中，那么做 消息数目减1 的操作
//			
//			if(!tag_inReadedList){//是未读的信息，并且不在已读列表中
//				LogUtils.toDebugLog("message", "这次点击之前不存在于 已读列表中，现在已被点击打开，需置为已读过状态");
//				HashMap<String, String> hashMap = new HashMap<String, String>();
//				hashMap.put("id", dataList.get(position).get("id"));
//				hasReadList.add(hashMap);//将此条信息塞入到 已经读过的列表中
//				--maijieMessageCounts;
//				v.maijie_point.setText(String.valueOf(maijieMessageCounts));
//				
//				//此处需访问更改状态接口：HttpRequest
//				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
//				params.put("type", "shop");
//				params.put("message_id", id);
//				refreshCurrentList(GlobalValue.URL_USER_MESSAGE+"/info?", params, 2, lv_message_list);
//				LogUtils.toDebugLog("message", "现将此条信息写入值hasReadList中");
//			}
//			FragmentEntity entity=new FragmentEntity();
//			Fragment fragment=new MaijieMessageDetailFragment();
//			Bundle bundle=new Bundle();
//			bundle.putString("content", dataList.get(position).get("tv_message_item_desc"));
//			bundle.putString("title", dataList.get(position).get("tv_message_item_name"));
//			bundle.putString("time", dataList.get(position).get("tv_search_activity_time"));
//			fragment.setArguments(bundle);
//			entity.setFragment(fragment);
//			EventBus.getDefault().post(entity);
			
			
			
			id = dataList.get(position).get("id");
			if(dataList.get(position).get("tv_message_item_count").equals("1")){
				jump2MaijieMessageDetailFragment(position);
				
			}else{//表明此条目为未读的消息
				//此处需访问更改状态接口：HttpRequest
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
				params.put("type", "shop");
				params.put("message_id", id);
				params.put("activity_id", "");
				params.put("shop_id", "");
				refreshCurrentList(GlobalValue.URL_USER_MESSAGE+"/info?", params, 2, lv_message_list);
				jump2MaijieMessageDetailFragment(position);
				--maijieMessageCounts;
				showCounts(v.maijie_point, maijieMessageCounts);
				dataList.get(position).put("tv_message_item_count", String.valueOf(1));
				LogUtils.toDebugLog("message", "消息状态已被更改");
		       }
	      }
	}

	private void jump2ActivityDetailFragment(int position) {
		//如果为已读的情况正常跳转
		//---------------------------正常页面跳转的代码---------------------------------------
		FragmentEntity entity=new FragmentEntity();
		Fragment fragment=new ActivityDetailFragment();
		Bundle bundle=new Bundle();
		bundle.putString("shopId", dataList.get(position).get("shop_id"));
		bundle.putString("activityId", dataList.get(position).get("activity_id"));
		fragment.setArguments(bundle);
		entity.setFragment(fragment);
		EventBus.getDefault().post(entity);
	}
	private void jump2MaijieMessageDetailFragment(int position) {
		//---------------------------正常跳转---------------------------------------
		FragmentEntity entity=new FragmentEntity();
		Fragment fragment=new MaijieMessageDetailFragment();
		Bundle bundle=new Bundle();
		bundle.putString("content", dataList.get(position).get("tv_message_item_desc"));
		bundle.putString("title", dataList.get(position).get("tv_message_item_name"));
		bundle.putString("time", dataList.get(position).get("tv_search_activity_time"));
		fragment.setArguments(bundle);
		entity.setFragment(fragment);
		EventBus.getDefault().post(entity);
	}
	
	

	@Override
	@InjectMethod(@InjectListener(ids = 2131427431, listeners = OnClick.class))
	protected void back() {
		NotifyUtils.getInstance().sendNotifictionCounts();
		super.back();
	}

	class MessageCenterBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.message")){
		    	 int messageCounts = intent.getIntExtra("messageCounts", 0);
		    	 Intent intent_changeLGP = new Intent("com.lansun.qmyo.ChangeTheLGPStatus");
		    	 intent_changeLGP.putExtra("messageCounts", messageCounts);
		         activity.sendBroadcast(intent_changeLGP);//通知绿点展示
		         LogUtils.toDebugLog("notify", "MessageCenter Frag接收到广播通知");
		        
		         NotifyUtils.mContext = activity;
//		         showCounts(v.activity_point, Integer.valueOf(NotifyUtils.getActivityMessageCounts()));
//		         showCounts(v.maijie_point, Integer.valueOf(NotifyUtils.getMaijeMessageCounts()));
		         showCounts(v.activity_point, Integer.valueOf(NotifyUtils.getInstance().getActivityMessageCounts()));
		         showCounts(v.maijie_point, Integer.valueOf(NotifyUtils.getInstance().getMaijeMessageCounts()));
		         
		         
		         /*if(Integer.valueOf(NotifyUtils.getActivityMessageCounts()) > 0){
		 			v.activity_point.setVisibility(View.VISIBLE);
		 			if(Integer.valueOf(NotifyUtils.getActivityMessageCounts())>99){
		 				v.activity_point.setText("99+");
		 			}else{
		 				v.activity_point.setText(NotifyUtils.getActivityMessageCounts());
		 			}
		 			activityMessageCounts = Integer.valueOf(NotifyUtils.getActivityMessageCounts());
		 		}else{
		 			v.activity_point.setVisibility(View.GONE);
		 		}
		 		
		 		if(Integer.valueOf(NotifyUtils.getMaijeMessageCounts()) > 0){
		 			v.maijie_point.setVisibility(View.VISIBLE);
		 			if(Integer.valueOf(NotifyUtils.getMaijeMessageCounts())>99){
		 				v.maijie_point.setText("99+");
		 			}else{
		 				v.maijie_point.setText(NotifyUtils.getMaijeMessageCounts());
		 			}
		 		    maijieMessageCounts = Integer.valueOf(NotifyUtils.getMaijeMessageCounts());
		 		    LogUtils.toDebugLog("notify", "maijieMessageCounts的值为： "+maijieMessageCounts);
		 		}else{
		 			v.maijie_point.setVisibility(View.GONE);
		 		}*/
			  }
			else if(intent.getAction().equals("com.lansun.qmyo.notifyMessageCenterList")){
				//接收来自活动详情页面的广播通知，刷新当前的页面列表
				init();
				NotifyUtils.getInstance().sendNotifictionCounts();
			}
		}
		
	}
	
	public void showCounts(TextView v,int counts){
		if(counts>99){
			v.setVisibility(View.VISIBLE);
			v.setText("99+");
		}else if(counts>0&&counts<100){
			v.setVisibility(View.VISIBLE);
			v.setText(String.valueOf(counts));
		}else if(counts == 0){
			v.setVisibility(View.GONE);
		}
	 }
	@Override
	public boolean onBackPressed() {
		back();
		return true;
	}
}
