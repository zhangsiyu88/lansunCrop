package com.lansun.qmyo.fragment.searchbrand;
import java.io.IOException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
public interface RequestCallBack {
	void onSuccess(Response response);
	void onFailure(Request request,IOException o);
}
