package com.lansun.qmyo.domain;

import java.util.ArrayList;

public class IFlytekResultBean {

	private int sn;
	private boolean ls;
	private int bg;
	private int ed;
	private ArrayList<IFlytekResultWordsBean> ws;
	
	public int getSn() {
		return sn;
	}
	public void setSn(int sn) {
		this.sn = sn;
	}
	public boolean isLs() {
		return ls;
	}
	public void setLs(boolean ls) {
		this.ls = ls;
	}
	public int getBg() {
		return bg;
	}
	public void setBg(int bg) {
		this.bg = bg;
	}
	public int getEd() {
		return ed;
	}
	public void setEd(int ed) {
		this.ed = ed;
	}
	public ArrayList<IFlytekResultWordsBean> getWs() {
		return ws;
	}
	public void setWs(ArrayList<IFlytekResultWordsBean> ws) {
		this.ws = ws;
	}

	
	
	
}
