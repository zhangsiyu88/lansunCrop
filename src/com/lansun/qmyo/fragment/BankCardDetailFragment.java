package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityContent;
import com.lansun.qmyo.domain.BankCard;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.lansun.qmyo.R;

/**
 * 信用卡详情界面
 * 
 * @author bhxx
 * 
 */
public class BankCardDetailFragment extends DialogFragment {

	public BankCardDetailFragment() {
		super();
	}

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_bank_card_close;
		private RecyclingImageView iv_bank_card_head;
		private TextView tv_bank_card_name, tv_bank_card_desc,
				tv_bankcard_detail_desc, tv_bankcard_detail_bltj,
				tv_bankcard_detail_sfbj, tv_bankcard_detail_jbfw;
	}

	public static BankCardDetailFragment newInstance(
			DetailHeaderPagerAdapter adapter, int position) {
		BankCardDetailFragment fragment = new BankCardDetailFragment();
		fragment.setCancelable(true);
		return fragment;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getArguments().getString("id");

		InternetConfig config = new InternetConfig();
		config.setKey(0);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		FastHttpHander.ajaxGet(GlobalValue.URL_BANKCARD_DETAIL + id, config,
				this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.bankcard_detail_dialog, null);
		Handler_Inject.injectFragment(this, view);
		return view;
	}

	private String id;

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				BankCardData data = Handler_Json.JsonToBean(BankCardData.class,
						r.getContentAsString());
				v.tv_bank_card_name.setText(data.getBank().getName());
				v.tv_bank_card_desc.setText(data.getBankcard().getName());
				v.tv_bankcard_detail_desc.setText(data.getBankcard()
						.getSummary());
				
				//给图片设置上要求的裁剪效果
				ImageLoader.getInstance().displayImage(data.getBankcard().getPhoto(), v.iv_bank_card_head,options);
				
				ArrayList<ActivityContent> content = data.getBankcard()
						.getContent();
				for (ActivityContent dataContent : content) {
					String contentStr = initContent(dataContent);
					if ("办理条件".equals(dataContent.getTitle())) {
						v.tv_bankcard_detail_bltj.setText(contentStr);
					} else if ("收费标准".equals(dataContent.getTitle())) {
						v.tv_bankcard_detail_sfbj.setText(contentStr);
					} else {
						v.tv_bankcard_detail_jbfw.setText(contentStr);
					}
				}
				break;
			}
		} else {
			CustomToast.show(getActivity(), R.string.tip,
					R.string.loading_faild);
		}
	}

	private String initContent(ActivityContent dataContent) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < dataContent.getContent().size(); i++) {
			if (i == dataContent.getContent().size()) {
				sb.append("\u2022  " + dataContent.getContent().get(i));
			} else {
				sb.append("\u2022  " + dataContent.getContent().get(i) + "\r\n");
			}
		}
		return sb.toString();
	}

	private void click(View view) {
		dismiss();
	}

}
