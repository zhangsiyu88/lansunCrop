package com.lansun.qmyo.fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.view.GifMovieView;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Json;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.service.AccessTokenService;
import com.lansun.qmyo.utils.DataUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.lansun.qmyo.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
@SuppressLint("SimpleDateFormat") public class BaseFragment extends Fragment implements OnTouchListener{

	protected LayoutInflater inflater ;
	public  Activity activity;
	protected View progress;
	protected LinearLayout progress_container;
	protected ProgressDialog pd;
	protected DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300)).build();
	protected View loadView;

	protected String refreshUrl;
	protected int refreshKey ;
	protected LinkedHashMap<String, String> refreshParams;
	protected TextView progress_text;
	protected int first_enter = 0;
	protected Gson gson = new Gson();
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		inflater = LayoutInflater.from(getActivity());
		this.pd = new ProgressDialog(activity);
	    this.pd.setMessage("小迈努力加载中... ...");
	    this.pd.setCancelable(false);
	    this.pd.setCanceledOnTouchOutside(false);
}

	protected void setProgress(View view) {
		if (progress != null) {
			return;
		}
		loadView = view;
		LayoutParams lp = view.getLayoutParams();
		ViewParent parent = view.getParent();
		FrameLayout container = new FrameLayout(activity);
		ViewGroup group = (ViewGroup) parent;
		int index = group.indexOfChild(view);
		group.removeView(view);
		group.addView(container, index, lp);
		container.addView(view);
		if (inflater != null) {
			progress = inflater.inflate(R.layout.fragment_progress, null);
			progress_container = (LinearLayout) progress
					.findViewById(R.id.progress_container);

			progress_text = (TextView) progress.findViewById(R.id.progress_text);//动态猫头鹰底部显示：内容正在加载中
			
			progress_container.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try{
						refreshCurrentList(refreshUrl, refreshParams, refreshKey,loadView);
					}catch(Exception e){
						App.app.startActivity(new Intent(App.app,MainActivity.class));
					}
					
				}
			});
			GifMovieView loading_gif = (GifMovieView) progress.findViewById(R.id.loading_gif);
			loading_gif.setMovieResource(R.drawable.loading);
			container.addView(progress);
			progress_container.setTag(view);
			view.setVisibility(View.GONE);
		}
		group.invalidate();
	}

	protected void startProgress() {
		if (progress_container != null) {
			progress_container.setVisibility(View.VISIBLE);
		}
		hideProgress();
	}

	protected void endProgress() {
		if (progress_container != null) {
			//这个与progress_container为Tag关系的是ListView对象，即progress_container.getTag()为ListView对象
			((View) progress_container.getTag()).setVisibility(View.VISIBLE);
			
			progress_container.setVisibility(View.GONE);
			progress.setVisibility(View.GONE);
		}
	}

	Handler handler ;
	private int mKey;
	private String mUrl;
	private LinkedHashMap<String, String> mParams;
	private View mmView;

	protected void hideProgress() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// endProgress();
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		}).start();
	}

	/**
	 * 刷新当前页面的数据
	 * 
	 * @param url
	 *            链接
	 * @param params
	 *            参数 默认返回 0 的消息
	 */
	protected void refreshCurrentList(final String url,
			final LinkedHashMap<String, String> params, final int key, final View view) {
		
//		App.app.setData("LastRefreshTokenTime",String.valueOf(System.currentTimeMillis()-31*60*1000));
		
		/**
		 * 大前提： App.app.getData("LastRefreshTokenTime")不为 空
		 */
		if(App.app.getData("LastRefreshTokenTime")!=null&&
				!App.app.getData("LastRefreshTokenTime").equals("")&&
				!TextUtils.isEmpty(App.app.getData("LastRefreshTokenTime"))){
			
			long LastRefreshTokenTime = Long.valueOf(App.app.getData("LastRefreshTokenTime"));
			LogUtils.toDebugLog("LastRefreshTokenTime", "上次最近更新token服务的时刻： "+DataUtils.dataConvert(LastRefreshTokenTime));
			LogUtils.toDebugLog("LastRefreshTokenTime", "两次更新token的时间差"+((System.currentTimeMillis()-LastRefreshTokenTime))/1000/60);
			
			if((System.currentTimeMillis()-LastRefreshTokenTime)>10*60*1000){
				//进行刷新token的操作
				HttpUtils httpUtils = new HttpUtils();
				RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException e, String result) {
					}
					@Override
					public void onSuccess(ResponseInfo<String> result) {
						Token token = Handler_Json.JsonToBean(Token.class,result.result.toString());
					
					/* 手动解析返回的结果
					 * try {
						JSONObject jObject = new JSONObject(result.result.toString());
						token = (String) jObject.get("token");
						App.app.setData("access_token", token);
					} catch (JSONException e) {
						e.printStackTrace();
					}*/
						//Token token = gson.fromJson(result.toString(), Token.class);
						App.app.setData("access_token", token.getToken());
						
						App.app.setData("LastRefreshTokenTime",String.valueOf(System.currentTimeMillis()));
						
						LogUtils.toDebugLog("Token",token.getToken() );
						LogUtils.toDebugLog("Token", App.app.getData("access_token"));
						LogUtils.toDebugLog("LastRefreshTokenTime", 
								"此次最近更新token服务的时刻： "+DataUtils.dataConvert(Long.valueOf(App.app.getData("LastRefreshTokenTime"))));
						LogUtils.toDebugLog("LastRefreshTokenTime", "在BaseFragment中令牌更新操作成功！");
						
						InternetConfig config = new InternetConfig();
						config.setKey(key);
						HashMap<String, Object> head = new HashMap<>();
						head.put("Authorization", "Bearer " + App.app.getData("access_token"));
						
						config.setHead(head);
						FastHttpHander.ajaxGet(url, params, config, this);
						if (view != null) {
							setProgress(view);
							startProgress();
						}
						return;
					}
				};
				if(isExperience()){//体验用户的情况下，是需要使用临时Exp_Secret去更新Token
					if(App.app.getData("exp_secret")!=""){
						httpUtils.send(HttpMethod.GET, GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("exp_secret"), null,requestCallBack );
					}
				}else{//非体验用户（登陆用户），是需要使用正式的Secret去更新Token
					httpUtils.send(HttpMethod.GET, GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("secret"), null,requestCallBack );
				}
			}
		}
		
		
		InternetConfig config = new InternetConfig();
		config.setKey(key);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		
		config.setHead(head);
		FastHttpHander.ajaxGet(url, params, config, this);
		if (view != null) {
			setProgress(view);
			startProgress();
		}
		
		
	}

	OnScrollListener onScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			System.out.println(firstVisibleItem);
			// EventBus eventBus = EventBus.getDefault();
			// HomeEntity entity = new HomeEntity();
			// if (firstVisibleItem >= 1) {
			// entity.setBottom_hidden(true);
			// eventBus.post(entity);
			// } else {
			// entity.setBottom_hidden(false);
			// eventBus.post(entity);
			// }
		}
	};

	@InjectMethod(value = { @InjectListener(ids = { R.id.iv_activity_back }, listeners = { OnClick.class }) })
	protected void back() {
		getFragmentManager().popBackStack();
	}
	
	
	/*@InjectMethod(value = { @InjectListener(ids = { R.id.iv_activity_back }, listeners = { OnClick.class }) })
	protected void backandinitMine() {
		 * 先从栈中弹走一个Fragment，后再在重新去生成一个对应的Fragment
		 
		getActivity().getSupportFragmentManager().popBackStack();
		
		替换成万能代码
		 * Fragment needFragment = new MineFragment();
		FragmentEntity event = new FragmentEntity();
		event.setFragment(needFragment);
		EventBus.getDefault().post(event);
		
		int backStackEntryCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();
		Log.i("backStackEntryCount的值", String.valueOf(backStackEntryCount));
		
		List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
		Fragment fragment =getActivity().getSupportFragmentManager().getFragments().get(fragments.size()-1);
		
		String fragmentString = fragment.getClass().getName().toString();
		Log.i("backStackEntryCount-1的值", String.valueOf(backStackEntryCount-1));
		Log.i("这是列表中第getBackStackEntryCount()-1的位置上的fragment名字", fragmentString);
		
		Fragment fragment = new MineFragment();
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
		
		for(Fragment f:fragments){
			String simpleFragmentName = f.getClass().getName();
			Log.i("单个fragment的名字",simpleFragmentName);
		}
	}*/
	

	protected void loadPhoto(String imageUrl, ImageView v) {
		ImageLoader.getInstance().displayImage(imageUrl, v, options);
	}

	/**
	 * 初始化公用头部
	 * 
	 * @param tv_activity_title
	 * @param titleId
	 * @param iv_activity_shared
	 * @param rightRes
	 */
	protected void initTitle(TextView tv_activity_title, int titleId,
			View iv_activity_shared, int rightRes) {
		if (iv_activity_shared instanceof TextView) {
			((TextView) iv_activity_shared).setText(rightRes);
		}
		if (iv_activity_shared instanceof ImageView) {
			((ImageView) iv_activity_shared).setImageResource(rightRes);
		}
		tv_activity_title.setText(titleId);
	}

	protected void initTitle(TextView tv_activity_title, String title,
			View iv_activity_shared, int rightRes) {

		if (iv_activity_shared instanceof TextView) {
			((TextView) iv_activity_shared).setText(rightRes);
		}
		if (iv_activity_shared instanceof ImageView) {
			((ImageView) iv_activity_shared).setImageResource(rightRes);
		}
		tv_activity_title.setText(title);
	}

	public static void clearTempTokenAndSercet() {
		App.app.setData("exp_secret", "");
		App.app.setData("access_token", "");
	}

	public static void clearTokenAndSercet() {
		App.app.setData("secret", "");
		App.app.setData("access_token", "");
	}

	/**
	 * 定位的城市
	 * 
	 * @param cityCode
	 * @param cityName
	 */
	public void saveCurrentCity(String cityCode, String cityName) {
		App.app.setData("cityCode", cityCode);
		App.app.setData("cityName", cityName);
	}

	/**
	 * 用户选的城市
	 * 
	 * @param cityCode
	 * @param cityName
	 */
	public void saveSelectCity(String cityCode, String cityName) {
		App.app.setData("select_cityCode", cityCode);
		App.app.setData("select_cityName", cityName);
	}

	/**
	 * 返回2个数组第一个表示citycode 第二个cityName
	 * 
	 * @return
	 */
	public String[] getCurrentCity() {
		return new String[] { App.app.getData("cityCode"),
				App.app.getData("cityName") };
	}

	/**
	 * 返回用户选中城市 2个数组第一个表示citycode 第二个cityName
	 * 
	 * @return
	 */
	public String[] getSelectCity() {
		return new String[] { App.app.getData("select_cityCode"),
				App.app.getData("select_cityName") };
	}

	@Override
	public void onPause() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		/* imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		
		super.onPause();
	}

	/**
	 * 隐藏键盘
	 */
	protected void autoHiddenKey() {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	protected boolean isExperience() {
		return "true".equals(App.app.getData("isExperience"));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void onViewCreated(View view,Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	
}
