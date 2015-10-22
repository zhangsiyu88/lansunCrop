package com.lansun.qmyo.fragment;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.lansun.qmyo.fragment.searchbrand.HotAddHistoryFragment;
import com.lansun.qmyo.fragment.searchbrand.HotAddHistoryFragment.OnCallBack;
import com.lansun.qmyo.fragment.searchbrand.PuzzyFragment;
import com.lansun.qmyo.fragment.searchbrand.PuzzyFragment.OnPuzzyClickCallBack;
import com.lansun.qmyo.fragment.searchbrand.SearchBranListOkHttpFragment;
import com.lansun.qmyo.utils.AnimUtils;
import com.lansun.qmyo.utils.GlobalValue;
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
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_search, container,false);
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Handler_Inject.injectFragment(this, rootView);
		initView(rootView);
		return rootView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager=getChildFragmentManager();
		if (getArguments()!=null) {
			query=getArguments().getString("query");
			Log.e("query", query);
		}
	}

	private void initView(View root) {
		del_search_content=(ImageView)root.findViewById(R.id.del_search_content);
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
			setFragmentChose(new HotAddHistoryFragment(this));
		}
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
			v.search_tv_cancle.setText(R.string.cancle);
			v.search_tv_cancle.setTextColor(Color.parseColor("#939393"));
			setFragmentChose(new HotAddHistoryFragment(this));//输入框为空，则进入HotAddHistoryFragment部分
			del_search_content.setVisibility(View.GONE);
			isPuzzy=false;
		} else {
			del_search_content.setVisibility(View.VISIBLE);
			Log.e("isPuzzy", isPuzzy+"");
			if (!isPuzzy) {
				PuzzyFragment fragment=new PuzzyFragment(this);
				Bundle bundle = new Bundle();  
				bundle.putString("searchName",s.toString().trim());  
				fragment.setArguments(bundle);  
				setFragmentChose(fragment);//当输入框不为空即将其转至PuzzyFragment
				et_home_search.setFocusable(true);
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
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		initData();
		
		et_home_search.setHint(R.string.search_all_hint);
		AnimUtils.startTopInAnim(activity, v.ll_search_top_menu, 300,
				et_home_search);
	}
	private void initData() {
		refreshCurrentList(GlobalValue.URL_USER_QUERYS, null, 0, null);
		refreshCurrentList(GlobalValue.URL_ADVERTISEMENT_SEARCH, null, 1, null);
	}
	private void click(View view) {
		switch (view.getId()) {
		case R.id.search_tv_cancle:
			if (getString(R.string.cancle).equals(v.search_tv_cancle.getText())) {
				EventBus bus = EventBus.getDefault();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(new HomeFragment());
				bus.post(entity);
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘
			} else {
				String search_value=et_home_search.getText().toString().trim();
				postQuery(search_value);
				startSearch(search_value);
				for (String li:App.search_list_history) {
					if (search_value.equals(li)) {
						return;
					}
				}
				addSearchHistory(search_value);
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
	private void startSearch(String query) {
		et_home_search.setText("");
		isPuzzy=false;
//		SearchBrandListFragment fragment = new SearchBrandListFragment();
//		SearchContentFragment fragment = new SearchContentFragment();
		SearchBranListOkHttpFragment fragment = new SearchBranListOkHttpFragment();
		Bundle args = new Bundle();
		args.putString("query", query);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	private void postQuery(String querya) {
		InternetConfig config = new InternetConfig();
		config.setKey(6);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		LinkedHashMap<String, String> params = new LinkedHashMap<>();
		params.put("key", querya);
		FastHttpHander.ajaxForm(GlobalValue.URL_USER_USER_QUERY, params, null,
				config, this);
	}

	private void startSearchOutAnim(final Context activity, View view,
			long duration) {
		Animation search_top_out = AnimationUtils.loadAnimation(activity,
				R.anim.search_top_out);
		search_top_out.setDuration(duration);
		search_top_out.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				EventBus bus = EventBus.getDefault();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(new HomeFragment());
				bus.post(entity);
			}
		});
		view.startAnimation(search_top_out);
	}
	@Override
	public void onTextCallBack(View view, int position) {
		switch (view.getId()) {
		case R.id.tv_search_hot_ad:
			String search_name=((TextView)view).getText().toString();
			startSearch(search_name);
			postQuery(search_name);
			for (String li:App.search_list_history) {
				if (search_name.equals(li)) {
					return;
				}
			}
			addSearchHistory(search_name);
			break;
		case R.id.history_tv:
			startSearch(((TextView)view).getText().toString());
			
			break;
		}
	}
	@Override
	public void onPuzzCallBack(String sel_name) {
		if (!isTrue) {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			isTrue=true;
		}
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
}