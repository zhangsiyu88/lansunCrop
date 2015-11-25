package com.lansun.qmyo.fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;

/**
 * 私人秘书2 引导界面
 * 
 * @author bhxx
 * 
 */
public class SecondSecretaryFragment extends BaseFragment {
	@InjectAll
	Views v;
	private String currentHeadPath;
	private String secretaryName;
	private Button carema;
	private Button album;
	private Button give_up;

	protected static final int ACTION_IMAGE_CAPTURE = 2;
	protected static final int ACTION_IMAGE_PICK = 1;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View tv_secretary_first_confirm, fl_secretary_change_head;
		private CircularImage iv_secretary_head;
		private TextView tv_secretary_name;
		private EditText et_secretary_hope_call_you;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_secretary_second,null);
		
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		Bundle arguments = getArguments();
		if (arguments != null) {
			currentHeadPath = arguments.getString("head");
			secretaryName = arguments.getString("secretaryName");
			v.tv_secretary_name.setText(secretaryName);
			Bitmap decodeFile = BitmapFactory.decodeFile(currentHeadPath);
			v.iv_secretary_head.setImageBitmap(decodeFile);
		}
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_secretary_first_confirm:
			if (TextUtils.isEmpty(v.et_secretary_hope_call_you.getText().toString().trim())) {
				//提示：小秘书该如何称呼你？
				CustomToast.show(activity, R.string.tip,R.string.please_enter_hope_call_you);
				return;
			}
			
			//User为空，就弹出来叫 你丫给我登录 
			if (GlobalValue.user == null) {
				DialogUtil.createTipAlertDialog(activity,R.string.please_login, new TipAlertDialogCallBack() {

							@Override
							public void onPositiveButtonClick(
									DialogInterface dialog, int which) {
								FragmentEntity event = new FragmentEntity();
								RegisterFragment fragment = new RegisterFragment();
								event.setFragment(fragment);
								EventBus.getDefault().post(event);
								dialog.dismiss();
							}

							@Override
							public void onNegativeButtonClick(
									DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				return;
			}

			// 注册当前小秘书信息
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			// TODO 需要添加秘书头像
			params.put("name", secretaryName);
			params.put("owner", v.et_secretary_hope_call_you.getText()
					.toString().trim());
			// HashMap<String, File> files = new HashMap<>();
			// files.put(key, value)
			
		
			//将已填写的用户信息和数据提交到服务器上去
			FastHttpHander.ajaxForm(GlobalValue.URL_SECRETARY_SAVE, params,
					null, config, this);
			break;
			
		case R.id.fl_secretary_change_head:
			upDataHead();
			break;
		}
	}

	private Uri imageUri;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				//用户信息提交后，转而跳转到SecretaryFragment上去
				Secretary secretary = Handler_Json.JsonToBean(Secretary.class,
						r.getContentAsString());
				if (secretary != null) {
					CustomToast.show(activity, R.string.tip,
							R.string.save_secretary_success);
					FragmentEntity event = new FragmentEntity();
					/*SecretaryFragment fragment = new SecretaryFragment();*/
					MainFragment fragment = new MainFragment(1);
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
				break;
			}
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

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

}
