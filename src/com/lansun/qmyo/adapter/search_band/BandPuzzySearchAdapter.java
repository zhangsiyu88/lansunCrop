package com.lansun.qmyo.adapter.search_band;
import java.util.List;

import com.lansun.qmyo.R;
import com.lansun.qmyo.domain.band_puzzy.BandPuzzyData;
import com.lansun.qmyo.listener.PuzzyItemClickCallBack;


import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class BandPuzzySearchAdapter extends Adapter<BandPuzzySearchAdapter.MyViewHolder>{
	private List<String> list;
	private PuzzyItemClickCallBack callBack;
	public BandPuzzySearchAdapter(List<String> list,PuzzyItemClickCallBack callBack){
		this.list=list;
		this.callBack=callBack;
	}
	public class MyViewHolder extends ViewHolder{
		private TextView tv_show_band;
		public MyViewHolder(View itemView) {
			super(itemView);
			tv_show_band=(TextView)itemView.findViewById(R.id.tv_show);
		}
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.tv_show_band.setText(list.get(position).toString());
		holder.tv_show_band.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callBack.onPuzzyItemClick(holder.tv_show_band, position);
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
