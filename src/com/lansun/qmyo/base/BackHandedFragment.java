package com.lansun.qmyo.base;

import android.os.Bundle;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.port.BackHanderInterface;

public abstract class BackHandedFragment extends BaseFragment {
	
	public BackHanderInterface mBackHanderInterface;
	
	public abstract boolean onBackPressed();
	
	@Override
	public void onStart() {
		/*
		 * 将当前选中的Fragment放入到MainActivity的Fragment栈中
		 */
		mBackHanderInterface.selectFragment(this);
		super.onStart();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		if(!(getActivity() instanceof BackHanderInterface)){  
            throw new ClassCastException("Hosting Activity must implements BackHandledInterface");  
        }else{  
            mBackHanderInterface = (BackHanderInterface)getActivity();  
        } 
		super.onCreate(savedInstanceState);
	}
}
