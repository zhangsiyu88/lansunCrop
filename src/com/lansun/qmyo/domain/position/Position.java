package com.lansun.qmyo.domain.position;

import java.util.List;

public class Position {
	private String name;

	private List<Contain> data ;

	public void setName(String name){
	this.name = name;
	}
	public String getName(){
	return this.name;
	}
	public void setData(List<Contain> data){
	this.data = data;
	}
	public List<Contain> getData(){
	return this.data;
	}
}
