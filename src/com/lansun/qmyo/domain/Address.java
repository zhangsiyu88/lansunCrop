package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.android.pc.ioc.db.annotation.Id;

/**
 * 省
 * 
 * @author bhxx
 * 
 */
public class Address implements Serializable {
	@Id
	protected int id;
	protected String name;
	protected int code;
	protected int cityCount;
	protected ArrayList<Address2> items;

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

	public ArrayList<Address2> getItems() {
		return items;
	}

	public void setItems(ArrayList<Address2> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public int getCityCount() {
		return cityCount;
	}

	public void setCityCount(int cityCount) {
		this.cityCount = cityCount;
	}

}