package com.lansun.qmyo.view;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.dd.CircularProgressButton;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.UpdateAppVersionInfo;
import com.lansun.qmyo.fragment.ExperienceSearchFragment;
import com.lansun.qmyo.fragment.FoundFragment;
import com.lansun.qmyo.fragment.HomeFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.fragment.SecretaryFragment;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogFragment;


/**
 * 体验dialog
 * 
 * @author bhxx
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @SuppressLint("ValidFragment")
public class UpdateAppVersionDialog extends BlurDialogFragment {

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	.displayer(new FadeInBitmapDisplayer(300))
	.displayer(new RoundedBitmapDisplayer(10)).build();
	
	PackageManager manager;
/*	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.displayer(new RoundedBitmapDisplayer(10)).build();*/
	/*
	 * 回调接口
	 */
	public interface OnConfirmListener {
		void confirm();
	}

	@InjectAll
	Views v;
	private OnConfirmListener listener;
	private Bitmap mBitmap;
	
	private CircularProgressButton circularButton;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		//Button btn_expe_confirm;
		FrameLayout fr_glassfilter;
		RelativeLayout dialog_bg; 
		TextView  tv_tittle1,tv_tittle2,
		tv_desc_num1,tv_desc_content1,
		tv_desc_num2,tv_desc_content2, 
		tv_desc_num3,tv_desc_content3, 
		tv_desc_num4,tv_desc_content4, 
		tv_desc_num5,tv_desc_content5,tv_update_progress; 
		LinearLayout ll_desc_item1,
					 ll_desc_item2,
					 ll_desc_item3,
					 ll_desc_item4,
					 ll_desc_item5;
		ProgressBar pb_update;
		View line_splite;
		
		
		//, tv_expe_relogin
//		RecyclingImageView iv_exp_bankcard;
//		TextView tv_expe_desc;
		
	}

	public UpdateAppVersionDialog() {
		super();
		
		decodeResource = BitmapFactory.decodeResource(App.app.getResources(), R.drawable.listbg);
//		fastblur = fastblur(App.app.getApplicationContext(), decodeResource, 1);
		
		PackageInfo info = null;
		manager = App.app.getPackageManager();
		try {
		  info = manager.getPackageInfo(App.app.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		  e.printStackTrace();
		}
		
//		info.versionCode;
//		info.versionName;
//		info.packageName;
//		info.signatures;
		
		InternetConfig config = new InternetConfig();
		config.setKey(1);
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
//		params.put("key", "Android");
//		params.put("version", "1");
		
//		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION + info.versionCode , config, this);
		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION+"?key=Android&version="+info.versionCode , config, this);
		
	}

	public UpdateAppVersionDialog(String cardId2, String cardHeadPhotoUrl, String cardDesc, boolean isFirstEnter) {
		super();
	}

//	public UpdateAppVersionDialog(Bitmap bitmap) {
//		
//		this.mBitmap = bitmap;
//		
//		PackageInfo info = null;
//		manager = App.app.getPackageManager();
//		try {
//		  info = manager.getPackageInfo(App.app.getPackageName(), 0);
//		} catch (NameNotFoundException e) {
//		  e.printStackTrace();
//		}
//		InternetConfig config = new InternetConfig();
//		config.setKey(1);
////		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION + info.versionCode , config, this);
//		FastHttpHander.ajaxGet(GlobalValue.UPDATE_NOTIFICATION + 1 , config, this);
//	}

	public void setOnConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getDialog().getWindow().setDimAmount((float) 0.8);
		getDialog().setOnKeyListener(new OnKeyListener(){
			
           public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
           if (keyCode == KeyEvent.KEYCODE_BACK)
             return false; // pretend we've processed it//允许其返回
           else
             return false; // pass on to be processed as normal
         }
       });
		
		
		view = inflater.inflate(R.layout.dialog_update_new_version, container);
//		decodeResource = BitmapFactory.decodeResource(App.app.getResources(), R.drawable.listbg);
		
//		fastblur = fastblur(App.app.getApplicationContext(), mBitmap, 50);
//		@SuppressWarnings("deprecation")
//		Drawable drawable = new BitmapDrawable(fastblur);
//		drawable.setBounds(0, 0, 0, 0);
//		getDialog().getWindow().setBackgroundDrawable(drawable);
		getDialog().setCanceledOnTouchOutside(true);
		
		Handler_Inject.injectFragment(this, view);
		circularButton = (CircularProgressButton) view.findViewById(R.id.btn_update_app_confirm);
		circularButton.setText("点击升级哦");
		circularButton.setBackground(getResources().getDrawable(R.drawable.button));
		circularButton.setStrokeColor(getResources().getColor(R.color.update_text_tittle_green));
		
		 circularButton.setIndeterminateProgressMode(true);
	        circularButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	               /* if (circularButton.getProgress() == 0) {
	                    circularButton.setProgress(50);
	                } else if (circularButton.getProgress() == 100) {
	                    circularButton.setProgress(0);
	                } else {
	                    circularButton.setProgress(100);
	                }*/
	            	downloadFix(v);
	            }
	        });
		
		
