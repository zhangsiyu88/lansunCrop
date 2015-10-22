package com.lansun.qmyo.domain;

public class BrowseData {

	private Browse browse;
	private Shop shop;
	private Activity activity;
	private HomePromoteData article;

	public HomePromoteData getArticle() {
		return article;
	}

	public void setArticle(HomePromoteData article) {
		this.article = article;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Browse getBrowse() {
		return browse;
	}

	public void setBrowse(Browse browse) {
		this.browse = browse;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

}
