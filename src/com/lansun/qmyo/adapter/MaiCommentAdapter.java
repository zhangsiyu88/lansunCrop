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
/*import com.lansun.qmyo.view.CircularImage;*/
import com.lansun.qmyo.override.CircleImageView;
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
			viewHold.iv_comment_head = (CircleImageView) convertView
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
			
			//书写评论时对应的门店名称
			viewHold.tv_comment_from = (TextView) convertView
					.findViewById(R.id.tv_comment_from);

			viewHold.tv_comment_desc = (TextView) convertView
					.findViewById(R.id.tv_comment_desc);
			viewHold.tv_reply = (TextView) convertView
					.findViewById(R.id.tv_reply);
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
		LogUtils.toDebugLog("holder", "开始： 我复用的是第"+viewHold.lv_activity_mai_comments.getTag()+"个convertView");
		
		if (position != Integer.parseInt(viewHold.lv_activity_mai_comments.getTag().toString())) {//子级评论的列表展示
			viewHold.lv_activity_mai_comments.setAdapter(null);
		}
		
		
		/*if(position<holders.size()-1){
			viewHold = holders.get(position);
		}*/
		
		deal(dataList.get(position), viewHold, position);
		return convertView;
	}

	/**
	 * 回复列表adapter
	 */
	private MaiCommentContentAdapter contentAdapter;

	
	/**
	 * 将数据和ViewHold的对象连接起来
	 */
	@Override
	public void deal(final HashMap<String, Object> data,
			final ViewHolder viewHold, final int position) {
		
		
		//1.1 当position不断增大且扩充着Holders列表时，我们将viewHold填充至Holders列表中去
		
		/*
		 * 1.2 当向上回滚时，此时的position会变小，但此时的有convertView.getTag()拿到的ViewHold是不需要将其重新写入Holders列表的，这样就避免了由于复用covertView时，
		 * 将之前复用此对象的ViewHold中的getTag(R.id.tag_show)得到的obj作用在了新生成的position位置上的Item上
		 */
		
		/*if(position< holders.size()-1){
			
			//DO-NOThing
		}else{
			
			holders.add(position,viewHold);
			holders.get(position).lv_activity_mai_comments.setTag(R.id.tag_show,false);
			LogUtils.toDebugLog("holder", "position: "+position);
			LogUtils.toDebugLog("holder", "holder的个数"+holders.size());
		}*/
		
		
		LogUtils.toDebugLog("holder", "    position: "+position);
		LogUtils.toDebugLog("holder", "holders.size: "+holders.size());
		if(position<=holders.size()-1){
			/* 如果此时position位置上在Holders中存在时，从convertView中拿到的viewHold是不需要重新塞入到Holders中的
			 * 之前的数据就存在，所以无需下面的删除 remove和重新 add进入Holders
			 * */
//			   holders.remove(position);
//			   holders.add(position,viewHold);
			LogUtils.toDebugLog("holder", "position <= holders.size()-1:   去除的是holder中的第"+position+"个对象");
		}else{
			//下面这句代码： 保证了在新生成holder时，即使复用了之前的convertView，也要保证此时拿到的viewHold中的lv_activity_mai_comments.getTag(R.id.tag_show)标签为 正常的false;
			holders.add(position,viewHold);
//			holders.set(position,viewHold);
			LogUtils.toDebugLog("holder", "position > holders.size()-1   ，当前添加至Holders列表中的viewHold到第 "+position+" 位上");
			holders.get(position).lv_activity_mai_comments.setTag(R.id.tag_show,false);
			
			LogUtils.toDebugLog("holder", "getTag(R.id.tag_show): "+ holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show)+"");
		}
		
