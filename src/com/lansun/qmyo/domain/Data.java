package com.lansun.qmyo.domain;

import java.io.Serializable;

public class Data implements Serializable{

	private String key;
	private String name;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
