package com.lansun.qmyo.fragment.secretary_detail;

import jp.wasabeef.blurry.Blurry;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.lansun.qmyo.R;
import com.lansun.qmyo.fragment.BaseFragment;


public class SecretaryDetailsBaseFragment extends BaseFragment {

	private SecretaryDetailsFragmentBroadCastReceiver broadCastReceiver;
	private IntentFilter filter;
	public ExecutInitData  executInitData;
	//private IExecutBlurryView executBlurryView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(broadCastReceiver==null){
			broadCastReceiver = new SecretaryDetailsFragmentBroadCastReceiver();
			System.out.println("七大秘书详情页在注册广播 ing");
			filter = new IntentFilter();
			filter.addAction("com.lansun.qmyo.refreshMySecretary");
			getActivity().registerReceiver(broadCastReceiver, filter);
		}
		
	}
	
	public class SecretaryDetailsFragmentBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.refreshMySecretary")){
				System.out.println("七大秘书详情页收到刷新 图标 和 称呼 的广播了");
				
				/*initData();*/
				executInitData.exeInitHeaderAndName();
			}
		}
	}
	
	public void blurryView(final View rootView,Dialog dialog){
		
		/**
		 * 模糊化背景
		 */
		Blurry.with(getActivity())
		.radius(25)
		.sampling(2)
		.async()
		.animate(500)
		.onto((ViewGroup) rootView);
		
		//dialog消失时，需要恢复背景页面的效果
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				Blurry.delete((ViewGroup) rootView);
			}
		});
		
	}
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(broadCastReceiver);
		super.onDestroy();
		
	}
	
	public  interface  ExecutInitData { 
		 public void exeInitHeaderAndName();
	}
	
	public void setExecutInitData(ExecutInitData executInitData){
		this.executInitData = executInitData;
	}
	
//	public interface IExecutBlurryView{
//		 public void exeBluryView(View rootView,Dialog dialog);
//	}
//	public void setExecutBlurryView(IExecutBlurryView executBlurryView){
//		this.executBlurryView = executBlurryView;
//	}
}
