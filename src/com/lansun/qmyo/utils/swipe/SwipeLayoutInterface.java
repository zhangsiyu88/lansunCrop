package com.lansun.qmyo.utils.swipe;

import com.lansun.qmyo.utils.swipe.SwipeLayout.Status;



public interface SwipeLayoutInterface {

	Status getCurrentStatus();
	
	void close();
	
	void open();
}
