package com.lansun.qmyo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshLocationReceiver extends BroadcastReceiver {


	/**
     * 自定义广播接收者类    暂时未使用
     * 
     *  @author Yeun
     */
  
        /**
         * 空构造函数,用于初始化Recevier
         */
        public RefreshLocationReceiver() {

        }

        private void disposeUpdateDataAction(Intent intent) {
            //书写更新控件数据逻辑
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            //接收到广播时，需要将位置进行更新
           /* if (Constants.INTENT_ACTION_UPDATE_DATA.equals(intent.getAction()))
                disposeUpdateDataAction(intent);
            
            else if (Constants.INTENT_ACTION_UPDATE_TIME.equals(intent
                    .getAction()))
             RunCustomActivity.this.updateTime();*/
        }
}
