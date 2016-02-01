package com.lansun.qmyo.fragment.secretary_detail;
import jp.wasabeef.blurry.Blurry;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.R;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SecretarySettingFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryDetailsBaseFragment.ExecutInitData;
import com.lansun.qmyo.fragment.task.TaskAssignment;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.FastBlurBitmap;
import com.lansun.qmyo.utils.GlobalValue;


public class SecretaryCardShowFragment extends SecretaryDetailsBaseFragment implements ExecutInitData{
	private RecyclingImageView iv_activity_back;
	private TextView commit_tv,check_suprice,content;
	private Fragment fragment;
	private FragmentEntity event;
	private View rootView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		event = new FragmentEntity();
	}
//	private void initData() {
//		owner_name ="";
//		if (GlobalValue.mySecretary!=null) {
//			loadPhoto(GlobalValue.mySecretary.getAvatar(), iv_secretary_head);
//			owner_name=GlobalValue.mySecretary.getOwner_name();
//		}else {
//			owner_name="总裁大大";
//		}
//		tv_secretary_answer_text = tv_secretary_answer.getText().toString();
//		tv_secretary_answer.setText(owner_name+","+tv_secretary_answer_text);
//	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		rootView = inflater.inflate(R.layout.secretary_card_fragment,container,false);
		initView(rootView);
		initData();
		setListener();
		
		setExecutInitData(this);//将接口对象放进去
		
		return rootView;
	}
	private void setListener() {
		commit_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isExperience()) {
					final Dialog dialog=new Dialog(getActivity(), R.style.Translucent_NoTitle);
			        dialog.setCancelable(true);
			        blurryView(rootView, dialog);
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
							
							blurryView(rootView, dialog);
							
							
							dialog.show();
							dialog.setContentView(R.layout.dialog_setting_secretary);
							Window window = dialog.getWindow();
							
//							
//							// 模糊度
//							window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//							//透明度
//							lp.alpha=1f;//（0.0-1.0）
//							//黑暗度
//							lp.dimAmount=0.7f;
//							window.setAttributes(lp);
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
								
								blurryView(rootView, dialog);
								
								dialog.show();
								dialog.setContentView(R.layout.dialog_setting_secretary);
								Window window = dialog.getWindow();
								
//								// 模糊度
//								window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//								//透明度
//								lp.alpha=1f;//（0.0-1.0）
//								//黑暗度
//								lp.dimAmount=0.5f;
//								window.setAttributes(lp);
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

								Fragment fragment=new TaskAssignment();

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
	
	@Override
	public void exeInitHeaderAndName() {
		if (GlobalValue.mySecretary!=null) {
			loadPhoto(GlobalValue.mySecretary.getAvatar(), iv_secretary_head);
			owner_name=GlobalValue.mySecretary.getOwner_name();
		}else {
			owner_name="总裁大大";
		}
		tv_secretary_answer.setText(owner_name+","+tv_secretary_answer_text);
	
	}
}
