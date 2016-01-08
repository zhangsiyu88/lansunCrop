package com.lansun.qmyo.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.ClipboardManager;
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
	
	public String mCardId ;
	public String mCardHeadPhotoUrl ;
	public String mCardDesc ;
	public boolean mIsFirstEnter;
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
		TextView tv_expe_desc,tv_copy_randomnum,tv_random_num;
	}
	
	public RandomNumDialog(Activity activity) {
		this.mActivity = activity;
	}

	public RandomNumDialog(Activity activity, String result) {
		this.mResult = result;
	}

	
	
	public void setOnConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT)); 
		
		//保证其可以取消随机码的dialog
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
		v.tv_random_num.setText(mResult);
		
	   
		/*
		 * 刮刮乐
		((RubbleTextView) v.tv_random_num).beginRubbler(0XFFCECECE,20,1f);
		 * 
		 */
		getDialog().setCancelable(true);
		getDialog().setCanceledOnTouchOutside(true);
		
		return view;
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_copy_randomnum:
				ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(App.app.CLIPBOARD_SERVICE);
		        cm.setText(v.tv_random_num.getText());
		        Toast.makeText(getActivity(), "复制成功,棒棒哒！速领红包", Toast.LENGTH_LONG).show();
			
			    /*JumpToBizProfile.Req req = new JumpToBizProfile.Req();
				req.toUserName = "gh_aed20ad78a2d"; //公众号原始ID
				req.profileType = JumpToBizProfile.JUMP_TO_NORMAL_BIZ_PROFILE;
				req.extMsg = "extMsg";
				App.app.api.sendReq(req);*/
		        App.app.api.openWXApp();
			
//			dismiss();
			break;
		}
	}

	int cardId = 0;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			
			}
		}

	}
	

	
	
	
	
	
	
//	
//	/**
//	 * FileName: RubblerAct.java
//	 * @Desc    该类通过调用Text_Rubbler这个类将在Activity上显示一片刮一刮的区域，可以出发触摸事件
//	 * @author  HTP
//	 * @Date    20140312
//	 * @version 1.00 
//	 */ 
//	   
//	   
//	public class RubblerAct extends Activity { 
//	    // 刮开后文字显示 
//	    private TextView tv_rubbler; 
//	    // 得到刮一刮的内容 
//	    private Sentence mSentence; 
//	    // 下一张 
//	    private TextView tv_next; 
//	   
//	    @Override 
//	    public void onCreate(Bundle savedInstanceState) { 
//	        super.onCreate(savedInstanceState); 
//	   
//	        // setContentView(new Rubble(this,"谢谢惠顾",new Rect(100, 200, 
//	        // 300,250),2,1f,14)); 
//	   
//	        // ///////////////////////////////////////// 
//	        setContentView(R.layout.rubbler); 
//	        // 设置的颜色必须要有透明度。 
//	        ((Rubble) findViewById(R.id.tv_random_num)).beginRubbler(0xFFFFFFFF, 20, 
//	                1f);// 设置橡皮擦的宽度等 
//	        mSentence = new Sentence(); 
//	        
//	        
//	        tv_rubbler = (TextView) findViewById(R.id.tv_random_num); 
//	        String str = mSentence.getSentence(); 
//	        tv_rubbler.setText(str); 
//	   
//	        tv_next = (TextView) findViewById(R.id.tv_next); 
//	   
//	        // 点击下一步 
//	        tv_next.setOnClickListener(new OnClickListener() { 
//	   
//	            @Override 
//	            public void onClick(View v) { 
//	                // TODO Auto-generated method stub 
//	                String str = mSentence.getSentence(); 
//	                tv_rubbler.setText(str); 
//	                ((Text_Rubbler) findViewById(R.id.rubbler))// 初始化状态 
//	                        .beginRubbler(0xFFFFFFFF, 20, 1f); 
//	   
//	            } 
//	        }); 
//	   
//	    } 

	   
//	    /**
//	     * 键盘事件，当按下back键的时候询问是否再按一次退出程序
//	     */ 
//	    // 退出时间 
//	    private long exitTime = 0; 
//	    @Override 
//	    public boolean onKeyDown(int keyCode, KeyEvent event) { 
//	        if (keyCode == KeyEvent.KEYCODE_BACK 
//	                && event.getAction() == KeyEvent.ACTION_DOWN) { 
//	            if ((System.currentTimeMillis() - exitTime) > 2000) { 
//	                Toast.makeText(getApplicationContext(), "再按一次退出程序", 
//	                        Toast.LENGTH_SHORT).show(); 
//	                exitTime = System.currentTimeMillis(); 
//	            } else { 
//	                finish(); 
//	                System.exit(0); 
//	   
//	            } 
//	            return true; 
//	        } 
//	        return super.onKeyDown(keyCode, event); 
//	    } 
//	   
//	}
	
	
	
	
	
}
