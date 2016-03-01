package com.lansun.qmyo.fragment;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
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
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.ErrorInfo;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryCardShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryInvestmentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryLifeShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryPartyShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryShoppingShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryStudentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryTravelShowFragment;
import com.lansun.qmyo.utils.CommitStaticsinfoUtils;
import com.lansun.qmyo.utils.FixedSpeedScroller;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CloudView;
import com.lansun.qmyo.view.CustomToast;
import com.ryanharter.viewpager.NoTouchViewPager;
import com.ryanharter.viewpager.PagerAdapter;
import com.ryanharter.viewpager.ViewPager;

/**
 * 注册界面
 * 
 * @author 
 * 
 */
@SuppressLint("InflateParams") 
public class RegisterFragment extends BaseFragment{
	@InjectAll
	Views v;
	private String code;
	private String phone;
	public boolean isReset = false;
	private ErrorInfo errorInfo;
	private ErrorInfo errorInfo2;
	private boolean isHasExpSecretWhenClickToRegister = true;
	private boolean isJustLogin;
	private boolean isFromMyBankcardFragToRigisterFrag = false;
	private boolean initTime = true;

	private boolean mIsFromHome = false;
	private boolean mIsFromEightPart = false;
	private boolean mIsFromNewPart = false;
	private boolean mIsFromRigisterFragToMyBankcardFrag = false;
	private int type;
	private int mInitType;
	private String fragment_name;
	private boolean isResetPsw;
	private Token token;
	/**
	 * 注意下面的adapter的类型为： SecretaryBgPagerAdapter，而非常规的V4包中的PagerAdapter
	 */
	private SecretaryBgPagerAdapter adapter;
	private FixedSpeedScroller mScroller;
	private InternalHandler handler = new InternalHandler();
	
	private boolean isTheSameCardAsUsual = false;
	private RecyclingImageView iv_activity_back;
	private boolean mIsFromRegisterAndHaveNothingThenGoToRegister = false;
	private View rootView;
	private boolean toRegister = false;
	private int resume_item_num = 300*1000;
	class Views {
		private View fl_register_recode, rl_register_dialog, fl_register_pwd;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_register_reg_login, tv_register_forget_password;

		private RecyclingImageView register_bg;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TimeButton tv_register_send_code;
		private EditText et_register_phone, et_register_recode,
		et_register_pwd;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView btn_register_reg_login;
		
		/*@InjectBinder(listeners = { OnClick.class }, method = "click")
		private RecyclingImageView iv_activity_back;*/

//		private CloudView iv_register_bg;
		
		private NoTouchViewPager vp_sercretary_bg_pager;
	}
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if (getArguments()!=null) {
			fragment_name=getArguments().getString("fragment_name");
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;

		//getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);

		rootView = inflater.inflate(R.layout.activity_register, null);
		Handler_Inject.injectFragment(this, rootView);

		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		adapter = new SecretaryBgPagerAdapter();
		v.vp_sercretary_bg_pager.setOffscreenPageLimit(4);
		v.vp_sercretary_bg_pager.setAdapter(adapter);
        v.vp_sercretary_bg_pager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
//		try {
//			Field mField = NoTouchViewPager.class.getDeclaredField("mScroller");
//			mField.setAccessible(true);//允许暴力反射
//			mScroller = new FixedSpeedScroller(v.vp_sercretary_bg_pager.getContext(),new LinearInterpolator());//CycleInterpolator(Float.MAX_VALUE)
//			mScroller.setmDuration(15000);
//			mField.set(v.vp_sercretary_bg_pager, mScroller);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		//mScroller.setmDuration(8000);
//		handler.removeCallbacksAndMessages(null);
//		handler.postDelayed(new InternalTask(), 0);
//		v.vp_sercretary_bg_pager.setCurrentItem(1000*300);
		

//		activity.getWindow().setSoftInputMode(
//				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
//		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE| 
//				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		/*SpannableString ss = new SpannableString("输入密码");
		AbsoluteSizeSpan ass = new AbsoluteSizeSpan(7,true);
		ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		v.et_register_pwd.setHint(new SpannedString(ss));*/
		/*iv_activity_back = (RecyclingImageView) rootView.findViewById(R.id.iv_activity_back);*/
		
		return rootView;
	}

