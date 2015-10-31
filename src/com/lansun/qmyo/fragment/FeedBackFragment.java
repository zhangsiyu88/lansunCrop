package com.lansun.qmyo.fragment;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class FeedBackFragment extends BaseFragment implements TextWatcher{
	private final String TAG=FeedBackFragment.class.getSimpleName();
	@InjectView
	private TextView tv_feedback_title;
	@InjectView(binders = { @InjectBinder(listeners = { OnClick.class }, method = "click") })
	private TextView btn_feedback_commit;
	@InjectView
	private EditText et_feedback_content;
	private String content;
	private String type;
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (progressDialog!=null) {
					progressDialog.dismiss();
				}
				CustomToast.show(activity, "提示", "提交成功,请您耐心等候");
				break;
			case 1:
				if (progressDialog!=null) {
					progressDialog.dismiss();
				}
				CustomToast.show(activity, "提示", "提交失败,请查看网络异常");
				break;
			}
		}
	};
	private ProgressDialog progressDialog;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_feedback, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		String title = getArguments().getString("title");
		type = getArguments().getString("type");
		tv_feedback_title.setText(title);
		et_feedback_content.addTextChangedListener(this);
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_feedback_commit:
			content = et_feedback_content.getText().toString().trim();
			if (TextUtils.isEmpty(content)) {
				CustomToast.show(activity, getString(R.string.tip), "请填写内容");
				return;
			}
			progressDialog = new ProgressDialog(activity, R.style.Translucent_NoTitle);
			progressDialog.setMessage("正在提交...");
			progressDialog.show();
			Map<String, String> paramas=new HashMap<>();
			paramas.put("content", content);
			paramas.put("type", "bankcard");
			OkHttp.asyncPost(GlobalValue.URL_USER_FEEDBACK,paramas, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						handleOk.sendEmptyMessage(0);	
					}else {
						handleOk.sendEmptyMessage(1);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {
					handleOk.sendEmptyMessage(1);
				}
			});
			break;
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
		if (!TextUtils.isEmpty(s.toString())) {
			btn_feedback_commit.setClickable(true);
			btn_feedback_commit.setTextColor(getResources().getColor(R.color.app_green1));
		}else {
			btn_feedback_commit.setClickable(false);
			btn_feedback_commit.setTextColor(Color.parseColor("#AFAFAF"));
		}	
	}
}
