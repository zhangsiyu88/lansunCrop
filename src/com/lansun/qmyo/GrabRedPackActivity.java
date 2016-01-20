package com.lansun.qmyo;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import main.java.me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.google.gson.Gson;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ClickGoUrl;
import com.lansun.qmyo.domain.RedPackInfo;
import com.lansun.qmyo.domain.ShareRedPackInfo;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.GrabRedPackFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomDialogGrebRedpack;
import com.lansun.qmyo.view.CustomProgressDialog;
import com.lansun.qmyo.view.GrabRedPackOverDialog;
import com.lansun.qmyo.view.GrabRedPackSharedDialog;
import com.lansun.qmyo.view.ObservableWebView;
import com.lansun.qmyo.view.ObservableWebView.OnScrollChangedCallback;
import com.lansun.qmyo.view.RandomNumDialog;

public class GrabRedPackActivity extends SwipeBackActivity implements OnClickListener{

private static final String APP_CACAHE_DIRNAME = "/webcache";
private static final String TAG = GrabRedPackActivity.class.getSimpleName();
private static final int HAVE_OVER = 1;
private static final int HAVE_GOT = 2;

	//	@InjectAll
//	Views v;
//	private HomePromoteData promote;
//
//	class Views {
//		private ObservableWebView webView;
//		@InjectBinder(listeners = { OnClick.class }, method = "click")
//		private View ll_promote_detail_title;
//	}
	private ObservableWebView webView;
	private View ll_promote_detail_title;
	private String loadUrl;
	private LinearLayout iv_activity_back;
	private LinearLayout iv_activity_shared;
	private LinearLayout iv_activity_refresh;
	private ProgressBar pb_refresh;
	private Handler refreshHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(pb_refresh!=null && pb_refresh.getProgress()!=100){
				pb_refresh.setProgress(pb_refresh.getProgress()+(int)(Math.random()*10));
		     }
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			loadUrl = bundle.getString("loadUrl");
			
			loadUrl = "http://lansun.andrew.qmyo.net/redpack/1";
		}
		dialog = CustomDialogGrebRedpack.createDialog(this);
		setContentView(R.layout.activity_grab_red_pack_detail);
		
//		ViewParent parent = getWindow().getDecorView().getParent();
//		ViewGroup group = (ViewGroup) parent;
//		ViewGroup group = (ViewGroup) getWindow().getDecorView();
//		ViewGroup viewGroup = (ViewGroup) group.getChildAt(0);
//		TextView textView = new TextView(this);
//		textView.setText("新增TextView");
//		viewGroup.addView(textView);
		
		webView = (ObservableWebView)findViewById(R.id.webView);
		ll_promote_detail_title=  findViewById(R.id.ll_promote_detail_title);
		iv_activity_back = (LinearLayout) findViewById(R.id.iv_activity_back);
		iv_activity_shared = (LinearLayout) findViewById(R.id.iv_activity_shared);
		iv_activity_refresh = (LinearLayout) findViewById(R.id.iv_activity_refresh);
		pb_refresh = (ProgressBar) findViewById(R.id.pb_refresh);
