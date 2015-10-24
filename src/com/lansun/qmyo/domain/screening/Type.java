package com.lansun.qmyo.domain.screening;

import java.util.List;

public class Type {
	private String name;
	private List<DataScrolling> data;
	public void setName(String name){
	this.name = name;
	}
	public String getName(){
	return this.name;
	}
	public void setData(List<DataScrolling> data){
	this.data = data;
	}
	public List<DataScrolling> getData(){
	return this.data;
	}
}
