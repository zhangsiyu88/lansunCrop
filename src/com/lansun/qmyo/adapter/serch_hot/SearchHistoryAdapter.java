package com.lansun.qmyo.adapter.serch_hot;
import java.util.List;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.listener.HotItemClickCallBack;
import com.lansun.qmyo.R;

public class SearchHistoryAdapter extends Adapter<SearchHistoryAdapter.MyViewHolder> {
	private List<String> list;
	private HotItemClickCallBack callBack;
	public SearchHistoryAdapter(List<String> list,final HotItemClickCallBack callBack) {
		this.list=list;
		this.callBack=callBack;
	}
	public class MyViewHolder extends ViewHolder{
		public TextView history_tv;
		public View iv_history_delete;
		public MyViewHolder(View itemView) {
			super(itemView);
			iv_history_delete=itemView.findViewById(R.id.iv_history_delete);
			history_tv=(TextView) itemView.findViewById(R.id.history_tv);
		}
	}
	@Override
	public int getItemCount() {
		if (list.size()>3) {
			return 3;
		}
		return list.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.history_tv.setText(list.get(position));
		holder.history_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callBack.onHotItemClick(holder.history_tv,position);
			}
		});
		holder.iv_history_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callBack.onHotItemClick(holder.iv_history_delete,position);	
			}
		});
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view=LayoutInflater.from(arg0.getContext()).inflate(R.layout.activity_history_item, null);
		MyViewHolder holder=new MyViewHolder(view);
		return holder;
	}
}
