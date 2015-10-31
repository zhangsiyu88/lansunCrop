package com.lansun.qmyo.fragment;

import java.math.BigDecimal;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Html;
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
import com.lansun.qmyo.view.TelDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.lansun.qmyo.R;

/**
 * 关于迈界
 * 
 * @author bhxx
 * 
 */
public class AboutFragment extends BaseFragment {
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_about_qmyo_net, tv_setting_cache_size;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View rl_mine_common_problem, rl_mine_feedback,
				rl_mine_clear_cache, rl_mine_user_agreement;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_about, null);
		Handler_Inject.injectOrther(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.tv_about_qmyo_net
				.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		initCacheSize();
		init2();
		
	}
	
	
	private void init2() {
		v.tv_about_qmyo_net
				.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		initCacheSize();
		v.tv_about_qmyo_net
		.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		initCacheSize();
		v.tv_about_qmyo_net
		.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		initCacheSize();
		v.tv_about_qmyo_net
		.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		initCacheSize();
	}
	
	

	private void click(View view) {
		BaseFragment fragment = null;
		switch (view.getId()) {
		case R.id.rl_mine_common_problem:// 常见问题 --〉帮助中心
			fragment = new HelpFragment();
			break;
		case R.id.rl_mine_feedback:// 意见反馈
			fragment = new FeedBackListFragment();
			break;
		case R.id.rl_mine_clear_cache:// 清空缓存
			Builder dialog = new AlertDialog.Builder(activity);
			dialog.setIcon(R.drawable.icon);
			dialog.setTitle(R.string.tip);
			dialog.setMessage(R.string.remove_cache_tip);
			dialog.setPositiveButton(getString(android.R.string.ok),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
							//实质上清除图片的缓存内容来达到节省空间的作用
							ImageLoader.getInstance().getDiskCache()
									.getDirectory().delete();
							ImageLoader.getInstance().getDiskCache().clear();
							initCacheSize();
						}
					});
			dialog.setNegativeButton(getString(android.R.string.cancel), null);
			
			//dialog千万不要忘了show()
			dialog.create().show();
			break;
		case R.id.rl_mine_user_agreement:// TODO 用户协议
			fragment = new UserProtocolFragment();
			break;
		}
		if (fragment != null) {
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
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

}
