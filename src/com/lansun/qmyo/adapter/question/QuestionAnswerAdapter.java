package com.lansun.qmyo.adapter.question;
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
		if (position==0) {
			holder.tv_user_question.setText(detail.getContent());
			String answer=String.valueOf(detail.getAnswer());
			if ("null".equals(answer)||"".equals(answer)||" ".equals(answer)) {
				holder.tv_secretary_answer.setText(GlobalValue.mySecretary.getName()+"已经收到您的任务指派哦，2小时内必有回复，请耐心等待哟。如果您对收到的回答不满意，还可以继续追问哦~");
			}else {
				holder.tv_secretary_answer.setText(GlobalValue.mySecretary.getName()+answer);
			}
		}else {
			holder.tv_user_question.setText(detail.getItems().get(position-1).getContent());
			String answer=String.valueOf(detail.getItems().get(position-1).getAnswer());
			if ("null".equals(answer)||"".equals(answer)||" ".equals(answer)) {
				holder.tv_secretary_answer.setText(GlobalValue.mySecretary.getName()+"已经收到您的任务指派哦，2小时内必有回复，请耐心等待哟。如果您对收到的回答不满意，还可以继续追问哦~");
			}else {
				holder.tv_secretary_answer.setText(GlobalValue.mySecretary.getName()+answer);
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