	@InjectInit
	private void init() {
		v.tv_register_send_code.setAfterBg(R.drawable.button_validation);//这是TimeButton对象设置前后背景中
		v.tv_register_send_code.setBeforBg(R.drawable.button);

		//去获取传递进来的args值，下面对应作出判断和回应
		if (getArguments() != null) {
			isReset = getArguments().getBoolean("isReset");
			isJustLogin = getArguments().getBoolean("isJustLogin");
			toRegister = getArguments().getBoolean("toRegister");
			isResetPsw = getArguments().getBoolean("isResetPsw");
			isFromMyBankcardFragToRigisterFrag  = getArguments().getBoolean("isFromMyBankcardFragToRigisterFrag");
			boolean isFromHome = getArguments().getBoolean("isFromHome");
			boolean isFromEightPart = getArguments().getBoolean("isFromEightPart");
			boolean isFromNewPart = getArguments().getBoolean("isFromNewPart");
			boolean isFromRigisterFragToMyBankcardFrag = getArguments().getBoolean("isFromRigisterFragToMyBankcardFrag");
			int initType = getArguments().getInt("type");
			
			boolean isFromRegisterAndHaveNothingThenGoToRegister = getArguments().getBoolean("isFromRegisterAndHaveNothingThenGoToRegister");
			
			mIsFromRegisterAndHaveNothingThenGoToRegister  = isFromRegisterAndHaveNothingThenGoToRegister;
			mIsFromHome = isFromHome;
			mIsFromEightPart = isFromEightPart;
			mIsFromNewPart = isFromNewPart;
			mInitType = initType;


			if (isReset) {
				v.fl_register_pwd.setVisibility(View.GONE);
				v.fl_register_recode.setVisibility(View.VISIBLE);
				v.tv_register_reg_login.setVisibility(View.GONE);
				v.btn_register_reg_login.setText(getString(R.string.commit));
				v.tv_register_forget_password.setVisibility(View.GONE);
				v.tv_register_reg_login.setVisibility(View.GONE);
			}
			if(isJustLogin){
				/* 暂时将注册的效果展开
				 * */
				v.fl_register_pwd.setVisibility(View.VISIBLE);
				v.fl_register_recode.setVisibility(View.GONE);
				v.register_bg.setImageResource(R.drawable.log_in);
				v.tv_register_forget_password.setVisibility(View.VISIBLE);
//				v.tv_register_reg_login.setVisibility(View.GONE);//将右上角的注册gone掉
				v.btn_register_reg_login.setText(getString(R.string.login));
				v.et_register_pwd.setHint("输入密码");
				
				if(toRegister){
					click(v.tv_register_reg_login);
				}
			}
			if (isResetPsw) {
				App.app.setData("isResetPsw", "true");//这个的作用是为了控制拦截物理返回键的作用
				
				/*iv_activity_back.setVisibility(View.GONE);*/
				
				v.fl_register_pwd.setVisibility(View.VISIBLE);
				v.fl_register_recode.setVisibility(View.GONE);
				v.register_bg.setImageResource(R.drawable.log_in);
				v.tv_register_forget_password.setVisibility(View.INVISIBLE);
				v.tv_register_reg_login.setVisibility(View.GONE);//将右上角的注册gone掉
				v.btn_register_reg_login.setText(getString(R.string.login));
				v.et_register_pwd.setText(getArguments().getString("password"));
				v.et_register_phone.setText(getArguments().getString("mobile"));
			}


			if(mIsFromRegisterAndHaveNothingThenGoToRegister){
				
			}
			/*SpannableString ss = new SpannableString("输入密码");
			AbsoluteSizeSpan ass = new AbsoluteSizeSpan(7,true);
			ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			v.et_register_pwd.setHint(new SpannedString(ss));*/
		}
	}

	/* 重写Fragment中的方法
	 * (non-Javadoc)
	 * @see com.qmyo.fragment.BaseFragment#back()
	 */
	/*@InjectMethod(value = { @InjectListener(ids = { R.id.iv_activity_back }, listeners = { OnClick.class }) })*/
	protected void back() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
		
		//强制收下软键盘
		/*activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);*/
		/*autoHiddenKey();//内部实为Toggle方法，不是真是的控制操作*/		
		
		if(App.app.getData("isEmbrassStatus").equals("true") ){
			CustomToast.show(activity, "请使用注册账号登录哦~", "登陆后请至少添加一张银行卡");
			return;
		}
		
		if(isFromMyBankcardFragToRigisterFrag){
			MineBankcardFragment fragment = new MineBankcardFragment();
			Bundle bundle = new Bundle();

			if(mIsFromHome){                                                //从HomeFrag--->MineBankcardFrag--->RegisterFrag ,带上需要的isFromHomeFragment
				bundle.putBoolean("isFromHome", mIsFromHome);
			}else if(mIsFromEightPart){                                     //从EightPartsFrag--->MineBankcardFrag--->RegisterFrag 
				bundle.putBoolean("isFromEightPart", mIsFromEightPart);
				bundle.putInt("type", mInitType);
			}else if(mIsFromNewPart){                                       //从NewPartsFrag--->MineBankcardFrag--->RegisterFrag 
				bundle.putBoolean("isFromNewPart", mIsFromNewPart);
				bundle.putBoolean("IsNew", true);
				bundle.putInt("type", R.string.new_exposure);
			}
			//bundle.putBoolean("isFromRigisterFragToMyBankcardFrag", true);

			fragment.setArguments(bundle);
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			return;
		}

