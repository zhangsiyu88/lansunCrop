package com.lansun.qmyo.fragment.task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.RecognizerListener;
import com.iflytek.speech.RecognizerResult;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.IFlytekResultBean;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.RegisterFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.LoginDialog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.umeng.socialize.utils.LoadingDialog;
public class TaskAssignment extends BaseFragment implements TextWatcher{//,OnClickListener
	
	private RegisterFragment fragment;
	private FragmentEntity event;
	private EditText et_secretary_form_content;
	private String hint;
	private TextView btn_secretary_commit_form;
	private String type;
	private LoginDialog loginDialog;
	private RecyclingImageView iv_activity_back;
	private ProgressDialog dialogpg;
	private String enterText = "";
	protected static final int RESULT_SPEECH = 1;  
	
	
	
	private Handler handleOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (dialogpg!=null) {
					dialogpg.dismiss();
				}
				final Dialog dialog=new Dialog(activity,R.style.Translucent_NoTitle);
				
				/**
				 * 模糊化背景
				 */
				Blurry.with(getActivity())
				.radius(25)
				.sampling(2)
				.async()
				.animate(500)
				.onto((ViewGroup)rootView);
				
				//dialog消失时，需要恢复背景页面的效果
				dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface arg0) {
						Blurry.deleteNoAnimation((ViewGroup) rootView);
						try{
							FragmentEntity entity=new FragmentEntity();
							/*Fragment f=new SecretaryFragment(); */
							Fragment f=new MainFragment(1); 
							if(App.app.getData("firstCommitPersonalSecretaryAsk").isEmpty()){
								Bundle bundle = new Bundle();
								bundle.putString("firstCommitAsk","true");
								f.setArguments(bundle);
								LogUtils.toDebugLog("catch", "传递第一次的提问的标示过去");
							}
							entity.setFragment(f);
							EventBus.getDefault().post(entity);
						}catch (Exception e) {
							FragmentEntity entity=new FragmentEntity();
							Fragment f=new MainFragment(1); 
							entity.setFragment(f);
							EventBus.getDefault().post(entity);
							LogUtils.toDebugLog("catch", "提交成功后弹出的反馈页面 报出 异常");
						}
					}
				});
				
				dialog.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					 if (keyCode == KeyEvent.KEYCODE_BACK){
						 dialog.dismiss();
			             return true; // pretend we've processed it
					 }else{
			        	   return false; // pass on to be processed as normal
			           }
					}
				});
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				
				dialog.show();
				Window window=dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setContentView(R.layout.task_dialog);
				
				window.findViewById(R.id.iknow).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						
