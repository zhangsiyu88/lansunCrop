package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.TimeButton;
import com.android.pc.ioc.view.TimeTextView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.lansun.qmyo.adapter.HomeListAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.MainActivity;
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
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 分享app界面
 * 
 * @author bhxx
 * 
 */
public class SharedFragment extends BaseFragment {
	@InjectAll
	Views v;
	private UMSocialService mController = UMServiceFactory.getUMSocialService(GlobalValue.DESCRIPTOR);
	private String targetUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.lansun.qmyo";

	class Views {
		private TextView tv_activity_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View ll_shared_wx_friend, rl_shared_friend,
				fl_comments_right_iv, ll_shared_qq_friend, ll_shared_tx_wb,
				ll_shared_sina_wb, tv_shared_cancle;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		
		//getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		View rootView = inflater.inflate(R.layout.activity_shared, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.shared_app, null, 0);
		
		configPlatforms();
		
		/*setShareContent(getString(R.string.app_name),getString(R.string.shared_comtent), R.drawable.share_app);*/
		/*setShareContent(getString(R.string.app_name),getString(R.string.the_new_shared_comtent), R.drawable.share_app);*/
		setShareContent("推荐一个找优惠的应用","在这里可以找到世界各地最惊喜的活动，你也试试", R.drawable.icon_redpack1);
		
		
	}

	/**
	 * 配置分享平台参数</br>
	 */
	private void configPlatforms() {
		
		//添加QQ、QZone平台
		addQZoneQQPlatform();
		// 添加微信、微信朋友圈平台
		addWXPlatform();
	}

	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
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
	 * 设置分享的内容
	 */
	private void setShareContent(String title, String content, int resId) {
		/**
		 * 微信好友
		 */
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareContent(content);
		weixinContent.setTitle(title);
		
		
		weixinContent.setTargetUrl(targetUrl);//--->需要修改为对应的软件下载网址
		
		UMImage urlImage = new UMImage(activity, resId);
		if (!TextUtils.isEmpty(String.valueOf(resId)))
			 weixinContent.setShareImage(new UMImage(this.activity, resId));
		
		weixinContent.setShareMedia(urlImage);
		mController.setShareMedia(weixinContent);

		/**
		 * 微信朋友圈
		 */
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(content);
		
		/*circleMedia.setTitle(title +"-" +content);*/
		circleMedia.setTitle(content);
		
		
		urlImage = new UMImage(activity, resId);
		//weixinContent.setShareMedia(urlImage);  //写错啦！
		// circleMedia.setShareMedia(uMusic);
		// circleMedia.setShareMedia(video);
		
		if (!TextUtils.isEmpty(String.valueOf(resId)))
			circleMedia.setShareImage(new UMImage(this.activity, resId));
		circleMedia.setTargetUrl(targetUrl);//--->需要修改为对应的软件下载网址
		mController.setShareMedia(circleMedia);

		// 设置QQ空间分享内容
		QZoneShareContent qzone = new QZoneShareContent();
		qzone.setShareContent(content);
		qzone.setTargetUrl(GlobalValue.TARGET_URL);
		qzone.setTitle(title);
		urlImage = new UMImage(activity, resId);
		weixinContent.setShareMedia(urlImage);
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
		
		
	    /**
		 * 网页版腾讯微博的分享
		 */
	    TencentWbShareContent tencentWbShareContent = new TencentWbShareContent();
	    tencentWbShareContent.setTitle(title);
	    tencentWbShareContent.setShareContent(content+"  "+targetUrl);
	    if (!TextUtils.isEmpty(String.valueOf(resId)))
	    	tencentWbShareContent.setShareImage(new UMImage(this.activity, resId));
	   /* tencentWbShareContent.setShareImage(new UMImage(this.activity, resId));*/
	    this.mController.setShareMedia(tencentWbShareContent);
	    
	    
	    /**
	     * 网页版新浪微博的分享
	     */
	    SinaShareContent sinaShareContent = new SinaShareContent();
	    sinaShareContent.setTitle(title);
	    sinaShareContent.setShareContent(content+"  "+targetUrl);
	    if (!TextUtils.isEmpty(String.valueOf(resId)))
	    	sinaShareContent.setShareImage(new UMImage(this.activity, resId));
	    this.mController.setShareMedia(sinaShareContent);
	}

	public void click(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.ll_shared_wx_friend:
			performShare(SHARE_MEDIA.WEIXIN);
			break;
		case R.id.rl_shared_friend:
			performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
			break;
		/*case R.id.ll_shared_qq_friend:
			performShare(SHARE_MEDIA.QQ);
			break;*/
		case R.id.ll_shared_tx_wb:
			
			try{
				performShare(SHARE_MEDIA.TENCENT);
			}catch(Exception e){
				System.out.println("错误信息如下:  "+e.toString());
			    MainActivity mainActivity = new MainActivity();
				mainActivity.startFragmentAdd(new SharedFragment());
			}
			break;
		case R.id.ll_shared_sina_wb:
			try{
				performShare(SHARE_MEDIA.SINA);
			}catch(Exception e){
				System.out.println("错误信息如下:  "+e.toString());
			    MainActivity mainActivity = new MainActivity();
				mainActivity.startFragmentAdd(new SharedFragment());
			}
			break;
		}
	}

	private void addQZoneQQPlatform() {
		String appId = GlobalValue.QZONE_APP_ID;
		String appKey = GlobalValue.QZONE_APP_KEY;
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activity, appId,
				appKey);
		qqSsoHandler.setTargetUrl(GlobalValue.TARGET_URL);
		qqSsoHandler.addToSocialSDK();

		// 添加QZone平台
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity, appId,appKey);
		qZoneSsoHandler.addToSocialSDK();
	}

	private void performShare(SHARE_MEDIA platform) {
		mController.postShare(activity, platform, new SnsPostListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int eCode,SocializeEntity entity) {
				/*String showText = platform.toString();
				
				if (eCode == StatusCode.ST_CODE_SUCCESSED) {
					showText += "平台分享成功";
				} else {
					showText += "平台分享失败";
				}
				Toast.makeText(activity, showText, Toast.LENGTH_SHORT).show();*/
			}
		});
	}
}
