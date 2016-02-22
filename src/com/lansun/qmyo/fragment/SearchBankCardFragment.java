package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.search_band.BandPuzzySearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.band_puzzy.BandPuzzy;
import com.lansun.qmyo.domain.band_puzzy.BandPuzzyData;
import com.lansun.qmyo.event.entity.BackEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.listener.PuzzyItemClickCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.MyGridLayoutManager;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.swipe.SwipeListMineBankcardAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListSearchBankcardAdapter;
import com.lansun.qmyo.view.BankcardListView;
import com.lansun.qmyo.view.BankcardListView.OnRefreshListener;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MyListView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tencent.weibo.sdk.android.api.util.ImageLoaderAsync.callBackImage;
/**
 * 搜索银行卡界面
 * 
 * @author bhxx
 * 
 */
public class SearchBankCardFragment extends BaseFragment implements TextWatcher,PuzzyItemClickCallBack,OnFocusChangeListener{
	private final String TAG=SearchBankCardFragment.class.getSimpleName();
	private ImageView del_search_content;
	private EditText et_home_search;
	private BandPuzzySearchAdapter adapter;
	private RecyclerView band_puzzy_recycle;
	private String query_name;
	private int times = 0;
	
	private static List<String> list=new ArrayList<>();
	private Handler handlerPuzzy=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (dialogpg!=null) {
				dialogpg.dismiss();
			}
			switch (msg.what) {
			case 0:
				adapter.notifyDataSetChanged();
				break;
			case 2:
				lv_search_bank_card.onLoadMoreFished();
				
				setEmptityView(0);//将isFirst置为空-->不填充 emptyView（为空图片），且将其从ListView的父布局中去除掉
				if(bankcardAdapter == null){
					/*bankcardAdapter = new BankCardAdapter(lv_search_bank_card, dataList,
							R.layout.activity_bank_card_item,tagOfSearchBankCardFragment);//BankCardAdapter中别有洞天*/		
					
					bankcardAdapter = new SwipeListSearchBankcardAdapter(false,activity, dataList,R.layout.activity_bank_card_item_swipe_search,tagOfSearchBankCardFragment);
					bankcardAdapter.setFragment(SearchBankCardFragment.this);
					//bankcardAdapter.setActivity(SearchBankCardFragment.this);
					lv_search_bank_card.setAdapter(bankcardAdapter);
					lv_search_bank_card.setVisibility(View.VISIBLE);
				}else{
					bankcardAdapter.notifyDataSetChanged();
					//bankcardAdapter.setActivity(SearchBankCardFragment.this);
					lv_search_bank_card.setVisibility(View.VISIBLE);
				}
				
				if(bankList.getData().size()<10){
					if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
						lv_search_bank_card.onLoadMoreOverFished();
					}else{
						//DO-OP
					}
//					lv_search_bank_card.onLoadMoreOverFished();
//					CustomToast.show(activity, "到底啦！", "该关键词下关联的 银行卡暂时只有这么多");
				}
				
				break;
			case 3://搜索无结果
				dataList.clear();
				lv_search_bank_card.setVisibility(View.GONE);   //listView去掉
				v.ll_bank_card_tip.setVisibility(View.GONE);    //填卡提示页也被gone掉
				setEmptityView(1);//搜索卡名无果，需要进行添上emptyView    将emptyView充起来，并且在ListView的父控件中addView进去这个emptyView
				break;
			case 9:
				CustomToast.show(activity,"提示","请先更新到最新版本");
				break;
			case 10:
				CustomToast.show(activity,"提示","网络连接异常请刷新后重试");
				break;
			case 11:
			//在一进入我的这个界面时，将其展示出Dialog
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final Dialog dialog2 = new Dialog(activity,R.style.CustomDialogForNewUserTip);
			View layout2 = inflater.inflate(R.layout.dialog_addbankcard, null);
			dialog2.setContentView(layout2, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			layout2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//一旦点击就要将其置为 true，下一次进入不再执行下面的弹框操作
					App.app.setData("firstEnterBankcardToSearch","true");
					dialog2.dismiss();
				}
			});
			dialog2.show();
			dialog2.setCanceledOnTouchOutside(true);
			dialog2.setCancelable(true);
			   break;
			}