//		Bitmap decodeResource = BitmapFactory.decodeResource(App.app.getResources(), R.drawable.glassfliter);
		
//		View fr_glassfilter = view.findViewById(R.id.fr_glassfilter);
//		View ll_dialog_content = view.findViewById(R.id.ll_dialog_content);
//		
//		LogUtils.toDebugLog("width", fr_glassfilter.getMeasuredWidth()+"");
//		LogUtils.toDebugLog("Height", fr_glassfilter.getMeasuredHeight()+"");
//		LogUtils.toDebugLog("width", "ll_dialog_content    "+ll_dialog_content.getMeasuredWidth()+"");
//		LogUtils.toDebugLog("Height", "ll_dialog_content   "+ll_dialog_content.getMeasuredHeight()+"");
		
//		v.dialog_bg.buildDrawingCache();
//		Bitmap bitmap = v.dialog_bg.getDrawingCache();
		
//		blur(mBitmap, , 5);
		
		
//		@SuppressWarnings("deprecation")
//		Drawable drawable = new BitmapDrawable(fastblur);
//		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
//		v.fr_glassfilter.setLayoutParams(layoutParams);
//		v.fr_glassfilter.setBackground(drawable);

		getDialog().setCancelable(false); 
		getDialog().setCanceledOnTouchOutside(false);
		
