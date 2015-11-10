package com.lansun.qmyo.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.lansun.qmyo.R;

/**
 * 体验dialog
 * 
 * @author bhxx
 * 
 */
@SuppressLint("ValidFragment")
public class ExperienceDialog extends DialogFragment {

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	.displayer(new FadeInBitmapDisplayer(300))
	.displayer(new RoundedBitmapDisplayer(10)).build();
	
	public String mCardId ;
	public String mCardHeadPhotoUrl ;
	public String mCardDesc ;
	public boolean mIsFirstEnter;
/*	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();*/
	/*
	 * 回调接口
	 */
	public interface OnConfirmListener {
		void confirm();
	}

	@InjectAll
	Views v;
	private OnConfirmListener listener;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		Button btn_expe_confirm, tv_expe_relogin;
		RecyclingImageView iv_exp_bankcard;
		TextView tv_expe_desc;
	}

	public ExperienceDialog() {
		super();
		
		// 获取当前推荐信用卡(随机分配的cardID)
		InternetConfig config = new InternetConfig();
		config.setKey(1);
		FastHttpHander.ajaxGet(GlobalValue.URL_BANKCARD_RECOMMEND, config, this);
	}

	public ExperienceDialog(String cardId2, String cardHeadPhotoUrl, String cardDesc, boolean isFirstEnter) {
		super();
		mCardHeadPhotoUrl = cardHeadPhotoUrl;
		mCardDesc = cardDesc;
		mCardId = cardId2;
		mIsFirstEnter = isFirstEnter;
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
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.dialog_experience, container);
		Handler_Inject.injectFragment(this, view);
		getDialog().setCancelable(false);
		getDialog().setCanceledOnTouchOutside(false);
	
		
		ImageLoader.getInstance().displayImage(mCardHeadPhotoUrl, v.iv_exp_bankcard,options);
		v.tv_expe_desc.setText(mCardDesc);
		
		return view;
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_expe_confirm://点击确定前去访问获取临时的exp_secret
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_TEMPORARY, config,this);*/
			FastHttpHander.ajax(GlobalValue.URL_AUTH_TEMPORARY, config, this);
			break;
			
		case R.id.tv_expe_relogin:
			RegisterFragment fragment = new RegisterFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			dismiss();
			break;
		}
	}

	int cardId = 0;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.isFirst = false;
				Secret secret = Handler_Json.JsonToBean(Secret.class,r.getContentAsString());
				App.app.setData("exp_secret", secret.getSecret());
				
				Log.i("临时用户拿到exp_secret","临时用户拿到的exp_secret为："+App.app.getData("exp_secret"));
				
				CustomToast.show(App.app, R.string.tip4,R.string.tiyan_cuccess_welcome);
				
				// 再次前往获取临时的access_token
				InternetConfig config = new InternetConfig();
				config.setKey(2);
				FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN+ secret.getSecret(), config, this);
				if (listener != null) {
					listener.confirm();
				}
				/*if(mIsFirstEnter){
					HomeFragment fragment = new HomeFragment(mIsFirstEnter);
					FragmentEntity entity = new FragmentEntity();
					entity.setFragment(fragment);
					EventBus.getDefault().post(entity);
				}*/
				
				
				dismiss();
				break;

			case 1:// 获取当前信用卡
				BankCardData data = Handler_Json.JsonToBean(BankCardData.class,
						r.getContentAsString());
				/*ImageLoader.getInstance().displayImage(data.getBankcard().getPhoto(), v.iv_exp_bankcard, options);*/
				
				ImageLoader.getInstance().displayImage(data.getBankcard().getPhoto(), v.iv_exp_bankcard,options);
				
				/*v.tv_expe_desc.setText(data.getBank().getName()+"\n"+data.getBankcard().getName());//给卡前面加上银行的名字*/				
				v.tv_expe_desc.setText(data.getBank().getName()+data.getBankcard().getName());
				
				cardId = data.getBankcard().getId();
				Log.i("进入首页后随机分配的银行卡的ID为：","进入首页后随机分配的银行卡的ID为："+cardId);
				
				
				break;
			case 2:// 更新 token
				Token token = Handler_Json.JsonToBean(Token.class,
						r.getContentAsString());
				App.app.setData("access_token", token.getToken());
				
				Log.i("临时用户拿到token","临时用户拿到的token为："+App.app.getData("access_token"));
				
				/**
				 * 添加银行卡
				 */
				InternetConfig config2 = new InternetConfig();
				config2.setKey(3);
				HashMap<String, Object> head = new HashMap<String, Object>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config2.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			
				Log.i("从选卡页进入首页后随机分配的银行卡的ID为：","进入首页后随机分配的银行卡的ID为："+	mCardId);
				if(cardId==0){
					cardId = Integer.valueOf(mCardId);
				}
				
				Log.i("作为体验用户实际提交上去的银行卡的ID为：","作为体验用户实际提交上去的银行卡的ID为："+cardId);
				params.put("bankcard_id", cardId + "");
				
				/*FastHttpHander.ajaxForm(GlobalValue.URL_BANKCARD_ADD, params,
						null, config2, this);*/
				FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD, params, config2, this);
				
				App.app.setData("ExperienceBankcardId", String.valueOf(cardId));
				Log.d("银行卡页的id", "首页处写入本地sp中的银行卡ID为：" + App.app.getData("ExperienceBankcardId"));
				
				//务必将卡提交上去才可以拿到体验用户的标志 ： isExperience = true
				App.app.setData("isExperience", "true");
				App.app.getData("isEmbrassStatus").equals("");//此时用户状态不再是尴尬的状态时
				
				HomeFragment fragment = new HomeFragment();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
				
				break;
			}
		} else {
			CustomToast.show(App.app, R.string.tip, R.string.loading_faild);
		}

	}
	
	
	
	
	
}
