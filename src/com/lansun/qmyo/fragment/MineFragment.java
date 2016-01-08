package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.MessageAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.MessageData;
import com.lansun.qmyo.domain.MessageList;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.domain.information.InformationCount;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryDetailsBaseFragment.SecretaryDetailsFragmentBroadCastReceiver;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.UpdateAppVersionDialog;
import com.lansun.qmyo.view.UpdateAppVersionDialog.OnConfirmListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
public class MineFragment extends BaseFragment implements RequestCallBack{
	@InjectAll
	Views v;

	class Views {
		private TextView tv_mine_nickname, tv_mine_icon,have_secretary,have_information;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_secretary, bottom_found,
		ll_mine_register_login, rl_mine_secretary, rl_mine_comments,
		rl_mine_history, ll_mine_xy_card, ll_mine_yhq, ll_mine_message,
		rl_mine_about, rl_mine_shared, sc_mine;
		private RecyclingImageView iv_mine_icon;
		private CircleImageView iv_mine_head;
		
	}
	private Handler handlerOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (count.getActivity()==1||count.getMaijie()==1) {
					v.have_information.setVisibility(View.VISIBLE);
					v.have_secretary.setVisibility(View.VISIBLE);
				}else {
					v.have_information.setVisibility(View.GONE);
					v.have_secretary.setVisibility(View.GONE);
				}
				break;
			}
		};
	};
	private InformationCount count;
	public  ArrayList<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
	private MineFragmentBroadCastReceiver broadCastReceiver = null;
	private IntentFilter filter;
	public boolean isFirstReceiveBroadcast = true;
	private View rootView; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//App.app.setData("firstEnterBankcardAndAddAnotherBankcard","");
		super.onCreate(savedInstanceState);
		
		if(broadCastReceiver == null){
			broadCastReceiver = new MineFragmentBroadCastReceiver();
			System.out.println("我的  页面在注册广播 ing");
			filter = new IntentFilter();
			filter.addAction("com.lansun.qmyo.refreshTheIcon");
			filter.addAction("com.lansun.qmyo.refreshAvatar_NickName");
			
			getActivity().registerReceiver(broadCastReceiver, filter);
		}else{
			//NO-OP
		}
		initMySecretary();
		
		//此方法暂无任何具体操作
		initInformation();
	}
	private void initInformation() {
//		InformationBiz biz=new InformationBiz();
//		biz.getInformation(this);
	}
	/**
	 * 此处请求网络目的是当我进入私人秘书的时候要有个私人秘书的名字。
	 */
	private void initMySecretary() {
		
		LogUtils.toDebugLog("initMySecretary", "initMySecretary()");
		
		if (!isExperience()) {//登录用户
			OkHttp.asyncGet(GlobalValue.URL_SECRETARY_SAVE, "Authorization","Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						GlobalValue.mySecretary=gson.fromJson(json,MySecretary.class);
						activity.sendBroadcast(new Intent("com.lansun.qmyo.refreshMySecretary"));
						/*if(first_enter == 0){
							activity.sendBroadcast(new Intent("com.lansun.qmyo.refreshMySecretary"));
							first_enter = Integer.MAX_VALUE;
						}*/
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {
				}
			});
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_mine,container,false);
		
		Handler_Inject.injectFragment(this, rootView);//此方法一旦被调用，由于随即调用了init()方法，所以看到log显示init()方法优于onCreatView()方法，实则不然
		
		v.iv_mine_icon.setPressed(true);
		
		loadNickAndAvatar();
		
		//v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));
		return rootView;
	}

	
	@InjectInit
	private void init() {
		
		//1.使用消息中心的网络接口前去访问 活动 和 迈界 消息，当返回的列表中的某一项的data.getIs_read()有值时 ，
		//即可将通知首页小圆点的显示
	
		String activityMessageUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
		String    majieMessageUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.maijie;
		
//		refreshCurrentList(activityMessageUrl, null, 0, null);
//		LogUtils.toDebugLog("infos", "去拿关于活动的信息");
//		refreshCurrentList(majieMessageUrl, null, 1, null);
//		LogUtils.toDebugLog("infos", "去拿关于迈界的信息");
		
		
		v.iv_mine_icon.setPressed(true);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));
		
		loadNickAndAvatar();
		
		
	}

	@Override
	public void onResume() {
		v.iv_mine_icon.setPressed(true);
		
		/*if (GlobalValue.user != null) {
			String avatar = GlobalValue.user.getAvatar();
			if (!TextUtils.isEmpty(avatar)) {
				//加载头像上去
				loadPhoto(avatar, v.iv_mine_head);
			}
			if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
				v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				Log.i("Tag：nickName","在init之后起作用");

				if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
					v.tv_mine_nickname.setText("请设置昵称");
				}else{
					v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				}
			}else{//GlobalValue.user.getNickname()
				v.tv_mine_nickname.setText("请注册或登陆");
			}
		}*/
		super.onResume();
	}
	@Override
	public void onPause() {
		/*v.iv_mine_icon.setPressed(false);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.text_nomal));*/
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment = null;
		switch (view.getId()) {
		// case R.id.iv_mine_setting:// 设置界面
		// fragment = new SettingFragment();
		// break;
		case R.id.ll_mine_register_login:// 注册登录
			if (!TextUtils.isEmpty(App.app.getData("secret"))) {
				fragment = new PersonCenterFragment();
			} else {
				fragment = new RegisterFragment();
				Bundle bundle=new Bundle();
				bundle.putString("fragment_name",MineFragment.class.getSimpleName());
				fragment.setArguments(bundle);
			}
			break;
		case R.id.rl_mine_secretary:// 我的私人秘书
			if (GlobalValue.user == null || isExperience()) {
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_enjoysecretaryyourself, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", MineFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else
				/*fragment = new MineSecretaryFragment();*/
				fragment = new MineSecretaryListFragment();
			break;
		case R.id.rl_mine_comments:// 我的评论
			if (GlobalValue.user == null || isExperience()) {
				fragment = new RegisterFragment();
				Bundle bundle = new Bundle();
				bundle.putString("fragment_name", MineFragment.class.getSimpleName());
				fragment.setArguments(bundle);
			} else {
				fragment = new MineCommentsFragment();
			}
			break;
		case R.id.rl_mine_history:// 最近浏览
			if (GlobalValue.user == null || isExperience()) {
				/*fragment = new RegisterFragment();*///体验用户也可以进入到我的历史浏览页
				fragment = new MineHistoryFragment();
			} else {
				fragment = new MineHistoryFragment();
			}
			break;
		case R.id.ll_mine_xy_card:// 信用卡
			/*	if (GlobalValue.user == null) {
				return;
			}
			if (isExperience()) {
				fragment = new RegisterFragment();
			} else
			 */
			fragment = new MineBankcardFragment();
			break;
		case R.id.ll_mine_yhq:// TODO 优惠券
			if (GlobalValue.user == null || isExperience()) {
				/*fragment = new RegisterFragment();*/
				fragment = new MineCouponsFragment();
			} else
				fragment = new MineCouponsFragment();
			break;
		case R.id.ll_mine_message:// TODO 消息中心
			if(App.app.getData("isExperience").equals("true")){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_getmessage, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", MineFragment.class.getSimpleName());
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
				fragment = new MessageCenterFragment();
			}
			/*fragment = new MessageCenterFragment();*/

			break;
		case R.id.rl_mine_about:
			fragment = new AboutFragment();
//			fragment = new PromoteDetailFragment();
//			供测试使用
//			App.app.setData("access_token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIx" +
//					"NDU4IiwiaXNzIjoiaHR0cDpcL1wvYXBwYXBpLnFteW8uY29tXC90b2tlblwvbXpwaWMzemlhcCIsImlhd" +
//					"CI6IjE0NDkxOTE1NTIiLCJleHAiOiIxNDQ5MTk1MTUyIiwibmJmIjoiMTQ0OTE5MTU1MiIsImp0aSI6ImZlZDRkZTRmMDJ" +
//					"mOGYwMGNjNjFhMTUwOTBjOGM2N2IyIn0.WEXsGxrSvmO1sGhQ3pVjFJf2gWlTIgkzVc5XPT2G-QY");
//			App.app.setData("LastRefreshTokenTime",String.valueOf(30*60*1000));
			
//			rootView.buildDrawingCache();
//			Bitmap bitmap = rootView.getDrawingCache();
//
			
			
//			App.app.setData("firstCommitPersonalSecretaryAsk","");
//			UpdateAppVersionDialog dialog = new UpdateAppVersionDialog();//这么个体验的对话框，需要单独在其内部设置点击响应事件
//			//进来首先就弹出对话框
//			dialog.setOnConfirmListener(new OnConfirmListener(){
//				@Override
//				public void confirm() {
//					CustomToast.show(activity, "看好咯", "我要下载咯");
//				}
//			});
//			dialog.show(getActivity().getFragmentManager(), "update");
			
			
//			return;
			
			break;
		case R.id.rl_mine_shared://分享APP
			fragment = new SharedFragment();
			//activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));
			break;

		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.bottom_secretary:
			fragment = new SecretaryFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
			break;
		}
		entity.setFragment(fragment);
		bus.post(entity);
	}

	
	@InjectHttp
	private void result(ResponseEntity r) {
		
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			
			switch (r.getKey()) {
			case 0:
				int is_readForActivity = Integer.MAX_VALUE;
				MessageList activityMessList = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				
				LogUtils.toDebugLog("infos", "拿到关于活动的信息");
				LogUtils.toDebugLog("infos", "拿到关于活动的信息"+ activityMessList.toString());
				
				HashMap<String, String> messMap = new HashMap<String, String>();
				if (activityMessList.getData() != null) {
					
					for (MessageData data : activityMessList.getData()) {
						messMap = new HashMap<String, String>();
						messMap.put("tv_message_item_count", data.getIs_read() + "");
						dataList.add(messMap);
					}
					for(HashMap data:dataList){
						LogUtils.toDebugLog("infos", "活动：  dataList中的每个信息的已读与未读标签为： "+data.get("tv_message_item_count")+"");
						LogUtils.toDebugLog("infos", "活动 ：  is_readForActivity ： "+is_readForActivity);
						if(is_readForActivity != 0){
							//is_read为0时 ，表明其未读，小绿点可见
							is_readForActivity= Integer.valueOf((String) data.get("tv_message_item_count"));
							LogUtils.toDebugLog("infos", "关于活动的消息--》已读");
						}else{
							activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));
							LogUtils.toDebugLog("infos", "活动： 发送广播");
							break;
						}
					}
				} 
				break;
			case 1:
				int is_readForMaijie = Integer.MAX_VALUE;
				MessageList maijieMessList = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				
				LogUtils.toDebugLog("infos", "拿到关于迈界的信息");
				LogUtils.toDebugLog("infos", "拿到关于迈界的信息"+ maijieMessList.toString());
				
				if (maijieMessList.getData() != null) {
					
					HashMap<String, String> maijieMap = new HashMap<String, String>();
					
					for (MessageData data : maijieMessList.getData()) {
						maijieMap.put("tv_message_item_count", data.getIs_read() + "");
						dataList.add(maijieMap);
					}
					for(HashMap<String, String> data:dataList){
						LogUtils.toDebugLog("infos", "迈界 ：  dataList中的每个信息的已读与未读标签为： "+data.get("tv_message_item_count")+"");
						LogUtils.toDebugLog("infos", "迈界 ：  is_readForMaijie ： "+is_readForMaijie);
						
						if(is_readForMaijie != 0){//一旦有一个消息未读，我们都需要将首页的小绿点展示出来
							//is_read为0时 ，表明其未读，小绿点可见
							is_readForMaijie= Integer.valueOf(data.get("tv_message_item_count"));
							LogUtils.toDebugLog("infos", "关于迈界的消息--》已读");
						}else{
							activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));//LGP:Little Green Point
							LogUtils.toDebugLog("infos", "迈界： 发送广播");
							break;
						}
					}
				} 
				break;
			}
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	
	@Override
	public void onResponse(Response response) throws IOException {
		if (response.isSuccessful()) {
			String json=response.body().string();
			Gson gson=new Gson();
			count=gson.fromJson(json, InformationCount.class);
			handlerOk.sendEmptyMessage(0);
		}
	}
	@Override
	public void onFailure(Request request, IOException exception) {

	}
	
	public  class MineFragmentBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(isFirstReceiveBroadcast){
				if(intent.getAction().equals("com.lansun.qmyo.refreshTheIcon")){
//					System.out.println("我的页收到刷新 头像 和 昵称 的广播了");
//					
//					//重新执行以下init方法
//					if(first_enter==0){
//						initMySecretary();//重新去获取关于登陆后账户的 私人秘书的信息
//						first_enter = Integer.MAX_VALUE;
//					}else{
//						//NO-OP
//					}
					
					if (!isExperience()) {
						OkHttp.asyncGet(GlobalValue.URL_SECRETARY_SAVE, "Authorization","Bearer "+App.app.getData("access_token"), null, new Callback() {
							@Override
							public void onResponse(Response response) throws IOException {
								if (response.isSuccessful()) {
									Gson gson=new Gson();
									String json=response.body().string();
									GlobalValue.mySecretary=gson.fromJson(json,MySecretary.class);
									activity.sendBroadcast(new Intent("com.lansun.qmyo.refreshMySecretary"));
									
									/*if(first_enter == 0){
										activity.sendBroadcast(new Intent("com.lansun.qmyo.refreshMySecretary"));
										first_enter = Integer.MAX_VALUE;
									}*/
								}
							}
							@Override
							public void onFailure(Request arg0, IOException arg1) {
							}
						});
					}
					
					
					
					
					
					
					
					/*init();*/
					/**
					 * 保证从后台重新进入当前fragment时，头像部分可以重新加载上去
					 */
					if (GlobalValue.user != null) {
						String avatar = GlobalValue.user.getAvatar();
						if (!TextUtils.isEmpty(avatar)) {
							//加载头像上去
							loadPhoto(avatar, v.iv_mine_head);
						}
						
						if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
							
							if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
								v.tv_mine_nickname.setText("请设置昵称");
								Log.i("Tag：nickName","NickName应该为设置昵称");
								
							}else{
								Log.i("Tag：nickName","NickName有值："+GlobalValue.user.getNickname());
								v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
							}
						}else{
							v.tv_mine_nickname.setText("请注册或登陆");
						}
					}else{    //--- >GlobalValue.user == null
						
						LogUtils.toDebugLog("userinfo", "onCreatView()方法里： GlobalValue.user为空");
						//CustomToast.show(activity, "onCreatView()方法里： GlobalValue.user为空", "同上");
						
						if(!TextUtils.isEmpty(App.app.getData("user_avatar"))&&!TextUtils.isEmpty(App.app.getData("user_nickname"))){
							loadPhoto(App.app.getData("user_avatar"), v.iv_mine_head);
							v.tv_mine_nickname.setText(App.app.getData("user_nickname"));
						}else{
							
						}
					}
				}else if(intent.getAction().equals("com.lansun.qmyo.refreshAvatar_NickName")){
					loadNickAndAvatar();
				}
				
				isFirstReceiveBroadcast=false;
			}
		}
	}
	@Override
	public void onDestroy() {
		activity.unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
	
	private void loadNickAndAvatar() {
		
		LogUtils.toDebugLog("userinfo", "init()方法里：App.app.getData(isExperience)=： "+App.app.getData("isExperience"));
		if(App.app.getData("isExperience").equals("true")){ //此处的isExperience是无用
			v.tv_mine_nickname.setText("请注册或登陆");
			
		}else{
			if (GlobalValue.user != null) {
				String avatar = GlobalValue.user.getAvatar();
				if (!TextUtils.isEmpty(avatar)) {
					//加载头像上去
					loadPhoto(avatar, v.iv_mine_head);
				}
				if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
					if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
						v.tv_mine_nickname.setText("请设置昵称");
						Log.i("Tag：nickName","NickName应该为设置昵称");
						
					}else{
						Log.i("Tag：nickName","NickName有值："+GlobalValue.user.getNickname());
						v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
					}
				}else{
					v.tv_mine_nickname.setText("请注册或登陆");
				}
			}else{   		 //--- >GlobalValue.user == null
				LogUtils.toDebugLog("userinfo", "init()方法里： GlobalValue.user为空");
				
				if(!TextUtils.isEmpty(App.app.getData("user_avatar"))&&!TextUtils.isEmpty(App.app.getData("user_nickname"))){
					loadPhoto(App.app.getData("user_avatar"), v.iv_mine_head);
					/*
					 * 下面的判断是针对由于强行退出程序，导致类GlobalValue的类数据丢失，在重新启动程序时，因为AccessTokenService的服务含有网络请求，导致获取user对象时，
					 	速度不及页面加载的速度，故在此针对user为空时，以防万一，将其设置为 ： 请设置昵称的提示语，
					 	实际上，此处的防护基本上用不到，
					 	因为在登录用户保持登录状态度的情况下，已将用户的头像和昵称的基本信息已存入本地保存起来，一旦启动程序时，都将从本地获取，
					 	代码并不会走到此处
					 */
					
					if(App.app.getData("user_nickname").equals("null")){
						v.tv_mine_nickname.setText("请设置昵称");
					}else{
						v.tv_mine_nickname.setText(App.app.getData("user_nickname"));
					}
					
					LogUtils.toDebugLog("userinfo", "init()方法里： App.app.getData(user_nickname)=： "+App.app.getData("user_nickname"));
				}else{
					LogUtils.toDebugLog("userinfo", "init()方法里：  GlobalValue.user为空，App.app.getData(user_nickname)为空，App.app.getData(user_avatar)为空");
				}
			}
		}
		//refreshCurrentList(GlobalValue.URL_USER_MESSAGE, null, 0, null);//去刷新消息
	}
}
