package com.lansun.qmyo.fragment;

import java.math.BigDecimal;

import android.R.anim;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.lansun.qmyo.R;

/**
 * 设置界面
 * 
 * @author bhxx
 * 
 */
public class SettingFragment extends BaseFragment {
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_setting_clear_cache, iv_setting_feedback,
				iv_setting_help_center;
		private TextView tv_setting_cache_size;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_setting, null);
		Handler_Inject.injectOrther(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		initCacheSize();
	}

	private void initCacheSize() {
		long length = ImageLoader.getInstance().getDiskCache().getDirectory()
				.length();
		float mbSize = (float) length / 1024 / 1024;
		BigDecimal b = new BigDecimal(mbSize);
		float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		v.tv_setting_cache_size.setText(String.format(
				getString(R.string.cache_size), f1));
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.iv_setting_clear_cache:
			Builder dialog = new AlertDialog.Builder(activity);
			dialog.setIcon(R.drawable.icon);
			dialog.setTitle(R.string.tip);
			dialog.setMessage(R.string.remove_cache_tip);
			dialog.setPositiveButton(getString(android.R.string.ok),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ImageLoader.getInstance().getDiskCache()
									.getDirectory().delete();
							ImageLoader.getInstance().getDiskCache().clear();
							initCacheSize();
						}
					});
			dialog.setNegativeButton(getString(android.R.string.cancel), null);
			dialog.create().show();
			break;
		case R.id.iv_setting_feedback:
			FeedBackListFragment fragment = new FeedBackListFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);

			break;
		case R.id.iv_setting_help_center:// TODO 帮助中心

			break;

		}
	}
}
