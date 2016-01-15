package com.lansun.qmyo.fragment;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ClickGoUrl;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.RedPackInfo;
import com.lansun.qmyo.domain.ShareRedPackInfo;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.GrabRedPackOverDialog;
import com.lansun.qmyo.view.GrabRedPackSharedDialog;
import com.lansun.qmyo.view.ObservableWebView;
import com.lansun.qmyo.view.ObservableWebView.OnScrollChangedCallback;
import com.lansun.qmyo.view.RandomNumDialog;
import com.lansun.qmyo.view.SharedDialog;

/**
 *
 * 
 * @author Yeun
 * 
 */
public class GrabRedPackFragment extends BaseFragment implements OnClickListener {

	@InjectAll
	Views v;
	private HomePromoteData promote;

	class Views {
		private ObservableWebView webView;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View ll_promote_detail_title,iv_activity_back,iv_activity_shared;
		
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_grab_red_pack_detail,null, false);
		Bundle arguments = getArguments();
		if (arguments != null) {
			promote = (HomePromoteData) arguments.getSerializable("promote");
			loadUrl = (String)arguments.get("loadUrl");
			LogUtils.toDebugLog("loadUrl", loadUrl);
		}
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@SuppressWarnings("deprecation")
	@InjectInit
	private void init() {
		WebSettings settings = v.webView.getSettings();
		settings.setJavaScriptEnabled(true);
		
		v.webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		/*v.webView.getSettings().setSupportZoom(true);
		v.webView.getSettings().setBuiltInZoomControls(true);
		v.webView.scrollTo(0, 20);*/
		v.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		//v.webView.setHorizontalScrollBarEnabled(false);//水平不显示
		v.webView.setVerticalScrollBarEnabled(false); //垂直不显示
		//v.webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);//滚动条在WebView内侧显示
		//v.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);//滚动条在WebView外侧显示
		
		
		/* v.webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);*/
		/*v.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);*/
		v.webView.setOnKeyListener(new OnKeyListener( ){
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& v.webView.canGoBack()) { // 表示按返回键 时的操作
						v.webView.goBack(); // 后退
						return true; // 已处理
					}
				}
				return false;
			}
		});

		
		v.webView.setOnScrollChangedCallback(onScrollChangedCallback);
		v.webView.loadUrl(loadUrl);
		
		v.webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
//				try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				super.onPageFinished(view, url);
				endProgress();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				setProgress(v.webView);
				startProgress();
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
		
		
		
		v.iv_activity_shared.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK_SHARE_CONTENT, null, config, GrabRedPackFragment.this);
				
				
			}
		});
		
//		v.webView.loadUrl("http://act.qmyo.com/redpack/1");
	}

	OnScrollChangedCallback onScrollChangedCallback = new OnScrollChangedCallback() {
		@Override
		public void onScroll(int dx, int dy) {
			if (dy > 0) {
				v.ll_promote_detail_title.setVisibility(View.GONE);
			} else {
				v.ll_promote_detail_title.setVisibility(View.VISIBLE);
			}
		}
	};
	private boolean my_attention;
	private String loadUrl;
	private View rootView;


	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 1:
				String result = r.getContentAsString();
				//1.1 返回的是随机串-->可以去领红包
				
				//对result进行解析
				Gson grabJson = new Gson();
				RedPackInfo redPackInfo = grabJson.fromJson(result, RedPackInfo.class);
				int statueTag = redPackInfo.getData();
				LogUtils.toDebugLog("result", "result: "+ statueTag);
				switch(statueTag){
				    case -2://已领完
				    	//Toast.makeText(activity, "大大您来迟了，请等待下一大波红包到来~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog = new GrabRedPackOverDialog(activity,this,v.webView);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog.show(getFragmentManager(), "grabredpackisover");
					break;
				    case -1://未开始
//				    	Toast.makeText(activity, "大大您来早了，活动还未开始呢~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog1 = new GrabRedPackOverDialog(activity,this,v.webView);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog1.show(getFragmentManager(), "grabredpackisover");
					break;
				    case 0://已领过
//				    	Toast.makeText(activity, "大大您忘啦，您刚刚领过了~~", Toast.LENGTH_LONG).show();
				    	GrabRedPackOverDialog overDialog2 = new GrabRedPackOverDialog(activity,this,v.webView);//这么个体验的对话框，需要单独在其内部设置点击响应事件
				    	overDialog2.show(getFragmentManager(), "grabredpackisover");
					break;
				   default://正常返回数据
					   RandomNumDialog dialog = new RandomNumDialog(activity,String.valueOf(statueTag));//这么个体验的对话框，需要单独在其内部设置点击响应事件
					   dialog.show(getFragmentManager(), "grabredpack");
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
//				String imageUrl = "http://act.qmyo.com/images/redpack/pre-redpack.jpg";
				String currentActivityUrl = shareRedPackInfo.getShare_url();
				
				new GrabRedPackSharedDialog().showPopwindow(rootView, getActivity(), 
						title , 
						content ,
						"",
						currentActivityUrl);
				LogUtils.toDebugLog("Grab", "执行到分享这一步");
				
				break;
				//1.2 返回的不正确-->提示红包已被邻过
//				LogUtils.toDebugLog("返回数字", "返回数字"+result);
//				if(result.contains("false")){
//					Toast.makeText(activity, "小子，不要贪心~~", Toast.LENGTH_LONG).show();
//				}else{
//					result = result.substring(result.indexOf(":")+2, result.length()-2);
//					RandomNumDialog dialog = new RandomNumDialog(activity,result);//这么个体验的对话框，需要单独在其内部设置点击响应事件
//					//进来首先就弹出对话框
//					dialog.setOnConfirmListener(new OnConfirmListener(){
//						@Override
//						public void confirm() { }
//					});
//					dialog.show(getFragmentManager(), "grabredpack");
//					break;
//				}
				
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
		String urlTailStr = url.substring(headIndex, url.length()).replaceAll("\'","\"" );
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
		}else if(tag==30){//------------------------------------>进行抢红包的点击事件
			/*
			 *  "lansunqmyo://maijieclient/?{'tag':30,'type':'redpack'}"
			 *   打开红包的随机码的页面
			 */
			if(isExperience()==true){
				Toast.makeText(activity, "请登陆注册后,再抢红包大奖", Toast.LENGTH_LONG).show();
				
				Bundle bundle = new Bundle();
				bundle.putString("fragment_name","GrabRedpackFragment");
				RegisterFragment fragment = new RegisterFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fragment.setArguments(bundle);
				fEntity.setFragment(fragment);
				EventBus.getDefault().post(fEntity);
				
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
		}else if(tag == 40){ //----------------------------------------->进行分享活动预告页面的点击事件
			   //获取分享内容的网址
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.GRAB_RED_PACK_SHARE_CONTENT, null, config, GrabRedPackFragment.this);
			
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case R.id.iv_activity_shared:
			
			String title = "biaoti";
			String content = "neirong";
			String imageUrl = "";
			String currentActivityUrl ="http://www.baidu.com";
			
			LogUtils.toDebugLog("popup", "弹出分享按钮");
			
			new GrabRedPackSharedDialog().showPopwindow(rootView, getActivity(), 
					title , 
					content ,
					imageUrl,
					currentActivityUrl);
			break;
		}
	}
}
