package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.adapter.CommentActivityAdapter;
import com.lansun.qmyo.adapter.MineCommentAdapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Activity;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.domain.HistoryActivity;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.domain.StoreInfo;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.MessageCenterFragment.Views;
import com.lansun.qmyo.utils.DBSensitiveUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 我的评论
 * 
 * @author bhxx
 * 
 */
public class MineCommentsFragment extends BaseFragment {

	@InjectAll
	Views v;

	private CommentList list;

	private CommentActivityAdapter adapter;
	@InjectView(down = true, pull = true)
	private MyListView lv_comments_list;

	private ArrayList<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

	private int replyId;

	private int replyUserId;

	private List<Sensitive> sensitiveList;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, tv_activity_shared,
				ll_mai_comment_reply, btn_mai_comment_reply_commit;
		private TextView tv_activity_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_mine_comments_all, tv_mine_comments_replay;
		private EditText et_mai_comment_reply_content;
	}

	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		
		/*
		 * 猜测： 读取数据库的速度 影响了整个UI进程的速度
		 * 初步解决办法： 在子线程中进行本地数据库的加载任务
		 */
		new Thread(new  Runnable() {
			public void run() {
//				sensitiveList = Ioc.getIoc().getDb(activity.getCacheDir().getPath(), "qmyo_sensitive_new.db").findAll(provinceSelector);
				sensitiveList = DBSensitiveUtils.getToDbData(activity);
			}
		}).start();
		
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_comments, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.mine_comments, null, 0);
		refreshUrl = GlobalValue.URL_USER_COMMENTS;
		refreshKey = 0;
		/*
		 * 下面这个方法作用就是去刷新当前的ListView
		 */
		refreshCurrentList(refreshUrl, null, refreshKey, lv_comments_list);
		
		EventBus.getDefault().register(this);
		

		provinceSelector = Selector.from(Sensitive.class);
		provinceSelector.select(" * ");
		provinceSelector.limit(Integer.MAX_VALUE);
		

	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				list = Handler_Json.JsonToBean(CommentList.class,
						r.getContentAsString());
				if (list.getData() != null) {
					for (CommentListData commentListData : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						String content = commentListData.getComment()
								.getContent();
						String time = commentListData.getComment().getTime();
						ArrayList<String> photos2 = commentListData
								.getComment().getPhotos();
						map.put("tv_comment_activity_name", commentListData
								.getActivity().getName());
						map.put("tv_comment_time", time);
						map.put("activityId", commentListData.getActivity()
								.getId());
						map.put("commentid", commentListData.getComment()
								.getId());
						map.put("shopId", commentListData.getComment()
								.getShop_id());
						map.put("tv_comment_desc", content);
						map.put("tv_mai_comment_communicate", commentListData
								.getComment().getRespond());
						if (photos2 != null) {
							map.put("tv_comment_images_count,", String.format(
									getString(R.string.images_count),
									photos2.size()));
						}
						map.put("photos", photos2);
						dataList.add(map);
					}
				} else {
					lv_comments_list.setAdapter(null);
				}
				if (adapter == null) {
					adapter = new CommentActivityAdapter(lv_comments_list,
							dataList, R.layout.mine_comments_activity_item,
							MineCommentsFragment.this);
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
					adapter.refresh(replyId, list.getData().get(replyId)
							.getActivity().getId()
							+ "", list.getData().get(replyId).getComment()
							.getId()
							+ "");
					v.et_mai_comment_reply_content.setText("");
					return;
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private void changeTextColor(TextView tv) {
		v.tv_mine_comments_all.setTextColor(getResources().getColor(
				R.color.app_grey5));
		v.tv_mine_comments_replay.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	
	private void click(View view) {

		switch (view.getId()) {
		case R.id.tv_mine_comments_all:// 评论列表
			dataList.clear();
			adapter = null;
			refreshUrl = GlobalValue.URL_USER_COMMENTS;
			changeTextColor(v.tv_mine_comments_all);
			refreshCurrentList(refreshUrl, null, refreshKey, lv_comments_list);
			break;
		case R.id.tv_mine_comments_replay:// 回复
			dataList.clear();
			adapter = null;
			refreshUrl = GlobalValue.URL_USER_RESPONDS;
			changeTextColor(v.tv_mine_comments_replay);
			refreshCurrentList(refreshUrl, null, refreshKey, lv_comments_list);
			break;

		case R.id.btn_mai_comment_reply_commit:// 提交回复内容

			/*
			 * replaceSensitive取代掉回复输入框中的字符创中的敏感字词
			 */
			replaceSensitive(v.et_mai_comment_reply_content.getText().toString().trim());

			InternetConfig config = new InternetConfig();
			config.setKey(1);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("activity_id", list.getData().get(replyId).getActivity()
					.getId()
					+ "");
			params.put("shop_id", list.getData().get(replyId).getComment()
					.getShop_id()
					+ "");
			params.put("content", v.et_mai_comment_reply_content.getText()
					.toString().trim());
			params.put("principal", list.getData().get(replyId).getComment()
					.getId()
					+ "");
			if (replyUserId == 0) {
				params.put("to_user_id", list.getData().get(replyId).getUser()
						.getId()
						+ "");
			} else {
				params.put("to_user_id", replyUserId + "");
			}
			/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_ACTIVITY_COMMENT,
					params, null, config, this);*/
			FastHttpHander.ajax(GlobalValue.URL_USER_ACTIVITY_COMMENT,
					params, config, this);
			break;
		}
	}

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

	/**
	 * 刷新
	 * 
	 * @param type
	 */
	@InjectPullRefresh
	private void call(int type) {
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())) {
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();
					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
				} else {
					refreshCurrentList(list.getNext_page_url(), null, 0,
							lv_comments_list);
				}
			}
			break;
		case InjectView.DOWN:
			refreshCurrentList(refreshUrl, null, refreshKey, lv_comments_list);
			break;
		}
	}

	@Override
	public void onPause() {
		adapter = null;
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(ReplyEntity event) {
		if (event.isEnable()) {
			v.ll_mai_comment_reply.setVisibility(View.VISIBLE);
			v.et_mai_comment_reply_content.setFocusable(true);
			v.et_mai_comment_reply_content.setFocusableInTouchMode(true);
			v.et_mai_comment_reply_content.requestFocus();
			v.et_mai_comment_reply_content.setHint(getString(R.string.replay)
					+ event.getReplyUserName());
			replyId = event.getPosition();
			if (event.getToUserId() != 0) {
				replyUserId = event.getToUserId();
			}
			Handler_Ui.showSoftkeyboard(v.et_mai_comment_reply_content);
		}
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

	private Selector provinceSelector;
}
