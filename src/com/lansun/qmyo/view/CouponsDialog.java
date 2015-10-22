package com.lansun.qmyo.view;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.lansun.qmyo.adapter.BankCardAdapter;
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
 * TODO 优惠券 二维码界面
 * 
 * @author bhxx
 * 
 */
public class CouponsDialog extends DialogFragment {

	@InjectAll
	Views v;

	class Views {
		private TextView tv_coupons_money;
		private RecyclingImageView iv_coupons_qr_code;
		private TextView tv_coupons_qr_code;
	}

	public CouponsDialog() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.coupons_dialog, container);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		view.setLayoutParams(new LayoutParams(screenWidth, screenHeight));
		Handler_Inject.injectFragment(this, view);
		getDialog().setCancelable(false);
		getDialog().setCanceledOnTouchOutside(false);
		return view;
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.isFirst = false;
				Secret secret = Handler_Json.JsonToBean(Secret.class,
						r.getContentAsString());
				App.app.setData("exp_secret", secret.getSecret());
				CustomToast.show(getActivity(), R.string.tip,
						R.string.tiyan_cuccess);
				// 更新token
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN
						+ secret.getSecret(), config, this);
				dismiss();
				break;
			}
		}
	}
}
