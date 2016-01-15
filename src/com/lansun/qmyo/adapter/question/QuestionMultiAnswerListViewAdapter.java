package com.lansun.qmyo.adapter.question;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.QAMetaData;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.domain.QuestionDetailNew;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
//import android.support.v7.widget.RecyclerView.Adapter;
//import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * 该适配器根据最新（假设接口完成）进行数据的解析
 * 
 * @author Yeun.Zhang
 *
 */
public class QuestionMultiAnswerListViewAdapter extends BaseAdapter{
	
	private QuestionDetailNew detail;
	public ArrayList<QAMetaData> mArrayQAMetaData ;
	
	final int VIEW_TYPE = 3;

	final int TYPE_1 = 0;

	final int TYPE_2 = 1;

	final int TYPE_3 = 2;

	public QuestionMultiAnswerListViewAdapter(QuestionDetailNew detail) {
		this.detail=detail;
	}
	public QuestionMultiAnswerListViewAdapter(ArrayList<QAMetaData> arrayQAMetaData) {
		this.mArrayQAMetaData = arrayQAMetaData;
	}
	
	@Override
	public int getItemViewType(int position) {
		int type = mArrayQAMetaData.get(position).getType();
		if(type == 1){
			return TYPE_1;
		}
		if(type == 2){
			return TYPE_2;
		}
		if(type == 3){
			return TYPE_3;
		}
		return super.getItemViewType(position);
	}
	/**
	 * size
	 */
//	@Override
//	public int getItemCount() {
//		return mArrayQAMetaData.size();
//		//return detail.getItems()==null?1:detail.getItems().size()+1;
//	}
//	
//	@Override
//	public void onBindViewHolder(MyViewHolder holder, int position) {
//		//添加头像
//	    loadPhoto(holder);
//	    
//	    switch(holder.getItemViewType()){
//		    case  TYPE_1: 
//		    	holder.tv_user_question.setText(mArrayQAMetaData.get(position).getContent());
//		    	holder.tv_secretary_answer.setText(mArrayQAMetaData.get(position).getAnswer());
//		    	
//		    	holder.tv_conversation_time.setVisibility(View.INVISIBLE);
//		    	
//		    	if(!TextUtils.isEmpty(mArrayQAMetaData.get(position).getTime())){
//		    		holder.tv_conversation_time.setVisibility(View.VISIBLE);
//		    		holder.tv_conversation_time.setText(mArrayQAMetaData.get(position).getTime());
//		    	}
//		    	break;
//		    case  TYPE_2: 
//		    	holder.tv_secretary_answer.setText(mArrayQAMetaData.get(position).getAnswer());
//		    	break;
//		    case  TYPE_3: 
//		    	holder.tv_conversation_title.setVisibility(View.VISIBLE);
//		    	holder.tv_conversation_title.setText(mArrayQAMetaData.get(position).getContent());
//		    	
//		    	holder.tv_conversation_time.setVisibility(View.GONE);
//		    	holder.tv_secretary_answer.setVisibility(View.GONE);
//		    	holder.tv_user_question.setVisibility(View.GONE);
//		    	holder.iv_secretary_head.setVisibility(View.GONE);
//		    	holder.iv_user_head.setVisibility(View.GONE);
//		    	
//		    	holder.ll_secretary_head.setVisibility(View.GONE);
//		    	break;
//	    }
//	}
//	
//	
//	@Override
//	public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//		LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
//		MyViewHolder holder = null;
//		if(viewType == TYPE_1){//完整的一问一答形式的ItemView
//			View view=inflater.inflate(R.layout.my_secretary_ask_answer_item, viewGroup,false);
//			holder = new MyViewHolder(view, viewType);
//		}
//		if(viewType == TYPE_2){//小秘书有多次回答
//			View view=inflater.inflate(R.layout.my_secretary_ask_multianswer_item, viewGroup,false);
//			holder = new MyViewHolder(view, viewType);
//		}
//		if(viewType == TYPE_3){//活动的标题
//			View view=inflater.inflate(R.layout.my_secretary_ask_multianswer_item, viewGroup,false);
//			holder = new MyViewHolder(view, viewType);
//		}
//		return holder;
//	}
	
	
	public class MyViewHolder {
		private CircularImage iv_secretary_head,iv_user_head;
		private TextView tv_secretary_answer,tv_user_question,tv_conversation_time,tv_conversation_title;
		private LinearLayout ll_secretary_head;
		private int viewType;
		private View view;
		public MyViewHolder(View itemView,int viewtype) {
			    viewType = viewtype;
			    view = itemView;
			    ll_secretary_head=(LinearLayout)itemView.findViewById(R.id.ll_secretary_head);
				iv_user_head=(CircularImage)itemView.findViewById(R.id.iv_user_head);
				tv_user_question=(TextView)itemView.findViewById(R.id.tv_user_question);
				iv_secretary_head=(CircularImage)itemView.findViewById(R.id.iv_secretary_head);
				tv_secretary_answer=(TextView)itemView.findViewById(R.id.tv_secretary_answer);
				tv_conversation_time=(TextView)itemView.findViewById(R.id.tv_conversation_time);
				tv_conversation_title=(TextView)itemView.findViewById(R.id.tv_conversation_title);
		}
		public int getViewType() {
			return viewType;
		}
		public void setViewType(int viewType) {
			this.viewType = viewType;
		}
		public View getView() {
			return view;
		}
		public void setView(View view) {
			this.view = view;
		}
	}

