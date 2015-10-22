package com.lansun.qmyo.domain;

import java.io.Serializable;

public class ActivityListData implements Serializable {
	private Shop shop;
	private Activity activity;
	
	public Shop getShop() {
		return shop;
	}
	public void setShop(Shop shop) {
		this.shop = shop;
	}
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
