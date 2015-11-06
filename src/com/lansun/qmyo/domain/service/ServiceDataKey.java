package com.lansun.qmyo.domain.service;

import java.util.List;

public class ServiceDataKey {
	private String name;
	private String key;
	private List<ServiceData> data ;

	public void setName(String name){
	this.name = name;
	}
	public String getName(){
	return this.name;
	}
	public void setData(List<ServiceData> data){
	this.data = data;
	}
	public List<ServiceData> getData(){
	return this.data;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
