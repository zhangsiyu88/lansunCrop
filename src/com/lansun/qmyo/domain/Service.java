package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.ClipData.Item;

/**
 * 板块服务
 * @author bhxx
 *
 */
public class Service implements Serializable{

	private String name;
	private ArrayList<ServiceData> data;
	

	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public ArrayList<ServiceData> getData() {
		return data;
	}




	public void setData(ArrayList<ServiceData> data) {
		this.data = data;
	}
	
}
