package com.lansun.qmyo.utils;

import com.android.pc.util.Handler_Bitmap;
import com.android.pc.util.Handler_File;

import android.annotation.SuppressLint;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class GridViewUtils {
	/**
	 * 计算GridView的高度
	 * 
	 * @param gridView
	 *            要计算的GridView
	 */
	@SuppressLint("NewApi")
	public static void updateGridViewLayoutParams(GridView gridView,
			int maxColumn, int horizontalSpacing) {
		int childs = gridView.getAdapter().getCount();

		if (childs > 0) {
			int columns = childs < maxColumn ? childs % maxColumn : maxColumn;
			gridView.setNumColumns(columns);
			int width = 0;
			int rowCounts = childs < maxColumn ? childs : maxColumn;
			for (int i = 0; i < rowCounts; i++) {
				View childView = gridView.getAdapter().getView(i, null,
						gridView);
				childView.measure(0, 0);
				width += childView.getMeasuredWidth();
			}
			ViewGroup.LayoutParams params = gridView.getLayoutParams();
			params.width = width;
			gridView.setLayoutParams(params);
		}
	}
}
