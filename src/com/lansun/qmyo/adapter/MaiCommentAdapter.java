package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.AjaxCallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.MaiCommentAdapter.ViewHolder;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.ReportCommentFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.GridViewUtils;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.MyGridView;
import com.lansun.qmyo.view.MySubListView;
import com.lansun.qmyo.R;

/**
 * 迈友评论列表
 * 
 * @author bhxx
 * 
 */
public class MaiCommentAdapter extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	private ListView parentListView;

	private Fragment activity;
	boolean isShow;

	private ArrayList<ViewHolder> holders = new ArrayList<ViewHolder>();

	public void setActivity(Fragment fragment) {
		this.activity = fragment;
	}

	public MaiCommentAdapter(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	public void setParentScrollView(ListView parentListView) {
		this.parentListView = parentListView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = layoutInflater.inflate(layout_id, null);
			viewHold = new ViewHolder();
			viewHold.iv_comment_head = (CircularImage) convertView
					.findViewById(R.id.iv_comment_head);
			viewHold.iv_comment_reply = (RecyclingImageView) convertView
					.findViewById(R.id.iv_comment_reply);
			viewHold.iv_comment_report = (RecyclingImageView) convertView
					.findViewById(R.id.iv_comment_report);
			viewHold.tv_comment_time = (TextView) convertView
					.findViewById(R.id.tv_comment_time);
			viewHold.tv_comment_name = (TextView) convertView
					.findViewById(R.id.tv_comment_name);
			viewHold.tv_mai_comment_communicate = (TextView) convertView
					.findViewById(R.id.tv_mai_comment_communicate);

			viewHold.tv_comment_desc = (TextView) convertView
					.findViewById(R.id.tv_comment_desc);
			viewHold.rl_comment_item_content = (RelativeLayout) convertView
					.findViewById(R.id.rl_comment_item_content);
			viewHold.lv_activity_mai_comments = (MySubListView) convertView
					.findViewById(R.id.lv_activity_mai_comments);
			viewHold.iv_comment_more = (RecyclingImageView) convertView
					.findViewById(R.id.iv_comment_more);
			viewHold.lv_activity_mai_comments.setTag(position);

			viewHold.tv_mai_images_count = (TextView) convertView
					.findViewById(R.id.tv_mai_images_count);
			viewHold.tv_mai_images_count.setTag(position);

			viewHold.gv_comment_images = (MyGridView) convertView
					.findViewById(R.id.gv_comment_images);
			viewHold.ll_comment_desc = (LinearLayout) convertView
					.findViewById(R.id.ll_comment_desc);
			viewHold.line = (View) convertView.findViewById(R.id.line);
			convertView.setTag(viewHold);
		}
		viewHold = (ViewHolder) convertView.getTag();
		
		if (position != Integer.parseInt(viewHold.lv_activity_mai_comments.getTag().toString())) {//子级评论的列表展示
			viewHold.lv_activity_mai_comments.setAdapter(null);
		}

		deal(dataList.get(position), viewHold, position);
		return convertView;
	}

	/**
	 * 回复列表adapter
	 */
	private MaiCommentContentAdapter contentAdapter;

	@Override
	public void deal(final HashMap<String, Object> data,
			final ViewHolder viewHold, final int position) {
		
		
		holders.add(viewHold);
		LogUtils.toDebugLog("holder", "holder的个数"+holders.size());
		
		
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		String headUrl = data.get("iv_comment_head").toString().trim();
		String time = data.get("tv_comment_time").toString().trim();
		String desc = data.get("tv_comment_desc").toString().trim();
		final String name = data.get("tv_comment_name").toString().trim();
		if (desc.length() < 45) {
			viewHold.iv_comment_more.setVisibility(View.GONE);
		} else {
			viewHold.iv_comment_more.setVisibility(View.VISIBLE);
		}
		final String activityid = data.get("activityid").toString().trim();
		final String commentid = data.get("commentid").toString().trim();
		final String mai_communicate = data.get("mai_communicate").toString()
				.trim();
		viewHold.iv_comment_reply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ReplyEntity event = new ReplyEntity();
				event.setPosition(position);
				if (!isShow) {
					event.setEnable(true);
					isShow = true;
				} else {
					event.setEnable(false);
					isShow = false;
				}
				event.setReplyUserName(name);
				EventBus.getDefault().post(event);
			}
		});
		if (!TextUtils.isEmpty(headUrl)) {
			download(viewHold.iv_comment_head, headUrl);
		}
		if (!TextUtils.isEmpty(desc)) {
			viewHold.tv_comment_desc.setText(desc);
		}
		if (!TextUtils.isEmpty(name)) {
			viewHold.tv_comment_name.setText(name);
		}
		if (!TextUtils.isEmpty(time)) {
			viewHold.tv_comment_time.setText(time);
		}
		if (!TextUtils.isEmpty(mai_communicate)) {
			viewHold.tv_mai_comment_communicate.setText(String.format(
					context.getString(R.string.mai_communicate),
					mai_communicate));
		}
		
		
		if(holders.size()-1<position){
			//holders的大小需 大于等于 位置position的值，否则会造成下方代码越界
		}else{
			
			if ("0".equals(mai_communicate)) {
				holders.get(position).lv_activity_mai_comments.setVisibility(View.GONE);
			} else {
				if (holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show) != null
						&& (boolean) holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show)) {
					viewHold.lv_activity_mai_comments.setVisibility(View.VISIBLE);
				} else {
					viewHold.lv_activity_mai_comments.setVisibility(View.GONE);
				}
			}
			
		}
		viewHold.tv_mai_comment_communicate
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if ("0".equals(mai_communicate)) {
							return;
						}
						if (holders.get(position).lv_activity_mai_comments
								.getVisibility() == View.VISIBLE) {
							holders.get(position).lv_activity_mai_comments
									.setTag(R.id.tag_show, false);
							holders.get(position).lv_activity_mai_comments
									.setVisibility(View.GONE);
						} else {
							holders.get(position).lv_activity_mai_comments
									.setTag(R.id.tag_show, true);
							holders.get(position).lv_activity_mai_comments
									.setVisibility(View.VISIBLE);
						}
					}
				});

		viewHold.iv_comment_report.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ReportCommentFragment fragment = new ReportCommentFragment();
				Bundle args = new Bundle();
				args.putString("comment_id", data.get("commentid").toString()
						.trim());
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});
		if (Integer.parseInt(mai_communicate) != 0) {

			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
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
							map.put("iv_comment_head", data.getUser()
									.getAvatar());
							map.put("tv_comment_name", data.getUser().getName());
							map.put("tv_comment_time", data.getComment()
									.getTime());
							map.put("tv_comment_desc", data.getComment()
									.getContent());
							map.put("to_user_id", data.getUser().getId() + "");
							map.put("replyId", position + "");
							dataList.add(map);
						}
						contentAdapter = new MaiCommentContentAdapter(
								viewHold.lv_activity_mai_comments, dataList,
								R.layout.activity_mai_comments_item);//涉及到子级评论的具体列表内容，与之前列表的显示与否无关

						viewHold.lv_activity_mai_comments
								.setAdapter(contentAdapter);

					}
				}

				@Override
				public boolean stop() {
					return false;
				}
			});
		}
		viewHold.ll_comment_desc.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (viewHold.tv_comment_desc.getMaxLines() > 5) {
					viewHold.tv_comment_desc.setMaxLines(5);
					viewHold.iv_comment_more
							.setImageResource(R.drawable.arrow_down);
				} else {
					viewHold.tv_comment_desc.setMaxLines(Integer.MAX_VALUE);
					viewHold.iv_comment_more
							.setImageResource(R.drawable.arrow_up);
				}
			}
		});
		final ArrayList<String> photos = (ArrayList<String>) data.get("photos");

		if (photos != null) {
			viewHold.tv_mai_images_count.setVisibility(View.VISIBLE);
			viewHold.tv_mai_images_count.setText(String.format(
					context.getString(R.string.images_count), photos.size()));
		} else {
			viewHold.tv_mai_images_count.setVisibility(View.GONE);
		}
		if (photos == null) {
			viewHold.gv_comment_images.setVisibility(View.GONE);
		}
		if (photos != null) {
			if (photos.size() > 0) {
				viewHold.gv_comment_images.setVisibility(View.VISIBLE);
			} else {
				viewHold.gv_comment_images.setVisibility(View.GONE);
			}
			MaiCommentGVAdapter gvAdapter = new MaiCommentGVAdapter(context,photos);

			viewHold.gv_comment_images.setAdapter(gvAdapter);
			
			//针对GridView的工具类
			GridViewUtils.updateGridViewLayoutParams(
					viewHold.gv_comment_images, 4, (int) context.getResources()
							.getDimension(R.dimen.l_r_10));
		}

		//图片点击后展开的展示效果
		viewHold.gv_comment_images
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						DetailHeaderPagerAdapter headPagerAdapter = new DetailHeaderPagerAdapter(
								activity.getActivity(), photos);
						ImageGalleryDialog dialog = ImageGalleryDialog
								.newInstance(headPagerAdapter, arg2);
						dialog.show(activity.getFragmentManager(), "gallery");
					}
				});
	}

	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		@InjectView
		LinearLayout ll_comment_desc;
		@InjectView
		CircularImage iv_comment_head;
		RecyclingImageView iv_comment_reply;
		@InjectView
		RelativeLayout rl_comment_item_content;
		@InjectView
		TextView tv_comment_name, tv_comment_time, tv_comment_desc,
				tv_mai_comment_communicate, tv_mai_images_count;
		@InjectView
		RecyclingImageView iv_comment_report, iv_comment_more;
		@InjectView
		MyGridView gv_comment_images;
		MySubListView lv_activity_mai_comments;// 评论列表
		@InjectView
		View line;

	}

	/**
	 * 刷新adapter
	 * 
	 * @param position
	 * @param activityid
	 * @param commentid
	 */
	public void refresh(final int position, String activityid, String commentid) {
		final ViewHolder viewHolder = holders.get(position);
		MaiCommentContentAdapter adapter = (MaiCommentContentAdapter) viewHolder.lv_activity_mai_comments
				.getAdapter();

		InternetConfig config = new InternetConfig();
		config.setKey(0);
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
					contentAdapter = new MaiCommentContentAdapter(
							viewHolder.lv_activity_mai_comments, dataList,
							R.layout.activity_mai_comments_item);
					viewHolder.lv_activity_mai_comments
							.setAdapter(contentAdapter);

				}
			}

			@Override
			public boolean stop() {
				return false;
			}
		});
	}

}
