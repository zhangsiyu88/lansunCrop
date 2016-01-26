package com.lansun.qmyo.listener;
/**
 * 刮刮乐的内容的涂抹监听器
 * 
 * @author zdy
 *
 */
public interface RubbleTextListener {

	/** 当刮刮乐的内容被涂抹超过一定量的时候，对外进行通知 */
	public void notifyShowBehind();
}
