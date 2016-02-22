package com.lansun.qmyo.adapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
//import android.support.v7.widget.RecyclerView.Adapter;
//import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.R;
import com.lansun.qmyo.domain.QuestionDetailItem;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeLayout;
import com.lansun.qmyo.view.CircularImage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
public class QuestionListAdapter extends BaseAdapter {
	
	private List<QuestionDetailItem> mList;
	private OnItemClickCallBack callBack;
	
	private Context mContext;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	//private ArrayList<HashMap<String, Object>> mList;
	
	
	public QuestionListAdapter(Context context,List<QuestionDetailItem> list,OnItemClickCallBack callBack){
		super();
		this.mContext = context;
		this.mList=list;
		this.callBack=callBack;
		mInflater = LayoutInflater.from(mContext);
	}
	
	
	  public QuestionListAdapter(Context mContext) {
			super();
			this.mContext = mContext;
			mInflater = LayoutInflater.from(mContext);
		}



		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
		@SuppressWarnings("unchecked")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView != null) {
				holder = (ViewHolder) convertView.getTag();
			}else {
				convertView = mInflater.inflate(R.layout.my_secretary_question_item, null);
				holder = ViewHolder.fromValues(convertView);
				convertView.setTag(holder);
			}
			
			if ("0".equals(String.valueOf(mList.get(position).getIs_read()))) {
				holder.have_information.setVisibility(View.VISIBLE);
			}else {
				holder.have_information.setVisibility(View.INVISIBLE);
			}
			
			/**
			 * 若用户点击过快，进入我的私人秘书页面时，GlobalValue.mySecretary还没有从网络获取到，那么下面去get()会出现奔溃的现象，故在此进行防护
			 */
			if(GlobalValue.mySecretary != null){
				
				ImageLoader.getInstance().displayImage(GlobalValue.mySecretary.getAvatar(), holder.c_iv, new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String arg0, View arg1) {
						
					}
					@Override
					public void onLoadingFailed(String arg0, View view, FailReason arg2) {
						((ImageView)view).setImageResource(R.drawable.secretary_default_avatar);
					}
					@Override
					public void onLoadingComplete(String arg0, View view, Bitmap bitmap) {
						((ImageView)view).setImageBitmap(bitmap);
					}
					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
						
					}
				});
				
				holder.tv_title.setText(GlobalValue.mySecretary.getName());
				type=mList.get(position).getType();
				if ("travel".equals(type)) {
					type="旅行度假";
				}else if ("shopping".equals(type)) {
					type="新品购物";
				}else if ("party".equals(type)) {
					type="盛宴狂欢";
				}else if ("life".equals(type)) {
					type="高质生活";
				}else if ("student".equals(type)) {
					type="留学服务";
				}else if ("investment".equals(type)) {
					type="理财投资";
				}else if ("card".equals(type)) {
					type="办卡推荐";
				}
				holder.tv_type.setText(" "+type+" ");
				//获得返回时间
				String get_time=mList.get(position).getTime();
				//截取返回日期
				String current_date=get_time.substring(0,10).trim();
				Log.e("current_date", current_date);
				//截取返回时间
				String current_time=get_time.substring(11,get_time.length());
				long time=System.currentTimeMillis();
				//格式化日期
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				Date date=new Date(time);
				//获取当前日期
				String newDate=format.format(date).trim();
				Log.e("newDate", newDate);
				//对比日期是否一样
				if (newDate.equals(current_date)){
					holder.tv_time.setText(current_time);
				}else {
					holder.tv_time.setText(current_date);
				}
				SpannableString sp_question=new SpannableString("问题:");
				SpannableString sp_question_content=new SpannableString(Html.fromHtml("<font style='font-size:12pt' color='#939393'>"+"   "+mList.get(position).getContent()+"</font>"));
				SpannableStringBuilder builder_question=new SpannableStringBuilder();
				builder_question.append(sp_question).append(sp_question_content);
				holder.tv_question.setText(builder_question);
				
				SpannableString sp_answer=new SpannableString("回复:");
				SpannableString sp_answer_content=new SpannableString(Html.fromHtml
						("<font style='font-size:12pt' color='#939393'>"+"   "+GlobalValue.mySecretary.getName()+
								mList.get(position).getAnswer()+"</font>"));//已收到您的任务指派哦,2小时内必有回复,请耐心等待哟。如果您对收到的回答不满意,还可以继续追问哦~
				SpannableStringBuilder answer=new SpannableStringBuilder();
				answer.append(sp_answer).append(sp_answer_content);
				holder.tv_answer.setText(answer);
				
				holder.view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						if(position >= mList.size()){//点击的位置position超过了列表的长度
							LogUtils.toDebugLog("mySercretary", "点击了，但不做反应");
							return;
						}
						holder.have_information.setVisibility(View.GONE);
						mList.get(position).setIs_read(1);
						callBack.callBack(Integer.valueOf(mList.get(position).getId()),type);
					}
				});
				
			}
			return convertView;
		}
	
	private String type;


//	@Override
//	public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//		View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_secretary_question_item, viewGroup,false);
//		MyViewHolder holer=new MyViewHolder(view);
//		return holer;
//	}
	
	
	
	public interface OnItemClickCallBack{
		void callBack(int question_id,String type);
	}
	
	
	
	public static class ViewHolder{
		private View view;
		private CircularImage c_iv;
		private TextView tv_title,tv_type,tv_time,tv_question,tv_answer,have_information;
		
		public ViewHolder(View itemView) {
			
			this.view=itemView;
			this.c_iv=(CircularImage) itemView.findViewById(R.id.iv_mine_question_head);
			this.tv_title=(TextView)itemView.findViewById(R.id.my_question_title_tv);
			this.tv_type=(TextView)itemView.findViewById(R.id.my_question_type_tv);
			this.tv_time=(TextView)itemView.findViewById(R.id.my_question_time);
			this.tv_question=(TextView)itemView.findViewById(R.id.question);
			this.tv_answer=(TextView)itemView.findViewById(R.id.answer);
			this.have_information=(TextView)itemView.findViewById(R.id.have_information);
		}
		
		
		public static ViewHolder fromValues(View view) {
			return new ViewHolder(view);
				
		}
		
		
	}
}
