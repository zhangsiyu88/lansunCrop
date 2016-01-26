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

public class CustomDialogGrebRedpackWait extends Dialog {
	
	public  static CustomDialogGrebRedpackWait customDialogProgress = null;
	public Context context = null;
	
	public CustomDialogGrebRedpackWait(Context context, int theme) {
		super(context, theme);
	}


	public CustomDialogGrebRedpackWait(Context context) {
		super(context);
		this.context = context;
		createDialog(this.context);
		
	}

	public static CustomDialogGrebRedpackWait createDialog(Context context){
		
		customDialogProgress = new CustomDialogGrebRedpackWait(context,R.style.CustomProgressDialogWait);
		customDialogProgress.setContentView(R.layout.customdialog_grebredpack);
//		customDialogProgress.getWindow().setDimAmount((float) 0.2);
		customDialogProgress.getWindow().getAttributes().gravity = Gravity.CENTER;
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
    public CustomDialogGrebRedpackWait setTitile(String strTitle){
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
    public CustomDialogGrebRedpackWait setMessage(String strMessage){
    	TextView tvMsg = (TextView)customDialogProgress.findViewById(R.id.messageText);
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	return customDialogProgress;
    }

	public static CustomDialogGrebRedpackWait customDialogProgress(Activity activity) {
		customDialogProgress = new CustomDialogGrebRedpackWait(activity,R.style.CustomProgressDialog);
		customDialogProgress.setContentView(R.layout.customdialogprogress);
		customDialogProgress.getWindow().getAttributes().gravity = Gravity.CENTER;
		/*GifMovieView gifLoadingView = (GifMovieView) customDialogProgress.findViewById(R.id.gifLoadingView);
    	gifLoadingView.setMovieResource(R.drawable.loading);*/
		return customDialogProgress;
	}
	
	public void setText(String str){
		TextView tvMsg = (TextView)customDialogProgress.findViewById(R.id.messageText);
    	if (tvMsg != null){
    		tvMsg.setText(str);
    	}
	}
	
	public View getImageView(){
		ImageView view = (ImageView) customDialogProgress.findViewById(R.id.iv_gif_loadingprogress);
		return view;
	}
}
