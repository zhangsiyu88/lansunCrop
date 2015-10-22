package com.lansun.qmyo.domain;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BankCard {

	private int id;
	private String name;
	private String summary;
	private String photo;
	private ArrayList<ActivityContent> content;

	public ArrayList<ActivityContent> getContent() {
		return content;
	}

	public void setContent(ArrayList<ActivityContent> content) {
		this.content = content;
	}

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

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
