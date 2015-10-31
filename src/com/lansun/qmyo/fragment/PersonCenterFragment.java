package com.lansun.qmyo.fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.lansun.qmyo.domain.HistoryActivity;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.lansun.qmyo.R;

public class PersonCenterFragment extends BaseFragment {
	private RecyclingImageView iv_activity_back;
	@InjectAll
	Views v;
	private Button carema;
	private Button album;
	private Button give_up;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View iv_person_center_nickname, rl_person_center_head,
				iv_person_center_reset_pwd, iv_person_center_edit_person_info,
				iv_person_center_qr_code, tv_person_center_exit,
				fl_comments_right_iv;
		private TextView tv_activity_title, tv_person_center_nickname;
		private CircularImage iv_person_center_head;
	}

	protected static final int ACTION_IMAGE_CAPTURE = 2;
	protected static final int ACTION_IMAGE_PICK = 1;
	private Handler handleOKhttp=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (dialogpg!=null) {
					dialogpg.dismiss();
				}
				CustomToast.show(activity,"提示", "头像修改成功");
				break;
			case 1:
				if (dialogpg!=null) {
					dialogpg.dismiss();
				}
				CustomToast.show(activity,"提示", "头像修改失败");
				break;
			}
		};
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_person_cent, null);
		iv_activity_back=(RecyclingImageView)rootView.findViewById(R.id.iv_activity_back);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}
	
	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		
		if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
			v.tv_person_center_nickname.setText(GlobalValue.user.getNickname());
		}
		if (!TextUtils.isEmpty(GlobalValue.user.getAvatar())) {
			loadPhoto(GlobalValue.user.getAvatar(), v.iv_person_center_head);
		}
		initTitle(v.tv_activity_title, R.string.person_info, null, 0);
		iv_activity_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentEntity entity=new FragmentEntity();
				Fragment fragment=new MineFragment();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
			}
		});
	}

	private void click(View view) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.rl_person_center_head:
			upDataHead();
			break;
		case R.id.iv_person_center_nickname:
			fragment = new EditUserInfoFragment(); //更改用户昵称
			Bundle args = new Bundle();
			args.putString("name", getString(R.string.edit_nick_name));
			args.putString("paramName", "nickname");
			fragment.setArguments(args);
			break;

		case R.id.iv_person_center_reset_pwd:   //充值密码
			fragment = new RegisterFragment();
			Bundle args1 = new Bundle();
			args1.putBoolean("isReset", true);//关键的一个标示
			fragment.setArguments(args1);
			break;

		case R.id.iv_person_center_edit_person_info: //当点击"完善个人信息"一栏时跳转至用户详细信息编辑的栏目里
			fragment = new EditUserFragment();
			break;
		case R.id.tv_person_center_exit://点击退出登录时，要求跳至  登录页_Dick语
			GlobalValue.user = null;
			GlobalValue.isFirst = true;//即为三无状态，那么就需要成为是第一次进入的用户状态，也就会是需要自己加卡那个页面
			clearTokenAndSercet();
			/**
			 * 2015-10-24修改退出时清除信息
			 */
			GlobalValue.mySecretary=null;
			fragment = new RegisterFragment();
			
			/*back();此处无需back，因为主动跳往登录界面*/
			break;
		}
		FragmentEntity entity = new FragmentEntity();
		entity.setFragment(fragment);
		EventBus.getDefault().post(entity);
	}

	private Uri imageUri;
	private ProgressDialog dialogpg;
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
		String path=null;
		switch (requestCode) {
		case ACTION_IMAGE_CAPTURE:
			if (new File(getPath(activity, imageUri)).length() == 0) {
				return;
			}
			path = getPath(activity, imageUri);
			ImageLoader.getInstance().displayImage(imageUri.toString(),
					v.iv_person_center_head);
			break;
		case ACTION_IMAGE_PICK:
			if (data == null) {
				return;
			}
			Uri uri = data.getData();
			if (uri == null) {
				return;
			}
			path = getPath(activity, uri);
			ImageLoader.getInstance().displayImage(uri.toString(),
					v.iv_person_center_head);
			break;
		}
		File file=new File(path);
		dialogpg = new ProgressDialog(activity);
		dialogpg.setMessage("图片上传中...");
		dialogpg.show();
		Map<String, String> paramas=new HashMap<>();
		OkHttp.asyncPost(GlobalValue.URL_USER_SAVE, paramas, file, new Callback() {
			@Override
			public void onResponse(Response arg0) throws IOException {
				if (arg0.isSuccessful()) {
					GlobalValue.user = Handler_Json.JsonToBean(User.class,arg0.body().string());
					handleOKhttp.sendEmptyMessage(0);
				}
			}
			@Override
			public void onFailure(Request arg0, IOException arg1) {
				handleOKhttp.sendEmptyMessage(1);
			}
		});
		super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * 检测相机是否可用
	 * 
	 * @param context
	 * @return
	 */
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
