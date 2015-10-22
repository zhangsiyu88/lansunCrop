package com.lansun.qmyo.adapter.serch_hot;

import java.util.List;

import com.lansun.qmyo.listener.HotItemClickCallBack;
import com.lansun.qmyo.R;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchHotAdapter extends RecyclerView.Adapter<SearchHotAdapter.MyViewHolder>{
	private List<String> list;
	private HotItemClickCallBack callBack;
	public SearchHotAdapter(List<String> list,final HotItemClickCallBack callBack) {
		this.list=list;
		this.callBack=callBack;
	}
	public class MyViewHolder extends ViewHolder{
		public TextView item_tv;
		public MyViewHolder(View itemView) {
			super(itemView);
			item_tv=(TextView) itemView.findViewById(R.id.tv_search_hot_ad);
		}
	}
	@Override
	public int getItemCount() {
		return list.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.item_tv.setText(list.get(position));
		holder.item_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callBack.onHotItemClick(holder.item_tv,position);	
			}
		});
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view=LayoutInflater.from(arg0.getContext()).inflate(R.layout.activity_search_hot_item, null);
		MyViewHolder holder=new MyViewHolder(view);
		return holder;
	}
}
