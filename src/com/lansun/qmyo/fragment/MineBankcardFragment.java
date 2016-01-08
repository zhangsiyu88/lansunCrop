package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.bu;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
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
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.base.BackHandedFragment;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.newbrand.NewBrandFragment;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeListMineBankcardAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineBankcardAdapter.FromNetCallBack;
import com.lansun.qmyo.view.BankcardListView;
import com.lansun.qmyo.view.BankcardListView.OnRefreshListener;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
//import com.lansun.qmyo.view.MyListView;
//import com.lansun.qmyo.view.TestMyListView;
//import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

@SuppressLint("InflateParams") public class MineBankcardFragment extends BackHandedFragment implements FromNetCallBack{

	
	private boolean mIsFromHome = false;
	private boolean mIsFromEightPart = false;
	private boolean mIsFromNewPart = false;
	private boolean mIsFromRigisterFragToMyBankcardFrag = false;
	private boolean mIsFromInsertBankcardAdapterPage = false;
	private int type;
	private int mInitType;
	private String bankCardId;
	private int times = 0;
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
					//在一进入我的这个界面时，将其展示出Dialog
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					final Dialog dialog2 = new Dialog(activity,R.style.CustomDialogForNewUserTip);
					View layout2 = inflater.inflate(R.layout.dialog_changebankcard, null);
					dialog2.setContentView(layout2, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					
					final ImageView iv_dialog_changebankcard = (ImageView) layout2.findViewById(R.id.iv_dialog_changebankcard);
					final ImageView iv_dialog_deletebankcard = (ImageView) layout2.findViewById(R.id.iv_dialog_deletebankcard);
					
					iv_dialog_changebankcard.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							Log.d("click", "触摸产生点击事件");
							iv_dialog_changebankcard.setVisibility(View.GONE);
							iv_dialog_deletebankcard.setVisibility(View.VISIBLE);
							
							iv_dialog_deletebankcard.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									dialog2.dismiss();
								}
							});
						};
					});
					dialog2.show();
					dialog2.setCanceledOnTouchOutside(true);
					dialog2.setCancelable(true);
				break;
			case 2:
				//此处表示换卡成功，需将之前存在本地的json置为""
				for(int i = 0;i<8;i++){
					App.app.setData((App.TAGS[i]),"");
				}
				App.app.setData("in_this_fragment_time","");
				
				refresh();
				
				MineBankcardFragment.this.back();
				break;
			}
			
		};
	};
	
	
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	.displayer(new FadeInBitmapDisplayer(300))
	.displayer(new RoundedBitmapDisplayer(10)).build();

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View ll_bank_currentCard, iv_activity_shared,
		fl_comments_right_iv, ll_bank_card_other, ll_bank_card_exp;
		private TextView tv_bank_card_name, tv_bank_card_desc,
		tv_activity_title, tv_bank_card_exp;
		private LinearLayout search_click;
		private RecyclingImageView iv_ban_card_head;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_shared;
	}

	/*@InjectView(pull = true)
	private MyListView lv_ban_card_other;*/
	
	@InjectView
	private BankcardListView lv_ban_card_other;  //这个BankcardListView是无头的ListView
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_bank_card, null);
		
		Handler_Inject.injectFragment(this, rootView);
		
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		return rootView;
	}

	@InjectInit
	private void init() {
		/*ListViewSwipeGesture touchListener = new ListViewSwipeGesture(
				lv_ban_card_other, swipeListener, getActivity());
		
		touchListener.SwipeType = ListViewSwipeGesture.Dismiss;
		lv_ban_card_other.setOnTouchListener(touchListener);*/
		
		
		
		/*
		 *  listView的整体判断自己的Item被点击效果，暂时使用单个Item被点击的效果
		 * 
		 * lv_ban_card_other.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {

				final HashMap<String, String> map = dataList.get(position);
				LogUtils.toDebugLog("", "检测到响应了点击事件");
				
				DialogUtil.createTipAlertDialog(activity, R.string.is_switch_card,
						new TipAlertDialogCallBack() {
					
					private String bankCardId;
	 
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						
						bankCardId = map.get("id");
						InternetConfig config = new InternetConfig();
						config.setKey(2);
						HashMap<String, Object> head = new HashMap<>();
						head.put("Authorization","Bearer " + App.app.getData("access_token"));
						config.setHead(head);
						LinkedHashMap<String, String> params = new LinkedHashMap<>();
						params.put("bankcard_id", bankCardId);
						
						//使用post方式去提交，是选中其中的卡作为选中卡 
					    FastHttpHander.ajax(GlobalValue.URL_SELECT_BANKCARD, params,config, MineBankcardFragment.this);
						
						//mList.clear();
						//this = null;
						
						//Handler_Inject.injectFragment(this, rootView);
						
						dialog.dismiss();
					}
					@Override
					public void onNegativeButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
		});*/
		
		
		lv_ban_card_other.setNoHeader(true);
		/**
		 * 初始化上拉刷新和下拉加载的监听
		 */
		lv_ban_card_other.setOnRefreshListener(new OnRefreshListener() {
			
			
			@Override
			public void onRefreshing() {//屏蔽刷新的操作，手直面下滑时，并不显示头布局
				//DoNothing!
			}
			@Override
			public void onLoadingMore() {//目前只有上拉加载有效
					if (list!=null) {
					if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null"||list.getNext_page_url().equals("")) {
						while(times <1){
							CustomToast.show(activity, "到底啦!", "您添加的银行卡目前只有这么多");
							times++;
						}
						lv_ban_card_other.onLoadMoreOverFished();
					} else {
						refreshCurrentList(list.getNext_page_url(), null, 0,lv_ban_card_other);
						isPull = true;
					}
				}else{
					lv_ban_card_other.onLoadMoreOverFished();
				}
			}
		});
		
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setTextColor(getResources().getColor(R.color.app_green2));

		initTitle(v.tv_activity_title, R.string.xy_card, v.tv_activity_shared,R.string.add);
		Log.i("","getArguments() 的值为：  "+ getArguments());
		
		//TODO
		if (getArguments() != null) {
			boolean isFromHome = getArguments().getBoolean("isFromHome");
			boolean isFromEightPart = getArguments().getBoolean("isFromEightPart");
			boolean isFromNewPart = getArguments().getBoolean("isFromNewPart");
			boolean isFromRigisterFragToMyBankcardFrag = getArguments().getBoolean("isFromRigisterFragToMyBankcardFrag");
			boolean isFromInsertBankcardAdapterPage = getArguments().getBoolean("isFromInsertBankcardAdapterPage");
			Log.d("isFromInsertBankcardAdapterPage的值为：", "isFromInsertBankcardAdapterPage的值为："+isFromInsertBankcardAdapterPage);
			int initType = getArguments().getInt("type");
			
			mIsFromHome = isFromHome;
			mIsFromEightPart = isFromEightPart;
			Log.i("","mIsFromEightPart被赋值后的置为：  "+  mIsFromEightPart );
			mIsFromNewPart = isFromNewPart;
			Log.i("","mIsFromNewPart被赋值后的置为：  "+  mIsFromNewPart );
			
			mInitType = initType;
			mIsFromRigisterFragToMyBankcardFrag = isFromRigisterFragToMyBankcardFrag;
			
			mIsFromInsertBankcardAdapterPage = isFromInsertBankcardAdapterPage;
	    }
		
		if(mIsFromInsertBankcardAdapterPage){//从添加卡页而来，此时本地的主卡数据未发生变化，下面将原始主卡送入选择接口数据中
			InternetConfig config = new InternetConfig();
			config.setKey(4);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization", "Bearer "+ App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			Log.i("MainBankCard的值", "MainBankCard的值：" +App.app.getData("MainBankcard"));
			params.put("bankcard_id", App.app.getData("MainBankcard"));
			Log.d("原始主卡的id为:  ", "原始主卡的id为:  "+App.app.getData("MainBankcard"));
			
			//在这里从搜索银行卡页回来，自己跑过去请求了一下，进行了选卡操作！！！
			FastHttpHander.ajax(GlobalValue.URL_SELECT_BANKCARD,  params, config,this);
			Handler_Inject.injectFragment(MineBankcardFragment.this, null);
			return;
			
		}else{
			refreshUrl = GlobalValue.URL_BANKCARD;
			refreshKey = 0;
			refresh();//刷新其他银行卡的操作		
		    decideHowToShow();
		    showChosenBankcard();
	  }
		
	  //坐等展示出需要的Dialog
		
	}

	public void showChosenBankcard() {
		//获取展示选中的卡
		if (!TextUtils.isEmpty(App.app.getData("exp_secret"))) {//体验用户
			/*refreshCurrentList(GlobalValue.URL_BANKCARD_RECOMMEND, null, 3,null);*/
			refreshCurrentList(GlobalValue.URL_BANKCARD_CHOSEN, null, 3, null);
		} else {//登陆用户
			refreshCurrentList(GlobalValue.URL_BANKCARD_CHOSEN, null, 3, null);
		}
	}

	public void decideHowToShow(){
		if (isExperience()) {//体验用户就只有一张卡并且显示是体验状态,那么下面的显示是那只猫头鹰
			v.ll_bank_card_other.setVisibility(View.GONE);

			v.ll_bank_card_exp.setVisibility(View.VISIBLE);
			View search_click = v.ll_bank_card_exp.findViewById(R.id.search_click);
			search_click.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					RegisterFragment fragment = new RegisterFragment();
//					FragmentEntity entity = new FragmentEntity();
//					Bundle bundle=new Bundle();
//					bundle.putString("fragment_name", MineBankcardFragment.class.getSimpleName());//TODO
//					bundle.putBoolean("isFromEightPart",mIsFromEightPart);
//					bundle.putBoolean("isFromNewPart",mIsFromNewPart);
//					bundle.putBoolean("isFromHome",mIsFromHome);
//					if(mIsFromEightPart){
//						bundle.putInt("type", mInitType);
//					}
					
					//TODO
					RegisterFragment fragment = new RegisterFragment();
					Bundle bundle = new Bundle();
					bundle.putString("fragment_name", MineBankcardFragment.class.getSimpleName());
					//下面的数据直接写到Register页面中
					if(mIsFromHome&&!mIsFromEightPart&&!mIsFromNewPart){//来自HomeFrag
						bundle.putBoolean("isFromHome", mIsFromHome);
					}else if(!mIsFromHome&&mIsFromEightPart&&!mIsFromNewPart){//来自八大板块
						bundle.putBoolean("isFromEightPart", mIsFromEightPart);
						bundle.putInt("type", mInitType);
					}else if(!mIsFromHome&&!mIsFromEightPart&&mIsFromNewPart){//来自New界面
						bundle.putBoolean("isFromNewPart", mIsFromNewPart);
						bundle.putBoolean("IsNew", true);
						bundle.putInt("type", R.string.new_exposure);
					}
					bundle.putBoolean("isFromMyBankcardFragToRigisterFrag", true);
					fragment.setArguments(bundle);
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
			});
			v.tv_bank_card_exp.setText(getString(R.string.experience));
		} else {
			v.ll_bank_card_other.setVisibility(View.VISIBLE);

			v.ll_bank_card_exp.setVisibility(View.GONE);
			v.tv_bank_card_exp.setText(getString(R.string.current));
		}
	}

	private void refresh() {
		//下行代码中的refreshKey为 0
		refreshCurrentList(refreshUrl, null, refreshKey, lv_ban_card_other);//其他银行卡的页面展示
		LogUtils.toDebugLog("refreshKey", "refreshKey值为： "+refreshKey);
		showChosenBankcard();
	}
	
	
