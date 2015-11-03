package com.lansun.qmyo.domain;

public class QuestionAnswerDetail {
	private int id;

	private String content;

	private String answer;

	private String time;

	private String type;

	public void setId(int id){
	this.id = id;
	}
	public int getId(){
	return this.id;
	}
	public void setContent(String content){
	this.content = content;
	}
	public String getContent(){
	return this.content;
	}
	public void setAnswer(String answer){
	this.answer = answer;
	}
	public String getAnswer(){
	return this.answer;
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
}
