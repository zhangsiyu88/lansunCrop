package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 活动列表
 * 
 * @author bhxx
 * 
 */
public class QuestionDetailNew implements Serializable {

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
	private ArrayList<QuestionAnswerDetailNew> items;

	public ArrayList<QuestionAnswerDetailNew> getItems() {
		return items;
	}

	public void setItems(ArrayList<QuestionAnswerDetailNew> items) {
		this.items = items;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}



	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	

	@Override
	public String toString() {
		return "QuestionDetailNew [id=" + id + ", content=" + content
				+ ", answer=" + answer + ", time=" + time + ", type=" + type
				+ ", items=" + items + "]";
	}
}
