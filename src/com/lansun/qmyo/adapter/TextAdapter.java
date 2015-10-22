package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.provider.CalendarContract.Colors;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lansun.qmyo.R;

public class TextAdapter extends ArrayAdapter<String> {

	private Context mContext;
	private List<String> mListData;
	private String[] mArrayData;
	private int selectedPos = -1;
	private String selectedText = "";
	private int normalDrawbleId;
	private int selectedDrawble;
	private float textSize;
	private OnClickListener onClickListener;
	private OnItemClickListener mOnItemClickListener;

	private boolean hasIcon = false;
	private ArrayList<String> icons;

	public TextAdapter(Context context, List<String> listData, int sId, int nId) {
		super(context, R.string.no_data, listData);
		mContext = context;
		mListData = listData;
		selectedDrawble = sId;
		normalDrawbleId = nId;
		init();

	}

	public void setHasIcon(boolean hasIcon) {
		this.hasIcon = hasIcon;
	}

	private void init() {
		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				selectedPos = ((Holder) view.getTag()).position;
				setSelectedPosition(selectedPos);
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick(view, selectedPos);
				}
			}
		};
	}

	public TextAdapter(Context context, String[] arrayData, int sId, int nId) {
		super(context, R.string.no_data, arrayData);
		mContext = context;
		mArrayData = arrayData;
		selectedDrawble = sId;
		normalDrawbleId = nId;
		init();
	}

	/**
	 * 设置选中的position,并通知列表刷新
	 */
	public void setSelectedPosition(int pos) {
		if (mListData != null && pos < mListData.size()) {
			selectedPos = pos;
			selectedText = mListData.get(pos);
			notifyDataSetChanged();
		} else if (mArrayData != null && pos < mArrayData.length) {
			selectedPos = pos;
			selectedText = mArrayData[pos];
			notifyDataSetChanged();
		}

	}

	/**
	 * 设置选中的position,但不通知刷新
	 */
	public void setSelectedPositionNoNotify(int pos) {
		selectedPos = pos;
		if (mListData != null && pos < mListData.size()) {
			selectedText = mListData.get(pos);
		} else if (mArrayData != null && pos < mArrayData.length) {
			selectedText = mArrayData[pos];
		}
	}

	/**
	 * 获取选中的position
	 */
	public int getSelectedPosition() {
		if (mArrayData != null && selectedPos < mArrayData.length) {
			return selectedPos;
		}
		if (mListData != null && selectedPos < mListData.size()) {
			return selectedPos;
		}

		return -1;
	}

	/**
	 * 设置列表字体大小
	 */
	public void setTextSize(float tSize) {
		textSize = tSize;
	}

	HashMap<Integer, View> lmap = new HashMap<Integer, View>();

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (lmap.get(position) == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.choose_item, parent, false);
			holder.tv_choose_item = (TextView) convertView
					.findViewById(R.id.tv_choose_item);
			holder.iv_choose_item = (ImageView) convertView
					.findViewById(R.id.iv_choose_item);
			holder.iv_choose_divider = (ImageView) convertView
					.findViewById(R.id.iv_choose_divider);
			convertView.setTag(holder);
		} else {
			convertView = lmap.get(position);
			holder = (Holder) convertView.getTag();
		}
		holder = (Holder) convertView.getTag();
		holder.position = position;
		convertView.setTag(holder);
		String mString = "";
		if (mListData != null) {
			if (position < mListData.size()) {
				mString = mListData.get(position);
			}
		} else if (mArrayData != null) {
			if (position < mArrayData.length) {
				mString = mArrayData[position];
			}
		}

		if (hasIcon) {
			holder.iv_choose_item.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 1);
			layoutParams.leftMargin = (int) mContext.getResources()
					.getDimension(R.dimen.l_r_35);
			holder.iv_choose_divider.setLayoutParams(layoutParams);
			if (icons != null) {
				String icon = icons.get(position);
				if ("discount".equals(icon.trim())) {
					holder.iv_choose_item.setImageResource(R.drawable.discount);
				} else if ("new".equals(icon.trim())) {
					holder.iv_choose_item.setImageResource(R.drawable.new1);
				} else if ("point".equals(icon.trim())) {
					holder.iv_choose_item.setImageResource(R.drawable.point);
				} else if ("staging".equals(icon.trim())) {
					holder.iv_choose_item.setImageResource(R.drawable.staging);
				} else if ("coupon".equals(icon.trim())) {
					holder.iv_choose_item.setImageResource(R.drawable.coupon);
				} else {
					holder.iv_choose_item.setImageResource(R.drawable.all);
				}
			}

		}

		// if (mString.contains("不限"))
		// holder.tv_choose_item.setText("不限");
		// else
		holder.tv_choose_item.setText(mString);

		if (selectedText != null && selectedText.equals(mString)) {
			if (selectedDrawble != 0) {
				holder.tv_choose_item.setBackgroundDrawable(mContext
						.getResources().getDrawable(selectedDrawble));// 设置选中的背景图片
			} else {
				// 没有设置背景色就把文字改为绿色
				holder.tv_choose_item.setTextColor(mContext.getResources()
						.getColor(R.color.app_green1));
			}
		} else {
			if (normalDrawbleId == 0) {
				holder.tv_choose_item.setBackgroundColor(mContext
						.getResources().getColor(R.color.line_bg2));
				// 文字绿色？
			} else if (normalDrawbleId == 1) {
				holder.tv_choose_item.setBackgroundColor(mContext
						.getResources().getColor(R.color.app_white));
			}

			else {
				holder.tv_choose_item.setBackgroundDrawable(mContext
						.getResources().getDrawable(normalDrawbleId));// 设置未选中状态背景图片
			}
		}
		holder.tv_choose_item.setPadding(20, 0, 0, 0);
		convertView.setOnClickListener(onClickListener);
		return convertView;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mOnItemClickListener = l;
	}

	class Holder {
		TextView tv_choose_item;
		ImageView iv_choose_item;
		ImageView iv_choose_divider;
		int position;
	}

	/**
	 * 重新定义菜单选项单击接口
	 */
	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public void setIcons(ArrayList<String> icons) {
		this.icons = icons;
	}

}
