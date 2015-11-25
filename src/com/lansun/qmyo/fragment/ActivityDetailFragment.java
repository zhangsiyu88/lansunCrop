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
import android.support.v4.app.ActionBarDrawerToggle.Delegate;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.AjaxCallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.ActivityDetailPagerAdapter;
import com.lansun.qmyo.adapter.MaiCommentAdapter2;
import com.lansun.qmyo.adapter.TipAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityContent;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.domain.CouponsData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.AnimUtils;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.MySubListView;
import com.lansun.qmyo.view.SharedDialog;
import com.lansun.qmyo.view.TelDialog;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.http.client.multipart.MultipartEntity.CallBackInfo;
import com.lansun.qmyo.R;
import com.umeng.socialize.media.GooglePlusShareContent;

/**
 * 活动详情 TODO 
 * 
 * @author bhxx
 * 
 */
public class ActivityDetailFragment extends BaseFragment {

	private int position;
	private ImageView[] imageViews;

	private boolean isOpen = true;

	@InjectAll
	Views v;
	private String activityId;
	private String shopId;
	private boolean HasComment = true;
	private String mRefreshTip="";

	class Views {
		private ScrollView sc_activity_detail;
		private LinearLayout ll_vp_points, ll_activity_detail_points;
		private ViewPager vp_baner;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_shop_telephone, tv_look_mine_coupons;
		private TextView tv_activity_tag, tv_activity_shop_name,
				tv_activity_shop_address, tv_activity_name, tv_activity_time,
				tv_activity_coupons_remainder_1,
				tv_activity_coupons_remainder_2,
				tv_activity_coupons_denomination_1,
				tv_activity_coupons_denomination_2;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_activity_detail_map, iv_back, ll_activity_detail_none,
				iv_activity_detail_join_store, iv_open_activity_detail,
				iv_activity_shop_detail, ll_activity_collection,
				iv_activity_shared, ll_activity_detail_bg, iv_shop_info,
				iv_activity_detail_report_list, ll_activity_detail_report,
				rl_activity_detail_institution, yh_detail_header;

		private View ll_activity_coupons_1, ll_activity_coupons, line;
		private MySubListView lv_comments_list;
		private MySubListView lv_tip_list;
		private RecyclingImageView iv_activity_collection;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_activity_detail, null);
		Handler_Inject.injectFragment(this, rootView);
		
		
		broadCastReceiver = new ActivityDetailsRefreshBroadCastReceiver();
		System.out.println("注册广播 ing");
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.refreshTheActivityDetailsFrag");
		getActivity().registerReceiver(broadCastReceiver, filter);
		
