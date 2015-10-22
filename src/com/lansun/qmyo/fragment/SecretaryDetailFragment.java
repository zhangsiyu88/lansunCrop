package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;

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
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.SecretaryDetailAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.LoginDialog;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TaskSuccessDialog;
import com.lansun.qmyo.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**
 * 秘书指派任务界面
 * 
 * @author bhxx
 * 
 */
public class SecretaryDetailFragment extends BaseFragment implements
		android.view.View.OnClickListener {

	@InjectAll
	Views v;

	private EditText et_secretary_form_content;

	private String type;

	private String title;

	private String hint;

	class Views {
		private View ll_float_menu;
		private MyListView lv_secretary_detail;
		private TextView tv_secretary_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_secretary_open;
		private RecyclingImageView iv_secretary_detail_more;
	}

	private TextView tv_header_secretary_title;

	private int[] images;

	private RecyclingImageView iv_header_secretary_more;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_secretary_detail,
				null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {

		header = inflater.inflate(R.layout.secretary_detail_header, null);
		View footer = inflater.inflate(R.layout.secretary_detail_footer, null);
		v.lv_secretary_detail.addHeaderView(header);
		v.lv_secretary_detail.addFooterView(footer);
		header.setVisibility(View.VISIBLE);
		footer.setVisibility(View.VISIBLE);
		tv_header_secretary_title = (TextView) v.lv_secretary_detail
				.findViewById(R.id.tv_header_secretary_title);
		v.lv_secretary_detail.findViewById(R.id.iv_activity_back)
				.setOnClickListener(this);
		et_secretary_form_content = (EditText) v.lv_secretary_detail
				.findViewById(R.id.et_secretary_form_content);
		iv_secretary_head = (CircularImage) v.lv_secretary_detail
				.findViewById(R.id.iv_secretary_head);
		iv_header_secretary_more = (RecyclingImageView) v.lv_secretary_detail
				.findViewById(R.id.iv_header_secretary_more);
		v.lv_secretary_detail.findViewById(R.id.iv_header_secretary_open)
				.setOnClickListener(this);
		v.lv_secretary_detail.findViewById(R.id.iv_secretary_help)
				.setOnClickListener(this);
		v.lv_secretary_detail.findViewById(R.id.btn_secretary_commit_form)
				.setOnClickListener(this);
		adapter = new SecretaryDetailAdapter(v.lv_secretary_detail, dataList,
				R.layout.secretary_detail_item, this);
		v.lv_secretary_detail.setAdapter(adapter);
		v.lv_secretary_detail.setOnScrollListener(onScrollListener);

		Bundle arguments = getArguments();
		if (arguments != null) {
			type = arguments.getString("type");
			title = arguments.getString("title");
			hint = arguments.getString("hint");
			images = arguments.getIntArray("images");
			if (GlobalValue.secretary != null) {
				initData();
			}
			et_secretary_form_content.setHint(hint);
		}
		if (GlobalValue.secretary == null) {
			refreshCurrentList(GlobalValue.URL_SECRETARY, null, 0, null);
		}

		v.iv_secretary_open.setOnClickListener(this);
	}

	private void initData() {
		loadPhoto(GlobalValue.secretary.getAvatar(), iv_secretary_head);
		v.tv_secretary_title.setText(String.format(title,
				GlobalValue.secretary.getOwner_name()));
		tv_header_secretary_title.setText(String.format(title,
				GlobalValue.secretary.getOwner_name()));
	}

	OnScrollListener onScrollListener = new OnScrollListener() {

		@SuppressLint("NewApi")
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
				View currentFocus = getActivity().getCurrentFocus();
				if (currentFocus != null) {
					currentFocus.clearFocus();
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			int[] location = new int[2];
			v.ll_float_menu.getLocationInWindow(location);
			int hiddenY = location[1] + v.ll_float_menu.getHeight();
			View searchView = v.lv_secretary_detail
					.findViewById(R.id.ll_header_title);
			int currY = getLocation(searchView);
			if (currY < 0) {
				v.ll_float_menu.setVisibility(View.VISIBLE);
			} else {
				v.ll_float_menu.setVisibility(View.GONE);
			}
		}
	};

	public int getLocation(View v) {
		int[] loc = new int[4];
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		loc[0] = location[0];
		loc[1] = location[1];
		return loc[1];
	}

	boolean isShow;
	private SecretaryDetailAdapter adapter;
	private ArrayList<HashMap<String, Integer>> dataList = new ArrayList<HashMap<String, Integer>>();

	@Override
	public void onClick(View view) {
		v.lv_secretary_detail.findViewById(R.id.iv_activity_back)
				.setVisibility(View.VISIBLE);
		switch (view.getId()) {
		case R.id.iv_secretary_help:// 帮助
			Fragment fragment = new AboutFragment();
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		case R.id.iv_activity_back:// 帮助
			back();
			break;
		case R.id.btn_secretary_commit_form:// 任务指派
			if ("true".equals(App.app.getData("isExperience"))) {
				fragment = new RegisterFragment();
				event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			} else {//登录用户
				if (GlobalValue.user == null) {
					LoginDialog loginDialog = new LoginDialog();
					loginDialog.show(getFragmentManager(), "login");
				} else {
					
					if (TextUtils.isEmpty(et_secretary_form_content.getText()
							.toString())) {
						CustomToast.show(activity, R.string.tip,
								R.string.please_enter_task);
						return;
					} else {
						InternetConfig config = new InternetConfig();
						config.setKey(1);
						HashMap<String, Object> head = new HashMap<>();
						head.put("Authorization",
								"Bearer " + App.app.getData("access_token"));
						config.setHead(head);
						LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
						
						params.put("content", et_secretary_form_content.getText().toString().trim());
						params.put("type", type);
						
						System.out.println(params.toString());
						
						//提交意见的代码
						/*FastHttpHander.ajaxForm(GlobalValue.URL_SECRETARY_QUESTION, params,null, config, this);*/
						
					
						HttpUtils httpUtils = new HttpUtils();
						RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
							@Override
							public void onFailure(HttpException arg0,String arg1) {
								Log.i("拿到的结果","返回结果错误拿到的结果:"+arg1.toString());
							}
							@Override
							public void onSuccess(ResponseInfo<String> arg0) {
								Log.i("拿到的结果","返回结果成功拿到的结果:"+arg0.result.toString());
								final TaskSuccessDialog dialog = new TaskSuccessDialog();
								dialog.show(getFragmentManager(), "task_success");
								
								final Timer t = new Timer();
							               t.schedule(new TimerTask() {
							                    public void run() {
							                    	dialog.dismiss(); 
								                   }}, 2000); 
							}
						};
						RequestParams requestParams = new RequestParams();
						requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
						requestParams.addBodyParameter("content", et_secretary_form_content.getText().toString().trim());
						requestParams.addBodyParameter("type", type);
						httpUtils.send(HttpMethod.POST,GlobalValue.URL_SECRETARY_QUESTION, requestParams, requestCallBack );
					}
				}
			}
			break;
		case R.id.iv_secretary_open:
			et_secretary_form_content.clearFocus();
			if (!isShow) {
				for (int i = 0; i < images.length; i++) {
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					map.put("iv_secretary_pic", images[i]);
					dataList.add(map);
				}
				adapter.notifyDataSetChanged();
				isShow = true;
				v.iv_secretary_detail_more
						.setImageResource(R.drawable.secretary_more_close);
				iv_header_secretary_more
						.setImageResource(R.drawable.secretary_more_close);
			} else {
				dataList.clear();
				adapter.notifyDataSetChanged();
				v.iv_secretary_detail_more
						.setImageResource(R.drawable.secretary_more);
				iv_header_secretary_more
						.setImageResource(R.drawable.secretary_more);
				isShow = false;
			}
			break;
		case R.id.iv_header_secretary_open:
			et_secretary_form_content.clearFocus();
			if (!isShow) {
				for (int i = 0; i < images.length; i++) {
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					map.put("iv_secretary_pic", images[i]);
					dataList.add(map);
				}
				adapter.notifyDataSetChanged();
				isShow = true;
				iv_header_secretary_more
						.setImageResource(R.drawable.secretary_more_close);
				v.iv_secretary_detail_more
						.setImageResource(R.drawable.secretary_more_close);
			} else {
				dataList.clear();
				adapter.notifyDataSetChanged();
				isShow = false;
				iv_header_secretary_more
						.setImageResource(R.drawable.secretary_more);
				v.iv_secretary_detail_more
						.setImageResource(R.drawable.secretary_more);
			}
			break;
		}
	}

	private View header;

	private CircularImage iv_secretary_head;

	@InjectHttp
	private void result(ResponseEntity r) {
		 
		System.out.println(r.getContentAsString());
		
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 1:
				if ("true".equals(r.getContentAsString())) {
					TaskSuccessDialog dialog = new TaskSuccessDialog();
					dialog.show(getFragmentManager(), "task_success");
				} else {
					CustomToast.show(activity, R.string.tip,
							R.string.task_faild);
				}
				break;
			case 0:
				GlobalValue.secretary = Handler_Json.JsonToBean(
						Secretary.class, r.getContentAsString());
				initData();
				break;
			}
		}
	}
}
