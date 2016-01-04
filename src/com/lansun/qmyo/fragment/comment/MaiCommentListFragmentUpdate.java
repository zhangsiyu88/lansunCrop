package com.lansun.qmyo.fragment.comment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;

import com.android.pc.ioc.app.Ioc;
import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.adapter.MaiCommentAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.NewCommentFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

/**
 * TODO 卖友评论
 * 
 * @author bhxx
 * 
 */
public class MaiCommentListFragmentUpdate extends BaseFragment {

	@InjectAll
	Views v;
	private String activityId;
	private View rootView;
	private String shopId;
	private int replyId;
	private MaiCommentAdapterUpdate adapter;
	private int replyUserId;

	@InjectView(down = true, pull = true)
	private MyListView lv_comments_list;
	private ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
	private CommentList list;
	private List<Sensitive> sensitiveList = new ArrayList<Sensitive>();
	private SQLiteDatabase db;
	private int counts = 0;

	class Views {

		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_activity_reply, btn_mai_comment_reply_commit;
		private View ll_mai_comment_reply;
		private EditText et_mai_comment_reply_content;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_mai_comments, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		if (getArguments() != null) {
			activityId = getArguments().getString("activityId");
			shopId = getArguments().getString("shopId");
		}
		
		/**
		 * 注册Eventbus的接收者  */
		/*EventBus.getDefault().register(this);*/
		
		
		refreshUrl = String.format(GlobalValue.URL_ACTIVITY_COMMENTS,activityId);
		refreshKey = 0;
		LogUtils.toDebugLog("activityId","activityId"+ activityId);
		// 得到活动评论列表
		refreshCurrentList(refreshUrl, null, refreshKey, lv_comments_list);

		//终于知道将其设置在子线程中类
		new Thread() {
			public void run() {
			    sensitiveList = getToDbData();
				/*
				 * if (sensitiveList == null) {
					Selector provinceSelector = Selector.from(Sensitive.class);
					provinceSelector.select(" * ");
					provinceSelector.limit(Integer.MAX_VALUE);
					sensitiveList = Ioc.getIoc().getDb(activity.getCacheDir().getPath(),
									"qmyo_sensitive.db").findAll(provinceSelector);
				}*/
			};
		}.start();
	}

	/**
	 * 对在上部注册的Eventbus接收者，进行事件的处理
	 * @param event
	 */
	public void onEventMainThread(ReplyEntity event) {
		if (event.isEnable()) {
			v.ll_mai_comment_reply.setVisibility(View.VISIBLE);
			v.et_mai_comment_reply_content.setFocusable(true);
			v.et_mai_comment_reply_content.setFocusableInTouchMode(true);
			v.et_mai_comment_reply_content.requestFocus();
			v.et_mai_comment_reply_content.setHint(getString(R.string.replay)+ event.getReplyUserName());
			
			replyId = event.getPosition();
			if (event.getToUserId() != 0) {
				replyUserId = event.getToUserId();
			}
			Handler_Ui.showSoftkeyboard(v.et_mai_comment_reply_content);
		}
	}

	/**
	 * 读取数据库里面的内容，将内容写入到之前就生成好的容器中去
	 * @return
	 */
	  public List<Sensitive> getToDbData(){  
		  List<Sensitive> sensitiveList = new ArrayList<Sensitive>();
		  String path = activity.getCacheDir().getPath()+File.separator+"qmyo_sensitive_new.db";
		  SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);  
		  List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
          //Cursor cursor = db.query("person", null, null, null, null, null, null);  
          Cursor cursor=db.query("com_qmyo_domain_Sensitive_new", null,null, null, null, null, "_id desc");  
        
          while(cursor.moveToNext()){  
              Map<String, Object> map = new HashMap<String, Object>();  
              Sensitive sens = new Sensitive();
              int id = cursor.getInt(cursor.getColumnIndex("_id"));  
              String name = cursor.getString(cursor.getColumnIndex("name"));  
              
              map.put("_id", id);  
              map.put("name", name);  
              list.add(map);  
              
              sens.set_id(id);
              sens.setName(name);
              sensitiveList.add(sens);
          }
          LogUtils.toDebugLog("start", "完成敏感词列表的写入操作");
          
