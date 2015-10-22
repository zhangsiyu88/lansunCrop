package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.ad;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.ioc.view.listener.OnTextChanged;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.JiWenListAdapter;
import com.lansun.qmyo.adapter.MineCouponsAdapter;
import com.lansun.qmyo.adapter.MineSecretaryAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.domain.CouponsData;
import com.lansun.qmyo.domain.CouponsList;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.QuestionDetailItem;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.SecretaryQuestions;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.RefreshJiWenEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CouponsGiftDialog;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

/**
 * 我的优惠券
 * 
 * @author bhxx
 * 
 */
public class MineCouponsFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {

		private TextView tv_activity_title;
		private RecyclingImageView iv_activity_shared;
		private ImageView iv_coupons_move;
	}

	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)
	private MyListView lv_mine_coupons_list;
	private CouponsList list;
	private MineCouponsAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		// View rootView = inflater.inflate(R.layout.activity_mine_coupons,
		// null);
		View rootView = inflater.inflate(R.layout.coupons_empty, null);
		
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		// initTitle(v.tv_activity_title, R.string.mine_coupons,
		// v.iv_activity_shared, R.drawable.secretary_help_1);
		refreshUrl = GlobalValue.URL_COUPON;
		refreshKey = 0;
		// refreshCurrentList(refreshUrl, null, refreshKey,
		// lv_mine_coupons_list);
		// refreshCurrentList(GlobalValue.URL_COUPON_GIFT, null, 1, null);

		Animation loadAnimation = AnimationUtils.loadAnimation(activity,
				R.anim.top_bottom);
		v.iv_coupons_move.startAnimation(loadAnimation);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				list = Handler_Json.JsonToBean(CouponsList.class,
						r.getContentAsString());
				ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

				if (list.getData() != null) {
					for (CouponsData data : list.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("tv_coupons_money", data.getDenomination());
						map.put("tv_coupons_time", data.getOffuse_time());
						long shengyuDay = (Handler_Time.getInstance(
								data.getOffuse_time()).getTimeInMillis() - System
								.currentTimeMillis()) / (1000 * 60 * 60 * 24);
						map.put("tv_coupons_shengyu_day_num", String.format(
								getString(R.string.shengyu_day_num), shengyuDay
										+ ""));
						dataList.add(map);
					}
					if (adapter == null) {
						adapter = new MineCouponsAdapter(lv_mine_coupons_list,
								dataList, R.layout.coupons_item);
						lv_mine_coupons_list.setAdapter(adapter);
					} else {
						adapter.notifyDataSetChanged();
					}
					PullToRefreshManager.getInstance()
							.onHeaderRefreshComplete();
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();

				} else {
					lv_mine_coupons_list.setAdapter(null);
				}
				break;

			case 1:
				if ("true".equals(r.getContentAsString())) {
					CouponsGiftDialog dialog = new CouponsGiftDialog();
					dialog.show(getFragmentManager(), "gift");
				}
				break;
			}
		} else {

			progress_text.setText(R.string.net_error_refresh);
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}
}