	@Override
	public int getCount() {
		return mArrayQAMetaData.size();
//		return 0;
	}

	@Override
	public MyViewHolder getItem(int position) {
		int viewType = getItemViewType(position);
		Context ctx = App.app;
		LayoutInflater inflater=LayoutInflater.from(ctx);
		MyViewHolder holder = null;
		if(viewType == TYPE_1){//完整的一问一答形式的ItemView
			View view=inflater.inflate(R.layout.my_secretary_ask_answer_item, null,false);
			holder = new MyViewHolder(view, viewType);
		}
		if(viewType == TYPE_2){//小秘书有多次回答
			View view=inflater.inflate(R.layout.my_secretary_ask_multianswer_item, null,false);
			holder = new MyViewHolder(view, viewType);
		}
		if(viewType == TYPE_3){//活动的标题
			View view=inflater.inflate(R.layout.my_secretary_ask_multianswer_item, null,false);
			holder = new MyViewHolder(view, viewType);
		}
		return holder;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyViewHolder viewHolder;
		if(convertView!=null){
			viewHolder = (MyViewHolder) convertView.getTag();//拿到convertView的内容
			if(viewHolder.getViewType()!= getItemViewType(position)){
				viewHolder = (MyViewHolder) getItem(position);//拿到新的MyViewHolder内容
				convertView = viewHolder.getView();
				convertView.setTag(viewHolder);
			}
		}else{
			viewHolder = (MyViewHolder) getItem(position);//拿到新的MyViewHolder内容
			convertView = viewHolder.getView();
			convertView.setTag(viewHolder);
		}
		    loadPhoto(viewHolder);
		    switch(getItemViewType(position)){
			    case  TYPE_1: 
			    	viewHolder.tv_user_question.setText(mArrayQAMetaData.get(position).getContent());
			    	viewHolder.tv_secretary_answer.setText(mArrayQAMetaData.get(position).getAnswer());
			    	
			    	viewHolder.tv_conversation_time.setVisibility(View.INVISIBLE);
			    	
			    	if(!TextUtils.isEmpty(mArrayQAMetaData.get(position).getTime())){
			    		viewHolder.tv_conversation_time.setVisibility(View.VISIBLE);
			    		viewHolder.tv_conversation_time.setText(mArrayQAMetaData.get(position).getTime());
			    	}
			    	break;
			    case  TYPE_2: 
			    	viewHolder.tv_secretary_answer.setText(mArrayQAMetaData.get(position).getAnswer());
			    	break;
			    case  TYPE_3: 
			    	viewHolder.tv_conversation_title.setVisibility(View.VISIBLE);
			    	viewHolder.tv_conversation_title.setText(mArrayQAMetaData.get(position).getContent());
			    	
			    	viewHolder.tv_conversation_time.setVisibility(View.GONE);
			    	viewHolder.tv_secretary_answer.setVisibility(View.GONE);
			    	viewHolder.tv_user_question.setVisibility(View.GONE);
			    	viewHolder.iv_secretary_head.setVisibility(View.GONE);
			    	viewHolder.iv_user_head.setVisibility(View.GONE);
			    	
			    	viewHolder.ll_secretary_head.setVisibility(View.GONE);
			    	break;
		    }
		return convertView;
	}
	
