package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lansun.qmyo.R;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.MyMatchFilter;
import com.lansun.qmyo.utils.MyTransformFilter;

import android.app.Activity;
import android.graphics.Color;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class TipInnerBaseAdapter extends BaseAdapter{

	private ArrayList<HashMap<String, String>> mDataList;
	private Activity ctx;
	private String baseUri;
	private Pattern pattern;
	private String realUrl;

	public TipInnerBaseAdapter(ArrayList<HashMap<String, String>> dataList,Activity activity){
		this.mDataList = dataList;
		this.ctx = activity;
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	ViewHolder	holder = null;
		if (convertView == null) {
			convertView = View.inflate(ctx, R.layout.activity_activity_tip_item_inner_item, null);
			holder = new ViewHolder();
			holder.left_point = (TextView) convertView.findViewById(R.id.left_point);
			holder.tv_activity_content_mx_content = (TextView) convertView.findViewById(R.id.tv_activity_content_mx_content);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.left_point.setVisibility(View.VISIBLE);
		String oldStr = mDataList.get(position).get("contentId"+position);
//		covertLong2ShortLink(holder, oldStr);
		holder.tv_activity_content_mx_content.setText(oldStr);
		return convertView;
	}

	/**
	 * 长连接转至短连接
	 * @param holder
	 * @param oldStr
	 */
	private void covertLong2ShortLink(ViewHolder holder, String oldStr) {
		//		holder.tv_activity_content_mx_content.setText(oldStr);
		//		String oldStr = "http://zhidao.baidu.com/link?url=F1dRZU4jmsD5XJDXh3cT5SePRJcxQLue75UstYb0hEe63OrJkeozVfL-uO4oNkIiDFeiH4U9pWG3-cJaYHHhC_ 带我去的玩企鹅去问问  43535332312 望闻问切 "; 
				
				 //筛选出网址
		        int flag = Pattern.CASE_INSENSITIVE;
		        String regex =  "[a-zA-z]+://[^\\s]*";
		        String regex1 = "\u70b9\u51fb\u8df3\u8f6c\u0028\u8fc8\u754c\u5df2\u5ba1\u6838\u0029";
		        
		        Pattern pattern = Pattern.compile(regex,flag);
		        Pattern pattern1 = Pattern.compile(regex1,flag);
				String  baseUri = ""; 
				String  baseUri1 = "http://"; 
		//        String afterRegexStr = oldStr.replaceAll(regex, "点击跳转(迈界已审核)");
		//        String afterRegexStr = oldStr.replaceAll(regex, "\u70b9\u51fb\u8df3\u8f6c\u0028\u8fc8\u754c\u5df2\u5ba1\u6838\u0029");
		        String afterRegexStr = oldStr.replaceAll(regex, "http://maijie/LoadingUrl");
		       
		        //匹配出实际的realUrl
		        Matcher matcher = pattern.matcher(oldStr);
		        //修改后的文字，对其中的"点击跳转（迈界已审核）"设置 网页超链接
		        Matcher matcher1 = pattern1.matcher(afterRegexStr);
		      
		        LogUtils.toDebugLog("afterRegexStr", "afterRegexStr"+afterRegexStr);
		       
		    	if(matcher.find()){
		    		realUrl = matcher.group();
		        	holder.tv_activity_content_mx_content.setText(afterRegexStr);
		        	Linkify.addLinks(holder.tv_activity_content_mx_content,pattern, baseUri, new MyMatchFilter(), new MyTransformFilter(realUrl));
		//        	Linkify.addLinks(holder.tv_activity_content_mx_content, Linkify.PHONE_NUMBERS);
		        }else{
		//        	if(matcher.find()){
		//        		realUrl = matcher.group();
		//        	 }
		//        	holder.tv_activity_content_mx_content.setText(afterRegexStr);
		//        	Linkify.addLinks(holder.tv_activity_content_mx_content, Patterns.WEB_URL, baseUri1, new MyMatchFilter(), new MyTransformFilter(realUrl));
		//        	Linkify.addLinks(holder.tv_activity_content_mx_content, pattern1, baseUri1);
		        	holder.tv_activity_content_mx_content.setText(oldStr);
		        	Linkify.addLinks(holder.tv_activity_content_mx_content, Linkify.PHONE_NUMBERS);
		        }
		//        if(matcher1.find()){
		//        	realUrl = matcher1.group();
		//        }else{
		//        	holder.tv_activity_content_mx_content.setText(oldStr);
		//        }
	}
	
	class ViewHolder{
		public TextView tv_activity_content_mx_content;
		private TextView left_point;
	}
	
}
