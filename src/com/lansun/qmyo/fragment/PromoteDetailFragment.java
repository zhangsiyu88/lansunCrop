package com.lansun.qmyo.fragment;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
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
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ObservableWebView;
import com.lansun.qmyo.view.SharedDialog;
import com.lansun.qmyo.view.ObservableWebView.OnScrollChangedCallback;
import com.lansun.qmyo.R;

/**
 *
 * 
 * @author Yeun
 * 
 */
public class PromoteDetailFragment extends BaseFragment {

	@InjectAll
	Views v;
	private HomePromoteData promote;

	class Views {
		private ObservableWebView webView;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View ll_promote_detail_collection, ll_promote_detail_shared,
				ll_promote_detail_title;
		private RecyclingImageView iv_promote_detail_collection,
				iv_promote_detail_shared;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_promote_detail,
				null, false);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		
		Bundle arguments = getArguments();
		if (arguments != null) {
			promote = (HomePromoteData) arguments.getSerializable("promote");
			loadUrl = (String) arguments.get("loadUrl");
		}
		
		
		WebSettings settings = v.webView.getSettings();
		settings.setJavaScriptEnabled(true);
		v.webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		v.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		v.webView.getSettings().setCacheMode(
				WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		v.webView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& v.webView.canGoBack()) { // 表示按返回键
														// 时的操作
						v.webView.goBack(); // 后退
						return true; // 已处理
					}
				}
				return false;
			}
		});

		v.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		v.webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				endProgress();
//				refreshCurrentList(
//						GlobalValue.URL_USER_ARTICLE_INFO + promote.getId(),
//						null, 1, null);
				super.onPageFinished(view, url);
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
					
					//setNewFrag(url);
					getUrlTailDict(url);
					return true;
				}else{
					return super.shouldOverrideUrlLoading(view, url);
				}
			}

		});
		
		v.webView.setOnScrollChangedCallback(onScrollChangedCallback);
		//v.webView.loadUrl(loadUrl);
		v.webView.loadUrl("http://act.qmyo.com/poster/1");
		/**
		 * 在webView上展示出网址的内容
		 */
		/*if (!TextUtils.isEmpty(promote.getUrl())) {
			v.webView.loadUrl(promote.getUrl());
		}*/
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

	private void click(View view) {
		switch (view.getId()) {
		case R.id.ll_promote_detail_collection:
			if (my_attention) {
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				config.setRequest_type(InternetConfig.request_delete);
				config.setMethod("DELETE");
				FastHttpHander.ajax(GlobalValue.URL_USER_ARTICLE_DELETE
						+ promote.getId(), config, this);
			} else {
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
				params.put("article_id", promote.getId());
				FastHttpHander.ajaxForm(GlobalValue.URL_USER_ARTICLE, params,
						null, config, this);
			}
			break;
		case R.id.ll_promote_detail_shared:
			SharedDialog dialog = new SharedDialog();
			dialog.showPopwindow(v.webView, activity, promote.getName(),
					promote.getTag(), promote.getPhoto());
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:

				if ("true".equals(r.getContentAsString())) {
					if (my_attention) {
						v.ll_promote_detail_collection
								.setBackgroundResource(R.drawable.circle_background_gray);
						v.iv_promote_detail_collection.setPressed(false);
						CustomToast.show(activity, R.string.tip,
								R.string.sc_sucess_cancle);
					} else {
						v.ll_promote_detail_collection
								.setBackgroundResource(R.drawable.circle_background_green);
						v.iv_promote_detail_collection.setPressed(true);

						CustomToast.show(activity, R.string.tip,
								R.string.sc_sucess);
					}

				}
				break;
			case 1:
				JSONObject object;
				try {
					object = new JSONObject(r.getContentAsString());
					my_attention = object.getBoolean("my_attention");
					if (my_attention) {
						v.ll_promote_detail_collection
								.setBackgroundResource(R.drawable.circle_background_green);
						v.iv_promote_detail_collection.setPressed(true);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					object = new JSONObject(r.getContentAsString());
					my_attention = object.getBoolean("my_attention");
					if (my_attention) {
						v.ll_promote_detail_collection
								.setBackgroundResource(R.drawable.circle_background_green);
						v.iv_promote_detail_collection.setPressed(true);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
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
	
	private void getUrlTailDict(String url) {
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
		}
		
	}
}
