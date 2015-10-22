package com.lansun.qmyo.domain;

import java.util.ArrayList;

/**
 * 回复
 * 
 * @author bhxx
 * 
 */
public class Comment {
	private int id;
	private String content;
	private String time;
	private ArrayList<String> photos;
	private int respond;
	private int principal;
	private int shop_id;
	private String comment_id;

	public String getComment_id() {
		return comment_id;
	}

	public void setComment_id(String comment_id) {
		this.comment_id = comment_id;
	}

	public int getShop_id() {
		return shop_id;
	}

	public void setShop_id(int shop_id) {
		this.shop_id = shop_id;
	}

	public int getPrincipal() {
		return principal;
	}

	public void setPrincipal(int principal) {
		this.principal = principal;
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

	public ArrayList<String> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<String> photos) {
		this.photos = photos;
	}

	public int getRespond() {
		return respond;
	}

	public void setRespond(int respond) {
		this.respond = respond;
	}
}
