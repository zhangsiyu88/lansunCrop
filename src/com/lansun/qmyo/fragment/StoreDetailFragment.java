package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.amap.api.location.core.e;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.HorizontalListView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.ActivityDetailPagerAdapter;
import com.lansun.qmyo.adapter.DetailHeaderAdapter;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.adapter.TipAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Activity;
import com.lansun.qmyo.domain.ActivityContent;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.CouponsData;
import com.lansun.qmyo.domain.Shop;
import com.lansun.qmyo.domain.ShopActivity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityFragment.ActivityRefreshBroadCastReceiver;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.MySubListView;
import com.lansun.qmyo.view.SharedDialog;
import com.lansun.qmyo.view.TelDialog;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;

/**
 * 门店详情
 * 
 * @author bhxx
 * 
 */
public class StoreDetailFragment extends BaseFragment {

	@InjectAll
	Views v;
	private DetailHeaderAdapter headAdapter;
	private String shopId;
	private Shop shop;
	private String shopAddress;
	private String shopName;
	private String shopTelephone;
	private ShopActivity shopActivity;
	private int tempShopAttention;
	private Fragment fragment;
	private Bundle args;
	private String mRefreshTip = "";
	private boolean isRefreshTheActivityDetailsFrag = false;
	private IntentFilter filter;
	private StoreDetailsFragRefreshBroadCastReceiver broadCastReceiver;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_back, iv_shared, iv_store_detail_gz_store,
				sc_store_detail, rl_url, rl_store_playday,
				rl_store_business_hours;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_store_details_store_num, tv_store_shop_name,
				iv_store_detail_map, tv_store_fans_num,
				tv_store_details_telephone, tv_store_details_address,
				tv_report_content, tv_store_detail_url,
				tv_store_detail_playday, tv_store_detail_business_hours;
		@InjectBinder(listeners = { OnItemClick.class }, method = "fendianClick")
		private MySubListView lv_activity_detail_fendian;
		private RatingBar rb_store_details;
		private LinearLayout ll_blank_gray;
		
		
		@InjectBinder(listeners = { OnItemClick.class }, method = "headItemClick")
		private HorizontalListView lv_store_detail_head;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_store_details, null);
		Handler_Inject.injectFragment(this, rootView);
		
		
		broadCastReceiver = new StoreDetailsFragRefreshBroadCastReceiver();
		System.out.println("StoreDetailFragment 注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.refreshTheIcon");
		filter.addAction("com.lansun.qmyo.refreshStoreDetailPageAndClick");
		filter.addAction("com.lansun.qmyo.refreshStoreDetailPage");
		getActivity().registerReceiver(broadCastReceiver, filter);
		return rootView;
	}
	
	
