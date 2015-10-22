package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 活动列表
 * 
 * @author bhxx
 * 
 */
public class AreaList implements Serializable {
	private String type;
	private ArrayList<City> data;
	private int lv;
	private String name;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<City> getData() {
		return data;
	}

	public void setData(ArrayList<City> data) {
		this.data = data;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
