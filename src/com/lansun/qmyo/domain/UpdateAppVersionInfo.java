package com.lansun.qmyo.domain;

import java.util.ArrayList;

public class UpdateAppVersionInfo {

	private String title;
	private ArrayList<UpdateAppVersionInfoContentBean>  data;
	private String url;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ArrayList<UpdateAppVersionInfoContentBean> getData() {
		return data;
	}
	public void setData(ArrayList<UpdateAppVersionInfoContentBean> data) {
		this.data = data;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
