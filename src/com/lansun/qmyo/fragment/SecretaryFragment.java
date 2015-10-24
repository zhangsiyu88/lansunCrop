package com.lansun.qmyo.fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryCardShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryInvestmentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryLifeShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryPartyShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryShoppingShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryStudentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryTravelShowFragment;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;

public class SecretaryFragment extends BaseFragment {
	private CircleImageView iv_secretary_head;
	private LinearLayout ll_secretary_setting;
	@InjectAll
	Views v;
	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_found, bottom_mine, ll_shopping_carnival, ll_new_shopping,
		ll_shengyan_part, ll_gaozhi_life, ll_studybroad,
		ll_licai_touzi, ll_handlecard, ll_secretary_help;
		private ImageView iv_secretary_icon;
		private TextView tv_secretary_icon, tv_secretary_name,tv_secretary_tip1;
	}
	private String[] secretaryTitle;
	private String[] secretaryhint;
	private int[][] secretaryImages = new int[][] {
			{ R.drawable.details_figure01, R.drawable.details_figure02,
				R.drawable.details_figure03, R.drawable.details_figure04,
				R.drawable.details_figure05, R.drawable.details_cannot },
				{ R.drawable.details_shopping01, R.drawable.details_shopping02,
					R.drawable.details_shopping03,
					R.drawable.details_shopping04, R.drawable.details_cannot },
					{ R.drawable.details_party01, R.drawable.details_party02,
						R.drawable.details_party03, R.drawable.details_party04,
						R.drawable.details_cannot },
						{ R.drawable.details_life01, R.drawable.details_life02,
							R.drawable.details_life03, R.drawable.details_life04,
							R.drawable.details_cannot },
							{ R.drawable.details_abroad01, R.drawable.details_abroad02,
								R.drawable.details_cannot },
								{ R.drawable.details_financial01, R.drawable.details_financial02,
									R.drawable.details_cannot },
									{ R.drawable.details_card01, R.drawable.details_cannot } };
	private Fragment fragment;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_secretary, container,false);
		Handler_Inject.injectFragment(this, rootView);
		v.iv_secretary_icon.setPressed(true);
		initView(rootView);
		setListener();
		return rootView;
	}

	private void setListener() {
		iv_secretary_head.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus bus = EventBus.getDefault();
				FragmentEntity entity = new FragmentEntity();
				Fragment fragment = new SecretarySettingFragment();
				entity.setFragment(fragment);
				bus.post(entity);
			}
		});
		ll_secretary_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GlobalValue.user == null || isExperience()) {
					DialogUtil.createTipAlertDialog(activity,R.string.please_registerorlogin_title,
							new TipAlertDialogCallBack() {
								@Override
								public void onPositiveButtonClick(DialogInterface dialog, int which) {
									EventBus bus = EventBus.getDefault();
									FragmentEntity entity = new FragmentEntity();
									Fragment fragment=new RegisterFragment();
									entity.setFragment(fragment);
									bus.post(entity);
									dialog.dismiss();
								}
								@Override
								public void onNegativeButtonClick(
										DialogInterface dialog, int which) {
										dialog.dismiss();
								}
							});
				} else{
					EventBus bus = EventBus.getDefault();
					FragmentEntity entity = new FragmentEntity();
					Fragment fragment = new MineSecretaryFragment();
					entity.setFragment(fragment);
					bus.post(entity);
				}

			}
		});
	}

	private void initView(View view) {
		iv_secretary_head=(CircleImageView)view.findViewById(R.id.iv_secretary_head);
		ll_secretary_setting=(LinearLayout)view.findViewById(R.id.ll_secretary_setting);
	}

	@InjectInit
	private void init() {
		secretaryTitle = getResources().getStringArray(R.array.secretary_title);
		secretaryhint = getResources().getStringArray(R.array.secretary_hint);
		v.iv_secretary_icon.setPressed(true);
		v.tv_secretary_icon.setTextColor(getResources().getColor(
				R.color.app_green2));

		//获取小秘书
		refreshCurrentList(GlobalValue.URL_SECRETARY, null, 0, null);
	}

	@Override
	public void onResume() {
		v.iv_secretary_icon.setPressed(true);
		super.onResume();
	}
	@Override
	public void onPause() {
		/*	v.iv_secretary_icon.setPressed(false);
		v.tv_secretary_icon.setTextColor(getResources().getColor(
				R.color.text_nomal));*/
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Bundle args = new Bundle();
		switch (view.getId()) {
		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
			break;
		case R.id.bottom_mine:
			fragment = new MineFragment();
			break;
		case R.id.ll_secretary_help:// 帮助界面
			fragment = new HelpFragment();
			break;
		case R.id.ll_shopping_carnival:// 旅游度假
			fragment = new SecretaryTravelShowFragment();
			break;
		case R.id.ll_new_shopping:// 新品购物
			fragment = new SecretaryShoppingShowFragment();
			break;
		case R.id.ll_shengyan_part:// 盛宴派对
			fragment = new SecretaryPartyShowFragment();
			break;
		case R.id.ll_gaozhi_life:// 高质生活
			fragment = new SecretaryLifeShowFragment();
			break;
		case R.id.ll_studybroad:// 留学服务
			fragment = new SecretaryStudentShowFragment();
			break;
		case R.id.ll_licai_touzi:// 理财投资
			fragment = new SecretaryInvestmentShowFragment();
			break;
		case R.id.ll_handlecard:// 办卡推荐
			fragment = new SecretaryCardShowFragment();
			break;
		}
		
		entity.setFragment(fragment);
		bus.post(entity);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.secretary = Handler_Json.JsonToBean(
						Secretary.class, r.getContentAsString());
				v.tv_secretary_name.setText(GlobalValue.secretary.getName());
				loadPhoto(GlobalValue.secretary.getAvatar(),
						iv_secretary_head);
				if (!TextUtils.isEmpty(GlobalValue.secretary.getOwner_name())) {
					v.tv_secretary_tip1.setText(String.format(
							getString(R.string.secretary_tip),
							GlobalValue.secretary.getOwner_name()));
				} else {
					v.tv_secretary_tip1.setText(String.format(
							getString(R.string.secretary_tip), "亲爱的大大"));
				}
				break;
			}
		}else {
			CustomToast.show(activity, "提示", "网络异常,请先检查网络状态");
		} 
	}
}
