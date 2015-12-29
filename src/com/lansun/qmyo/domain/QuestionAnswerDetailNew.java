package com.lansun.qmyo.domain;

import java.util.ArrayList;

public class QuestionAnswerDetailNew {
	private int id;

	private String content;

	private ArrayList<SubAnswer> answer;

	public ArrayList<SubAnswer> getAnswer() {
		return answer;
	}
	public void setAnswer(ArrayList<SubAnswer> answer) {
		this.answer = answer;
	}
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
