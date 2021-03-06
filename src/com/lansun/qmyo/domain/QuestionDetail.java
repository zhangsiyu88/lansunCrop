package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 活动列表
 * 
 * @author bhxx
 * 
 */
public class QuestionDetail implements Serializable {

	private int id;
	private String content;
	private String answer;
	private String time;
	private String type;
	private ArrayList<QuestionAnswerDetail> items;

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

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
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

	public ArrayList<QuestionAnswerDetail> getItems() {
		return items;
	}

	public void setItems(ArrayList<QuestionAnswerDetail> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "QuestionDetail [id=" + id + ", content=" + content
				+ ", answer=" + answer + ", time=" + time + ", type=" + type
				+ ", items=" + items + "]";
	}
}
