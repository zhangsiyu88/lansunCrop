package com.lansun.qmyo.utils;

import java.util.List;

import com.android.pc.util.Gps;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.domain.User;
public class GlobalValue {

	public static final String DESCRIPTOR = "com.umeng.share";

	public static Gps gps;
	public static String IP = "appapi.qmyo.com";
//	public static String IP = "appapi.qmyo.org";
	public static String URL_HOME_AD = "http://" + IP + "/advertisement/poster/";
	/**
	 * wx5078ff274c69bc8c a5af88ff8deb95831a8a4a21d5f0aaec
	 */
	/**
	 * 打包 wx75ffbaca73416dcb c9c2068fff7320366d54e2a88ff273b1
	 */
	/**
	 * 微信appid
	 */
	public static String WX_APP_ID = "wx75ffbaca73416dcb";
	public static String WX_APP_SECRET = "c9c2068fff7320366d54e2a88ff273b1";
	/**
	 * QQappid
	 */
	public static String QZONE_APP_ID = "wx5078ff274c69bc8c";
	public static String QZONE_APP_KEY = "a5af88ff8deb95831a8a4a21d5f0aaec";

	public static String TARGET_URL = "http://www.qmyo.com";

	/**
	 * 智能排序
	 */
	public static String URL_AUTH_CAPTCHA_LOGIN = "http://" + IP
			+ "/auth/captcha-login";
	/**
	 * 智能排序
	 */
	public static String URL_SEARCH_HOLDER_INTELLIGENT = "http://" + IP
			+ "/search/intelligent";
	
	/**
	 * 活动模糊搜索
	 */
	public static String URL_ACTIVITY_PUZZY = "http://" + IP
			+ "/xs/list?";
	
	/**
	 * 银行卡模糊搜索
	 */
	public static String URL_BANKCARD_PUZZY = "http://" + IP
			+ "/xs/bankcard?";
	/**
	 * 筛选
	 */
	public static String URL_SEARCH_HOLDER_SCREENING = "http://" + IP
			+ "/search/screening";
	/**
	 * 地区商圈
	 */
	public static String URL_SEARCH_HOLDER_DISTRICT = "http://" + IP
			+ "/search/district/";
	/**
	 * 板块服务
	 */
	public static String URL_SEARCH_HOLDER_SERVICE = "http://" + IP
			+ "/search/service/";
	
	public static String URL_ADVERTISEMENT_SEARCH = "http://" + IP
			+ "/advertisement/search";
	/**
	 * 获取用户token
	 */
	public static String URL_GET_ACCESS_TOKEN = "http://" + IP + "/token/";
	/**
	 * 全部活动
	 */
	public static String URL_ALL_ACTIVITY = "http://" + IP + "/activity/all?";
	/**
	 * 活动详情
	 */
	public static String URL_ACTIVITY_SHOP = "http://" + IP
			+ "/activity/%1$s/shop/%2$s";
	/**
	 * 活动中的商店
	 */
	public static String URL_ACTIVITY_SHOPS = "http://" + IP
			+ "/activity/%1$s/shops?";
	/**
	 * 商店信息
	 */
	public static String URL_AUTH_CAPTCHA = "http://" + IP
			+ "/auth/captcha?mobile=";
	public static String URL_AUTH_REGISTER = "http://" + IP + "/auth/register";
	public static String URL_AUTH_LOGIN = "http://" + IP + "/auth/login";
	public static String URL_AUTH_RESET = "http://" + IP + "/auth/reset";
	/**
	 * 活动的评论
	 */
	public static String URL_ACTIVITY_COMMENTS = "http://" + IP
			+ "/activity/%1$s/comments";
	/**
	 * 活动的评论的回复
	 */
	public static String URL_ACTIVITY_COMMENT = "http://" + IP
			+ "/activity/%1$s/comment/%2$s";
	/**
	 * 门店信息
	 */
	public static final String URL_SHOP = "http://" + IP + "/shop/";

	/**
	 * 活动投诉
	 */
	public static final String ACTIVITY_COMPLAIN = "http://" + IP
			+ "/activity/complain";

	public static final String URL_AREA_ALL = "http://" + IP + "/area/all";

	/**
	 * 极文列表
	 */
	public static final String URL_ARTICLE_ALL = "http://" + IP
			+ "/article/all?";

	/**
	 * 首页推荐
	 */
	public static final String URL_ARTICLE_PROMOTE = "http://" + IP
			+ "/article/promote/%1$s";

	/**
	 * 极文海报
	 */
	public static final String URL_ARTICLE_POSTER = "http://" + IP
			+ "/article/poster/";

	/**
	 * 版本更新通知
	 */
	public static final String UPDATE_NOTIFICATION = "http://" + IP
			+ "/version/info/";
	/**
	 * 关注门店
	 */
	public static String URL_USER_GZ_SHOP = "http://" + IP + "/shop";
	/**
	 * 取消门店关注
	 */
	public static String URL_QX_GZ_SHOP = "http://" + IP + "/shop/";

	public static String URL_FRESHEN_USER = "http://" + IP + "/user";
	
