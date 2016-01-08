package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class Shop implements Serializable {
	private int id;
	private String name;
	private String summary;
	private String distance;
	private int evaluate;
	private String address;
	private String telephone;
	private String lat;
	private String lng;
	private int attention;
	private int praise;

	private ArrayList<String> photos;
	

	private String collection_time;

	public String getCollection_time() {
		return collection_time;
	}

	public void setCollection_time(String collection_time) {
		this.collection_time = collection_time;
	}

	private String url;
	private String business_hours;
	private String playday;

	private boolean my_attention;

	public String getUrl() {
		return url;
	}

	public boolean isMy_attention() {
		return my_attention;
	}

	public void setMy_attention(boolean my_attention) {
		this.my_attention = my_attention;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBusiness_hours() {
		return business_hours;
	}

	public void setBusiness_hours(String business_hours) {
		this.business_hours = business_hours;
	}

	public String getPlayday() {
		return playday;
	}

	public void setPlayday(String playday) {
		this.playday = playday;
	}

	public String getSummary() {
		return summary;
	}

	public ArrayList<String> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<String> photos) {
		this.photos = photos;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public int getEvaluate() {
		return evaluate;
	}

	public void setEvaluate(int evaluate) {
		this.evaluate = evaluate;
	}

	public int getPraise() {
		return praise;
	}

	public void setPraise(int praise) {
		this.praise = praise;
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

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public int getAttention() {
		return attention;
	}

	public void setAttention(int attention) {
		this.attention = attention;
	}
}
