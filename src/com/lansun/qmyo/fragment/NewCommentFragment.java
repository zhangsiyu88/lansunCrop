package com.lansun.qmyo.fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.iflytek.cloud.Setting;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.DetailHeaderDeletePagerAdapter;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.adapter.UpLoadPhotoAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.event.entity.DeleteEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ImageGalleryDeleteDialog;
import com.lansun.qmyo.view.ImageGalleryDeleteDialog.OnNotifyGridViewListener;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.ImageGalleryDialog2;
import com.lansun.qmyo.view.MyGridView;
import com.ns.mutiphotochoser.GalleryActivity;
import com.ns.mutiphotochoser.constant.Constant;
import com.ns.mutiphotochoser.fragment.ImagePagerFragment;

public class NewCommentFragment extends BaseFragment implements OnNotifyGridViewListener{//implements openSelectPhotoListener

	@InjectAll
	Views v;
	private String shopId;
	private String activityId;
	private Button carema;
	private Button album;
	private Button give_up;

	protected static final int ACTION_IMAGE_CAPTURE = 2;
	protected static final int ACTION_IMAGE_PICK = 1;

	private ArrayList<String> files = new ArrayList<>();
	private UpLoadPhotoAdapter adapter;
	private DetailHeaderPagerAdapter headAdapter;
	private ImageGalleryDialog2 dialog;
	private SQLiteDatabase db;

	public  List<Sensitive> sensitiveList = new ArrayList<Sensitive>() ;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, iv_activity_shared,
				ll_new_comment_upload;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_title, tv_activity_shared,tv_wait_input_counts,
						 tv_rb_room_ratingbar_counts,tv_big_num;
		private EditText et_new_comment_content;
		@InjectBinder(listeners = { OnItemClick.class }, method = "itemClick")
		private MyGridView gv_new_comment_images;
		
