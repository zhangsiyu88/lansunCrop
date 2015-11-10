package com.lansun.qmyo.fragment;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.mapcore2d.an;
import com.android.pc.ioc.event.EventBus;
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
import com.lansun.qmyo.domain.QuestionAnswerDetail;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.event.entity.FragmentEntity;
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
public class QuestionDetailFragment extends BaseFragment implements RequestCallBack,OnFocusChangeListener{
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
	class Views {
		private ImageView iv_activity_back;
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title;
		private EditText et_secretary_question;
	}
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String type=switchType(list.getType());
				v.tv_activity_title.setText(GlobalValue.mySecretary.getName()+"["+type+"]");
				my_secretary_question_recycle.setAdapter(adapter);
				my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
				v.et_secretary_question.setOnFocusChangeListener(QuestionDetailFragment.this);
				break;
			case 1:
				v.et_secretary_question.setText("");
				QuestionAnswerDetail detail=new QuestionAnswerDetail();
				detail.setContent(question);
				list.getItems().add(list.getItems().size(), detail);
				adapter.notifyDataSetChanged();
				my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
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

	private String question;	@Override
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
				commit();
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
					Gson gson=new Gson();
					list=gson.fromJson(json, QuestionDetail.class);
					currentType=list.getType();
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
		v.iv_activity_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentEntity entity=new FragmentEntity();
				Fragment fragment=new MineSecretaryFragment();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
			}
		});
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
		question = v.et_secretary_question.getText().toString();
		biz.sendQuestion(question, currentType, question_id+"", this);
	}
	private String switchType(String type) {
		switch (type) {
		case "travel":
			return getResources().getString(R.string.travel_holiday);
		case "shopping":
			return getResources().getString(R.string.new_shopping);
		case "party":
			return getResources().getString(R.string.shengyan_part);
		case "life":
			return getResources().getString(R.string.life_quality);
		case "student":
			return getResources().getString(R.string.studybroad);
		case "investment":
			return getResources().getString(R.string.investment);
		case "card":
			return getResources().getString(R.string.handlecard);
		}
		return "";
	}

	@Override
	public void onResponse(Response response) throws IOException {
		if (response.isSuccessful()) {
			String json=response.body().string();
			if (json.contains("true")) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.et_secretary_question.getWindowToken(), 0); 
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
	class MessageReplayBraodCast extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.lansun.qmyo.fragment.questionDetailFragment".equals(intent.getAction())) {

			}
		}
	}
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		Log.e("focus", hasFocus+"");
		my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
	}
}