		return rootView;
	}

	@InjectInit
	private void init() {
		//将评论列表的listView 放入到 scrollview 这个父布局中
		v.lv_comments_list.setParentScrollView(v.sc_activity_detail);
		
		AnimUtils.startRotateIn(activity, v.iv_open_activity_detail);
		
		if(getArguments()!=null){
			shopId = getArguments().getString("shopId");
			activityId = getArguments().getString("activityId");
			mRefreshTip = getArguments().getString("refreshTip");
		}
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		
		int cueentHeight = (int) (((double) screenWidth / 720) * v.vp_baner.getLayoutParams().height);
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, cueentHeight);
		
		v.vp_baner.setLayoutParams(params);
		
		initData();
		
		v.vp_baner.setOnPageChangeListener(pageChangeListener);
		
		progress_container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				initData();
			}
		});
	}


	private void initData() {
		refreshUrl = String.format(GlobalValue.URL_ACTIVITY_SHOP, activityId+ "", shopId + "");
		refreshCurrentList(refreshUrl, null, 0, v.sc_activity_detail);//获取活动详情
		refreshCurrentList(String.format(GlobalValue.URL_ACTIVITY_COMMENTS, activityId),null, 1, null);//获取评论内容
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				data = Handler_Json.JsonToBean(ActivityListData.class,
						r.getContentAsString());
				String activityName = data.getActivity().getName();
				String activityTime = data.getActivity().getTime();
				String tag = data.getActivity().getTag();
				
				/*
				 * 这一步私自篡改了我的定位信息,气伤我了!
				 * 关键这里的gps在当前类中还是没有被用到的玩意
				 * 暂禁下面的方法*/
				 /* GlobalValue.gps = new Gps(Double.parseDouble(data.getShop()
						.getLat()), Double.parseDouble(data.getShop().getLng()));*/
				
				
				Gps TargetPointGps = new Gps(Double.parseDouble(data.getShop()
						.getLat()), Double.parseDouble(data.getShop().getLng()));
				App.app.setData("TargetPointLat", TargetPointGps.getWgLat()+"");
				App.app.setData("TargetPointLng", TargetPointGps.getWgLon()+"");
				
				ArrayList<String> photos = data.getActivity().getPhotos();
				ArrayList<String> icons = data.getActivity().getCategory();
				v.tv_activity_shop_name.setText(data.getShop().getName());
				v.tv_activity_shop_address.setText(String.format(
						getString(R.string.tele_address), data.getShop()
								.getAddress()));
				
				if(data.getShop().getTelephone()==""||data.getShop().getTelephone().isEmpty()||data.getShop().getTelephone().equals(null)){
					v.tv_activity_shop_telephone.setText(Html.fromHtml(String
							.format(getString(R.string.telephone_nothave_now), "暂无")));
					
				}else{
					v.tv_activity_shop_telephone.setText(Html.fromHtml(String
							.format(getString(R.string.telephone), data.getShop()
									.getTelephone())));
				}
				
				//优惠券返回暂时为空的！
				ArrayList<CouponsData> coupons = data.getActivity().getCoupons();
				if (coupons != null) {
					if (coupons.size() > 1) {
						v.tv_activity_coupons_denomination_1.setText(coupons
								.get(0).getDenomination());
						v.tv_activity_coupons_remainder_1.setText(String
								.format(getString(R.string.coupons_remainder),
										coupons.get(0).getRemainder()));
						v.tv_activity_coupons_denomination_2.setText(coupons
								.get(1).getDenomination());
						v.tv_activity_coupons_remainder_2.setText(String
								.format(getString(R.string.coupons_remainder),
										coupons.get(1).getRemainder()));
					} else {
						v.ll_activity_coupons_1.setVisibility(View.GONE);
						v.tv_activity_coupons_denomination_2.setText(coupons
								.get(0).getDenomination());
						v.tv_activity_coupons_remainder_2.setText(coupons
								.get(0).getRemainder());
					}
				} else {//优惠券为空，则将ll_activity_coupons设置null,那么下面的对象无疑是找不到的
					//TODO
					/*v.ll_activity_coupons.setVisibility(View.GONE);
					v.line.setVisibility(View.GONE);*/
				}

				
				if (data.getActivity().getInstitution() != 0) {
					v.rl_activity_detail_institution.setVisibility(View.VISIBLE);
				}

				if (data.getActivity().getOther_shop() < 0) {
					v.iv_activity_detail_join_store.setVisibility(View.GONE);
				}

				v.tv_activity_name.setText(activityName);
				v.tv_activity_time.setText(activityTime);
				if (data.getActivity().isMy_attention()) {
					v.iv_activity_collection.setPressed(true);
					v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_green);
				}

				if (data.getActivity().getContent() != null) {

					/*ArrayList<HashMap<String, String>> dataList2 = new ArrayList<HashMap<String, String>>();*/
					
					
					ArrayList<HashMap<String, String>> dataList3 = new ArrayList<HashMap<String, String>>();
					
					for (ActivityContent data1 : data.getActivity().getContent()) {//data.getActivity().getContent()和标题数目一致
						
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("tv_activity_content_mx_title",data1.getTitle());
						
						/*StringBuilder sb = new StringBuilder();
						  for (int i = 0; i < data1.getContent().size(); i++) {
							if (i == data1.getContent().size()) {
								sb.append("\u2022  "+ data1.getContent().get(i));
							} else {
								sb.append("\u2022  "+ data1.getContent().get(i) + "\r\n\r\n");
							}
						}*/
						/*
						for (int i = 0; i < data1.getContent().size(); i++) {
							if (i == data1.getContent().size()) {
								sb.append(data1.getContent().get(i));
							} else {
								sb.append(data1.getContent().get(i) + "\r\n\r\n");
							}
						}
						map.put("tv_activity_content_mx_content", sb.toString());*/
						
						for (int i = 0; i < data1.getContent().size(); i++) {
							map.put("tv_activity_content_mx_content"+i, data1.getContent().get(i));
						}
						dataList3.add(map);
						/*dataList2.add(map);*/
					}
					
					
					//活动详情页下面的MySubListView对应的适配器
					TipAdapter adapter = new TipAdapter(v.lv_tip_list,
							dataList3, R.layout.activity_activity_tip_item, activity);
					v.lv_tip_list.setAdapter(adapter);
					
				}
				
				switchIcon(icons);
				v.tv_activity_tag.setText(tag);
				ActivityDetailPagerAdapter pagerAdapter = new ActivityDetailPagerAdapter(
						activity, data.getActivity().getPhotos());
				
				initBar(pagerAdapter.getCount());//拿到需要展示的小圆点个数
				
				v.vp_baner.setAdapter(pagerAdapter);
				
				if(mRefreshTip.equals("true")){
					//执行更新界面的操作
					clickToCollectActivity();
				}
				break;
			case 1:
				CommentList list = Handler_Json.JsonToBean(CommentList.class,
						r.getContentAsString());
				ArrayList<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
				if (list.getData() != null) {

					for (CommentListData commentListData : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						String content = commentListData.getComment()
								.getContent();
						String time = commentListData.getComment().getTime();
						String user_name = commentListData.getUser().getName();
						String user_avatar = commentListData.getUser()
								.getAvatar();
						ArrayList<String> photos2 = commentListData
								.getComment().getPhotos();
						map.put("iv_comment_head", user_avatar);
						map.put("tv_comment_name", user_name);
						map.put("tv_comment_time", time);
						map.put("tv_comment_desc", content);
						map.put("photos", photos2);
						dataList.add(map);
					}
					MaiCommentAdapter2 maiCommentAdapter2 = new MaiCommentAdapter2(
							v.lv_comments_list, dataList,
							R.layout.mai_comment_lv_item2);
					maiCommentAdapter2.setActivity(this);
					v.lv_comments_list.setAdapter(maiCommentAdapter2);
				} else {
					
					
					/**暂时禁掉！！
					 * 
					 * 即使没有拿到数据也不要将其显示
					 * v.ll_activity_detail_none.setVisibility(View.VISIBLE);*/
					HasComment = false;
				}

				
				
				break;
			case 2:
				if ("true".equals(r.getContentAsString())) {
					v.iv_activity_collection.setPressed(true);
					v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_green);
					data.getActivity().setMy_attention(true);
					CustomToast.show(activity, getString(R.string.sc_sucess),getString(R.string.sc_sucess_content));

				}
				break;
			case 3:
				if ("true".equals(r.getContentAsString())) {
					v.iv_activity_collection.setPressed(false);
					v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_gray);
					data.getActivity().setMy_attention(false);
					CustomToast.show(activity,getString(R.string.sc_sucess_cancle),getString(R.string.sc_sucess_content_cancle));
				}
				break;
			}
		} else {
			//当上面众多的网络访问中出现一个有r.getState 不为 OK时，底下的大界面中的文字都会出现“网络错误”的提示
			progress_text.setText(R.string.net_error_refresh);
		}
	}

	/**
	 * 加标签的内容
	 * @param icons
	 */
	private void switchIcon(ArrayList<String> icons) {
		if (icons != null) {
			for (String icon : icons) {
				RecyclingImageView iv = new RecyclingImageView(activity);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin = 15;
				iv.setLayoutParams(params);
				if ("discount".equals(icon)) {
					iv.setBackgroundResource(R.drawable.discount);
					v.ll_activity_detail_points.addView(iv);
				} else if ("new".equals(icon)) {
					iv.setBackgroundResource(R.drawable.new1);
					v.ll_activity_detail_points.addView(iv);
				} else if ("point".equals(icon)) {
					iv.setBackgroundResource(R.drawable.point);
					v.ll_activity_detail_points.addView(iv);
				} else if ("staging".equals(icon)) {
					iv.setBackgroundResource(R.drawable.staging);
					v.ll_activity_detail_points.addView(iv);
				} else if ("coupon".equals(icon)) {
					iv.setBackgroundResource(R.drawable.coupon);
					v.ll_activity_detail_points.addView(iv);
				}
			}
		}
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		Bundle bundle = new Bundle();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment;
		Bundle args;
		switch (view.getId()) {
		case R.id.yh_detail_header://点击活动优惠详情页的线性布局
			/**
			点击上面横栏不产生任何操作 */
			  if (isOpen) {
				AnimUtils.startRotateOut(activity, v.iv_open_activity_detail);
				v.lv_tip_list.setVisibility(View.GONE);
				isOpen = false;
			} else {
				v.lv_tip_list.setVisibility(View.VISIBLE);
				AnimUtils.startRotateIn(activity, v.iv_open_activity_detail);
				isOpen = true;
			}
			break;
			
		case R.id.iv_open_activity_detail:
			if (isOpen) {
				AnimUtils.startRotateOut(activity, v.iv_open_activity_detail);
				v.lv_tip_list.setVisibility(View.GONE);
				isOpen = false;
			} else {
				v.lv_tip_list.setVisibility(View.VISIBLE);
				AnimUtils.startRotateIn(activity, v.iv_open_activity_detail);
				isOpen = true;
			}
			break;
			
		case R.id.iv_activity_shop_detail:
			
			if(App.app.getData("isExperience")=="true"){//为了保证，在体验用户的状态下，后面在门店详情页登陆完之后，可能需要重新获取一次当前活动是否为收藏的信息
				App.app.setData("shop_id", shopId);
				App.app.setData("activity_id", activityId);
			}
			
			fragment = new StoreDetailFragment();
			bundle.putString("shopId", shopId);
			fragment.setArguments(bundle);
			entity.setFragment(fragment);
			bus.post(entity);
			break;
		case R.id.iv_activity_detail_join_store:   //展示参与此活动的分店

			fragment = new StoreListFragment();
			bundle.putInt("type", 1);
			bundle.putString("activityId", activityId);
			bundle.putString("shopId", shopId);
			fragment.setArguments(bundle);
			entity.setFragment(fragment);
			bus.post(entity);

			break;
		case R.id.iv_activity_detail_report_list://评论列表
			if (HasComment) {
				fragment = new MaiCommentListFragment();
				args = new Bundle();
				args.putString("activityId", activityId);
				args.putString("shopId", shopId);
				fragment.setArguments(args);
				entity.setFragment(fragment);
				bus.post(entity);
			} else {
				if(App.app.getData("isExperience")=="true"){
					DialogUtil.createTipAlertDialog(getActivity(),
							R.string.login_to_comment, new TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							RegisterFragment fragment = new RegisterFragment();
							FragmentEntity event = new FragmentEntity();
							App.app.setData("shopId", shopId);
							App.app.setData("activityId", activityId);
							Bundle bundle = new Bundle();
							bundle.putString("fragment_name", ActivityDetailFragment.class.getSimpleName());
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
					fragment = new NewCommentFragment();
					bundle.putString("activity_id", activityId);
					bundle.putString("shop_id", shopId);
					fragment.setArguments(bundle);
					entity.setFragment(fragment);
					bus.post(entity);
				}
			}
			break;
		case R.id.rl_activity_detail_institution:// 服务机构
			fragment = new InstitutionFragment();
			entity.setFragment(fragment);
			bus.post(entity);
			break;
		case R.id.iv_activity_shared:              //TODO 分享
			showDialog();
			break;
		case R.id.ll_activity_detail_report:// 举报
			if(App.app.getData("isExperience").equals("true")){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_report, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						App.app.setData("shopId", shopId);
						App.app.setData("activityId", activityId);
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", ActivityDetailFragment.class.getSimpleName());
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
				args.putString("activity_id", activityId + "");
				args.putString("shop_id", shopId + "");
				args.putInt("type", 0);
				fragment.setArguments(args);
				entity.setFragment(fragment);
				bus.post(entity);
			}
			break;
		//TODO
		/*case R.id.tv_look_mine_coupons:// 优惠券
			fragment = new MineCouponsFragment();
			entity.setFragment(fragment);
			bus.post(entity);
			break;                              					暂时去除我的优惠券的 展示，在xml中将其注解掉，以免出现闪现的现象   */
		case R.id.iv_activity_detail_map:// 地图功能
			fragment = new MapFragment();
			args = new Bundle();
			args.putString("shopname", data.getShop().getName());
			args.putString("shopaddress", data.getShop().getAddress());
			fragment.setArguments(args);
			entity.setFragment(fragment);
			bus.post(entity);
			break;
		case R.id.ll_activity_collection: // 活动详情收藏
			
			clickToCollectActivity();

			break;
		case R.id.tv_activity_shop_telephone:
			if (!TextUtils.isEmpty(data.getShop().getTelephone())) {
				TelDialog dialog = new TelDialog();
				Bundle args1 = new Bundle();
				args1.putString("phone", data.getShop().getTelephone());
				dialog.setArguments(args1);
				dialog.show(getFragmentManager(), "tel");
			}
			break;
		}
	}
		/*	
		private void showDialog() {
			new SharedDialog().showPopwindow(rootView, getActivity(), data
				.getShop().getName(), data.getShop().getName() + "举办"
				+ data.getActivity().getName(), data.getActivity().getPhotos()
				.get(0),data.getActivity().getShare_url());
	}*/
	
	//去除掉xxx（店名）举办xxx（活动）中的前半部分
	private void showDialog() {
			new SharedDialog().showPopwindow(rootView, getActivity(), data
			.getShop().getName(), data.getShop().getName()+ " 举办  "+data.getActivity().getName(), data.getActivity().getPhotos()
			.get(0),data.getActivity().getShare_url());
}
	

	/*
	 * page的页面改变监听器
	 */
	OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			for (int i = 0; i < imageViews.length; i++) {
					imageViews[position].setBackgroundResource(R.drawable.oval_select);
				if (position != i) {
					imageViews[i].setBackgroundResource(R.drawable.oval_nomal);
				}
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};
	private View rootView;
	private ActivityListData data;
	private ActivityDetailsRefreshBroadCastReceiver broadCastReceiver;

	
	/*
	 * 绿色小圆点的显示
	 */
	private void initBar(int count) {
		imageViews = new ImageView[count];
		if(count == 1){
			v.ll_vp_points.setVisibility(View.INVISIBLE);
			return;
		}
		for (int i = 0; i < count; i++) {
			ImageView iv = new ImageView(activity);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			params.leftMargin = 8;
			iv.setLayoutParams(params);
			imageViews[i] = iv;
			if (i == 0) {
				imageViews[i].setBackgroundResource(R.drawable.oval_select);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.oval_nomal);
			}
			imageViews[i] = iv;
			
			v.ll_vp_points.addView(imageViews[i]);
		}
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * 点击收藏之前看的活动
	 */
	private void clickToCollectActivity() {
		Log.d("异常处的isExperience的值为： ", "异常处的isExperience的值为： "+App.app.getData("isExperience"));
		
		if(App.app.getData("isExperience").equals("true")){
			DialogUtil.createTipAlertDialog(getActivity(),
					R.string.logintocollectactivity, new TipAlertDialogCallBack() {
				@Override
				public void onPositiveButtonClick(
						DialogInterface dialog, int which) {
					dialog.dismiss();
					RegisterFragment fragment = new RegisterFragment();
					FragmentEntity event = new FragmentEntity();
					App.app.setData("shopId", shopId);
					App.app.setData("activityId", activityId);
					Bundle bundle = new Bundle();
					bundle.putString("fragment_name", ActivityDetailFragment.class.getSimpleName());
					fragment.setArguments(bundle);
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
				@Override
				public void onNegativeButtonClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return;
		}
		
		if (!data.getActivity().isMy_attention()) {//如果不是我收藏过的,点击即收藏
			InternetConfig config = new InternetConfig();
			config.setKey(2);
			HashMap<String, Object> head = new HashMap<String, Object>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			params.put("activity_id", activityId + "");
			params.put("shop_id", shopId + "");
			
			/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_ACTIVITY, params,
					null, config, this);*/
			
			//使用post方式去提交
			FastHttpHander.ajax(GlobalValue.URL_USER_ACTIVITY, params,
					config, this);
			
		} else {
			InternetConfig config = new InternetConfig();
			config.setKey(3);
			HashMap<String, Object> head = new HashMap<String, Object>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			config.setRequest_type(InternetConfig.request_delete);
			config.setMethod("DELETE");
			
			String url = "http://appapi.qmyo.com/activity/"+data.getActivity().getId()+"?shop_id="+data.getShop().getId();
			
			/*FastHttpHander.ajax(String.format(GlobalValue.URL_USER_ACTIVITY_DELETE,data.getActivity().getId(), data.getShop().getId()), 
			  config, this);*/
			
			//提交方式的设置是在config中的setMethod上,也就是说config.setRequest_type(InternetConfig.request_delete)没啥实用
			/*	FastHttpHander.ajax(url,config, this);
			
			FastHttpHander.ajaxForm(url, config, this);
			AjaxCallBack callBack = new AjaxCallBack(){

				@Override
				public void callBack(ResponseEntity status) {
					Log.i("ajaxCallBack_callBack", "CallBack");
				}
				@Override
				public boolean stop() {
					Log.i("ajaxCallBack_stop", "CallBack");
					return false;
				}
			};
			FastHttp.ajaxWebServer(url, "DELETE", config, callBack );*/
			
			/*暂时关闭了取消收藏的按钮，这样避免不能提供足够数据进行测试
			 * 
			 * HttpUtils httpUtils = new HttpUtils();
			RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Log.i("Activity取消收藏失败返回的结果","访问是失败的!");
				}
				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					Log.i("Activity取消收藏成功返回的结果",arg0.result.toString());
					if ("true".equals(arg0.result.toString())) {
						v.iv_activity_collection.setPressed(false);
						v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_gray);
						data.getActivity().setMy_attention(false);
						CustomToast.show(activity,
								getString(R.string.sc_sucess_cancle),
								getString(R.string.sc_sucess_content_cancle));
					}
				}
			};
			RequestParams requestParams = new RequestParams();
			requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
			httpUtils.send(HttpMethod.DELETE, 
					"http://appapi.qmyo.com/activity/"+data.getActivity().getId()+"?shop_id="+data.getShop().getId(),
					requestParams,requestCallBack );*/
			
			
			
			
			// 上述代码的副本！！ 将其打开
			//暂时关闭了取消收藏的按钮，这样避免不能提供足够数据进行测试
			  HttpUtils httpUtils = new HttpUtils();
			  RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Log.i("Activity取消收藏失败返回的结果","访问是失败的!");
				}
				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					Log.i("Activity取消收藏成功返回的结果",arg0.result.toString());
					if ("true".equals(arg0.result.toString())) {
						v.iv_activity_collection.setPressed(false);
						v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_gray);
						data.getActivity().setMy_attention(false);
						CustomToast.show(activity,
								getString(R.string.sc_sucess_cancle_collection),
								getString(R.string.sc_sucess_content_cancle_collection));
					}
				}
			};
			RequestParams requestParams = new RequestParams();
			requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
			/*httpUtils.send(HttpMethod.DELETE, 
					GlobalValue.IP+"activity/"+data.getActivity().getId()+"?shop_id="+data.getShop().getId(),
					requestParams,requestCallBack );*/
			httpUtils.send(HttpMethod.DELETE,String.format(GlobalValue.URL_USER_ACTIVITY_DELETE,data.getActivity().getId(), data.getShop().getId()),
					requestParams,requestCallBack);
		}
	}
	
	 class ActivityDetailsRefreshBroadCastReceiver extends BroadcastReceiver{
			@Override
			public void onReceive(Context ctx, Intent intent) {
				if(intent.getAction().equals("com.lansun.qmyo.refreshTheActivityDetailsFrag")){
					System.out.println("活动详情页面   收到来自  门店详情页刷新Icon的广播了");
					v.iv_activity_collection.setPressed(true);
					v.ll_activity_collection.setBackgroundResource(R.drawable.circle_background_green);
				
				}
			}
		 }

		
		
}
