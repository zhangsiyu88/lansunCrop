package com.lansun.qmyo.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.lansun.qmyo.R;

public class DialogUtil {

	/**
	 * 弹出没有网络连接提示对话框
	 * 
	 * @param context
	 */
	// public static void showNoNetWork(final Context context) {
	// AlertDialog.Builder builder = new Builder(context);
	// builder.setTitle(R.string.app_title).setIcon(R.drawable.ic_launcher)
	// .setMessage(R.string.net_faild)
	// .setPositiveButton(android.R.string.ok, new OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// if (android.os.Build.VERSION.SDK_INT > 10) {
	// context.startActivity(new Intent(
	// android.provider.Settings.ACTION_SETTINGS));
	// } else {
	// context.startActivity(new Intent(
	// android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	// }
	// }
	//
	// }).setNegativeButton(android.R.string.cancel, null).show();
	// }

	public static void createTipAlertDialog(Context ctx, String content,
			final TipAlertDialogCallBack callBack) {
		CustomDialog.Builder dialog = new CustomDialog.Builder(ctx);
		dialog.setTitle(R.string.tip);
		dialog.setMessage(content);
		dialog.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callBack.onNegativeButtonClick(dialog, which);
					}
				});
		dialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						callBack.onPositiveButtonClick(dialog, which);
					}
				});
		dialog.create().show();
	}

	public static void createTipAlertDialog(Context ctx, int contentId,
			final TipAlertDialogCallBack callBack) {
		CustomDialog.Builder dialog = new CustomDialog.Builder(ctx);
		dialog.setTitle(R.string.tip);
		dialog.setMessage(contentId);
		dialog.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callBack.onNegativeButtonClick(dialog, which);
					}
				});
		dialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						callBack.onPositiveButtonClick(dialog, which);
					}
				});
		dialog.create().show();
	}

	public interface TipAlertDialogCallBack {
		void onPositiveButtonClick(DialogInterface dialog, int which);

		void onNegativeButtonClick(DialogInterface dialog, int which);
	}

//	public static void createUserLoginDialog(final Context ctx, int contentId) {
//		createTipAlertDialog(ctx, contentId, new TipAlertDialogCallBack() {
//
//			@Override
//			public void onPositiveButtonClick(DialogInterface dialog, int which) {
//				Intent intent = new Intent(ctx, LoginActivity.class);
//				ctx.startActivity(intent);
//				dialog.dismiss();
//			}
//
//			@Override
//			public void onNegativeButtonClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
//	}
}