	public static String URL_USER_ACTIVITYBROWSES = "http://" + IP
			+ "/user/activitybrowses";
	public static String URL_USER_SHOPBROWSES = "http://" + IP
			+ "/user/shopbrowses";
	public static String URL_USER_COMMENTS = "http://" + IP + "/user/comments";
	public static String URL_USER_RESPONDS = "http://" + IP + "/user/responds";
	public static String URL_SHOP_ACTIVITYS = "http://" + IP
			+ "/shop/%1$s/activitys";
	public static String URL_USER_SAVE = "http://" + IP + "/user";
	/**
	 * 意见反馈
	 */
	public static String URL_USER_FEEDBACK = "http://" + IP + "/user/feedback";
	/**
	 * 收藏活动
	 */
	public static String URL_USER_ACTIVITY = "http://" + IP + "/activity";
	/**
	 * 银行卡
	 */
	public static String URL_BANKCARD = "http://" + IP + "/bankcard";
	/**
	 * 私人秘书
	 */
	public static String URL_SECRETARY = "http://" + IP + "/secretary";
	/**
	 * 提问列表
	 */
	public static String URL_SECRETARY_QUESTIONS = "http://" + IP
			+ "/secretary/questions?";
	/**
	 * 地区列表
	 */
	public static String URL_AREA = "http://" + IP + "/area";
	/**
	 * 城市站点
	 */
	public static String URL_SEARCH_SITE = "http://" + IP + "/search/site/";
	/**
	 * 地区
	 */
	public static String URL_SEARCH_CITY = "http://" + IP + "/area/";
	/**
	 * 搜索银行卡
	 */
	public static String URL_BANKCARD_ALL = "http://" + IP + "/bankcard/all?";

	/**
	 * 用户添加信用卡
	 */
	public static String URL_BANKCARD_ADD = "http://" + IP + "/bankcard";

	/**
	 * 用户搜索列表
	 */
	public static String URL_USER_QUERYS = "http://" + IP + "/user/querys";

	/**
	 * 用户删除银行卡
	 */
	public static String URL_BANKCARD_DELETE = "http://" + IP + "/bankcard/";
	/**
	 * 用户删除银行卡
	 */
	public static String URL_BANKCARD_DETAIL = "http://" + IP + "/bankcard/";
	/**
	 * 用户添加搜索记录
	 */
	public static String URL_USER_USER_QUERY = "http://" + IP + "/user/query";

	/**
	 * 临时用户
	 */
	public static String URL_AUTH_TEMPORARY = "http://" + IP
			+ "/auth/temporary";

	/**
	 * 推荐信用卡
	 */
	public static String URL_BANKCARD_RECOMMEND = "http://" + IP
			+ "/bankcard/recommend";

	/**
	 * 选中银行卡
	 */
	public static String URL_SELECT_BANKCARD = "http://" + IP
			+ "/bankcard/chosen";
	/**
	 * 获取当前银行卡
	 */
	public static String URL_BANKCARD_CHOSEN = "http://" + IP
			+ "/bankcard/chosen";
	/**
	 * 获取当前银行卡
	 */
	public static String URL_SECRETARY_SAVE = "http://" + IP + "/secretary";
	/**
	 * 获取当前银行卡
	 */
	public static String URL_PUSH_TOKEN = "http://" + IP + "/push-token";
	/**
	 * 删除活动
	 */
	public static String URL_USER_ACTIVITY_DELETE = "http://" + IP
			+ "/activity/%1$s?shop_id=%2$s";
	/**
	 * 秘书提问
	 */
	public static String URL_SECRETARY_QUESTION = "http://" + IP
			+ "/secretary/question";
	/**
	 * 秘书提问
	 */
	public static String URL_SECRETARY_QUESTION_DETAIL = "http://" + IP
			+ "/secretary/question/";
	/**
	 * 用户秘书提问列表
	 */
	public static String URL_SECRETARY_QUESTION_LIST = "http://" + IP
			+ "/secretary/questions";
	/**
	 * 用户秘书提问列表
	 */
	public static String URL_USER_ACTIVITY_COMMENT = "http://" + IP
			+ "/activity/comment";
	/**
	 * 评论举报
	 */
	public static String URL_ACTIVITY_REPORT = "http://" + IP
			+ "/activity/report";

	public static User user;

	public static Secretary secretary;
	
	public static List<Sensitive> sensitiveList; 

	public static boolean isFirst = true;

	/**
	 * 优惠券
	 */
	public static String URL_COUPON = "http://" + IP + "/coupon";

	/**
	 * 领取优惠券
	 */
	public static String URL_COUPON_GIFT = "http://" + IP + "/coupon/gift";

	/**
	 * 删除用户浏览记录
	 */
	public static String URL_USER_DELETE_BROWSES = "http://" + IP
			+ "/user/browses";

	/**
	 * 用户极文操作
	 */
	public static String URL_USER_ARTICLE = "http://" + IP + "/article";
	/**
	 * 用户删除极文操作
	 */
	public static String URL_USER_ARTICLE_DELETE = "http://" + IP + "/article/";
	/**
	 * 用户极文信息
	 */
	public static String URL_USER_ARTICLE_INFO = "http://" + IP + "/article/";
	/**
	 * 用户消息
	 */
	public static String URL_USER_MESSAGE = "http://" + IP + "/message";

	/**
	 * 用户消息列表
	 */
	public static String URL_USER_MESSAGE_LIST = "http://" + IP + "/message/";
	/**
	 * 用户消息列表
	 */
	public static String URL_USER_ARTICLEBROWSES = "http://" + IP
			+ "/user/articlebrowses";
	/**
	 * 用户消息列表(在代码中直接使用了下面的串)
	 */
	public static String URL_GPS_ADCODE = "http://mo.amap.com/service/geo/getadcode.json?longitude=%1$s&latitude=%2$s";
	
	

	public static enum MESSAGE {
		maijie, activity, comment, secretary
	}
	
	/**
	 * 用户消息
	 */
	public  static   MySecretary  mySecretary;

	public static  boolean  isWaitingForUpdateApp = false;
	

}
