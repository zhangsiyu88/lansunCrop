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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.jpush.android.data.s;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
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
 * 领取优惠券dialog
 * 
 * @author bhxx
 * 
 */
public class CouponsGiftDialog extends DialogFragment {

	public CouponsGiftDialog() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(true);

		
		View view = inflater.inflate(R.layout.coupons_gift_dialog, container);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		view.setLayoutParams(new LayoutParams(screenWidth, screenHeight));
		Handler_Inject.injectFragment(this, view);
		getDialog().setCancelable(true);
		getDialog().setCanceledOnTouchOutside(false);
		
		return view;
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_expe_confirm:
			dismiss();
			break;
		}
	}

	@InjectMethod(value = { @InjectListener(ids = { R.id.ll_coupons_gift_look }, listeners = { OnClick.class }) })
	private void close(View view) {
		dismiss();
	}

}
