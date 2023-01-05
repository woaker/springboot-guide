package com.example.disruptor.ulog;

public enum UlogType {
	
	/** 专辑 **/
	ALBUM("专辑"),
	/** 年龄段 **/
	AGEGROUP("年龄段"),
	/** 栏目 **/
	CATEGORY("栏目"),
	/** 焦点图位置 **/
	FOCUS("焦点图位置"),
	/** 焦点图图片 **/
	FOCUSURL("焦点图图片"),
	/** 专辑推荐信息 **/
	RECOMMEND("专辑推荐信息"),
	/** 标签 **/
	TAG("标签"),
	/** 声音标题 **/
	TRACK("声音"),
	/** 用户信息 **/
	USER("用户信息"),
	/** 发放vip **/
	SEND_VIP("发放vip"),
	/** 发放专辑 **/
	SEND_ALBUM("发放专辑"),
	/** 推送消息 **/
	PUSH("推送消息"),
	/** 拼团活动 **/
	ACTIVITY("拼团活动"),
	/** 拼团订单 **/
	ORDER("拼团个人订单"),
	/** 拼团订单 **/
	ORDERGROUP("拼团团订单"),
	/** 跟读/朗读 **/
	FOLLOWANDREAD("跟读/朗读"),
	/** 听单列表 **/
	TINGLIST("听单列表"),
	/** 听单声音 **/
	TINGITEM("听单声音"),
	/** 枚举 **/
	ENUM("枚举字段"),
	/** 商品 **/
	PRODUCT("商品"),
	/** 首页金刚区 **/
	HOME_PARTITION("首页金刚区"),
	/** 首页模块 **/
	HOME_MODULE("首页模块"),
	/** 首页头图 **/
	HOME_PIC("首页头图"),
	/** 打卡补偿 **/
	ADD_SIGN("打卡补偿");
	
	private String name;
	
	UlogType(String name) {
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
