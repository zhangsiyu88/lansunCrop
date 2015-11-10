package com.lansun.qmyo.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.android.pc.ioc.app.Ioc;
import com.android.pc.ioc.db.sqlite.DbUtils;
import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_File;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_SharedPreferences;


import com.lansun.qmyo.domain.Address;
import com.lansun.qmyo.domain.Address2;
import com.lansun.qmyo.domain.Address3;
import com.lansun.qmyo.domain.AddressList;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.utils.GlobalValue;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;


public class App extends Application {
	public static final String[] TAGS=new String[]{"shopping","travel","food","car","happy","life","core","investment"};
	public static App app;
	public static List<String> search_list_history=new ArrayList<String>();
	public static App getInstance() {
		return app;
	}
	
	
	@Override
	public void onCreate() {
		
		Ioc.getIoc().init(this);
		
		JPushInterface.init(getApplicationContext());
		Log.i("很明显，程序一旦某机上安装成功，便获取到唯一的RegisterId","");
		 
		super.onCreate();
		initHistory();
		initImageLoader(getApplicationContext());
		
		app = this;
		// JPushInterface.init(this);
		
		/*if (TextUtils.isEmpty(App.app.getData("HasCity"))) {*/
	
		
//		if (true){
//			// 网络获取城市
//			InternetConfig config = new InternetConfig();
//			config.setKey(0);
//			System.out.println("下面去访问网络,去拿所有省市区的信息回来!!!!");
//			FastHttpHander.ajaxGet(GlobalValue.URL_AREA_ALL, config, this);//前往服务器
//			Handler_Inject.injectFragment(this, null);
//			
//			DbUtils.create(this, getCacheDir().getPath(), "province");
//			DbUtils.create(this, getCacheDir().getPath(), "city");
//			DbUtils.create(this, getCacheDir().getPath(), "area");
//		}
		
		/*	try {
				if (!new File(getCacheDir().getPath() + "/qmyo_sensitive.db").exists()) {
					Handler_File.writeFile(getCacheDir().getPath()+ "/qmyo_sensitive.db", getResources().getAssets()
							.open("qmyo_sensitive.db"));
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}*/

	}

	public void initHistory() {
		String first_history=getData("first_history");	
		String second_history=getData("second_history");	
		String third_history=getData("third_history");
//		if (App.search_list_history.size()>=4) {
//			App.search_list_history.remove(3);
//		}
		if (!TextUtils.isEmpty(first_history)) {
			addHistory(first_history);
		}
		if (!TextUtils.isEmpty(second_history)) {
			addHistory(second_history);
//			search_list_history.add(second_history);
		}
		if (!TextUtils.isEmpty(third_history)) {
			addHistory(third_history);
//			search_list_history.add(third_history);
		}
	}
	public void addHistory(String search_name){
		for (String search : App.search_list_history) {
			if (search.equals(search_name)) {
				return;
			}
		}
		App.search_list_history.add(0, search_name);
		if (App.search_list_history.size()>=4) {
			App.search_list_history.remove(3);
		}
	}
	public String inputStream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * 保存访问服务器后返回回来的省市区信息,供后面的wheelview使用
	 * @param json
	 */
//	private void saveCity(String json) {
//		final AddressList list = Handler_Json.JsonToBean(AddressList.class,
//				json);
//		if (list.getData() != null) {
//
//			new Thread() {
//				public void run() {
//					for (int i = 0; i < list.getData().size(); i++) {
//						Address address = list.getData().get(i);
//						if (list.getData().get(i).getItems() != null) {
//							address.setCityCount(list.getData().get(i)
//									.getItems().size());
//						}
//					/*	new Thread(new Runnable() {
//							@Override
//							public void run() {
//								Ioc.getIoc().getDb(getCacheDir().getPath(), "province").saveBindingId(address);
//							}
//						}).start();*/
//						
//						Ioc.getIoc().getDb(getCacheDir().getPath(), "province").saveBindingId(address);
//						
//						
//
//						// 市
//						if (list.getData().get(i).getItems() != null) {
//							for (int j = 0; j < list.getData().get(i)
//									.getItems().size(); j++) {
//								 Address2 address2 = list.getData().get(i)
//										.getItems().get(j);
//								if (list.getData().get(i).getItems() != null
//										&& list.getData().get(i).getItems()
//												.get(j).getItems() != null) {
//									address2.setAreaCount(list.getData().get(i)
//											.getItems().get(j).getItems()
//											.size());
//								}
//								/*System.out.println("city" + address2.getName());*/
//								
//								/*new Thread(new Runnable() {
//									@Override
//									public void run() {
//										Ioc.getIoc().getDb(getCacheDir().getPath(), "city").saveBindingId(address2);
//									}
//								}).start();*/
//								
//								Ioc.getIoc().getDb(getCacheDir().getPath(), "city").saveBindingId(address2);
//								
//								// 区
//								if (list.getData().get(i).getItems().get(j)
//										.getItems() != null) {
//									for (int k = 0; k < list.getData().get(i)
//											.getItems().get(j).getItems()
//											.size(); k++) {
//										 Address3 address3 = list.getData()
//												.get(i).getItems().get(j)
//												.getItems().get(k);
//										
//										
//										/*new Thread(new Runnable() {
//											@Override
//											public void run() {
//												Ioc.getIoc().getDb(getCacheDir().getPath(),"area").saveBindingId(address3);
//											}
//										}).start();*/
//										
//										Ioc.getIoc().getDb(getCacheDir().getPath(),"area").saveBindingId(address3);
//									}
//								}
//							}
//						}
//					}
//
//				};
//			}.start();
//		}
//	}