		/*RegisterFragment fragment = new RegisterFragment();
		Bundle args = new Bundle();
		args.putBoolean("isResetPsw", true);
		args.putString("mobile", mobile);
		args.putString("password", pwd);
		fragment.setArguments(args);
		
		FragmentEntity entity = new FragmentEntity();
		entity.setFragment(fragment);
		EventBus.getDefault().post(entity);*/
		
		/**
		 * 左上角返回键已清，暂时没有进行返回地操作*/
		  if(isResetPsw){
			  CustomToast.show(activity, "迈界小贴士", "更改新密码后，请登录");
			 return;
			/*MineFragment fragment = new MineFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);*/
		}

		if(GlobalValue.user==null && GlobalValue.isFirst){
			ExperienceSearchFragment fragment = new ExperienceSearchFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
		}else{
			getActivity().getSupportFragmentManager().popBackStack();
		}

		/*	//要求：只有在第一次登陆成功进入界面时，自动跳转至首页
		if(!(TextUtils.isEmpty(App.app.getData("secret")))&&TextUtils.isEmpty(App.app.getData("exp_secret"))){//登录用户
			getActivity().getSupportFragmentManager().popBackStack();
			HomeFragment fragment = new HomeFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
		}else{
			getActivity().getSupportFragmentManager().popBackStack();
		}*/
	}


	private void click(View view) {
		phone = v.et_register_phone.getText().toString();
		String pwd = v.et_register_pwd.getText().toString();
		code = v.et_register_recode.getText().toString();
		InternetConfig config = new InternetConfig();

		switch (view.getId()) {
		case R.id.btn_register_reg_login://登录或者注册的横向长按钮

			/**
			 * 对输入的手机号码在此处进行校验
			 */
			if (phone.length() != 11) {
				CustomToast.show(activity, R.string.tip3,R.string.please_enter_phone_pwd);
				return;
			}

			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

			if (getString(R.string.register).equals(/*注册账号*/
					v.btn_register_reg_login.getText().toString())) {
				//将EditText中的Hint设置密码改为：请您设置6-16位数字或字母的密码

				if (pwd.length() < 6) {
					CustomToast.show(activity, R.string.tip_setpwderror,
							R.string.please_enter_correct_pwd);//请您设置6-16位数字或字母的密码
					return;
				}

				if (code.toCharArray().length != 4) {
					CustomToast.show(activity, R.string.tip_setcodeerror,
							R.string.please_enter_correct_code);//请您设置正确的验证码
					return;
				}

				config.setKey(1);//注册 走起
				params.put("mobile", phone);
				params.put("password", pwd);
				params.put("code", code);

				if(TextUtils.isEmpty(App.app.getData("exp_secret"))){//当准备点击前去注册时，检测到没有exp_secret值，则将isHasExpSecretWhenClickToRegister置为false
					isHasExpSecretWhenClickToRegister  = false;
				}else{
					isHasExpSecretWhenClickToRegister  = true;
					params.put("secret", App.app.getData("exp_secret"));
				}
				config.setCharset("utf-8");

				/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_REGISTER, params, null, config, this);*/
				FastHttpHander.ajax(GlobalValue.URL_AUTH_REGISTER, params, config, this);

			} else if (getString(R.string.login).equals(/*用户登录*/
					v.btn_register_reg_login.getText().toString())) {

				if (pwd.length() < 1) {
					CustomToast.show(activity, R.string.tip_pswnotenter,
							R.string.please_enter_phone_pwd);
					return;
				}

				if (pwd.length() < 6) {
					CustomToast.show(activity, R.string.tip3,
							R.string.please_enter_phone_pwd);
					return;
				}

				pd = new ProgressDialog(activity);
				pd.setMessage(getString(R.string.tip5));
				pd.show();


				Handler myHandler = new Handler(); 
				myHandler.postDelayed(new Runnable() { 
					public void run() { 
						if(pd!= null){
							pd.cancel(); 
							CustomToast.show(activity, "登录失败", "服务器睡着了...");
						}
					} 
				}, 10*1000);


				params.put("mobile", phone);
				params.put("password", pwd);
				config.setCharset("utf-8");
				config.setKey(2);//登陆 走起
				/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_LOGIN, params, null, config, this);*/

				/**
				 * 使用post方式去提交
				 */
				FastHttpHander.ajax(GlobalValue.URL_AUTH_LOGIN, params,config, this);

			} else if (getString(R.string.commit).equals(/*忘记密码中的 提交 按钮*/
					v.btn_register_reg_login.getText().toString())) {

				if (TextUtils.isEmpty(code)) {
					CustomToast.show(activity, "请填写验证码", "没有验证码哟");
					return;
				}
				if (code.toCharArray().length != 4) {
					CustomToast.show(activity, R.string.tip_setcodeerror,
							R.string.please_enter_correct_code);//请您设置正确的验证码
					return;
				}

				config = new InternetConfig();
				config.setKey(5);
				LinkedHashMap<String, String> resetParams = new LinkedHashMap<String, String>();
				resetParams.put("mobile", phone);
				resetParams.put("code", code);
				/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_CAPTCHA_LOGIN,
						resetParams, null, config, this);*/

				FastHttpHander.ajax(GlobalValue.URL_AUTH_CAPTCHA_LOGIN,resetParams,  config, this);
			}

			break;
		case R.id.tv_register_send_code:// 获取验证码
			if (phone.length() != 11) {
				CustomToast.show(activity, "格式不正确", "请正确填写手机号或密码");
				v.tv_register_send_code.setRun(false);
				return;
			}
			v.tv_register_send_code.setRun(true);
			config.setKey(0);
			LinkedHashMap<String, String> typeParams = new LinkedHashMap<String, String>();
			if (isReset){//重置
				typeParams.put("type", "reset");
			}else{//正常注册
				typeParams.put("type", "register");
			}
			FastHttpHander.ajaxGet(GlobalValue.URL_AUTH_CAPTCHA + phone, typeParams ,config, this);

			break;
		case R.id.tv_register_reg_login://点击右上角的登录和注册复用按钮
			if (getString(R.string.register).equals(//当右上角的圆形TextView中的值为  注册 时，即原本是登录页面页面（圆形TextView和实际页面功能相反），点击后变为注册页面
					v.tv_register_reg_login.getText().toString())) {

				v.fl_register_recode.setVisibility(View.VISIBLE);
				v.tv_register_reg_login.setText(getString(R.string.login));
				v.register_bg.setImageResource(R.drawable.registered);
				v.btn_register_reg_login.setText(getString(R.string.register));//底部按钮设置为 注册
				v.tv_register_forget_password.setVisibility(View.GONE);//忘记密码不显示
				v.et_register_pwd.setHint("设置密码：6-16位数字或字母");

				/*	SpannableString ss = new SpannableString("设置密码：6-16位数字或字母");
				AbsoluteSizeSpan ass = new AbsoluteSizeSpan(7,true);
				ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				v.et_register_pwd.setHint(new SpannedString(ss));*/



			} else if (getString(R.string.login).equals(
					v.tv_register_reg_login.getText().toString())) {
				v.fl_register_pwd.setVisibility(View.VISIBLE);
				v.fl_register_recode.setVisibility(View.GONE);
				v.register_bg.setImageResource(R.drawable.log_in);
				v.tv_register_forget_password.setVisibility(View.VISIBLE);
				v.tv_register_reg_login.setText(getString(R.string.register));
				v.btn_register_reg_login.setText(getString(R.string.login));
				v.et_register_pwd.setHint("输入密码");

				/*// 新建一个可以添加属性的文本对象
				SpannableString ss = new SpannableString("喝酒就要喝一斤!");
				// 新建一个属性对象,设置文字的大小
				AbsoluteSizeSpan ass = new AbsoluteSizeSpan(8,true);
				// 附加属性到文本
				ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				// 设置hint
				editText.setHint(new SpannedString(ss));*/

				/*SpannableString ss = new SpannableString("输入密码");
				AbsoluteSizeSpan ass = new AbsoluteSizeSpan(7,true);
				ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				v.et_register_pwd.setHint(new SpannedString(ss));*/
			}

			break;
		case R.id.tv_register_forget_password:// 点击忘记密码
			v.tv_register_forget_password.setVisibility(View.GONE);
			v.fl_register_recode.setVisibility(View.VISIBLE);
			v.fl_register_pwd.setVisibility(View.GONE);
			v.btn_register_reg_login.setText(getString(R.string.commit));
			v.tv_register_reg_login.setVisibility(View.GONE);
			isReset = true;
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {

			switch (r.getKey()) {
			case 0://点击发送验证码的操作
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, "验证码已发送至您的手机", "请注意查收哦~");
				}
				errorInfo = (ErrorInfo)Handler_Json.JsonToBean(ErrorInfo.class, r.getContentAsString());
				if (!TextUtils.isEmpty(errorInfo.getError())){//errorInfo不为空，证明拿到的是错误信息，证明号码此号码已经被注册啦啦啦啦！
					/* errorInfo.getError()*/
					/* CustomToast.show(this.activity, "您的手机号码已注册","您可以直接登录迈界哦");*/
					CustomToast.show(this.activity, "此手机号码已注册","您可以直接登录迈界哦");
					Log.i("手机号码已注册",errorInfo.getError());
					return;
				}



				break;
			case 1:// 点击注册后返回信息
				errorInfo2 = Handler_Json.JsonToBean(ErrorInfo.class,r.getContentAsString());
				if (!TextUtils.isEmpty(errorInfo2.getError())) {
					/*errorInfo2.getError()*/
					CustomToast.show(activity, R.string.tip_setcodeerror,R.string.please_enter_correct_code);
					return;
				}

				//一旦注册就已经拿到了user对象，那么如果此时就将user中的secret写入本地中，
				//那么就在此时立即退出程序，下次进入时肯定会自动进入登陆后的状态了

				GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());//-------------------------> 注册成功后，便拿到了返回的 user信息 
				if (GlobalValue.user != null) {
					clearTempTokenAndSercet();
					// v.rl_register_dialog.setVisibility(View.GONE);
					// v.rl_register_reg_sucess.setVisibility(View.VISIBLE);
					CustomToast.show(activity, "恭喜你注册成功哟", "小迈在此恭候多时，请您立即登录哟~");
					pushToken(GlobalValue.user.getMobile());//---------------------------------------------->注册成功（或者登陆成功）后才会进行上传RegisterId的操作
				}
				/*App.app.setData("secret", GlobalValue.user.getSecret());*/             //------>是否在一注册成功就进行写入secret的操作？这一步是去是留待考虑

				//点击注册成功后，立即跳转至登录页，虽然是立即跳转至登录页，但是并未真正限制了用户在App中的使用权限
				v.fl_register_pwd.setVisibility(View.VISIBLE);
				v.fl_register_recode.setVisibility(View.GONE);
				v.register_bg.setImageResource(R.drawable.log_in);
				v.tv_register_forget_password.setVisibility(View.VISIBLE);
				v.tv_register_reg_login.setText(getString(R.string.register));
				v.btn_register_reg_login.setText(getString(R.string.login));
				v.et_register_pwd.setHint("输入密码");

				break;
			case 2://登陆
				/*this.pd.dismiss();*/
				ErrorInfo errorInfo = Handler_Json.JsonToBean(ErrorInfo.class,r.getContentAsString());
				if (!TextUtils.isEmpty(errorInfo.getError())) {

					/*CustomToast.show(activity, R.string.tip, errorInfo.getError());*/
					CustomToast.show(activity, "手机号或密码错误", "小迈希望您仔细填写哦");
					pd.dismiss();
					pd = null;
					return;
				}

				App.app.setData("user_phone",phone);
				
				GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());//----> 登录成功后，便拿到了返回的 user信息 
				Log.i("打出包含secret的返回信息内容",r.getContentAsString());
				
				if (GlobalValue.user != null) {
					Log.i("","用户的status状态为： "+GlobalValue.user.getStatus());
					if(GlobalValue.user.getStatus().contains("0")){
						CustomToast.show(activity, "抱歉，您的账号暂时冻结", "详情请联系迈界客服");
						pd.dismiss();
						this.pd = null;
						return;
						
					}else{
						clearTempTokenAndSercet();//清除掉临时的secret和 token
						App.app.setData("secret", GlobalValue.user.getSecret());
						// 拿着user中的secret 去访问服务器获取token
						InternetConfig config = new InternetConfig();
						config.setKey(3);
						LinkedHashMap<String, String> params = new LinkedHashMap<>();
						FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN+ App.app.getData("secret"), config, this);
						CustomToast.show(activity, "迈界欢迎您", "不用找，优惠到，随身小秘帮您挑~");
						
						InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
						
						App.app.setData("isExperience", "false");//将体验用户标示置为 false
						App.app.getData("isEmbrassStatus").equals("");//此时用户状态不再是尴尬的状态时
						
//						pushToken(GlobalValue.user.getMobile());//-------------------------------------------> 进行极光推送的token ！！
					}
				}
				break;
			case 3:// 拿到了token，燥起来
				token = Handler_Json.JsonToBean(Token.class,r.getContentAsString());
				App.app.setData("access_token", token.getToken());
				pushToken(GlobalValue.user.getMobile());//-------------------------------------------> 进行极光推送的token ！！
				Log.d("token", "token : "+token.getToken());
				
				
				/**
				 * 向服务器端提交一次，用户登录的信息
				 */
				CommitStaticsinfoUtils.commitStaticsinfo(2);
				
				
				/*back();*/
				InternetConfig config1 = new InternetConfig();
				config1.setKey(6);
				HashMap<String, Object> head1 = new HashMap<>();
				head1.put("Authorization", "Bearer " + token.getToken());
				config1.setHead(head1);
				FastHttpHander.ajaxGet(GlobalValue.URL_BANKCARD_CHOSEN, config1, this);
				
				
				/**
				 * 参数: access_token   &  secret
				 */
				if(!TextUtils.isEmpty(App.app.getData("access_token"))){
					if(!TextUtils.isEmpty(App.app.getData("secret"))){
				        //推送的服务被关闭掉之后，之前启动推送的服务
							JPushInterface.resumePush(getActivity().getApplicationContext());
							JPushInterface.init(activity.getApplicationContext());
					}
				}
				//App.app.setData("access_token", token.getToken());

				/*back();*///先于弹窗提醒之前,将界面跳转至上一页(由体验用户去登陆时)
