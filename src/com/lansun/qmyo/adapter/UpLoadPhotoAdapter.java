package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.BackEntity;
import com.lansun.qmyo.fragment.BankCardDetailFragment;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.lansun.qmyo.R;

public class UpLoadPhotoAdapter extends CommonAdapter {

//	private openSelectPhotoListener mOpenSelectPhotoListener;

	public UpLoadPhotoAdapter(Activity activity, ArrayList<String> data) {
		this.activity = activity;
		this.data = data;
		inflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {//会执行多少次getView（）操作
		if(data.size()>=9){//当选中的照片为9时，不需要最后哪张图片
			return 9;
		}
		return data.size() + 1;
	}

	@Override
	public View view(int position, View convertView, ViewGroup parent) {
		//position是从0开始计数，故当getCount的值增大时，那么position才有机会和data.size()相等
		if (position == data.size()) {
			//在所有选中的图片后面加上一个照相机的图片
			convertView = inflater.inflate(R.layout.upload_photo, null);
			return convertView;
		}
		
		convertView = inflater.inflate(R.layout.upload_photo_item, null);
		RecyclingImageView iv_upload_photo = (RecyclingImageView) convertView
				.findViewById(R.id.iv_upload_photo);
		//iv_upload_photo.setScaleType(ScaleType.FIT_XY);
		iv_upload_photo.setScaleType(ScaleType.CENTER_CROP);//改成居中展示
		String path = data.get(position);
		
		ImageLoader.getInstance().displayImage(path, iv_upload_photo);
		return convertView;
	}

	
//	public interface openSelectPhotoListener{
//		public void openUpdataHead();
//	}
//	
//	public void setOpenSelectPhotoListener(openSelectPhotoListener lis){
//		this.mOpenSelectPhotoListener = lis;
//	}
}
