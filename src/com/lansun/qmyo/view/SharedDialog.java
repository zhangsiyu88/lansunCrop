package com.lansun.qmyo.view;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.R.interpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.lansun.qmyo.app.App;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

/**
 * TODO 分享对话框
 * 
 * @author bhxx
 * 
 */
public class SharedDialog implements OnClickListener {
	private Activity activity;
	private UMSocialService mController = UMServiceFactory
			.getUMSocialService(GlobalValue.DESCRIPTOR);
	
	private PopupWindow window;
	private float mDensity;
	public int i = 0 ;
	private ArrayList<View> viewList = new ArrayList<View>();
	private boolean mNeedShake = true;
	

	public void showPopwindow(View v, final Activity activity, String title,
			String content, String imageUrl) {
		this.activity = activity;
		
		 
		configPlatforms();
		setShareContent(title, content, imageUrl,null);//分享的内容
		
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view = inflater.inflate(R.layout.dialog_shared, null);
		View ll_shared_wx_friend = view.findViewById(R.id.ll_shared_wx_friend);
		View rl_shared_friend = view.findViewById(R.id.rl_shared_friend);
		View ll_shared_tx_wb = view.findViewById(R.id.ll_shared_tx_wb);
		View ll_shared_sina_wb = view.findViewById(R.id.ll_shared_sina_wb);
		
		
		view.findViewById(R.id.ll_shared_wx_friend).setOnClickListener(this);
		view.findViewById(R.id.rl_shared_friend).setOnClickListener(this);
		view.findViewById(R.id.ll_shared_tx_wb).setOnClickListener(this);
		view.findViewById(R.id.ll_shared_sina_wb).setOnClickListener(this);
		view.findViewById(R.id.tv_shared_cancle).setOnClickListener(this);

		// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

		window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		WindowManager.LayoutParams params = activity.getWindow()
				.getAttributes();
		params.alpha = 0.7f;
		params.dimAmount = (float) 1.0;
		
		window.update();
		activity.getWindow().setAttributes(params);

		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);
		window.setOutsideTouchable(true);

		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		window.setBackgroundDrawable(dw);

