package com.lansun.qmyo.view;

/*import com.android.pc.ioc.view.GifMovieView;*/
import com.lansun.qmyo.R;
import com.umeng.socialize.utils.Log;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogProgress extends Dialog {
	
	public  static CustomDialogProgress customDialogProgress = null;
	public Context context = null;
	
	public CustomDialogProgress(Context context, int theme) {
		super(context, theme);
	}


	public CustomDialogProgress(Context context) {
		super(context);
		this.context = context;
		createDialog(this.context);
		
	}

	public static CustomDialogProgress createDialog(Context context){
		
		customDialogProgress = new CustomDialogProgress(context,R.style.CustomProgressDialog);
		
		Log.d("dialog","将布局送入到的控件上");
		
		
		/* 1.
		 * View customDialogProgress_view = LayoutInflater.from(context).inflate(R.layout.customdialogprogress, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		customDialogProgress.setContentView(customDialogProgress_view, layoutParams);*/
		
		/* 2.
		 * View customDialogProgress_view = LayoutInflater.from(context).inflate(R.layout.customdialogprogress, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		customDialogProgress.addContentView(customDialogProgress_view, layoutParams);*/
		
		
		/*3.
		 * */customDialogProgress.setContentView(R.layout.customdialogprogress);
		
		/*4.
		 * View customDialogProgress_view = LayoutInflater.from(context).inflate(R.layout.customdialogprogress, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		customDialogProgress.setContentView(customDialogProgress_view,layoutParams);*/
		
		
		customDialogProgress.getWindow().getAttributes().gravity = Gravity.CENTER;
		/*GifMovieView gifLoadingView = (GifMovieView) customDialogProgress.findViewById(R.id.gifLoadingView);
    	gifLoadingView.setMovieResource(R.drawable.loading);*/
		
		customDialogProgress.show();
		
		return customDialogProgress;
	}
	
	
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
    	
    	if (customDialogProgress == null){
    		return;
    	}
    	
    	/*GifMovieView gifLoadingView = (GifMovieView) customDialogProgress.findViewById(R.id.gifLoadingView);
    	gifLoadingView.setMovieResource(R.drawable.loading);*/
    	
    	
    	ImageView iv_gif_loadingprogress = (ImageView) customDialogProgress.findViewById(R.id.iv_gif_loadingprogress);
    	((AnimationDrawable)iv_gif_loadingprogress.getDrawable()).start();
    	
       /* AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();*/
    }
 
    /**
     * 
     * [Summary]
     *       setTitile 标题
     * @param strTitle
     * @return
     *
     */
    public CustomDialogProgress setTitile(String strTitle){
    	return customDialogProgress;
    }
    
    /**
     * 
     * [Summary]
     *       setMessage 提示内容
     * @param strMessage
     * @return
     *
     */
    public CustomDialogProgress setMessage(String strMessage){
    	TextView tvMsg = (TextView)customDialogProgress.findViewById(R.id.messageText);
    	
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	
    	return customDialogProgress;
    }

	public static CustomDialogProgress customDialogProgress(Activity activity) {
		customDialogProgress = new CustomDialogProgress(activity,R.style.CustomProgressDialog);
		customDialogProgress.setContentView(R.layout.customdialogprogress);
		customDialogProgress.getWindow().getAttributes().gravity = Gravity.CENTER;
		/*GifMovieView gifLoadingView = (GifMovieView) customDialogProgress.findViewById(R.id.gifLoadingView);
    	gifLoadingView.setMovieResource(R.drawable.loading);*/
		
		return customDialogProgress;
	}
	
	
}
