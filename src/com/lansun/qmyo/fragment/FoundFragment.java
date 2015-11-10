package com.lansun.qmyo.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.lansun.qmyo.domain.ArticlePoster;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.R;

/**
 * 发现
 * 
 * @author bhxx
 * 
 */
public class FoundFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_secretary, bottom_mine, ll_found_v16,
		ll_found_bangdan, rl_found_activity, rl_found_store,ll_found_activity,ll_found_store,
		rl_found_v16, rl_found_maijie;
		private RecyclingImageView iv_found_icon, iv_found_bg;
		private TextView tv_found_icon;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_found, null);
		Handler_Inject.injectFragment(this, rootView);
		v.iv_found_icon.setPressed(true);
		v.tv_found_icon.setTextColor(getResources()
				.getColor(R.color.app_green2));
		return rootView;
	}

	@InjectInit
	private void init() {
		v.iv_found_icon.setPressed(true);
		v.tv_found_icon.setTextColor(getResources()
				.getColor(R.color.app_green2));

		//拿图的网络访问暂时停掉，先从本地拿到手
		/*refreshCurrentList(GlobalValue.URL_ARTICLE_POSTER + getSelectCity()[0],null, 0, null);*/


	}

	@Override
	public void onPause() {
		//v.iv_found_icon.setPressed(false);
		/*v.tv_found_icon.setTextColor(getResources().getColor(R.color.text_nomal));*/
		super.onPause();
	}

	@Override
	public void onResume() {//在resume中重新设置了底部icon设置的按压状态
		v.iv_found_icon.setPressed(true);
		super.onResume();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.ll_found_activity:// 活动收藏
			if(App.app.getData("isExperience").equals("true")){
				/*CustomToast.show(getActivity(), "迈界小贴士", "总裁大大，要登陆的哟~");*/
				DialogUtil.createTipAlertDialog(activity,
						R.string.tip_activittiescollection, new TipAlertDialogCallBack() {

					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {

						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity entity = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", FoundFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						entity.setFragment(fragment);
						EventBus.getDefault().post(entity);

					}

					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return;
			}

			fragment = new MineActivityFragment();
			break;
		case R.id.ll_found_store:// 关注门店
			if(App.app.getData("isExperience").equals("true")||App.app.getData("isExperience").contains("true")){
				/*CustomToast.show(getActivity(), "迈界小贴士", "总裁大大，要登陆的哟~");*/

				DialogUtil.createTipAlertDialog(activity,
						R.string.tip_attentionstore, new TipAlertDialogCallBack() {

					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {

						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity entity = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", FoundFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						entity.setFragment(fragment);
						EventBus.getDefault().post(entity);
					}

					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				return;
			}
			fragment = new MineStoreFragment();
			break;
		case R.id.bottom_secretary:
			fragment = new SecretaryFragment();
			break;
		case R.id.bottom_mine:
			fragment = new MineFragment();
			break;
		case R.id.ll_found_v16:// 极文列表
			fragment = new JiWenListFragment();
			break;
		case R.id.ll_found_bangdan:// 榜单列表
			fragment = new BDListFragment();
			break;
		case R.id.rl_found_v16:// V16榜单列表
			fragment = new MineV16Fragment();
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
				ArticlePoster poster = Handler_Json.JsonToBean(ArticlePoster.class, r.getContentAsString());
				/*loadPhoto(poster.getPhoto(), v.iv_found_bg);*/
				break;
			}
		}else{
			String imageUrl = "http://7xn0y5.com2.z0.glb.qiniucdn.com/201509101040_144185284148?imageView2/1/w/960/h/720/format/jpg";
			loadPhoto(imageUrl, v.iv_found_bg);
		}

	}

}
