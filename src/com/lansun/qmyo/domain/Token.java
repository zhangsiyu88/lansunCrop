package com.lansun.qmyo.domain;

import java.io.Serializable;

/**
 * Token 
 * @author bhxx
 *
 */
public class Token implements Serializable{

	private String token;
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
