package com.lansun.qmyo.fragment.searchbrand;
import java.io.IOException;
import java.util.Map;

import android.util.Log;

import com.lansun.qmyo.app.App;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class SearchBrandBiz {
	public void getBrandList(Map<String, String> paramas,String tag,final RequestCallBack requestCallBack){
		String url=GlobalValue.URL_ALL_ACTIVITY+
				"site="+paramas.get("site")+"&"+
				"service="+paramas.get("service")+"&"+
				"position="+paramas.get("position")+"&"+
				"intelligent="+paramas.get("intelligent")+"&"+
				"type="+paramas.get("type")+"&"+
				"location="+paramas.get("location")+"&"+
				"query="+paramas.get("query");
		Log.e("SearchBrandBiz", "开始请求");
		OkHttp.asyncGet(url, 
				"Authorization", 
				"Bearer " + App.app.getData("access_token"),
				 tag,new Callback() {
					@Override
					public void onResponse(Response response) throws IOException {
						requestCallBack.onSuccess(response);
					}
					@Override
					public void onFailure(Request arg0, IOException arg1) {
						requestCallBack.onFailure(arg0,arg1);
					}
				});
	}
}