//						try{
//							FragmentEntity entity=new FragmentEntity();
//							/*Fragment f=new SecretaryFragment(); */
//							Fragment f=new MainFragment(1); 
//							if(App.app.getData("firstCommitPersonalSecretaryAsk").isEmpty()){
//								Bundle bundle = new Bundle();
//								bundle.putString("firstCommitAsk","true");
//								f.setArguments(bundle);
//								LogUtils.toDebugLog("catch", "传递第一次的提问的标示过去");
//							}
//							entity.setFragment(f);
//							EventBus.getDefault().post(entity);
//						}catch (Exception e) {
//							FragmentEntity entity=new FragmentEntity();
//							Fragment f=new MainFragment(1); 
//							entity.setFragment(f);
//							EventBus.getDefault().post(entity);
//							LogUtils.toDebugLog("catch", "提交成功后弹出的反馈页面 报出 异常");
//						}
					}
				});
				break;
			}
		};
	};
	private View rootView;
	private View ll_all_widget;
	private ImageView iv_speech;
	private TextView tv_ask_type;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments()!=null) {
			hint="关于任务指派的提问建议\n"+getArguments().getString("content", "");
			type=getArguments().getString("type");
		}
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater=inflater;
		rootView = inflater.inflate(R.layout.secretary_detail_footer_new, container,false);
		
		initView(rootView);
		
		switch(type){
			case "card":
				tv_ask_type.setText("  办卡推荐  ");
				break;
			case "investment":
				tv_ask_type.setText("  理财投资  ");
				break;
			case "life":
				tv_ask_type.setText("  高质生活  ");
				break;
			case "party":
				tv_ask_type.setText("  盛宴派对  ");
				break;
			case "shopping":
				tv_ask_type.setText("  新品购物  ");
				break;
			case "student":
				tv_ask_type.setText("  留学服务  ");
				break;
			case "travel":
				tv_ask_type.setText("  旅游度假  ");
				break;
		}
		
		setListener();
		return rootView;
	}
	private void setListener() {
		iv_activity_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		
	btn_secretary_commit_form.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content=et_secretary_form_content.getText().toString();
				if (TextUtils.isEmpty(content)) {
					CustomToast.show(activity, R.string.tip,
							R.string.please_enter_task);
					return;
				}
				Log.e("content", content);
				Map<String, String> map=new HashMap<String, String>();
				map.put("content", content);
				map.put("type", type);
				OkHttp.asyncPost(GlobalValue.URL_SECRETARY_QUESTION, map, new Callback() {
					@Override
					public void onResponse(Response response) throws IOException {
						handleOk.sendEmptyMessage(0);
					}
					@Override
					public void onFailure(Request arg0, IOException arg1) {

					}
				});
				dialogpg = new ProgressDialog(activity, R.style.Translucent_NoTitle);
				dialogpg.setMessage("正在提交中,请耐心等候");
				dialogpg.show();
			}
		});
		
		
		iv_speech.setOnClickListener(new View.OnClickListener(){  
		       
            @Override  
            public void onClick(View v) {  
   
//                Intent intent = new Intent(  RecognizerIntent.ACTION_RECOGNIZE_SPEECH);  
//                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");//语音提示语
//                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US"); 
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);//语音模式识别为自由模式  
//                try {  
//                    startActivityForResult(intent, RESULT_SPEECH);  //requestCode
//                } catch (ActivityNotFoundException a) {  
//                    Toast t = Toast.makeText(activity,  
//                            "Opps! Your device doesn't support Speech to Text",  
//                            Toast.LENGTH_SHORT);  
//                    t.show();  
//                } 
            	
           	/* 使用讯飞提供的UI进行交互
           	 * //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener 
                SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(activity, null);
             	//2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类 
             	mIat.setParameter(SpeechConstant.DOMAIN, "iat"); 
             	mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
             	mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
             	//3.开始听写 
             	mIat.startListening((com.iflytek.cloud.RecognizerListener) mRecoListener);*/
            	
             	
             	
             	com.iflytek.cloud.InitListener mInitListener = new com.iflytek.cloud.InitListener(){

					@Override
					public void onInit(int arg0) {
						System.out.println("What are you 干啥呢~~");
					}
             	};
             	
            	
            	//1.创建RecognizerDialog对象
            	RecognizerDialog mDialog = new RecognizerDialog(activity, mInitListener);
            	//2.设置accent、language等参数
            	mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            	mDialog.setParameter(SpeechConstant.ACCENT, "xiaoqian");
            	//若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解//结果
            	// mDialog.setParameter("asr_sch", "1");
            	// mDialog.setParameter("nlp_version", "2.0");
            	//3.设置回调接口
            	mDialog.setListener(mRecognizerDialogListener);
            	//4.显示dialog，接收语音输入
            	mDialog.show();
            	

            	
          }
		});
	}
		
	private void initView(View view) {
		iv_speech = (ImageView) view.findViewById(R.id.iv_speech);
		tv_ask_type = (TextView) view.findViewById(R.id.tv_ask_type);
		iv_activity_back=(RecyclingImageView)view.findViewById(R.id.iv_activity_back);
		et_secretary_form_content=(EditText)view.findViewById(R.id.et_secretary_form_content);
		et_secretary_form_content.setHint(hint);
		et_secretary_form_content.requestFocus();
		et_secretary_form_content.setHighlightColor(Color.parseColor("#AFAFAF"));
		btn_secretary_commit_form=(TextView)view.findViewById(R.id.btn_secretary_commit_form);
		et_secretary_form_content.addTextChangedListener(this);
		//ll_all_widget = view.findViewById(R.id.ll_all_widget);
		
		/**
		 * 此方法设置无效，对于多行的Edittext右下角为换行键
		 */
		et_secretary_form_content.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId ==EditorInfo.IME_ACTION_GO){
					//et_secretary_form_content.setImeActionLabel("提交", actionId);
					return true;
				}
				return false;
			}
			
		});
		
		
		
	}
	@Override
	public void afterTextChanged(Editable s) {

	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!TextUtils.isEmpty(s.toString())) {
			btn_secretary_commit_form.setClickable(true);
			btn_secretary_commit_form.setTextColor(getResources().getColor(R.color.app_green1));
		}else {
			btn_secretary_commit_form.setClickable(false);
			btn_secretary_commit_form.setTextColor(Color.parseColor("#AFAFAF"));
			
			//et_secretary_form_content.setImeOptions(EditorInfo.IME_ACTION_GO);
		}
	}
	
