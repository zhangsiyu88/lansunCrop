package com.lansun.qmyo.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.lansun.qmyo.R;

/**
 * TODO 分享对话框
 * @author bhxx
 *
 */
public class TelDialog extends DialogFragment{
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder timeDialog =  new AlertDialog.Builder(getActivity()); 
		timeDialog.setMessage(getArguments().getString("phone")); 
		timeDialog.setPositiveButton(getActivity().getString(R.string.tel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+getArguments().getString("phone")));  
		         startActivity(intent);  				
			}
		});
		timeDialog.setNegativeButton(R.string.cancle, null);
		return timeDialog.create();  
	}

}
