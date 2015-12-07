package com.lansun.qmyo.utils.swipe;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


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
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.BankCardAdapter.ViewHolder;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secret;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BankCardDetailFragment;
import com.lansun.qmyo.fragment.MineBankcardFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeLayout.SwipeListener;
import com.lansun.qmyo.view.CustomToast;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;



public class SwipeListSearchBankcardAdapter extends  BaseAdapter{//LazyAdapter<HashMap<String, String>, ViewHolder>

/*	public SwipeListMineBankcardAdapter(Activity activity,ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
		mList = dataList;
		mContext = listView.getContext();
	}*/

	private Context mContext;
	private LayoutInflater mInflater ;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	private ArrayList<HashMap<String, String>> mList;
	private int deletePosition;
	
	
	//private Fragment activity;
	private boolean isEmbarrassingStatue;
	private boolean addBankcardUnderLoginStatus = false;
	private boolean mIsFromRigisterFragToMyBankcardFrag = false;
	public static String selectCardId;
	public FromNetCallBack  mFromNetCallback;
	
	
	boolean hasAdd = true;
	private boolean mIsNotChanged;
	private boolean mIsFromRegisterAndHaveNoBankcard = false;
	private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
			.cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();
	private Fragment mFragment;
	private boolean isSlide = true;
	

	public void setHasAdd(boolean b) {
		this.hasAdd = b;
	}


	public void setFragment(Fragment fragment) {
		this.mFragment = fragment;
	}
	

	public SwipeListSearchBankcardAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
	}

	public SwipeListSearchBankcardAdapter(Activity activity,
			ArrayList<HashMap<String, String>> dataList) {//调用的是 此构造方法
		super();
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
	}
	
	
	public SwipeListSearchBankcardAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		
		
	}
	public SwipeListSearchBankcardAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id, boolean isNotNeedChange) {
	
		mIsNotChanged = isNotNeedChange;
		Log.d("mIsNotChanged", mIsNotChanged+"");
		
	}
	
	public SwipeListSearchBankcardAdapter(Activity activity,
			ArrayList<HashMap<String, String>> dataList, int layout_id, boolean isNotNeedChange) {
		
		mIsNotChanged = isNotNeedChange;
		
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
		Log.d("mIsNotChanged", mIsNotChanged+"");
		
	}

	public SwipeListSearchBankcardAdapter(ListView listView,ArrayList<HashMap<String, String>> dataList,
			int layout_id,  boolean isNotNeedChange,boolean isFromRegisterAndHaveNoBankcard) {
		
		mIsNotChanged = isNotNeedChange;
		mIsFromRegisterAndHaveNoBankcard = isFromRegisterAndHaveNoBankcard;
	}

	
	
	public SwipeListSearchBankcardAdapter(Activity activity,
			ArrayList<HashMap<String, String>> dataList,
			int activityBankCardItemSwipe, boolean isNotNeedChange,
			boolean isFromRegisterAndHaveNoBankcard) {
		
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
		mIsNotChanged = isNotNeedChange;
		mIsFromRegisterAndHaveNoBankcard = isFromRegisterAndHaveNoBankcard;
		
	}


	public SwipeListSearchBankcardAdapter(boolean b, Activity activity,
			ArrayList<HashMap<String, String>> dataList,
			int activityBankCardItemSwipe, boolean isNotNeedChange) {
		
		mIsNotChanged = isNotNeedChange;
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		this.isSlide = b;
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
	}
	
	
	


	public SwipeListSearchBankcardAdapter(boolean b, Activity activity,
			ArrayList<HashMap<String, String>> dataList,
			int activityBankCardItemSwipe, boolean isNotNeedChange,
			boolean isFromRegisterAndHaveNoBankcard) {
		
		this.isSlide = b;
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		mIsNotChanged = isNotNeedChange;
		mIsFromRegisterAndHaveNoBankcard = isFromRegisterAndHaveNoBankcard;
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
		
		
	}


	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		HashMap<String, String> data = mList.get(position);
		final String head = data.get("iv_bank_card_head");
		final String name = data.get("tv_bank_card_name");
		final String desc = data.get("tv_bank_card_desc");
		final String cardId = data.get("id");
		mPosition = position;
		
		ViewHolder viewHold;
		if (convertView != null) {
			viewHold = (ViewHolder) convertView.getTag();
		}else {
			convertView = (SwipeLayout)mInflater.inflate(R.layout.activity_bank_card_item_swipe_search, null);
			
			//Handler_Inject.injectFragment(this, convertView);
			viewHold = ViewHolder.fromValues(convertView);
			convertView.setTag(viewHold);
		}
		
		final SwipeLayout view = (SwipeLayout) convertView;
		Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, convertView);
		
		view.close(false, false);
		
