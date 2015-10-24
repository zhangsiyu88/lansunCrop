package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * 活动信息
 * 
 * @author bhxx
 * 
 */
public class Activity implements Serializable {

	private int id;
	private String name;
	private String tag;
	private ArrayList<String> category;
	private int attention;
	private int praise;
	private String photo;
	private ArrayList<ActivityContent> content;
	private int allow;
	private String time;
	private String start_time;
	private String end_time;
	private ArrayList<String> photos;
	private int activityid;
	private int other_shop;
	private boolean my_attention;
	private String collection_time;
	private ArrayList<CouponsData> coupons;
	private String share_url ;
	

	public String getShare_url() {
		return share_url;
	}

	public void setShare_url(String share_url) {
		this.share_url = share_url;
	}

	public ArrayList<CouponsData> getCoupons() {
		return coupons;
	}

	public void setCoupons(ArrayList<CouponsData> coupons) {
		this.coupons = coupons;
	}

	private int institution;

	public String getCollection_time() {
		return collection_time;
	}

	public void setCollection_time(String collection_time) {
		this.collection_time = collection_time;
	}

	public int getInstitution() {
		return institution;
	}

	public void setInstitution(int institution) {
		this.institution = institution;
	}

	public boolean isMy_attention() {
		return my_attention;
	}

	public void setMy_attention(boolean my_attention) {
		this.my_attention = my_attention;
	}

	public int getOther_shop() {
		return other_shop;
	}

	public void setOther_shop(int other_shop) {
		this.other_shop = other_shop;
	}

	public int getActivityid() {
		return activityid;
	}

	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}

	private boolean other_shops;

	public boolean isOther_shops() {
		return other_shops;
	}

	public void setOther_shops(boolean other_shops) {
		this.other_shops = other_shops;
	}

	public ArrayList<ActivityContent> getContent() {
		return content;
	}

	public void setContent(ArrayList<ActivityContent> content) {
		this.content = content;
	}

	public int getAllow() {
		return allow;
	}

	public void setAllow(int allow) {
		this.allow = allow;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public ArrayList<String> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<String> photos) {
		this.photos = photos;
	}

	public int getAttention() {
		return attention;
	}

	public void setAttention(int attention) {
		this.attention = attention;
	}

	public int getPraise() {
		return praise;
	}

	public void setPraise(int praise) {
		this.praise = praise;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public ArrayList<String> getCategory() {
		return category;
	}

	public void setCategory(ArrayList<String> category) {
		this.category = category;
	}
}