		// 在底部显示
		window.showAtLocation(v, Gravity.BOTTOM, 0, 0);

		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				WindowManager.LayoutParams params = activity.getWindow().getAttributes();
				params.alpha = 1f;
				params.dimAmount = (float) 0.5;
				activity.getWindow().setAttributes(params);
			}
		});
	}
	
	
	
	
	
	@SuppressLint("HandlerLeak") 
	public void showPopwindow(View v, final Activity activity, String title,
			String content, String imageUrl, String currentActivityUrl) {
		this.activity = activity;
		DisplayMetrics dm = new DisplayMetrics(); 
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);  
        if (dm != null) {  
            mDensity = dm.density;  
        } 
        
		configPlatforms();
		
		setShareContent(title, content, imageUrl,currentActivityUrl);//分享的内容
		
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view = inflater.inflate(R.layout.dialog_shared, null);
		ll_shared_wx_friend = view.findViewById(R.id.ll_shared_wx_friend);
		rl_shared_friend = view.findViewById(R.id.rl_shared_friend);
		ll_shared_tx_wb = view.findViewById(R.id.ll_shared_tx_wb);
		ll_shared_sina_wb = view.findViewById(R.id.ll_shared_sina_wb);
		
		view.findViewById(R.id.ll_shared_wx_friend).setOnClickListener(this);
		view.findViewById(R.id.rl_shared_friend).setOnClickListener(this);
		view.findViewById(R.id.ll_shared_tx_wb).setOnClickListener(this);
		view.findViewById(R.id.ll_shared_sina_wb).setOnClickListener(this);
		view.findViewById(R.id.tv_shared_cancle).setOnClickListener(this);
		
		
		
		final ArrayList<View> viewList = new ArrayList<View>();
		viewList.add(ll_shared_wx_friend);
		viewList.add(rl_shared_friend);
		viewList.add(ll_shared_tx_wb);
		viewList.add(ll_shared_sina_wb);
		
		
		// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

		window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		
		
		WindowManager.LayoutParams params = activity.getWindow().getAttributes();
		params.alpha = 0.2f;
		activity.getWindow().setAttributes(params);

		window.setOutsideTouchable(false);
		window.update();
		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);

		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 实例化一个ColorDrawable颜色为半透明
		/*ColorDrawable dw = new ColorDrawable(Color.BLACK);
		window.setBackgroundDrawable(dw);*/
		

		// 在底部显示
		window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				/*super.handleMessage(msg);*/
				if(i<4){
					shakeAnimation(viewList.get(i));
					i++;
//					LogUtils.toDebugLog("shake", "shakeAnimation中的i为："+ i);
				}else if(i>=4){
//					mNeedShake = false;
//					LogUtils.toDebugLog("shake", "shakeAnimation中的i为："+ i);
				}
			}
		};
		
		

		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				WindowManager.LayoutParams params = activity.getWindow().getAttributes();
				params.alpha = 1f;
				activity.getWindow().setAttributes(params);
			}
		});
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		}, 200,100);//TODO
		
	}

	/**
	 * 配置分享平台参数</br>
	 */
	private void configPlatforms() {
		// 添加QQ、QZone平台
		addQZoneQQPlatform();
		// 添加微信、微信朋友圈平台
		addWXPlatform();
	}

	
	private void addWXPlatform() {
		String appId = GlobalValue.WX_APP_ID;
		String appSecret = GlobalValue.WX_APP_SECRET;
		
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(activity, appId, appSecret);
		wxHandler.addToSocialSDK();

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(activity, appId,appSecret);
		
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

	}

	/**
	 * @功能描述 : 添加QQ平台分享
	 * @return
	 */
	private void addQZoneQQPlatform() {
		String appId = GlobalValue.QZONE_APP_ID;
		String appKey = GlobalValue.QZONE_APP_KEY;
		
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activity, appId,appKey);
		
		qqSsoHandler.setTargetUrl(GlobalValue.TARGET_URL);
		qqSsoHandler.addToSocialSDK();

		// 添加QZone平台
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity, appId,appKey);
		qZoneSsoHandler.addToSocialSDK();
	}
	/**
	 * 设置四大板块的分享内容
	 */
	private void setShareContent(String title, String content, String imageUrl,String currentActivityUrl) {
		/**
		 * 微信个人分享
		 */
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareContent(title);
		weixinContent.setTitle(content);
		/*weixinContent.setTargetUrl(GlobalValue.TARGET_URL);*/
		weixinContent.setTargetUrl(currentActivityUrl);
		
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			weixinContent.setShareMedia(urlImage);
		}
		mController.setShareMedia(weixinContent);
		
		
		
		
		
		/**
		 * 朋友圈分享
		 */
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(content);
		
		/*circleMedia.setTitle(title);*/ //---> 将分享内容修改至标题处
		
		String  circleShareContent = content+","+title;
		circleMedia.setTitle(circleShareContent);
		
		/*circleMedia.setTargetUrl(GlobalValue.TARGET_URL);*/
		circleMedia.setTargetUrl(currentActivityUrl);
		
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			circleMedia.setShareMedia(urlImage);
		}
		// circleMedia.setShareMedia(uMusic);
		// circleMedia.setShareMedia(video);
		mController.setShareMedia(circleMedia);
		
		/*
		 * showPopwindow(View v, final Activity activity,
		 *      String title,// 门店名称
				String content,  //活动的名称
				String imageUrl,  //photo的地址
				String currentActivityUrl)  //分享的链接 */

		/**
		 * 设置QQ空间分享内容
		 */
/*		QZoneShareContent qzone = new QZoneShareContent();
		qzone.setShareContent(content);
		qzone.setTargetUrl(GlobalValue.TARGET_URL);
		qzone.setTitle(title);
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			weixinContent.setShareMedia(urlImage);
		}
		// qzone.setShareMedia(uMusic);
		mController.setShareMedia(qzone);*/

		// video.setThumb(new UMImage(getActivity(),
		// BitmapFactory.decodeResource(
		// getResources(), R.drawable.device)));
		
		/**
		 * 设置QQ分享
		 */
