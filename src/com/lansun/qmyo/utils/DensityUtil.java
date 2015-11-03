package com.lansun.qmyo.utils;




import com.lansun.qmyo.app.App;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DensityUtil {

	@SuppressWarnings("unused")
	private static final String TAG = DensityUtil.class.getSimpleName();

	public final static float DENSITY;
	public static float DIM_SCREEN_WIDTH;
	public static float DIM_SCREEN_HEIGHT;

	static {
		DisplayMetrics dms = new DisplayMetrics();
		WindowManager m = (WindowManager) App.getInstance()
				.getSystemService(Context.WINDOW_SERVICE);
		m.getDefaultDisplay().getMetrics(dms);

		DENSITY = dms.density;
		DIM_SCREEN_WIDTH = dms.widthPixels;
		DIM_SCREEN_HEIGHT = dms.heightPixels;
	}

	/**
	 * dip->Px
	 * 
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(float dipValue) {
		if (dipValue == 0)
			return (int) dipValue;
		return (int) (dipValue * DENSITY + 0.5f);

	}

	/**
	 * px->dip
	 * 
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(float pxValue) {
		return (int) (pxValue / DENSITY + 0.5f);
	}

	@Override
	public String toString() {
		return " DENSITY:" + DENSITY;
	}

	/**
	 * sp->px
	 * 
	 * @param context
	 * @param spValue
	 * @return
	 */
	public static int sp2px(float spValue) {
		if (spValue == 0)
			return (int) spValue;
		return (int) (spValue * DENSITY + 0.5f);
	}

	/**
	 * px->sp
	 * 
	 * @param context
	 * @param spValue
	 * @return
	 */
	public static int px2sp(float pxValue) {
		if (pxValue == 0)
			return (int) pxValue;
		return (int) (pxValue / DENSITY + 0.5f);
	}

}