//
//	ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {
//		@Override
//		public void FullSwipeListView(int position) {
//		}
//
//		@Override
//		public void HalfSwipeListView(final int position) {// 删除操作
//			HashMap<String, String> map = dataList.get(position);
//			String id = map.get("id");
//
//			dataList.remove(position);//这就是为什么你在点删除后,可以看到立马删除的效果体现出来,
//			//但实际未删除服务器上的数据
//
//			InternetConfig config = new InternetConfig();
//			config.setKey(1);
//			HashMap<String, Object> head = new HashMap<>();
//			head.put("Authorization",
//					"Bearer " + App.app.getData("access_token"));
//			config.setHead(head);
//			config.setRequest_type(InternetConfig.request_delete);
//			config.setMethod("DELETE");
//
//			/*FastHttpHander.ajaxGet(GlobalValue.URL_BANKCARD_DELETE + id, config,
//					MineBankcardFragment.this);*/
//
//			HttpUtils httpUtils = new HttpUtils();
//			RequestParams requestParams = new RequestParams();
//			requestParams.addHeader("Authorization","Bearer " + App.app.getData("access_token"));
//			httpUtils.send(HttpMethod.DELETE, GlobalValue.URL_BANKCARD_DELETE + id, requestParams,new RequestCallBack<String>(){
//				@Override
//				public void onFailure(HttpException arg0, String arg1) {
//					
//				}
//
//				@Override
//				public void onSuccess(ResponseInfo arg0) {
//					if ("true".equals(arg0.result)) {
//						//很明显,前面的删除银行卡操作是没有起到作用的,若有作用至少是会有toast提醒
//						CustomToast.show(activity, getString(R.string.tip),
//								getString(R.string.delete_success));
//					} else {
//						System.out.println("获取到返回值,但显示是删除失败!");
//						CustomToast.show(activity, getString(R.string.tip),
//								getString(R.string.delete_faild));
//					}
//				}
//			});
//			adapter.notifyDataSetChanged();
//		}
//
//		/*
//		 * 打开银行卡列表中的每一个内容
//		 * @see com.qmyo.view.ListViewSwipeGesture.TouchCallbacks#OnClickListView(int)
//		 */
//		@Override
//		public void OnClickListView(int position) {//---------------------------------------------->listView 的点击事件
//			final HashMap<String, String> map = dataList.get(position);
//
//			DialogUtil.createTipAlertDialog(activity, R.string.is_switch_card,
//					new TipAlertDialogCallBack() {
//
//
//				@Override
//				public void onPositiveButtonClick(
//						DialogInterface dialog, int which) {
//					dialog.dismiss();
//					bankCardId = map.get("id");
//					InternetConfig config = new InternetConfig();
//					config.setKey(2);
//					HashMap<String, Object> head = new HashMap<>();
//					head.put("Authorization",
//							"Bearer " + App.app.getData("access_token"));
//					config.setHead(head);
//					LinkedHashMap<String, String> params = new LinkedHashMap<>();
//					params.put("bankcard_id", bankCardId);
//
//
//					//使用post方式去提交，是选中其中的卡作为选中卡 
//					FastHttpHander.ajax(GlobalValue.URL_SELECT_BANKCARD, params,
//							config, MineBankcardFragment.this);
//
//					dataList.clear();
//					adapter = null;
//				}
//
//				@Override
//				public void onNegativeButtonClick(
//						DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			});
//		}
//
//		@Override
//		public void LoadDataForScroll(int count) {
//
//		}
//		@Override
//		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
//		}
//
//	};
	
	
	
	private SwipeListMineBankcardAdapter adapter;
	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>() ;
	private int currentCardId;
	private BankCardList list;

	private boolean isPull = false;

	private boolean isChangeTheChoseCard = false;
	private boolean isChangeTheChoseCardUnderSearchBankcardPage = false;
	private TimerTask timerTask;
	private View rootView;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				
				lv_ban_card_other.onLoadMoreFished();
				
				if(isPull){
					isPull = false;
				}else{
					dataList.clear();
				}
				
				list = Handler_Json.JsonToBean(BankCardList.class,r.getContentAsString());
				if (list.getData() != null) {
					for (BankCardData data : list.getData()) {
						HashMap<String, String> map = new HashMap<>();
						map.put("iv_bank_card_head", data.getBankcard()
								.getPhoto());
						map.put("id", data.getBankcard().getId() + "");
						map.put("tv_bank_card_name", data.getBank().getName());
						map.put("tv_bank_card_desc", data.getBankcard().getName());
						map.put("id", data.getBankcard().getId() + "");
						dataList.add(map);
					}
					if (adapter == null) {
//						adapter = new BankCardAdapter(lv_ban_card_other,
//								dataList, R.layout.activity_bank_card_item);//主要的都在BankCardAdapter中
//						adapter.setActivity(this);
//						adapter.setHasAdd(false);
						
						//替换掉适配器的内容
						/*adapter = new SwipeListMineBankcardAdapter(activity,lv_ban_card_other,dataList,R.layout.activity_bank_card_item_swipe);*/
						adapter = new SwipeListMineBankcardAdapter(activity, dataList);
						adapter.setFromNetCallBack(this);
						lv_ban_card_other.setAdapter(adapter);
						adapter.setFragment(this);
						adapter.setHasAdd(false);
						
					} else {
						adapter.notifyDataSetChanged();
					}
					/*
					 * 当刷新到最后一页，且此页数据少于10条时，需要弹出吐司表示到底，并且将尾布局去除
					 */
					if(list.getData().size()<10){
						if(first_enter==0){
							lv_ban_card_other.onLoadMoreOverFished();
						}
						//TODO
						//CustomToast.show(activity, "到底啦!", "您添加的银行卡目前只有这么多");
					}
					
					if(App.app.getData("firstEnterBankcardAndAddAnotherBankcard").isEmpty()){//证明是第一次
						if(list.getData().size()==1){
							//一旦进来就将其设置为 本地标签赋上值
							App.app.setData("firstEnterBankcardAndAddAnotherBankcard","true");
						    handler.sendEmptyMessage(0);//TODO
				    	}
					}
					this.first_enter = Integer.MAX_VALUE;
//					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
				} else {
					//lv_ban_card_other.setAdapter(null);
					adapter.notifyDataSetChanged();
					lv_ban_card_other.onLoadMoreOverFished();//当初次无数据时，不可将尾禁止掉，因为可能还有下一页的内容
				}
				break;

			case 1:
				if ("true".equals(r.getContentAsString())) {
					//很明显,前面的删除银行卡操作是没有起到作用的,若有作用至少是会有toast提醒
//					CustomToast.show(activity, getString(R.string.tip),
//							getString(R.string.delete_success));
				} else {
					System.out.println("获取到返回值,但显示是删除失败!");
					CustomToast.show(activity, getString(R.string.tip),
							getString(R.string.delete_faild));
				}
				break;
			case 2:// 选中银行卡
				if ("true".equals(r.getContentAsString())) {
					/*CustomToast.show(activity, R.string.tip,
							R.string.select_bank_card_success);*/
					CustomToast.show(activity, R.string.select_bank_card_success,
							R.string.search_bankcard_act);
					
					App.app.setData("MainBankcard", bankCardId);
					
					if(mIsFromInsertBankcardAdapterPage){//从换卡页回来，并且进行了换卡操作
						//刷下页面
						refreshUrl = GlobalValue.URL_BANKCARD;
						refreshKey = 0;
						refresh();
						decideHowToShow();
						showChosenBankcard();
						isChangeTheChoseCardUnderSearchBankcardPage = true;
						
						//从选卡页回来，且更换当前主卡后自动回跳至首页
						Timer timer = new Timer();
						 timerTask = new TimerTask() {
								@Override
								public void run() {
									handler.sendEmptyMessage(2);
								}
							 };
						 timer.schedule(timerTask, 1200);
						return;
					}
					
					
					
					refresh();
					//上面的refresh()操作完成后，表明已经进行了换卡的操作
					 isChangeTheChoseCard  = true;
					 
					 Timer timer = new Timer();
					 timerTask = new TimerTask() {
							@Override
							public void run() {
								handler.sendEmptyMessage(2);
							}
						 };
					 timer.schedule(timerTask, 1200);
				} else {
					CustomToast.show(activity, R.string.tip,R.string.select_bank_card_faild);
				}
				break;
				
			
			case 3:// 获取当前银行卡
				BankCardData data = Handler_Json.JsonToBean(BankCardData.class,r.getContentAsString());
				
				currentCardId = data.getBankcard().getId();
				App.app.setData("MainBankcard", currentCardId+"");//只要进入就将当前的原始主卡ID写入到本地文件中
				Log.d("","存入到本地的MainBankcard为: "+App.app.getData("MainBankcard"));
				
				App.app.setData("MainBankcardForTest", currentCardId+"");
				
				v.ll_bank_currentCard.setVisibility(View.VISIBLE);
				v.tv_bank_card_name.setText(data.getBank().getName());
				v.tv_bank_card_desc.setText(data.getBankcard().getName());
				ImageLoader.getInstance().displayImage(data.getBankcard().getPhoto(), v.iv_ban_card_head,options);
				

				/**
				 * 在此处进行拦截操作！//TODO
				 */
				/*if(mIsFromInsertBankcardAdapterPage){//将原BankcardId重新去访问一下服务器接口，这样就将选中卡返回至原卡状态
					InternetConfig config = new InternetConfig();
					config.setKey(0);
					HashMap<String, Object> head = new HashMap<>();
					head.put("Authorization", "Bearer "
							+ App.app.getData("access_token"));
					config.setHead(head);
					LinkedHashMap<String, String> params = new LinkedHashMap<>();
					params.put("bankcard_id", currentCardId+"");
					FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD,  params, config,MineBankcardFragment.this);
					Handler_Inject.injectFragment(MineBankcardFragment.this, null);
					
					refreshUrl = GlobalValue.URL_BANKCARD;
					refreshKey = 0;
					refresh();//刷新其他银行卡的操作
				}*/
				
				
				break;
			case 4://从银行卡搜索页并且完成添卡操作后，需要将主卡id恢复到本地
				if ("true".equals(r.getContentAsString())) {
					/*CustomToast.show(activity, R.string.tip,
							R.string.select_bank_card_success);*/
					
					refreshUrl = GlobalValue.URL_BANKCARD;
					refreshKey = 0;
					refresh();
					decideHowToShow();
					showChosenBankcard();
					
					
					/*App.app.setData("MainBankcard", bankCardId);*/
					/*if(mIsFromInsertBankcardAdapterPage){
						//刷下页面
						refreshUrl = GlobalValue.URL_BANKCARD;
						refreshKey = 0;
						refresh();
						decideHowToShow();
						showChosenBankcard();
						isChangeTheChoseCardUnderSearchBankcardPage = true;
						return;
					}
					refresh();
					//上面的refresh()操作完成后，表明已经进行了换卡的操作
					 isChangeTheChoseCard  = true;*/
				} else {
					CustomToast.show(activity, R.string.tip,R.string.select_bank_card_faild);
				}
			   break;
			   
			case 6:// 选中银行卡 //实则拷贝的是case 2 的处理操作
				if ("true".equals(r.getContentAsString())) {
					/*CustomToast.show(activity, R.string.tip,
							R.string.select_bank_card_success);*/
					CustomToast.show(activity, R.string.select_bank_card_success,
							R.string.search_bankcard_act);
					
					App.app.setData("MainBankcard", bankCardId);
					
					if(mIsFromInsertBankcardAdapterPage){//从换卡页回来，并且进行了换卡操作
						//刷下页面
						refreshUrl = GlobalValue.URL_BANKCARD;
						refreshKey = 0;
						refresh();
						decideHowToShow();
						showChosenBankcard();
						isChangeTheChoseCardUnderSearchBankcardPage = true;
						
						//从选卡页回来，且更换当前主卡后自动回跳至首页
						Timer timer = new Timer();
						 timerTask = new TimerTask() {
								@Override
								public void run() {
									handler.sendEmptyMessage(2);
								}
							 };
						 timer.schedule(timerTask, 1200);
						return;
					}
					
					
					adapter = null; //保证了其他卡页的列表刷新效果
					refresh();
					//上面的refresh()操作完成后，表明已经进行了换卡的操作
					 isChangeTheChoseCard  = true;
					 
					 Timer timer = new Timer();
					 timerTask = new TimerTask() {
							@Override
							public void run() {
								handler.sendEmptyMessage(2);
							}
						 };
					 timer.schedule(timerTask, 1200);
				} else {
					CustomToast.show(activity, R.string.tip,R.string.select_bank_card_faild);
				}
				break;
			
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}

