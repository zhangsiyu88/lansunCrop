package com.lansun.qmyo.fragment.searchbrand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.serch_hot.SearchHistoryAdapter;
import com.lansun.qmyo.adapter.serch_hot.SearchHotAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.HotSearchAd;
import com.lansun.qmyo.domain.HotSearchAdData;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.listener.HotItemClickCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.MyGridLayoutManager;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class HotAddHistoryFragment extends BaseFragment implements HotItemClickCallBack{
	private SearchHistoryAdapter searchHistoryAdapter;
	private RecyclerView search_gv_list;
	private RecyclerView lv_search_history;
	private OnCallBack onCallBack;
	private TextView tv_search_clear_all;
	private List<String> hot;
	public HotAddHistoryFragment(OnCallBack onCallBack) {
		this.onCallBack=onCallBack;
	}
	private Handler handleHot=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				SearchHotAdapter adapter=new SearchHotAdapter(hot, HotAddHistoryFragment.this);
				search_gv_list.setAdapter(adapter);
				break;
			}
		};
	};
	private TextView history_tv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initSearchList();
	}
	private void initSearchList() {
		//hot搜索12小时更新一次
		getAllServer();
		searchHistoryAdapter=new SearchHistoryAdapter(App.search_list_history,this);
	}
	private void getAllServer() {
		OkHttp.asyncGet(
				GlobalValue.URL_ADVERTISEMENT_SEARCH, 
				"Authorization", "Bearer " + App.app.getData("access_token"), null, new Callback() {
					@Override
					public void onResponse(Response response) throws IOException {
						if (response.isSuccessful()) {
							HotSearchAd searchAd = Handler_Json.JsonToBean(
									HotSearchAd.class, response.body().string());
							hot = new ArrayList<String>();
							for (HotSearchAdData data : searchAd.getData()) {
								hot.add(data.getName());
							}
							handleHot.sendEmptyMessage(0);
						}else {
							CustomToast.show(activity, "提示", "服务器正在维护");
						}
					}

					@Override
					public void onFailure(Request arg0, IOException arg1) {
						//CustomToast.show(activity, "提示", "服务器正在维护");
					}
				});
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.hot_history_fagment, container,false);


		initView(view);
		return view;
	}
	private void initView(View root) {
		history_tv=(TextView)root.findViewById(R.id.history_tv);
		search_gv_list=(RecyclerView)root.findViewById(R.id.search_gv_list);
		MyGridLayoutManager manager=new MyGridLayoutManager(getActivity(), 3);
		search_gv_list.setLayoutManager(manager);
		lv_search_history=(RecyclerView)root.findViewById(R.id.lv_search_history);
		MyGridLayoutManager myGridLayoutManager=new MyGridLayoutManager(getActivity(), 1);
		lv_search_history.setLayoutManager(myGridLayoutManager);

		lv_search_history.setAdapter(searchHistoryAdapter);
		tv_search_clear_all=(TextView)root.findViewById(R.id.tv_search_clear_all);
		setHistoryState();
		tv_search_clear_all.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//				App.search_list_history.clear();
				//				searchHistoryAdapter.notifyDataSetChanged();
				//				MyGridLayoutManager myGridLayoutManager=new MyGridLayoutManager(getActivity(), 1);
				//				lv_search_history.setLayoutManager(myGridLayoutManager);
				App.search_list_history.clear();
				searchHistoryAdapter.notifyDataSetChanged();
				App.app.setData("first_history", "");
				App.app.setData("second_history", "");
				App.app.setData("third_history", "");
				setHistoryState();
			}
		});
	}
	/**
	 * 初始化搜索历史各个控件的状态
	 */
	private void setHistoryState() {
		if (App.search_list_history.size()!=0) {
			history_tv.setVisibility(View.VISIBLE);
			lv_search_history.setVisibility(View.VISIBLE);
			tv_search_clear_all.setVisibility(View.VISIBLE);
		}else {
			history_tv.setVisibility(View.GONE);
			lv_search_history.setVisibility(View.GONE);
			tv_search_clear_all.setVisibility(View.GONE);
		}
	}
	/**
	 * 全部清除和历史的按钮显示
	 */
	private void setClearAllState() {
		if (App.search_list_history.size()==0) {
			tv_search_clear_all.setVisibility(View.INVISIBLE);

		}else {
			tv_search_clear_all.setVisibility(View.VISIBLE);
		}
	}
	//	@InjectHttp
	//	private void result(ResponseEntity r) {
	//		if (r.getStatus() == FastHttp.result_ok) {
	//			switch (r.getKey()) {
	//			case 1:// 热门搜索
	//				Log.e("jsonsssss", r.getContentAsString());
	//				HotSearchAd searchAd = Handler_Json.JsonToBean(
	//						HotSearchAd.class, r.getContentAsString());
	//				List<String> hot=new ArrayList<>();
	//				for (HotSearchAdData data : searchAd.getData()) {
	//					hot.add(data.getName());
	//				}
	//				SearchHotAdapter adapter=new SearchHotAdapter(hot, this);
	//				search_gv_list.setAdapter(adapter);
	//				break;
	//			}
	//		}
	//	}
	@Override
	public void onHotItemClick(View v, int position) {
		switch (v.getId()) {
		case R.id.tv_search_hot_ad:
			onCallBack.onTextCallBack(v, position);
			break;
		case R.id.history_tv:
			onCallBack.onTextCallBack(v, position);
			break;
		case R.id.iv_history_delete:
			//			App.search_list_history.remove(position);
			//			searchHistoryAdapter.notifyDataSetChanged();
			App.search_list_history.remove(position);
			if (position==0) {
				App.app.setData("first_history", "");
			}else if(position==1) {
				App.app.setData("second_history", "");
			}else if(position==2) {
				App.app.setData("third_history", "");
			}
			setHistoryState();
			searchHistoryAdapter.notifyDataSetChanged();
			break;
		}
	}
	public void setCallBack(OnCallBack onCallBack){
		this.onCallBack=onCallBack;
	}
	public interface OnCallBack{
		void onTextCallBack(View view,int position);
	}
}
