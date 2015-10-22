package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.MaiCommentContentAdapter.ViewHolder;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.R;

/**
 * TODO 迈友回复列表
 * 
 * @author bhxx
 * 
 */
public class MaiCommentContentAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {
	public MaiCommentContentAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(final HashMap<String, String> data, ViewHolder viewHold,
			int position) {
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		if (!TextUtils.isEmpty(data.get("tv_comment_desc"))) {
			String conetne = data.get("tv_comment_desc").replace(" 回复 ",
					context.getString(R.string.reply_green));
			viewHold.tv_comment_desc.setText(Html.fromHtml(conetne));
		}

		if (!TextUtils.isEmpty(data.get("iv_comment_head"))) {
			download(viewHold.iv_comment_head, data.get("iv_comment_head"));
		}

		viewHold.tv_comment_desc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				System.out.println(data.get("to_user_id"));
				if (GlobalValue.user.getId() == Integer.parseInt(data
						.get("to_user_id"))) {
					return;
				}
				String to_user_id = data.get("to_user_id").toString();
				String replyId = data.get("replyId").toString();
				ReplyEntity event = new ReplyEntity();
				event.setEnable(true);
				event.setReplyUserName(data.get("tv_comment_name"));
				event.setPosition(Integer.parseInt(replyId));
				event.setToUserId(Integer.parseInt(to_user_id));
				EventBus.getDefault().post(event);
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
		@InjectView
		CircularImage iv_comment_head;
		@InjectView
		TextView tv_comment_desc;
		@InjectView
		View line;
	}
}
