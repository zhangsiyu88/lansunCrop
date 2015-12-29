package com.lansun.qmyo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lansun.qmyo.domain.Sensitive;

public class DBSensitiveUtils {

	 public static List<Sensitive> getToDbData(Context activity){  
		  List<Sensitive> sensitiveList = new ArrayList<Sensitive>();
		  String path = activity.getCacheDir().getPath()+File.separator+"qmyo_sensitive_new.db";
		  SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);  
		  List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
         //Cursor cursor = db.query("person", null, null, null, null, null, null);  
         Cursor cursor=db.query("com_qmyo_domain_Sensitive_new", null,null, null, null, null, "_id desc");  
       
         while(cursor.moveToNext()){  
             Map<String, Object> map = new HashMap<String, Object>();  
             Sensitive sens = new Sensitive();
             int id = cursor.getInt(cursor.getColumnIndex("_id"));  
             String name = cursor.getString(cursor.getColumnIndex("name"));  
             
             map.put("_id", id);  
             map.put("name", name);  
             list.add(map);  
             
             sens.set_id(id);
             sens.setName(name);
             sensitiveList.add(sens);
         }
		return sensitiveList;
	 }
}
