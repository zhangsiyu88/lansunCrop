package com.lansun.qmyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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
import com.lansun.qmyo.listener.ToLoginListener;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomDialogGrebRedpack;
import com.lansun.qmyo.view.CustomDialogGrebRedpackWait;
import com.lansun.qmyo.view.GrabRedPackOverDialog;
import com.lansun.qmyo.view.GrabRedPackSharedDialog;
import com.lansun.qmyo.view.ObservableWebView;
import com.lansun.qmyo.view.ObservableWebView.OnScrollChangedCallback;
import com.lansun.qmyo.view.RandomNumDialog;
import com.lansun.qmyo.view.RandomNumDialog.onDismissListener;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

@SuppressLint("SetJavaScriptEnabled") 
public class GrabRedPackActivity extends FragmentActivity implements OnClickListener,onDismissListener{//SwipeBackActivity

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
	public ToLoginListener mToLoginListener;
	private ObservableWebView webView;
//	private WebView webView;
	private View ll_promote_detail_title;
	private String loadUrl;
	private LinearLayout iv_activity_back;
	private LinearLayout iv_activity_shared;
	private LinearLayout iv_activity_refresh;
	private ProgressBar pb_refresh;
	private long lastClickTime = System.currentTimeMillis();
	String htmlContent;
	private Handler refreshHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			switch(msg.what){
			case 0:
				if(pb_refresh!=null && pb_refresh.getProgress()!=100){
					pb_refresh.setProgress(pb_refresh.getProgress()+(int)(Math.random()*10));
				}
				break;
			case 1:
				htmlContent = htmlContent.replace("\"", "\'");
			    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
                break;
			case 2:
//				if(refreshDialog!=null&&refreshDialog.isShowing()){
//					refreshDialog.dismiss();
//				}
				RandomNumDialog dialog = new RandomNumDialog(GrabRedPackActivity.this,statueTag);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				dialog.show(getSupportFragmentManager(), "grabredpack");
				
				break;
			}
			hadDone = false;
//			lastClickTime = System.currentTimeMillis()-2*1000;//为了保证当Dialog关闭掉后，能够迅速响应按钮的有可能即将进行的一次点击事件
		}
	};
	private String currentPage = null;
	
	
	
	public GrabRedPackActivity(){
		
	}
	/**
	 * 将Activity传入进来
	 * @param act
	 */
	public GrabRedPackActivity(ToLoginListener mainActivity){
		mToLoginListener = ((ToLoginListener) mainActivity);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			loadUrl = bundle.getString("loadUrl");
//			loadUrl = "http://lansun.andrew.qmyo.net/redpack/1";//--------------------------------------------------->guanbi
		}
		dialog = CustomDialogGrebRedpack.createDialog(this);
		setContentView(R.layout.activity_grab_red_pack_detail);
//		getWindow().requestFeature(Window.FEATURE_PROGRESS);  
		webView = (ObservableWebView) findViewById(R.id.webView);
		ll_promote_detail_title=  findViewById(R.id.ll_promote_detail_title);
		iv_activity_back = (LinearLayout) findViewById(R.id.iv_activity_back);
		iv_activity_shared = (LinearLayout) findViewById(R.id.iv_activity_shared);
		iv_activity_refresh = (LinearLayout) findViewById(R.id.iv_activity_refresh);
		pb_refresh = (ProgressBar) findViewById(R.id.pb_refresh);
