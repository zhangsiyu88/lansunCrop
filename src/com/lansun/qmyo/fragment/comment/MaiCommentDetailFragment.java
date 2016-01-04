package com.lansun.qmyo.fragment.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.Inflater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.AjaxCallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.adapter.MaiCommentAdapter;
import com.lansun.qmyo.adapter.MaiCommentContentAdapter;
import com.lansun.qmyo.adapter.MaiCommentGVAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.ReportCommentFragment;
import com.lansun.qmyo.fragment.comment.MaiCommentAdapterUpdate.ViewHolder;
import com.lansun.qmyo.fragment.comment.MaiCommentListFragmentUpdate.Views;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DBSensitiveUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.GridViewUtils;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.MyGridView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.MySubListView;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressLint("InflateParams") public class MaiCommentDetailFragment extends BaseFragment {

	private HashMap<String, Object> mHashMap;
	private ArrayList<HashMap<String, String>> mSubCommentDataList;
	private String mActivityid;
	private String mCommentid;
	private String mShopid;
	public List<Sensitive> sensitiveList;
	private int replyId;
	private int replyUserId;
	private String mUserName;
	
	@InjectAll
	Views v;
	class Views {
		/*@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_activity_reply, btn_mai_comment_reply_commit;*/
		
		
		public  EditText et_mai_comment_reply_content;
		private ImageView iv_comment_head,iv_comment_reply,iv_comment_report,iv_comment_more;
		
		private TextView tv_comment_time,tv_comment_name,
		     tv_mai_comment_communicate,tv_comment_from,tv_comment_desc,tv_reply,rl_comment_item_content,
		     tv_mai_images_count,tv_activity_title,tv_all_rapley_counts;
		     
		private GridView gv_comment_images;
		private LinearLayout ll_comment_desc,ll_mai_comment_reply,ll_activity_shared;
		private View line,iv_activity_shared;
		
		private ListView lv_activity_mai_comments;
		private Button btn_mai_comment_reply_commit;
	}
	
	
	
	
	
	@InjectView
	private MyListView lv_sub_comments;
	private String mPrincipal;

	
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		if(getArguments()!=null){
			mHashMap = (HashMap<String, Object>) getArguments().get("commentDetail");
			mSubCommentDataList = (ArrayList<HashMap<String, String>>) getArguments().get("subcomment");
			mCommentid = (String) getArguments().get("commentid");
			mActivityid = (String) getArguments().get("activityid");
			
			mUserName = (String) getArguments().get("user_name");
			
			LogUtils.toDebugLog("mUserName", "mUserName: "+ getArguments().get("user_name"));
			LogUtils.toDebugLog("mUserName", "mUserName: "+mUserName);
			
			mShopid = (String) getArguments().get("shopid");
			
			mPrincipal = (String) getArguments().get("principal");
			
			mShopid = (String) getArguments().get("shopid");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				sensitiveList = DBSensitiveUtils.getToDbData(activity);				
			}
		}).start();
		
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		inflater = LayoutInflater.from(activity);
		View rootView = inflater.inflate(R.layout.activity_comment_detail, null);
		Handler_Inject.injectFragment(this, rootView);
		
		//子级回复列表 listView 完全展开
		
		if(mSubCommentDataList!=null){
			contentAdapter = new MaiCommentContentAdapterUpdate(
					lv_sub_comments, mSubCommentDataList,
					R.layout.activity_mai_comments_item_update,mUserName);
			
			contentAdapter.setMainCommentUserName(mUserName,activity);
			lv_sub_comments.setAdapter(contentAdapter);
		}else{
			lv_sub_comments.setVisibility(View.GONE);
			v.tv_all_rapley_counts.setText("此评论暂无回复，赶紧抢沙发啦啦啦~~");
		}
		return rootView;
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
			LogUtils.toDebugLog("reply", event.getPosition()+"");
			
			if (event.getToUserId() != 0) {
				replyUserId = event.getToUserId();
				LogUtils.toDebugLog("replyUserId", event.getToUserId()+"");
			}
			Handler_Ui.showSoftkeyboard(v.et_mai_comment_reply_content);
		}
	}
	
	@InjectInit
	private void init() {
		String headUrl = mHashMap.get("iv_comment_head").toString().trim();
		String time = mHashMap.get("tv_comment_time").toString().trim();
		String desc = mHashMap.get("tv_comment_desc").toString().trim();
		final String name = mHashMap.get("tv_comment_name").toString().trim();
		final String mai_communicate = mHashMap.get("mai_communicate").toString().trim();
		ArrayList<String> photos = (ArrayList<String>) mHashMap.get("photos");
		
		lv_sub_comments.setOnScrollListener(mOnScrollListener);
		v.ll_activity_shared.setVisibility(View.GONE);
		v.tv_activity_title.setText("评论详情");
		
		
		v.btn_mai_comment_reply_commit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				replaceSensitive(v.et_mai_comment_reply_content.getText().toString());
				
				InternetConfig config = new InternetConfig();
				config.setKey(1);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<>();
				
				params.put("activity_id", mActivityid );
				LogUtils.toDebugLog("activity_id", "activity_id:  "+mActivityid);
				
				params.put("shop_id", mShopid);
				LogUtils.toDebugLog("shop_id", "shop_id:  "+mShopid);
				
				
				params.put("content", v.et_mai_comment_reply_content.getText().toString().trim());
				
				
				//params.put("principal", mSubCommentDataList.get(replyId).get("principal"));
				params.put("principal", mCommentid);
				
				
				LogUtils.toDebugLog("principal", "principal:  "+mSubCommentDataList.get(replyId).get("principal"));
				
				params.put("to_user_id",mSubCommentDataList.get(replyId).get("to_user_id"));
				LogUtils.toDebugLog("to_user_id", "to_user_id:  " + mSubCommentDataList.get(replyId).get("to_user_id"));
				
				
//				if (replyUserId == 0) {
//					params.put("to_user_id", mSubCommentDataList.get(replyId).get("to_user_id"));
//				} else {
//					params.put("to_user_id", replyUserId + "");
//					LogUtils.toDebugLog("to_user_id", "to_user_id:  " + replyUserId);
//				}
				FastHttpHander.ajax(GlobalValue.URL_USER_ACTIVITY_COMMENT,params, config, MaiCommentDetailFragment.this);
			}
		});
		
		v.iv_comment_report.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ReportCommentFragment fragment = new ReportCommentFragment();
				Bundle args = new Bundle();
				args.putString("comment_id", mCommentid);
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});
		
		
		
		if (photos != null) {
			if (photos.size() > 0) {
				v.gv_comment_images.setVisibility(View.VISIBLE);
			} else {
				v.gv_comment_images.setVisibility(View.GONE);
			}
			MaiCommentGVAdapter gvAdapter = new MaiCommentGVAdapter(activity,photos);
			v.gv_comment_images.setAdapter(gvAdapter);
			GridViewUtils.updateGridViewLayoutParams(v.gv_comment_images, 4, (int) activity.getResources()
							.getDimension(R.dimen.l_r_10));
		}
		v.gv_comment_images.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				DetailHeaderPagerAdapter headPagerAdapter = new DetailHeaderPagerAdapter(
						activity, (ArrayList<String>) mHashMap.get("photos"));
				ImageGalleryDialog dialog = ImageGalleryDialog.newInstance(headPagerAdapter, arg2);
				dialog.show(getFragmentManager(), "gallery");
			}
		});
		if (photos != null) {
			v.tv_mai_images_count.setVisibility(View.VISIBLE);
			v.tv_mai_images_count.setText(String.format(
					activity.getString(R.string.images_count), photos.size()));
		} else {
			v.tv_mai_images_count.setVisibility(View.GONE);
		}
		if(mSubCommentDataList!= null){
			v.tv_all_rapley_counts.setText(String.format(
					activity.getString(R.string.rapley_counts1), mSubCommentDataList.size()));
		}
		
		
		
		
		if (!TextUtils.isEmpty(headUrl)) {
			download(v.iv_comment_head, headUrl);
		}
		if (!TextUtils.isEmpty(desc)) {
			v.tv_comment_desc.setText(desc);
		}
		if (!TextUtils.isEmpty(name)) {
			v.tv_comment_name.setText(name);
		}
		if (!TextUtils.isEmpty(time)) {
			v.tv_comment_time.setText(time);
		}
		/*if (!TextUtils.isEmpty(store_name)) {
			viewHold.tv_comment_from.setText(store_name);
		}*/
		v.tv_comment_from.setText(String.format(activity.getString
				(R.string.tv_comment_from), "ThreeBodyPlanet"));
		
		
	}

	private void download(ImageView view, String url) {
		loadPhoto(url, view);
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
	private MaiCommentContentAdapterUpdate contentAdapter;

	
	/**
	 * 替换关键字
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
			replaceSensitive(v.et_mai_comment_reply_content.getText().toString().trim());
		}
	}

	
	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 1:
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, R.string.tip,R.string.replay_success);
					
					//contentAdapter.refresh(replyId, mActivityid,((CommentList) list).getData().get(replyId).getComment().getId()+ "");
					
					v.et_mai_comment_reply_content.setText("");
					return;
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_refresh);
		}

	}
	/**
	 * 刷新当前adapter链接着的数据源  mSubCommentDataList
	 * 
	 */
	public void refresh(final int position, String activityid, String commentid) {
		

		InternetConfig config = new InternetConfig();
		config.setKey(3);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		FastHttp.ajaxGet(String.format(GlobalValue.URL_ACTIVITY_COMMENT,
				activityid, commentid), config, new AjaxCallBack() {

			@Override
			public void callBack(ResponseEntity r) {
				if (r.getStatus() == FastHttp.result_ok) {
					CommentList commentList = Handler_Json.JsonToBean(
							CommentList.class, r.getContentAsString());
					ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
					for (CommentListData data : commentList.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("iv_comment_head", data.getUser().getAvatar());
						map.put("tv_comment_desc", data.getComment()
								.getContent());
						map.put("to_user_id", data.getUser().getId() + "");
						map.put("replyId", position + "");

						dataList.add(map);
					}
					
					/*contentAdapter = new MaiCommentContentAdapter(
							viewHolder.lv_activity_mai_comments, dataList,
							R.layout.activity_mai_comments_item);
					viewHolder.lv_activity_mai_comments.setAdapter(contentAdapter);*/

				}
			}

			@Override
			public boolean stop() {
				return false;
			}
		});
	}
	
}
