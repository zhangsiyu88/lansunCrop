package com.lansun.qmyo.adapter;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.jpush.android.util.ac;

import com.amap.api.mapcore2d.ev;
import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.adapter.BankCardAdapter.ViewHolder;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secret;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.event.entity.BackEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BankCardDetailFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.MineBankcardFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SearchBankCardFragment;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExperienceDialog;
import com.lansun.qmyo.view.ExperienceDialog.OnConfirmListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.lansun.qmyo.R;

public class BankCardAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	private Fragment activity;
	private DisplayImageOptions options;
	private boolean isEmbarrassingStatue;
	private boolean addBankcardUnderLoginStatus = false;
	public static String selectCardId;
	
	public void setActivity(Fragment activity) {
		this.activity = activity;
	}

	public BankCardAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
		/*options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300))
				.displayer(new RoundedBitmapDisplayer(6)).build();
		*/
		
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300))
				.displayer(new RoundedBitmapDisplayer(10)).build();
		
	}
	public BankCardAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id,boolean isNotNeedChange) {
		super(listView, dataList, layout_id);
		
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300))
				.displayer(new RoundedBitmapDisplayer(10)).build();
		
		mIsNotChanged = isNotNeedChange;
		Log.d("sssssssss", mIsNotChanged+"");
		
	}

	/*
	 * getView()方法中调用
	 * @see com.android.pc.ioc.adapter.LazyAdapter#deal(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void deal(HashMap<String, String> data, ViewHolder viewHold,
			int position) {
		final String head = data.get("iv_bank_card_head");
		final String name = data.get("tv_bank_card_name");
		final String desc = data.get("tv_bank_card_desc");
		final String cardId = data.get("id");
		
		if (!TextUtils.isEmpty(head)) {
			download(viewHold.iv_bank_card_head, head);
		}
		if (!TextUtils.isEmpty(name)) {
			viewHold.tv_bank_card_name.setText(name);
		}
		if (!TextUtils.isEmpty(desc)) {
			viewHold.tv_bank_card_desc.setText(desc);
		}
		if (!hasAdd) {
			viewHold.iv_bank_card_add.setVisibility(View.GONE);
		}

		// 弹出详情框
		viewHold.iv_bank_card_head.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				BankCardDetailFragment fragment = new BankCardDetailFragment();
				Bundle args = new Bundle();
				args.putString("id", cardId);
				Log.i("弹出来的银行卡详情页","此卡的id为:"+cardId);
				fragment.setArguments(args);
				fragment.show(activity.getFragmentManager(), "bankCardDetail");
			}
		});

		//条目中的那个绿色的一小块区域的点击添加
		viewHold.iv_bank_card_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ("true".equals(App.app.getData("isExperience"))) {//体验用户状态下
					
					RegisterFragment fragment = new RegisterFragment();
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
					
				}else if(!TextUtils.isEmpty(App.app.getData("secret"))&&!TextUtils.isEmpty(App.app.getData("access_token"))){//登录状态下
					DialogUtil.createTipAlertDialog(context,R.string.add_bankCard,
							new TipAlertDialogCallBack() {
								

								@Override
								public void onPositiveButtonClick(
										DialogInterface dialog, int which) {
									InternetConfig config = new InternetConfig();
									config.setKey(0);
									HashMap<String, Object> head = new HashMap<>();
									head.put("Authorization", "Bearer "
											+ App.app.getData("access_token"));
									config.setHead(head);
									LinkedHashMap<String, String> params = new LinkedHashMap<>();
									params.put("bankcard_id", cardId);
									
									//TODO
									if(mIsNotChanged){//不需要改变的标签值，即此时的点击操作：是将原始卡替换为欲添卡，但保存于本地的sp中的原始卡数据不需要更改
										addBankcardUnderLoginStatus = true;
									}
									/*else{//此时是我的银行卡页中列表的点击事件，那么需要将原始卡替换掉
										App.app.setData("MainBankcard",cardId);
									}*/
									
									FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD,params, config,BankCardAdapter.this);
									/*FastHttpHander.ajaxForm(GlobalValue.URL_BANKCARD_ADD,params, null, config,BankCardAdapter.this);*/
									Log.i("警报警报！！","添加进来的卡的id为： "+ cardId);
									
									Handler_Inject.injectFragment(BankCardAdapter.this, null);
									dialog.dismiss();
								}

								@Override
								public void onNegativeButtonClick(
										DialogInterface dialog, int which) {
										dialog.dismiss();
								}
							});

				} 
				/*else if(TextUtils.isEmpty(App.app.getData("secret"))
						&&TextUtils.isEmpty(App.app.getData("access_token"))
						&&TextUtils.isEmpty(App.app.getData("exp_secret"))){//三无状态下： 第一次安装程序  或  退出登录后
					DialogUtil.createTipAlertDialog(context,R.string.add_bankCard,
							new TipAlertDialogCallBack() {
								@Override
								public void onPositiveButtonClick(
										DialogInterface dialog, int which) {
									//
									InternetConfig config = new InternetConfig();
									config.setKey(0);
									HashMap<String, Object> head = new HashMap<>();
									head.put("Authorization", "Bearer "
											+ App.app.getData("access_token"));
									config.setHead(head);
									LinkedHashMap<String, String> params = new LinkedHashMap<>();
									params.put("bankcard_id", cardId);
									
									FastHttpHander.ajaxForm(
											GlobalValue.URL_BANKCARD_ADD,
											params, null, config,
											BankCardAdapter.this);
									FastHttpHander.ajax(
											GlobalValue.URL_BANKCARD_ADD,
											params,  config,
											BankCardAdapter.this);
									
									
									Handler_Inject.injectFragment(
											BankCardAdapter.this, null);
									dialog.dismiss();
								}

								@Override
								public void onNegativeButtonClick(
										DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});

				}*/
				else if (TextUtils.isEmpty(App.app.getData("exp_secret"))
						&& TextUtils.isEmpty(App.app.getData("secret"))
						&& GlobalValue.isFirst
						&& GlobalValue.user == null){//通过搜索卡页第一次进来，即从入口处进入的内容
					
				/*	Boolean isFirstEnter = true;
					ExperienceDialog dialog = new ExperienceDialog(cardId,head,desc,isFirstEnter);//这么个体验的对话框，需要单独在其内部设置点击响应事件
					//进来首先就弹出对话框
					dialog.setOnConfirmListener(new OnConfirmListener() {
						@Override
						public void confirm() {
//							v.rl_bg.setPressed(true);//这是“体验”二字后面的背景绿色和灰色选择器，那么为了取消点击效果，则在此将选择器设置为  点击和非点击都为 统一效果
//							v.rl_top_bg.setPressed(true);
//							v.tv_home_experience.setVisibility(View.VISIBLE);
//							v.tv_top_home_experience.setVisibility(View.VISIBLE);
//							v.iv_card.setVisibility(View.GONE);
//							v.iv_top_card.setVisibility(View.GONE);
						}
					});
					dialog.show(activity.getFragmentManager() , "experience");*/
					
					//弹出对话框进行设
					DialogUtil.createTipAlertDialog(context,R.string.add_bankCard,new TipAlertDialogCallBack() {
								@Override
								public void onPositiveButtonClick(DialogInterface dialog, int which) {
									
									selectCardId = cardId;
									InternetConfig config = new InternetConfig();
									config.setKey(2);
									/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_TEMPORARY, config,this);*/
									FastHttpHander.ajax(GlobalValue.URL_AUTH_TEMPORARY, config, BankCardAdapter.this);
									Handler_Inject.injectFragment(BankCardAdapter.this, null);
									dialog.dismiss();
								}

								@Override
								public void onNegativeButtonClick(
										DialogInterface dialog, int which) {
										dialog.dismiss();
								}
							});
					
					
				}else if(TextUtils.isEmpty(App.app.getData("exp_secret"))
						&& !TextUtils.isEmpty(App.app.getData("secret"))
						&& !TextUtils.isEmpty(App.app.getData("access_token"))
						&& !TextUtils.isEmpty(App.app.getData("access_token"))
						&& GlobalValue.isFirst
						&& GlobalValue.user != null){               
				
					/*
					 * 刚退出登录时：
					 * GlobalValue.user = null;
					   GlobalValue.isFirst = true;//即为三无状态，那么就需要成为是第一次进入的用户状态，也就会是需要自己加卡那个页面
					   clearTokenAndSercet();
					   
					     注册并登陆成功后：
					         拥有了secret，access_token 和 user对象 ，并且非第一次进入程序GlobalValue.user != null 
					 */
					
					DialogUtil.createTipAlertDialog(context,R.string.add_bankCard,new TipAlertDialogCallBack() {
						
						@Override
						public void onPositiveButtonClick(DialogInterface dialog, int which) {
							
							InternetConfig config = new InternetConfig();
							config.setKey(0);
							HashMap<String, Object> head = new HashMap<>();
							head.put("Authorization", "Bearer "
									+ App.app.getData("access_token"));
							config.setHead(head);
							LinkedHashMap<String, String> params = new LinkedHashMap<>();
							params.put("bankcard_id", cardId);
							
							/*FastHttpHander.ajaxForm(GlobalValue.URL_BANKCARD_ADD,params, null, config,BankCardAdapter.this);*/
							FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD,  params, config, BankCardAdapter.this);
							Log.i("警报警报！！","添加进来的卡的id为： "+ cardId);
							
							Handler_Inject.injectFragment(BankCardAdapter.this, null);
							 
							isEmbarrassingStatue = true;
							dialog.dismiss();
						}

						@Override
						public void onNegativeButtonClick(DialogInterface dialog, int which) {
								dialog.dismiss();
						}
					});
					
				}
				
				
			}
		});
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0://TODO
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(context, context.getString(R.string.tip),"添加成功");
					/*BackEntity event = new BackEntity();
					EventBus.getDefault().post(event);*/
					if(isEmbarrassingStatue){
						HomeFragment fragment = new HomeFragment();
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
						return;
					}
					
					
					if(mIsNotChanged){//不需要改变的标签值，即此时的点击操作：是将原始卡替换为欲添卡，但保存于本地的sp中的原始卡数据不需要更改
						if(addBankcardUnderLoginStatus){
							Bundle bundle = new Bundle();
							bundle.putBoolean("isFromInsertBankcardAdapterPage", addBankcardUnderLoginStatus);
							MineBankcardFragment fragment = new MineBankcardFragment();
							fragment.setArguments(bundle);
							FragmentEntity event = new FragmentEntity();
							event.setFragment(fragment);
							EventBus.getDefault().post(event);
							return;
						}
					}else{//此时是我的银行卡页中列表的点击事件，那么需要将原始卡替换掉
						/*App.app.setData("MainBankcard",selectCardId);*/
						
					}
					
					
					MineBankcardFragment fragment = new MineBankcardFragment();
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
					
					
				} else {
					CustomToast.show(context, context.getString(R.string.tip),"卡种已重复");
				}
				break;
				
			case 2:
				GlobalValue.isFirst = false;
				Secret secret = Handler_Json.JsonToBean(Secret.class,r.getContentAsString());
				App.app.setData("exp_secret", secret.getSecret());
				Log.i("临时用户拿到exp_secret","临时用户拿到的exp_secret为："+App.app.getData("exp_secret"));
				CustomToast.show(App.app, R.string.tip4,R.string.tiyan_cuccess_welcome);
				
				// 再次前往获取临时的access_token
				InternetConfig config = new InternetConfig();
				config.setKey(3);
				FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN+ secret.getSecret(), config, this);
				
				break;
			case 3:// 更新 token
				Token token = Handler_Json.JsonToBean(Token.class,
						r.getContentAsString());
				App.app.setData("access_token", token.getToken());
				
				Log.i("临时用户拿到token","临时用户拿到的token为："+App.app.getData("access_token"));
				/**
				 * 添加银行卡
				 */
				InternetConfig config2 = new InternetConfig();
				config2.setKey(4);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization","Bearer " + App.app.getData("access_token"));
				config2.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<>();
			
				/*Log.i("从选卡页进入首页后随机分配的银行卡的ID为：","进入首页后随机分配的银行卡的ID为："+	mCardId);
				if(cardId==0){
					cardId = Integer.valueOf(mCardId);
				}
				*/
				/*Log.i("作为体验用户实际提交上去的银行卡的ID为：","作为体验用户实际提交上去的银行卡的ID为："+cardId);*/
			
				
				params.put("bankcard_id", selectCardId);//这个selectCardId是点击选中的时候传递过来的
				
				/*FastHttpHander.ajaxForm(GlobalValue.URL_BANKCARD_ADD, params,
						null, config2, this);*/
				
				FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD, params,config2, this);
				
				//务必将卡提交上去才可以拿到体验用户的标志 ： isExperience = true
				App.app.setData("isExperience", "true");
				
				HomeFragment fragment = new HomeFragment();
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
				
				break;
			}
		} else {
			 	CustomToast.show(context, "网络故障", "网络错误");
		}

	}

	@Override
	public void download(ImageView view, String url) {
		/*super.download(view, url);*/
		ImageLoader.getInstance().displayImage(url, view, options);
	}

	public class ViewHolder {
		@InjectView
		private RecyclingImageView iv_bank_card_head, iv_bank_card_add;
		@InjectView
		private TextView tv_bank_card_name, tv_bank_card_desc;
	}

	boolean hasAdd = true;
	private boolean mIsNotChanged;
	


	public void setHasAdd(boolean b) {
		this.hasAdd = b;
	}

}
