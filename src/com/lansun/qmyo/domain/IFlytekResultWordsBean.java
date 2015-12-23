package com.lansun.qmyo.domain;

import java.util.ArrayList;

public class IFlytekResultWordsBean {

	private int bg;
	public int getBg() {
		return bg;
	}
	public void setBg(int bg) {
		this.bg = bg;
	}
	public ArrayList<IFlytekResultSimpleWordBean> getCw() {
		return cw;
	}
	public void setCw(ArrayList<IFlytekResultSimpleWordBean> cw) {
		this.cw = cw;
	}
	private ArrayList<IFlytekResultSimpleWordBean> cw;
}