	/**
	 * 初始化ImageLoader
	 * @param context
	 */
	private void initImageLoader(Context context) {
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				"qmyo/Cachee");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				.memoryCacheExtraOptions(720, 1080)
				// 缓存图片的大小
				.threadPoolSize(5)
				// 线程池数量
				.threadPriority(Thread.NORM_PRIORITY - 2)
				// 线程的优先等级
				.denyCacheImageMultipleSizesInMemory()
				// 缓存图片在内存
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.memoryCache(new UsingFreqLimitedMemoryCache(16 * 1024 * 1024))
				// 内存缓存大小
				.memoryCacheSize(16 * 1024 * 1024)
				// 内存缓存最大值
				.diskCacheSize(16 * 1024 * 1024)
				// SD卡缓存最大值
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCache(new UnlimitedDiskCache(cacheDir))
				.imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 3000))
				.writeDebugLogs().build();
		
		ImageLoader.getInstance().init(config);
	}

	/**
	 * 数据存储到本地数据库
	 * 
	 * @param key
	 * @param value
	 * @return void
	 */
	public void setData(String key, String value) {
		Handler_SharedPreferences.WriteSharedPreferences("Cache", key, value);
	}

	/**
	 * 取出本地数据
	 * 
	 * @param key
	 * @return
	 * @return String
	 */
	public String getData(String key) {
		return Handler_SharedPreferences.getValueByName("Cache", key,
				Handler_SharedPreferences.STRING).toString();
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			System.out.println("什么是省市区的数据,让我来告诉你吧!!!!");
			switch (r.getKey()) {

//			case 0://拿省市区地数据库就是为了后面的WheelView来使用的
//				final AddressList list = Handler_Json.JsonToBean(AddressList.class, r.getContentAsString());
//				System.out.println("什么是省市区的数据,让我来告诉你吧!!!!"+r.getContentAsString());
//				System.out.println("什么是省市区的数据,让我来告诉你吧!!!!"+list.toString());
//				
//				if (list.getData() != null) {
//					new Thread() {
//						public void run() {
//							for (int i = 0; i < list.getData().size(); i++) {
//								Address address = list.getData().get(i);
//								if (list.getData().get(i).getItems() != null) {
//									address.setCityCount(list.getData().get(i)
//											.getItems().size());
//								}
//								Ioc.getIoc().getDb(getCacheDir().getPath(),"province").saveBindingId(address);
//
//								// 市
//								if (list.getData().get(i).getItems() != null) {
//									for (int j = 0; j < list.getData().get(i)
//											.getItems().size(); j++) {
//										Address2 address2 = list.getData()
//												.get(i).getItems().get(j);
//										
//										if (list.getData().get(i).getItems() != null
//												&& list.getData().get(i)
//														.getItems().get(j)
//														.getItems() != null) {
//											address2.setAreaCount(list
//													.getData().get(i)
//													.getItems().get(j)
//													.getItems().size());
//										}
//										
//										Ioc.getIoc().getDb(getCacheDir().getPath(),"city").saveBindingId(address2);
//										
//										// 区
//										if (list.getData().get(i).getItems()
//												.get(j).getItems() != null) {
//											for (int k = 0; k < list.getData()
//													.get(i).getItems().get(j)
//													.getItems().size(); k++) {
//												
//												Address3 address3 = list.getData().get(i).getItems().get(j).getItems().get(k);
//												
//												Ioc.getIoc().getDb(getCacheDir().getPath(),"area").saveBindingId(address3);
//											}
//										}
//									}
//								}
//							}
//
//						};
//					}.start();
//					
//					
//					App.app.setData("HasCity", "true");
//				}
//				break;
			}
		}
	}
}
