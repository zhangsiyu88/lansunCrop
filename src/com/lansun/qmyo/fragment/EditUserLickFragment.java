package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
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
import com.lansun.qmyo.adapter.CommonAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 编辑用户爱好
 * 
 * @author bhxx
 * 
 */
@SuppressLint("UseSparseArrays")
public class EditUserLickFragment extends BaseFragment {
	@InjectAll
	Views v;
	private String[] likes;
	private MyAdapter adapter;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View tv_edit_user_like_commit, iv_activity_back,
				fl_comments_right_iv, tv_activity_shared;
		private GridView gv_edit_user_lick;
		private TextView tv_activity_title;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_edit_user_like, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		likes = getResources().getStringArray(R.array.likes);
		adapter = new MyAdapter();
		v.gv_edit_user_lick.setAdapter(adapter);
		
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.like, null, 0);
	}

	//每个框框对应着一个CheckBox对象
	private HashMap<Integer, CheckBox> cbs = new HashMap<Integer, CheckBox>();

	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return likes.length;
		}

		@Override
		public Object getItem(int position) {
			return cbs.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup parent) {

			CheckBox cb = (CheckBox) EditUserLickFragment.this.inflater
					.inflate(R.layout.like_toggle_button, null);
			
			//CheckBox设置选中框框的监听器
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					if (arg1) {
						arg0.setTextColor(getActivity().getResources()
								.getColor(R.color.app_white));
					} else {
						arg0.setTextColor(getActivity().getResources()
								.getColor(R.color.app_grey5));
					}
				}
			});
			
			//根据获取到的User的对象中是否有Hobby对象与String[] likes的字符数组
			if (GlobalValue.user != null) {
				if (!TextUtils.isEmpty(GlobalValue.user.getHobby())) {
					if (GlobalValue.user.getHobby().contains(likes[position])) {
						cb.setChecked(true);//影响背景
					}
				}
			}
			cb.setText(likes[position]);//-->cb
			cbs.put(position, cb);      //-->cbs
			return cb;
		}

	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_edit_user_like_commit:
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cbs.size(); i++) {
				CheckBox cb = (CheckBox) cbs.get(i);
				if (cb.isChecked()) {
					sb.append(cb.getText());
					sb.append(",");
				}
			}
			String hobby = sb.substring(0, sb.length() - 1).toString();
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("hobby", hobby);
			
			/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_SAVE, params, null,config, this);*/
			
			FastHttpHander.ajax(GlobalValue.URL_USER_SAVE, params,config, this);
			
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.user = Handler_Json.JsonToBean(User.class,
						r.getContentAsString());
				CustomToast.show(activity, getString(R.string.tip), "修改成功");
				
				/*back();*/
				EditUserFragment editUserFragment = new EditUserFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(editUserFragment);
				EventBus.getDefault().post(fEntity);
				
				break;
			}
		}
	}

}
