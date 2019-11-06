package io.hpb.contract.util;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser.Feature;
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
	
}
