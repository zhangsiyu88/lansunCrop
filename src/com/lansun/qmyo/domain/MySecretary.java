package com.lansun.qmyo.domain;

import java.io.Serializable;

public class MySecretary implements Serializable{
	private String id;
	private String name;
	private String avatar;
	private String owner_name;
	private String has;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getOwner_name() {
		return owner_name;
	}
	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
	}
	public String getHas() {
		return has;
	}
	public void setHas(String has) {
		this.has = has;
	}
	
}
