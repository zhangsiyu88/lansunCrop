package com.lansun.qmyo.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.pc.ioc.image.RecyclingImageView;
import com.ns.mutiphotochoser.adapter.ImageGridAdapter.ViewHolder;
import com.lansun.qmyo.R;

/**
 * 菜单控件头部，封装了下拉动画，动态生成头部按钮个数
 * 
 */

public class ExpandTabView extends LinearLayout implements OnDismissListener {

	private TextView selectedButton;
	private ArrayList<RelativeLayout> mViewArray = new ArrayList<RelativeLayout>();
	private ArrayList<TextView> mToggleButton = new ArrayList<TextView>();
	private ArrayList<RecyclingImageView> ivs = new ArrayList<RecyclingImageView>();
	private Context mContext;
	private final int SMALL = 0;
	private int displayWidth;
	private int displayHeight;
	private PopupWindow popupWindow;
	private int selectPosition;

	public ExpandTabView(Context context) {
		super(context);
		init(context);
	}

	public ExpandTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 根据选择的位置设置tabitem显示的值
	 */
	public void setTitle(String valueText, int position) {
		if (position < mToggleButton.size()) {
			mToggleButton.get(position).setText(valueText);
		}
	}

	public void setTitle(String title) {
	}

	/**
	 * 根据选择的位置获取tabitem显示的值
	 */
	public String getTitle(int position) {
		if (position < mToggleButton.size()
				&& mToggleButton.get(position).getText() != null) {
			return mToggleButton.get(position).getText().toString();
		}
		return "";
	}

	/**
	 * 设置tabitem的个数和初始值
	 */
	public void setValue(HashMap<Integer, String> textArray,
			HashMap<Integer, View> viewArray) {
		mToggleButton.clear();
		if (mContext == null) {
			return;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (int i = 0; i < textArray.size(); i++) {
			final RelativeLayout r = new RelativeLayout(mContext);
			r.removeAllViews();
			int maxHeight = (int) (displayHeight);
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, maxHeight);
			r.addView(viewArray.get(i), rl);
			mViewArray.add(r);
			r.setTag(SMALL);
			RelativeLayout mRl = (RelativeLayout) inflater.inflate(
					R.layout.toggle_button, this, false);
			final TextView tv_expand_name = (TextView) mRl
					.findViewById(R.id.tv_expand_name);
			final RecyclingImageView iv_expand_more = (RecyclingImageView) mRl
					.findViewById(R.id.iv_expand_more);
			addView(mRl);
			mToggleButton.add(tv_expand_name);
			ivs.add(iv_expand_more);
			tv_expand_name.setTag(i);
			tv_expand_name.setText(textArray.get(i).toString());

			r.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onPressBack();
				}
			});
			// r.setBackgroundColor(mContext.getResources().getColor(R.color.popup_main_background));
			mRl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (popupWindow != null && popupWindow.isShowing()) {
						for (int i = 0; i < mToggleButton.size(); i++) {
							mToggleButton.get(i).setTextColor(
									mContext.getResources().getColor(
											R.color.text_gray1));
							ivs.get(i).setImageResource(R.drawable.arrow_down);
						}
					}
					// initPopupWindow();
					if (isShow) {
						tv_expand_name.setTextColor(mContext.getResources()
								.getColor(R.color.text_gray1));
						view.setTag("true");
						iv_expand_more.setImageResource(R.drawable.arrow_down);
						isShow = false;
					} else {
						tv_expand_name.setTextColor(mContext.getResources()
								.getColor(R.color.app_green1));
						iv_expand_more.setImageResource(R.drawable.arrow_up);
						isShow = true;
					}
					startAnimation();
					selectedButton = tv_expand_name;
					selectPosition = (Integer) selectedButton.getTag();
					startAnimation();
					if (mOnButtonClickListener != null && isShow) {
						mOnButtonClickListener.onClick(selectPosition);
					}
				}
			});
		}
	}

	private boolean isShow = false;

	private void startAnimation() {

		if (popupWindow == null) {
			popupWindow = new PopupWindow(mViewArray.get(selectPosition),
					displayWidth, displayHeight, false);
			popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
			popupWindow.setFocusable(false);
			popupWindow.setOutsideTouchable(true);
		}
		if (isShow) {
			if (!popupWindow.isShowing()) {
				showPopup(selectPosition);
			} else {
				popupWindow.setOnDismissListener(this);
				popupWindow.dismiss();
				hideView();
			}
		} else {
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
				hideView();
			}
		}
	}

	private void showPopup(int position) {
		View tView = mViewArray.get(selectPosition).getChildAt(0);
		if (tView instanceof ViewBaseAction) {
			ViewBaseAction f = (ViewBaseAction) tView;
			f.show();
		}
		if (popupWindow.getContentView() != mViewArray.get(position)) {
			popupWindow.setContentView(mViewArray.get(position));
		}
		popupWindow.showAsDropDown(this, 0, 0);
	}

	public boolean onPressBack() {
		if (popupWindow != null && popupWindow.isShowing()) {
			for (int i = 0; i < mToggleButton.size(); i++) {
				mToggleButton.get(i).setTextColor(
						mContext.getResources().getColor(R.color.text_gray1));
				ivs.get(i).setImageResource(R.drawable.arrow_down);
			}
			popupWindow.dismiss();
			hideView();
			if (selectedButton != null) {
				isShow = false;
			}
			return true;
		} else {
			return false;
		}

	}

	private void hideView() {
		View tView = mViewArray.get(selectPosition).getChildAt(0);
		if (tView instanceof ViewBaseAction) {
			ViewBaseAction f = (ViewBaseAction) tView;
			f.hide();
		}
	}

	@SuppressLint("NewApi")
	private void init(Context context) {
		mContext = context;
		mToggleButton.clear();
		displayWidth = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getWidth();
		displayHeight = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getHeight();
		setOrientation(LinearLayout.HORIZONTAL);
		setBackground(null);
		setDividerDrawable(null);
	}

	@Override
	public void onDismiss() {
		if (popupWindow != null && popupWindow.isShowing()) {
			for (int i = 0; i < mToggleButton.size(); i++) {
				mToggleButton.get(i).setTextColor(
						mContext.getResources().getColor(R.color.text_gray1));
				ivs.get(i).setImageResource(R.drawable.arrow_down);
			}
		}
		showPopup(selectPosition);
		popupWindow.setOnDismissListener(null);
	}

	private OnButtonClickListener mOnButtonClickListener;

	/**
	 * 设置tabitem的点击监听事件
	 */
	public void setOnButtonClickListener(OnButtonClickListener l) {
		mOnButtonClickListener = l;
	}

	/**
	 * 自定义tabitem点击回调接口
	 */
	public interface OnButtonClickListener {
		public void onClick(int selectPosition);
	}

}
