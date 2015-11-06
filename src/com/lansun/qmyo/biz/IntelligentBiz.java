package com.lansun.qmyo.biz;

import java.io.IOException;

import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class IntelligentBiz {
	public void getIntelligent(final RequestCallBack callback){
		OkHttp.asyncGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT, new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				callback.onResponse(response);
			}
			@Override
			public void onFailure(Request request, IOException exception) {
				callback.onFailure(request, exception);
			}
		});
	}
}