//		ImageLoader.getInstance().displayImage(mCardHeadPhotoUrl, v.iv_exp_bankcard,options);
//		v.tv_expe_desc.setText(mCardDesc);
		return view;
	}

	private void click(final View view) {
		switch (view.getId()) {
		case R.id.btn_expe_confirm://点击确定前去访问获取临时的exp_secret
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					download(view);
				}
			}).start();
			
			break;
			
		}
	}

	int cardId = 0;

	private UpdateAppVersionInfo updateInfo;
	private Bitmap fastblur;
	private HttpHandler<File> httpHandler;
	private Bitmap decodeResource;
	private View view;

	
	
	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 1://拿到更新的描述内容
				
				
				updateInfo = Handler_Json.JsonToBean(UpdateAppVersionInfo.class,r.getContentAsString());
				
				LogUtils.toDebugLog("updateInfo",updateInfo.getData().get(0).toString() );
				LogUtils.toDebugLog("updateInfo",updateInfo.getData().get(1).toString() );
				LogUtils.toDebugLog("updateInfo",updateInfo.getData().get(2).toString() );
				
				
				ArrayList<LinearLayout>  llList = new ArrayList<LinearLayout>();
				llList.add(v.ll_desc_item1);
				llList.add(v.ll_desc_item2);
				llList.add(v.ll_desc_item3);
				llList.add(v.ll_desc_item4);
				llList.add(v.ll_desc_item5);
				
				ArrayList<TextView>  tvList = new ArrayList<TextView>();
				tvList.add(v.tv_desc_content1);
				tvList.add(v.tv_desc_content2);
				tvList.add(v.tv_desc_content3);
				tvList.add(v.tv_desc_content4);
				tvList.add(v.tv_desc_content5);
				
				
				String tittle = updateInfo.getTitle();
				int indexOfBlank = tittle.indexOf(" ");
				String realTittle = tittle.substring(0, indexOfBlank);
				String realVer = tittle.substring(indexOfBlank+1, tittle.length());
				LogUtils.toDebugLog("updateInfo", realTittle);
				LogUtils.toDebugLog("updateInfo", realVer);
				
				
				//v.tv_tittle1.setText(realTittle);	
				v.tv_tittle2.setText(realVer);
				
				for(int i =0;i<updateInfo.getData().size();i++){
					
					llList.get(i).setVisibility(View.VISIBLE);
					String content = String.valueOf(updateInfo.getData().get(i).toString());
					((TextView) tvList.get(i)).setText(content.substring(2, content.length()));
					
					/*v.ll_desc_item1.setVisibility(View.VISIBLE);
					String content = String.valueOf(updateInfo.getData().get(i));
					v.tv_desc_content1.setText(content.substring(2, content.length()));*/
				}
				break;
			}
		}
	}
	
	 @SuppressLint("SdCardPath") public void download(View view){
		    HttpUtils http = new HttpUtils();
		    RequestCallBack<File> requestCallBack = new RequestCallBack<File>() {

				private int progress;

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					v.tv_update_progress.setText("下载失败,请重试");
					LogUtils.toDebugLog("updateInfo)", arg0.toString());
					
				}

				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					v.tv_update_progress.setText("下载完成");
					v.pb_update.setProgress(100);
					
					
					   Intent intent = new Intent(Intent.ACTION_VIEW);
					   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					   intent.setAction(android.content.Intent.ACTION_VIEW);
					//   String type = getMIMEType(f);
					//   intent.setDataAndType(Uri.fromFile(f), type);

					    intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk"),
					    "application/vnd.android.package-archive");
				        App.app.startActivity(intent);
				}
				@Override
				public void onStart() {
					//super.onStart();
					v.pb_update.setVisibility(View.VISIBLE);
					//v.line_splite.setVisibility(View.INVISIBLE);
					v.tv_update_progress.setVisibility(View.VISIBLE);
					v.tv_update_progress.setText("开始下载");
					File file = new File (Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk");
					LogUtils.toDebugLog("delete", file.getAbsolutePath());
					
					boolean delete = file.delete();
					if(delete){
						LogUtils.toDebugLog("delete", "删除成功");
					}else{
						LogUtils.toDebugLog("delete)", "删除失败");
					}
				}
				
				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					//super.onLoading(total, current, isUploading);
					
					if(current!=0.0&&total!=0.0){
						double result = (double)current/(double)total;
						String resultStr = String.valueOf(result);
						if(resultStr.length()>4){
							progress = Integer.valueOf(resultStr.substring(2, 4));
							v.pb_update.setProgress(progress);
							v.tv_update_progress.setText("下载中："+ progress+"%");
						}
						
						LogUtils.toDebugLog("updateInfo)", current+"");
						LogUtils.toDebugLog("updateInfo)", total+"");
						LogUtils.toDebugLog("updateInfo)", result+"");
					}
				}
				
			};
			LogUtils.toDebugLog("updateInfo.getUrl()", updateInfo.getUrl());
			LogUtils.toDebugLog("updateInfo.getUrl()", Environment.getExternalStorageDirectory().getAbsolutePath());
			
			httpHandler = http.download(updateInfo.getUrl(), 
					Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk", true, true, requestCallBack);

			//httpHandler.cancel(true);
		  }
	
	 
	  private void blur(Bitmap bkg, View view, float radius) {
	        Bitmap overlay = Bitmap.createBitmap(this.view.getMeasuredWidth(), this.view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(overlay);
	        canvas.drawBitmap(bkg, -view.getLeft(), -view.getTop(), null);
	        RenderScript rs = RenderScript.create(App.app);
	        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
	        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
	        blur.setInput(overlayAlloc);
	        blur.setRadius(radius);
	        blur.forEach(overlayAlloc);
	        overlayAlloc.copyTo(overlay);
	        view.setBackground(new BitmapDrawable(getResources(), overlay));
	        rs.destroy();
	    }
	  
	  
	  
		 @SuppressLint("SdCardPath") public void downloadFix(View view){
			    HttpUtils http = new HttpUtils();
			    RequestCallBack<File> requestCallBack = new RequestCallBack<File>() {

					private int progress;

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						v.tv_update_progress.setText("下载失败,请重试");
						LogUtils.toDebugLog("updateInfo)", arg0.toString());
						
					}

					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						v.tv_update_progress.setText("下载完成");
						v.pb_update.setProgress(100);
						circularButton.setProgress(100);
						
						   Intent intent = new Intent(Intent.ACTION_VIEW);
						   
						   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						   intent.setAction(android.content.Intent.ACTION_VIEW);
						//   String type = getMIMEType(f);
						//   intent.setDataAndType(Uri.fromFile(f), type);

						    intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk"),
						    "application/vnd.android.package-archive");
					        App.app.startActivity(intent);
					}
					@Override
					public void onStart() {
						//super.onStart();
						v.pb_update.setVisibility(View.VISIBLE);
						//circularButton.setBackgroundResource(R.drawable.button);
						circularButton.setText("点击升级哦");
						circularButton.setProgress(0);
						
						//v.line_splite.setVisibility(View.INVISIBLE);
						v.tv_update_progress.setVisibility(View.VISIBLE);
						v.tv_update_progress.setText("开始下载");
						File file = new File (Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk");
						
						LogUtils.toDebugLog("delete", Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk");
						LogUtils.toDebugLog("delete", file.getAbsolutePath());
						
						boolean delete = file.delete();
						if(delete){
							LogUtils.toDebugLog("delete", "删除成功");
						}else{
							LogUtils.toDebugLog("delete", "删除失败");
						}
					}
					
					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						//super.onLoading(total, current, isUploading);
						
						if(current!=0.0&&total!=0.0){
							double result = (double)current/(double)total;
							String resultStr = String.valueOf(result);
							if(resultStr.length()>4){
								progress = Integer.valueOf(resultStr.substring(2, 4));
								v.pb_update.setProgress(progress);
								circularButton.setProgress(progress);
								v.tv_update_progress.setText("下载中："+ progress+"%");
							}
							if(result==1.0){
								v.pb_update.setProgress(100);
							}
							
							LogUtils.toDebugLog("updateInfo)", current+"");
							LogUtils.toDebugLog("updateInfo)", total+"");
							LogUtils.toDebugLog("updateInfo)", result+"");
						}
					}
					
				};
				LogUtils.toDebugLog("updateInfo.getUrl()", updateInfo.getUrl());
				LogUtils.toDebugLog("updateInfo.getUrl()", Environment.getExternalStorageDirectory().getAbsolutePath());
				
				httpHandler = http.download(updateInfo.getUrl(), 
						Environment.getExternalStorageDirectory().getAbsolutePath()+"/maijie_newVer.apk", true, true, requestCallBack);

				//httpHandler.cancel(true);
			  }
	  
	 
}