//				backandinitMine();//跳转并刷新MineFragemnt				
				break;
			case 4:
				// GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());
				if(r.getContentAsString().contains("true")){
					/*CustomToast.show(activity, "已绑定推送服务","小迈会为您提供更多惊喜哦");*/
					LogUtils.toDebugLog("Jpush", "绑定推送服务更换RegisterID完成");
					Toast.makeText(activity, "^_^", Toast.LENGTH_SHORT).show();
				}
				break;
			case 5:
				if ("true".equals(r.getContentAsString())) {
					ResetPwdFragment fragment = new ResetPwdFragment();
					Bundle args = new Bundle();
					args.putString("code", code);
					args.putString("mobile", phone);
					fragment.setArguments(args);
					FragmentEntity entity = new FragmentEntity();
					entity.setFragment(fragment);
					EventBus.getDefault().post(entity);
				} else {
					CustomToast.show(activity, R.string.tip,R.string.code_faild);
				}
				break;
				
			case 6:
				Log.d("选中的卡","返回回来的选中的Bankcard的id为： "+r.getContentAsString());
				
				if(r.getContentAsString().equals("[]")){//表明当前的用户是个没有卡的已登录用户，此时需要提醒他进行加卡操作
					isHasExpSecretWhenClickToRegister = false;
					
					
					
				}else{
					App.app.setData("isEmbrassStatus", "");//表明登录正常，则将isEmbrassStatus的状态值置为：""
					BankCardData theChosenBankCardData  = Handler_Json.JsonToBean(BankCardData.class,r.getContentAsString());
					int theChosenBankcardIdUnderTheLoginUser = theChosenBankCardData.getBankcard().getId();
					
					Log.d("银行卡页的id", "从服务器上拿到的银行卡ID"+theChosenBankcardIdUnderTheLoginUser+"");
					Log.d("银行卡页的id", "从本地sp中拿到的银行卡ID"+App.app.getData("ExperienceBankcardId"));
					if(String.valueOf(theChosenBankcardIdUnderTheLoginUser).equals(App.app.getData("ExperienceBankcardId"))){
						isTheSameCardAsUsual = true; //登陆前后是同一张卡
					}else{
						isTheSameCardAsUsual = false;//登陆前后并非是同一张卡
					}
					App.app.setData("ExperienceBankcardId","");//不管前后是不是一张卡，我们都需要将这个体验卡id的本地内容设置为 空
				}
				
				InternetConfig config2 = new InternetConfig();
				config2.setKey(7);
				HashMap<String, Object> head2 = new HashMap<>();
				head2.put("Authorization", "Bearer " + token.getToken());
				config2.setHead(head2);
				FastHttpHander.ajaxGet(GlobalValue.URL_FRESHEN_USER, config2, this);
				
				break;
			case 7:
				GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());
				pd.dismiss();
				pd = null;
				
				//和AccessTokenSer服务中保持一致
				App.app.setData("user_avatar",GlobalValue.user.getAvatar());
				App.app.setData("user_nickname",GlobalValue.user.getNickname());
				
				Intent intent_Avatar_NickName = new Intent("com.lansun.qmyo.refreshAvatar_NickName");
				getActivity().sendBroadcast(intent_Avatar_NickName);
			    System.out.println("通知MineFragment 换个头像和昵称");
			    
				
				if(isResetPsw){
					/*MineFragment mineFragment = new MineFragment();*/
					MainFragment mainFragment = new MainFragment(3);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(mainFragment);
					EventBus.getDefault().post(fEntity);
					App.app.setData("isResetPsw", "");
					return;
				}
				
				if(isHasExpSecretWhenClickToRegister){//点击注册时是拥有临时secret的 ,说明是在体验状态下试图去实现登录用户的操作，那么需要登录成功后进入之前跳入的那一页
					
					if(mIsFromRegisterAndHaveNothingThenGoToRegister){
						App.app.setData("isEmbrassStatus", "");//虽然在登录界面，但是输入了新的可登录账号，那么将其跳转至首页，必将isEmbrassStatus的状态值置为："" (不再为true)
						/*HomeFragment homeFragment = new HomeFragment();*/
						MainFragment mainFragment = new MainFragment(0);
						FragmentEntity fEntity = new FragmentEntity();
						fEntity.setFragment(mainFragment);
						EventBus.getDefault().post(fEntity);
						return;
					}
					
					if(isJustLogin){
						try{
							/*HomeFragment homeFragment = new HomeFragment();*/
							MainFragment mainFragment = new MainFragment(0);
							FragmentEntity fEntity = new FragmentEntity();
							fEntity.setFragment(mainFragment);
							EventBus.getDefault().post(fEntity);
							
						}catch(Exception e){
							
						}
						return;
					}else{
						Fragment fragment=null;
						Log.e("fragment_name", fragment_name.getClass().getSimpleName());
						//此处就是登录成功之后的页面跳转
						FragmentEntity fEntity = new FragmentEntity();
						if ("PersonCenterFragment".equals(fragment_name)) {
							fragment=new PersonCenterFragment();
						}else if ("MineFragment".equals(fragment_name)) {
							/*fragment=new MineFragment();*/
							fragment=new MainFragment(3);
						}else if("GrabRedpackFragment".equals(fragment_name)){
							fragment=new MainFragment(0);
						}else if("MineBankcardFragment".equals(fragment_name)){
							fragment=new MineBankcardFragment();
							Bundle bundle=new Bundle();
							bundle.putBoolean("isFromEightPart",mIsFromEightPart);
							if(mIsFromEightPart){
								bundle.putInt("type",mInitType);
							}
							bundle.putBoolean("isFromNewPart",mIsFromNewPart);
							bundle.putBoolean("isFromHome",mIsFromHome);
							bundle.putBoolean("isFromRigisterFragToMyBankcardFrag",true);
							fragment.setArguments(bundle);
						}else if("ActivityDetailFragment".equals(fragment_name)){
							if(isTheSameCardAsUsual){
//								fragment=new ActivityDetailFragment();
//								Bundle bundle=new Bundle();
//								bundle.putString("shopId", App.app.getData("shopId"));
//								bundle.putString("activityId", App.app.getData("activityId"));
//								if(App.app.getData("isReportInActivityDetailPage").equals("true")){
//									App.app.setData("isReportInActivityDetailPage","");
//								}else{
//									bundle.putString("refreshTip", "true");
//								}
//								fragment.setArguments(bundle);
								
								if(App.app.getData("isReportInActivityDetailPage").equals("true")){
									App.app.setData("isReportInActivityDetailPage","");
									//仅重新获取数据内容，不做模拟点击操作
									getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshActivityDetailPage"));
								}else{
									//不仅重新获取数据内容，且做模拟点击操作
									getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshActivityDetailPageAndClick"));
								}
								Intent intent=new Intent("com.lansun.qmyo.refreshTheIcon");
								getActivity().sendBroadcast(intent);
								System.out.println("从活动详情页过来，发送广播！");
								
								back();
								return;
								
							}else{
								fragment = new MainFragment(0);
								/*fragment=new HomeFragment();*/
							}
						}else if("StoreDetailFragment".equals(fragment_name)){
							if(isTheSameCardAsUsual){
//								fragment=new StoreDetailFragment();
//								Bundle bundle=new Bundle();
//								bundle.putString("shopId", App.app.getData("shopId"));
//								if(App.app.getData("isReportInStoreDetailPage").equals("true")){
//									App.app.setData("isReportInStoreDetailPage","");
//								}else{
//									bundle.putString("refreshTip", "true");
//								}
//								fragment.setArguments(bundle);
								
								if(App.app.getData("isReportInStoreDetailPage").equals("true")){
									App.app.setData("isReportInStoreDetailPage","");
									//仅重新获取数据内容，不做模拟点击操作
									getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshStoreDetailPage"));
									
								}else{
									//不仅重新获取数据内容，且做模拟点击操作
									getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshStoreDetailPageAndClick"));
								}
								
								
								Intent intent=new Intent("com.lansun.qmyo.refreshTheIcon");
								getActivity().sendBroadcast(intent);
								System.out.println("从门店详情页过来，发送广播！");
								back();
								return;
								
							}else{
								/*fragment=new HomeFragment();*/
								fragment = new MainFragment(0);
								
							}
						}else if("SecretaryFragment".equals(fragment_name)){
							/*fragment=new SecretaryFragment();*/
							fragment=new MainFragment(1);
						}else if("SecretaryTravelShowFragment".equals(fragment_name)){
							fragment=new SecretaryTravelShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryTravelShowFragment 发送刷新广播");
							
						}else if("SecretaryShoppingShowFragment".equals(fragment_name)){
							fragment=new SecretaryShoppingShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryShoppingShowFragment 发送刷新广播");
							
						}else if("SecretaryPartyShowFragment".equals(fragment_name)){
							fragment=new SecretaryPartyShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryPartyShowFragment 发送刷新广播");
							
						}else if("SecretaryLifeShowFragment".equals(fragment_name)){
							fragment=new SecretaryLifeShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryLifeShowFragment 发送刷新广播");
							
						}else if("SecretaryStudentShowFragment".equals(fragment_name)){
							fragment=new SecretaryStudentShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryStudentShowFragment 发送刷新广播");
							
						}else if("SecretaryInvestmentShowFragment".equals(fragment_name)){
							fragment=new SecretaryInvestmentShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryInvestmentShowFragment 发送刷新广播");
							
						}else if("SecretaryCardShowFragment".equals(fragment_name)){
							fragment=new SecretaryCardShowFragment();
							sendBroadcastDelay();
							LogUtils.toDebugLog("RegisterFragment", "SecretaryCardShowFragment 发送刷新广播");
						}else if("FoundFragment".equals(fragment_name)){
							/*fragment=new FoundFragment();*/
							fragment=new MainFragment(2);
							
						}
						if (isTheSameCardAsUsual) {//登陆前后为同一张卡，故而无需消除写在本地的 筛选栏数据
							
						}else{
							//需要将之前是体验用户时， 在本地存好的筛选栏的json串删除掉,首次访问标签置为""
							for(int i = 0;i<8;i++){
								App.app.setData((App.TAGS[i]),"");
							}
							App.app.setData("in_this_fragment_time","");
						}
						
						fEntity.setFragment(fragment);
						EventBus.getDefault().post(fEntity);
						
//						back();
					}
				}else{//点击注册时是不曾    拥有临时secret的 ,那么需跳转至银行卡搜索页添加一张卡后，再跳入首页
					SearchBankCardFragment searchBankCardFragment = new SearchBankCardFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean("isFromRegisterAndHaveNothing",true);
					Log.d("isFromRegisterAndHaveNothing", "isFromRegisterAndHaveNothing的值为： "+bundle.get("isFromRegisterAndHaveNothing"));
					
					App.app.setData("isEmbrassStatus", "true");
					
					searchBankCardFragment.setArguments(bundle);
					FragmentEntity fEntity = new FragmentEntity();
					fEntity.setFragment(searchBankCardFragment);
					EventBus.getDefault().post(fEntity);
				}
				break;
			}
		}
	}
	/**
	 * 注册推送的RegisterId
	 * 
	 * @param mobile
	 */
	public void pushToken(String mobile) {
		InternetConfig config = new InternetConfig();
		config.setKey(4);
		LinkedHashMap<String, String> params = new LinkedHashMap<>();
		params.put("mobile", mobile);
		params.put("registration_id",JPushInterface.getRegistrationID(activity));

		Log.i("我拿到的registration_id为： ",JPushInterface.getRegistrationID(activity));

		/*FastHttpHander.ajaxForm(GlobalValue.URL_PUSH_TOKEN, params, null,config, this);*/
		FastHttpHander.ajax(GlobalValue.URL_PUSH_TOKEN, params, config, this);
	}

	@Override
	public void onResume() {
		/**
		 * 背景上的云暂关闭掉
		 **/ 
//		v.iv_register_bg.threadFlag = true;
		
		try {
			Field mField = NoTouchViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);//允许暴力反射
			mScroller = new FixedSpeedScroller(v.vp_sercretary_bg_pager.getContext(),new LinearInterpolator());//CycleInterpolator(Float.MAX_VALUE)
			mScroller.setmDuration(15000);
			mField.set(v.vp_sercretary_bg_pager, mScroller);
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.removeCallbacksAndMessages(null);
		if(initTime){
			v.vp_sercretary_bg_pager.setCurrentItem(1000*300);
	        initTime = !initTime;
		}else{
			v.vp_sercretary_bg_pager.setAdapter(null);
			v.vp_sercretary_bg_pager.setAdapter(adapter);
			v.vp_sercretary_bg_pager.setCurrentItem(resume_item_num+1);
		}
		handler.postDelayed(new InternalTask(), 0);
		super.onResume();
	}

	@Override
	public void onPause() {//在Fragmentment暂停的时候，将背景设置成单图
		/**///v.iv_register_bg.threadFlag = false;
		//v.iv_register_bg.setBackgroundResource(R.drawable.cloud_1);
//		int nextIndex = v.vp_sercretary_bg_pager.getCurrentItem()+1;
//		v.vp_sercretary_bg_pager.setCurrentItem(nextIndex);
		
		int currentItem = v.vp_sercretary_bg_pager.getCurrentItem();
		resume_item_num  = currentItem;
//		v.vp_sercretary_bg_pager.invalidate();
		super.onPause();
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	
	
	
    class SecretaryBgPagerAdapter extends PagerAdapter {
	   
			@Override
			public int getCount() {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ImageView imageView = new ImageView(activity);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				if(position%2==0){
					imageView.setBackgroundResource(R.drawable.cloud_1);
				}else{
					imageView.setBackgroundResource(R.drawable.cloud_2);
				}
				container.addView(imageView);
				return imageView;
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View) object);
			}
		}
	   
	@SuppressLint("HandlerLeak")
	class InternalHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			int nextIndex = v.vp_sercretary_bg_pager.getCurrentItem()+1;
			v.vp_sercretary_bg_pager.setCurrentItem(nextIndex);
			handler.postDelayed(new InternalTask(), 15000);//自己给自己发信息
		}
	}
	
	class InternalTask implements Runnable{
		@Override
		public void run() {
			handler.sendEmptyMessage(0);
		}
	}

		/** 针对七大秘书页面进行的延迟发送广播操作*/
		private void sendBroadcastDelay(){
			getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshTheIcon"));
		}
}
