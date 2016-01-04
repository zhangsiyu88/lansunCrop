package com.lansun.qmyo.fragment.comment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
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
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.comment.MaiCommentContentAdapterUpdate.ViewHolder;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.R;

/**
 * TODO 迈友回复列表
 * 
 * @author bhxx
 * 
 */
public class MaiCommentContentAdapterUpdate extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {
	private String mCommentUserName;
	private int indexOfReplay;
	private String htmlStr;
	private Context mCtx;

	public MaiCommentContentAdapterUpdate(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}
	public MaiCommentContentAdapterUpdate(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id,String userName) {
		super(listView, dataList, layout_id);
		mCommentUserName = userName;
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(final HashMap<String, String> data, ViewHolder viewHold,
			final int position) {
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		
		//1.注入时间
		if(!TextUtils.isEmpty(data.get("tv_comment_time"))){
			viewHold.tv_comment_replay_time.setText(data.get("tv_comment_time"));
		}
		
		//3.注入头像
		if (!TextUtils.isEmpty(data.get("iv_comment_head"))) {
			download(viewHold.iv_comment_head, data.get("iv_comment_head"));
		}
		
		//2.分解服务器端给出的单一String串
		
		/* 
		 * 2.1 如果回复的是当前活动评论的user，那么评论的内容是 不需要前面的   “@评论用户”
		 * 2.2 若回复的只是普通的其他的user，需要在 content处，添加上“回复@username”的内容
		 */
		if (!TextUtils.isEmpty(data.get("tv_comment_desc"))) {
			String content = data.get("tv_comment_desc"); //.replace(" 回复 ",context.getString(R.string.reply_green));
			
			LogUtils.toDebugLog("content", "实际返回的回复内容是： "+content);
			
			if(content.contains(" 回复 ")){
				LogUtils.toDebugLog("content", "content 包含 “ 回复 ”");
				
					indexOfReplay = content.indexOf(" 回复");
				int indexOfColon  = content.indexOf("：");//注意是中文的：
				
				String reply_username = content.substring(0, indexOfReplay); //回复评论的username
				LogUtils.toDebugLog("mCommentUserName", "reply_username:  "+reply_username);
				
				String reply_to_username = content.substring(indexOfReplay+4, indexOfColon);//回复二字之后到冒号之间的内容
				
				String  afterReplayStr= content.substring(indexOfReplay+1, content.length());//发表回复评论用户名之后的所有内容，从“回复”二字开始
				String  afterColonStr= content.substring(indexOfColon+1, content.length());
				
				viewHold.tv_comment_replay_user.setText(reply_username);
				LogUtils.toDebugLog("mCommentUserName", "mCommentUserName:  "+mCommentUserName);
				
				//判断是否时主评论用户进行的回复
				if(reply_username.equals(mCommentUserName)){
					
					//将人名拿出来
					
					 //String htmlStr = "<Data><![CDATA[<font color="+"\""+"#9DC14F"+"\""+"> @%1$s </font>]]></Data>";
					 int indexOfColonAfter = afterReplayStr.indexOf("：");
					 int indexOfReplyAfter = afterReplayStr.indexOf("回复 ");
					 //真实回复的内容
					 String contentStr = afterReplayStr.substring(indexOfColonAfter+1, afterReplayStr.length());
					 //将@的对象给截取出来
					 String atUserName = afterReplayStr.substring(indexOfReplyAfter+2, indexOfColonAfter);
					 //拼接出需要的字符串
//					 String showStr = "回复"+ String.format(htmlStr, atUserName)+contentStr;
					 String showStr = mCtx.getString(R.string.all_desc);
					 showStr =  String.format(showStr, atUserName ,contentStr);
					 
					viewHold.tv_comment_desc.setText(Html.fromHtml(showStr));
					
					
					
				}else{
					viewHold.tv_comment_desc.setText(afterColonStr);
				}
			//viewHold.tv_comment_desc.setText(Html.fromHtml(content));
		  }else{
			  LogUtils.toDebugLog("content", "content  不 包含“ 回复 ”");
			  
			  int indexOfColon = content.indexOf("：");
			  String reply_username = content.substring(0, indexOfColon);
			  String afterColonStr = content.substring(indexOfColon+1,content.length());
			  
			  viewHold.tv_comment_replay_user.setText(reply_username);
			  viewHold.tv_comment_desc.setText(afterColonStr);
		  }
		}
		
		
		
		
		
		
		
		
		
		

		
		
		viewHold.tv_comment_desc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				System.out.println(data.get("to_user_id"));
				if (GlobalValue.user.getId() == Integer.parseInt(data.get("to_user_id"))) {//不允许自己回复
					return;
				}
				String to_user_id = data.get("to_user_id").toString();
				String replyId = data.get("replyId").toString();
				int pos = position;
				ReplyEntity event = new ReplyEntity();
				event.setEnable(true);
				event.setReplyUserName(data.get("tv_comment_name"));
				
//				event.setPosition(Integer.parseInt(replyId));
				event.setPosition(position);
				
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
		@InjectView
		TextView tv_comment_replay_user;
		@InjectView
		TextView tv_comment_replay_time;
	}

	public void setMainCommentUserName(String name,Context ctx) {
		this.mCommentUserName = name;
		this.mCtx = ctx;
	}
}
