package com.lansun.qmyo.adapter.serch_hot;
import java.util.List;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lansun.qmyo.domain.PuzzySearch;
import com.lansun.qmyo.listener.HotItemClickCallBack;
import com.lansun.qmyo.listener.PuzzyItemClickCallBack;
import com.lansun.qmyo.R;

public class PuzzyListAdapter extends Adapter<PuzzyListAdapter.MyViewHolder>{
	private List<PuzzySearch> list;
	private PuzzyItemClickCallBack callBack;
	public PuzzyListAdapter(List<PuzzySearch> list,final PuzzyItemClickCallBack callBack) {
		this.list=list;
		this.callBack=callBack;
	}
	public class MyViewHolder extends ViewHolder{
		public TextView tv_search_hot_ad;
		public MyViewHolder(View itemView) {
			super(itemView);
			tv_search_hot_ad=(TextView)itemView.findViewById(R.id.tv_show);
		}
	}
	@Override
	public int getItemCount() {
		return list.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.tv_search_hot_ad.setText(list.get(position).getPuzzytitle());
		holder.tv_search_hot_ad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callBack.onPuzzyItemClick(holder.tv_search_hot_ad,position);
			}
		});
	}
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view=LayoutInflater.from(arg0.getContext()).inflate(R.layout.pop_recyc_item, arg0,false);
		MyViewHolder holder=new MyViewHolder(view);
		return holder;
	}
}
