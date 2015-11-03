package com.lansun.qmyo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.lansun.qmyo.domain.area.CityBean;
import com.lansun.qmyo.domain.area.DegreeBean;
import com.lansun.qmyo.domain.area.JobBean;
import com.lansun.qmyo.domain.area.NationBean;



import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class DBManager {

	protected static final String TAG = DBManager.class.getSimpleName();

	public static final String PACKAGE_NAME = "com.lansun.qmyo";// 你的包名

	public static final String DB_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/"
			+ PACKAGE_NAME + "/" + "databases";

	private static final String DB_NAME = "datas.sqlite"; //省市区的数据库文件

	private static final String filename = DB_PATH + "/" + DB_NAME;

	private final String TABLE_NAME = "t_city";

	private final String JOB_TABLE_NAME = "t_job_type";

	private final String MAJOR_TABLE_NAME = "t_major";

	private final String INDUSTRY_TABLE_NAME = "t_industry";

	private final String DEGREE_TABLE_NAME = "t_degree";

	private final String SALARY_TABLE_NAME = "t_salary";

	private final String NATION_TABLE_NAME = "t_nation";

	private static DBManager dbManager;

	private Context context;

	private SQLiteDatabase sDatabase;

	private DBManager(Context context) {
		super();
		this.context = context;
		sDatabase = open(filename);
	}

	/**
	 * 把assets目录下数据库文件写入SDcard中
	 * 
	 * @param liutao
	 * @return
	 */
	private SQLiteDatabase open(String filename) {
		File file = new File(filename);

		if (file.exists()) {

			return SQLiteDatabase.openOrCreateDatabase(file, null);

		} else {
			File path = new File(DB_PATH);
			if (!path.exists()) {
				path.mkdirs();
			} else {
			}

			try {
				AssetManager am = context.getAssets();
				InputStream is = am.open("datas.sqlite");
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					Log.i(TAG, "得到");
					fos.write(buffer, 0, count);
				}
				fos.flush();
				fos.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
		return open(filename);
	}

	public static DBManager getNewInstance(Context context) {

		if (dbManager == null) {
			dbManager = new DBManager(context);
		}
		return dbManager;
	}

	/**
	 * 根据type查询城市中省份、市、区列表数据
	 * 
	 * @param type
	 * @return
	 */
	public List<CityBean> queryByProvince(String type) {
		List<CityBean> mCityBean = new ArrayList<CityBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + TABLE_NAME
				+ " where " + "type" + "=?", new String[] { type });

		while (cursor.moveToNext()) {
			CityBean cityBean = new CityBean();

			String id = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mType = cursor.getString(cursor.getColumnIndex("type"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			cityBean.setId(id);
			cityBean.setFid(fid);
			cityBean.setType(mType);
			cityBean.setName(name);
			mCityBean.add(cityBean);
		}
		cursor.close();

		return mCityBean;
	}

	/**
	 * 根据城市名查找id
	 * 
	 * @param name
	 * @return
	 */

	public List<CityBean> queryByName(String name) {
		List<CityBean> mCityBean = new ArrayList<CityBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + TABLE_NAME
				+ " where " + "name" + "=?", new String[] { name });

		while (cursor.moveToNext()) {
			CityBean cityBean = new CityBean();

			String id = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mType = cursor.getString(cursor.getColumnIndex("type"));
			String mName = cursor.getString(cursor.getColumnIndex("name"));
			cityBean.setId(id);
			cityBean.setFid(fid);
			cityBean.setType(mType);
			cityBean.setName(mName);
			mCityBean.add(cityBean);
		}
		cursor.close();

		return mCityBean;
	}

	/**
	 * 查询省对于的市数据
	 * 
	 * @param ProvinceId
	 */
	public List<CityBean> queryByProAndCity(String provinceId) {
		List<CityBean> mCityBean = new ArrayList<CityBean>();
		Cursor cursor = sDatabase.rawQuery("select * from " + TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { provinceId });

		while (cursor.moveToNext()) {
			CityBean cityBean = new CityBean();

			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mType = cursor.getString(cursor.getColumnIndex("type"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			cityBean.setId(mid);
			cityBean.setFid(fid);
			cityBean.setType(mType);
			cityBean.setName(name);
			mCityBean.add(cityBean);
		}
		cursor.close();

		return mCityBean;
	}

	/**
	 * 查询市对于区县信息
	 * 
	 * @param cityId
	 */
	public List<CityBean> queryByCityAndCounty(String cityId) {
		// TODO Auto-generated method stub
		List<CityBean> mCityBean = new ArrayList<CityBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { cityId });

		while (cursor.moveToNext()) {
			CityBean cityBean = new CityBean();

			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mType = cursor.getString(cursor.getColumnIndex("type"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			cityBean.setId(mid);
			cityBean.setFid(fid);
			cityBean.setType(mType);
			cityBean.setName(name);
			mCityBean.add(cityBean);
		}
		cursor.close();

		return mCityBean;
	}

	/**
	 * 查询职位类型
	 * 
	 * @param type
	 * @return
	 */
	public List<JobBean> queryByJobType(String type) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + JOB_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { type });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(fid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 根据职位类型查询职位
	 * 
	 * @param id
	 * @return
	 */
	public List<JobBean> queryByJobTypeAndJob(String id) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + JOB_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { id });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(fid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 根据职位名称查询职位id
	 * 
	 * @param name
	 * @return
	 */
	public List<JobBean> queryByJobName(String name) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + JOB_TABLE_NAME
				+ " where " + "name" + "=?", new String[] { name });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mName = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(fid);
			jBean.setName(mName);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 查询专业类型
	 * 
	 * @param id
	 * @return
	 */
	public List<JobBean> queryByMajorType(String type) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + MAJOR_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { type });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(fid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 根据专业类型查询专业
	 * 
	 * @param id
	 * @return
	 */
	public List<JobBean> queryByMajorTypeAndMajor(String id) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + MAJOR_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { id });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(fid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 查询行业类型
	 * 
	 * @param fid
	 * @return
	 */
	public List<JobBean> queryByIndustryType(String fid) {
		// TODO Auto-generated method stub
		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from "
				+ INDUSTRY_TABLE_NAME + " where " + "fid" + "=?",
				new String[] { fid });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String mfid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(mfid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 根据行业类型查询行业
	 * 
	 * @param id
	 * @return
	 */
	public List<JobBean> queryByTypeAndIndustry(String id) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from "
				+ INDUSTRY_TABLE_NAME + " where " + "fid" + "=?",
				new String[] { id });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String mfid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(mfid);
			jBean.setName(name);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 根据行业名称查询行业id
	 * 
	 * @param name
	 * @return
	 */
	public List<JobBean> queryByIndustryName(String name) {

		List<JobBean> mJobBean = new ArrayList<JobBean>();

		Cursor cursor = sDatabase.rawQuery("select * from "
				+ INDUSTRY_TABLE_NAME + " where " + " name " + " like " + "?",
				new String[] { "%" + name + "%" });

		while (cursor.moveToNext()) {

			JobBean jBean = new JobBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String mfid = cursor.getString(cursor.getColumnIndex("fid"));
			String mName = cursor.getString(cursor.getColumnIndex("name"));
			jBean.setId(mid);
			jBean.setFid(mfid);
			jBean.setName(mName);
			mJobBean.add(jBean);
		}
		cursor.close();

		return mJobBean;
	}

	/**
	 * 查询学历
	 * 
	 * @param type
	 * @return
	 */
	public List<DegreeBean> queryByDegreeType(String type) {

		List<DegreeBean> mDegBean = new ArrayList<DegreeBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + DEGREE_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { type });

		while (cursor.moveToNext()) {

			DegreeBean dBean = new DegreeBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			dBean.setId(mid);
			dBean.setName(name);
			mDegBean.add(dBean);
		}
		cursor.close();

		return mDegBean;
	}

	/**
	 * 查询工资
	 * 
	 * @param type
	 * @return
	 */
	public List<DegreeBean> queryBySalaryType(String type) {

		List<DegreeBean> mDegBean = new ArrayList<DegreeBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + SALARY_TABLE_NAME
				+ " where " + "fid" + "=?", new String[] { type });

		while (cursor.moveToNext()) {

			DegreeBean dBean = new DegreeBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			dBean.setId(mid);
			dBean.setName(name);
			mDegBean.add(dBean);
		}
		cursor.close();

		return mDegBean;
	}

	/**
	 * 查询工资
	 * 
	 * @param type
	 * @return
	 */
	public List<DegreeBean> queryBySalaryName(String name) {

		List<DegreeBean> mDegBean = new ArrayList<DegreeBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + SALARY_TABLE_NAME
				+ " where " + "name" + "=?", new String[] { name });

		while (cursor.moveToNext()) {

			DegreeBean dBean = new DegreeBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String fid = cursor.getString(cursor.getColumnIndex("fid"));
			String mName = cursor.getString(cursor.getColumnIndex("name"));
			dBean.setId(mid);
			dBean.setName(mName);
			mDegBean.add(dBean);
		}
		cursor.close();

		return mDegBean;
	}

	/**
	 * 查询民族
	 * 
	 * @param id
	 * @return
	 */
	public List<NationBean> queryByNation(String id) {

		List<NationBean> mNaBean = new ArrayList<NationBean>();

		Cursor cursor = sDatabase.rawQuery("select * from " + NATION_TABLE_NAME
				+ " where " + "id" + "=?", new String[] { id });

		while (cursor.moveToNext()) {

			NationBean nBean = new NationBean();
			String mid = cursor.getString(cursor.getColumnIndex("id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			nBean.setId(mid);
			nBean.setName(name);
			mNaBean.add(nBean);
		}
		cursor.close();

		return mNaBean;
	}

	public static void fileExists() {

		File f = new File(filename);
		if (f.exists()) {
			// 删除文件
			f.delete();
		}
	}
}
