package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 首页极文Data
 * 
 * @author bhxx
 * 
 */
public class HomePromoteData implements Serializable {
	private String id;
	private String name;
	private String tag;
	private String url;
	private String photo;
	private ArrayList<String> tags;
	private String time;

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}
