package com.lansun.qmyo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtils {

	public static  String dataConvert(long currentTime){
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss");     
		Date curDate = new Date(currentTime);
		String str = formatter.format(curDate);
		return str;
		
	}
}
