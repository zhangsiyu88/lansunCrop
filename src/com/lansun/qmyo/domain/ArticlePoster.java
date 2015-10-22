package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.android.pc.ioc.db.annotation.Id;

/**
 * 极文海报
 * 
 * @author bhxx
 * 
 */
public class ArticlePoster implements Serializable {
	private String name;
	private String photo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

}