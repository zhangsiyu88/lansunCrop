package com.lansun.qmyo.fragment.secretary_detail;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.MineBankcardFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SecretarySettingFragment;
import com.lansun.qmyo.fragment.task.TaskAssignment;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.GlobalValue;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;
public class SecretaryCardShowFragment extends BaseFragment{
	private RecyclingImageView iv_activity_back;
	private TextView commit_tv,check_suprice,content,tv_secretary_answer;
	private Fragment fragment;
	private FragmentEntity event;
	private CircleImageView iv_secretary_head;
	private String owner_name;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		event = new FragmentEntity();
	}
	private void initData() {
		if (GlobalValue.mySecretary!=null) {
			loadPhoto(GlobalValue.mySecretary.getAvatar(), iv_secretary_head);
			owner_name=GlobalValue.mySecretary.getOwner_name();
		}else {
			owner_name="总裁大人";
		}
		tv_secretary_answer.setText(owner_name+","+tv_secretary_answer.getText().toString());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		View view=inflater.inflate(R.layout.secretary_card_fragment,container,false);
		initView(view);
		initData();
		setListener();
		return view;
	}
	private void setListener() {
		commit_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isExperience()) {
					final Dialog dialog=new Dialog(getActivity(), R.style.Translucent_NoTitle);
			        dialog.setCancelable(true);
			        dialog.show();
			        Window window = dialog.getWindow();
			        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			        window.setContentView(R.layout.login_dialog);
					window.findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							FragmentEntity entity=new FragmentEntity();
							Fragment fragment=new RegisterFragment();
							Bundle bundle = new Bundle();
							bundle.putString("fragment_name", SecretaryCardShowFragment.class.getSimpleName());
							fragment.setArguments(bundle);
							entity.setFragment(fragment);
							EventBus.getDefault().post(entity);
						}
					});
				}else {
					if (GlobalValue.user!=null) {
						if (GlobalValue.mySecretary==null) {
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
								FragmentEntity entity=new FragmentEntity();
								Fragment fragment=new HomeFragment();
								Bundle bundle=new Bundle();
								bundle.putString("content", content.getText().toString());
								bundle.putString("type", "card");
								fragment.setArguments(bundle);
								entity.setFragment(fragment);
								EventBus.getDefault().post(entity);
							}
						}
					}
				}
			}
		});
		check_suprice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view=inflater.inflate(R.layout.card_dialog, null);	
				final AlertDialog dialog=new AlertDialog.Builder(getActivity()).create();
				dialog.setView(view);
				dialog.setCancelable(true);
				dialog.show();
				view.findViewById(R.id.card).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		});
		iv_activity_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
	}
	private void initView(View view) {
		tv_secretary_answer=(TextView)view.findViewById(R.id.tv_secretary_answer);
		iv_secretary_head=(CircleImageView)view.findViewById(R.id.iv_secretary_head);
		commit_tv=(TextView)view.findViewById(R.id.commit_tv);
		check_suprice=(TextView)view.findViewById(R.id.check_suprice);
		iv_activity_back=(RecyclingImageView)view.findViewById(R.id.iv_activity_back);
		content=(TextView)view.findViewById(R.id.content);
	}
}