		private RatingBar rb_room_ratingbar;
		

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_new_comment, null);
		Handler_Inject.injectFragment(this, rootView);
		
		v.et_new_comment_content.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after){
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length()<=15){
					v.tv_wait_input_counts.setText(String.format("您还需要输入%1$s个字", String.valueOf(15 - s.length())));
				}else{
					v.tv_wait_input_counts.setText(String.format("您还可以输入%1$s个字", String.valueOf(500 - s.length())));
				}
			}
		});
		
		
		v.rb_room_ratingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				v.tv_rb_room_ratingbar_counts.setText("您的评分为"+String.valueOf(rating).substring(0, 1)+"星");
				v.tv_big_num.setText(String.valueOf(rating).substring(0, 1));
			}
		});
		
		
		/**
		 * 给GridView设置上条目监听事件
		 */
		v.gv_new_comment_images.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position < 8){
					if(position == adapter.getCount()-1){//点击的位置小于8（即个数小于9），且是所填图的最后一个时
						upDataHead();
						return;
					}
				}else if(position == 8){//点击对象为9张图片中的最后一张图像
					View layout = LayoutInflater.from(activity).inflate(
		    				R.layout.custom_toast, null);
		    		TextView tv_title = (TextView) layout.findViewById(R.id.tv_title);
		    		TextView tv_content = (TextView) layout.findViewById(R.id.tv_content);
		    		tv_title.setText("选择上传的照片已满");
		    		tv_content.setText("最多上传9张照片哦");
		    		Toast toast = new Toast(activity);
		    		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		    		toast.setDuration(0);
		    		toast.setView(layout);
		    		toast.show();
		    		
		    		openPhotoDetails(8);
		    		return;
				}
				 openPhotoDetails(position);
			}
		});
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		
		initTitle(v.tv_activity_title, R.string.mine_comments,
				v.tv_activity_shared, R.string.publish);
		Bundle arguments = getArguments();
		if (arguments != null) {
			activityId = arguments.getString("activity_id");
			shopId = arguments.getString("shop_id");
		}
		adapter = new UpLoadPhotoAdapter(activity, files);
		//adapter.setOpenSelectPhotoListener(this);
		
		v.gv_new_comment_images.setAdapter(adapter);
		EventBus.getDefault().register(this);
		
		provinceSelector = Selector.from(Sensitive.class);
		provinceSelector.select("*");
		provinceSelector.limit(Integer.MAX_VALUE);
		
		/*
		 * 猜测： 读取数据库的速度 影响了整个UI进程的速度
		 * 初步解决办法： 在子线程中进行本地数据库的加载任务
		 */
		LogUtils.toDebugLog("数据库的路径", activity.getCacheDir().getPath());
		new Thread(new Runnable() {
			@Override
			public void run() {
			/*
			 * 方法1：
			 */
			//String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Download"+File.separator+"info.db";  
	       
			String path = activity.getCacheDir().getPath()+File.separator+"qmyo_sensitive_new.db";
		    db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);  
		    sensitiveList = getToDbData();
			/*
			 * 2.sensitiveList = Ioc.getIoc().getDb(activity.getCacheDir().getPath(), "qmyo_sensitive.db").findAll(provinceSelector);
			*/
			}
		}).start();
	}

	/**
	 * 读取数据库里面的内容，将内容写入到之前就生成好的容器中去
	 * @return
	 */
	  public List<Sensitive> getToDbData(){  
         
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
	
	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEventMainThread(DeleteEntity event) {
		int position = event.getPosition();
		files.remove(position);
		adapter.notifyDataSetChanged();
		if (files.size() == 0) {
			dialog.dismiss();
		}
		headAdapter.notifyDataSetChanged();
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.ll_new_comment_upload:// 打开相册
			upDataHead();
			break;
		case R.id.tv_activity_shared:// 发表评论
			if (TextUtils.isEmpty(v.et_new_comment_content.getText().toString())) {
				CustomToast.show(activity, R.string.tip,R.string.please_enter_activity_comment);
				return;
			}
			/*
			 * 提交Rating值，需要注意将获取到的rating作为参数提交上去
			 */
			float rating = v.rb_room_ratingbar.getRating();
			LogUtils.toDebugLog("rating", "获取到的Rating值为： "+ rating);
			
			
			//替代关键字的操作
			replaceSensitive(v.et_new_comment_content.getText().toString().trim());
			
			//提交发表内容的ProcessDialog
			pd = new ProgressDialog(activity);
			pd.setMessage("提交中");
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);
			pd.show();
			
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("activity_id", activityId);
			params.put("content", v.et_new_comment_content.getText().toString().trim());
			params.put("shop_id", shopId);
			
			HashMap<String, File> images = new HashMap<>();
			for (int i = 0; i < files.size(); i++) {
				
				//照片提交上去是将照片文件的绝对地址前面的file：//给取代掉
				images.put("images[" + i + "]",new File(files.get(i).replace("file://", "")));
			}
			//提交方式是正确的
			FastHttpHander.ajaxForm(GlobalValue.URL_USER_ACTIVITY_COMMENT,params, images, config, this);
			Log.i("Tag","评论已提交!");
			
			
			break;
		}
	}

	/**
	 * gridView 图片点击事件
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void itemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (position == files.size()) {
			if (files.size() >= 8) {
				CustomToast.show(activity, R.string.tip, R.string.max_add_eight);
				return;
			}
			upDataHead();
		} else {
			headAdapter = new DetailHeaderPagerAdapter(activity, files);
			dialog = ImageGalleryDialog2.newInstance(headAdapter, position);
			dialog.show(getFragmentManager(), "gallery");
		}
	}

	/*
	 * 尤其需要注意的static的修饰的玩意
	 */
	public static void setListViewHeightBasedOnChildren(GridView listView) {
		// 获取listview的adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		// 固定列宽，有多少列
		int col = 4;// listView.getNumColumns();
		int totalHeight = 0;
		// i每次加4，相当于listAdapter.getCount()小于等于4时 循环一次，计算一次item的高度，
		// listAdapter.getCount()小于等于8时计算两次高度相加
		for (int i = 0; i < listAdapter.getCount(); i += col) {
			// 获取listview的每一个item
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			// 获取item的高度和
			totalHeight += listItem.getMeasuredHeight();
		}

		// 获取listview的布局参数
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		// 设置高度
		params.height = totalHeight;
		// 设置margin
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		// 设置参数
		listView.setLayoutParams(params);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				pd.dismiss();
				//弹出评论成功的Dialog
				CustomToast.show(activity, R.string.tip,R.string.comment_success);
				back();
				break;
			}
		}
	}

	private String filename;
	private Uri imageUri;
	private ProgressDialog pd;
	private Selector provinceSelector;
	private ImageGalleryDeleteDialog imageGalleryDeleteDialog;

	public void upDataHead() {
		View view = inflater.inflate(R.layout.photo_choose_dialog, null);
		carema = (Button) view.findViewById(R.id.camera);
		album = (Button) view.findViewById(R.id.album);
		give_up = (Button) view.findViewById(R.id.give_up);
		final Dialog dialog = new Dialog(activity,
				R.style.transparentFrameWindowStyle);
		dialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.mypopwindow_anim_style);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = activity.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);

		carema.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (checkCameraHardWare(activity)) {

					String status = Environment.getExternalStorageState();
					if (status.equals(Environment.MEDIA_MOUNTED)) {
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyyMMddHHmmss");
						Date date = new Date(System.currentTimeMillis());
						filename = format.format(date);
						// 创建File对象用于存储拍照的图片 SD卡根目录
						// File outputImage = new
						// File(Environment.getExternalStorageDirectory(),test.jpg);
						// 存储至DCIM文件夹
						File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						File outputImage = new File(path, filename + ".jpg");
						try {
							if (outputImage.exists()) {
								outputImage.delete();
							}
							outputImage.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (9 - files.size() < 0) {
							CustomToast.show(activity, R.string.tip,
									R.string.max_photos);
							return;
						}
						// 将File对象转换为Uri并启动照相程序
						imageUri = Uri.fromFile(outputImage);
						Intent intent = new Intent(
								android.provider.MediaStore.ACTION_IMAGE_CAPTURE); // 照相
						intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 指定图片输出地址
						intent.putExtra("return-data", true);
						startActivityForResult(intent, ACTION_IMAGE_CAPTURE); // 启动照相
					}
				}
			}
		});
		album.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Intent intent = new Intent(getActivity(), GalleryActivity.class);
				// 指定图片选择数
				int max = 9;
				
