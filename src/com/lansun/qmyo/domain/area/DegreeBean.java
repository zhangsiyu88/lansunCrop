package com.lansun.qmyo.domain.area;

import java.io.Serializable;

public class DegreeBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String fid;
	
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
