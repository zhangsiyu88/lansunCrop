package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
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
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.search_band.BandPuzzySearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.domain.band_puzzy.BandPuzzy;
import com.lansun.qmyo.domain.band_puzzy.BandPuzzyData;
import com.lansun.qmyo.event.entity.BackEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.listener.PuzzyItemClickCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.MyGridLayoutManager;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MyListView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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
				
				setEmptityView(0);//将isFirst置为空                -->不填充 emptyView（为空图片），且将其从ListView的父布局中去除掉
				if(bankcardAdapter == null){
					bankcardAdapter = new BankCardAdapter(lv_search_bank_card, dataList,
							R.layout.activity_bank_card_item);//BankCardAdapter中别有洞天
					bankcardAdapter.setActivity(SearchBankCardFragment.this);
					lv_search_bank_card.setAdapter(bankcardAdapter);
					lv_search_bank_card.setVisibility(View.VISIBLE);
				}else{
					bankcardAdapter.notifyDataSetChanged();
					bankcardAdapter.setActivity(SearchBankCardFragment.this);
					//lv_search_bank_card.setAdapter(bankcardAdapter);
					lv_search_bank_card.setVisibility(View.VISIBLE);
				}
				break;
			case 3:
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
			}
			PullToRefreshManager.getInstance().onHeaderRefreshComplete();
			PullToRefreshManager.getInstance().onFooterRefreshComplete();
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
	@InjectView(binders = @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick"), pull = true)
	private MyListView lv_search_bank_card;
	private boolean isPull = false;
	private BankCardAdapter bankcardAdapter;
	private boolean isMove=true;
	private ProgressDialog dialogpg;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("token", App.app.getData("access_token"));
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_search_bank_card,container,false);
		initView(rootView);
		Handler_Inject.injectFragment(this, rootView);
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
	}

	private void onKeyShowAlways() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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
			v.ll_bank_card_tip.setVisibility(View.VISIBLE);
			setEmptityView(0);
			v.puzz_floor.setVisibility(View.GONE);
			del_search_content.setVisibility(View.GONE);
			lv_search_bank_card.setVisibility(View.GONE);
			et_home_search.requestFocus();
			v.tv_search_cancle.setText(R.string.cancle);
			v.tv_search_cancle.setTextColor(Color.parseColor("#939393"));
		} else {
			del_search_content.setVisibility(View.VISIBLE);
			v.tv_search_cancle.setText(R.string.search);
			v.tv_search_cancle.setTextColor(getResources().getColor(
					R.color.app_green1));
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
			if (!isExperience()) {                                      //如果是登录的正式用户
				emptyView.findViewById(R.id.tv_found_secretary).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						FeedBackFragment fragment = new FeedBackFragment();//无卡进入银行卡反馈界面
						Bundle args = new Bundle();
						args.putString("title",getString(R.string.yhk_feedback));
						args.putString("type", "bankcard");
						fragment.setArguments(args);
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
				});
			}else {                                                                                    //-->注册用户
				emptyView.findViewById(R.id.tv_found_secretary).setVisibility(View.GONE);//让下面的绿色小字去除掉
			}
		}else {//visible 为 0时，将emptyView从ListView的父布局中去除掉
			if (!isMove) {
				((ViewGroup) lv_search_bank_card.getParent()).removeView(emptyView);
				isMove=true;
			}
		}
	}
	private void getPuzzyData(String query) {
		lv_search_bank_card.setVisibility(View.GONE);
		v.puzz_floor.setVisibility(View.VISIBLE);
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
						Log.e("back", back);
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
						Log.e("info", "我是有返回值的");
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
				Log.e("hasfocus", "获得焦点");
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
	@InjectPullRefresh
	public void call(int type) {
		Log.i("上拉或者下拉的动作是否可以识别?", "说明动作能够被系统识别");
		switch (type) {
		case InjectView.PULL:
			Log.i("检测： ","上拉操作被识别");
			if (bankList != null) {
				/*if (TextUtils.isEmpty(activityList.getNext_page_url())) {//下一页为空的时候，加上footerview*/
				if (bankList.getNext_page_url()== "null"||TextUtils.isEmpty(bankList.getNext_page_url())) {
					Log.i("检测： ","下一页已经消失！");
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();//此处关闭上拉的操作
					CustomToast.show(activity, "到底啦！", "该关键字下的银行卡暂时只有这么多");

				} else {
					String next_page_url = bankList.getNext_page_url();
					int from = next_page_url.indexOf("[");
					int to = next_page_url.indexOf("]");
					String firstString = next_page_url.substring(0, from);
					String lastString = next_page_url.substring(to+1, next_page_url.length());
					next_page_url = firstString + lastString;

					Log.i("下一页的地址为：","第一部分："+firstString);
					Log.i("下一页的地址为：","第二部分："+lastString);
					Log.i("下一页的地址为：","下一页合并好的银行卡地址为："+next_page_url);

					isPull = true;

					String replaceUrl = next_page_url.replace("/?","?");
					Log.i("replaceUrl的地址为："  ,  "replaceUrl："+ replaceUrl);
					String forntUrl = replaceUrl.substring(0, 36);
					Log.i("forntUrl的地址为："  ,  "forntUrl："+ forntUrl);

					int indexOfWordPage = replaceUrl.indexOf("page=");
					int indexOfWordQuery = replaceUrl.indexOf("query=");
					int indexOfWordAnd = replaceUrl.indexOf("&");

					String queryText = replaceUrl.substring(indexOfWordQuery+6, indexOfWordAnd);
					Log.i("queryText为："  ,  "queryText："+ queryText);
					String pageText = replaceUrl.substring(indexOfWordPage+5, replaceUrl.length());
					Log.i("pageText为："  ,  "pageText："+ pageText);
					try {
						Log.i("queryTextUtfEncode为："  ,  "queryTextUtfEncode："+ URLEncoder.encode( queryText, "utf-8"));
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}

					LinkedHashMap<String, String> params = new LinkedHashMap<String, String>() ;
					try {
						params.put("query",URLEncoder.encode(queryText, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						Log.i("","大家快来抓bug");
					}
					params.put("page",pageText);

					InternetConfig config = new InternetConfig();
					config.setKey(0);
					FastHttpHander.ajaxGet(forntUrl, params, config, this);
					//更新当前页面的下一个页面时,前面的数据不应该被取消掉,应该拼接在后面
					/*refreshCurrentList(next_page_url,null, 0, lv_search_bank_card);*/

					Log.i("","上拉去获取新的一次银行卡列表的值");
					Log.i("检测： ","走的是刷新列表的操作！");
				}
			}else{
				CustomToast.show(activity, "bankList = null", "BankList为空！");
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
			}
			break;
		case InjectView.DOWN:

			break;
		}
	}


}
