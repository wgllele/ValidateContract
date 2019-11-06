package io.hpb.contract.common;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppObjectUtil {
	
	private static final String CLASS_NAME = "class";
	private static Boolean isEscapeNull = false;
	private static final String lineSeparator = System.getProperty("line.separator");
	private static final Logger logger = LoggerFactory.getLogger(AppObjectUtil.class);
	private final static String[][] ESCAPE_CHARACTERS = {
		{"\\\\", "\\\\\\\\"},
		{"\"", "\\\\\""},
		{"\\n", "\\\\n"},
		{"\\r", "\\\\r"},
		{"\\t", "\\\\t"},
		{"\\f", "\\\\f"},
		// 根据http://www.json.org/上说明不必过滤一下字符
		//{"\'", "\\\\\'"},
	};
	
	
	private static Set<Object> iProcessedObjects;
	
	/**
	* <p>Title: </p>
	* <p>Description:构造器 </p>
	*/
	public AppObjectUtil() {
		super();
	}
	
    /**
    * @Title: objectToJson
    * @Description: 把对象转换成json格式的数据
    * @param object
    * @return 
    */
	public static String toJson(Object object,String dateFormat){
		StringBuilder sb=new StringBuilder();
    	try {
    		iProcessedObjects = new HashSet<Object>();	
			outputAsJson(sb, "", object,dateFormat);
		} catch (Exception e) {
			 logger.error("toJson error",e);
		}
    	return sb.toString();
	}
    public static String toJson(Object object){
    	StringBuilder sb=new StringBuilder();
    	try {
    		iProcessedObjects = new HashSet<Object>();	
			outputAsJson(sb, "", object);
		} catch (Exception e) {
			logger.error("toJson error",e);
		}
    	return sb.toString();
    }
	
	/**
	* @Title: outputAsJson
	* @Description: 返回json格式的数据
	* @param sb 返回json数据到StringBuilder对象中
	* @param indent 当前indent
	* @param object 需要转换成json格式的对象
	* @throws Exception 
	*/
    private static void outputAsJson(StringBuilder sb,String indent, Object object,String dateFormat) throws Exception {

		if (object == null) {
			if(getIsEscapeNull()){
				return;
			}
			sb.append("null");
		}else if(object instanceof Date){
			Date date=(Date)object;
			SimpleDateFormat localSimpleDateFormat=new SimpleDateFormat();
			localSimpleDateFormat.applyPattern(dateFormat);
			sb.append(quote(localSimpleDateFormat.format(date)));
		}
		else if (iProcessedObjects.contains(object) || object instanceof String  || 
				object instanceof Character || object instanceof Class || object instanceof Throwable) {
			sb.append(quote(object));
		}else if (object instanceof Boolean || object instanceof Number) {
			sb.append(object.toString());
		}else if (object instanceof Map) {
			iProcessedObjects.add(object);
			Map<?,?> map = (Map<?,?>) object;
			if (map.size() == 0) {
				sb.append("{}");
			}else {
				sb.append("{");
				Iterator<?> keys = map.keySet().iterator();
				int i=0;
				while (keys.hasNext()) {
					Object key = keys.next();
					if(getIsEscapeNull()&&map.get(key)==null){
						continue;
					}
					if(i>0){
						sb.append(",");
					}
					i++;
					sb.append(lineSeparator);
					sb.append(indent);
					sb.append("\t");
					sb.append(quote(key.toString()));
					sb.append(": ");
					outputAsJson(sb,indent + "\t", map.get(key),dateFormat);
				}
				sb.append(lineSeparator);
				sb.append(indent);
				sb.append("}");
			}
			iProcessedObjects.remove(object);
		}else if (object.getClass().isArray()) {
			iProcessedObjects.add(object);
			int length = Array.getLength(object);
			if (length == 0) {
				sb.append("[]");
			}else if (length == 1) {
				sb.append("[");
				if(getIsEscapeNull()&&Array.get(object, 0)==null){
					
				}else{
					outputAsJson(sb,indent, Array.get(object, 0),dateFormat);
				}
				sb.append("]");
			}else {
				sb.append("[");
				int k=0;
				for (int i = 0; i < length; i++) {
					if(getIsEscapeNull()&&Array.get(object, i)==null){
						continue;
					}
					if (k>0) {
						sb.append(",");
					}
					k++;
					sb.append(lineSeparator);
					sb.append(indent);
					sb.append("\t");
					outputAsJson(sb,indent + "\t", Array.get(object, i),dateFormat);
				}
				sb.append(lineSeparator);
				sb.append(indent);
				sb.append("\t");
				sb.append("]");
			}
			iProcessedObjects.remove(object);
		}else if (object instanceof Collection) {
			iProcessedObjects.add(object);
			outputAsJson(sb,indent, ((Collection<?>)object).toArray(),dateFormat);
			iProcessedObjects.remove(object);
		}else {
			iProcessedObjects.add(object);
			try {
				PropertyDescriptor properties[] = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
				Map<Object,Object> map = new HashMap<Object,Object>(properties.length);
				for (int i = 0; i < properties.length; i++) {
					Method readMethod = properties[i].getReadMethod();
					if (readMethod != null) {
						boolean isContinue=false;
						for(Field field:object.getClass().getDeclaredFields()){
							if((field.getName().substring(0,1).toLowerCase()+field.getName().substring(1)).equals(properties[i].getName())){
								map.put(field.getName(), readMethod.invoke(object, (Object[])null));
								isContinue=true;
								break;
							}
						}
						if(!isContinue){
							map.put(properties[i].getName(), readMethod.invoke(object, (Object[])null));
						}
					}
				}
				outputAsJson(sb,indent, map,dateFormat);
				iProcessedObjects.remove(object);
			}catch (InvocationTargetException e) {
				 logger.error("outputAsJson error",e);
				throw e;
			}catch (IllegalAccessException e) {
				 logger.error("IllegalAccessException error",e);
				throw e;
			}catch (IntrospectionException e) {
				 logger.error("IntrospectionException error",e);
				throw e;
			}
		}
	
    }
	private static void outputAsJson(StringBuilder sb,String indent, Object object) throws Exception {
		if (object == null) {
			if(getIsEscapeNull()){
				return;
			}
			sb.append("null");
		}else if (iProcessedObjects.contains(object) || object instanceof String || object instanceof Date || 
				object instanceof Character || object instanceof Class || object instanceof Throwable) {
			sb.append(quote(object));
		}else if (object instanceof Boolean || object instanceof Number) {
			sb.append(object.toString());
		}else if (object instanceof Map) {
			iProcessedObjects.add(object);
			Map<?,?> map = (Map<?,?>) object;
			if (map.size() == 0) {
				sb.append("{}");
			}else {
				sb.append("{");
				Iterator<?> keys = map.keySet().iterator();
				int i=0;
				while (keys.hasNext()) {
					Object key = keys.next();
					if(getIsEscapeNull()&&map.get(key)==null){
						continue;
					}
					if(i>0){
						sb.append(",");
					}
					i++;
					sb.append(lineSeparator);
					sb.append(indent);
					sb.append("\t");
					sb.append(quote(key.toString()));
					sb.append(": ");
					outputAsJson(sb,indent + "\t", map.get(key));
				}
				sb.append(lineSeparator);
				sb.append(indent);
				sb.append("}");
			}
			iProcessedObjects.remove(object);
		}else if (object.getClass().isArray()) {
			iProcessedObjects.add(object);
			int length = Array.getLength(object);
			if (length == 0) {
				sb.append("[]");
			}else if (length == 1) {
				sb.append("[");
				if(getIsEscapeNull()&&Array.get(object, 0)==null){
					
				}else{
					outputAsJson(sb,indent, Array.get(object, 0));
				}
				sb.append("]");
			}else {
				sb.append("[");
				int k=0;
				for (int i = 0; i < length; i++) {
					if(getIsEscapeNull()&&Array.get(object, i)==null){
						continue;
					}
					if (k>0) {
						sb.append(",");
					}
					k++;
					sb.append(lineSeparator);
					sb.append(indent);
					sb.append("\t");
					outputAsJson(sb,indent + "\t", Array.get(object, i));
				}
				sb.append(lineSeparator);
				sb.append(indent);
				sb.append("\t");
				sb.append("]");
			}
			iProcessedObjects.remove(object);
		}else if (object instanceof Collection) {
			iProcessedObjects.add(object);
			outputAsJson(sb,indent, ((Collection<?>)object).toArray());
			iProcessedObjects.remove(object);
		}else {
			iProcessedObjects.add(object);
			try {
				PropertyDescriptor properties[] = Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors();
				Map<Object,Object> map = new HashMap<Object,Object>(properties.length);
				for (int i = 0; i < properties.length; i++) {
					Method readMethod = properties[i].getReadMethod();
					if (readMethod != null) {
						boolean isContinue=false;
						for(Field field:object.getClass().getDeclaredFields()){
							if((field.getName().substring(0,1).toLowerCase()+field.getName().substring(1)).equals(properties[i].getName())){
								try {
									map.put(field.getName(), readMethod.invoke(object, (Object[])null));
								} catch (Exception e) {
									logger.error(field.getName(),e);
								}
								isContinue=true;
								break;
							}
						}
						if(!isContinue){
							map.put(properties[i].getName(), readMethod.invoke(object, (Object[])null));
						}
					}
				}
				outputAsJson(sb,indent, map);
				iProcessedObjects.remove(object);
			}catch (InvocationTargetException e) {
				 logger.error("InvocationTargetException error",e);
				throw e;
			}catch (IllegalAccessException e) {
				 logger.error("IllegalAccessException error",e);
				throw e;
			}catch (IntrospectionException e) {
				 logger.error("IntrospectionException error",e);
				throw e;
			}
		}
	}
	
	/**
	* @Title: quote
	* @Description: 替换掉特殊字符为html识别的字符
	* @param object
	* @return 
	*/
	private static String quote(Object object) {
		String str = object.toString();
		for (int i = 0; i < ESCAPE_CHARACTERS.length; i++) {
			str = str.replaceAll(ESCAPE_CHARACTERS[i][0], ESCAPE_CHARACTERS[i][1]);
		}
		StringBuffer sb = new StringBuffer(str.length() + 2);
		sb.append("\"");
		sb.append(str);
		sb.append("\"");
		String result = sb.toString();
		return result;
	}

	/**
	* @Title: sqlStrToSqlQueryString
	* @Description: 把sql字符串按行解析并生成合法SQL语句
	* @param sqlStr
	* @return
	* @throws IOException 
	*/
	public static String sqlStrToSqlQueryString(String sqlStr) throws IOException {
		if(sqlStr==null){
			return null;
		}
		BufferedReader reader = new BufferedReader(new StringReader(sqlStr));
		String line = null;
		StringBuffer queryBuffer = null;
		while ((line = reader.readLine()) != null) {
			if (queryBuffer == null) {
				queryBuffer = new StringBuffer();
			}
			if (line.length() > 0 && !line.startsWith("--")) {
				queryBuffer.append(line).append(" ");
				if (line.lastIndexOf(";") !=line.length()-1) {
					continue;
				}else{
					return queryBuffer.substring(0, queryBuffer.length()-2);
				}
			}
		}
		if(queryBuffer==null){
			return sqlStr;
		}
		return queryBuffer.substring(0, queryBuffer.length()-1);
	}

	/**
	 * @return the isEscapeNull
	 */
	public static Boolean getIsEscapeNull() {
		return isEscapeNull;
	}

	/**
	 * @param isEscapeNull the isEscapeNull to set
	 */
	public static void setIsEscapeNull(Boolean isEscapeNull) {
		AppObjectUtil.isEscapeNull = isEscapeNull;
	}
	/**
	* @Title: generateFieldMapFromObject
	* @param targetClass
	* @return
	* @throws Exception 
	*/
	public static Map<String, Object> generateFieldMapFromObject(Object targetClass)throws Exception{
		if (targetClass == null) {
			return null;
		}
		Map<String, Object> fieldMap=null;
		PropertyDescriptor properties[] = Introspector.getBeanInfo(targetClass.getClass()).getPropertyDescriptors();
		for (int i = 0; i < properties.length; i++) {
			Method readMethod = properties[i].getReadMethod();
			if (readMethod != null) {
				if(fieldMap==null){
					fieldMap=new HashMap<String, Object>();
				}
				try {
					if(CLASS_NAME.equals(properties[i].getName())){
						continue;
					}
					fieldMap.put(properties[i].getName(), readMethod.invoke(targetClass, (Object[])null));
				} catch (Exception e) {
					continue;
				}
			}
		}
		return fieldMap;
		
	}
	/**
	 * @param targetClass
	 * @param fieldMap
	 * @return
	 * @throws Exception
	 */
	public static <K> K generateObjectFromFieldMap(Class<K> objectClass, Map<String, Object> fieldMap) throws Exception {
		if (objectClass == null) {
			return null;
		}
		K targetClass = objectClass.newInstance();
		PropertyDescriptor properties[] = Introspector.getBeanInfo(objectClass).getPropertyDescriptors();
		for (int i = 0; i < properties.length; i++) {
			Method writeMethod = properties[i].getWriteMethod();
			try {
				Object object = fieldMap.get(properties[i].getName());
				if(object!=null){
					writeMethod.invoke(targetClass, object);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				continue;
			}
		}
		return targetClass;
	}	
	
}