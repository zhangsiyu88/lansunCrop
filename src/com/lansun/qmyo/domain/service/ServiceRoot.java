package com.lansun.qmyo.domain.service;

import java.util.List;

public class ServiceRoot {
	private String name;

	private List<ServiceDataKey> data ;

	public void setName(String name){
	this.name = name;
	}
	public String getName(){
	return this.name;
	}
	public void setData(List<ServiceDataKey> data){
	this.data = data;
	}
	public List<ServiceDataKey> getData(){
	return this.data;
	}
}
