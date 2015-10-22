package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.android.pc.ioc.db.annotation.Foreign;
import com.android.pc.ioc.db.annotation.Id;

/**
 * 活动信息
 * 
 * @author bhxx
 * 
 */
public class Message implements Serializable {
	private String maijie;
	private String activity;
	private String comment;
	private String secretary;

	public String getMaijie() {
		return maijie;
	}

	public void setMaijie(String maijie) {
		this.maijie = maijie;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSecretary() {
		return secretary;
	}

	public void setSecretary(String secretary) {
		this.secretary = secretary;
	}

}