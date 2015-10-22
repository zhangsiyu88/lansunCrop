package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import android.R.integer;

import com.android.pc.ioc.db.annotation.Foreign;
import com.android.pc.ioc.db.annotation.Id;

/**
 * 活动信息
 * 
 * @author bhxx
 * 
 */
public class Address2 implements Serializable {
	protected int id;
	protected int code;
	protected String name;
	protected int areaCount;
	protected ArrayList<Address3> items;

	public int getAreaCount() {
		return areaCount;
	}

	public void setAreaCount(int areaCount) {
		this.areaCount = areaCount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public ArrayList<Address3> getItems() {
		return items;
	}

	public void setItems(ArrayList<Address3> items) {
		this.items = items;
	}

}