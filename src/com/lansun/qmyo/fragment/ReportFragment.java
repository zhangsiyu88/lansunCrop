package com.lansun.qmyo.fragment;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.base.BackHandedFragment;
import com.lansun.qmyo.port.BackHanderInterface;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 举报
 * 
 * @author bhxx
 * 
 */
public class ReportFragment extends BackHandedFragment {

	
	
	@InjectAll
	Views v;
	private String shopId;
	private String activityId;
	private int type;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  fl_comments_right_iv,
				btn_activity_report_commit;
		private TextView tv_activity_title,textView1;
		private EditText et_activity_report_content;
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_report, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}
	
	

	@InjectInit
	private void init() {
		autoHiddenKey();
		Bundle arguments = getArguments();
		if (arguments != null) {
			shopId = arguments.getString("shop_id");
			activityId = arguments.getString("activity_id");
			type = arguments.getInt("type");
		}
		

		int titileRes = R.string.report_activity;
		switch (type) {
		case 0:
			titileRes = R.string.report_activity;
			v.textView1.setText(R.string.report_content);
			if(!TextUtils.isEmpty(App.app.getData("_activity_report"))){
				v.et_activity_report_content.setText(App.app.getData("_activity_report"));
			}
			break;
		case 1:
			titileRes = R.string.report_store;
			v.textView1.setText(R.string.report_store_content);
			if(!TextUtils.isEmpty(App.app.getData("_shop_report"))){
				v.et_activity_report_content.setText(App.app.getData("_shop_report"));
			}
			break;

		}
		initTitle(v.tv_activity_title, titileRes, null, 0);
		v.fl_comments_right_iv.setVisibility(View.GONE);

	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_activity_report_commit:
			if (TextUtils.isEmpty(v.et_activity_report_content.getText()
					.toString())) {
				CustomToast.show(activity, R.string.tip,
						R.string.please_enter_content);
				return;
			}
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			
			
			String shopComplainUrl = GlobalValue.SHOP_COMPLAIN;
			String activityComplainUrl = GlobalValue.ACTIVITY_COMPLAIN;
			 String complainUrl = "";
			
			params.put("content", v.et_activity_report_content.getText().toString());
			params.put("shop_id", shopId);
			
		/*	FastHttpHander.ajaxForm(GlobalValue.ACTIVITY_COMPLAIN, params,
					null, config, this);*/
			
			if (!TextUtils.isEmpty(activityId)) {
				params.put("activity_id", activityId);
				complainUrl = activityComplainUrl;
			}else{
				complainUrl = shopComplainUrl;
			}
			Log.i("提交上去的url为", "提交上去的url为："+ complainUrl );
			FastHttpHander.ajax(complainUrl, params, config, this);
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, R.string.tip,
							R.string.report_success);
					v.et_activity_report_content.setText("");
					back();
				}
				break;
			}
		}

	}
	
	@Override
	@InjectMethod(@InjectListener(ids = 2131361895, listeners = OnClick.class))
	protected void back() {
		/*if(!TextUtils.isEmpty(v.et_activity_report_content.getText().toString())){
			Toast.makeText(activity, "内容存至草稿箱", Toast.LENGTH_LONG).show();
			if (!TextUtils.isEmpty(activityId)) {//活动举报
				App.app.setData("_activity_report", v.et_activity_report_content.getText().toString());
			}else{//投诉门店
				App.app.setData("_shop_report", v.et_activity_report_content.getText().toString());
			}
		}else{
			if(type==0){//举报活动
				App.app.setData("_activity_report","");
			}else if(type==1){//投诉门店
				App.app.setData("_shop_report", "");
			}
		}*/
		if(v.et_activity_report_content.getEditableText().length()>0){
			DialogUtil.createTipAlertDialog(getActivity(), "确认放弃已编辑内容？",new TipAlertDialogCallBack() {
				@Override
				public void onPositiveButtonClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					getFragmentManager().popBackStack();
				}
				@Override
				public void onNegativeButtonClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return;
		}
		super.back();
	}



	/**
	 * 执行物理返回键的拦截操作,消费掉此次的物理返回事件
	 */
	@Override
	public boolean onBackPressed(){
		back();
		return true;
	}
}
