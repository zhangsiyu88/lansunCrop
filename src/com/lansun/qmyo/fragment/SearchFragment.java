package com.lansun.qmyo.fragment;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import cn.jpush.android.util.h;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.RefreshTokenEntity;
import com.lansun.qmyo.fragment.HomeFragment.HomeRefreshBroadCastReceiver;
import com.lansun.qmyo.fragment.searchbrand.HotAddHistoryFragment;
import com.lansun.qmyo.fragment.searchbrand.HotAddHistoryFragment.OnCallBack;
import com.lansun.qmyo.fragment.searchbrand.PuzzyFragment;
import com.lansun.qmyo.fragment.searchbrand.PuzzyFragment.OnPuzzyClickCallBack;
import com.lansun.qmyo.fragment.searchbrand.SearchBranListOkHttpFragment;
import com.lansun.qmyo.fragment.searchbrand.SearchBrandListOkHttpFragment;
import com.lansun.qmyo.utils.AnimUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
/**
 * 搜索界面
 * 
 * @author bhxx
 * 
 */
public class SearchFragment extends BaseFragment implements OnCallBack,OnPuzzyClickCallBack,TextWatcher{
	public static String search_Name;
	@InjectAll
	Views v;
	class Views {
		private View ll_search_top_menu, ll_search, ll_hot_history;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView search_tv_cancle;
	}
	public EditText et_home_search;
	private ImageView del_search_content;
	private FragmentManager manager;
	private FragmentTransaction transaction;
	private boolean isPuzzy;
	private String query;
	private boolean isTrue;
	private InputMethodManager imm;
	private View rootView;
	private IntentFilter filter;
	private ToggleKeyBoardBroadCastReceiver broadCastReceiver;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_search, container,false);

		//一进来就已经将键盘收齐
//		  activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
//		  WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		  
		Handler_Inject.injectFragment(this, rootView);//这一步实际上是重新调用了init()方法
		initView(rootView);
		return rootView;
	}
	/**
	 * 加载时最先运行
	 * 首先拿到查询的参数,这个参数是从下一个页面传递过来的主要为了保存和搜索
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		broadCastReceiver = new ToggleKeyBoardBroadCastReceiver();
		System.out.println("搜索页注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.toggleSoftKeyboard");
		getActivity().registerReceiver(broadCastReceiver, filter);
		
		manager=getChildFragmentManager();
		if (getArguments()!=null) {
			query=getArguments().getString("query");
		}
	}

	private void initView(View root) {
		del_search_content=(ImageView)root.findViewById(R.id.del_search_content);
		//
		
		et_home_search=(EditText)root.findViewById(R.id.et_home_search);
		et_home_search.requestFocus();
		et_home_search.setCursorVisible(true);
		et_home_search.addTextChangedListener(this);
		if (!TextUtils.isEmpty(query)) {
			et_home_search.setText(query);
			et_home_search.setSelection(query.length());
		}
		del_search_content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!TextUtils.isEmpty(et_home_search.getText())) {
					et_home_search.setText("");
					et_home_search.setHint(R.string.search_all_hint);
				}
			}
		});
		if (query==null) {
			setFragmentChose(new HotAddHistoryFragment(this));//当query为空的时候，需要将界面替换为热门搜索加历史搜索页
		}
		
		et_home_search.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId ==EditorInfo.IME_ACTION_SEARCH){
					String search_value=et_home_search.getText().toString().trim();
					if(search_value.equals("")){
						Toast.makeText(activity, "请输入搜索内容", 2000).show();
					}else{
						/*android:inputType="text"
						android:imeActionLabel="完成"
						android:imeOptions="actionNone"*/
						