//		setEdgeFromLeft();
		init();
	}

	private void init() {
		iv_activity_back.setOnClickListener(this);
		iv_activity_shared.setOnClickListener(this);
		iv_activity_refresh.setOnClickListener(this);
		initWebView();
	}
	@SuppressWarnings("deprecation")
	private void initWebView() {
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		webView.setVerticalScrollBarEnabled(false); //垂直不显示
		// 用JavaScript调用Android函数：
        // 先建立桥梁类，将要调用的Android代码写入桥梁类的public函数
        // 绑定桥梁类和WebView中运行的JavaScript代码
        // 将一个对象起一个别名传入，在JS代码中用这个别名代替这个对象，可以调用这个对象的一些方法
//      webView.addJavascriptInterface(new WebAppInterface(this),"myInterfaceToAndroid");
		webView.getSettings().setDefaultTextEncodingName("UTF-8");
		
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
						
						//因当前webView只有一层界面，故可以在此统一执行finish的操作
						GrabRedPackActivity.this.onClick(iv_activity_back);
						return true; // 已处理
					}
				}
				return false;
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			
			private int times = 1;
			@Override
			public void onPageFinished(WebView view, String url) {
				//endProgress();
				if(dialog!=null && dialog.isShowing()){
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
				LogUtils.toDebugLog("hadDone", "点击次数：  "+ times ++);
				if(!hadDone){//若未曾点击过，即响应
					if(url.contains("lansunqmyo://maijieclient/?")){
						   long diffTime = System.currentTimeMillis() - lastClickTime;
						   lastClickTime = System.currentTimeMillis();
					       if(diffTime >= 2000){
					           hadDone = true;
					           LogUtils.toDebugLog("hadDone", "访问次数： "+"1");
					           getUrlTailDict(url);
					           return true;
					       }else{
					    	   return true;
					       }
					}else{
						return super.shouldOverrideUrlLoading(view, url);
					}
		        }else{
		        	return true;
			   }
			}
		});
		webView.setOnScrollChangedCallback(onScrollChangedCallback);
		
//		testLocalPort();//--------------------------------------------------------------------------->guanbi
		
		
		webView.loadUrl(loadUrl);
	}

	 public String convertToString(InputStream inputStream) {
	        StringBuffer string = new StringBuffer();
	        BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream));
	        String line;
	        try {
	            while ((line = reader.readLine()) != null) {
	                string.append(line + "\n");
	            }
	        } catch (IOException e) {
	        }
	        return string.toString();
	    }
	
	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {//
			case 1:
				String result = r.getContentAsString();
				//1.1 返回的是随机串-->可以去领红包
				LogUtils.toDebugLog("result", "result=: "+ result );
				//对result进行解析
				Gson grabJson = new Gson();
				RedPackInfo redPackInfo = grabJson.fromJson(result, RedPackInfo.class);
				statueTag  = redPackInfo.getData();
				LogUtils.toDebugLog("result", "result: "+ statueTag );
				switch(statueTag){//statueTag
				    case "-2"://已领完
				    	GrabRedPackOverDialog overDialog = new GrabRedPackOverDialog(this,webView,HAVE_OVER);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog.show(getSupportFragmentManager(), "grabredpack_isover");
					break;
				    case "-1"://未开始,也显示为已领完
				    	GrabRedPackOverDialog overDialog1 = new GrabRedPackOverDialog(this,webView);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog1.show(getSupportFragmentManager(), "grabredpackisover");
					break;
				    case "0"://已领过
				    	GrabRedPackOverDialog overDialog2 = new GrabRedPackOverDialog(this,webView,HAVE_GOT);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog2.show(getSupportFragmentManager(), "grabredpack_havegot");
					break;
				   default://正常返回数据
					   new Timer().schedule(new TimerTask(){
						@Override
						public void run() {
							refreshHandler.sendEmptyMessage(2);
						}
					}, 10);
					break;
		    }
		break;
		
	case 2:
//		   statueTag  = "8rh62G";
//		   new Timer().schedule(new TimerTask(){
//			@Override
//			public void run() {
//				refreshHandler.sendEmptyMessage(2);
//			}
//		}, 100);
		
		result = r.getContentAsString();
		Gson shareJson = new Gson();
		ShareRedPackInfo shareRedPackInfo = shareJson.fromJson(result, ShareRedPackInfo.class);
		LogUtils.toDebugLog("", r.getContentAsString());
		String wechat_title = shareRedPackInfo.getWechat_title();
		String wechat_sub = shareRedPackInfo.getWechat_sub();
		String weibo_title = shareRedPackInfo.getWeibo_title();
		String currentActivityUrl = shareRedPackInfo.getShare_url();
		
		new GrabRedPackSharedDialog().showPopwindow(getWindow().getDecorView(), this, 
				wechat_title , 
				wechat_sub ,
				weibo_title,
				"",
				currentActivityUrl);
		LogUtils.toDebugLog("Grab", "执行到分享这一步");
		break;
		}
		hadDone = false;	//恢复按钮可以被再次点击的效果
		currentPage = null;
		
	}else if(r.getStatus() == FastHttp.result_net_err){
			dialog.setText("网络异常，请检查网络");
			noNetClickView = dialog.getImageView();
			noNetClickView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					GrabRedPackActivity.this.onClick(iv_activity_refresh);
					dialog.setText("幸运红包运输中");
				}
			});
			
		}
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
//				Bundle bundle = new Bundle();
//				bundle.putString("back", "yes");
//				Intent intent = new Intent();
//				intent.putExtras(bundle);
//				setResult(1, intent);//Activity.RESULT_FIRST_USER
				
				//此处换用接口的形式进行操作
