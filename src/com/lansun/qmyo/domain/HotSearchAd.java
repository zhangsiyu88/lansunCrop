package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * 热门搜索推广
 * @author bhxx
 *
 */
public class HotSearchAd implements Serializable{

	private ArrayList<HotSearchAdData> data;

	public ArrayList<HotSearchAdData> getData() {
		return data;
	}

	public void setData(ArrayList<HotSearchAdData> data) {
		this.data = data;
	}
	
	
}