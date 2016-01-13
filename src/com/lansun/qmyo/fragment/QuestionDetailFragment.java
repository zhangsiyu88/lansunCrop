package com.lansun.qmyo.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.R.integer;
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
import com.lansun.qmyo.adapter.question.QuestionMultiAnswerAdapter;
import com.lansun.qmyo.biz.AddQuestionBiz;
import com.lansun.qmyo.domain.QAMetaData;
import com.lansun.qmyo.domain.QuestionAnswerDetail;
import com.lansun.qmyo.domain.QuestionAnswerDetailNew;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.domain.QuestionDetailNew;
import com.lansun.qmyo.domain.SubAnswer;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
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
	
	
	@InjectView
	private RecyclerView my_secretary_question_recycle;//(down = true, pull = false)
	@InjectAll
	Views v;
	private String question_id;
	private QuestionDetail list;
	private QuestionDetailNew newList;
	private QuestionMultiAnswerAdapter newAdapter;
	private ArrayList<QAMetaData> processList;
	private int HISTORY_VER = 0;
	private int NEW_VER = 1;
	
	private int type = 1;//默认进来已新的数据格式 解析返回回来的数据内容
	

	private String currentType;
	private ProgressDialog pd;
	private QuestionAnswerAdapter adapter;
	private TextView btn_secretary_question_commit;
	class Views {
		private ImageView iv_activity_back;
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title;
		private EditText et_secretary_question;
		private TextView tv_conversation_type;
	}
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				
				if(type == HISTORY_VER){
					String type=switchType(list.getType());
					v.tv_activity_title.setText(GlobalValue.mySecretary.getName()+"["+type+"]");
					my_secretary_question_recycle.setAdapter(adapter);
					my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);//刷新后强制滑动至消息列表上的最后一个位置
					v.et_secretary_question.setOnFocusChangeListener(QuestionDetailFragment.this);
					
				}else if(type == NEW_VER){
					String type=switchType(currentType);
					//v.tv_activity_title.setText(GlobalValue.mySecretary.getName()+"["+type+"]");
					v.tv_activity_title.setText(GlobalValue.mySecretary.getName());
					
					/* 此处无需再次将其展示，已经将标题放入列表的头部
					 * v.tv_conversation_type.setVisibility(View.VISIBLE);
					v.tv_conversation_type.setText(type);*/
					
					my_secretary_question_recycle.setAdapter(newAdapter);
					my_secretary_question_recycle.scrollToPosition(newAdapter.getItemCount()-1);//刷新后强制滑动至消息列表上的最后一个位置
					v.et_secretary_question.setOnFocusChangeListener(QuestionDetailFragment.this);
				}
				
		//暂时 关闭		
