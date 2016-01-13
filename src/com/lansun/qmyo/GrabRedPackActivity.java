package com.lansun.qmyo;

import java.util.HashMap;
import java.util.zip.Inflater;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ClickGoUrl;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.ObservableWebView;
import com.lansun.qmyo.view.RandomNumDialog;
import com.lansun.qmyo.view.ObservableWebView.OnScrollChangedCallback;
import com.lansun.qmyo.view.RandomNumDialog.OnConfirmListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.RenderPriority;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GrabRedPackActivity extends Activity {

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Intent intent = getIntent();
		if (intent != null) {
			
			Bundle bundle = intent.getExtras();
			loadUrl = bundle.getString("loadUrl");
		}
		setContentView(R.layout.activity_grab_red_pack_detail);
		
		webView = (ObservableWebView)findViewById(R.id.webView);
		ll_promote_detail_title=  findViewById(R.id.ll_promote_detail_title);
		iv_activity_back = (LinearLayout) findViewById(R.id.iv_activity_back);
		init();
		super.onCreate(savedInstanceState);
	}

	private void init() {
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		//webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

	   /*webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);*/
		webView.setOnKeyListener(new OnKeyListener( ){
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& webView.canGoBack()) { // 表示按返回键 时的操作
						webView.goBack(); // 后退
						return true; // 已处理
					}
				}
				return false;
			}
		});

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//endProgress();
				super.onPageFinished(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//setProgress(webView);
				//startProgress();
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.contains("lansunqmyo://maijieclient/?")){
					getUrlTailDict(url);
					return true;
				}
				
				if(url.contains("lansunqmyo://maijieclient/?")){
					//setNewFrag(url);
					getUrlTailDict(url);
					return true;
				}else{
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
		
		webView.setOnScrollChangedCallback(onScrollChangedCallback);
		webView.loadUrl(loadUrl);
		
//		webView.loadUrl("http://act.qmyo.com/redpack/1");
	
		/**
		 * 在webView上展示出网址的内容
		 */
		/*if (!TextUtils.isEmpty(promote.getUrl())) {
			webView.loadUrl(promote.getUrl());
		}*/
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
	private boolean my_attention;
	private String loadUrl;
	private LinearLayout iv_activity_back;

	private void click(View view) {
		switch (view.getId()) {
		
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 1:
				String result = r.getContentAsString();
				//1.1 返回的是随机串-->可以去领红包
				
				//1.2 返回的不正确-->提示红包已被邻过
				LogUtils.toDebugLog("返回数字", "返回数字"+result);
				if(result.contains("false")){
					Toast.makeText(this, "小子，不要贪心~~", Toast.LENGTH_LONG).show();
				}else{
					RandomNumDialog dialog = new RandomNumDialog(this,result);//这么个体验的对话框，需要单独在其内部设置点击响应事件
					//dialog.show(getFragmentManager(), "grabredpack");
					break;
				}
			}
		}
	}
	
	public void setNewFrag(String url) {
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
		}else{
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
				setResult(Activity.RESULT_FIRST_USER, intent);
				finish();
				
			}else{
				//1.抢红包网址
					/*http://appapi.qmyo.org/redpack/key*/				
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
		}
	}

	
}
