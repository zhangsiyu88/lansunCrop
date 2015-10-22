package com.lansun.qmyo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
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

	/*
	 * 以构造函数的形式将其传递进来，以便进行分享
	 */
	public void showPopwindow(View v, final Activity activity, String title,
			String content, String imageUrl) {
		this.activity = activity;
		
		configPlatforms();
		
		setShareContent(title, content, imageUrl);//分享的内容
		
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view = inflater.inflate(R.layout.dialog_shared, null);
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
		window.setOutsideTouchable(false);
		window.update();

		activity.getWindow().setAttributes(params);

		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);

		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		window.setBackgroundDrawable(dw);

		// 在底部显示
		window.showAtLocation(v, Gravity.BOTTOM, 0, 0);

		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				WindowManager.LayoutParams params = activity.getWindow()
						.getAttributes();
				params.alpha = 1f;
				activity.getWindow().setAttributes(params);
			}
		});
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
	 * 设置分享内容
	 */
	private void setShareContent(String title, String content, String imageUrl) {
		/**
		 * 微信朋友圈
		 */
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareContent(content);
		weixinContent.setTitle(title);
		weixinContent.setTargetUrl(GlobalValue.TARGET_URL);
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			weixinContent.setShareMedia(urlImage);
		}
		mController.setShareMedia(weixinContent);

		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(content);
		circleMedia.setTitle(title);
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			weixinContent.setShareMedia(urlImage);
		}
		// circleMedia.setShareMedia(uMusic);
		// circleMedia.setShareMedia(video);
		circleMedia.setTargetUrl(GlobalValue.TARGET_URL);
		mController.setShareMedia(circleMedia);

		// 设置QQ空间分享内容
		QZoneShareContent qzone = new QZoneShareContent();
		qzone.setShareContent(content);
		qzone.setTargetUrl(GlobalValue.TARGET_URL);
		qzone.setTitle(title);
		if (!TextUtils.isEmpty(imageUrl)) {
			UMImage urlImage = new UMImage(activity, imageUrl);
			weixinContent.setShareMedia(urlImage);
		}
		// qzone.setShareMedia(uMusic);
		mController.setShareMedia(qzone);

		// video.setThumb(new UMImage(getActivity(),
		// BitmapFactory.decodeResource(
		// getResources(), R.drawable.device)));

		QQShareContent qqShareContent = new QQShareContent();
		qqShareContent.setShareContent(content);
		qqShareContent.setTitle(title);
		// qqShareContent.setShareMedia(image);
		qqShareContent.setTargetUrl(GlobalValue.TARGET_URL);
		mController.setShareMedia(qqShareContent);
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
				CustomToast.show(activity, "分享平台："+ plat.name(), "开始分享");
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
				Toast.makeText(activity, showText, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
