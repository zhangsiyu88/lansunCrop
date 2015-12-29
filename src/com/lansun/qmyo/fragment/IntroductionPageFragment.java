package com.lansun.qmyo.fragment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.CommonPagerAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.service.AccessTokenService;
import com.lansun.qmyo.utils.CommitStaticsinfoUtils;
import com.lansun.qmyo.utils.DataUtils;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**
 * 欢迎首页
 * 
 * @author bhxx
 * 
 */
public class IntroductionPageFragment extends BaseFragment implements
		OnPageChangeListener {

	@InjectAll
	Views v;
	private ArrayList<ImageView> imageViewList;

	class Views {
		private ViewPager viewpager;
		private LinearLayout ll_points;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View tv_introduction_jump, ll_exp_now;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		App.app.setData("isFirst", "true");
		
		/*
		 * 为解决首次安装app后，home键退至后台，再使用桌面的图标进行启动后，由于服务冲突，导致出现长时间的绿色欢迎页面的情况
		 * 那么，首次安装后不进行正常退出app的行之前，要求每当Home键退至后台时，需将服务停掉，回到前台时，重新开启服务
		 */
		App.app.setData("firstUseApp", "true");
		
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_introduction, null);
		
		
		
		/**
		 * 上传渠道包的标示内容
		 * 
		 * 1.拿到写在MEAT-INF文件中的不同的标示code
		 * 2.从本地拿到该手机的IMEI号码
		 * 3.拿到定位成功后需要的地址
		 * 4.将上面获取到的两个参数，一起作为接口的参数，上传到服务器上
		 */
		
		/*String phoneIMEI = getPhoneIMEI();
		String channelCode = getChannelCode(App.getInstance());
		int intChannelCode = 0;
		if(channelCode.equals("default")){
			//NO-OP
		}else{
			intChannelCode = Integer.valueOf(channelCode);
		}
		String cityName = App.app.getData("cityName");
		
		LogUtils.toDebugLog("start", "phoneIMEI :"+phoneIMEI);
		LogUtils.toDebugLog("start", "channelCode :"+channelCode);
		//CustomToast.show(activity, "渠道号", channelCode);
		LogUtils.toDebugLog("start", "cityName :"+cityName);
		
		//网络请求
		
	    //http://appapi.qmyo.org/statistic/collection?device=Android&device_id=IMEI_here1&platform=10&city=云南
		
		HttpUtils httpUtils = new HttpUtils();
		RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException e, String result ) {
			}
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.toDebugLog("start", "已提交至后台记录");
			}
		};
		
		httpUtils.send(HttpMethod.POST, "http://appapi.qmyo.com/statistic/collection?" +
				"device=Android" +
				"&device_id="+phoneIMEI+
				"&platform="+intChannelCode+
				"&city="+cityName , null,requestCallBack );*/
		
		//上方代码
		CommitStaticsinfoUtils.commitStaticsinfo(1);
		
		
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	private void click(View view) {
		MainFragment fragment = new MainFragment();
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	ViewPagerAdapter adapter = new ViewPagerAdapter();

	@InjectInit
	private void init() {
		prepareData();
		v.viewpager.setAdapter(adapter);
		
		//v.ll_points.getChildAt(0).setEnabled(true);
		v.ll_points.getChildAt(0).setBackgroundResource(R.drawable.oval_select); 
		v.viewpager.setOnPageChangeListener(this);
		v.viewpager.setCurrentItem(0);
	}

	/**
	 * 准备数据
	 */
	private void prepareData() {
		imageViewList = new ArrayList<ImageView>();
		int[] imageResIDs = getImageResIDs();

		ImageView iv;
		View view;
		
		// 添加点view对象
		for (int i = 0; i < imageResIDs.length; i++) {
			iv = new ImageView(activity);
			/*iv.setBackgroundResource(imageResIDs[i]);*/
			iv.setImageResource(imageResIDs[i]);
			iv.setScaleType(ScaleType.CENTER_INSIDE);
			imageViewList.add(iv);
			view = new View(activity);
			view.setBackgroundDrawable(getResources().getDrawable(R.drawable.oval_nomal));
			LayoutParams lp = new LayoutParams(8, 8);
			lp.leftMargin = 8;
			view.setLayoutParams(lp);
			view.setEnabled(false);
			v.ll_points.addView(view);
		}
		
	}

	private int[] getImageResIDs() {
		return new int[] { R.drawable.jieshao_shopping,
				R.drawable.jieshao_holiday, R.drawable.jieshao_more,R.drawable.jieshao_tiyan};
	}

	class ViewPagerAdapter extends CommonPagerAdapter {

		/**
		 * 创建一个view
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(imageViewList.get(position));
			return imageViewList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(imageViewList.get(position));
		}

		@Override
		public int getCount() {
			return imageViewList.size();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		if (position == imageViewList.size() - 1) {
			v.ll_exp_now.setVisibility(View.VISIBLE);
		} else {
			v.ll_exp_now.setVisibility(View.GONE);
		}
		// 切换选中的点
		for (int i = 0; i < v.ll_points.getChildCount(); i++) {
			//进来就首先将所有的点的背景都置为 app背景色
			v.ll_points.getChildAt(i).setBackgroundResource(
					R.drawable.oval_nomal);
			
		}
		//然后再将当前的位置所在的点置为选中颜色
		v.ll_points.getChildAt(position).setBackgroundResource(
				R.drawable.oval_select); 
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	/*
	 * 手机IMEI唯一标示符
	 */
	public String  getPhoneIMEI(){
		TelephonyManager TelephonyMgr = (TelephonyManager)(App.app.getSystemService("phone")); 
		
		String szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		//CustomToast.show(getApplicationContext(), "deviceImei", szImei);
		LogUtils.toDebugLog("imei", "设备的deviceImei： "+szImei);
		
		return szImei;
	}
	
	public String getChannelCode(Context context){
		String mChannel = "";
		if(!TextUtils.isEmpty(mChannel)){
	        return mChannel;
	    }
	    mChannel = "default";
	 
	    ApplicationInfo appinfo = context.getApplicationInfo();
	    String sourceDir = appinfo.sourceDir;
	    Log.d("getChannel sourceDir", sourceDir);
	 
	    ZipFile zf = null;
	    InputStream in = null;
	    ZipInputStream zin = null;
	 
	    try {
	        zf = new ZipFile(sourceDir);
	        in = new BufferedInputStream(new FileInputStream(sourceDir));
	        zin = new ZipInputStream(in);
	 
	        ZipEntry ze;
	        Enumeration<?> entries = zf.entries();
	 
	        while (entries.hasMoreElements()) {
	            ZipEntry entry = ((ZipEntry) entries.nextElement());
	            Log.d("getChannel getName", entry.getName());
	            if( entry.getName().equalsIgnoreCase("META-INF/channel_info")){
	                long size = entry.getSize();
	                if (size > 0) {
	                    BufferedReader br = new BufferedReader( new InputStreamReader(zf.getInputStream(entry)));
	                    String line;
	                    while ((line = br.readLine()) != null) {
	                        mChannel = line;
	                    }
	                    br.close();
	                }
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    finally {
	        if(in != null){
	            try {
	                in.close();
	            }
	            catch (Exception e){
	            }
	        }
	        if(zin != null){
	            try {
	                zin.closeEntry();
	            }
	            catch (Exception e){
	            }
	        }
	 
	        if(zf != null){
	            try {
	                zin.closeEntry();
	            }
	            catch (Exception e){
	            }
	        }
	        try {
				zf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    Log.d("getChannel", mChannel);
	    return mChannel;
	}
	

}