	/*
	 * 对一问一答式的条目执行头像加载的问题
	 */
	private void loadPhoto(MyViewHolder holder) {
		//在位置上放上需要的图片
		ImageLoader.getInstance().displayImage(GlobalValue.user.getAvatar(), holder.iv_user_head, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				
			}
			@Override
			public void onLoadingFailed(String arg0, View view, FailReason arg2) {
				((ImageView)view).setImageResource(R.drawable.default_avatar);
			}
			@Override
			public void onLoadingComplete(String arg0, View view, Bitmap bitmap) {
				if (GlobalValue.user.getAvatar()==null) {
					((ImageView)view).setImageResource(R.drawable.default_avatar);
				}else {
					((ImageView)view).setImageBitmap(bitmap);
				}
			}
			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				
			}
		});
		ImageLoader.getInstance().displayImage(GlobalValue.mySecretary.getAvatar(), holder.iv_secretary_head, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				
			}
			@Override
			public void onLoadingFailed(String arg0, View view, FailReason arg2) {
				((ImageView)view).setImageResource(R.drawable.secretary_default_avatar);
			}
			@Override
			public void onLoadingComplete(String arg0, View view, Bitmap bitmap) {
				if (GlobalValue.user.getAvatar()==null) {
					((ImageView)view).setImageResource(R.drawable.secretary_default_avatar);
				}else {
					((ImageView)view).setImageBitmap(bitmap);
				}
			}
			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				
			}
		});
	}
	
	/**
	 * 之前的无用代码
	 * 
	 * SimpleDateFormat format=new SimpleDateFormat("HH");
		final int hour=Integer.valueOf(format.format(new Date(System.currentTimeMillis())));
		Log.e("hour", String.valueOf(hour));
		
		
		if (position==0) {
			holder.tv_user_question.setText(detail.getContent());
			
			String answer=String.valueOf(detail.getAnswer());
			if ("null".equals(answer)||"".equals(answer)||" ".equals(answer)) {
				if (hour>=9&&hour<18) {
					holder.tv_secretary_answer.setText("小秘书已经收到您的留言喽，立即开启暴风处理模式~2小时内必定有回复！为保证答复质量，如需更多处理时间，小秘书也将第一时间告知~全心全意为你哟~");
				}else {
					holder.tv_secretary_answer.setText("收到您的留言喽~但但但...人家现在正休息呢，为了养足精神更好为您服务哦~小秘书开工后将立即处理（工作日9:00-18:00），谢谢体谅哟~");
				}
			}else {
				holder.tv_secretary_answer.setText(answer);
			}
		}else if (position==1) {
			holder.tv_user_question.setText(detail.getItems().get(0).getContent());
			
			String answer=String.valueOf(detail.getItems().get(0).getAnswer());
			
			if ("null".equals(answer)||"".equals(answer)||" ".equals(answer)) {
				if (hour>=9&&hour<18) {
					holder.tv_secretary_answer.setText("收到啦，给我点点时间来处理~");
				}else {
					holder.tv_secretary_answer.setText("收到您的留言喽~但但但...人家现在正休息呢，为了养足精神更好为您服务哦~小秘书开工后将立即处理（工作日9:00-18:00），谢谢体谅哟~");
				}
			}else {
				holder.tv_secretary_answer.setText(answer);
			}
		}else {
			//这个地方写的有点多余，后来人可以修改下
			holder.tv_user_question.setText(detail.getItems().get(position-1).getContent());
			String answer=String.valueOf(detail.getItems().get(position-1).getAnswer());
			
			String previou_answer=String.valueOf(detail.getItems().get(position-2).getAnswer());
			
			if ("null".equals(answer)||"".equals(answer)||" ".equals(answer)) {
				if ("null".equals(previou_answer)||"".equals(previou_answer)||" ".equals(previou_answer)) {
					if (hour>=9&&hour<18) {
						holder.tv_secretary_answer.setText("收到啦，给我点点时间来处理~爱你爱你么么哒~");
					}else {
						holder.tv_secretary_answer.setText("收到您的留言喽~但但但...人家现在正休息呢，为了养足精神更好为您服务哦~小秘书开工后将立即处理（工作日9:00-18:00），谢谢体谅哟~");
					}
				}else {
					if (hour>=9&&hour<18) {
						holder.tv_secretary_answer.setText("小秘书已经收到您的留言喽，立即开启暴风处理模式~2小时内必定有回复！为保证答复质量，如需更多处理时间，小秘书也将第一时间告知~全心全意为你哟~");
					}else {
						holder.tv_secretary_answer.setText("收到您的留言喽~但但但...人家现在正休息呢，为了养足精神更好为您服务哦~小秘书开工后将立即处理（工作日9:00-18:00），谢谢体谅哟~");
					}
				}
			}else {
				holder.tv_secretary_answer.setText(answer);
			}
		}
	 */
	
}
