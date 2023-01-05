package com.example.disruptor.ulog;

public enum BizId {

	/** 新增 **/
	ADD(-1,"新增"),
	/** 修改 **/
	MODIFY(0,"修改"),
	/** 修改 **/
	ONOROFF(2,"上下架"),
	/** 删除 **/
	DELETE(-2,"删除"),
	/** 发布 **/
	PUBLISH(1,"发布");

	private int code;

	private String name;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private BizId(int code, String name){
		this.code = code;
		this.name = name;
	}

}
