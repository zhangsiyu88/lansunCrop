package com.lansun.qmyo.utils;

import android.util.Log;

import com.autonavi.tbt.IFrameForTBT;

public class LogUtils {

	private static boolean debug = true;
	private static String mOne;
	private static String mtwo;
	
	public  LogUtils(){
	}
    public  LogUtils(String one ,String two){
		this.mOne = one;
		this.mtwo = two;
	}
    
    
	public static void toDebugLog(String s1,String s2){
		if(debug == true){
			Log.d(s1, s2);
		}else{
			//doNothing
		}
	}
}
