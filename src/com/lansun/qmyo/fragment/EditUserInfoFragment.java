package com.lansun.qmyo.fragment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 编辑用户信息的Fragment
 * 
 * @author bhxx
 * 
 */
public class EditUserInfoFragment extends BaseFragment {

	@InjectAll
	Views v;
	private String name;
	private String paramName;
	private String fragment_name;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_shared;
		private TextView tv_edit_title, tv_activity_title;
		private EditText et_edit_content;
		private View fl_comments_right_iv;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
	
		View rootView = inflater.inflate(R.layout.activity_edit_userinfo, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		name = getArguments().getString("name");
		paramName = getArguments().getString("paramName");
		fragment_name=getArguments().getString("fragment_name");
		v.tv_activity_shared.setText(R.string.save);
		initTitle(v.tv_activity_title, name, null, 0);
		v.fl_comments_right_iv.setVisibility(View.GONE);
		switch (paramName) {
		case "email":
			if(GlobalValue.user.getEmail()=="null"||GlobalValue.user.getEmail()==null||GlobalValue.user.getEmail().isEmpty()){
				v.et_edit_content.setHint("请输入您希望绑定的邮箱");
			}else{
				v.et_edit_content.setText(GlobalValue.user.getEmail());
				v.et_edit_content.setSelection(String.valueOf(GlobalValue.user.getEmail()).length());
			}
			break;
		case "truename":
			if(GlobalValue.user.getTruename()=="null"||GlobalValue.user.getTruename()==null||GlobalValue.user.getTruename().isEmpty()){
				v.et_edit_content.setHint("请输入您的真实姓名");
			}else{
				v.et_edit_content.setText(GlobalValue.user.getTruename());
				v.et_edit_content.setSelection(String.valueOf(GlobalValue.user.getTruename()).length());
			}
			break;
		case "nickname":
			if(GlobalValue.user.getNickname()=="null"||GlobalValue.user.getNickname()==null||
			GlobalValue.user.getNickname().isEmpty()||GlobalValue.user.getNickname().contains("null")){
				v.et_edit_content.setHint("请输入您的昵称");
			}else{
				v.et_edit_content.setText(GlobalValue.user.getNickname());
				v.et_edit_content.setSelection(String.valueOf(GlobalValue.user.getNickname()).length());
			}
			break;
		}
		autoHiddenKey();
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_activity_shared:
			if (TextUtils.isEmpty(v.et_edit_content.getText().toString().trim())) {
				//输入框为空但点击提交，要提示
				CustomToast.show(activity, R.string.tip,R.string.please_enter_content);
				return;
			}
			if ("email".equals(paramName)) {
				if (!isEmail(v.et_edit_content.getText().toString())) {//isEail()用来判断是否符合邮件的正则形式
					CustomToast.show(activity, R.string.tip,R.string.email_faild);
					return;
				}
			}

			InternetConfig config = new InternetConfig();
			config.setKey(0);
			
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put(paramName, v.et_edit_content.getText().toString());
			pd.setMessage("保存中,请耐心等候...");
			pd.show();
			/*
			将用户的nickname以表单形式提交上去并保存
			 */
			FastHttpHander.ajax(GlobalValue.URL_USER_SAVE, params,config, this);
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				if(pd!=null ){
					pd.dismiss();
				}
				
				GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());
				CustomToast.show(activity, getString(R.string.tip), "修改成功");
				//App.app.setData("userNickname", paramName);
				
				if(paramName.equals("nickname")){
					GlobalValue.user.setNickname(v.et_edit_content.getText().toString());
				}else if(paramName.equals("truename")){
					GlobalValue.user.setTruename(v.et_edit_content.getText().toString());
				}else if(paramName.equals("email")){
					GlobalValue.user.setEmail(v.et_edit_content.getText().toString());
				}
				
				
				//BaseFragment中设置的一个方法，功能是：将自己从FragmentManager中弹出，类似于Activity中的销毁操作
				/*back();*/
				
				if(paramName.equals("nickname")){
					PersonCenterFragment personCenterFragment = new PersonCenterFragment();
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(personCenterFragment);
					EventBus.getDefault().post(fEntity);
				}else{
					EditUserFragment editUserFragment = new EditUserFragment();
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(editUserFragment);
					EventBus.getDefault().post(fEntity);
				}
				break;
			}
		}
	}

	public static boolean isEmail(String strEmail) {
		/*String strPattern = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";*/
		
		//下面这个是企业级的邮箱 正则表达式
		String strPattern  = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}
}
