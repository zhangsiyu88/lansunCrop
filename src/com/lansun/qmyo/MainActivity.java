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
import cn.jpush.android.api.JPushInterface;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectLayer;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.biz.ServiceAllBiz;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ExperienceSearchFragment;
import com.lansun.qmyo.fragment.FoundFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.HomeFragmentOld;
import com.lansun.qmyo.fragment.IntroductionPageFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SearchBankCardFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.fragment.TestMineActivityFragment;
import com.lansun.qmyo.service.AccessTokenService;
import com.lansun.qmyo.service.LocationService;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.ExampleUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
@InjectLayer(R.layout.activity_main)
public class MainActivity extends FragmentActivity {
	private FragmentTransaction fragmentTransaction;
	public static boolean isForeground = false;
	@Override
	protected void onCreate(@Nullable Bundle arg0) {
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
		super.onCreate(arg0);
	}
	
	@InjectInit
	private void init() {
		EventBus eventBus = EventBus.getDefault();
		eventBus.register(this);

		if (TextUtils.isEmpty(App.app.getData("isFirst"))) {
			startFragmentAdd(new IntroductionPageFragment());
		} else {
			/*startFragmentAdd(new HomeFragment());    */                 //--------------------->by Yeun 11.16//TODO
			/*startFragmentAdd(new HomeFragmentOld());*/					//--------------------->by Yeun 11.13//TODO
			/*startFragmentAdd(new MainFragment());*/
			startFragmentAdd(new TestMineActivityFragment());
			
			getTokenService();
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
		super.onPause();
	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		isForeground = false;
		super.onResume();
	}

	/**
	 * 获取token的服务
	 */
	private void getTokenService() {
		Intent service = new Intent(this, AccessTokenService.class);
		startService(service);
		Intent locationService = new Intent(this, LocationService.class);
		startService(locationService);
	}
	
	

	public  void startFragmentAdd(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentTransaction = fragmentManager
				.beginTransaction();
		Fragment to_fragment = fragmentManager.findFragmentByTag(fragment
				.getClass().getName());
		//
		// fragmentTransaction.setCustomAnimations(R.anim.left_in,
		// R.anim.left_out, R.anim.right_in, R.anim.right_out);

		/*将这里的动画效果取消掉，即隐去淡入淡出的效果
		 * fragmentTransaction.setCustomAnimations(R.anim.fade_in,
					R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);*/
		
		if (to_fragment != null) {
			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
				BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
				if (fragment.getClass().getName().equals(entry.getName())) {
					fragmentManager.popBackStack(entry.getName(), 1);
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
				DialogUtil.createTipAlertDialog(this, R.string.is_exit,
						new TipAlertDialogCallBack() {
							@Override
							public void onPositiveButtonClick(
									DialogInterface dialog, int which) {
								dialog.dismiss();
								/*//直接将进程关掉
								android.os.Process.killProcess(android.os.Process.myPid());*/
								MainActivity.this.finish();
							}

							@Override
							public void onNegativeButtonClick(
									DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				return true;
			} else if (fragment.getClass().getName()
					.equals(HomeFragment.class.getName())||fragment.getClass().getName()
					.equals(FoundFragment.class.getName())||fragment.getClass().getName()
					.equals(MineFragment.class.getName())||fragment.getClass().getName()
					.equals(SecretaryFragment.class.getName())||fragment.getClass().getName()
					.equals(MainFragment.class.getName())) {

				DialogUtil.createTipAlertDialog(MainActivity.this,R.string.is_exit, new TipAlertDialogCallBack() {

							@Override
							public void onPositiveButtonClick(
									DialogInterface dialog, int which) {
								/*finish();*/
								dialog.dismiss();
								/*onPause();*/
								//在四大板块上时，需要在点击确定按钮时，执行onDestory()的操作
								/*fragmentTransaction = null;*/
								/*onDestroy();*/
								finish();
							}

							@Override
							public void onNegativeButtonClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});

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
		
		/*App.app.setData("isExperience","true");
		GlobalValue.isFirst = false;
		App.app.setData("access_token", "");*/
		/*App.app.setData("exp_secret","dypesq1zsn");
		App.app.setData("isExperience","");*/
		unregisterReceiver(mMessageReceiver);
		EventBus eventBus = EventBus.getDefault();
		eventBus.unregister(this);
		
		if(App.app.getData("isExperience")=="true"){
			System.out.println("走到了onDestory!");
			App.app.setData("access_token", "");
			
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
		@Override
		protected void onRestoreInstanceState(Bundle savedInstanceState) {
			super.onRestoreInstanceState(savedInstanceState);
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
}
