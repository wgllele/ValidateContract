package io.hpb.contract.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *@author:wanggengle
 *@since:JDK1.8
 *@date:2017-05-15 12:40:32
 *@update:2017-05-15 12:40:32
 *@version:1.0 
 *@modifier:修改人
 *@reviewer:复审人
 *@description:json字符串和java对象之间的互转
 */
public class ObjectJsonHelper {
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	private static ObjectMapper om=new ObjectMapper();
	static{
		om.configure(Feature.ALLOW_SINGLE_QUOTES,true);
		om.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
		om.setSerializationInclusion(Include.ALWAYS);
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.setDateFormat(new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS));
	}
	/**
	 * @Title:serialize
	 * @Description:把java对象序列化为字符串
	 * @author:wanggengle
	 * @date:2017-05-15 12:41:26
	 * @return String
	 */
	public static String serialize(Object o) throws JsonProcessingException{
		return om.writeValueAsString(o);
	}
	/**
	 * @Title:deserialize
	 * @Description:把字符串反序列化为java对象
	 * @author:wanggengle
	 * @date:2017-05-15 12:42:00
	 * @return T
	 */
	public static  <T> T deserialize(String str,Class<T> clazz) throws JsonParseException, JsonMappingException, IOException{
		return om.readValue(str.getBytes(),clazz);
	}
	/**
	 * 在jQuery的ajax调用中加入即可，headers: {useEncode: false}
	 * 
	 * common.js已经对参数处理了
	 * // 指定预处理参数选项的函数，只预处理dataType为json
	 * $.ajaxPrefilter("json", function(options, originalOptions, jqXHR){
	 * 	if(options.headers.useEncode){
	 * 	  var newDatas=[
	 * 		originalOptions.data.serviceCode,
	 * 		corecode.security.BASE64.encode($.toJSON(originalOptions.data.param)),
	 * 		originalOptions.data.returnUrl,
	 * 		originalOptions.data.enCodekey,
	 * 		originalOptions.data.rsaKey
	 * 		options.data=$.toJSON(newDatas);
	 * 	  ];
	 * 	}else if(options.contentType.indexOf("application/json")>-1){
	 * 	   options.data=$.toJSON(originalOptions.data);
	 * 	}
	 * });
	 * 
	 * 
	 * 
	 * 对应前端corecode.security.BASE64.encode和corecode.security.BASE64.decode方法
	 * @param args
	 * @throws JsonProcessingException
	 */
	public static void main(String[] args) throws JsonProcessingException{
		String serialize="[\"AccountIndex\",\"FE343LEKF\",\"/Account/queryAcountInfo\",\"DF34F\",\"123456\"]";
		byte[] encode = Base64Utils.encode(serialize.getBytes(StandardCharsets.UTF_8));
		System.out.println(new String(encode,StandardCharsets.UTF_8));
		String decodeParam=new String(Base64Utils.decode(encode),StandardCharsets.UTF_8);
		System.out.println(decodeParam);
	}
}
