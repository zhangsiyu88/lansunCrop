package com.lansun.qmyo.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.R;
import com.lansun.qmyo.fragment.GrabRedPackFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * 体验dialog
 * 
 * @author bhxx
 * 
 */
@SuppressLint({ "ValidFragment", "NewApi" })
public class GrabRedPackOverDialog extends DialogFragment {

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();
	private static final int HAVE_OVER = 1;
	private static final int HAVE_GOT = 2;
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

	private GrabRedPackFragment mGrabRedPackFragment;

//	private ObservableWebView mWebView;
	private WebView mWebView;

	private int mStatue;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		Button btn_expe_confirm, tv_expe_relogin;
		RecyclingImageView iv_exp_bankcard;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		TextView tv_expe_desc,tv_copy_randomnum,tv_random_num,tv_command_content_2;
	}
	
	public GrabRedPackOverDialog(Activity activity) {
		this.mActivity = activity;
	}

	public GrabRedPackOverDialog(Activity activity, String result) {
		this.mResult = result;
	}

	public GrabRedPackOverDialog(Activity activity, ObservableWebView webView) {
		this.mActivity = activity;
		this.mWebView = webView;
	}
	public GrabRedPackOverDialog(Activity activity, WebView webView) {
		this.mActivity = activity;
		this.mWebView = webView;
	}

	public GrabRedPackOverDialog(Activity activity,
			ObservableWebView webView, int statue) {
		this.mActivity = activity;
		this.mWebView = webView;
		this.mStatue = statue;
	}
	public GrabRedPackOverDialog(Activity activity,
			WebView webView, int statue) {
		this.mActivity = activity;
		this.mWebView = webView;
		this.mStatue = statue;
	}

	public GrabRedPackOverDialog(Activity activity,
			GrabRedPackFragment grabRedPackFragment, ObservableWebView webView) {
		this.mActivity = activity;
		this.mWebView = webView;
		this.mGrabRedPackFragment = grabRedPackFragment;
		
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
		View view = null;
		switch(mStatue){
		case HAVE_GOT:
			view = inflater.inflate(R.layout.dialog_grabredpack_got, container);
			break;
		case HAVE_OVER:
			view = inflater.inflate(R.layout.dialog_grabredpack_over, container);
			break;
		default:
			view = inflater.inflate(R.layout.dialog_grabredpack_over, container);
			break;
		}
		
		Handler_Inject.injectFragment(this, view);
		
		/*v.tv_command_content_2.setText(Html.fromHtml(String.format(getString(R.string.command_content_2), "迈界","qmyoservice")));
		v.tv_random_num.setText(mResult);*/
		
		/*
		 * 刮刮乐
		((RubbleTextView) v.tv_random_num).beginRubbler(0XFFCECECE,20,1f);
		 * 
		 */
		getDialog().setCancelable(true);
		getDialog().setCanceledOnTouchOutside(true);
		
		return view;
	}

	@SuppressLint("NewApi") private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_copy_randomnum://实际为：webView滑动至底端的操作
				/*ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(App.app.CLIPBOARD_SERVICE);
		        cm.setText(v.tv_random_num.getText());
		        Toast.makeText(getActivity(), "复制成功,棒棒哒！关注“迈界”速领红包", Toast.LENGTH_LONG).show();*/
			    this.dismiss();
			    mWebView.scrollTo(0, (int) (mWebView.getContentHeight()));
			
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

	
	
	/*public interface onWebViewScrollToBottom{
		public void webViewScrollTo();
	}*/
}