//		refreshDialog = new CustomProgressDialog(this, "幸运红包搬运中...",R.anim.frame);
		setEdgeFromLeft();
		
		init();
		
	}

	private void init() {
		iv_activity_back.setOnClickListener(this);
		iv_activity_shared.setOnClickListener(this);
		iv_activity_refresh.setOnClickListener(this);
		
		initWebView();
	}

	
	
	private void initWebView() {
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		webView.setVerticalScrollBarEnabled(false); //垂直不显示
		//webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		
		// 用JavaScript调用Android函数：
        // 先建立桥梁类，将要调用的Android代码写入桥梁类的public函数
        // 绑定桥梁类和WebView中运行的JavaScript代码
        // 将一个对象起一个别名传入，在JS代码中用这个别名代替这个对象，可以调用这个对象的一些方法
//      webView.addJavascriptInterface(new WebAppInterface(this),"myInterfaceToAndroid");
//		webView.getSettings().setDefaultTextEncodingName("GBK");
		
		//1.初始化webView的数据库地址和文件地址
		// 开启 DOM storage API 功能  
        webView.getSettings().setDomStorageEnabled(true);  
        //开启 database storage API 功能  
        webView.getSettings().setDatabaseEnabled(true);  
        //开启 Application Caches 功能  
        webView.getSettings().setAppCacheEnabled(true);  
        
        String cacheDirPath = getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME;  
//      String cacheDirPath = getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;  
        Log.e(TAG, "cacheDirPath="+cacheDirPath);  
        //设置数据库缓存路径  
        webView.getSettings().setDatabasePath(cacheDirPath);  
        //设置  Application Caches 缓存目录  
        webView.getSettings().setAppCachePath(cacheDirPath);  
		//2.初始化
		
		webView.setOnKeyListener(new OnKeyListener( ){
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK&& webView.canGoBack()) {// 表示按返回键 时的操作
						webView.goBack(); //后退
						return true; // 已处理
					}
				}
				return false;
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				//endProgress();
				if(dialog.isShowing()){
					dialog.dismiss();
				}
//				if(refreshDialog!=null && refreshDialog.isShowing()){
//					refreshDialog.dismiss();
//				}
				if(pb_refresh!=null && pb_refresh.getProgress()!=0){
					pb_refresh.setProgress(100);
					pb_refresh.setVisibility(View.GONE);
				}
				super.onPageFinished(view, url);
			}
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.contains("lansunqmyo://maijieclient/?")){
					getUrlTailDict(url);
					return true;
				}
				if(url.contains("lansunqmyo://maijieclient/?")){
					getUrlTailDict(url);
					return true;
				}else{
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
		webView.setOnScrollChangedCallback(onScrollChangedCallback);
		webView.loadUrl(loadUrl);
//		webView.loadUrl("http://lansun.andrew.qmyo.net/redpack/1");
//		LogUtils.toDebugLog("html", "加载html页面");
	}


	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {//
			case 1:
				String result = r.getContentAsString();
				//1.1 返回的是随机串-->可以去领红包
				
				//对result进行解析
				Gson grabJson = new Gson();
				RedPackInfo redPackInfo = grabJson.fromJson(result, RedPackInfo.class);
				int statueTag = redPackInfo.getData();
				LogUtils.toDebugLog("result", "result: "+ statueTag);
				
				switch(statueTag){//statueTag
				    case -2://已领完
				    	//Toast.makeText(activity, "大大您来迟了，请等待下一大波红包到来~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog = new GrabRedPackOverDialog(this,webView,HAVE_OVER);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog.show(getSupportFragmentManager(), "grabredpack_isover");
					break;
				    case -1://未开始
		//		    	Toast.makeText(activity, "大大您来早了，活动还未开始呢~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog1 = new GrabRedPackOverDialog(this,webView);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog1.show(getSupportFragmentManager(), "grabredpackisover");
					break;
				    case 0://已领过
		//		    	Toast.makeText(activity, "大大您忘啦，您刚刚领过了~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog2 = new GrabRedPackOverDialog(this,webView,HAVE_GOT);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog2.show(getSupportFragmentManager(), "grabredpack_hasgot");
					break;
				   default://正常返回数据
					   RandomNumDialog dialog = new RandomNumDialog(this,String.valueOf(statueTag));//这么个体验的对话框，需要单独在其内部设置点击响应事件
					   dialog.show(getSupportFragmentManager(), "grabredpack");
					break;
		    }
		break;
		
	case 2:
		result = r.getContentAsString();
		Gson shareJson = new Gson();
		ShareRedPackInfo shareRedPackInfo = shareJson.fromJson(result, ShareRedPackInfo.class);
		LogUtils.toDebugLog("", r.getContentAsString());
			
		String title = shareRedPackInfo.getRedpack_title();
		String content = shareRedPackInfo.getRedpack_sub();
//		String imageUrl = "http://act.qmyo.com/images/redpack/pre-redpack.jpg";
		String currentActivityUrl = shareRedPackInfo.getShare_url();
		
		new GrabRedPackSharedDialog().showPopwindow(getWindow().getDecorView(), this, 
				title , 
				content ,
				"",
				currentActivityUrl);
		LogUtils.toDebugLog("Grab", "执行到分享这一步");
		
		break;
		//1.2 返回的不正确-->提示红包已被邻过
//		LogUtils.toDebugLog("返回数字", "返回数字"+result);
//		if(result.contains("false")){
//			Toast.makeText(activity, "小子，不要贪心~~", Toast.LENGTH_LONG).show();
//		}else{
//			result = result.substring(result.indexOf(":")+2, result.length()-2);
//			RandomNumDialog dialog = new RandomNumDialog(activity,result);//这么个体验的对话框，需要单独在其内部设置点击响应事件
//			//进来首先就弹出对话框
//			dialog.setOnConfirmListener(new OnConfirmListener(){
//				@Override
//				public void confirm() { }
//			});
//			dialog.show(getFragmentManager(), "grabredpack");
//			break;
//		}
			}
		}
	}
	
	public void setNewFrag(String url){
		LogUtils.toDebugLog("webview", url);
		LogUtils.toDebugLog("webview", "走到判断里来了");
		ActivityDetailFragment activtiFragment = new ActivityDetailFragment();
		Bundle args = new Bundle();
		args.putString("activityId","8147");
		args.putString("shopId", "116629");
		activtiFragment.setArguments(args);
		FragmentEntity fEntity = new FragmentEntity();
		fEntity.setFragment(activtiFragment);
		EventBus.getDefault().post(fEntity);
	}
	
	private void getUrlTailDict(String url){
		int headIndex = url.indexOf("{");
		//int tailIndex = url.indexOf("}");
		String urlTailStr = url.substring(headIndex, url.length()).replaceAll("\'","\"" );//lansunqmyo://maijieclient/?{http://act.qmyo.com/redpack/1}
		LogUtils.toDebugLog("webviewUrl", urlTailStr);
		Gson gson = new Gson();
		ClickGoUrl goUrlData = gson.fromJson(urlTailStr, ClickGoUrl.class);
		String activity_id = String.valueOf(goUrlData.getActivity_id());
		String shop_id = String.valueOf(goUrlData.getShop_id());
		int tag = goUrlData.getTag();
		
		if(tag == 9){
			ActivityDetailFragment activtiFragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("activityId",activity_id);
			args.putString("shopId",shop_id);
			LogUtils.toDebugLog("activityId", activity_id);
			LogUtils.toDebugLog("shopId", shop_id);
			/*args.putString("activityId","8147");
			args.putString("shopId", "116629");*/
			activtiFragment.setArguments(args);
			FragmentEntity fEntity = new FragmentEntity();
			fEntity.setFragment(activtiFragment);
			EventBus.getDefault().post(fEntity);
		}else if(tag==30){
			/*
			 *  "lansunqmyo://maijieclient/?{'tag':30,'type':'redpack'}"
			 *   打开红包的随机码的页面
			 */
			if("true".equals(App.app.getData("isExperience"))){
				Toast.makeText(this, "请登陆注册后,再抢红包大奖", Toast.LENGTH_LONG).show();
				Bundle bundle = new Bundle();
				bundle.putString("back", "yes");
				Intent intent = new Intent();
				intent.putExtras(bundle);
				setResult(1, intent);//Activity.RESULT_FIRST_USER
				finish();
			}else{
				//1.抢红包网址/*http://appapi.qmyo.org/redpack/key*/				
				//2.获取随机码
				///3.传入至Dialog页面
				InternetConfig config = new InternetConfig();
				config.setKey(1);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization", "Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK, null, config, this);
				/*FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK, null, config, this);*/
			}
		}else if(tag == 40){ //----------------------------------------->进行分享活动预告页面的点击事件
			   //获取分享内容的网址
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK_SHARE_CONTENT, null, config, this);
			}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.iv_activity_back:
			LogUtils.toDebugLog("finish", "GrebRedpack的Activity中" +"执行一次scrollToFinishActivity()");
			scrollToFinishActivity();
			finish();
			overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
			break;
		case R.id.iv_activity_refresh:
			LogUtils.toDebugLog("reload", "执行reload()方法");
			/*if(refreshDialog!=null){
				refreshDialog.show();
			}*/
			if(pb_refresh!=null){
				pb_refresh.setVisibility(View.VISIBLE);
				pb_refresh.setProgress( 1+(int)(Math.random()*30));
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						refreshHandler.sendEmptyMessage(0);
					}
				}, 100, 70);
			}
			webView.loadUrl(loadUrl);
			break;
		case R.id.iv_activity_shared:
			//NO-OP
			break;
		}
	}
	
	OnScrollChangedCallback onScrollChangedCallback = new OnScrollChangedCallback() {
		@Override
		public void onScroll(int dx, int dy) {
			if (dy > 0) {
				ll_promote_detail_title.setVisibility(View.GONE);
			} else {
				ll_promote_detail_title.setVisibility(View.VISIBLE);
			}
		}
	};
	private CustomDialogGrebRedpack dialog;
	private CustomProgressDialog refreshDialog;
	 /*public void onBackPressed() {
		 onClick(iv_activity_back);
	 }*/
	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_BACK) {
			 onClick(iv_activity_back);
			 return true;
		  }
		 return super.onKeyDown(keyCode, event);
		}
	
	 
	 @Override
		protected void onDestroy() {
			super.onDestroy();
			clearWebViewCache();
		}
	 
	   /** 
	     * 清除WebView缓存 
	     */  
	    public void clearWebViewCache(){  
	        //清理Webview缓存数据库  
	        try {  
	            deleteDatabase("webview.db");  
	            Log.e(TAG, "deleteDatabase webview.db");  
	            deleteDatabase("webviewCache.db");  
	            Log.e(TAG, "deleteDatabase webviewCache.db");  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");  
	        Log.e(TAG, "webviewCacheDir path="+webviewCacheDir.getAbsolutePath());  
	          
	        //WebView 缓存文件  
	        File appCacheDir = new File(getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME);  //APP_CACAHE_DIRNAME-->"/webcache"
	        Log.e(TAG, "appCacheDir path="+appCacheDir.getAbsolutePath());  
	       
	        //删除webview 缓存目录  
	        if(webviewCacheDir.exists()){  
	        	Log.e(TAG, "deleteFile webviewCacheDir");  
	            deleteFile(webviewCacheDir);  
	        }  
	        //删除webview 缓存 缓存目录  
	        if(appCacheDir.exists()){  
	            deleteFile(appCacheDir);  
	            Log.e(TAG, "deleteFile appCacheDir");  
	        }  
	    }  
	      
	    /** 
	     * 递归删除 文件/文件夹 
	     *  
	     * @param file 
	     */  
	    public void deleteFile(File file) {  
	        Log.i(TAG, "delete file path=" + file.getAbsolutePath());  
	        if (file.exists()) {  
	            if (file.isFile()) {  
	                file.delete();  
	            } else if (file.isDirectory()) {  
	                File files[] = file.listFiles();  
	                for (int i = 0; i < files.length; i++) {  
	                    deleteFile(files[i]);  
	                }  
	            }  
	            file.delete();  
	        } else {  
	            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());  
	        }  
	    }  
	    
	    /**
	     * 自定义的Android代码和JavaScript代码之间的桥梁类
	     * @author 1
	     */
	    public class WebAppInterface
	    {
	        Context mContext;

	        /** Instantiate the interface and set the context */
	        WebAppInterface(Context c)
	        {
	            mContext = c;
	        }

	        /** Show a toast from the web page */
	        // 如果target 大于等于API 17，则需要加上如下注解
	        @JavascriptInterface
	        public void reload(){
	        	Toast.makeText(mContext, "重新加载咯！", Toast.LENGTH_LONG).show();
	        	webView.loadUrl(loadUrl);
	        }
	    }
}
