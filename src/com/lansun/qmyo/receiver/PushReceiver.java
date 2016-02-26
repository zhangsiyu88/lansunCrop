package com.lansun.qmyo.receiver;


import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;


import com.google.gson.Gson;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.SplashActivity;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.JumpTag;
import com.lansun.qmyo.utils.ExampleUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.NotifyUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

/**
 * 推送的receiver
 * 
 * @author Yeun.zhang
 * 
 */
public class PushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPushToQmyoActivity";
	private JumpTag jTag;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 Bundle bundle = intent.getExtras();
			Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			/*String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Log.d("ab" + "", "[MyReceiver] 接收Registration Id : " + regId);*/
			  String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
	          Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
	            
		}else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            
        	
        	NotifyUtils.mContext = context;
            NotifyUtils.getInstance().sendNotifictionCounts();
        	context.sendBroadcast(new Intent("com.lansun.qmyo.message"));//LGP:Little Green Point
//        	context.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));//LGP:Little Green Point
			LogUtils.toDebugLog("infos", "我的右上角绿色提示点展示");
			
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            
            
            
            Log.d(TAG, "[MyReceiver] 接收到通知中涉跳转页面指向: "+bundle.getString(JPushInterface.EXTRA_EXTRA));
            Gson gson = new Gson();
            jTag = gson.fromJson(bundle.getString(JPushInterface.EXTRA_EXTRA).toString(), JumpTag.class);
            
            
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	//在广播接收者中进行对通知栏的点击接收，做到对应触摸的操作
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            
        	//打开自定义的Activity
        	Intent i = new Intent(context, MainActivity.class);
        	bundle.putString("message", "message");
        	i.putExtras(bundle);
        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );//Activity的逻辑问题
        	context.startActivity(i);
        	
        	//非登录用户（体验用户或尴尬用户）点击推送过来的信息是无效跳转，只能进入登陆注册页面
    	    //       a.临时secret  b.正常secret  c.token    d.user对象    
    		/*
    		 * 正式用户：                   空		非空			非空			非空
    		 */
    		/*
    		 * 体验用户：                 非空                     空                               非空                           非空
    		 */
    		/*
    		 * 三无用户：		 空                         空                               空			空
    		 */
        	
        	//满足下面所有的并 的条件为true 才能算作登录账户 ，那么其他自然为    非登录状态
        	boolean condition4Login = 
        			  (
        			  (!TextUtils.isEmpty(App.app.getData("secret")))
        			&&(TextUtils.isEmpty(App.app.getData("exp_secret")))
        			&&(!TextUtils.isEmpty(App.app.getData("access_token")))
        			&&(GlobalValue.user!=null)
        			  );
        	
        	if(!condition4Login){
        		context.sendBroadcast(new Intent("com.lansun.qmyo.Click2RegisterFragment"));
        		Toast.makeText(context, "请登录或注册后，查看信息详情", Toast.LENGTH_LONG).show();
        		return;
        	}
        	Gson gson = new Gson();
            jTag = gson.fromJson(bundle.getString(JPushInterface.EXTRA_EXTRA).toString(), JumpTag.class);
        	switch(jTag.getKey()){
        	case "secretary":
        		context.sendBroadcast(new Intent("com.lansun.qmyo.Click2MineSecretaryListFragment"));
        		break;
        	case "maijie":
        		context.sendBroadcast(new Intent("com.lansun.qmyo.Click2MessageCenterFragment"));
        		break;
        	case "activity":
        		Intent intent2Activity = new Intent("com.lansun.qmyo.Click2ActivityDetailFragment");
        		intent2Activity.putExtra("activity_id", jTag.getActivity_id());
        		intent2Activity.putExtra("shop_id", jTag.getShop_id());
        		context.sendBroadcast(intent2Activity);
        		break;
        	}
            /*
             * 当推送过来的信息（通知栏上展示的玩意）打开之后，我们重新计算一下除推送信息之外在后台存有的（除评论信息外）信息数目
             * 当数目大于0时，小绿点打开；数目小于0时，默认小绿点关闭点
             */
//            NotifyUtils.sendNotifictionCounts();
        	NotifyUtils.mContext = context;
            NotifyUtils.getInstance().sendNotifictionCounts();
			LogUtils.toDebugLog("infos", "点击推送消息后，进入我的页面");
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
            
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
		
	}
	

	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		if (MainActivity.isForeground) {
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
			
			msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					if (null != extraJson && extraJson.length() > 0) {
						msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
					}
				} catch (JSONException e) {
				}
			}
			context.sendBroadcast(msgIntent);
		}
	}
	
	// 打印所有的 intent extra 数据
		private static String printBundle(Bundle bundle) {
			StringBuilder sb = new StringBuilder();
			for (String key : bundle.keySet()) {
				if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
					sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
				}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
					sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
				} 
				else {
					sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
				}
			}
			return sb.toString();
		}
}
