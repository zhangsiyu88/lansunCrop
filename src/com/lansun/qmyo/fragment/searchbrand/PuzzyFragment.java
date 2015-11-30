package com.lansun.qmyo.fragment.searchbrand;
import java.io.IOException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.serch_hot.PuzzyListAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.PuzzyData;
import com.lansun.qmyo.domain.PuzzySearch;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.listener.PuzzyItemClickCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
public class PuzzyFragment extends BaseFragment implements PuzzyItemClickCallBack{
	private RecyclerView puzzy_search_list;
	private List<PuzzySearch> list;
	private String searchName;
	private Handler h=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				PuzzyListAdapter adapter=new PuzzyListAdapter(list,PuzzyFragment.this);
				puzzy_search_list.setAdapter(adapter);
				break;
			}
		};
	};
	private OnPuzzyClickCallBack callBack;
	public PuzzyFragment(OnPuzzyClickCallBack callBack) {
		this.callBack=callBack;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments()!=null) {
			searchName=getArguments().getString("searchName");
			ontextChange(searchName);
		}
		//注册广播
		MyBroadCastReceiver broadCastReceiver=new MyBroadCastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("com.qmyo.puzzysearch");
		getActivity().registerReceiver(broadCastReceiver, filter);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		
		View view=inflater.inflate(R.layout.puzzy_fragment, container,false);
		initView(view);
		return view;
	}
	private void initView(View v) {
		puzzy_search_list=(RecyclerView)v.findViewById(R.id.puzzy_search_list);
		LinearLayoutManager manager=new LinearLayoutManager(getActivity());
		puzzy_search_list.setLayoutManager(manager);
	}
	public void ontextChange(final String text) {
		Log.e("token", App.app.getData("access_token"));
		
		OkHttp.asyncGet(GlobalValue.URL_ACTIVITY_PUZZY+"search="+text, "Authorization", "Bearer " + App.app.getData("access_token"), null, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				
				LogUtils.toDebugLog("搜索的地址： ",GlobalValue.URL_ACTIVITY_PUZZY+"search="+text);
				
				if (response.isSuccessful()) {
					String json=response.body().string();
					if (json.contains("{")&&json.contains("}")) {       //对服务器返回回来的json重新做了拼接，并解析操作
						json="{list:"+json+"}";
						Gson gson=new Gson();
						PuzzyData data=gson.fromJson(json, PuzzyData.class);
						list=data.getList();
						h.sendEmptyMessage(0);
					}else {
						String back="{list:[{"+"name: "+"查找："+searchName+"}]}";   //返回值为 空 
						back=back.replace(" ", "");
						Gson gson=new Gson();
						PuzzyData data=gson.fromJson(back, PuzzyData.class);
						list=data.getList();
						h.sendEmptyMessage(0);
					}
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {

			}
		});
	}
	@Override
	public void onPuzzyItemClick(View v, int position) {
		String backString=((TextView)v).getText().toString();
		if (backString.contains("查找：")) {
			backString=backString.substring(3,backString.length());
		}
		callBack.onPuzzCallBack(backString);
	}
	
	
	private class MyBroadCastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("com.qmyo.puzzysearch")){
				//当输入框产生字符修改时，需实时将修改完的字符跑过去访问服务器
				searchName = intent.getStringExtra("searchName");
				ontextChange(searchName);
				
			}
		}
	}
	public interface OnPuzzyClickCallBack{
		void onPuzzCallBack(String sel_name);
	}
}