//				mToLoginListener.toLoginForGrabRedpack();
				//发送广播通知MainActivity跳转至注册页面
				this.sendBroadcast(new Intent("com.lansun.qmyo.toRegisterPage"));
				
				finish();
			}else{
				//1.抢红包网址/*http://appapi.qmyo.org/redpack/key*/				
				//2.获取随机码
				///3.传入至Dialog页面
				if(currentPage==null&&hadDone==true){
					hadDone=false;
					currentPage = GlobalValue.GRAB_RED_PACK;
					InternetConfig config = new InternetConfig();
					config.setKey(1);
					HashMap<String, Object> head = new HashMap<>();
					head.put("Authorization", "Bearer " + App.app.getData("access_token"));
					config.setHead(head);
					FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK, null, config, this);
//					FastHttpHander.ajaxGet("http://api.andrew.qmyo.net/redpack/key", null, config, this);
				}
			}
		}else if(tag == 40){ //----------------------------------------->进行分享活动预告页面的点击事件
			/*"lansunqmyo://maijieclient/?{'tag':40,'type':'share'}"*/

			//获取分享内容的网址
			if(currentPage==null&&hadDone==true){
				currentPage = GlobalValue.GRAB_RED_PACK_SHARE_CONTENT;
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK_SHARE_CONTENT, null, config, this);
				LogUtils.toDebugLog("send", "发送获取分享内容的请求");
			}
		   }
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.iv_activity_back:
			LogUtils.toDebugLog("finish", "GrebRedpack的Activity中" +"执行一次scrollToFinishActivity()");
//			scrollToFinishActivity();
			finish();
//			overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);//导致在GrebRedPackActivity在finish()后，回留有一个透明层在MainActivity的表面
			break;
		case R.id.iv_activity_refresh:
			LogUtils.toDebugLog("reload", "执行reload()方法");
			if(pb_refresh!=null){
				pb_refresh.setVisibility(View.VISIBLE);
				pb_refresh.setProgress( 1+(int)(Math.random()*30));
				Timer timer = new Timer();
				timer.schedule(new TimerTask(){
					@Override
					public void run(){
						refreshHandler.sendEmptyMessage(0);
					}
				}, 100, 70);
			}
//			hashMap = new HashMap<>();
//			hashMap.put("User-Agent","Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
//			webView.loadUrl(loadUrl,hashMap);
			
