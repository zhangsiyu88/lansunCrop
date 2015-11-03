package com.lansun.qmyo.listener;

import java.io.IOException;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public interface RequestCallBack {
	void onResponse(Response response) throws IOException ;
	void onFailure(Request request, IOException exception);
}
