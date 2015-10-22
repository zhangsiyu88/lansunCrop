package com.lansun.qmyo.view;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lansun.qmyo.adapter.TextAdapter;
import com.lansun.qmyo.R;

public class ViewRight extends RelativeLayout implements ViewBaseAction {

	private ListView mListView;
	private ArrayList<String> items = new ArrayList<String>();// 显示字段
	private String[] itemsVaule;
	private OnSelectListener mOnSelectListener;
	private TextAdapter adapter;
	private String mDistance;
	private String showText = "item1";
	private Context mContext;
	private ArrayList<String> icons;

	public void setItems(ArrayList<String> items) {
		this.items = items;
		init(mContext);
	}

	public String getShowText() {
		return showText;
	}

	public ViewRight(Context context) {
		super(context);
		this.mContext = context;
	}

	public ViewRight(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	public ViewRight(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		itemsVaule = new String[items.size()];
		for (int i = 0; i < items.size(); i++) {
			itemsVaule[i] = i + "";
		}
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_distance, this, true);
		mListView = (ListView) findViewById(R.id.listView);
		adapter = new TextAdapter(context, items, 0, 1);
		adapter.setHasIcon(true);
		adapter.setIcons(icons);
		if (mDistance != null) {
			for (int i = 0; i < itemsVaule.length; i++) {
				if (itemsVaule[i].equals(mDistance)) {
					adapter.setSelectedPositionNoNotify(i);
					showText = items.get(i);
					break;
				}
			}
		}
		mListView.setAdapter(adapter);
		adapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

			@Override
			public void onItemClick(View view, int position) {

				if (mOnSelectListener != null) {
					showText = items.get(position);
					mOnSelectListener.getValue(itemsVaule[position],
							items.get(position),position);
				}
			}
		});
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		mOnSelectListener = onSelectListener;
	}

	public interface OnSelectListener {
		public void getValue(String distance, String showText,int position);
	}

	@Override
	public void hide() {

	}

	@Override
	public void show() {

	}

	public void setICons(ArrayList<String> iconGroup) {
		this.icons = iconGroup;
	}

}
