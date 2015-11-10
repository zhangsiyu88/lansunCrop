package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.AjaxCallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.CommentActivityAdapter.ViewHolder;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.ListViewEntity;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.ReportCommentFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.GridViewUtils;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.MyGridView;
import com.lansun.qmyo.view.MySubListView;
import com.lansun.qmyo.R;

public class CommentActivityAdapter extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	private Fragment fragment;

	public CommentActivityAdapter(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id,
			Fragment fragment) {
		super(listView, dataList, layout_id);
		this.fragment = fragment;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHold;
		if (convertView == null) {
			convertView = layoutInflater.inflate(layout_id, null);
			viewHold = new ViewHolder();
			viewHold.iv_comment_head = (CircularImage) convertView
					.findViewById(R.id.iv_comment_head);
			viewHold.tv_comment_time = (TextView) convertView
					.findViewById(R.id.tv_comment_time);
			viewHold.tv_comment_activity_name = (TextView) convertView
					.findViewById(R.id.tv_comment_activity_name);
			viewHold.tv_mai_comment_communicate = (TextView) convertView
					.findViewById(R.id.tv_mai_comment_communicate);
			viewHold.tv_comment_desc = (TextView) convertView
					.findViewById(R.id.tv_comment_desc);
			viewHold.lv_activity_mai_comments = (MySubListView) convertView
					.findViewById(R.id.lv_activity_mai_comments);
			viewHold.iv_comment_more = (RecyclingImageView) convertView
					.findViewById(R.id.iv_comment_more);
			// 计算图片
			viewHold.tv_comment_images_count = (TextView) convertView
					.findViewById(R.id.tv_comment_images_count);
			viewHold.tv_comment_images_count.setTag(position);

			viewHold.lv_activity_mai_comments.setTag(position);
			viewHold.gv_comment_images = (MyGridView) convertView
					.findViewById(R.id.gv_comment_images);
			viewHold.ll_comment_desc = (LinearLayout) convertView
					.findViewById(R.id.ll_comment_desc);
			viewHold.rl_comment_activity_name = (RelativeLayout) convertView
					.findViewById(R.id.rl_comment_activity_name);
			convertView.setTag(viewHold);
		}

		viewHold = (ViewHolder) convertView.getTag();
		if (position != Integer.parseInt(viewHold.lv_activity_mai_comments
				.getTag().toString())) {
			viewHold.lv_activity_mai_comments.setAdapter(null);
		}

		deal(dataList.get(position), viewHold, position);
		return convertView;
	}

	private ArrayList<ViewHolder> holders = new ArrayList<ViewHolder>();

	/**
	 * 回复列表adapter
	 */
	private MaiCommentContentAdapter contentAdapter;

	@Override
	public void deal(final HashMap<String, Object> data,
			final ViewHolder viewHold, final int position) {
		holders.add(viewHold);
		String time = data.get("tv_comment_time").toString().trim();
		String desc = data.get("tv_comment_desc").toString().trim();
		String name = data.get("tv_comment_activity_name").toString().trim();
		if (desc.length() < 45) {
			viewHold.iv_comment_more.setVisibility(View.GONE);
		} else {
			viewHold.iv_comment_more.setVisibility(View.VISIBLE);
		}
		final String activityid = data.get("activityId").toString().trim();
		final String commentid = data.get("commentid").toString().trim();
		final String mai_communicate = data.get("tv_mai_comment_communicate")
				.toString().trim();
		if (!TextUtils.isEmpty(desc)) {
			String conetne = data.get("tv_comment_desc").toString()
					.replace(" 回复 ", context.getString(R.string.reply_green));
			viewHold.tv_comment_desc.setText(Html.fromHtml(conetne));
		}
		if (!TextUtils.isEmpty(name)) {
			viewHold.tv_comment_activity_name.setText(name);
		}
		if (!TextUtils.isEmpty(time)) {
			viewHold.tv_comment_time.setText(time);
		}
		if (!TextUtils.isEmpty(mai_communicate)) {
			viewHold.tv_mai_comment_communicate.setText(String.format(
					context.getString(R.string.mai_communicate),
					mai_communicate));
		}
		if ("0".equals(mai_communicate)) {
			viewHold.lv_activity_mai_comments.setVisibility(View.GONE);
		} else {
			viewHold.lv_activity_mai_comments.setVisibility(View.VISIBLE);
		}
		viewHold.tv_mai_comment_communicate
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if ("0".equals(mai_communicate)) {
							return;
						}
						if (viewHold.lv_activity_mai_comments.getVisibility() == View.VISIBLE) {
							viewHold.lv_activity_mai_comments
									.setVisibility(View.GONE);
						} else {
							viewHold.lv_activity_mai_comments
									.setVisibility(View.VISIBLE);
						}
					}
				});

		viewHold.rl_comment_activity_name
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Fragment fragment = new ActivityDetailFragment();
						Bundle args = new Bundle();
						args.putString("shopId", data.get("shopId").toString());
						args.putString("activityId", data.get("activityId")
								.toString());
						fragment.setArguments(args);
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
				});
		if (Integer.parseInt(mai_communicate) != 0) {
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<String, Object>();
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
								R.layout.activity_mai_comments_item);

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
				if (viewHold.tv_comment_desc.getMaxLines() > 2) {
					viewHold.tv_comment_desc.setMaxLines(2);
					viewHold.iv_comment_more.setImageResource(R.drawable.more);
				} else {
					viewHold.tv_comment_desc.setMaxLines(Integer.MAX_VALUE);
					viewHold.iv_comment_more
							.setImageResource(R.drawable.more_close);
				}
			}
		});
		final ArrayList<String> photos = (ArrayList<String>) data.get("photos");
		if (photos == null) {
			viewHold.gv_comment_images.setVisibility(View.GONE);
		}

		if (photos != null) {
			viewHold.tv_comment_images_count.setVisibility(View.VISIBLE);
			viewHold.tv_comment_images_count.setText(String.format(
					context.getString(R.string.images_count), photos.size()));
		} else {
			viewHold.tv_comment_images_count.setVisibility(View.GONE);
		}

		if (photos != null) {
			if (photos.size() > 0) {
				viewHold.gv_comment_images.setVisibility(View.VISIBLE);
			} else {
				viewHold.gv_comment_images.setVisibility(View.GONE);
			}
			MaiCommentGVAdapter gvAdapter = new MaiCommentGVAdapter(context,
					photos);
			viewHold.gv_comment_images.setAdapter(gvAdapter);
			GridViewUtils.updateGridViewLayoutParams(
					viewHold.gv_comment_images, 3, (int) context.getResources()
							.getDimension(R.dimen.l_r_10));
		}

		viewHold.gv_comment_images
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						DetailHeaderPagerAdapter headPagerAdapter = new DetailHeaderPagerAdapter(
								fragment.getActivity(), photos);
						ImageGalleryDialog dialog = new ImageGalleryDialog()
								.newInstance(headPagerAdapter, arg2);
						dialog.show(fragment.getFragmentManager(), "gallery");
					}
				});
	}

	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 */
	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		public RelativeLayout rl_comment_activity_name;
		private RecyclingImageView iv_comment_more;
		private CircularImage iv_comment_head;
		private TextView tv_comment_activity_name, tv_comment_time,
				tv_comment_desc, tv_comment_images_count,
				tv_mai_comment_communicate;
		private MyGridView gv_comment_images;
		private View rl_comments_images, ll_comment_desc;
		private MySubListView lv_activity_mai_comments;
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
		HashMap<String, Object> head = new HashMap<String, Object>();
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