//		view.getFrontView().setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				
//				final HashMap<String, String> map = mList.get(position);
//				DialogUtil.createTipAlertDialog(mContext, R.string.is_switch_card,
//						new TipAlertDialogCallBack() {
//					
//					private String bankCardId;
//	 
//					@Override
//					public void onPositiveButtonClick(
//							DialogInterface dialog, int which) {
//						
//						bankCardId = map.get("id");
//						InternetConfig config = new InternetConfig();
//						config.setKey(6);
//						HashMap<String, Object> head = new HashMap<>();
//						head.put("Authorization","Bearer " + App.app.getData("access_token"));
//						config.setHead(head);
//						LinkedHashMap<String, String> params = new LinkedHashMap<>();
//						params.put("bankcard_id", bankCardId);
//						
//						
//						//使用post方式去提交，是选中其中的卡作为选中卡 
//					    FastHttpHander.ajax(GlobalValue.URL_SELECT_BANKCARD, params,config, SwipeListSearchBankcardAdapter.this);
//						
//						//mList.clear();
//						//this = null;
//						
//						Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, view);
//						
//						dialog.dismiss();
//					}
//					@Override
//					public void onNegativeButtonClick(
//							DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//					
//				}
//		});
		
		
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
		
		
		if(isSlide ){
			view.setSwipeListener(mSwipeListener);
		}else{
			/*view.setSwipeListener(null);*/
			/*view.close();*/
			view.setGestureValid(true);
		}
