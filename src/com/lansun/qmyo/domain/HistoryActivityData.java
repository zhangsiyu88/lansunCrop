package com.lansun.qmyo.domain;

import java.io.Serializable;

/**
 * 浏览活动记录
 * @author bhxx
 *
 */
public class HistoryActivityData implements Serializable {

	private int id;
	private String time;
	private int activityid;
	private String activityname;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getActivityid() {
		return activityid;
	}
	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}
	public String getActivityname() {
		return activityname;
	}
	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}
	
	
	
}
