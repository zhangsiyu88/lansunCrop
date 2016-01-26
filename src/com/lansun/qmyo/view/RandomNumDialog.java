package com.lansun.qmyo.view;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
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
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tencent.mm.sdk.modelbiz.JumpToBizProfile;
import com.lansun.qmyo.GrabRedPackActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;

/**
 * 体验dialog
 * 
 * @author bhxx
 * 
 */
@SuppressLint("ValidFragment")
public class RandomNumDialog extends DialogFragment {

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();
	 private static String[] lowercase = {
		   "a","b","c","d","e","f","g","h","i","j","k",
		   "l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	 private static String[] capital = {
		   "A","B","C","D","E","F","G","H","I","J","K",
		   "L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"}; 
	private static String[] number = {
		   "1","2","3","4","5","6","7","8","9","0"};
	private String _random_str;
	
	public onDismissListener mDdismissListener;
	public String mCardId ;
	public String mCardHeadPhotoUrl ;
	public String mCardDesc ;
	public boolean mIsFirstEnter;
	private Handler setTextHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			switch(msg.what){
			case 0:
				v.tv_random_num.setText(_random_str);
				break;
			case 1:
				v.tv_random_num.setText(mResult);
				if(_random_str == v.tv_random_num.getText()){
					v.tv_random_num.setText("请重新获取口令");
				}
				v.tv_copy_randomnum.setVisibility(View.VISIBLE);
                break;
			}
		}
	};
	
	/*
	 * 回调接口
	 */
	public interface OnConfirmListener {
		void confirm();
	}

	@InjectAll
	Views v;
	private OnConfirmListener listener;

	private Activity mActivity;

	private String mResult;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		Button btn_expe_confirm, tv_expe_relogin;
		RecyclingImageView iv_exp_bankcard;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		TextView tv_expe_desc,tv_copy_randomnum,tv_random_num,tv_command_content_2;
	}
	
	public RandomNumDialog(Activity activity) {
		this.mActivity = activity;
	}

	public RandomNumDialog(Activity activity, String result) {
		this.mResult = result;
		this.mDdismissListener = (onDismissListener) activity;
		
		_random_arrayList = new ArrayList<String>();
		for(int _i_lowercase = 0;_i_lowercase<26;_i_lowercase++){
			_random_arrayList.add(_i_lowercase,lowercase[_i_lowercase]);
		}
		for(int _i_capital = 0;_i_capital<26;_i_capital++){
			_random_arrayList.add(26+_i_capital,capital[_i_capital]);
		}
		for(int _i_number = 0;_i_number<10;_i_number++){
			_random_arrayList.add(52+_i_number,number[_i_number]);
		}
		for(int i=0;i<62;i++){
			LogUtils.toDebugLog("random", "第"+i+"个值为："+_random_arrayList.get(i));
		}
	}

	
	
	public void setOnConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); 
		
		//保证其可以取消随机码的dialog出现之前的dialog
		getDialog().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			 if (keyCode == KeyEvent.KEYCODE_BACK){
	            /* return true; // pretend we've processed it*/	            
				 return false; // pretend we've processed it
			 }else{
	        	   return false; // pass on to be processed as normal
	           }
			}
		});
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.dialog_random_num, container);
		Handler_Inject.injectFragment(this, view);
		
		v.tv_command_content_2.setText(Html.fromHtml(String.format(getString(R.string.command_content_2), "迈界","qmyoservice")));
		
		_creat_num_timer = new Timer();
		_creat_num_timer.schedule(new TimerTask(){
			@Override
			public void run(){
				_random_str = creatRandomNum();
				setTextHandler.sendEmptyMessage(0);
			}
		}, 100, 100);
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				_creat_num_timer.cancel();
				setTextHandler.sendEmptyMessage(1);
			}
		}, GlobalValue.RANDOM_TIME);
		
		/*
		 * 刮刮乐
		 * ((RubbleTextView) v.tv_random_num).beginRubbler(0XFFCECECE,20,1f);* 
		 */
		
		getDialog().setCancelable(true);
		getDialog().setCanceledOnTouchOutside(true);
		
		getDialog().setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mDdismissListener.onDismiss();//由GrabRedPackActivity来实现，当RandomNumDialog消失的时候，GrabRedPackActivity产生的具体操作
			}
		});
		return view;
	}

	protected String  creatRandomNum() {
		String str =new String();
		for(int i = 0;i<6;i++){
			LogUtils.toDebugLog("random", "获取到的随机位置为： "+(int)(Math.random()*62));
			str = str+_random_arrayList.get((int)(Math.random()*62));
		}
		return str;
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_copy_randomnum:
				ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(App.app.CLIPBOARD_SERVICE);
		        cm.setText(v.tv_random_num.getText());
		        Toast.makeText(getActivity(), "复制成功,棒棒哒！关注“迈界”速领红包", Toast.LENGTH_LONG).show();
			
			    /*JumpToBizProfile.Req req = new JumpToBizProfile.Req();
				req.toUserName = "gh_aed20ad78a2d"; //公众号原始ID
				req.profileType = JumpToBizProfile.JUMP_TO_NORMAL_BIZ_PROFILE;
				req.extMsg = "extMsg";
				App.app.api.sendReq(req);*/
		        App.app.api.openWXApp();
		        dismiss();
			break;
		}
	}

	int cardId = 0;
	private ArrayList<String> _random_arrayList;
	private Timer _creat_num_timer;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			
			}
		}
	}
	
	public interface onDismissListener{
		public void  onDismiss();
	}
	
}