//			PullToRefreshManager.getInstance().onFooterRefreshComplete();
//			PullToRefreshManager.getInstance().footerEnable();
		};
	};
	
	
	
	@InjectAll
	Views v;
	private View emptyView;
	private boolean isFirst;
	private ArrayList<HashMap<String, String>> dataList;
	private BankCardList bankList;
	class Views {
		private LinearLayout ll_bank_card_tip;
		private RelativeLayout puzz_floor;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_title, tv_search_cancle;
	}
	
	/*@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), pull = true)
	private MyListView lv_search_bank_card;*/
	
	@InjectView
	private BankcardListView lv_search_bank_card;
	
	
	
	private boolean isPull = false;
	/*private BankCardAdapter bankcardAdapter;*/
	private SwipeListSearchBankcardAdapter bankcardAdapter;
	
	private boolean isMove=true;
	private ProgressDialog dialogpg;
	private boolean tagOfSearchBankCardFragment = true; //此值铁定为真，那么传到 SwipeListSearchBankcardAdapter 中的 mIsNotChange 铁定为 真
	private boolean mIsFromRegisterAndHaveNoBankcard = false;
	private View rootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_search_bank_card,container,false);
		initView(rootView);
		
		if(getArguments()!= null){
			mIsFromRegisterAndHaveNoBankcard  = getArguments().getBoolean("isFromRegisterAndHaveNoBankcard");
		}
		
		/* 如果目前是无卡的状态，那么下面可以进行对应的界面变化的操作
		 * 
		 * if(mIsFromRegisterAndHaveNoBankcard){
			//当我点击银行卡的搜索结果页时，需要直接将卡添置到用户选中的卡上，作为默认的选中卡
		    }*/
		Handler_Inject.injectFragment(this, rootView);
		
		lv_search_bank_card.setNoHeader(true);