//						imm = (InputMethodManager) getActivity()
//								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(et_home_search.getWindowToken(), 0);
						//CustomToast.show(activity, "et_home_search的点击搜索", "收起键盘");
						startSearch(search_value);//开始搜索
						
					}
					return true;
				}
				return false;
			}
			
		});
	}
	/**
	 * 搜索内容监听
	 * 
	 * @param s
	 * @param start
	 * @param before
	 * @param count
	 */
	private void changeText(CharSequence s, int start, int before, int count) {
		Log.e("change", s.toString());

		if (TextUtils.isEmpty(s)) {
			et_home_search.setImeOptions(EditorInfo.IME_ACTION_DONE);
			
			v.search_tv_cancle.setText(R.string.cancle);
			v.search_tv_cancle.setTextColor(Color.parseColor("#939393"));
			/* 关闭此方法： 为的是在有网进入热门和历史页，断网进入搜索结果页，来网回到热门和历史页时，保证热门搜索内容存在
			 * setFragmentChose(new HotAddHistoryFragment(this));//输入框为空，则进入HotAddHistoryFragment部分                   setFragmentChose切换页面的数据
			 */
			
			//打开此方法：保证编辑栏为空时，回到热门和历史页
			setFragmentChose(new HotAddHistoryFragment(this));//输入框为空，则进入HotAddHistoryFragment部分                   setFragmentChose切换页面的数据
			
			/*imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/
			
			del_search_content.setVisibility(View.GONE);
			isPuzzy=false;
			
		} else {
			del_search_content.setVisibility(View.VISIBLE);
			if (!isPuzzy) {
				PuzzyFragment fragment=new PuzzyFragment(this);
				Bundle bundle = new Bundle();  
				bundle.putString("searchName",s.toString().trim());  
				fragment.setArguments(bundle);  
				setFragmentChose(fragment);//                       -->当输入框不为空即将其转至PuzzyFragment
				et_home_search.setFocusable(true);
				
				//当搜索栏中的内容不为空的时候，需要将
				et_home_search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
				isPuzzy=true;
			}
			v.search_tv_cancle.setText(R.string.search);
			v.search_tv_cancle.setTextColor(getResources().getColor(
					R.color.app_green1));
			String search_name=et_home_search.getText().toString().trim();
			Intent intent=new Intent("com.qmyo.puzzysearch");
			intent.putExtra("searchName", search_name);
			getActivity().sendBroadcast(intent);
		}
	}
	/**
	 * 设置Fragment
	 * @param fragment
	 */
	private void setFragmentChose(Fragment fragment) {
		transaction = manager.beginTransaction();
		transaction.replace(R.id.search_conten,fragment);
		transaction.commit();
	}
	@InjectInit
	private void init() {
		LogUtils.toDebugLog("init()", "走到了init()方法");
		
		imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		//CustomToast.show(activity, "init()方法中键盘升起来", "键盘升起来");
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		
		initData();
		et_home_search.setHint(R.string.search_all_hint);
		AnimUtils.startTopInAnim(activity, v.ll_search_top_menu, 300,et_home_search);
	}
	private void initData() {
		/*refreshCurrentList(GlobalValue.URL_USER_QUERYS, null, 0, null);
		refreshCurrentList(GlobalValue.URL_ADVERTISEMENT_SEARCH, null, 1, null);*/
	}
	
	
	private void click(View view) {
		switch (view.getId()) {
		case R.id.search_tv_cancle:
			if (getString(R.string.cancle).equals(v.search_tv_cancle.getText())) {//字为“取消”
				back();

			} else {//内容为“搜索”二字时
				String search_value=et_home_search.getText().toString().trim();
				
				//点击搜索按钮时，将键盘降下来
//				InputMethodManager imm = (InputMethodManager) getActivity()
//						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				
				startSearch(search_value);//开始搜索
				
				//imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				
				for (String li:App.search_list_history) {
					if (search_value.equals(li)) {
						return;
					}
				}
				addSearchHistory(search_value);//将刚刚搜索的关键字写入到历史记录中去
			}
			break;
		}
	}
	/**
	 * 添加搜索历史
	 * @param searcchName
	 */
	public void addSearchHistory(String searchName){
		if (App.search_list_history.size()==0){	
			App.app.setData("first_history",searchName);
		}else if(App.search_list_history.size()==1){
			App.app.setData("second_history",searchName);
		}else if (App.search_list_history.size()==2) {
			App.app.setData("third_history",searchName);
		}else if (App.search_list_history.size()==3) {
			App.app.setData("third_history",App.app.getData("second_history"));
			App.app.setData("second_history",App.app.getData("first_history"));
			App.app.setData("first_history",searchName);
		}
		App.app.initHistory();
	}
	/**
	 * 跳转页面
	 * @param query
	 */
	private void startSearch(String query) {
		et_home_search.setText("");
		isPuzzy=false;
		SearchBrandListOkHttpFragment fragment = new SearchBrandListOkHttpFragment();
		Bundle args = new Bundle();
		args.putString("query", query);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}
	/**
	 * 开启搜索
	 * @param querya
	 */
	private void postQuery(String query) {
		InternetConfig config = new InternetConfig();
		config.setKey(6);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		LinkedHashMap<String, String> params = new LinkedHashMap<>();
		params.put("key", query);
		FastHttpHander.ajaxForm(GlobalValue.URL_USER_USER_QUERY, params, null,config, this);
	}
	/**
	 * 历史和热门搜索的回调方法保存和请求网络
	 */
	@Override
	public void onTextCallBack(final View view, int position) {
		switch (view.getId()) {
		case R.id.tv_search_hot_ad:
			String search_name=((TextView)view).getText().toString();

			//文字发生变化时，也会将键盘缩回去
//			InputMethodManager imm = (InputMethodManager) getActivity()
//					.getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//			imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
			/*InputMethodManager imm_next = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm_next.toggleSoftInput(1, InputMethodManager.HIDE_NOT_ALWAYS);*/
			
//			imm = (InputMethodManager) getActivity()
//					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			//CustomToast.show(activity, "onTextCallBack中hot", "收起键盘");

			startSearch(search_name);
			//postQuery(search_name);
			for (String li:App.search_list_history) {
				if (search_name.equals(li)) {
					return;
				}
			}
			addSearchHistory(search_name);
			
			break;
		case R.id.history_tv:

//			InputMethodManager imm1 = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm1.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

//			imm = (InputMethodManager) getActivity()
//					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			//CustomToast.show(activity, "onTextCallBack中hository", "收起键盘");
//			new Timer().schedule(new TimerTask() {
//				@Override
//				public void run() {
//					//OP
//				}
//			}, 500);
			
		     String search_name_history=((TextView)view).getText().toString();
			 startSearch(search_name_history);

			break;
		}
	}

	//实现了PuzzyFragemnt中的 interface OnPuzzyClickCallBack
	@Override
	public void onPuzzCallBack(String sel_name) {
		if (!isTrue) {
			isTrue=true;
		}
//		imm = (InputMethodManager) getActivity()
//				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
		//CustomToast.show(activity, "onPuzzCallBack中Item", "收起键盘");
		
		startSearch(sel_name);
		addSearchHistory(sel_name);
	}
	@Override
	public void afterTextChanged(Editable s) {

	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		changeText(s, start, before, count);
	}
	
	class ToggleKeyBoardBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.toggleSoftKeyboard")){
				System.out.println("全局搜索页收到搜索结果页消失前发送的广播了");
				//imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				//imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				//et_home_search.requestFocus();
				
				/*
				 * 强制将键盘弹起
				 */
				imm.showSoftInput(et_home_search, InputMethodManager.SHOW_FORCED);
			}
		}
	 }
	
	@Override
	public void onDestroy() {
		activity.unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
}