//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_activity_shared:// 添加
			//			if(App.app.getData("isExperience").equals("false")){
			if(App.app.getData("isExperience").equals("true")){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.registeror_login, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						
						RegisterFragment fragment = new RegisterFragment();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", MineBankcardFragment.class.getSimpleName());
						//下面的数据直接写到Register页面中
						if(mIsFromHome&&!mIsFromEightPart&&!mIsFromNewPart){//来自HomeFrag
							bundle.putBoolean("isFromHome", mIsFromHome);
						}else if(!mIsFromHome&&mIsFromEightPart&&!mIsFromNewPart){//来自八大板块
							bundle.putBoolean("isFromEightPart", mIsFromEightPart);
							bundle.putInt("type", mInitType);
						}else if(!mIsFromHome&&!mIsFromEightPart&&mIsFromNewPart){//来自New界面
							bundle.putBoolean("isFromNewPart", mIsFromNewPart);
							bundle.putBoolean("IsNew", true);
							bundle.putInt("type", R.string.new_exposure);
						}
						bundle.putBoolean("isFromMyBankcardFragToRigisterFrag", true);
						fragment.setArguments(bundle);
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);

					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

			}else{//登录状态下需要将标签写到本地中，供后续使用
				SearchBankCardFragment fragment = new SearchBankCardFragment();
				FragmentEntity event = new FragmentEntity();
				
				/*boolean isFromHome = getArguments().getBoolean("isFromHome");
				boolean isFromEightPart = getArguments().getBoolean("isFromEightPart");
				boolean isFromNewPart = getArguments().getBoolean("isFromNewPart");
				mIsFromHome = isFromHome;
				mIsFromEightPart = isFromEightPart;
				mIsFromNewPart = isFromNewPart;
				mInitType = initType;
				mIsFromRigisterFragToMyBankcardFrag = isFromRigisterFragToMyBankcardFrag;
				mIsFromInsertBankcardAdapterPage = isFromInsertBankcardAdapterPage;
				*/
				
				//来自 home页
				if(mIsFromHome){
					App.app.setData("isFromHome", "true");
					App.app.setData("isFromNewPart", "");
					App.app.setData("isFromEightPart", "");
				}
				//来自 New页
				if(mIsFromNewPart){
					App.app.setData("isFromHome", "");
					App.app.setData("isFromNewPart", "true");
					App.app.setData("isFromEightPart", "");
				}
				//来自 8大板块页
				if(mIsFromEightPart){
					App.app.setData("isFromHome", "");
					App.app.setData("isFromNewPart", "");
					App.app.setData("isFromEightPart", "true");
					App.app.setData("type", mInitType+"");
				}
				if(mIsFromRigisterFragToMyBankcardFrag){//标记当前的银行卡页是刚刚登录页返回回来的
					App.app.setData("isFromRigisterFragToMyBankcardFrag", "true");
				}
				/*Bundle bundle = new Bundle();
				bundle.putInt("currentBankcardId", currentCardId);*/
				App.app.setData("currentBankcardId", currentCardId+"");
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
			break;
			
		case R.id.ll_bank_currentCard:// 当前行用卡详情
			BankCardDetailFragment cardDetailFragment = new BankCardDetailFragment();
			Bundle args = new Bundle();
			args.putString("id", currentCardId + "");
			cardDetailFragment.setArguments(args);
			cardDetailFragment.show(getFragmentManager(), "card");
			break;
		}
	}

