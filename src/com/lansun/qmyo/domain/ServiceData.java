package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;


public class ServiceData implements Serializable{
	private int key;
	private String name;
	private ArrayList<ServiceDataItem> items;
	public int getKey() {
		return key;
	}
	public void setKey(int key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<ServiceDataItem> getItems() {
		return items;
	}
	public void setItems(ArrayList<ServiceDataItem> items) {
		this.items = items;
	}


}