/*		QQShareContent qqShareContent = new QQShareContent();
		qqShareContent.setShareContent(content);
		qqShareContent.setTitle(title);
		// qqShareContent.setShareMedia(image);
		qqShareContent.setTargetUrl(GlobalValue.TARGET_URL);
		mController.setShareMedia(qqShareContent);*/
		
		
		/**
		 * 网页版腾讯微博的分享
		 */
	    TencentWbShareContent tencentWbShareContent = new TencentWbShareContent();
	    tencentWbShareContent.setTitle(title);
	    
	    String newTencentShareContent = "#迈界惊喜好活动#我发现了一个超赞的活动,就在【"+title+"】,"+content+", 戳我戳我-->"+currentActivityUrl;
	    tencentWbShareContent.setShareContent(newTencentShareContent);
	    
	    if (!TextUtils.isEmpty(imageUrl))
	    	tencentWbShareContent.setShareImage(new UMImage(this.activity, imageUrl));
	    tencentWbShareContent.setTargetUrl(currentActivityUrl);//腾讯微博分享的活动链接
	    this.mController.setShareMedia(tencentWbShareContent);
	    
	    
	    /**
	     * 网页版新浪微博的分享
	     */
	    SinaShareContent sinaShareContent = new SinaShareContent();
	    sinaShareContent.setTitle(title);
	    
	    
	    String newSinaShareContent = "#迈界惊喜好活动#我发现了一个超赞的活动,就在【"+title+"】,"+content+", 戳我戳我-->"+currentActivityUrl;
	    /*sinaShareContent.setShareContent(content+"."+currentActivityUrl);//-->test*/	
	    sinaShareContent.setShareContent(newSinaShareContent);
    
	    if (!TextUtils.isEmpty(imageUrl))
	    	sinaShareContent.setShareImage(new UMImage(this.activity, imageUrl));
	    
	    sinaShareContent.setTargetUrl(currentActivityUrl);//新浪微博分享的活动链接
	    
	    /*sinaShareContent.setAppWebSite(currentActivityUrl);*///-->test
	    
	    this.mController.setShareMedia(sinaShareContent);
	    
		/*
		 * showPopwindow(View v, final Activity activity,
		 *      String title,// 门店名称
				String content,  //活动的名称
				String imageUrl,  //photo的地址
				String currentActivityUrl)  //分享的链接 */
	}

	@Override
	public void onClick(View v) {
		window.dismiss();
		int id = v.getId();
		switch (id) {
		case R.id.ll_shared_wx_friend:
			performShare(SHARE_MEDIA.WEIXIN);
			break;
		case R.id.rl_shared_friend:
			performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
			break;
		case R.id.ll_shared_tx_wb:
			performShare(SHARE_MEDIA.TENCENT);
			break;
		case R.id.ll_shared_sina_wb:
			performShare(SHARE_MEDIA.SINA);
			break;
		}
	}

	

	private void performShare(SHARE_MEDIA platform) {
		final SHARE_MEDIA plat = platform;
		
		mController.postShare(activity, platform, new SnsPostListener() {

			@Override
			public void onStart() {
				/*CustomToast.show(activity, "分享平台："+ plat.name(), "开始分享");*/
			}
			
			@Override
			public void onComplete(SHARE_MEDIA platform, int eCode,
					SocializeEntity entity) {
				String showText = platform.toString();
				if (eCode == StatusCode.ST_CODE_SUCCESSED) {
					showText += "平台分享成功";
				} else {
					showText += "平台分享失败";
				}
				/*Toast.makeText(activity, showText, Toast.LENGTH_SHORT).show();*/
			}
		});
	}
	
//	private static final int ICON_WIDTH = 80;  
//    private static final int ICON_HEIGHT = 80;  
//    private static final float DEGREE_0 = 1.8f;  
//    private static final float DEGREE_1 = -2.0f;  
//    private static final float DEGREE_2 = 2.0f;  
//    private static final float DEGREE_3 = -1.5f;  
//    private static final float DEGREE_4 = 1.5f;  
    private static final int ANIMATION_DURATION = 250;
//    private static final int DELAY_ANIMATION_DURATION = 2000;
    
	private View ll_shared_wx_friend;
	private View rl_shared_friend;
	private View ll_shared_tx_wb;
	private View ll_shared_sina_wb; 
	private Handler handler;
	
	private void shakeAnimation(final View v) {  
       
		
		 final TranslateAnimation mra = new TranslateAnimation(0, 0, 0, -50);
		 final TranslateAnimation mrb = new TranslateAnimation(0, 0, -50, 0);
		
//        final RotateAnimation mra = new RotateAnimation(rotate, -rotate, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);  
//        final RotateAnimation mrb = new RotateAnimation(-rotate, rotate, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);  
  
//		mra.setInterpolator(new DecelerateInterpolator());
//		mrb.setInterpolator(new DecelerateInterpolator());
        mra.setDuration(ANIMATION_DURATION);  
        mrb.setDuration(ANIMATION_DURATION);  
  
        mra.setAnimationListener(new AnimationListener() {  
            @Override  
            public void onAnimationEnd(Animation animation) {  
                if (mNeedShake) {  
                    mra.reset();  
                    v.startAnimation(mrb);  
                }  
            }  
            @Override  
            public void onAnimationRepeat(Animation animation) {  
            }  
            @Override  
            public void onAnimationStart(Animation animation) {  
            }  
        });  
  
//        mrb.setAnimationListener(new AnimationListener() {  
//			@Override  
//            public void onAnimationEnd(Animation animation) {  
//                if (mNeedShake) {  
//                    //mrb.reset(); 
//                    //v.startAnimation(mra);  
//                    //v.startAnimation(null);
//                }  
//            }  
//            @Override  
//            public void onAnimationRepeat(Animation animation) {  
//            }  
//            @Override  
//            public void onAnimationStart(Animation animation) {  
//            }  
//        });  
        
        v.startAnimation(mra);  
    } 
	
	
	  
	 
}
