package com.lansun.qmyo.db;


import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lansun.qmyo.domain.ReportContentBean;
import com.lansun.qmyo.utils.LogUtils;


public class DraftBoxDBOpenHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "_DraftBox";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "_ActivityAndShopDraftBox";
	private final static String FIELD_ID = "_id";
	private final static String FIELD_ACTIVITY_ID = "_activity_id";
	private final static String FIELD_SHOP_ID = "_shop_id";
	private final static String FIELD_CONTENT = "_content";
	
	public  SQLiteDatabase db;
	private String string;

	public DraftBoxDBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "Create table " + TABLE_NAME + "(" + FIELD_ID
				+ " integer primary key autoincrement," + FIELD_ACTIVITY_ID + ","
				+ FIELD_SHOP_ID + "," + FIELD_CONTENT +")";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
		db.execSQL(sql);
	}

	/**
	 * 根据活动的acId到本地数据库中进行查询
	 * @param acId
	 * @return
	 */
	public boolean queryItemByAcId(int acId){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("select * from " + TABLE_NAME+" where "+ FIELD_ACTIVITY_ID +"=?" , new String[] {acId+""} );
		// 需要时刻警觉着 cursor.moveToNext()的重复操作，造成拿到的  数据缺失  的情况
		if(!c.moveToNext()){//表明没有查到包含该AcId的列内容,证明当前的活动还没有被当前这个用户 举报
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 
	 * @param shopId
	 * @return
	 */
	public boolean queryItemByShopId(int shopId){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("select * from " + TABLE_NAME+" where "+ FIELD_SHOP_ID +"=?" , new String[] {shopId+""} );
		// 需要时刻警觉着 cursor.moveToNext()的重复操作，造成拿到的  数据缺失  的情况
		if(!c.moveToNext()){//表明没有查到包含该AcId的列内容,证明当前的活动还没有被当前这个用户 举报
			LogUtils.toDebugLog("cursor", "cursor没有下一波数据" );
			return false; 
		}else{
			LogUtils.toDebugLog("cursor", "cursor等待轮询下一波数据" );
			return true;
		}
	}
	
	public ArrayList<ReportContentBean> getAll() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
		ArrayList<ReportContentBean> agendas = toArrayList(cursor);//内部方法内容
		db.close();
		return agendas;
	}

	
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = {Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
		db.close();
	}
	
	
	/**
	 * 根据activity_id 或者 shop_id 进行删除表中的 某一列内容
	 * @param id
	 */
	public void deleteByAcId(int acId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ACTIVITY_ID + "=?";
		String[] whereValue = {Integer.toString(acId) };
		db.delete(TABLE_NAME, where, whereValue);
		db.close();
	}
	public void deleteByShopId(int shopId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_SHOP_ID + "=?";
		String[] whereValue = {Integer.toString(shopId) };
		db.delete(TABLE_NAME, where, whereValue);
		db.close();
	}
	
	
	

	public void update(ReportContentBean a) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(a.getId()) };
		ContentValues cv = new ContentValues();
		cv.put(FIELD_ACTIVITY_ID, a.getActivity_id());
		cv.put(FIELD_SHOP_ID, a.getShop_id());
		cv.put(FIELD_CONTENT, a.getContent());
		db.update(TABLE_NAME, cv, where, whereValue);
		db.close();
	}

	public long insert(ReportContentBean a) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_ACTIVITY_ID, a.getActivity_id());
		cv.put(FIELD_SHOP_ID, a.getShop_id());
		cv.put(FIELD_CONTENT, a.getContent());
		long row = db.insert(TABLE_NAME, null, cv);
		db.close();
		return row;
	}

	private ArrayList<ReportContentBean> toArrayList(Cursor c) {
		ArrayList<ReportContentBean> arr = new ArrayList<ReportContentBean>();
		while (c.moveToNext()){
			ReportContentBean a = new ReportContentBean();
			a.setId(c.getInt(0));
			a.setActivity_id(c.getString(1));
			a.setShop_id(c.getString(2));
			a.setContent(c.getString(3));
			arr.add(a);
		}
		return arr;
	}

}

