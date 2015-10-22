package com.lansun.qmyo.domain;

import java.util.ArrayList;

/**
 * 用户地域
 * @author bhxx
 *
 */
public class Area {

	private ArrayList<String> code;
	private String name;
	public ArrayList<String> getCode() {
		return code;
	}
	public void setCode(ArrayList<String> code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
