package com.lansun.qmyo.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.lansun.qmyo.R;

/**
 * TODO 分享对话框
 * @author bhxx
 *
 */
public class TelDialog extends DialogFragment{
	
	public int mWhich = 0 ;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder timeDialog =  new AlertDialog.Builder(getActivity()); 
		if(getArguments().getString("phone").contains(";")){//表示电话不止一条
			
			String multiPhoneNum = getArguments().getString("phone");
			
			int indexOfSign = multiPhoneNum.indexOf(";");
			final String firstNum = multiPhoneNum.substring(0, indexOfSign);
			final String secondNum = multiPhoneNum.substring(indexOfSign+1, multiPhoneNum.length());
			
			
			timeDialog.setIcon(R.drawable.icon);
			timeDialog.setTitle("请选择联系号码:");
			
			final String[] items = new String[]{firstNum,secondNum};
		    //final boolean[] mulFlags = new boolean[]{true,false};
			
			timeDialog.setSingleChoiceItems(items, 0, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					/*Toast.makeText(getActivity(), "选中的是"+ which, 0).show();*/
					mWhich = which;
				}
			});
			timeDialog.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+ items[mWhich]));  
			        startActivity(intent); 
				}
			});
			timeDialog.setNegativeButton("取消", null);
			
		}else{//只有一条电话号码可拨打时
			timeDialog.setIcon(R.drawable.icon);
			timeDialog.setTitle("确定拨打此号码:");
			timeDialog.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+getArguments().getString("phone")));  
			         startActivity(intent);  				
				}
			});
			timeDialog.setNegativeButton("取消", null);
		}
		
		return timeDialog.create();  
	}

}
