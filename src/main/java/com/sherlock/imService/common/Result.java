package com.sherlock.imService.common;

public class Result {

	private static final Integer SUCCESS_CODE = 1;
	private static final Integer EXCEPTION_CODE = 100;
	public static final Integer TOKEN_INVALID_CODE = 400;
	
	private Integer code;
	
	private String msg;
	
	private Object data;

	public static Result success(Object data){
		Result result = new Result();
		result.setCode(SUCCESS_CODE);
		result.setData(data);
		return result;
	}
	
	public static Result success(String msg, Object data){
		Result result = new Result();
		result.setCode(SUCCESS_CODE);
		result.setMsg(msg);
		result.setData(data);
		return result;
	}
	
	public static Result exception(Integer code, String msg){
		Result result = new Result();
		result.setCode(code==null?EXCEPTION_CODE:code);
		result.setMsg(msg);
		return result;
	}
	
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
