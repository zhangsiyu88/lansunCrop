package com.lansun.qmyo.view;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lansun.qmyo.adapter.TextAdapter;
import com.lansun.qmyo.R;

public class ViewLeft extends LinearLayout implements ViewBaseAction {

	private ListView regionListView;
	private ListView plateListView;
	private ArrayList<String> groups = new ArrayList<String>();
	private LinkedList<String> childrenItem = new LinkedList<String>();
	private SparseArray<LinkedList<String>> children = new SparseArray<LinkedList<String>>();
	private TextAdapter plateListViewAdapter;
	private TextAdapter earaListViewAdapter;
	private OnSelectListener mOnSelectListener;
	private int tEaraPosition = 0;
	private int tBlockPosition = 0;
	private String showString = "不限";
	private Context ctx;

	public ViewLeft(Context context) {
		super(context);
		this.ctx = context;
	}

	public ArrayList<String> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<String> groups) {//Group是个ArrayList
		this.groups = groups;
	}

	public LinkedList<String> getChildrenItem() {
		return childrenItem;
	}

	public void setChildrenItem(LinkedList<String> childrenItem) {
		this.childrenItem = childrenItem;
	}

	public ViewLeft(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 根据参数showArea 和 showBlock 跟显示的数据内容
	 * @param showArea
	 * @param showBlock
	 */
	public void updateShowText(String showArea, String showBlock) {
		if (showArea == null || showBlock == null) {
			return;
		}
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).equals(showArea)) {
				//如果showArea的值和ArrayList中的某个值相等时，将earaListViewAdapter的选中位置设置在第i个位置
				earaListViewAdapter.setSelectedPosition(i);
				
				
				childrenItem.clear();//立即将这个链表清掉，准备把位置腾出来给children链表的第i个位置的LinkedList
				//children为SparseArray(LinkedList<String>)
				if (i < children.size()) {//children为SparseArray(LinkedList<String>)
					childrenItem.addAll(children.get(i));
				}
				tEaraPosition = i;//将地域的位置现在修改为之前选中的第i个位置
				break;
			}
		}
		for (int j = 0; j < childrenItem.size(); j++) {
			if (childrenItem.get(j).replace("不限", "").equals(showBlock.trim())) {
				plateListViewAdapter.setSelectedPosition(j);//商圈的适配器设置到选中的位置
				tBlockPosition = j;
				break;
			}
		}
		setDefaultSelect();
	}

	public void setChildren(SparseArray<LinkedList<String>> children) {
		this.children = children;

		init(this.ctx);
	}

	private int parentId = 0;

	private void init(Context context) {
		// LayoutInflater inflater = (LayoutInflater) context
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.view_region, this, true);
		regionListView = (ListView) findViewById(R.id.listView);
		plateListView = (ListView) findViewById(R.id.listView2);
		earaListViewAdapter = new TextAdapter(context, groups,
				R.drawable.choose_item_selected, 0);
		earaListViewAdapter.setSelectedPositionNoNotify(tEaraPosition);
		regionListView.setAdapter(earaListViewAdapter);

		regionListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				System.out.println(arg1);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		earaListViewAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

					@Override
					public void onItemClick(View view, int position) {
						//
						// mOnSelectListener.getValue(showString, parentId,
						// parentId);
						if (position < children.size()) {
							parentId = position;
							childrenItem.clear();
							childrenItem.addAll(children.get(position));
							if (children.get(position).size() == 0) {
								mOnSelectListener.getValue(
										groups.get(position), parentId,
										position);
							}
							plateListViewAdapter.notifyDataSetChanged();
						}
					}
				});
		if (tEaraPosition < children.size())
			childrenItem.addAll(children.get(tEaraPosition));
		plateListViewAdapter = new TextAdapter(context, childrenItem, 0, 1);
		plateListViewAdapter.setSelectedPositionNoNotify(tBlockPosition);
		plateListView.setAdapter(plateListViewAdapter);
		plateListViewAdapter
				.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

					@Override
					public void onItemClick(View view, final int position) {
						showString = childrenItem.get(position);
						if (mOnSelectListener != null) {
							mOnSelectListener.getValue(showString, parentId,
									position);
						}
						plateListViewAdapter.notifyDataSetChanged();

					}
				});
		if (tBlockPosition < childrenItem.size())
			showString = childrenItem.get(tBlockPosition);
		if (showString.contains("不限")) {
			showString = showString.replace("不限", "");
		}
		setDefaultSelect();

	}

	/*
	 * 设置默认的选中状态栏
	 */
	public void setDefaultSelect() {
		regionListView.setSelection(tEaraPosition);
		plateListView.setSelection(tBlockPosition);
	}

	public String getShowText() {
		return showString;
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		mOnSelectListener = onSelectListener;
	}

	public interface OnSelectListener {
		public void getValue(String showText, int parendId, int position);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
