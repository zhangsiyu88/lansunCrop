package com.lansun.qmyo.biz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class AddQuestionBiz implements IAddQuestionBiz{
	private Map<String,String> paramas=new HashMap<>();
	@Override
	public void sendQuestion(String content, String type, String principal,final RequestCallBack callback) {
		Log.e("paramas", "内容"+content+",type"+type+",principal"+principal);
		paramas.put("content", content);
		paramas.put("type", type);
		paramas.put("principal", principal);
		OkHttp.asyncPost(GlobalValue.URL_SECRETARY_QUESTION, paramas, new Callback() {
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