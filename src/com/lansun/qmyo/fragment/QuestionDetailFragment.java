package com.lansun.qmyo.fragment;

import java.io.IOException;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.mapcore2d.an;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.question.QuestionAnswerAdapter;
import com.lansun.qmyo.biz.AddQuestionBiz;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 *我的私人秘书信息详情
 * 
 * @author bhxx
 * 
 */
public class QuestionDetailFragment extends BaseFragment implements RequestCallBack{
	@InjectView(down = true, pull = false)
	private RecyclerView my_secretary_question_recycle;
	@InjectAll
	Views v;
	private String question_id;
	private QuestionDetail list;
	private String currentType;
	private ProgressDialog pd;
	private QuestionAnswerAdapter adapter;
	private TextView btn_secretary_question_commit;
	protected boolean canSend;
	class Views {
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title,tv_mine_secretary_type;
		private EditText et_secretary_question;
	}
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				int res_id=switchType(list.getType());
				v.tv_mine_secretary_type.setText(res_id);
				my_secretary_question_recycle.setAdapter(adapter);
				my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
				if (list.getItems().size()==0) {
					String answer=String.valueOf(list.getAnswer());
					if ("".equals(answer)||"null".equals(answer)) {
						v.et_secretary_question.setFocusable(false);
						canSend=false;
					}else {
						v.et_secretary_question.setFocusable(true);
						canSend=true;
					}
				}else {
					String answer_item=String.valueOf(list.getItems().get(list.getItems().size()-1).getAnswer());
					if("".equals(answer_item)||"null".equals(answer_item)){
						v.et_secretary_question.setFocusable(false);
						canSend=false;
					}else{
						v.et_secretary_question.setFocusable(true);
						canSend=true;
					}
				}
				break;
			case 1:
				CustomToast.show(activity, R.string.tip,"提交成功");
				break;
			case 2:
				CustomToast.show(activity, R.string.tip,"提交失败");
				break;
			}
			endProgress();
			if(pd!=null){
				pd.dismiss();
			}
		};
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments()!=null) {
			question_id=getArguments().getString("question_id");
			refreshUrl = GlobalValue.URL_SECRETARY_QUESTION_DETAIL + question_id;
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(
				R.layout.activity_secretary_question_detail,container,false);
		initView(rootView);
		getNewAnswer(refreshUrl);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}
	private void initView(View view) {
		my_secretary_question_recycle=(RecyclerView)view.findViewById(R.id.lv_mine_secretary_quetions_detail);
		LinearLayoutManager manager=new LinearLayoutManager(getActivity());
		my_secretary_question_recycle.setLayoutManager(manager);
		btn_secretary_question_commit=(TextView)view.findViewById(R.id.btn_secretary_question_commit);
		btn_secretary_question_commit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (canSend) {
					commit();
				}else {
					CustomToast.show(getActivity(), R.string.tip_send_question, "请耐心等待哟");
				}
			}
		});
	}
	/**
	 * 联网获取最新数据
	 * @param url
	 */
	private void getNewAnswer(String url) {
		setProgress(my_secretary_question_recycle);
		startProgress();
		OkHttp.asyncGet(url, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String json=response.body().string();
					json=json.replace(" ", "");
					Log.e("detail", json);
					Gson gson=new Gson();
					list=gson.fromJson(json, QuestionDetail.class);
					adapter=new QuestionAnswerAdapter(list);
					handleOk.sendEmptyMessage(0);
				}
			}
			@Override
			public void onFailure(Request request, IOException arg1) {
				
			}
		});
	}
	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_title.setText(GlobalValue.mySecretary.getName());
		v.et_secretary_question.setFocusable(false);
	}


	/**
	 * 追加提问
	 */
	private void commit() {
		if (TextUtils.isEmpty(v.et_secretary_question.getText().toString()
				.trim())) {
			CustomToast.show(activity, R.string.tip,
					R.string.please_enter_content);
			return;
		}
		pd = new ProgressDialog(activity);
		pd.setMessage(getString(R.string.up_dataing));
		pd.show();
		AddQuestionBiz biz=new AddQuestionBiz();
		biz.sendQuestion(v.et_secretary_question.getText().toString(), currentType, question_id+"", this);
	}
	private int switchType(String type) {
		int resId = R.string.travel_holiday;
		switch (type) {
		case "travel":
			resId = R.string.travel_holiday;
			break;
		case "shopping":
			resId = R.string.new_shopping;
			break;
		case "party":
			resId = R.string.shengyan_part;
			break;
		case "life":
			resId = R.string.life_service;
			break;
		case "student":
			resId = R.string.studybroad;
			break;
		case "investment":
			resId = R.string.investment;
			break;
		case "card":
			resId = R.string.handlecard;
			break;
		}
		return resId;
	}
	@InjectPullRefresh
	private void call(int type) {
		switch (type) {
		case InjectView.DOWN:
			if (list != null) {
				
			}
			break;
		}
	}
	@Override
	public void onResponse(Response response) throws IOException {
		if (response.isSuccessful()) {
			String json=response.body().string();
			if (json.contains("true")) {
				handleOk.sendEmptyMessage(1);
			}else {
				handleOk.sendEmptyMessage(2);
			}
		}
	}
	@Override
	public void onFailure(Request request, IOException exception) {
		handleOk.sendEmptyMessage(2);
	}
}
