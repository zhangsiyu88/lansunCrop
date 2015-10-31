package com.lansun.qmyo.fragment.bankcard;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.utils.GlobalValue;

public class SearchBankCardOkHttpFragment extends BaseFragment{
	private EditText et_home_search;
	private TextView tv_search_cancle;
	protected RecyclerView lv_search_bank_card;
	protected RelativeLayout puzz_floor;
	protected String query_name;
	private LinearLayout ll_bank_card_tip;
	private BankCardAdapter bankcardAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.activity_search_bank_card,container,false);
		initView(view);
		setListener();
		return view;
	}
	private void setListener() {
		tv_search_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getString(R.string.cancle).equals(tv_search_cancle.getText().toString())) {//文字内容为取消,则取消
					onKeyHide(v);
					back();
				} else {//否则,前去搜索 (后面会在当前界面上展示银行卡列表)
					et_home_search.clearFocus();
					lv_search_bank_card.setVisibility(View.VISIBLE);//listView展示出来
					puzz_floor.setVisibility(View.GONE);//模糊搜素的内容去除掉
					search(query_name);//带上关键字去服务器上去访问搜索
				}	
			}
		});
	}
	protected void search(String search_Name) {
		bankcardAdapter = null;
		ll_bank_card_tip.setVisibility(View.GONE);
		if (TextUtils.isEmpty(search_Name)) {
			lv_search_bank_card.setAdapter(null);
			return;
		}
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		HashMap<String, Object> head = new HashMap<>();
		if (!TextUtils.isEmpty(App.app.getData("access_token"))) {
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
		}
		config.setHead(head);
		config.setCharset("UTF-8");
		try {
			FastHttpHander.ajaxGet(GlobalValue.URL_BANKCARD_ALL
				+ "query="+ URLEncoder.encode(et_home_search.getText().toString().trim(), "utf-8"), 
				config, this);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	private void onKeyHide(View v) {
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
	}
	private void initView(View view) {
		et_home_search=(EditText)view.findViewById(R.id.et_home_search);
		et_home_search.setHint(R.string.please_enter_card);
		et_home_search.requestFocus();
		tv_search_cancle=(TextView)view.findViewById(R.id.tv_search_cancle);
		puzz_floor=(RelativeLayout)view.findViewById(R.id.puzz_floor);
	}
}
