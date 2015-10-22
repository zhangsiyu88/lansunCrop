package com.lansun.qmyo.fragment;

import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.fragment.FoundFragment.Views;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController.AnimationParameters;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

/**
 * 榜单
 * 
 * @author bhxx
 * 
 */
public class BDListFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		RecyclingImageView iv_bd_lr, iv_bd_boom;
	}

	private Matrix mMatrix;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_bd, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {

		mMatrix = new Matrix();
		mMatrix.postScale(1, 1, 1, 1);
		
		v.iv_bd_lr.setImageMatrix(mMatrix);  
		v.iv_bd_lr.invalidate();

		v.iv_bd_lr.startAnimation(getAnimation());//对打拳击的那张图来回移动
		
		((AnimationDrawable) v.iv_bd_boom.getDrawable()).start();//是个帧动画
	}

	private Animation getAnimation() {
		MyTranslateAnimation animation = new MyTranslateAnimation(
				getResources().getDimension(R.dimen.bd_l_r_45), -getResources()
						.getDimension(R.dimen.bd_l_r_45), 0f, 0f);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setDuration(2000);
		animation.setInterpolator(new LinearInterpolator());
		return animation;
	}

	class MyTranslateAnimation extends Animation {//继承自Animation
		private int mFromXType = ABSOLUTE;
		private int mToXType = ABSOLUTE;

		private int mFromYType = ABSOLUTE;
		private int mToYType = ABSOLUTE;

		private float mFromXValue = 0.0f;
		private float mToXValue = 0.0f;

		private float mFromYValue = 0.0f;
		private float mToYValue = 0.0f;

		private float mFromXDelta;
		private float mToXDelta;
		private float mFromYDelta;
		private float mToYDelta;

		public MyTranslateAnimation(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public MyTranslateAnimation(float fromXDelta, float toXDelta,
				float fromYDelta, float toYDelta) {
			mFromXValue = fromXDelta;
			mToXValue = toXDelta;
			mFromYValue = fromYDelta;
			mToYValue = toYDelta;

			mFromXType = ABSOLUTE;
			mToXType = ABSOLUTE;
			mFromYType = ABSOLUTE;
			mToYType = ABSOLUTE;
		}

		public MyTranslateAnimation(int fromXType, float fromXValue,
				int toXType, float toXValue, int fromYType, float fromYValue,
				int toYType, float toYValue) {
			mFromXValue = fromXValue;
			mToXValue = toXValue;
			mFromYValue = fromYValue;
			mToYValue = toYValue;

			mFromXType = fromXType;
			mToXType = toXType;
			mFromYType = fromYType;
			mToYType = toYType;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			float dx = mFromXDelta;
			float dy = mFromYDelta;
			if (mFromXDelta != mToXDelta) {
				dx = mFromXDelta
						+ ((mToXDelta - mFromXDelta) * interpolatedTime);
			}
			if (mFromYDelta != mToYDelta) {
				dy = mFromYDelta
						+ ((mToYDelta - mFromYDelta) * interpolatedTime);
			}
			mMatrix.setTranslate(dx, dy);
			v.iv_bd_lr.setImageMatrix(mMatrix);
			v.iv_bd_lr.invalidate();
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			mFromXDelta = resolveSize(mFromXType, mFromXValue, width,
					parentWidth);
			mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth);
			mFromYDelta = resolveSize(mFromYType, mFromYValue, height,
					parentHeight);
			mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight);
		}
	}

}
