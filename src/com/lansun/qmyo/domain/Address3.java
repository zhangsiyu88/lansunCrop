package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.android.pc.ioc.db.annotation.Foreign;
import com.android.pc.ioc.db.annotation.Id;

/**
 * 活动信息
 * 
 * @author bhxx
 * 
 */
public class Address3 implements Serializable {
	protected int id;
	protected String name;
	protected int code;

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

}