//		LogUtils.toDebugLog("holder", "添加进去的是holder中的第"+position+"个对象");
//		LogUtils.toDebugLog("holder", "holder的个数"+holders.size());
		
		
		
		if (position + 1 == dataList.size()){
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
		LogUtils.toDebugLog("commentid", "commentid的值为： "+commentid);
		
		final String mai_communicate = data.get("mai_communicate").toString().trim();
		
		/*final String store_name = data.get("store_name").toString().trim();*/
		
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
		/*if (!TextUtils.isEmpty(store_name)) {
			viewHold.tv_comment_from.setText(store_name);
		}*/
		if (!TextUtils.isEmpty(mai_communicate)) {
			viewHold.tv_mai_comment_communicate.setText(String.format(
					context.getString(R.string.mai_communicate),
					mai_communicate));
			
			viewHold.tv_reply.setText(String.format(
					context.getString(R.string.rapley_counts),
					mai_communicate));
		}
		viewHold.tv_comment_from.setText(String.format(activity.getString(R.string.tv_comment_from), "ThreeBodyPlanet"));
		
		
		/*if(holders.size()-1<position){
			//holders的大小需 大于等于 位置position的值，否则会造成下方代码越界
		}else{
			if ("0".equals(mai_communicate)){
			holders.get(position).lv_activity_mai_comments.setVisibility(View.GONE);
		} else {
			if (holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show) != null
					&& (boolean) holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show)) {
				viewHold.lv_activity_mai_comments.setVisibility(View.VISIBLE);
			} else {
				viewHold.lv_activity_mai_comments.setVisibility(View.GONE);
			}
		}
		}*/
		
		
		/*
		 * 当之前的评论页面并未关闭的时候，当再一次滚动到该条目时，需要获取当前位置上的评论回复列表的展开标签，若为打开则将当前的ViewHold中的评论回复列表展开为可见
		 */
		if ("0".equals(mai_communicate)){
			holders.get(position).lv_activity_mai_comments.setVisibility(View.GONE);
			
			viewHold.lv_activity_mai_comments.setVisibility(View.GONE);
		} else {
			if (holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show) != null
					&& (boolean) holders.get(position).lv_activity_mai_comments.getTag(R.id.tag_show)) {
				
				
				viewHold.lv_activity_mai_comments.setVisibility(View.VISIBLE);
				LogUtils.toDebugLog("holder", "可能是最后一个： getView()中的deal()方法中，拿的是holder中的第"+position+"个对象");
				
			} else {
				viewHold.lv_activity_mai_comments.setVisibility(View.GONE);
			}
		}
		
		
		viewHold.tv_mai_comment_communicate
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if ("0".equals(mai_communicate)) {
							LogUtils.toDebugLog("holder", "评论列表  为 空！");
							return;
						}
						if (holders.get(position).lv_activity_mai_comments
								.getVisibility() == View.VISIBLE) {
							
							LogUtils.toDebugLog("holder", "评论列表  关闭展示：拿的是holder中的第"+position+"个对象");
							holders.get(position).lv_activity_mai_comments
									.setTag(R.id.tag_show, false);
							holders.get(position).lv_activity_mai_comments
									.setVisibility(View.GONE);
							
							viewHold.lv_activity_mai_comments.setVisibility(View.GONE);//Add
							
						} else {
							LogUtils.toDebugLog("holder", "评论列表  正在展示：拿的是holder中的第"+position+"个对象");
							holders.get(position).lv_activity_mai_comments
									.setTag(R.id.tag_show, true);
							holders.get(position).lv_activity_mai_comments
									.setVisibility(View.VISIBLE);
							
							viewHold.lv_activity_mai_comments.setVisibility(View.VISIBLE);//Add
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
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			
			LogUtils.toDebugLog("commentid", "commentid的值为： "+commentid);
			LogUtils.toDebugLog("commentid", "activityid的值为： "+activityid);
			
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
						/*contentAdapter = new MaiCommentContentAdapter(
								viewHold.lv_activity_mai_comments, dataList,
								R.layout.activity_mai_comments_item);//涉及到子级评论的具体列表内容，与之前列表的显示与否无关

						viewHold.lv_activity_mai_comments
								.setAdapter(contentAdapter);*/
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
					viewHold.iv_comment_more.setImageResource(R.drawable.arrow_down);
				} else {
					viewHold.tv_comment_desc.setMaxLines(Integer.MAX_VALUE);
					viewHold.iv_comment_more.setImageResource(R.drawable.arrow_up);
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
		
		
		//在所有数据完成复制的时候，在进行存储操作
		/*holders.remove(position);
		holders.add(position,viewHold);*/
		
	}

	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		
		@InjectView
		TextView tv_comment_from;
		@InjectView
	    TextView tv_reply;
		@InjectView
		LinearLayout ll_comment_desc;
		@InjectView
		CircleImageView iv_comment_head;
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
