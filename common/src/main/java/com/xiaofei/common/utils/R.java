/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.xiaofei.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xiaofei.common.exception.BizCodeEnum;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public R setData(Object data){
		put("data",data);
		return this;
	}
	//利用fastJson对远程调用的实体类进行逆转
	public <T>T getData(TypeReference<T> typeReference){
		Object data = get("data");//默认map
		String s = JSON.toJSONString(data);
		return JSON.parseObject(s, typeReference);
	}
	//利用fastJson对远程调用的实体类进行逆转(参数为class)
	public <T>T getData( Class<T> clazz){
		Object data = get("data");//默认map
		String s = JSON.toJSONString(data);//转换成String
		return JSON.parseObject(s, clazz);
	}
	//利用fastJson对远程调用的实体类进行逆转
	public <T>T getDataByKey(String key,TypeReference<T> typeReference){
		Object data = get(key);//默认map
		String s = JSON.toJSONString(data);
		return JSON.parseObject(s, typeReference);
	}

	//利用fastJson对远程调用的实体类进行逆转(参数为class)
	public <T>T getDataByKey(String key, Class<T> clazz){
		Object data = get(key);//默认map
		String s = JSON.toJSONString(data);//转换成String
		return JSON.parseObject(s, clazz);
	}


	public R() {
		put("code", BizCodeEnum.SUCCESS_CODE.getCode());
		put("msg", "success");
	}

	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode() {
		return  (Integer) this.get("code");
	}
	public String getMsg() {
		return  (String) this.get("msg");
	}
}
