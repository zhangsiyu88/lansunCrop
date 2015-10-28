package com.lansun.qmyo.domain.information;

import java.io.Serializable;

public class InformationCount implements Serializable{
	private int maijie;

	private int activity;

	private int comment;

	private int secretary;

	public void setMaijie(int maijie){
	this.maijie = maijie;
	}
	public int getMaijie(){
	return this.maijie;
	}
	public void setActivity(int activity){
	this.activity = activity;
	}
	public int getActivity(){
	return this.activity;
	}
	public void setComment(int comment){
	this.comment = comment;
	}
	public int getComment(){
	return this.comment;
	}
	public void setSecretary(int secretary){
	this.secretary = secretary;
	}
	public int getSecretary(){
	return this.secretary;
	}
}