//	@InjectPullRefresh
//	private void call(int type) {
//		// 这里的type来判断是否是下拉还是上拉
//		switch (type) {
//			case InjectView.PULL:
//				if (list != null) {
//					
//					if (TextUtils.isEmpty(list.getNext_page_url())||list.getNext_page_url()=="null") {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();
//						CustomToast.show(activity, "到底啦!", "您添加的银行卡目前只有这么多");
//					} else {
//						refreshCurrentList(list.getNext_page_url(), null, 0,lv_ban_card_other);
//						isPull = true;
//					}
//				}
//				break;
//		}
//	}

	@Override
	public void onPause() {
		/*	dataList.clear();
		adapter = null;*/
		super.onPause();
	}
	
	@Override/*@InjectMethod(@InjectListener(ids = R.id.iv_activity_back, listeners = OnClick.class))*/
	protected void back() {
		
		Log.d("bankcard","isChangeTheChoseCard  :"+isChangeTheChoseCard );
		Log.d("bankcard","isChangeTheChoseCardUnderSearchBankcardPage  :"+isChangeTheChoseCardUnderSearchBankcardPage );
		
		Log.d("bankcard","mIsFromHome  :"+mIsFromHome );
		Log.d("bankcard","mIsFromEightPart  :"+mIsFromEightPart );
		Log.d("bankcard","mIsFromNewPart  :"+mIsFromNewPart );
		
		Log.d("bankcard","mIsFromInsertBankcardAdapterPage  :"+ mIsFromInsertBankcardAdapterPage );
		Log.d("bankcard","mIsFromRigisterFragToMyBankcardFrag  :"+ mIsFromRigisterFragToMyBankcardFrag );
		
	
		/**
		 * 注意： isChangeTheChoseCard：  未进入银行卡的搜索页，仅在现有卡池中，进行了替换当前卡的操作
		 *     isChangeTheChoseCardUnderSearchBankcardPage： 从搜索银行卡页返回回来，且在全新的 卡池中进行了替换当前卡的操作（同上面完全是两种情况，无 先后串联关系）
		 *     
		 *     mIsFromInsertBankcardAdapterPage： 在搜索银行卡页进行了添卡的行为操作
		 */
		
		
		
		if(isChangeTheChoseCard&&!isChangeTheChoseCardUnderSearchBankcardPage){ //在当前页进行了替换选中银行卡的操作后，需要对应的进行返回至上一页
			
			Log.i("","isChangeTheChoseCard被赋值后的置为：  "+  isChangeTheChoseCard );
			/*
			 * 下面的三步判断都是在换卡成功后进行的操作
			 */
			if(mIsFromHome){
				/*CustomToast.show(activity, "准备跳转至首页刷新", "来自我的银行卡页面");*/
				
				Intent intent=new Intent("com.lansun.qmyo.refreshHome");
				getActivity().sendBroadcast(intent);
				System.out.println("从首页直接进到我的银行卡页，发送广播！");
				super.back();
				/*HomeFragment homeFragment = new HomeFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(homeFragment);
				EventBus.getDefault().post(fEntity);*/
				return;
			}
			
			
			if(mIsFromEightPart){
				/*CustomToast.show(activity, "准备跳转至八大板块页刷新", "来自我的银行卡页面");*/
				ActivityFragment activityFragment = new ActivityFragment();
				Bundle args = new Bundle();
				args.putInt("type", mInitType);//将这个type参数放入到返回回去的八大板块页
				args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
				activityFragment.setArguments(args);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(activityFragment);
				EventBus.getDefault().post(fEntity);
				
				Intent intent=new Intent("com.lansun.qmyo.refreshHome");
				getActivity().sendBroadcast(intent);
				System.out.println("从八大板块页跳往我的银行卡页，且换卡后，需发送广播！");
				return;
			}
			/**
			 * 新品曝光单独开启了一个页面，所以不需要传递一些参数
			 */
			if(mIsFromNewPart){  
				
				/*CustomToast.show(activity, "准备跳转至新品曝光刷新", "来自我的银行卡页面");*/
				Fragment activityFragment = new NewBrandFragment();
//				Bundle args = new Bundle();
//				args.putBoolean("IsNew", true);
//				args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
//				args.putInt("type", R.string.new_exposure);
//				activityFragment.setArguments(args);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(activityFragment);
				EventBus.getDefault().post(fEntity);
				
				Intent intent=new Intent("com.lansun.qmyo.refreshHome");
				getActivity().sendBroadcast(intent);
				System.out.println("从新品曝光页跳往我的银行卡页，且换卡后，需发送广播！");
				return;
			}
			
			//从添加搜索银行卡页回来，并且 在  我的银行卡页  进行了换卡的操作,即表明肯定进行了换卡的操作
			if(mIsFromInsertBankcardAdapterPage){  
				if(App.app.getData("isFromHome")=="true"&&App.app.getData("isFromNewPart")==""&&App.app.getData("isFromEightPart")==""){
					/*HomeFragment homeFragment = new HomeFragment();*/
					MainFragment homeFragment = new MainFragment(0);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(homeFragment);
					EventBus.getDefault().post(fEntity);
					return;
					
				}
				//来自 New页
				if(App.app.getData("isFromHome")==""&&App.app.getData("isFromNewPart")=="true"&&App.app.getData("isFromEightPart")==""){
//					ActivityFragment activityFragment = new ActivityFragment();
//					Bundle args = new Bundle();
//					args.putBoolean("IsNew", true);
//					args.putInt("type", R.string.new_exposure);
//					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
//					activityFragment.setArguments(args);
//					FragmentEntity fEntity = new FragmentEntity();
//					fEntity.setFragment(activityFragment);
//					EventBus.getDefault().post(fEntity);
					Fragment fragment = new NewBrandFragment();
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(fragment);
					EventBus.getDefault().post(fEntity);
					
					Intent intent=new Intent("com.lansun.qmyo.refreshHome");
					getActivity().sendBroadcast(intent);
					System.out.println("从新品曝光页跳往我的银行卡页，再跳往搜索银行卡页，且添加卡入卡池后，还在我的银行卡页换卡后，需发送广播！");
					
					return;
				}
				//来自 8大板块页
				if(App.app.getData("isFromHome")==""&&App.app.getData("isFromNewPart")==""&&App.app.getData("isFromEightPart")=="true"){
					ActivityFragment activityFragment = new ActivityFragment();
					Bundle args = new Bundle();
					args.putInt("type", Integer.valueOf(App.app.getData("type"))); //将这个type参数放入到返回回去的八大板块页
					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
					activityFragment.setArguments(args);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(activityFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
			}
			
			super.back();
			return;
			
			
		}else if(!isChangeTheChoseCard&&mIsFromRigisterFragToMyBankcardFrag){//虽然没有进行换卡的操作，但是是从注册页返回回来的，故而仍需进行刷新返回页面的操作
			
			if(!isChangeTheChoseCard&&mIsFromRigisterFragToMyBankcardFrag&&mIsFromInsertBankcardAdapterPage){

				/*if(App.app.getData("isFromHome")=="true"&&App.app.getData("isFromNewPart")==""&&App.app.getData("isFromEightPart")==""){*/
				if(App.app.getData("isFromHome").equals("true")&&App.app.getData("isFromNewPart").equals("")&&App.app.getData("isFromEightPart").equals("")){
					/*HomeFragment homeFragment = new HomeFragment();*/
					MainFragment mainFragment = new MainFragment(0);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(mainFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				//来自 New页
				/*if(App.app.getData("isFromHome")==""&&App.app.getData("isFromNewPart")=="true"&&App.app.getData("isFromEightPart")==""){*/
				if(App.app.getData("isFromHome").equals("")&&App.app.getData("isFromNewPart").equals("true")&&App.app.getData("isFromEightPart").equals("")){
					ActivityFragment activityFragment = new ActivityFragment();
					Bundle args = new Bundle();
					args.putBoolean("IsNew", true);
					args.putInt("type", R.string.new_exposure);
					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true); 
					activityFragment.setArguments(args);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(activityFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				//来自 8大板块页
				if(App.app.getData("isFromHome").equals("")&&App.app.getData("isFromNewPart").equals("")&&App.app.getData("isFromEightPart").equals("true")){
					ActivityFragment activityFragment = new ActivityFragment();
					Bundle args = new Bundle();
					args.putInt("type", Integer.valueOf(App.app.getData("type"))); //将这个type参数放入到返回回去的八大板块页
					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
					activityFragment.setArguments(args);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(activityFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				super.back();
				
				/*
				Log.d("morenfangfa", "走到默认返回方法");
				super.back();
				return;*/
				
			}else {  //上面的判断条件!isChangeTheChoseCard&&mIsFromRigisterFragToMyBankcardFrag&&mIsFromInsertBankcardAdapterPage
				
				if(mIsFromHome){//从登录页返回回来，回到我的银行卡页，再按返回键时，重新刷新之前的Home页内容
					/*HomeFragment homeFragment = new HomeFragment();*/
					MainFragment mainFragment = new MainFragment(0);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(mainFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				
				if(mIsFromEightPart){//从登录页返回回来，回到我的银行卡页，再按返回键时，重新刷新之前的八大板块页内容
					Intent intent=new Intent("com.lansun.qmyo.refreshHome");
					getActivity().sendBroadcast(intent);
					System.out.println("从八大板块页跳往我的银行卡页，再跳往搜索银行卡页，且添加卡入卡池后，还在我的银行卡页换卡后，需发送广播！");
					
					ActivityFragment activityFragment = new ActivityFragment();
					Bundle args = new Bundle();
					args.putInt("type", mInitType);//将这个type参数放入到返回回去的八大板块页
					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
					activityFragment.setArguments(args);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(activityFragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				
				if(mIsFromNewPart){//从登录页返回回来(包括了正常进入登录注册页，但未进行登录操作，只是简单的进去了，又退出去了)，回到我的银行卡页，再按返回键时，重新刷新之前的新品曝光板块页内容
//					ActivityFragment activityFragment = new ActivityFragment();
//					Bundle args = new Bundle();
//					args.putBoolean("IsNew", true);
//					args.putInt("type", R.string.new_exposure);
//					args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
//					activityFragment.setArguments(args);
//					FragmentEntity fEntity = new FragmentEntity();
//					fEntity.setFragment(activityFragment);
//					EventBus.getDefault().post(fEntity);
					
					Intent intent=new Intent("com.lansun.qmyo.refreshHome");
					getActivity().sendBroadcast(intent);
					System.out.println("从新品曝光页跳往我的银行卡页，再跳往搜索银行卡页，且添加卡入卡池后，还在我的银行卡页换卡后，需发送广播！");
					
					
					Fragment fragment = new NewBrandFragment();
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(fragment);
					EventBus.getDefault().post(fEntity);
					return;
				}
				super.back();//若不做任何操作，那么需要将返回按钮执行正常的返回上一级的操作
			}
			
			
			
			if(mIsFromEightPart){//从登录页返回回来，回到我的银行卡页，再按返回键时，重新刷新之前的八大板块页内容
				ActivityFragment activityFragment = new ActivityFragment();
				Bundle args = new Bundle();
				args.putInt("type", mInitType);//将这个type参数放入到返回回去的八大板块页
				activityFragment.setArguments(args);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(activityFragment);
				EventBus.getDefault().post(fEntity);
			}
			if(mIsFromNewPart){//从登录页返回回来，回到我的银行卡页，再按返回键时，重新刷新之前的新品曝光板块页内容
				Fragment fragment = new NewBrandFragment();
//				Bundle args = new Bundle();
//				args.putBoolean("IsNew", true);
//				args.putInt("type", R.string.new_exposure);
//				fragment.setArguments(args);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(fragment);
				EventBus.getDefault().post(fEntity);
			}
		    return;	
			
		//如果是从添卡搜索页回来，并且在当前页进行换卡操作
		}else if(mIsFromInsertBankcardAdapterPage&&isChangeTheChoseCardUnderSearchBankcardPage){
			
			Log.d("isisis", "home:  "+App.app.getData("isFromHome")+ "   new:  "+ App.app.getData("isFromNewPart")+ "   eight:  "+App.app.getData("isFromEightPart"));
			
			/*if(App.app.getData("isFromHome")=="true"&&App.app.getData("isFromNewPart")==""&&App.app.getData("isFromEightPart")==""){*/
			if(App.app.getData("isFromHome").equals("true")&&App.app.getData("isFromNewPart").equals("")&&App.app.getData("isFromEightPart").equals("")){
				/*HomeFragment homeFragment = new HomeFragment();*/
				MainFragment mainFragment = new MainFragment(0);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(mainFragment);
				EventBus.getDefault().post(fEntity);
				return;
				
			}
			//来自 New页
			if(App.app.getData("isFromHome").equals("")&&App.app.getData("isFromNewPart").equals("true")&&App.app.getData("isFromEightPart").equals("")){
				
			/*if(App.app.getData("isFromHome")==""&&App.app.getData("isFromNewPart")=="true"&&App.app.getData("isFromEightPart")==""){*/
//			if(App.app.getData("isFromHome").equals("")&&App.app.getData("isFromNewPart").equals("true")&&App.app.getData("isFromEightPart").equals("")){
//				ActivityFragment activityFragment = new ActivityFragment();
//				Bundle args = new Bundle();
//				args.putBoolean("IsNew", true);
//				args.putInt("type", R.string.new_exposure);
//				args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
//				activityFragment.setArguments(args);
				Fragment fragment = new NewBrandFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(fragment);
				EventBus.getDefault().post(fEntity);
				
				
				/**
				 * 实际这个地方不应该出现广播事件，根据判断逻辑，此处在两处皆进行换卡操作时，是不会走到此处的判断的
				 * 但出于功能性保障考虑，此处补加首页进行列表刷新广播推送的操作
				 */
				Intent intent=new Intent("com.lansun.qmyo.refreshHome");
				getActivity().sendBroadcast(intent);
				//System.out.println("从新品曝光页跳往我的银行卡页,需发送广播！");
			  System.out.println("从新品曝光页跳往我的银行卡页，再跳往搜索银行卡页，且添加卡入卡池后，还在我的银行卡页换卡后，需发送广播！");
				
//				ActivityFragment activityFragment = new ActivityFragment();
//				Bundle args = new Bundle();
//				args.putBoolean("IsNew", true);
//				args.putInt("type", R.string.new_exposure);
//				args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
//				activityFragment.setArguments(args);
//				FragmentEntity fEntity = new FragmentEntity();
//				fEntity.setFragment(activityFragment);
//				EventBus.getDefault().post(fEntity);
				return;
			}
			//来自 8大板块页
			if(App.app.getData("isFromHome").equals("")&&App.app.getData("isFromNewPart").equals("")&&App.app.getData("isFromEightPart").equals("true")){
				ActivityFragment activityFragment = new ActivityFragment();
				Bundle args = new Bundle();
				args.putInt("type", Integer.valueOf(App.app.getData("type"))); //将这个type参数放入到返回回去的八大板块页
				args.putBoolean("isHasChangeTheBankcardInMineBankcardPage", true);
				activityFragment.setArguments(args);
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(activityFragment);
				EventBus.getDefault().post(fEntity);
				
				Intent intent=new Intent("com.lansun.qmyo.refreshHome");
				getActivity().sendBroadcast(intent);
				//System.out.println("从新品曝光页跳往我的银行卡页,需发送广播！");
			  System.out.println("从八大板块跳往我的银行卡页，再跳往搜索银行卡页，且添加卡入卡池后，还在我的银行卡页换卡后，需发送广播！");
				return;
			}
			
			Log.d("morenfangfa", "走到默认返回方法");
			super.back();
			return;
		}
		super.back();
		
	}

	
	
	@Override
	public void fromNetCallBck() {
		
	}
	@Override
	public void fromNetCallBck(ResponseEntity r) {
		this.result(r);
	}

	@Override
	public boolean onBackPressed() {
		back();
		return true;
	}
}
