package com.lansun.qmyo;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectLayer;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.ExperienceSearchFragment;
import com.lansun.qmyo.fragment.FoundFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.HomeFragmentOld;
import com.lansun.qmyo.fragment.IntroductionPageFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.fragment.PersonCenterFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SearchBankCardFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.fragment.StoreDetailFragment;
import com.lansun.qmyo.fragment.TestMineActivityFragment;
import com.lansun.qmyo.service.AccessTokenService;
import com.lansun.qmyo.service.LocationService;
import com.lansun.qmyo.utils.CommitStaticsinfoUtils;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.ExampleUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.nostra13.universalimageloader.core.ImageLoader;
@InjectLayer(R.layout.activity_main)
public class MainActivity extends FragmentActivity {
	private FragmentTransaction fragmentTransaction;
	public static boolean isForeground = false;
	private long exitTime = 0;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		
		
		
		
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		
		//注册一个广播接收者
		registerMessageReceiver();
		
		
		/*TextView mImei = (TextView) findViewById(R.id.tv_imei);*/
		String udid =  ExampleUtil.getImei(getApplicationContext(), "");
        if (null != udid) 
        Log.i("IMEI", "IMEI: " + udid);
        
		/*TextView mAppKey = (TextView) findViewById(R.id.tv_appkey);*/
		String appKey = ExampleUtil.getAppKey(getApplicationContext());
		if (null == appKey) appKey = "AppKey异常";
		Log.i("AppKey: ", "AppKey:" + appKey);

		String packageName =  getPackageName();
		/*TextView mPackage = (TextView) findViewById(R.id.tv_package);*/
		/*mPackage.setText("PackageName: " + packageName);*/
		String versionName =  ExampleUtil.GetVersion(getApplicationContext());
		/*TextView mVersion = (TextView) findViewById(R.id.tv_version);*/
		/*mVersion.setText("Version: " + versionName);*/
		
		
		//若发现权限被关闭后，那么进入之后会弹出开启定位权限的提醒
		int checkCallingOrSelfPermission = getApplication().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION");
		
