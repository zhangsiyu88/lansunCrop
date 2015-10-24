package com.lansun.qmyo.domain.position;

import java.util.List;

public class Contain {
	private String name;

	private List<City> items ;

	public void setName(String name){
	this.name = name;
	}
	public String getName(){
	return this.name;
	}
	public void setItems(List<City> items){
	this.items = items;
	}
	public List<City> getItems(){
	return this.items;
	}
}
