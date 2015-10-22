package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.SubMyListView;
import com.lansun.qmyo.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 我的评论列表
 * @author bhxx
 *
 */
public class MineCommentAdapter extends CommonAdapter{
	public MineCommentAdapter(Activity activity,
			ArrayList<String> data) {
		this.activity = activity;
		this.inflater = LayoutInflater.from(activity);
		this.data = data;
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public View view(int position, View convertView, final ViewGroup parent) {
//		DetailHeaderAdapter adapter = new DetailHeaderAdapter(activity, null, false);
			convertView =  inflater.inflate(R.layout.activity_mine_comments_item, null);
		return convertView;
	}
	
	class Holder{
		CircularImage iv_comment_head;
		ImageView iv_comment_card_type;
		TextView tv_comment_name;
		TextView tv_comment_time;
		TextView tv_comment_desc;
		ImageView iv_comment_report;
		GridView gv_comment_images;
		SubMyListView lv_activity_mai_comments;
	}
}