		return sensitiveList;  
      }
	
	@Override
	public void onPause() {
		/*list = null;
		dataList.clear();
		adapter = null;*/
		//EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			PullToRefreshManager.getInstance().footerEnable();
			PullToRefreshManager.getInstance().onFooterRefreshComplete();
			switch (r.getKey()) {
			case 0: //拿到评论列表，将评论列表重新展示
				list = Handler_Json.JsonToBean(CommentList.class,r.getContentAsString());
				if (list.getData() != null) {
					
					if(isPullDown){
						dataList.clear();
						isPullDown = false;
					}
					/* if (adapter != null) {
						adapter.notifyDataSetChanged();
					}*/
					
					for (CommentListData data : list.getData()) {
						HashMap<String, Object> map = new HashMap<>();
						map.put("iv_comment_head", data.getUser().getAvatar());
						map.put("tv_comment_time", data.getComment().getTime());
						map.put("tv_comment_name", data.getUser().getName());
						map.put("tv_comment_desc", data.getComment().getContent());
						map.put("mai_communicate", data.getComment().getRespond());
						map.put("photos", data.getComment().getPhotos());
						map.put("activityid", activityId);
						map.put("commentid", data.getComment().getId());
						
						//将在评论详情页需要的1个参数传递过去，供发表回复使用
						map.put("shopid", data.getComment().getShop_id());
						
						map.put("main_content_user_name", data.getUser().getName());
						LogUtils.toDebugLog("data.getUser().getName()", "data.getUser().getName() = :"+data.getUser().getName());
						
						
						/*
						 * 暂时无法拿到评论书写时对应的门店名称
						   map.put("store_name", data.getActivity().get。。。);
						 */
						dataList.add(map);
					}
				} else {
					lv_comments_list.setAdapter(null);
				}
				if (adapter == null) {
					adapter = new MaiCommentAdapterUpdate(lv_comments_list, dataList,
							R.layout.mai_comment_lv_item);
					adapter.setActivity(this);
					//adapter.setParentScrollView(lv_comments_list);
					
					lv_comments_list.setAdapter(adapter);
					lv_comments_list.setOnScrollListener(mOnScrollListener);
				} else {
					adapter.notifyDataSetChanged();
				}
				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				break;

			case 1:
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, R.string.tip,
							R.string.replay_success);
					adapter.refresh(replyId, activityId + "",
							((CommentList) list).getData().get(replyId).getComment().getId()+ "");
					
					v.et_mai_comment_reply_content.setText("");
					return;
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}
		
		//拿到数据数据后，都要将其安上 上拉刷新和下拉加载的布局
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView arg0, int arg1) {
			if (v.ll_mai_comment_reply.getVisibility() == View.VISIBLE) {
				v.ll_mai_comment_reply.setVisibility(View.GONE);
				Handler_Ui.hideSoftKeyboard(arg0);
			}
		}

		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

		}
	};
	
	
	private boolean isPullDown = false;

	/**
	 * 替换关键字
	 * 
	 * @param sensitive
	 */
	private void replaceSensitive(String sensitie) {
		String sensitiveStr = "";
		for (Sensitive sensitive : sensitiveList) {
			if (v.et_mai_comment_reply_content.getText().toString().trim()
					.contains(sensitive.getName().trim())) {
				sensitiveStr = sensitive.getName();
			}
		}
		if (sensitiveStr.length() >= 1) {
			if (sensitiveStr.toString().length() == 1) {
				v.et_mai_comment_reply_content
						.setText(v.et_mai_comment_reply_content.getText()
								.toString().trim()
								.replace(sensitiveStr.toString(), "*"));
			} else if (sensitiveStr.toString().length() == 2) {
				v.et_mai_comment_reply_content
						.setText(v.et_mai_comment_reply_content.getText()
								.toString().trim()
								.replace(sensitiveStr.toString(), "**"));
			} else if (sensitiveStr.toString().length() >= 3) {
				v.et_mai_comment_reply_content
						.setText(v.et_mai_comment_reply_content.getText()
								.toString().trim()
								.replace(sensitiveStr.toString(), "***"));

			}
			replaceSensitive(v.et_mai_comment_reply_content.getText()
					.toString().trim());
		}
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_mai_comment_reply_commit:// 底部靠右手边，提交回复内容
			replaceSensitive(v.et_mai_comment_reply_content.getText()
					.toString());
			InternetConfig config = new InternetConfig();
			config.setKey(1);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			
			params.put("activity_id", activityId + "");
			params.put("shop_id", list.getData().get(replyId).getComment().getShop_id()+ "");
			params.put("content", v.et_mai_comment_reply_content.getText().toString().trim());
			params.put("principal", list.getData().get(replyId).getComment().getId()+ "");
			
			if (replyUserId == 0) {
				params.put("to_user_id", list.getData().get(replyId).getUser().getId()+ "");
			} else {
				params.put("to_user_id", replyUserId + "");
			}
			
			//点击提交，针对某个迈友的回复内容
			/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_ACTIVITY_COMMENT,
					params, null, config, this);*/
			FastHttpHander.ajax(GlobalValue.URL_USER_ACTIVITY_COMMENT,
					params, config, this);
			break;
			
		case R.id.iv_activity_reply://点击  右上角   进入写"我的评论"一栏
			if ("true".equals(App.app.getData("isExperience"))) {
				RegisterFragment fragment = new RegisterFragment();
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			} else {
				NewCommentFragment fragment = new NewCommentFragment();
				Bundle args = new Bundle();
				args.putString("activity_id", activityId + "");
				args.putString("shop_id", shopId + "");
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
			break;
		}
	}

	@InjectPullRefresh
	private void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			//下拉是可以识别出来的，并且是有效的，但是由于下面的
			//CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
			Log.i("list是否有值", list.getNext_page_url());
			
			if (list != null) {
				//根据服务器返回回来的数据中含有next_page_url的内容
				if (TextUtils.isEmpty(list.getNext_page_url())) {
					CustomToast.show(activity, "到底啦！", "评论内容暂时只有这么多，期待您的评论~");
					
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					/*
					 * 下面这一步之所以执行不了，是因为上面的PullToRefreshManager的代码给堵塞住了
					   CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
					 */
				} else {
					refreshCurrentList(list.getNext_page_url(), null, 0,lv_comments_list);
				}
			}else{
				CustomToast.show(activity, "到底啦！", "评论内容暂时只有这么多，期待您的评论~");
				PullToRefreshManager.getInstance().footerUnable();
			}
			
			if(list.getNext_page_url()=="null"){
				CustomToast.show(activity, "到底啦！", "评论内容暂时只有这么多，期待您的评论~");
				PullToRefreshManager.getInstance().footerUnable();
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				
				refreshCurrentList(refreshUrl, null, refreshKey,
						lv_comments_list);
				isPullDown  = true;
				
				//CustomToast.show(activity, "数据来了", "来了一页！");
			}
			break;
		}
	}
}