//		lv_search_bank_card.onLoadMoreOverFished();
		lv_search_bank_card.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefreshing() {
				
			}
			
			@Override
			public void onLoadingMore() {
				if (bankList != null) {
					/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
					if (bankList.getNext_page_url()== "null"||TextUtils.isEmpty(bankList.getNext_page_url())) {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
						if(times == 0){
							lv_search_bank_card.onLoadMoreOverFished();
							CustomToast.show(activity,  R.string.reach_bottom,R.string.just_curr_bankcard);
							times++;
						}else{
							lv_search_bank_card.onLoadMoreOverFished();
						}
					}else{
						String next_page_url = bankList.getNext_page_url();
						int from = next_page_url.indexOf("[");
						int to = next_page_url.indexOf("]");
						String firstString = next_page_url.substring(0, from);
						String lastString = next_page_url.substring(to+1, next_page_url.length());
						next_page_url = firstString + lastString;
						isPull = true;
						String replaceUrl = next_page_url.replace("/?","?");
						String forntUrl = replaceUrl.substring(0, 36);
						int indexOfWordPage = replaceUrl.indexOf("page=");
						int indexOfWordQuery = replaceUrl.indexOf("query=");
						int indexOfWordAnd = replaceUrl.indexOf("&");
						String queryText = replaceUrl.substring(indexOfWordQuery+6, indexOfWordAnd);
						String pageText = replaceUrl.substring(indexOfWordPage+5, replaceUrl.length());
						LinkedHashMap<String, String> params = new LinkedHashMap<String, String>() ;
						try {
							params.put("query",URLEncoder.encode(queryText, "utf-8"));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						params.put("page",pageText);

						//需要上拉加载出新的银行卡的时，进行的网络访问的请求
						InternetConfig config = new InternetConfig();
						config.setKey(0);
						FastHttpHander.ajaxGet(forntUrl, params, config, SearchBankCardFragment.this);//此处进行了一次网络请求，result的那种
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						
						//更新当前页面的下一个页面时,前面的数据不应该被取消掉,应该拼接在后面
						/*refreshCurrentList(next_page_url,null, 0, lv_search_bank_card);*/
					}
				}else{
					CustomToast.show(activity, "bankList = null", "BankList为空！");
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
				}
			}
		});
		
		return rootView;
		
	}

	private void initView(View rootView) {
		MyGridLayoutManager manager=new MyGridLayoutManager(getActivity(),1);
		band_puzzy_recycle=(RecyclerView)rootView.findViewById(R.id.band_puzzy_recycle);
		band_puzzy_recycle.setLayoutManager(manager);
		adapter = new BandPuzzySearchAdapter(list, SearchBankCardFragment.this);
		band_puzzy_recycle.setAdapter(adapter);
		del_search_content=(ImageView) rootView.findViewById(R.id.del_search_content);
		et_home_search=(EditText)rootView.findViewById(R.id.et_home_search);
		
		et_home_search.requestFocus();//请求焦点
		
		et_home_search.setOnFocusChangeListener(this);//设置焦点改变的监听
		et_home_search.addTextChangedListener(this);//添加文本改变监听
		del_search_content.setOnClickListener(new OnClickListener() {//edit 编辑框
			@Override
			public void onClick(View view) {
				list.clear();
				et_home_search.setText("");
				onKeyShowAlways();
			}
		});
		emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.bank_search_empty, null);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	}

	@InjectInit
	private void init() {
		EventBus.getDefault().register(this);
		et_home_search.setHint(R.string.please_enter_card);
		onKeyShowAlways();
		final RelativeLayout puzz_floor = v.puzz_floor;
		et_home_search.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId ==EditorInfo.IME_ACTION_SEARCH){
					String search_value=et_home_search.getText().toString().trim();
					if(search_value.equals("")){
						Toast.makeText(activity, "请输入搜索内容", 2000).show();
					}else{
						et_home_search.clearFocus();
						lv_search_bank_card.setVisibility(View.VISIBLE);//listView展示出来
						puzz_floor.setVisibility(View.GONE);//模糊搜素的内容去除掉
						//强制隐藏掉键盘
						onKeyHideAlways(et_home_search);
						search(search_value);//开始搜索
					}
					return true;
				}
				return false;
			}
			
		});
	}

	private void onKeyShowAlways() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//对虚拟键盘进行隐藏
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
		if (TextUtils.isEmpty(s)) {
			v.ll_bank_card_tip.setVisibility(View.VISIBLE);//提示银行卡搜索页的Tip设置为显示可见
			setEmptityView(0);
			v.puzz_floor.setVisibility(View.GONE); //模糊搜索结果层设置为不可见
			del_search_content.setVisibility(View.GONE);
			lv_search_bank_card.setVisibility(View.GONE);  //搜索结果列表设置为不可见   搜索列表层和Tip语句提示层其实是放在同一个FrameLayout中
			et_home_search.requestFocus();
			v.tv_search_cancle.setText(R.string.cancle);
			v.tv_search_cancle.setTextColor(Color.parseColor("#939393"));
		} else {
			del_search_content.setVisibility(View.VISIBLE);
			v.tv_search_cancle.setText(R.string.search);
			v.tv_search_cancle.setTextColor(getResources().getColor(R.color.app_green1));
			query_name=et_home_search.getText().toString().trim();
			getPuzzyData(query_name);
		}
	}
	/**
	 * 搜索为空的时候添加
	 */
	public void setEmptityView(int visiable){
		if (visiable==1) {
			/*
			 * 重点关注下面的代码变化
			 */
			if (isMove) {
				((ViewGroup) lv_search_bank_card.getParent()).addView(emptyView,2);
				isMove=false;
			}
			//v.lv_search_bank_card.addView(emptyView);
			if (GlobalValue.user!=null) {                                      //如果是登录的正式用户
				emptyView.findViewById(R.id.tv_found_secretary).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FeedBackFragment fragment = new FeedBackFragment();//无卡进入银行卡反馈界面
						Bundle args = new Bundle();
						args.putString("title",getString(R.string.yhk_feedback));
						args.putString("type", "bankcard");
						args.putString("fragment_name", "SearchBankCardFragment");
						fragment.setArguments(args);
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
				});
				emptyView.findViewById(R.id.tv_found_secretary).setVisibility(View.VISIBLE);//让下面的绿色小字去除掉
			}
		}else {//visible 为 0时，将emptyView从ListView的父布局中去除掉
			if (!isMove) {
				((ViewGroup) lv_search_bank_card.getParent()).removeView(emptyView);
				isMove=true;
			}
		}
	}
	
	/**
	 * 关于银行卡的模糊搜索
	 * 
	 * @param query
	 */
	private void getPuzzyData(String query) {
		//puzz_floor模糊搜索层，包含了模糊搜索的RecyclerView
		lv_search_bank_card.setVisibility(View.GONE); //结果层设置为空
		v.puzz_floor.setVisibility(View.VISIBLE); //模糊搜索结果页设置为可见
		
		OkHttp.asyncGet(GlobalValue.URL_BANKCARD_PUZZY+"search="+query, "Authorization", "Bearer"+App.app.getData("access_token"), null, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String json=response.body().string();
					list.clear();
					if (json.contains("{")&&json.contains("}")) {
						json="{list: "+json+"}";
						Gson gson=new Gson();
						BandPuzzy data=gson.fromJson(json, BandPuzzy.class);
						for (BandPuzzyData puzzy : data.getList()) {
							list.add(puzzy.getName());
						};
					}else {
						Log.e("query_name", query_name);
						String back="{list: [{id: 0, "+"name: "+"查找："+query_name+"}]}";
						back=back.replace(" ", "");
						Gson gson=new Gson();
						BandPuzzy data=gson.fromJson(back, BandPuzzy.class);
						for (BandPuzzyData puzzy : data.getList()) {
							list.add(puzzy.getName());
						};
					}
					handlerPuzzy.sendEmptyMessage(0);
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {
			}
		});
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}
	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_search_cancle:
			
			if(App.app.getData("isEmbrassStatus").equals("true")&& getString(R.string.cancle).equals(v.tv_search_cancle.getText())){
				CustomToast.show(activity, "添加至少一张银行卡吧~", "添加的银行卡不同，看到的内容也不同哦");
				onKeyHideAlways(view);
				return;
			}
			if (getString(R.string.cancle).equals(v.tv_search_cancle.getText())) {//文字内容为取消,则取消
				onKeyHideAlways(view);
				back();
			} else {//否则,前去搜索 (后面会在当前界面上展示银行卡列表)
				
				et_home_search.clearFocus();
				lv_search_bank_card.setVisibility(View.VISIBLE);//listView展示出来
				v.puzz_floor.setVisibility(View.GONE);//模糊搜素的内容去除掉
				//强制隐藏掉键盘
				onKeyHideAlways(view);
				search(query_name);//带上关键字去服务器上去访问搜索
			}
			break;
		}
	}
	/**
	 *  隐藏键盘
	 * @param view
	 */
	private void onKeyHideAlways(View view) {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public void onEventMainThread(BackEntity event) {
		back();
	}
	
	/**
	 * 点击搜索或者模糊搜索条目开始搜索
	 * （搜索内容不可能为空所以此处不需要判断是否为空）
	 * 1：隐藏模糊搜索view
	 * 2：如果搜索有结果显示搜索列表的view
	 * 3: 如果没有结果显示emptyView
	 * @param search_Name
	 */
	private void search(String search_Name) {
		
		this.first_enter =0;//保证了每次新的关键字搜索时都拥有 是否为第一次加载的 判断标签
		this.times = 0;
		
		bankcardAdapter = null;
		//隐藏为空的时候view层
		v.ll_bank_card_tip.setVisibility(View.GONE);
		dialogpg = new ProgressDialog(activity);
		dialogpg.setMessage("正在查找中...");
		dialogpg.show();
		
		OkHttp.asyncGet(GlobalValue.URL_BANKCARD_ALL+"query="+search_Name,TAG, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					Gson gson = new Gson();
					String json=response.body().string();
					bankList = gson.fromJson(json, BankCardList.class);	
					if(isPull){
						isPull = false;
					}else{
						dataList = new ArrayList<>();
					}
					if (bankList.getData().size()!=0) {//服务器返回了值
						isFirst = false;
						for (BankCardData data : bankList.getData()) {
							HashMap<String, String> map = new HashMap<>();
							map.put("iv_bank_card_head", data.getBankcard().getPhoto());
							map.put("tv_bank_card_name", data.getBank().getName());
							map.put("tv_bank_card_desc", data.getBankcard().getName());
							map.put("id", data.getBankcard().getId() + "");
							dataList.add(map);
						}
						//更新查询列表页
						handlerPuzzy.sendEmptyMessage(2);
						
						if(App.app.getData("firstEnterBankcardToSearch").isEmpty()){//第一次进入搜索页，并且搜索出了结果
							handlerPuzzy.sendEmptyMessage(11);
						}
					}else {
						handlerPuzzy.sendEmptyMessage(3);
					} 
				}else {
					handlerPuzzy.sendEmptyMessage(1);
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {
				//网络异常的handle
				handlerPuzzy.sendEmptyMessage(10);
			}
		});
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onPuzzyItemClick(View view, int position) {
		String query=((TextView)view).getText().toString();
		if (query.contains("查找：")) {
			query=query.substring(3, query.length());
		}
		et_home_search.setText(query);
		v.puzz_floor.setVisibility(View.GONE);
		lv_search_bank_card.setVisibility(View.VISIBLE);
		search(query);
		onKeyHideAlways(view);
		et_home_search.clearFocus();
	}
	/**
	 * onFouseChange接口
	 */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (hasFocus) {
			if (!TextUtils.isEmpty(((EditText)view).getText().toString().trim())) {
				query_name=((EditText)view).getText().toString().trim();
				v.puzz_floor.setVisibility(View.VISIBLE);
				lv_search_bank_card.setVisibility(View.GONE);
				et_home_search.setSelection(query_name.length());
				if (emptyView!=null) {
					setEmptityView(0);
				}
			}
		}
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
	/**
	 * 对上拉和下拉操作的处理
	 * @param type
	 */
//	@InjectPullRefresh
//	public void call(int type) {
//		switch (type) {
//		case InjectView.PULL:
//			if (bankList != null) {
//				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
//				if (bankList.getNext_page_url()== "null"||TextUtils.isEmpty(bankList.getNext_page_url())) {
////					PullToRefreshManager.getInstance().onFooterRefreshComplete();
////					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
//					CustomToast.show(activity, "到底啦！", "该关键字下的银行卡暂时只有这么多");
//
//				} else {
//					String next_page_url = bankList.getNext_page_url();
//					int from = next_page_url.indexOf("[");
//					int to = next_page_url.indexOf("]");
//					String firstString = next_page_url.substring(0, from);
//					String lastString = next_page_url.substring(to+1, next_page_url.length());
//					next_page_url = firstString + lastString;
//					isPull = true;
//					String replaceUrl = next_page_url.replace("/?","?");
//					String forntUrl = replaceUrl.substring(0, 36);
//					int indexOfWordPage = replaceUrl.indexOf("page=");
//					int indexOfWordQuery = replaceUrl.indexOf("query=");
//					int indexOfWordAnd = replaceUrl.indexOf("&");
//					String queryText = replaceUrl.substring(indexOfWordQuery+6, indexOfWordAnd);
//					String pageText = replaceUrl.substring(indexOfWordPage+5, replaceUrl.length());
//					LinkedHashMap<String, String> params = new LinkedHashMap<String, String>() ;
//					
//					try {
//						params.put("query",URLEncoder.encode(queryText, "utf-8"));
//					} catch (UnsupportedEncodingException e) {
//						e.printStackTrace();
//					}
//					params.put("page",pageText);
//
//					//需要上拉加载出新的银行卡的时，进行的网络访问的请求
//					InternetConfig config = new InternetConfig();
//					config.setKey(0);
//					FastHttpHander.ajaxGet(forntUrl, params, config, this);//此处进行了一次网络请求，result的那种
////					PullToRefreshManager.getInstance().footerEnable();
////					PullToRefreshManager.getInstance().onFooterRefreshComplete();
//					
//					//更新当前页面的下一个页面时,前面的数据不应该被取消掉,应该拼接在后面
//					/*refreshCurrentList(next_page_url,null, 0, lv_search_bank_card);*/
//				}
//			}else{
//				CustomToast.show(activity, "bankList = null", "BankList为空！");
////				PullToRefreshManager.getInstance().onFooterRefreshComplete();
//				
//			}
//			break;
//		case InjectView.DOWN:
//
//			break;
//		}
//	}
	

	/**
	 * 处理上拉的操作时拿到数据进行展示的操作
	 * @param r
	 */
	@InjectHttp
	private void result(ResponseEntity r) {
	
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				//TODO
				
				lv_search_bank_card.onLoadMoreFished();
				
				bankList = Handler_Json.JsonToBean(BankCardList.class,r.getContentAsString());
				if(dataList!= null){
					if(isPull){
						isPull = false;
					}else{
						dataList.clear();
					}
					if (bankList.getData().size()!=0) {//服务器返回了值
						isFirst = false;
						for (BankCardData data : bankList.getData()) {
							HashMap<String, String> map = new HashMap<>();
							map.put("iv_bank_card_head", data.getBankcard().getPhoto());
							map.put("tv_bank_card_name", data.getBank().getName());
							map.put("tv_bank_card_desc", data.getBankcard().getName());
							map.put("id", data.getBankcard().getId() + "");
							dataList.add(map);
						}
						//更新查询列表页
						/*handlerPuzzy.sendEmptyMessage(2);*/ //正常刷新，更新Adapter
						
						if(bankcardAdapter == null){
							/*bankcardAdapter = new BankCardAdapter(lv_search_bank_card, dataList,
									R.layout.activity_bank_card_item, tagOfSearchBankCardFragment,mIsFromRegisterAndHaveNoBankcard);//BankCardAdapter中别有洞天
*/							//bankcardAdapter.setActivity(SearchBankCardFragment.this);
							
							bankcardAdapter = new SwipeListSearchBankcardAdapter(false,activity, dataList,
									R.layout.activity_bank_card_item_swipe_search, tagOfSearchBankCardFragment, mIsFromRegisterAndHaveNoBankcard);
							
							bankcardAdapter.setFragment(SearchBankCardFragment.this);
							lv_search_bank_card.setAdapter(bankcardAdapter);
							lv_search_bank_card.setVisibility(View.VISIBLE);
						}else{
							bankcardAdapter.notifyDataSetChanged();
							/*bankcardAdapter.setActivity(SearchBankCardFragment.this);
							lv_search_bank_card.setVisibility(View.VISIBLE);*/
						}
						if(bankList.getData().size()<10){
//							if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
//								lv_search_bank_card.onLoadMoreOverFished();
//							}else{
//								//DO-OP
//							}
							
							//lv_search_bank_card.onLoadMoreOverFished();
							//CustomToast.show(activity, "到底啦！", "该关键词下关联的 银行卡暂时只有这么多");
						}
						this.first_enter = Integer.MAX_VALUE;//当case 1 确实进行了页面的加载活动，将此标签置为非0
						
					}else {
						handlerPuzzy.sendEmptyMessage(3);//-->搜索无结果
					} 
				}else {//拿到的数据为空
					handlerPuzzy.sendEmptyMessage(1);//-->adapter.notifyDataSetChanged
				}
//				PullToRefreshManager.getInstance().onFooterRefreshComplete();
			
				
				break;

			}
		}//else{  }访问失败的处理
	}
	
	@Override//@InjectMethod(@InjectListener(ids = 2131296342, listeners = OnClick.class))
	protected void back() {
		if(App.app.getData("isEmbrassStatus").equals("true") ){
			CustomToast.show(activity, "添加至少一张银行卡吧~", "添加的银行卡不同，看到的内容也不同哦");
			return;
		}else{
			super.back();
		}
		
	}
}