//			webView.reload();
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
	/*
	 * 点击刷新按钮的产生的dialog
	 */
	/*private CustomProgressDialog refreshDialog;*/
	private HashMap<String,String> hashMap;
	private View noNetClickView;
	private String statueTag = "-2";
	private CustomDialogGrebRedpackWait refreshDialog;
	private boolean hadDone = false;
	
	/*  webView的Client 中首先就拦截着 物理返回键
	 * @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_BACK) {
			 GrabRedPackActivity.this.onClick(iv_activity_back);
			// finish();
			 return true;
		  }
		 return super.onKeyDown(keyCode, event);
		}*/
	 
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
	        /** 清除WebView中的 cookie*/
	        CookieSyncManager.createInstance(this);   
	        CookieSyncManager.getInstance().startSync();   
	        CookieManager.getInstance().removeSessionCookie(); 
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
	    
	    
	    private final class WebChromeClientExtension extends WebChromeClient {
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
	    
	    /**
		 * 测试服务器本地的数据内容
		 */
		private void testLocalPort() {
			Log.d("info", "===>>> shouldOverrideUrlLoading method is called!");
	        URL local_url;
	        URLConnection connection;
	        try {
	        	 final String url = "http://lansun.andrew.qmyo.net/redpack/1";
	             local_url = new URL(url);
//	           local_url = new URL();
	            connection = local_url.openConnection();
	            connection.setConnectTimeout(15000);
	            connection.connect();
	        } catch (Exception e) {}

	        final HttpRequest httpGet = new HttpRequest(HttpMethod.GET,"http://lansun.andrew.qmyo.net/redpack/1");
//	      final HttpGet httpGet = new HttpGet("http://lansun.andrew.qmyo.net/redpack/1");
	        
	       httpGet.setHeader("User-Agent","Mozilla/4.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
	       
	        if (httpGet!= null ){
	            Header[] headers = (Header[])httpGet.getAllHeaders();
	            for (Header header : headers) {
	                String name  = header.getName();
	                String value = header.getValue();
	                Log.d("request_info", "===>>> name:" + name);
	                Log.d("request_info", "===>>> value:" + value);
	            }
	            
	            
	            HttpEntity entity = httpGet.getEntity();
	            if (entity != null) {
	            	InputStream inputStream = null;
					try {
						inputStream = entity.getContent();
					 }catch (Exception e) {
				  } 
	                htmlContent = convertToString(inputStream);
	                Log.d("request_info", "===>>> request_content:" + htmlContent);
	                refreshHandler.sendEmptyMessage(1);
	            }else{
	            	 Log.d("request_info", "===>>> httpGet.getEntity()为空");
	            }
	                String requestLine = httpGet.getRequestLine().toString();
	                Log.d("request_info", "===>>> requestLine:" + requestLine.toString());
	        }
	        
	        
	        
	        Thread theard = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    HttpResponse response;
	                    
	                    HttpClient httpClient = new DefaultHttpClient();
	                    response = httpClient.execute(httpGet);
	                    HttpEntity entity1 = httpGet.getEntity();
	                    if (entity1 != null) {
	                    	InputStream inputStream = null;
	        				try {
	        					inputStream = entity1.getContent();
	        				 }catch (Exception e){
	        				} 
	                        htmlContent = convertToString(inputStream);
	                        Log.d("request_info", "===>>>已发送过请求 request_content:" + htmlContent);
	                        refreshHandler.sendEmptyMessage(1);
	                    }else{
	                    	 Log.d("request_info", "===>>> 已发送过请求  httpGet.getEntity()为空");
	                    }
	                    Log.d("info", "response==: "+ response.toString());
	                    
	                    if (response.getStatusLine().getStatusCode() == 200) {
	                        Header[] headers = (Header[]) response.getAllHeaders();
	                        for (Header header : headers) {
	                            String name  = header.getName();
	                            String value = header.getValue();
	                            Log.d("info", "===>>> name:" + name);
	                            Log.d("info", "===>>> value:" + value);
	                        }
	                        HttpEntity entity = response.getEntity();
	                        if (entity != null) {
	                            InputStream inputStream = entity.getContent();
	                            htmlContent = convertToString(inputStream);
	                            Log.d("info", "===>>> htmlContent:" + htmlContent);
	                           refreshHandler.sendEmptyMessage(1);
	                        }
	                    }
	                } catch (Exception e) {
	                };
	            }
	        });
	        theard.start();
		}

		/**
		 * Implements  the interface :com.lansun.qmyo.view.RandomNumDialog.onDismissListener,
		 * Notice the difference about them.
		 */
		@Override
		public void onDismiss() {
			lastClickTime = System.currentTimeMillis()-2000;
		}
	    
		
		
}