//				if(adapter==null||adapter.getCount()==0){
//					max = 9;
//				}else{
//					max = 9 - adapter.getCount();
//				}
				
				//此处判断写的好玩些
				if (max - files.size() < 0) {
					CustomToast.show(activity, R.string.tip,R.string.max_photos);
					return;
				} else {
					max = max - files.size();
				}

				intent.putExtra(Constant.EXTRA_PHOTO_LIMIT, max);
				startActivityForResult(intent, ACTION_IMAGE_PICK);
			}
		});

		give_up.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * 图片返回的数据
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_IMAGE_CAPTURE:
			if (new File(getPath(activity, imageUri)).length() == 0) {
				return;
			}
			v.ll_new_comment_upload.setVisibility(View.GONE);
			v.gv_new_comment_images.setVisibility(View.VISIBLE);
			files.add(imageUri.toString());
			// setListViewHeightBasedOnChildren(v.gv_new_comment_images);
			Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intentBc.setData(imageUri);
			getActivity().sendBroadcast(intentBc);
			adapter.notifyDataSetChanged();
			break;
		case ACTION_IMAGE_PICK:
			if (data == null) {
				return;
			}
			v.ll_new_comment_upload.setVisibility(View.GONE);
			
			v.gv_new_comment_images.setVisibility(View.VISIBLE);
			
			
			// String path = getPath(activity, uri);
			ArrayList<String> images = data
					.getStringArrayListExtra(Constant.EXTRA_PHOTO_PATHS);
			for (String path : images) {
				files.add("file://" + path);
			}
			// setListViewHeightBasedOnChildren(v.gv_new_comment_images);
			adapter.notifyDataSetChanged();
			
			
			
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean checkCameraHardWare(Context context) {
		PackageManager packageManager = context.getPackageManager();
		if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		}
		return false;
	}

	@SuppressLint("NewApi")
	public String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	private boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	private boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	private boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	private boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	private String getDataColumn(Context context, Uri uri, String selection,
			String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * 替换关键字
	 * 
	 * @param sensitive
	 */
	private void replaceSensitive(String sensitie) {
		String sensitiveStr = "";
		
		for (Sensitive sensitive : sensitiveList) {
			if (v.et_new_comment_content.getText().toString().trim().contains(sensitive.getName().trim())) {
				sensitiveStr = sensitive.getName();
			}
		}
		if (sensitiveStr.length() >= 1) {
			if (sensitiveStr.toString().length() == 1) {
				v.et_new_comment_content.setText(v.et_new_comment_content
						.getText().toString().trim()
						.replace(sensitiveStr.toString(), "*"));
			} else if (sensitiveStr.toString().length() == 2) {
				v.et_new_comment_content.setText(v.et_new_comment_content
						.getText().toString().trim()
						.replace(sensitiveStr.toString(), "**"));
			} else if (sensitiveStr.toString().length() >= 3) {
				v.et_new_comment_content.setText(v.et_new_comment_content
						.getText().toString().trim()
						.replace(sensitiveStr.toString(), "***"));
			}
		/*	replaceSensitive(v.et_new_comment_content.getText().toString().trim());*/
		}
		
	}

	/*
	 * 最后面的那个照片按钮点击后，便执行下面的回调方法
	 * @see com.lansun.qmyo.adapter.UpLoadPhotoAdapter.openSelectPhotoListener#openUpdataHead()
	 */
	/*@Override
	public void openUpdataHead(){
		upDataHead();
	}*/
	
	
	
	public void openPhotoDetails(int position){
		
		DetailHeaderDeletePagerAdapter headPagerAdapter = new DetailHeaderDeletePagerAdapter(activity, files);
		imageGalleryDeleteDialog = ImageGalleryDeleteDialog.newInstance(headPagerAdapter, position,files);
		imageGalleryDeleteDialog.setOnNotifyGridViewListener(this);
		imageGalleryDeleteDialog.show(getFragmentManager(), "galleryCanDelete");
        
	}

	
	@Override
	public void notifyGridView(){
		//此adapter是GridView挂钩的Adapter数据源
		adapter.notifyDataSetChanged();
	}
	@Override
	public void notifyGridView(int i) {
		imageGalleryDeleteDialog.dismiss();
		adapter.notifyDataSetChanged();
	}
}
