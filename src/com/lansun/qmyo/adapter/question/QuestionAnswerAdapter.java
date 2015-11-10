package com.lansun.qmyo.adapter.question;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lansun.qmyo.R;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class QuestionAnswerAdapter extends Adapter<QuestionAnswerAdapter.MyViewHolder>{
	private QuestionDetail detail;
	public QuestionAnswerAdapter(QuestionDetail detail) {
		this.detail=detail;
	}
	/**
	 * size
	 */
	@Override
	public int getItemCount() {
		return detail.getItems()==null?1:detail.getItems().size()+1;
	}
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
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
		SimpleDateFormat format=new SimpleDateFormat("HH");
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
					holder.tv_secretary_answer.setText("收到啦，给我点点时间来处理~爱你爱你么么哒~");
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
	}
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
		View view=inflater.inflate(R.layout.my_secretary_ask_answer_item, viewGroup,false);
		MyViewHolder holder=new MyViewHolder(view, viewType);
		return holder;
	}
	public class MyViewHolder extends ViewHolder{
		private CircularImage iv_secretary_head,iv_user_head;
		private TextView tv_secretary_answer,tv_user_question;
		public MyViewHolder(View itemView,int viewtype) {
			super(itemView);
				iv_user_head=(CircularImage)itemView.findViewById(R.id.iv_user_head);
				tv_user_question=(TextView)itemView.findViewById(R.id.tv_user_question);
				iv_secretary_head=(CircularImage)itemView.findViewById(R.id.iv_secretary_head);
				tv_secretary_answer=(TextView)itemView.findViewById(R.id.tv_secretary_answer);
		}
	}
}
