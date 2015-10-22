package com.lansun.qmyo.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NetUtil {
	public static HttpURLConnection doDelete(String urlStr,
			Map<String, Object> paramMap) throws Exception {
		String paramStr = prepareParam(paramMap);
		if (paramStr == null || paramStr.trim().length() < 1) {

		} else {
			urlStr += "?" + paramStr;
		}
		System.out.println(urlStr);
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("DELETE");
		if (conn.getResponseCode() == 200) {
			return conn;
		} else {
			return null;
		}
	}

	private static String prepareParam(Map<String, Object> paramMap) {
		StringBuffer sb = new StringBuffer();
		if (paramMap.isEmpty()) {
			return "";
		} else {
			for (String key : paramMap.keySet()) {
				String value = (String) paramMap.get(key);
				if (sb.length() < 1) {
					sb.append(key).append("=").append(value);
				} else {
					sb.append("&").append(key).append("=").append(value);
				}
			}
			return sb.toString();
		}
	}
}
