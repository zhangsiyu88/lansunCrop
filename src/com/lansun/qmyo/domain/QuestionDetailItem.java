package com.lansun.qmyo.domain;

public class QuestionDetailItem {
	private String id;

	private String content;

	private String time;

	private String type;

	private String status;

	private int is_read;

	public void setId(String id){
	this.id = id;
	}
	public String getId(){
	return this.id;
	}
	public void setContent(String content){
	this.content = content;
	}
	public String getContent(){
	return this.content;
	}
	public void setTime(String time){
	this.time = time;
	}
	public String getTime(){
	return this.time;
	}
	public void setType(String type){
	this.type = type;
	}
	public String getType(){
	return this.type;
	}
	public void setStatus(String status){
	this.status = status;
	}
	public String getStatus(){
	return this.status;
	}
	public void setIs_read(int is_read){
	this.is_read = is_read;
	}
	public int getIs_read(){
	return this.is_read;
	}
}
