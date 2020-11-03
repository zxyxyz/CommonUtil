package com.test;

import com.common.util.JsonUtils;

public class TestMain {
	
	public static void main(String[] args) {
		Bean bean = new Bean();
		bean.setName("王磊");
		bean.setAge("23");
		bean.setSex("女");
		String returnData = JsonUtils.objectToJson(bean);
		
		System.out.println(returnData);
		
		System.out.println(JsonUtils.jsonToPojo(returnData, Bean.class).getAge());
	}
}
