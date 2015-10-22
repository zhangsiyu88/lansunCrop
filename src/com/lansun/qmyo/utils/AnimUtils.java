package com.lansun.qmyo.utils;

import com.lansun.qmyo.R;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;

public class AnimUtils {

	public static void startAlpha(Context context, View view, long duration) {
		Animation animation = AnimationUtils.loadAnimation(context,
				R.anim.activity_close);
		animation.setDuration(duration);
		view.startAnimation(animation);
	}

	public static void endAlpha(Context context, View view) {
		Animation animation = AnimationUtils.loadAnimation(context,
				R.anim.activity_close);
		view.startAnimation(animation);
	}

	public static void startAlpha(Context context, View view) {
		Animation animation = AnimationUtils.loadAnimation(context,
				R.anim.activity_open);
		view.startAnimation(animation);
	}

	/**
	 * 右往左
	 * 
	 * @param context
	 * @param view
	 */
	public static void right2Left(Context context, final View view) {
		Animation gallery_in = AnimationUtils.loadAnimation(context,
				R.anim.gallery_in);
		view.setVisibility(View.INVISIBLE);
		gallery_in.setFillAfter(true);

		gallery_in.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.VISIBLE);
			}
		});
		view.startAnimation(gallery_in);
	}

	public static void startTopInAnim(Context activity, final View view,
			long duration, final EditText et) {
		Animation search_top_in = AnimationUtils.loadAnimation(activity,
				R.anim.home_top_in);
		search_top_in.setDuration(duration);
		search_top_in.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				view.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				if (et != null) {
					et.setFocusable(true);
					et.setFocusableInTouchMode(true);
					et.requestFocus();
				}
				view.setVisibility(View.VISIBLE);
			}
		});
		view.setAnimation(search_top_in);
		view.startAnimation(search_top_in);
	}

	public static void startTopInAnim(Context activity, final View view) {
		Animation search_top_in = AnimationUtils.loadAnimation(activity,
				R.anim.home_top_in);
		view.setVisibility(View.INVISIBLE);
		search_top_in.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				view.setVisibility(View.VISIBLE);
			}
		});
		view.setAnimation(search_top_in);
		view.startAnimation(search_top_in);
	}

	public static void startRotateIn(Context activity, final View view) {
		Animation animation = AnimationUtils.loadAnimation(activity,
				R.anim.rotate);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}

	public static void startRotateOut(Context activity, final View view) {
		Animation animation = AnimationUtils.loadAnimation(activity,
				R.anim.rotate_out);
		animation.setFillAfter(true);
		view.startAnimation(animation);
	}
}
