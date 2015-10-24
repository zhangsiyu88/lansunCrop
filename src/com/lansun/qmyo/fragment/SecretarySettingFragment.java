package com.lansun.qmyo.fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * 私人秘书保存界面
 * 
 * @author bhxx
 * 
 */
public class SecretarySettingFragment extends BaseFragment implements TextWatcher,OnFocusChangeListener{

	@InjectAll
	Views v;
	private String currentHeadPath;
	private String secretaryName;
	private String secretary_hope_call_you;
	private Button carema;
	private Button album;
	private Button give_up;
	private RecyclingImageView iv_activity_del,iv_activity_del_hope;
	protected static final int ACTION_IMAGE_CAPTURE = 2;
	protected static final int ACTION_IMAGE_PICK = 1;
	private Handler handlerOkHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				CustomToast.show(activity, R.string.tip,
						R.string.save_secretary_success);
				App.app.setData("secretary_name", secretaryName);
				EventBus bus = EventBus.getDefault();
				FragmentEntity entity = new FragmentEntity();
				Fragment fragment=new SecretaryFragment();
				entity.setFragment(fragment);
				bus.post(entity);
				break;
			case 1:
				CustomToast.show(activity, R.string.tip,"提交失败");
				break;
			}
		};
	};
	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View btn_secretary_save;
		private EditText et_secretary_name, et_hope_call_you;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private CircularImage iv_secretary_head;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.fragment_secretary_edit,
				container,false);
		Handler_Inject.injectFragment(this, rootView);
		initView(rootView);
		CustomToast.show(activity, "请先设置您的私人秘书哦","体验最强虚拟在线真人小秘书");
		return rootView;
	}

	private void initView(View rootView) {
		iv_activity_del=(RecyclingImageView)rootView.findViewById(R.id.iv_activity_del);
		iv_activity_del_hope=(RecyclingImageView)rootView.findViewById(R.id.iv_activity_del_hope);

		v.et_secretary_name.addTextChangedListener(this);
		v.et_hope_call_you.addTextChangedListener(this);

		v.et_hope_call_you.setOnFocusChangeListener(this);
		v.et_secretary_name.setOnFocusChangeListener(this);
	}

	@InjectInit
	private void init() {
		v.et_secretary_name.setText(GlobalValue.secretary.getName());
		v.et_hope_call_you.setText(GlobalValue.secretary.getOwner_name());
		if (!TextUtils.isEmpty(GlobalValue.secretary.getAvatar())) {
			loadPhoto(GlobalValue.secretary.getAvatar(), v.iv_secretary_head);
		}
		Bundle arguments = getArguments();
		if (arguments != null) {
			currentHeadPath = arguments.getString("head");
			secretary_hope_call_you = arguments
					.getString("et_secretary_hope_call_you");
			v.et_hope_call_you.setText(secretary_hope_call_you);
			secretaryName = arguments.getString("secretaryName");
			v.et_secretary_name.setText(secretaryName);
			Bitmap decodeFile = BitmapFactory.decodeFile(currentHeadPath);
			v.iv_secretary_head.setImageBitmap(decodeFile);

		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.iv_secretary_head:
			upDataHead();
			break;
		case R.id.btn_secretary_save:
			String secretary_name = v.et_secretary_name.getText().toString();
			String hope_call_you = v.et_hope_call_you.getText().toString();
			if (TextUtils.isEmpty(secretary_name)) {
				CustomToast.show(activity, "提示","总裁大大，给起个名字吧");
				return;
			}
			if (TextUtils.isEmpty(hope_call_you)) {
				CustomToast.show(activity, "提示","总裁大大，您希望小迈怎么称呼您啊");
				return;
			}
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("name", secretary_name);
			params.put("owner", hope_call_you);
			HashMap<String, File> files = new HashMap<>();
			if (!TextUtils.isEmpty(currentHeadPath)) {
				files.put("avatar", new File(currentHeadPath));
			}
			FastHttpHander.ajaxForm(GlobalValue.URL_SECRETARY_SAVE, params,
					files, config, this);
			break;
			//			//			InternetConfig config = new InternetConfig();
			//			//			config.setKey(0);
			//			//			HashMap<String, Object> head = new HashMap<>();
			//			//			head.put("Authorization",
			//			//					"Bearer " + App.app.getData("access_token"));
			//			//			config.setHead(head);
			//
			//						Map<String, String> params = new HashMap<>();
			//						params.put("name", secretary_name);
			//						params.put("owner", hope_call_you);
			//			//			params.put("avatar", currentHeadPath);
			//			//			HashMap<String, File> files = new HashMap<>();
			//			//			if (!TextUtils.isEmpty(currentHeadPath)) {
			//			//				files.put("avatar", new File(currentHeadPath));
			//			//			}
			//						File file=new File(currentHeadPath);
			//						OkHttp.asyncPost(GlobalValue.URL_SECRETARY_SAVE,params, file, new Callback() {
			//							@Override
			//							public void onResponse(Response response) throws IOException {
			//								if (response.isSuccessful()) {
			//									String result=response.body().string();
			//									Log.e("secretary",result);
			//			//						Secretary secretary = Handler_Json.JsonToBean(Secretary.class,
			//			//								response.body().string());
			//			//						if (secretary != null) {
			//			//							secretaryName=secretary.getName();
			//			//							handlerOkHandler.sendEmptyMessage(0);
			//			//							Log.e("success", currentHeadPath);
			//			//						}
			//								}else {
			//									handlerOkHandler.sendEmptyMessage(1);
			//								}
			//							}
			//							@Override
			//							public void onFailure(Request arg0, IOException arg1) {
			//			
			//							}
			//						});
			//			OkHttp.asyncPost(GlobalValue.URL_SECRETARY_SAVE, params, new Callback() {
			//			@Override
			//			public void onResponse(Response response) throws IOException {
			//				Log.e("currentHeadPath", currentHeadPath);
			//				Log.e("secretary",response.body().string());
			//			}
			//			
			//			@Override
			//			public void onFailure(Request arg0, IOException arg1) {
			//				
			//			}
			//		});
			//						FastHttpHander.ajaxForm(GlobalValue.URL_SECRETARY_SAVE, params,
			//								files, config, this);
//			break;
		}
		//		if (fragment != null) {
		//			entity.setFragment(fragment);
		//			bus.post(entity);
		//		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				Secretary secretary = Handler_Json.JsonToBean(Secretary.class,
						r.getContentAsString());
				if (secretary != null) {
					CustomToast.show(activity, R.string.tip,
							R.string.save_secretary_success);
					back();
				}
				break;
			}
		}
	}

	private Uri imageUri;

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
						String filename = format.format(date);
						// 创建File对象用于存储拍照的图片 SD卡根目录
						// File outputImage = new
						// File(Environment.getExternalStorageDirectory(),test.jpg);
						// 存储至DCIM文件夹
						File path = Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						File outputImage = new File(path, filename + ".jpg");
						try {
							if (outputImage.exists()) {
								outputImage.delete();
							}
							outputImage.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
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
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
			currentHeadPath = getPath(activity, imageUri);
			Bitmap secretaryHead1 = BitmapFactory.decodeFile(currentHeadPath);
			v.iv_secretary_head.setImageBitmap(secretaryHead1);
			break;
		case ACTION_IMAGE_PICK:
			if (data == null) {
				return;
			}
			Uri uri = data.getData();
			if (uri == null) {
				return;
			}
			currentHeadPath = getPath(activity, uri);
			Bitmap secretaryHead = BitmapFactory.decodeFile(currentHeadPath);
			v.iv_secretary_head.setImageBitmap(secretaryHead);
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
	@Override
	public void afterTextChanged(Editable s) {

	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s.toString())) {
			iv_activity_del_hope.setVisibility(View.INVISIBLE);
			iv_activity_del.setVisibility(View.INVISIBLE);
		}else {
			iv_activity_del_hope.setVisibility(View.VISIBLE);
			iv_activity_del.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (hasFocus) {
			switch (view.getId()) {
			case R.id.et_secretary_hope_call_you:
				v.et_hope_call_you.setHint("请设置2-12位字符");
				v.et_hope_call_you.setHintTextColor(getResources().getColor(R.color.translate_gray));
				break;
			case R.id.et_secretary_name:
				v.et_secretary_name.setHint("请设置2-12位字符");
				v.et_secretary_name.setHintTextColor(getResources().getColor(R.color.translate_gray));
				break;
			}
		}else {
			switch (view.getId()) {
			case R.id.et_secretary_hope_call_you:
				v.et_hope_call_you.setHint("给我设置个昵称吧");
				v.et_hope_call_you.setHintTextColor(getResources().getColor(R.color.app_white));
				break;
			case R.id.et_secretary_name:
				v.et_secretary_name.setHint("请问如何称呼您");
				v.et_secretary_name.setHintTextColor(getResources().getColor(R.color.app_white));
				break;
			}
		}
	}
}