/*  对于谷歌语音的调用回传数据
 * 	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){  
        super.onActivityResult(requestCode, resultCode, data);  
   
        switch (requestCode) {  
            case RESULT_SPEECH: {  
            if (resultCode == -1 && null != data) {  //RESULT_OK=-1
   
                ArrayList<String> text = data  
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  
   
                et_secretary_form_content.setText(text.get(0));  
            }  
            break;  
        }  
      }
    }*/
       
	/**
	 * Dialog的监听器
	 */
	RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener(){
		
		@Override
		public void onError(SpeechError arg0) {
			
		}

		@Override
		public void onResult(com.iflytek.cloud.RecognizerResult result,          //com.iflytek.cloud.RecognizerResult
				boolean isLast) {
			LogUtils.toDebugLog("listen", "Dialog结束了");
			LogUtils.toDebugLog("listen", result.getResultString());
			
			
			enterText = et_secretary_form_content.getText().toString();
			/*et_secretary_form_content.setText("来自RecognizerDialogListener返回的结果 "+result.getResultString()); */
			Gson gson = new Gson();
			IFlytekResultBean iflyResult = gson.fromJson(result.getResultString(), IFlytekResultBean.class);		
			
			for(int i =0 ;i<iflyResult.getWs().size();i++){
				String str = iflyResult.getWs().get(i).getCw().get(0).getW();
				enterText = enterText+str;
			}
			
			if(enterText.length()>=3){
				if(enterText.substring(enterText.length()-3, enterText.length()).contains("走你")){
					//1.截取输入文本的最后三个字，将“走你”删除掉，并且将整体显示到文本中去
					
					String lastStr = enterText.substring(enterText.length()-3, enterText.length());
					String baseStr = enterText.substring(0, enterText.length()-3);
					lastStr = lastStr.replace("走你", "");
					baseStr+=lastStr;
					et_secretary_form_content.setText(baseStr); 
					et_secretary_form_content.setSelection(baseStr.length());
					
					//2.模拟点击提交操作
					/*onClick(btn_secretary_commit_form);*/
					return;
				  }
			}
			et_secretary_form_content.setText(enterText); 
			et_secretary_form_content.setSelection(enterText.length());
			
		}
		 
	 };
	 
	 
	 
	

	 /**
	  * 听写识别 的监听器
	  */
 	//听写监听器
 	 RecognizerListener mRecoListener = new RecognizerListener(){
 	//听写结果回调接口(返回Json格式结果，用户可参附录13.1)；
 	//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
 	//关于解析Json的代码可参见Demo中JsonParser类；
 	//isLast等于true时会话结束。 
		@Override
		public IBinder asBinder() {
			return null;
		}

		@Override
		public void onError(int arg0) throws RemoteException {
			
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
				throws RemoteException {
			
		}

		@Override
		public void onResult(RecognizerResult result, boolean isLast)             //com.iflytek.speech.RecognizerResult
				throws RemoteException {
			
			
			et_secretary_form_content.setText("来自RecognizerListener返回的结果 "+result.getResultString()); 
			
		}	
		//volume量值0~30，data频数据 
		public void onVolumeChanged(int volume, byte[] data){
		
		}

		@Override
		public void onBeginOfSpeech() throws RemoteException {
			
		}

		@Override
		public void onEndOfSpeech() throws RemoteException {
		} 
    };
    
//	@Override
//	public void onClick(View v) {
//		switch(v.getId()){
//		case  R.id.btn_secretary_commit_form : 
//			
//			String content=et_secretary_form_content.getText().toString();
//			if (TextUtils.isEmpty(content)) {
//				CustomToast.show(activity, R.string.tip,
//						R.string.please_enter_task);
//				return;
//			}
//			Log.e("content", content);
//			Map<String, String> map=new HashMap<String, String>();
//			map.put("content", content);
//			map.put("type", type);
//			OkHttp.asyncPost(GlobalValue.URL_SECRETARY_QUESTION, map, new Callback() {
//				@Override
//				public void onResponse(Response response) throws IOException {
//					handleOk.sendEmptyMessage(0);
//				}
//				@Override
//				public void onFailure(Request arg0, IOException arg1) {
//	
//				}
//			});
//			dialogpg = new ProgressDialog(activity, R.style.Translucent_NoTitle);
//			dialogpg.setMessage("正在提交中,请耐心等候");
//			dialogpg.show();
//		break;
//		}
//	}
}	
