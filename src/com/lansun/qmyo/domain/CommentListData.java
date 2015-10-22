package com.lansun.qmyo.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 回复Data
 * 
 * @author bhxx
 * 
 */
public class CommentListData implements Serializable {

	private Comment comment;
	private Activity activity;
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