/** 暂时并不重写 back的返回操作，依旧使用广播机制前去部分更新icon
 * 	@Override
	@InjectMethod(@InjectListener(ids = 2131296342, listeners = OnClick.class))
	protected void back() {
		getFragmentManager().popBackStack();
		
		if(isRefreshTheActivityDetailsFrag){
			ActivityDetailFragment activtiyDetailFragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("shopId", "5467");
			args.putString("activityId", "105408");
			FragmentEntity fEntity = new FragmentEntity();
			fEntity.setFragment(activtiyDetailFragment);
			EventBus.getDefault().post(fEntity);
		}
	}*/
	
	
	

	/**
	 * 按钮的点击事件
	 * @param view
	 */
	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_report_content:// 门店投诉
			if(App.app.getData("isExperience").equals("true")){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.logintocomplain, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						App.app.setData("shopId", shopId);
						App.app.setData("isReportInStoreDetailPage","true");
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", StoreDetailFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else{
				fragment = new ReportFragment();
				args = new Bundle();
				args.putString("shop_id", shopId + "");
				args.putInt("type", 1);
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
			
			break;
		case R.id.iv_shared://分享
			SharedDialog dialog = new SharedDialog();
			dialog.showPopwindow(v.sc_store_detail, activity, shopName,
					shopAddress, shop.getPhotos().get(0));
			break;
		case R.id.iv_store_detail_gz_store://关注门店
			clickToCollectStore();

			break;

		case R.id.iv_store_detail_map://地图
			fragment = new MapFragment();
			args = new Bundle();
			args.putString("shopname", shopName);
			args.putString("shopaddress", shopAddress);
			fragment.setArguments(args);
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			break;
		case R.id.tv_store_details_telephone://打电话
			if (!TextUtils.isEmpty(shopTelephone)) {
				TelDialog telDialog = new TelDialog();
				Bundle args1 = new Bundle();
				args1.putString("phone", shopTelephone);
				telDialog.setArguments(args1);
				telDialog.show(getFragmentManager(), "tel");
			}
			break;
		}
	}


	@InjectInit
	private void init() {
		if (getArguments() != null) {
			shopId = getArguments().getString("shopId");
			mRefreshTip = getArguments().getString("refreshTip");
		}
		v.tv_report_content.setText(R.string.store_report);
		refreshUrl = GlobalValue.URL_SHOP + shopId;
		
		//一进来后我们就去访问并去刷新数据
		refreshCurrentList(refreshUrl, null, 0, v.sc_store_detail);
		
		//key为 1 的服务器提交请求，这其实也只是借用去refreshCurrentList，去完成FastHttpHander的任务
		//refreshCurrentList(String.format(GlobalValue.URL_SHOP_ACTIVITYS, shopId), null, 1,null);
		
		
		/*
		 * 当网络不好，界面上仍然是progress_container时，可通过点击去再次刷新去获取当前的list列表
		 */
		progress_container.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				refreshCurrentList(refreshUrl, null, 0, v.sc_store_detail);
			}
		});

		v.tv_report_content.setText(getString(R.string.store_report));//门店不靠谱，我要投诉！
		
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			
			endProgress();
			
			switch (r.getKey()) {
			case 0:// 门店信息
				shop = Handler_Json.JsonToBean(Shop.class,r.getContentAsString());
				shopName = shop.getName();
				shopAddress = shop.getAddress();
				v.tv_store_shop_name.setText(shop.getName());
				
				
				
				shopTelephone = shop.getTelephone();
				/*v.tv_store_details_telephone.setText(Html.fromHtml(String
						.format(getString(R.string.telephone),
								shop.getTelephone())));*/
				if(shopTelephone==""||shopTelephone.isEmpty()||shopTelephone.equals(null)){
					v.tv_store_details_telephone.setText(Html.fromHtml(String
							.format(getString(R.string.telephone_nothave_now), "暂无")));
				}else{
					v.tv_store_details_telephone.setText(Html.fromHtml(String
							.format(getString(R.string.telephone), shopTelephone)));
				}
				
				
				
				v.tv_store_details_address.setText(Html.fromHtml(String.format(
						getString(R.string.tele_address), shop.getAddress())));
				
				/*v.rb_store_details.setProgress(shop.getEvaluate());//星级*/		
				  v.rb_store_details.setProgress(shop.getEvaluate()*2);
				
				headAdapter = new DetailHeaderAdapter(activity,shop.getPhotos());
				
				
			
				//由于接口返回的值将空，错置为 字符串 “null”，故暂时将下面的代码注掉，临时使用替代代码
		/*		if (!TextUtils.isEmpty(shop.getUrl())||shop.getUrl()!= "null") {
					v.rl_url.setVisibility(View.VISIBLE);
					v.tv_store_detail_url.setText(String.format(
							getString(R.string.url), shop.getUrl()));
				}
				if (!TextUtils.isEmpty(shop.getBusiness_hours())) {
					v.rl_store_business_hours.setVisibility(View.VISIBLE);
					v.tv_store_detail_business_hours.setText(String.format(
							getString(R.string.business_hours),
							shop.getBusiness_hours()));
				}
				if (!TextUtils.isEmpty(shop.getPlayday())) {
					v.rl_store_playday.setVisibility(View.VISIBLE);
					v.tv_store_detail_playday.setText(String.format(
							getString(R.string.playday), shop.getPlayday()));
				}*/
				
				
				/*//-->针对服务器返回异常，而暂时打开的临时代码
				if (shop.getUrl()== null) {
					 	//v.rl_url.setVisibility(View.GONE);
						v.rl_url.setVisibility(View.VISIBLE);//由此可见原本是 gone掉的
						v.tv_store_detail_url.setText(String.format(
								getString(R.string.url), shop.getUrl()));
					}*/
				
				 if (shop.getUrl()== "null"||TextUtils.isEmpty(shop.getUrl())||shop.getUrl()==null) {
					 Log.i("看这里","shop.getUrl()== 字符串 null");
					 	v.rl_url.setVisibility(View.GONE);
						/*v.rl_url.setVisibility(View.VISIBLE);//由此可见原本是 gone掉的
						v.tv_store_detail_url.setText(String.format(
								getString(R.string.url), shop.getUrl()));*/
					}
				 
					if (shop.getBusiness_hours() =="null"||TextUtils.isEmpty(shop.getBusiness_hours())||shop.getBusiness_hours()==null){
						v.rl_store_business_hours.setVisibility(View.GONE);
						/*v.rl_store_business_hours.setVisibility(View.VISIBLE);
						v.tv_store_detail_business_hours.setText(String.format(
								getString(R.string.business_hours),
								shop.getBusiness_hours()));*/
					}
					if (shop.getPlayday()=="null"||TextUtils.isEmpty(shop.getPlayday())||shop.getPlayday()==null) {
						v.rl_store_playday.setVisibility(View.GONE);
						/*v.rl_store_playday.setVisibility(View.VISIBLE);
						v.tv_store_detail_playday.setText(String.format(
								getString(R.string.playday), shop.getPlayday()));*/
					}
				
				
				if (shop.isMy_attention()) {
					v.iv_store_detail_gz_store.setPressed(true);
				}
				if (shop.getPhotos()==null){
					v.lv_store_detail_head.setVisibility(View.GONE);
				}
				v.tv_store_fans_num.setText(shop.getAttention() + "");
				tempShopAttention = shop.getAttention();
				
				v.lv_store_detail_head.setAdapter(headAdapter);
				
				refreshCurrentList(String.format(GlobalValue.URL_SHOP_ACTIVITYS, shopId), null, 1,null);

				break;
			case 1: // 活动列表
				shopActivity = Handler_Json.JsonToBean(ShopActivity.class,
						r.getContentAsString());
				
				ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
				
				for (Activity data : shopActivity.getData()) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("tv_search_activity_name", shopName);
					map.put("tv_search_activity_desc", data.getName());//实际接口返回数据格式为Name值，应该是 desc位置的内容
					map.put("iv_search_activity_head", data.getPhoto());
					map.put("tv_search_tag", data.getTag());
					map.put("icons", data.getCategory());
					
					//少了一个对活动的描述内容   map.put("", data.getContent());
					dataList.add(map);
				}
				if(shopActivity.getData().size()==1){
					v.ll_blank_gray.setVisibility(View.VISIBLE);
				}
				//dataList中存储了分店的信息
				v.lv_activity_detail_fendian.setAdapter(new SearchAdapter(v.lv_activity_detail_fendian, dataList,R.layout.activity_search_item));
				
				
				//当分店列表信息展示开来时，且从登陆页回来，需要模拟点击后的操作
				if(mRefreshTip.equals("true")){
					//执行更新界面的操作
					clickToCollectStore();

					//自己给自己发送一个广播
					Intent intent=new Intent("com.lansun.qmyo.refreshTheIcon");
					getActivity().sendBroadcast(intent);
					System.out.println("从门店详情页过来，发送广播！");
				}
				
				
				
				
				break;
			case 2: // 关注门店
				//但获取网络反馈后，要弹出提示关注成功
				if ("true".equals(r.getContentAsString())){
					CustomToast.show(getActivity(),
									getActivity().getString(R.string.tip1),
									getActivity().getString(R.string.gz_sucess_content1));
					
					/*由于shop是首次进来据获取到的数据，删除后确实对服务器端的数据进行了修改，但是如果再次点击进行关注时，我们发现我们并没有拿到全新的服务器端的数据，
					 * 仅仅是将在生成页面时拿到的没未变的数据进行加 1，实则多加了一遍
					 *
					 * v.tv_store_fans_num.setText(shop.getAttention()+1 + "");
					 */
					v.tv_store_fans_num.setText((tempShopAttention+1)+ "");//因为只有当你取消了关注后，再次点击就会恢复成原先关注状态，实则效果与刚进入时拿到的数据无异，即不要 +1
					tempShopAttention = tempShopAttention +1;
					
					shop.setMy_attention(true);
					
					//关注成功后，将背景图设置为点击后的状态
					v.iv_store_detail_gz_store.setPressed(true);
					v.iv_store_detail_gz_store.setBackgroundResource(R.drawable.gz_store_press1);
				} else {
					CustomToast.show(activity,activity.getString(R.string.tip),
							activity.getString(R.string.gz_faild_content));
				}
				break;
				
				
			/*case 3: // 取消关注门店
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(
							getActivity(),
							getActivity().getString(R.string.tip),
							getActivity().getString(
									R.string.gz_qx_sucess_content));
					v.tv_store_fans_num.setText(shop.getAttention() - 1 + "");
				
					v.tv_store_fans_num.setText((tempShopAttention-1) + "");
					tempShopAttention =  tempShopAttention-1;
					
					
					shop.setMy_attention(false);
					v.iv_store_detail_gz_store.setPressed(false);
				} else {
					CustomToast.show(activity,
							activity.getString(R.string.tip),
							activity.getString(R.string.gz_qx_faild_content));
				}
				break;*/
				
			case 5://去获取前一页的活动的页面是否为我当前卡的收藏活动
				
				ActivityListData data = Handler_Json.JsonToBean(ActivityListData.class,
						r.getContentAsString());
				System.out.println("拿到前一页活动详情内容了，现在判断是否为我关注的状态： "+ data.getActivity().isMy_attention()+"");
				
				if (data.getActivity().isMy_attention()) {//如果前一页的活动的确是我曾经关注的，需要发送广播，让活动详情页进行   活动已被收藏icon  的刷新
					
					Intent intent1 =new Intent("com.lansun.qmyo.refreshTheActivityDetailsFrag");
					getActivity().sendBroadcast(intent1);
					System.out.println("门店详情页  发送广播给  活动详情页！");
					
					/*v.iv_activity_collection.setPressed(true);
					v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_green);*/
				}

			
				break;
			}
			
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}
	}

	/**
	 * 头部的纵向ListView的点击事件
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	private void headItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		DetailHeaderPagerAdapter headPagerAdapter = new DetailHeaderPagerAdapter(
				activity, shop.getPhotos());
		ImageGalleryDialog dialog = new ImageGalleryDialog().newInstance(
				headPagerAdapter, position);
		dialog.show(getFragmentManager(), "gallery");
	}
	@Override
	public void onDestroy() {
		activity.unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
	
	/**
	 * 门店活动的ListView的点击事件
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	private void fendianClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Activity activity = shopActivity.getData().get(position);
		ActivityDetailFragment fragment = new ActivityDetailFragment();
		Bundle args = new Bundle();
		args.putString("activityId", activity.getId() + "");
		args.putString("shopId", shopId);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}


	/**
	 * 点击关注按钮，进行关注
	 */
	private void clickToCollectStore() {
		if(App.app.getData("isExperience").equals("true")){
			DialogUtil.createTipAlertDialog(getActivity(),
					R.string.logintofllow, new TipAlertDialogCallBack() {
				@Override
				public void onPositiveButtonClick(
						DialogInterface dialog, int which) {
					dialog.dismiss();
					RegisterFragment fragment = new RegisterFragment();
					FragmentEntity event = new FragmentEntity();
					Bundle bundle = new Bundle();
					App.app.setData("shopId", shopId);
					bundle.putString("fragment_name", StoreDetailFragment.class.getSimpleName());
					fragment.setArguments(bundle);
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
				@Override
				public void onNegativeButtonClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		}else{
			if (shop.isMy_attention()) { 						//-->点击则取消关注
			DialogUtil.createTipAlertDialog(activity, R.string.gz_cancle,
					new TipAlertDialogCallBack() {

						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							InternetConfig config = new InternetConfig();
							config.setKey(3);
							HashMap<String, Object> head = new HashMap<>();
							head.put("Authorization",
									"Bearer "+ App.app.getData("access_token"));
							config.setHead(head);
							config.setMethod("DELETE");
							
							/*点击删除后，我们应该要有的返回提示
							 * if ("true".equals(r.getContentAsString())) {
								CustomToast.show(
										getActivity(),
										getActivity().getString(R.string.tip),
										getActivity().getString(
												R.string.gz_qx_sucess_content));
								v.tv_store_fans_num.setText(shop.getAttention() - 1 + "");
								shop.setMy_attention(false);
								v.iv_store_detail_gz_store.setPressed(false);
							} else {
								CustomToast.show(activity,
										activity.getString(R.string.tip),
										activity.getString(R.string.gz_qx_faild_content));
							}
							break;*/
							
							
							
							// 上述代码的副本！！
							//暂时关闭了取消收藏的按钮，这样避免不能提供足够数据进行测试
							  HttpUtils httpUtils = new HttpUtils();
							  RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

								@Override
								public void onFailure(HttpException arg0, String arg1) {
									Log.i("取消门店关注失败返回的结果","访问是失败的!");
									CustomToast.show(activity,
											activity.getString(R.string.tip),
											activity.getString(R.string.gz_qx_faild_content));
								}
								@Override
								public void onSuccess(ResponseInfo<String> arg0) {
									CustomToast.show(
											getActivity(),
											getActivity().getString(R.string.tip2),
											getActivity().getString(
													R.string.gz_qx_sucess_content));
									//v.tv_store_fans_num.setText(shop.getAttention() - 1 + "");
									
									v.tv_store_fans_num.setText((tempShopAttention-1) + "");
									tempShopAttention =  tempShopAttention-1;
									
									shop.setMy_attention(false);
									//v.iv_store_detail_gz_store.setPressed(false);
									v.iv_store_detail_gz_store.setBackgroundResource(R.drawable.gz_store1);
									
								}
							};
							RequestParams requestParams = new RequestParams();
							requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
							httpUtils.send(HttpMethod.DELETE, 
									GlobalValue.URL_QX_GZ_SHOP+ shopId,
									requestParams,requestCallBack );
							
							/* 实则无效的 删除 提交
							 * FastHttpHander.ajax(GlobalValue.URL_QX_GZ_SHOP+ shopId, null, config,StoreDetailFragment.this);*/
							dialog.dismiss();
						}
						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							v.iv_store_detail_gz_store.setBackgroundResource(R.drawable.gz_store_press1);//是我关注的，但点击后问我要不要取消关注，决定还是算了，不取消关注了吧
							dialog.dismiss();
						}
					});
		} else {
			InternetConfig config = new InternetConfig();
			config.setKey(2);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);

			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("shop_id", shopId + "");
			
			/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_GZ_SHOP, params,null, config, this);*/
			FastHttpHander.ajax(GlobalValue.URL_USER_GZ_SHOP, params, config, this);
		}
	  }
	}
	
	
	
	 class StoreDetailsFragRefreshBroadCastReceiver extends BroadcastReceiver{

			@Override
			public void onReceive(Context ctx, Intent intent) {
				if(intent.getAction().equals("com.lansun.qmyo.refreshTheIcon")){
					System.out.println("门店详情页面  收到刷新Icon的广播了");
					isRefreshTheActivityDetailsFrag = true;
					
					//这是在检测当前门店下的前一页中的活动是否为我曾收藏，若是收藏则需要将前一页的活动收藏按钮的置为绿色
					String shopId = App.app.getData("shop_id");
					String activityId = App.app.getData("activity_id");
					
					refreshUrl = String.format(GlobalValue.URL_ACTIVITY_SHOP, activityId+ "", shopId + "");
					refreshCurrentList(refreshUrl, null, 5, null);//获取活动详情
					
				
				}
				if(intent.getAction().equals("com.lansun.qmyo.refreshStoreDetailPageAndClick")){
					
					//一进来后我们就去访问并去刷新数据
					mRefreshTip = "true";
					refreshCurrentList(refreshUrl, null, 0, v.sc_store_detail);
					
					//需要模拟点击关注的操作
					//clickToCollectStore();
				}
				
				if(intent.getAction().equals("com.lansun.qmyo.refreshStoreDetailPage")){
					
					refreshCurrentList(refreshUrl, null, 0, v.sc_store_detail);
				}
			}
		 }
	
	
}
