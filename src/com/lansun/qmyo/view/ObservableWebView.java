package com.lansun.qmyo.view;

import java.util.HashMap;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.mapcore2d.ev;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.Secret;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.lansun.qmyo.R;

/**
 * 体验dialog
 * 
 * @author bhxx
 * 
 */
import android.content.Context;

import android.util.AttributeSet;

import android.webkit.WebView;

/**
 * Created by jianghejie on 15/7/16.
 */
public class ObservableWebView extends WebView {
	private OnScrollChangedCallback mOnScrollChangedCallback;

	public ObservableWebView(final Context context) {
		super(context);
	}

	public ObservableWebView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ObservableWebView(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(final int l, final int t, final int oldl,
			final int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (mOnScrollChangedCallback != null) {
			mOnScrollChangedCallback.onScroll(l - oldl, t - oldt);
		}
	}

	public OnScrollChangedCallback getOnScrollChangedCallback() {
		return mOnScrollChangedCallback;
	}

	public void setOnScrollChangedCallback(
			final OnScrollChangedCallback onScrollChangedCallback) {
		mOnScrollChangedCallback = onScrollChangedCallback;
	}

	/**
	 * Impliment in the activity/fragment/view that you want to listen to the
	 * webview
	 */
	public static interface OnScrollChangedCallback {
		public void onScroll(int dx, int dy);
	}
}