//		view.setSwipeListener(mSwipeListener);
		
		
		
		
		// 弹出详情框
		viewHold.iv_bank_card_head.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				BankCardDetailFragment fragment = new BankCardDetailFragment();
				Bundle args = new Bundle();
				args.putString("id", cardId);
				Log.i("弹出来的银行卡详情页","此卡的id为:"+cardId);
				fragment.setArguments(args);
				fragment.show(mFragment.getFragmentManager(), "bankCardDetail");
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
					DialogUtil.createTipAlertDialog(mContext,R.string.add_bankCard,
							new TipAlertDialogCallBack() {

								@Override
								public void onPositiveButtonClick(
										DialogInterface dialog, int which) {
									InternetConfig config = new InternetConfig();
									config.setKey(0);
									HashMap<String, Object> head = new HashMap<String, Object>();
									head.put("Authorization", "Bearer "
											+ App.app.getData("access_token"));
									config.setHead(head);
									LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
									params.put("bankcard_id", cardId);
									
									//TODO
									if(mIsNotChanged){//不需要改变的标签值，即此时的点击操作：是将原始卡替换为欲添卡，但保存于本地的sp中的原始卡数据不需要更改
										addBankcardUnderLoginStatus = true;
									}
									/*else{//此时是我的银行卡页中列表的点击事件，那么需要将原始卡替换掉
										App.app.setData("MainBankcard",cardId);
									}*/
									if(mIsFromRegisterAndHaveNoBankcard||App.app.getData("isEmbrassStatus").equals("true")){
										isEmbarrassingStatue = true;
									}
									
									
									FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD,params, config,SwipeListSearchBankcardAdapter.this);
									/*FastHttpHander.ajaxForm(GlobalValue.URL_BANKCARD_ADD,params, null, config,BankCardAdapter.this);*/
									Log.i("警报警报！！","添加进来的卡的id为： "+ cardId);
									
									Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
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
					DialogUtil.createTipAlertDialog(mContext,R.string.add_bankCard,new TipAlertDialogCallBack() {
								@Override
								public void onPositiveButtonClick(DialogInterface dialog, int which) {
									
									selectCardId = cardId;
									InternetConfig config = new InternetConfig();
									config.setKey(2);
									/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_TEMPORARY, config,this);*/
									FastHttpHander.ajax(GlobalValue.URL_AUTH_TEMPORARY, config, SwipeListSearchBankcardAdapter.this);
									Handler_Inject.injectFragment(this, null);
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
					
						DialogUtil.createTipAlertDialog(mContext,R.string.add_bankCard,new TipAlertDialogCallBack() {
							
							@Override
							public void onPositiveButtonClick(DialogInterface dialog, int which) {
								
								InternetConfig config = new InternetConfig();
								config.setKey(0);
								HashMap<String, Object> head = new HashMap<String, Object>();
								head.put("Authorization", "Bearer "
										+ App.app.getData("access_token"));
								config.setHead(head);
								LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
								params.put("bankcard_id", cardId);
								
								FastHttpHander.ajax(GlobalValue.URL_BANKCARD_ADD,  params, config, this);
								Log.i("警报警报！！","添加进来的卡的id为： "+ cardId);
								
								Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
								 
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
			
			/**
			 * 点击按钮,
			 */
//			mHolder.mButtonCall.setTag(position);
//			mHolder.mButtonCall.setOnClickListener(onActionClick);

			viewHold.mButtonDel.setTag(position);//给这个mHolder.mButtonDel的View设置上Tag标签，为了在后面使用时便于去找到
			viewHold.mButtonDel.setOnClickListener(onActionClick);
			return view;
		}
	
	

	 @InjectHttp
	private void result(ResponseEntity r) {
		 
		 LogUtils.toDebugLog("r的返回值为： ", "r的返回值为："+r.toString());
		 
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0://TODO
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(mContext, mContext.getString(R.string.tip),"添加成功");
					
					App.app.setData("isEmbrassStatus", "");//这儿已经表明现在用户将不再是尴尬的异常登录状态了
					
					/*BackEntity event = new BackEntity();
					EventBus.getDefault().post(event);*/
					if(isEmbarrassingStatue){
						
						/*HomeFragment fragment = new HomeFragment();*/
						MainFragment fragment = new MainFragment(0);
						FragmentEntity event = new FragmentEntity();
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
						return;
					}
					
					if(mIsNotChanged){//不需要改变的标签值，即此时的点击操作：是将原始卡替换为欲添卡，但保存于本地的sp中的原始卡数据不需要更改
						if(addBankcardUnderLoginStatus){
							Bundle bundle = new Bundle();//TODO
							bundle.putBoolean("isFromInsertBankcardAdapterPage", addBankcardUnderLoginStatus);
							
							//这个标签是当从登录注册页登陆回来时，此时已经刷新了MineBankcardFragment页面，且此时为登录状态下，再跑去添加搜索银行卡页，但是返回后却不进行替卡操作，此时点击back键，仍然是需要刷新对应界面的
							if(App.app.getData("isFromRigisterFragToMyBankcardFrag").equals("true")){
								bundle.putBoolean("isFromRigisterFragToMyBankcardFrag", true );
								App.app.setData("isFromRigisterFragToMyBankcardFrag","");
							}
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
					CustomToast.show(mContext, mContext.getString(R.string.tip),"卡种已重复");
					
					//同时需要补救措施！
					InternetConfig config = new InternetConfig();
					config.setKey(7);
					HashMap<String, Object> head = new HashMap<>();
					head.put("Authorization", "Bearer "+ App.app.getData("access_token"));
					config.setHead(head);
					LinkedHashMap<String, String> params = new LinkedHashMap<>();
					
					Log.i("MainBankCard的值", "MainBankCard的值：" +App.app.getData("MainBankcard"));
					params.put("bankcard_id", App.app.getData("MainBankcard"));
					Log.d("原始主卡的id为:  ", "原始主卡的id为:  "+App.app.getData("MainBankcard"));
					
					//在这里从搜索银行卡页回来，自己跑过去请求了一下，进行了选卡操作！！！
					FastHttpHander.ajax(GlobalValue.URL_SELECT_BANKCARD,  params, config,this);
					Handler_Inject.injectFragment(SwipeListSearchBankcardAdapter.this, null);
					
					
				}
				break;
				
			case 2:
				GlobalValue.isFirst = false;
				Secret secret = Handler_Json.JsonToBean(Secret.class,r.getContentAsString());
				App.app.setData("exp_secret", secret.getSecret());
				Log.i("临时用户拿到exp_secret","临时用户拿到的exp_secret为："+App.app.getData("exp_secret"));
				CustomToast.show(App.app, R.string.tip4,R.string.tiyan_cuccess_welcome);
				
				App.app.setData("ExperienceBankcardId",selectCardId);
				
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
				HashMap<String, Object> head = new HashMap<String, Object>();
				head.put("Authorization","Bearer " + App.app.getData("access_token"));
				config2.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			
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
				
				/*HomeFragment fragment = new HomeFragment();*/
				MainFragment fragment = new MainFragment(0);
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
				break;
			case 6:
				//由实现这个接口回调的子类（即：MineBankCardFragment）去执行
				mFromNetCallback.fromNetCallBck(r);
				LogUtils.toDebugLog("OK", "说明可以拿到网络访问的回复");
				break;
			case 7 :
				if ("true".equals(r.getContentAsString())) {
					/*CustomToast.show(mContext, R.string.tip,"主卡已恢复！");*/
				} else {
					/*CustomToast.show(mContext, "网络异常","主卡恢复失败，请再次尝试");*/
				}
			   break;
			}
		} else {
			 	CustomToast.show(mContext, "网络故障", "网络错误");
		}
	
	}
	

	OnClickListener onActionClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer p = (Integer) v.getTag();//这个View对应的tag是绑定的position的值
			int id = v.getId();
			if (id == R.id.bt_call) {
				closeAllLayout();
				//暂时不提供实际操作的方法
				
			} else if (id == R.id.bt_delete) {
				closeAllLayout();
				Utils.showToast(mContext, "删除此卡！");
				
				deletePosition = p;
				
				HashMap<String, String> map = mList.get(deletePosition);
				String deleteId = map.get("id");

				mList.remove(deletePosition);//这就是为什么你在点删除后,可以看到立马删除的效果体现出来,
				//但实际未删除服务器上的数据

				HttpUtils httpUtils = new HttpUtils();
				RequestParams requestParams = new RequestParams();
				requestParams.addHeader("Authorization","Bearer " + App.app.getData("access_token"));
				httpUtils.send(HttpMethod.DELETE, GlobalValue.URL_BANKCARD_DELETE + deleteId, requestParams,new RequestCallBack<String>(){
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}
					@Override
					public void onSuccess(ResponseInfo arg0) {
						if ("true".equals(arg0.result)) {
							//很明显,前面的删除银行卡操作是没有起到作用的,若有作用至少是会有toast提醒
							CustomToast.show(mContext, mContext.getString(R.string.tip),
									mContext.getString(R.string.delete_success));
						} else {
							System.out.println("获取到返回值,但显示是删除失败!");
							CustomToast.show(mContext, mContext.getString(R.string.tip),
									mContext.getString(R.string.delete_faild));
						}
					}
				});
				notifyDataSetChanged();
			}
		}
	};
	
	
	SwipeListener mSwipeListener = new SwipeListener() {
		@Override
		public void onOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onOpen");
			mUnClosedLayouts.add(swipeLayout);
		}

		@Override
		public void onClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onClose");
			mUnClosedLayouts.remove(swipeLayout);
		}

		@Override
		public void onStartClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartClose");
		}

		@Override
		public void onStartOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartOpen");
			closeAllLayout();
			mUnClosedLayouts.add(swipeLayout);
		}

	};
	private int mPosition;
	
	public int getUnClosedCount(){
		return mUnClosedLayouts.size();
	}
	
	public void closeAllLayout() {
		if(mUnClosedLayouts.size() == 0)
			return;
		
		for (SwipeLayout l : mUnClosedLayouts) {
			l.close(true, false);
		}
		mUnClosedLayouts.clear();
	}
	
	
	static class ViewHolder {

		public Button mButtonCall;
		public ImageButton mButtonDel;
		
		/*@InjectView*/
		private RecyclingImageView iv_bank_card_head;
		/*@InjectView*/
		private TextView tv_bank_card_name, tv_bank_card_desc;
		/*@InjectView*/
		private View iv_bank_card_add;
		
		private ViewHolder(
				Button mButtonCall,
				ImageButton mButtonDel, 
				RecyclingImageView iv_bank_card_head,
				View iv_bank_card_add, 
				TextView tv_bank_card_name, TextView tv_bank_card_desc){
					super();
					this.mButtonCall = mButtonCall;
					this.mButtonDel = mButtonDel;
					this.iv_bank_card_head = iv_bank_card_head;
					this.iv_bank_card_add = iv_bank_card_add;
					this.tv_bank_card_name = tv_bank_card_name;
					this.tv_bank_card_desc = tv_bank_card_desc;
			}
			public static ViewHolder fromValues(View view) {
				return new ViewHolder(
					(Button) view.findViewById(R.id.bt_call),
					(ImageButton) view.findViewById(R.id.bt_delete),
					(RecyclingImageView) view.findViewById(R.id.iv_bank_card_head),
					(View) view.findViewById(R.id.iv_bank_card_add),
					(TextView) view.findViewById(R.id.tv_bank_card_name),
					(TextView) view.findViewById(R.id.tv_bank_card_desc));
			}
	}
	
	public void download(ImageView view, String url) {
		ImageLoader.getInstance().displayImage(url, view, this.options);
	}

	
	public void setFromNetCallBack(FromNetCallBack fromNetCallback){
		this.mFromNetCallback = fromNetCallback;
	}
	
	public interface FromNetCallBack{
	   public void fromNetCallBck();
	   public void fromNetCallBck(ResponseEntity r);
	}
	
}