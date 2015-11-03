package com.lansun.qmyo.biz;
import com.lansun.qmyo.listener.RequestCallBack;
public interface IAddQuestionBiz {
	void sendQuestion(String content,String type,String principal,RequestCallBack callback);
}
