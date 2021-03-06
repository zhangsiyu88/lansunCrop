package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 活动列表
 * 
 * @author bhxx
 * 
 */
public class CommentList implements Serializable {
	private int total;
	private int per_page;
	private int current_page;
	private int last_page;
	private String next_page_url;
	private String prev_page_url;
	private int from;
	private int to;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPer_page() {
		return per_page;
	}

	public void setPer_page(int per_page) {
		this.per_page = per_page;
	}

	public int getCurrent_page() {
		return current_page;
	}

	public void setCurrent_page(int current_page) {
		this.current_page = current_page;
	}

	public int getLast_page() {
		return last_page;
	}

	public void setLast_page(int last_page) {
		this.last_page = last_page;
	}

	public String getNext_page_url() {
		return next_page_url;
	}

	public void setNext_page_url(String next_page_url) {
		this.next_page_url = next_page_url;
	}

	public String getPrev_page_url() {
		return prev_page_url;
	}

	public void setPrev_page_url(String prev_page_url) {
		this.prev_page_url = prev_page_url;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	private ArrayList<CommentListData> data;

	public ArrayList<CommentListData> getData() {
		return data;
	}

	public void setData(ArrayList<CommentListData> data) {
		this.data = data;
	}

}
