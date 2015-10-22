package com.lansun.qmyo.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.adapter.CommonPagerAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.R;

/**
 * 欢迎首页
 * 
 * @author bhxx
 * 
 */
public class IntroductionPageFragment extends BaseFragment implements
		OnPageChangeListener {

	@InjectAll
	Views v;
	private ArrayList<ImageView> imageViewList;

	class Views {
		private ViewPager viewpager;
		private LinearLayout ll_points;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View tv_introduction_jump, ll_exp_now;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		App.app.setData("isFirst", "true");
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_introduction, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	private void click(View view) {
		HomeFragment fragment = new HomeFragment();
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	ViewPagerAdapter adapter = new ViewPagerAdapter();

	@InjectInit
	private void init() {
		prepareData();
		v.viewpager.setAdapter(adapter);
		
		//v.ll_points.getChildAt(0).setEnabled(true);
		v.ll_points.getChildAt(0).setBackgroundResource(R.drawable.oval_select); 
		v.viewpager.setOnPageChangeListener(this);
		v.viewpager.setCurrentItem(0);
	}

	/**
	 * 准备数据
	 */
	private void prepareData() {
		imageViewList = new ArrayList<ImageView>();
		int[] imageResIDs = getImageResIDs();

		ImageView iv;
		View view;
		
		// 添加点view对象
		for (int i = 0; i < imageResIDs.length; i++) {
			iv = new ImageView(activity);
			/*iv.setBackgroundResource(imageResIDs[i]);*/
			iv.setImageResource(imageResIDs[i]);
			iv.setScaleType(ScaleType.CENTER_INSIDE);
			imageViewList.add(iv);
			view = new View(activity);
			view.setBackgroundDrawable(getResources().getDrawable(R.drawable.oval_nomal));
			LayoutParams lp = new LayoutParams(8, 8);
			lp.leftMargin = 8;
			view.setLayoutParams(lp);
			view.setEnabled(false);
			v.ll_points.addView(view);
		}
		
	}

	private int[] getImageResIDs() {
		return new int[] { R.drawable.jieshao_shopping,
				R.drawable.jieshao_holiday, R.drawable.jieshao_more,R.drawable.jieshao_tiyan};
	}

	class ViewPagerAdapter extends CommonPagerAdapter {

		/**
		 * 创建一个view
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(imageViewList.get(position));
			return imageViewList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(imageViewList.get(position));
		}

		@Override
		public int getCount() {
			return imageViewList.size();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		if (position == imageViewList.size() - 1) {
			v.ll_exp_now.setVisibility(View.VISIBLE);
		} else {
			v.ll_exp_now.setVisibility(View.GONE);
		}
		// 切换选中的点
		for (int i = 0; i < v.ll_points.getChildCount(); i++) {
			//进来就首先将所有的点的背景都置为 app背景色
			v.ll_points.getChildAt(i).setBackgroundResource(
					R.drawable.oval_nomal);
			
		}
		//然后再将当前的位置所在的点置为选中颜色
		v.ll_points.getChildAt(position).setBackgroundResource(
				R.drawable.oval_select); 
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}