//				/*String type=switchType(list.getType());
//				v.tv_activity_title.setText(GlobalValue.mySecretary.getName()+"["+type+"]");
//				my_secretary_question_recycle.setAdapter(adapter);
//				my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);//刷新后强制滑动至消息列表上的最后一个位置
//				v.et_secretary_question.setOnFocusChangeListener(QuestionDetailFragment.this);*/
//				
//				
//				String type=switchType(currentType);
//				v.tv_activity_title.setText(GlobalValue.mySecretary.getName()+"["+type+"]");
//				my_secretary_question_recycle.setAdapter(newAdapter);
//				my_secretary_question_recycle.scrollToPosition(newAdapter.getItemCount()-1);//刷新后强制滑动至消息列表上的最后一个位置
//				v.et_secretary_question.setOnFocusChangeListener(QuestionDetailFragment.this);
				break;
			case 1:
				if(type == HISTORY_VER){
					v.et_secretary_question.setText("");
					QuestionAnswerDetail detail = new QuestionAnswerDetail();
					detail.setContent(question);
					list.getItems().add(list.getItems().size(), detail);
					adapter.notifyDataSetChanged();
					my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
				}else if(type == NEW_VER){
					v.et_secretary_question.setText("");
					QAMetaData qaMetaData = new QAMetaData();
					SimpleDateFormat format=new SimpleDateFormat("HH");
					final int hour=Integer.valueOf(format.format(new Date(System.currentTimeMillis())));
					if((hour>=9) && (hour<18)){
						qaMetaData.setAnswer("收到啦，给我点点时间来处理~爱你哟~我们的工作时间：周一至周五工作日9:00-18:00（周末及法定节假日休息），小秘书将逐步实现7*24无休服务。");
					}
					else if(((hour>=0) && (hour<9))||((hour>=18)&&(hour<=24))){
						qaMetaData.setAnswer("收到您的留言喽~但但但...人家现在正休息，为了养足精神更好为您服务哦~小秘书开工后立即处理（周一至周五工作日9:00-18:00），谢谢体谅哟~小秘书将逐步实现7*24无休服务。");
					}
					qaMetaData.setContent(question);
					processList.add(qaMetaData);
					newAdapter.notifyDataSetChanged();
					my_secretary_question_recycle.scrollToPosition(newAdapter.getItemCount()-1);
				}
				
				
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

	private String question;
	private String simpleAnswer;	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments()!=null) {
			question_id=getArguments().getString("question_id");
			
			Log.d("question_id", "question_id"+question_id);
			
			refreshUrl = GlobalValue.URL_SECRETARY_QUESTION_DETAIL + question_id;
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_secretary_question_detail,container,false);
		Handler_Inject.injectFragment(this, rootView);
		v.tv_activity_title.setText("");
		
		initView(rootView);
		getNewAnswer(refreshUrl);
		return rootView;
	}
	private void initView(View view) {
		
		my_secretary_question_recycle=(RecyclerView)view.findViewById(R.id.lv_mine_secretary_quetions_detail);
		LinearLayoutManager manager=new LinearLayoutManager(getActivity());
		my_secretary_question_recycle.setLayoutManager(manager);
		
		btn_secretary_question_commit=(TextView)view.findViewById(R.id.btn_secretary_question_commit);
		btn_secretary_question_commit.setOnClickListener(new OnClickListener(){
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
					
					
//					if(json.contains(",'answer'：[")){
//						type = NEW_VER ;
//					}else{
//						type = HISTORY_VER ;
//					}
					
					
					//LogUtils.toDebugLog("json", response.body().string());
					//json=json.replace(" ", "");
					LogUtils.toDebugLog("json", json.toString());
					Gson gson=new Gson();
					
					
					if(type == HISTORY_VER){
						list=gson.fromJson(json, QuestionDetail.class);
						currentType=list.getType();
						adapter=new QuestionAnswerAdapter(list);
					}else if(type == NEW_VER){
						/*json = "{'answer':[{'simpleAnswer':'1.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'2.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'3.国家は政治制つまり政制ことができる。'}],'content':'测试数据(主问题)','id':'488','items':[{'content':'测试数据1','id':'491','time':'2015-12-2511:24:59','type':'travel','answer':[{'simpleAnswer':'1.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'2.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'3.国家は政治制つまり政制ことができる。'}]},{'content':'测试数据2','id':'492','time':'2015-12-2514:15:33','type':'travel','answer':[{'simpleAnswer':'1.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'2.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'3.国家は政治制つまり政制ことができる。'}]},{'content':'，测试3','id':'493','time':'2015-12-2514:18:12','type':'travel','answer':[{'simpleAnswer':'1.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'2.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'3.国家は政治制つまり政制ことができる。'}]},{'content':'0还是测试4','id':'494','time':'2015-12-2514:48:55','type':'travel','answer':[{'simpleAnswer':'1.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'2.国家は政治制つまり政制ことができる。'},{'simpleAnswer':'3.国家は政治制つまり政制ことができる。'}]}],'time':'2015-12-2416:41:28','type':'travel'}";*/
						newList= gson.fromJson(json, QuestionDetailNew.class);
						currentType = newList.getType();
						processList = processList(newList);
						newAdapter = new QuestionMultiAnswerAdapter(processList);
					}
					
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
		
//		back键不做点击响应
//		v.iv_activity_back.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				FragmentEntity entity=new FragmentEntity();
//				/*Fragment fragment=new MineSecretaryFragment();*/				
//				Fragment fragment=new MineSecretaryListFragment();
//				entity.setFragment(fragment);
//				EventBus.getDefault().post(entity);
//			}
//		});
	}

	/**
	 * 追加提问
	 */
	private void commit() {
		if (TextUtils.isEmpty(v.et_secretary_question.getText().toString()
				.trim())) {
			CustomToast.show(activity, R.string.tip,R.string.please_enter_content);
			return;
		}
		pd = new ProgressDialog(activity);
		pd.setMessage(getString(R.string.up_dataing));
		pd.show();
		
		AddQuestionBiz biz=new AddQuestionBiz();//封装特定的业务逻辑(网络访问)层
		question = v.et_secretary_question.getText().toString();
		biz.sendQuestion(question, currentType, question_id+"", this);
	}
	private String switchType(String type) {
		if(activity==null){
			return "";
		}
		switch (type) {
		case "travel":
			return "旅行度假";
//			return getResources().getString(R.string.travel_holiday);
		case "shopping":
			return "新品购物";
//			return getResources().getString(R.string.new_shopping);
		case "party":
			return "盛宴派对";
//			return getResources().getString(R.string.shengyan_part);
		case "life":
			return "高质生活";
//			return getResources().getString(R.string.life_quality);
		case "student":
			return "留学服务";
//			return getResources().getString(R.string.studybroad);
		case "investment":
			return "投资理财";
//			return getResources().getString(R.string.investment);
		case "card":
			return "办卡推荐";
//			return getResources().getString(R.string.handlecard);
		}
		return "";
	}

	/**
	 * 下面的网络访问成功和失败的回调函数具体实现，是由AddQuestionBiz中进行问题提交时完成的引起的
	 */
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
		if(type == HISTORY_VER){
			my_secretary_question_recycle.scrollToPosition(adapter.getItemCount()-1);
		}else if(type == NEW_VER){
			my_secretary_question_recycle.scrollToPosition(newAdapter.getItemCount()-1);
		}
	}
	
	/**
	 * 对服务器端拿到的数据进行处理，转化成新的可以解析的数据
	 * 处理对象： 将服务器返回对象中answer的类型转为：数组 []
	 * 
	 * @author Yeun.Zhang 
	 * @param list
	 * @return 
	 */
	public ArrayList<QAMetaData> processList(QuestionDetailNew list) {
		
		/**
		 * 约定 type==1： 一问一答的标准Item
		 *    type==2：追问底部的多次回答样式
		 */
		int type = 1;
		
		//假设服务器放回的数据类型已经将answer的类型修改为 数组类型[]
		//1.根据服务器发挥的标准数据，并且gson解析完成转换后的数据集，来提取数据
		ArrayList<QAMetaData> arrayDataSource = new ArrayList<QAMetaData>();
		
		
		//首先要给整个问答列表添加上一个标题的头，例如“旅游度假”
		QAMetaData titleQAMetaData = new QAMetaData();
		titleQAMetaData.setContent("  "+switchType(list.getType())+"  ");
		titleQAMetaData.setType(3);
		arrayDataSource.add(titleQAMetaData);
		
		
		String questionType = list.getType();
		//1.1主问题的提问数据
		String mainContent = list.getContent();//拿到第一次提问的内容
		String askTime = list.getTime();//拿到第一次提问的事件
		
		QAMetaData qaMetaData = new QAMetaData();
		
		//1.2主答复为不为空时，才进行answer的解析
		if(list.getAnswer()!= null&&list.getAnswer().size()!=0){
			ArrayList<SubAnswer> answer = list.getAnswer();//拿到主问题的多次回答
			int i = 0;
			for(SubAnswer simpleAnswer:answer){
				qaMetaData = new QAMetaData();
				
				qaMetaData.setTime(askTime);
				qaMetaData.setAnswer(simpleAnswer.getSimpleAnswer());
				qaMetaData.setContent(mainContent);
				if(i==0){
					qaMetaData.setType(1);//一问一答的操作
					i++;
				}else{
					qaMetaData.setType(2);
				}
				arrayDataSource.add(qaMetaData);
			}
		}
		//1.3当主答复为空的时候，需要将当前的回答可根据事件进行自行塞入对应提问时间的内容
		else{
			QAMetaData qaMetaDataSpareWheel = new QAMetaData();
			qaMetaDataSpareWheel.setContent(mainContent);
			qaMetaDataSpareWheel.setType(1);//标准一问一答的操作
			
			qaMetaDataSpareWheel.setTime(askTime);
			
//			qaMetaDataSpareWheel.setAnswer("大哥，终于等到你提问了");
			qaMetaDataSpareWheel.setAnswer("小秘书已经收到您的留言喽，立即开启暴风处理模式~2小时内必定有回复！为保证答复质量，如需更多处理时间，" +
					"小秘书也将第一时间告知~全心全意为您哟~我们的服务时间：周一至周五工作日9:00-18:00（周末及法定节假日休息），小秘书将逐步实现7*24无休服务。");
			arrayDataSource.add(qaMetaDataSpareWheel);
		}
		
		//2.子集追问数据的填入
		ArrayList<QuestionAnswerDetailNew> items = list.getItems();
		
		for(QuestionAnswerDetailNew qADetailNew : items){//循环每个追问的对象
			int j = 0;
			String content = qADetailNew.getContent();
			String sub_askTime = qADetailNew.getTime();
			
			//2.1 item的子集回答存在
			if(qADetailNew.getAnswer()!=null&& qADetailNew.getAnswer().size()!=0){
			ArrayList<SubAnswer> sAnswerList = qADetailNew.getAnswer();
		
			for(SubAnswer sAnswer :sAnswerList){
				qaMetaData = new QAMetaData();
				qaMetaData.setTime(sub_askTime);
				qaMetaData.setAnswer(sAnswer.getSimpleAnswer());
				qaMetaData.setContent(content);
				if(j==0){
					qaMetaData.setType(1);
					j++;
				}else{
					qaMetaData.setType(2);
				}
				arrayDataSource.add(qaMetaData);
			}
			
		  }
		   //2.2item的子集回答并不存在时，需自行追加回答作为展示
			else{
				QAMetaData qaMetaDataPump = new QAMetaData();
				qaMetaDataPump.setContent(content);
				qaMetaDataPump.setType(1);
				
				qaMetaDataPump.setTime(sub_askTime);
				
				String time = qADetailNew.getTime();
				LogUtils.toDebugLog("time",qADetailNew.getTime());
				String hourTime = time.substring(11, 13);
				int intHourTime = Integer.valueOf(hourTime);
				
				//2.2.1 9-18点
				if((intHourTime>=9) && (intHourTime<18)){
					qaMetaDataPump.setAnswer("收到啦，给我点点时间来处理~爱你哟~我们的工作时间：周一至周五工作日9:00-18:00（周末及法定节假日休息），小秘书将逐步实现7*24无休服务。");
//					qaMetaDataPump.setAnswer("晓得咯，等一哈子噢~~");
				}
				//2.2.2  18-24点      0-9点
				else if(((intHourTime>=0) && (intHourTime<9))||((intHourTime>=18)&&(intHourTime<=24))){
					qaMetaDataPump.setAnswer("收到您的留言喽~但但但...人家现在正休息，为了养足精神更好为您服务哦~小秘书开工后立即处理（周一至周五工作日9:00-18:00），谢谢体谅哟~小秘书将逐步实现7*24无休服务。");
//					qaMetaDataPump.setAnswer("大哥喂，我在休息哦~~");
				}
				arrayDataSource.add(qaMetaDataPump);
			}
			
		}//items 的大号for循环
		
		return arrayDataSource;
		
	}
	
}
