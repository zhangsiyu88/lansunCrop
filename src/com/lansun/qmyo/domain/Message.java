package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.android.pc.ioc.db.annotation.Foreign;
import com.android.pc.ioc.db.annotation.Id;

/**
 * 活动信息
 * 
 * @author Yeun.zhang
 * 
 */
public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maijie;
	private int activity;
	private int comment;
	private int secretary;

	public String getMaijie() {
		return String.valueOf(maijie);
	}

	public void setMaijie(String maijie) {
		this.maijie = Integer.valueOf(maijie);
	}

	public String getActivity() {
		return String.valueOf(activity);
	}

	public void setActivity(String activity) {
		this.activity = Integer.valueOf(activity);
	}

	public String getComment() {
		return String.valueOf(comment);
	}

	public void setComment(String comment) {
		this.comment = Integer.valueOf(comment);
	}

	public String getSecretary() {
		return String.valueOf(secretary);
	}

	public void setSecretary(String secretary) {
		this.secretary = Integer.valueOf(secretary);
	}

}