package com.lansun.qmyo.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import com.lansun.qmyo.utils.LogUtils;

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
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (int i = 0; i < textArray.size(); i++) {          //设置textArray.size()个RelativeLayout
			final RelativeLayout r = new RelativeLayout(mContext);
			r.removeAllViews();
			
			int maxHeight = (int) (displayHeight);
			System.out.println("displayHeight的值为： "+ displayHeight);
		    /*int maxHeight = 50;*/                         												   //  --->by Yeun 11.13
			
			RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, maxHeight);
			
			if(viewArray.get(i)!=null){
				r.addView(viewArray.get(i), rl);//r 布局添加入viewArray中的第i个View，并且是rl的布局设置
			}
			
			mViewArray.add(r);//相对布局控件的列表，布局的源，供后面的
			r.setTag(SMALL);
			
			
			//下面的这个是 ExpandTabView的四个或三个点击钮
			RelativeLayout mRl = (RelativeLayout) inflater.inflate(
					R.layout.toggle_button, this, false);
			final TextView tv_expand_name = (TextView) mRl.findViewById(R.id.tv_expand_name);
			final RecyclingImageView iv_expand_more = (RecyclingImageView) mRl.findViewById(R.id.iv_expand_more);
			
			addView(mRl);
			mToggleButton.add(tv_expand_name);//TextView控件的列表
			ivs.add(iv_expand_more);//标签图标RecycleImageView控件对象的列表
			tv_expand_name.setTag(i);
			tv_expand_name.setText(textArray.get(i).toString());

			
			//r对应的就是筛选栏中的三（或 四）个按钮
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
					
					if (popupWindow!= null) {
					LogUtils.toDebugLog("popupWindow", popupWindow.isShowing() == true?"isShowing 为true":"isShowing 不为true");
					LogUtils.toDebugLog("popupWindow", popupWindow == null?"popupWindow 为空":"popupWindow 不为空");
					}else{
					LogUtils.toDebugLog("popupWindow", popupWindow == null?"popupWindow 为空":"popupWindow 不为空");
					}
					
					if (popupWindow != null && popupWindow.isShowing()) {      //展开状态，当点击后，将箭头置为 朝下
						for (int i = 0; i < mToggleButton.size(); i++) {
							mToggleButton.get(i).setTextColor(mContext.getResources().getColor(R.color.text_gray1));
							ivs.get(i).setImageResource(R.drawable.arrow_down);
						}
					}
					// initPopupWindow();
					if (isShow) {												//同上；  展开状态，当点击后，将箭头置为 朝下
						tv_expand_name.setTextColor(mContext.getResources()
								.getColor(R.color.text_gray1));
						view.setTag("true");
						iv_expand_more.setImageResource(R.drawable.arrow_down);
						
						/*
						 * 由于这些监听器实际上都是在公用一些指示变量，须分清对应的监听判断标准
						   场景： 虽然popoupWindow已经展开，如果按照原先展开前的Button点回去，不会有任何影响，
						     但此时，更换下次点击的Button对象，那么走的是另一个监听器，此时，popoupWindow展开，点击按钮时，
						  不应该仅仅将popoupWindow收回，还需将整个expandTabView上的字体置回灰色，箭头向下
						 */
							for (int i = 0; i < mToggleButton.size(); i++) {
								mToggleButton.get(i).setTextColor(mContext.getResources().getColor(R.color.text_gray1));
								ivs.get(i).setImageResource(R.drawable.arrow_down);
							}
						isShow = false;
					} else {                                                   //非展示状态，点击后需要做展开操作
						tv_expand_name.setTextColor(mContext.getResources()
								.getColor(R.color.app_green1));
						iv_expand_more.setImageResource(R.drawable.arrow_up);
						isShow = true;
					}
					startAnimation();
					selectedButton = tv_expand_name;
					selectPosition = (Integer) selectedButton.getTag();
					startAnimation();
					if (mOnButtonClickListener != null && isShow) {               //展开状态时，且监听器监听
						mOnButtonClickListener.onClick(selectPosition);
					}
				}
			});
		}
	}

	private boolean isShow = false;

	
	/**
	 * 设置添加屏幕的背景透明度
	 * @param bgAlpha
	 */
/*	public void backgroundAlpha(float bgAlpha)
	{
		WindowManager.LayoutParams lp = getWindow().getAttributes();
        　　　　lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
	}*/
	
	
	private void startAnimation() {

		if (popupWindow == null) {
			//这里的PopupView是基于mViewArray.get(selectPosition)的RelativeLayout获取的
			popupWindow = new PopupWindow(mViewArray.get(selectPosition),displayWidth, displayHeight, false);
			/*popupWindow = new PopupWindow(mViewArray.get(selectPosition),displayWidth, 50, false);  */                      // --->by Yeun 11.13          
			
			
			
			ColorDrawable dw = new ColorDrawable(Color.argb(00, 255, 255, 255)); 										//--->by Yeun 11.13
			popupWindow.setBackgroundDrawable(dw); 
			//popupWindow.update();
			
			/*popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);*/
			popupWindow.setAnimationStyle(R.style.popwindow_anim_style);//更换一个新的popupWindow的动画展示方式                                                            
			popupWindow.setFocusable(false);
			popupWindow.setOutsideTouchable(true);
		}
		
		
		if (isShow) {//展示状态
			if (!popupWindow.isShowing()) {  //但popupWidow非展开,须立即将这个popoupWindow立即展开显示
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
				mToggleButton.get(i).setTextColor(mContext.getResources().getColor(R.color.text_gray1));
				ivs.get(i).setImageResource(R.drawable.arrow_down);
			}
			popupWindow.dismiss();
			
			hideView();//
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
		//拿到屏幕的宽和高
		displayWidth = ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth();
		displayHeight = ((Activity) mContext).getWindowManager().getDefaultDisplay().getHeight();
		
		setOrientation(LinearLayout.HORIZONTAL);
		setBackground(null);//这个地方设置的是ExpandTabView的整体横栏的背景色
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