		if (-1 ==checkCallingOrSelfPermission){
			
		      DialogUtil.createTipAlertDialog(this, "定位可以激发小宇宙！", new DialogUtil.TipAlertDialogCallBack(){
		    	  
		        public void onNegativeButtonClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){
		          paramAnonymousDialogInterface.dismiss();
		          App.app.setData("cityCode", "310000");
		          App.app.setData("cityName", "上海市");
		          App.app.setData("select_cityCode", "310000");
		          App.app.setData("select_cityName", "上海市");
		        }
		        public void onPositiveButtonClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){//确定
		          paramAnonymousDialogInterface.dismiss();
		          Intent localIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
		          localIntent.setData(Uri.fromParts("package", "com.qmyo.activity", null));
		          MainActivity.this.startActivity(localIntent);//前往权限设置的页面
		        }
		      });
		}else{
			System.out.println("android.permission.ACCESS_FINE_LOCATION已经打开！"+checkCallingOrSelfPermission);
		}
		
		super.onCreate(savedInstanceState);
	}
	
	@InjectInit
	private void init() {
		EventBus eventBus = EventBus.getDefault();
		eventBus.register(this);

		if (TextUtils.isEmpty(App.app.getData("isFirst"))) {
			//一进来，首先开启后台的两个服务
			accesstokenService = new Intent(this, AccessTokenService.class);
			startService(accesstokenService);
			locationService = new Intent(this, LocationService.class);
			startService(locationService);
			
			startFragmentAdd(new IntroductionPageFragment());
			
		} else {
			getTokenService();
			/*startFragmentAdd(new HomeFragment());    */                 //--------------------->by Yeun 11.16//TODO
			/*startFragmentAdd(new HomeFragmentOld());*/					//--------------------->by Yeun 11.13//TODO
//			startFragmentAdd(new MainFragment());
			/*startFragmentAdd(new TestMineActivityFragment());*/
			
			
		}
		
		
		//PullTorefreshManager中就已经获取到了界面内容
		PullToRefreshManager.getInstance().setRefreshLayout(
				R.layout.refresh_header);
		int[] headerIds = new int[] {R.id.mHeaderImageView,
				R.id.mHeaderArrowImageView, R.id.mHeaderProgressBar,
				R.id.mHeaderTextView };
		PullToRefreshManager.getInstance().setHeaderIds(headerIds);

		// 设置底部
		PullToRefreshManager.getInstance().setRefreshFooterLayout(
				R.layout.refresh_footer);
		int[] footerIds = new int[] { R.id.mFooterImageView,
				R.id.mFooterProgressBar, R.id.mFooterTextView };
		PullToRefreshManager.getInstance().setFooterIds(footerIds);
	}

	@Override
	protected void onPause() {
		JPushInterface.onPause(this);
		isForeground = true;
		//ImageLoader.getInstance().clearMemoryCache();  
		ImageLoader.getInstance().clearDiskCache();  //清除imageloader在Disk中的缓存
		LogUtils.toDebugLog("ImageLoader", "ImageLoader清除掉Disk缓存");
		
		if(App.app.getData("firstUseApp")=="true"){
			if(locationService!=null){
				stopService(locationService);
				LogUtils.toDebugLog("firstUseApp", "退至后台，关闭定位服务");
			}
		}
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		isForeground = false;
		//getTokenService();
		
		if(App.app.getData("firstUseApp")=="true"){
			if(locationService!=null){
				startService(locationService);
				LogUtils.toDebugLog("firstUseApp", "回到前台，重新启动定位服务，加速");
			}
		}
		super.onResume();
	}

	/**
	 * 获取token的服务
	 */
	private void getTokenService() {
		accesstokenService = new Intent(this, AccessTokenService.class);
		startService(accesstokenService);
		LogUtils.toDebugLog("accesstokenService", "accesstokenService正常启动");

		LogUtils.toDebugLog("location", "locationService正常启动");
		locationService = new Intent(this, LocationService.class);
		startService(locationService);
		
		startFragmentAdd(new MainFragment());
	}
	
	

	public  void startFragmentAdd(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentTransaction = fragmentManager
				.beginTransaction();
		Fragment to_fragment = fragmentManager.findFragmentByTag(fragment
				.getClass().getName());
		
		
		
	// fragmentTransaction.setCustomAnimations(R.anim.left_in,R.anim.left_out, R.anim.right_in, R.anim.right_out);
		/*将这里的动画效果取消掉，即隐去淡入淡出的效果
		 * */
	 //fragmentTransaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
		
		if (to_fragment != null) {
			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
				BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
				/**
				 * 针对活动详情页面和门店详情页面的跳转逻辑，实行类Activity中的standard的启动机制,
				 * 其余页面实行正常的逻辑，找到一样类型的类将其弹出，新生成的对象入栈
				 */
				if(fragment.getClass().getName().equals(ActivityDetailFragment.class.getName())||
						fragment.getClass().getName().equals(StoreDetailFragment.class.getName())){
					//NO_OP
				}else{
					if (fragment.getClass().getName().equals(entry.getName())) {
						fragmentManager.popBackStack(entry.getName(), 1);
					}
				}
				
			}
		}
		fragmentTransaction.addToBackStack(fragment.getClass().getName());
		fragmentTransaction.add(R.id.content_frame, fragment, fragment.getClass().getName());
		fragmentTransaction.commitAllowingStateLoss();
		/**
		 * 在此地方启动了一个服务来获取8大板块的导航
		 * 考虑在点击进入8大板块的里面进行判断然后终止服务
		 */
		/*fragmentTransaction.replace(R.id.content_frame, fragment,
				fragment.getClass().getName()).commitAllowingStateLoss();*/
	}

	
	//将重写的方法onBackPressed的方法给禁掉
	@Override
	public void onBackPressed() {
		
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		
		if (fragment.getClass().getName().equals(RegisterFragment.class.getName())) {
			
			if(App.app.getData("isResetPsw").equals("true")){
				CustomToast.show(this, "迈界小贴士", "更改新密码后，请登录");
				return;
			}
			
			if(App.app.getData("isEmbrassStatus").equals("true") ){
				CustomToast.show(this, "抱歉,请点击登录", "请至少选择一张银行卡作为通行证");
				return;
			}
			if(GlobalValue.user==null && GlobalValue.isFirst){
				Log.i("物理的back键", "物理返回键的返回操作被转换效果");
				ExperienceSearchFragment experienceFragment = new ExperienceSearchFragment();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(experienceFragment);
				EventBus.getDefault().post(entity);
				return;
			}else {
				getSupportFragmentManager().popBackStack();
				return;
			}
		}else if(fragment.getClass().getName().equals(SearchBankCardFragment.class.getName())){
			if(App.app.getData("isEmbrassStatus").equals("true") ){
				CustomToast.show(this, "抱歉,请选择银行卡", "请至少选择一张银行卡作为通行证");
				return;
			}
		}else if(fragment.getClass().getName().equals(PersonCenterFragment.class.getName())){
			FragmentEntity entity=new FragmentEntity();
			MainFragment mainFragment=new MainFragment(3);
			entity.setFragment(mainFragment);
			EventBus.getDefault().post(entity);
			LogUtils.toDebugLog("个人信息页的返回按钮和物理返回键的点击事件", "跳转至我的页面");
			return;
		}
		super.onBackPressed();
		/*else if(fragment.getClass().getName().equals(getSupportFragmentManager().findFragmentByTag("experience"))){
			if(GlobalValue.user==null && !GlobalValue.isFirst){//在首页，且还没有拿到体验用户的那张卡，那么需要将物理返回键去除掉，强行要求进行填卡操作者，避免三无状态有机会存在
				//DoNothing !!！
				Log.i("物理的back键", "首页界面里的  物理返回键的返回操作被转出的换效果");
				ExperienceSearchFragment experienceFragment = new ExperienceSearchFragment();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(experienceFragment);
				EventBus.getDefault().post(entity);
			}
		}*/
	 }
	
	//此方法是重写了Activity(Fragment)的
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
			
			if (fragment.getClass().getName()
					.equals(ExperienceSearchFragment.class.getName())) {
//				DialogUtil.createTipAlertDialog(this, R.string.is_exit,
//						new TipAlertDialogCallBack() {
//							@Override
//							public void onPositiveButtonClick(
//									DialogInterface dialog, int which) {
//								dialog.dismiss();
//								/*//直接将进程关掉
//								android.os.Process.killProcess(android.os.Process.myPid());*/
//								MainActivity.this.finish();
//							}
//
//							@Override
//							public void onNegativeButtonClick(
//									DialogInterface dialog, int which) {
//								dialog.dismiss();
//							}
//						});
				
				if((System.currentTimeMillis()-exitTime ) > 1000){  
		            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
		            exitTime = System.currentTimeMillis();   
		        } else {
		        	MainActivity.this.finish();
		        }
				
				return true;
			} else if (fragment.getClass().getName()
					.equals(HomeFragment.class.getName())||fragment.getClass().getName()
					.equals(FoundFragment.class.getName())||fragment.getClass().getName()
					.equals(MineFragment.class.getName())||fragment.getClass().getName()
					.equals(SecretaryFragment.class.getName())||fragment.getClass().getName()
					.equals(MainFragment.class.getName())) {

//				DialogUtil.createTipAlertDialog(MainActivity.this,R.string.is_exit, new TipAlertDialogCallBack() {
//
//							@Override
//							public void onPositiveButtonClick(
//									DialogInterface dialog, int which) {
//								/*finish();*/
//								dialog.dismiss();
//								/*onPause();*/
//								//在四大板块上时，需要在点击确定按钮时，执行onDestory()的操作
//								/*fragmentTransaction = null;*/
//								/*onDestroy();*/
//								finish();
//							}
//
//							@Override
//							public void onNegativeButtonClick(DialogInterface dialog, int which) {
//								dialog.dismiss();
//							}
//						});
				
				
				if((System.currentTimeMillis()-exitTime ) > 1000){  
		            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
		            exitTime = System.currentTimeMillis();   
		        } else {
		        	MainActivity.this.finish();
		        }
				return true;
			}/*else if(fragment.getClass().getName()
					.equals(ExperienceDialog.class.getName())){//当Fragment为ExperienceDialog时，取消返回键效果
				return true;
			}*/
		}
		  return super.onKeyDown(keyCode, event);
	}

	
	/**主线程的开启fragment事件
	 *
	 */
	public void onEventMainThread(FragmentEntity event) {
		startFragmentAdd(event.getFragment());
	}
	

	
	@Override
	protected void onDestroy() {
		
		
		GlobalValue.commitedStatisticsInfo_Login=false;
		
		/*App.app.setData("isExperience","true");
		GlobalValue.isFirst = false;
		App.app.setData("access_token", "");*/
		/*App.app.setData("exp_secret","dypesq1zsn");
		App.app.setData("isExperience","");*/
		
		if(App.app.getData("firstUseApp")=="true"){
			App.app.setData("firstUseApp","");
			LogUtils.toDebugLog("firstUseApp", "正常退出App，结束使用者的App首秀");
		}
		
		
		ImageLoader.getInstance().clearMemoryCache();  
		ImageLoader.getInstance().clearDiskCache();  
		LogUtils.toDebugLog("ImageLoader", "ImageLoader清除掉缓存");
		
		GlobalValue.isWaitingForUpdateApp =  true;
		
		unregisterReceiver(mMessageReceiver);
		EventBus eventBus = EventBus.getDefault();
		eventBus.unregister(this);
		
		/**
		 * 离开程序时，将定位服务给禁掉
		 */
		
		if(locationService!=null){
			stopService(locationService);
			LogUtils.toDebugLog("location", "locationService被停止掉");
		}
		if(accesstokenService!=null){
			stopService(accesstokenService);
			LogUtils.toDebugLog("accesstokenService", "accesstokenService被停止掉");
		}
		App.app.setData("toUpdateApp","");
		LogUtils.toDebugLog("toUpdateApp", "toUpdateApp重新设置为 空！保证了版本更新弹窗在App打开的第一次仅一次的有效性");
		
		
		/*
		 * 体验用户离开此次应用后，会将token置为空，为啥？
		 */
		if(App.app.getData("isExperience")=="true"){
			System.out.println("走到了onDestory!暂时未清除临时用户的token");
			//App.app.setData("access_token", "");
			
			
		/* App.app.setData("exp_secret","dypesq1zsn");*/
			//GlobalValue.isFirst = false;
				/*App.app.setData("isExperience","");
				  GlobalValue.isFirst = false;
				  App.app.setData("exp_secret","");为评测过期exp_secret是否可以自己去获取全新secret，暂时关闭*/
			
		}
		super.onDestroy();
	}

	//下面是极光的广播接收者!!!
	//for receive customer msg from jpush server
		private MessageReceiver mMessageReceiver;
		public static final String MESSAGE_RECEIVED_ACTION = "com.qmyo.activity.MESSAGE_RECEIVED_ACTION";
		public static final String KEY_TITLE = "title";
		public static final String KEY_MESSAGE = "message";
		public static final String KEY_EXTRAS = "extras";
		private Intent intent;
		private Intent locationService;
		private Intent accesstokenService;
		
		public void registerMessageReceiver() {
			mMessageReceiver = new MessageReceiver();
			IntentFilter filter = new IntentFilter();
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			filter.addAction(MESSAGE_RECEIVED_ACTION);
			registerReceiver(mMessageReceiver, filter);
		}

		
		
		public class MessageReceiver extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
	              String messge = intent.getStringExtra(KEY_MESSAGE);
	              String extras = intent.getStringExtra(KEY_EXTRAS);
	              StringBuilder showMsg = new StringBuilder();
	              showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
	              if (!ExampleUtil.isEmpty(extras)) {
	            	  showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
	              }
	              Log.i("由极光前来推送接收到的消息为： ", showMsg.toString());
	              
	              sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));//LGP:Little Green Point
			      LogUtils.toDebugLog("infos", "接收到推送消息后，发送广播");
	              
	              
	              //setCostomMsg(showMsg.toString());
				}
			}
		}
		/*private void setCostomMsg(String msg){
			 if (null != msgText) {
				 msgText.setText(msg);
				 msgText.setVisibility(android.view.View.VISIBLE);
	         }
		}*/
		
		@Override
		public boolean onSearchRequested() {
			return super.onSearchRequested();
		}
		
		
		/* protected void onSaveInstanceState(Bundle outState) {
			  outState.putString(loginname, App.app.LOGINNAME);
			  outState.putInt(classId, Application.classId);
			  outState.putSerializable(classinfos, (ArrayList<classinfo>)App.app.getInstance().getClassInfos());
			  super.onSaveInstanceState(outState);
			 }
			 
			 @Override
			 protected void onRestoreInstanceState(Bundle savedInstanceState) {
			  super.onRestoreInstanceState(savedInstanceState);
			  if (savedInstanceState != null) {
			   Application.LOGINNAME = savedInstanceState.getString(loginname);
			   Application.classId = savedInstanceState.getInt(classId);
			   Application.getInstance().setClassInfos((List<classinfo>)savedInstanceState.getSerializable(classinfos));
			  }
			 }*/
		
		/**
		 * 存储Activity被清除那一瞬之前的希望保留的内容
		 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
		 */
		@Override
		protected void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			
			
		}
		
		/**
		 * (non-Javadoc)
		 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
		 */
		@Override
		protected void onRestoreInstanceState(Bundle savedInstanceState) {
			super.onRestoreInstanceState(savedInstanceState);
		}
		
}
