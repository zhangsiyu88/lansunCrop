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
public class CouponsData implements Serializable {

	private int id;
	private String denomination;
	private String offuse_time;
	private String remainder;

	public String getRemainder() {
		return remainder;
	}

	public void setRemainder(String remainder) {
		this.remainder = remainder;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDenomination() {
		return denomination;
	}

	public void setDenomination(String denomination) {
		this.denomination = denomination;
	}

	public String getOffuse_time() {
		return offuse_time;
	}

	public void setOffuse_time(String offuse_time) {
		this.offuse_time = offuse_time;
	}

}