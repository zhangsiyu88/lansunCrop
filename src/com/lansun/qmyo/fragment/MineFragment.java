package com.lansun.qmyo.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Message;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

public class MineFragment extends BaseFragment {
	@InjectAll
	Views v;

	class Views {
		private TextView tv_mine_nickname, tv_mine_icon,
		tv_mine_message_center, tv_mine_secretary_message,
		tv_mine_comment_message;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_secretary, bottom_found,
		ll_mine_register_login, rl_mine_secretary, rl_mine_comments,
		rl_mine_history, ll_mine_xy_card, ll_mine_yhq, ll_mine_message,
		rl_mine_about, rl_mine_shared, sc_mine;
		private RecyclingImageView iv_mine_icon;
		private CircularImage iv_mine_head;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_mine, null);
		Handler_Inject.injectFragment(this, rootView);
		v.iv_mine_icon.setPressed(true);
		//v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));
		return rootView;
	}

	@InjectInit
	private void init() {
		v.iv_mine_icon.setPressed(true);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));

		if (GlobalValue.user != null) {
			String avatar = GlobalValue.user.getAvatar();
			if (!TextUtils.isEmpty(avatar)) {
				//加载头像上去
				loadPhoto(avatar, v.iv_mine_head);
			}

			if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {


				if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
					v.tv_mine_nickname.setText("请设置昵称");
					Log.i("Tag：nickName","NickName应该为设置昵称");

				}else{
					Log.i("Tag：nickName","NickName有值："+GlobalValue.user.getNickname());
					v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				}
			}else{
				v.tv_mine_nickname.setText("请注册或登陆");
			}
		}else{
			/*CustomToast.show(activity, "GlobalValue.user为空", "同上");*/
		}

		refreshCurrentList(GlobalValue.URL_USER_MESSAGE, null, 0, null);//去刷新消息
	}

	@Override
	public void onResume() {
		v.iv_mine_icon.setPressed(true);
		if (GlobalValue.user != null) {
			String avatar = GlobalValue.user.getAvatar();
			if (!TextUtils.isEmpty(avatar)) {
				//加载头像上去
				loadPhoto(avatar, v.iv_mine_head);
			}
			if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
				v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				Log.i("Tag：nickName","在init之后起作用");

				if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
					v.tv_mine_nickname.setText("请设置昵称");
				}else{
					v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				}
			}else{
				v.tv_mine_nickname.setText("请注册或登陆");
			}
		}
		super.onResume();
	}
	@Override
	public void onPause() {
		/*v.iv_mine_icon.setPressed(false);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.text_nomal));*/
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment = null;
		switch (view.getId()) {
		// case R.id.iv_mine_setting:// 设置界面
		// fragment = new SettingFragment();
		// break;
		case R.id.ll_mine_register_login:// 注册登录
			if (!TextUtils.isEmpty(App.app.getData("secret"))) {
				fragment = new PersonCenterFragment();
			} else {
				fragment = new RegisterFragment();
			}
			break;
		case R.id.rl_mine_secretary:// 我的私人秘书
			if (GlobalValue.user == null || isExperience()) {
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_enjoysecretaryyourself, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else
				fragment = new MineSecretaryFragment();
			break;
		case R.id.rl_mine_comments:// 我的评论
			if (GlobalValue.user == null || isExperience()) {
				fragment = new RegisterFragment();
			} else {
				fragment = new MineCommentsFragment();
			}
			break;
		case R.id.rl_mine_history:// 最近浏览
			if (GlobalValue.user == null || isExperience()) {
				fragment = new RegisterFragment();
			} else {
				fragment = new MineHistoryFragment();
			}
			break;
		case R.id.ll_mine_xy_card:// 信用卡

			/*	if (GlobalValue.user == null) {
				return;
			}
			if (isExperience()) {
				fragment = new RegisterFragment();
			} else
			 */
			fragment = new MineBankcardFragment();
			break;
		case R.id.ll_mine_yhq:// TODO 优惠券
			if (GlobalValue.user == null || isExperience()) {
				fragment = new RegisterFragment();
			} else
				fragment = new MineCouponsFragment();
			break;
		case R.id.ll_mine_message:// TODO 消息中心

			if(App.app.getData("isExperience")=="true"){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_getmessage, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else{
				fragment = new MessageCenterFragment();
			}


			/*fragment = new MessageCenterFragment();*/

			break;
		case R.id.rl_mine_about:
			fragment = new AboutFragment();
			break;
		case R.id.rl_mine_shared://分享APP
			fragment = new SharedFragment();
			break;

		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.bottom_secretary:
			fragment = new SecretaryFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
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
				Message msg = Handler_Json.JsonToBean(Message.class,
						r.getContentAsString());
				if (Integer.parseInt(msg.getMaijie()) > 0) {
					v.tv_mine_message_center.setText(msg.getMaijie());
					v.tv_mine_message_center.setVisibility(View.VISIBLE);
				}
				if (Integer.parseInt(msg.getComment()) > 0) {
					v.tv_mine_comment_message.setText(msg.getComment());
					v.tv_mine_comment_message.setVisibility(View.VISIBLE);
				}
				if (Integer.parseInt(msg.getSecretary()) > 0) {
					v.tv_mine_secretary_message.setText(msg.getSecretary());
					v.tv_mine_secretary_message.setVisibility(View.VISIBLE);
				}
				break;
			}
		}

	}
}
