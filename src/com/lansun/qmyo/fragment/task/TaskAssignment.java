package com.lansun.qmyo.fragment.task;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.LoginDialog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.umeng.socialize.utils.LoadingDialog;
public class TaskAssignment extends BaseFragment implements TextWatcher{
	private RegisterFragment fragment;
	private FragmentEntity event;
	private EditText et_secretary_form_content;
	private String hint;
	private TextView btn_secretary_commit_form;
	private String type;
	private LoginDialog loginDialog;
	private RecyclingImageView iv_activity_back;
	private ProgressDialog dialogpg;
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (dialogpg!=null) {
					dialogpg.dismiss();
				}
				final Dialog dialog=new Dialog(activity,R.style.Translucent_NoTitle);
				dialog.show();
				Window window=dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setContentView(R.layout.task_dialog);
				window.findViewById(R.id.iknow).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						
						try{
							FragmentEntity entity=new FragmentEntity();
							/*Fragment f=new SecretaryFragment(); */
							Fragment f=new MainFragment(1); 
							entity.setFragment(f);
							EventBus.getDefault().post(entity);
						}catch (Exception e) {
							FragmentEntity entity=new FragmentEntity();
							Fragment f=new MainFragment(1); 
							entity.setFragment(f);
							EventBus.getDefault().post(entity);
							LogUtils.toDebugLog("catch", "提交成功后弹出的反馈页面 报出 异常");
						}
					}
				});
				break;
			}
		};
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments()!=null) {
			hint="关于任务指派的提问建议\n"+getArguments().getString("content", "");
			type=getArguments().getString("type");
		}
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		View rootView = inflater.inflate(R.layout.secretary_detail_footer, container,false);
		initView(rootView);
		setListener();
		return rootView;
	}
	private void setListener() {
		iv_activity_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		btn_secretary_commit_form.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content=et_secretary_form_content.getText().toString();
				if (TextUtils.isEmpty(content)) {
					CustomToast.show(activity, R.string.tip,
							R.string.please_enter_task);
					return;
				}
				Log.e("content", content);
				Map<String, String> map=new HashMap<String, String>();
				map.put("content", content);
				map.put("type", type);
				OkHttp.asyncPost(GlobalValue.URL_SECRETARY_QUESTION, map, new Callback() {
					@Override
					public void onResponse(Response response) throws IOException {
						handleOk.sendEmptyMessage(0);
					}
					@Override
					public void onFailure(Request arg0, IOException arg1) {

					}
				});
				dialogpg = new ProgressDialog(activity, R.style.Translucent_NoTitle);
				dialogpg.setMessage("正在提交中,请耐心等候");
				dialogpg.show();
			}
		});
	}
	private void initView(View view) {
		iv_activity_back=(RecyclingImageView)view.findViewById(R.id.iv_activity_back);
		et_secretary_form_content=(EditText)view.findViewById(R.id.et_secretary_form_content);
		et_secretary_form_content.setHint(hint);
		et_secretary_form_content.requestFocus();
		et_secretary_form_content.setHighlightColor(Color.parseColor("#AFAFAF"));
		btn_secretary_commit_form=(TextView)view.findViewById(R.id.btn_secretary_commit_form);
		et_secretary_form_content.addTextChangedListener(this);
		
		/**
		 * 此方法设置无效，对于多行的Edittext右下角为换行键
		 */
		et_secretary_form_content.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId ==EditorInfo.IME_ACTION_GO){
					//et_secretary_form_content.setImeActionLabel("提交", actionId);
					return true;
				}
				return false;
			}
			
		});
		
		
		
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
			btn_secretary_commit_form.setClickable(true);
			btn_secretary_commit_form.setTextColor(getResources().getColor(R.color.app_green1));
		}else {
			btn_secretary_commit_form.setClickable(false);
			btn_secretary_commit_form.setTextColor(Color.parseColor("#AFAFAF"));
			
			//et_secretary_form_content.setImeOptions(EditorInfo.IME_ACTION_GO);
		}
	}
}	
