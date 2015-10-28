package com.lansun.qmyo.fragment;
import java.io.IOException;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
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
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.information.InformationCount;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryCardShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryInvestmentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryLifeShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryPartyShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryShoppingShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryStudentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryTravelShowFragment;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class SecretaryFragment extends BaseFragment {
	private String secretary_size;
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
		private TextView tv_secretary_icon, tv_secretary_name,tv_secretary_tip1,have_information;
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
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (isExperience()) {
					setSecretaryInformation();
				}else {
					if ("false".equals(GlobalValue.mySecretary.getHas())) {
						final Dialog dialog=new Dialog(activity, R.style.Translucent_NoTitle);
						dialog.show();
						dialog.setContentView(R.layout.dialog_setting_secretary);
						Window window = dialog.getWindow();
						window.findViewById(R.id.setting_now).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
								FragmentEntity entity=new FragmentEntity();
								Fragment fragment=new SecretarySettingFragment();
								entity.setFragment(fragment);
								EventBus.getDefault().post(entity);
							}
						});
					}else {
						setSecretaryInformation();
					}
				}
				break;
			case 1:
				CustomToast.show(activity, "提示", "信息获取失败,请重试");
				break;
			case 2:
				CustomToast.show(activity, "提示", "网络异常,请刷新后重试");
				break;
			case 5:
				if("1".equals(secretary_size)){
					v.have_information.setVisibility(View.VISIBLE);
				}else{
					v.have_information.setVisibility(View.GONE);
				}
				break;
			}
		}
	};
	private void setSecretaryInformation() {
		v.tv_secretary_name.setText(GlobalValue.mySecretary.getName());
		loadPhoto(GlobalValue.mySecretary.getAvatar(),
				iv_secretary_head);
	};
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	private void setListener() {
		iv_secretary_head.setOnClickListener(new OnClickListener() {
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
					Fragment fragment = new SecretarySettingFragment();
					entity.setFragment(fragment);
					bus.post(entity);
				}

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
		if (!isExperience()) {
			OkHttp.asyncGet(GlobalValue.URL_SECRETARY_SAVE, "Authorization","Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						GlobalValue.mySecretary=gson.fromJson(json,MySecretary.class);
						handleOk.sendEmptyMessage(0);
					}else {
						handleOk.sendEmptyMessage(1);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {
					handleOk.sendEmptyMessage(2);
				}
			});
			OkHttp.asyncGet(GlobalValue.URL_USER_MESSAGE, "Authorization", "Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						InformationCount iCount=gson.fromJson(json, InformationCount.class);
						secretary_size = String.valueOf(iCount.getSecretary());
						handleOk.sendEmptyMessage(5);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {

				}
			});
		}
	}

	@InjectInit
	private void init() {
		secretaryTitle = getResources().getStringArray(R.array.secretary_title);
		secretaryhint = getResources().getStringArray(R.array.secretary_hint);
		v.iv_secretary_icon.setPressed(true);
		v.tv_secretary_icon.setTextColor(getResources().getColor(
				R.color.app_green2));

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
}
