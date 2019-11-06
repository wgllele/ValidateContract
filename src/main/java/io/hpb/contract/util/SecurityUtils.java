package io.hpb.contract.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

public abstract class SecurityUtils{
	private static final String CIPHER_AES_NAME = "AES";
	private static final String CIPHER_AES_MODE = "AES/CBC/PKCS5Padding";
	private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
	/**
	 * 使用AES算法做数据解密处理。
	 * 
	 * @param key
	 *            解密Key（16字节）
	 * @param iv
	 *            解密初始化向量（16字节）
	 * @param encData
	 *            拟解密的数据（UTF8）
	 * @return 原始数据
	 */
	public static byte[] aesDecryption(byte[] key, byte[] iv, byte[] encData) {
		try {
			Cipher cipher;
			cipher = Cipher.getInstance(CIPHER_AES_MODE);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CIPHER_AES_NAME), new IvParameterSpec(iv));
			byte[] orgData = cipher.doFinal(encData);
			return orgData;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}

	/**
	 * 使用AES算法做数据加密处理。
	 * 
	 * @param key
	 *            加密Key
	 * @param iv
	 *            加密初始向量
	 * @param orgData
	 *            拟加密数据
	 * @return 加密数据
	 */
	public static byte[] aesEncryption(byte[] key, byte[] iv, byte[] orgData) {
		try {
			Cipher cipher;
			cipher = Cipher.getInstance(CIPHER_AES_MODE);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CIPHER_AES_NAME), new IvParameterSpec(iv));
			byte[] encData = cipher.doFinal(orgData);
			return encData;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}
	public static String decodeAESBySalt(String password, String salt) throws Exception {
		return new String(SecurityUtils.aesDecryption(
				salt.substring(0, 16).getBytes(StandardCharsets.UTF_8), 
				salt.substring(16, 32).getBytes(StandardCharsets.UTF_8), 
				Hex.decodeHex(password.toCharArray())),StandardCharsets.UTF_8);
	}
	public static String encodeAESBySalt(String password, String salt) {
		return new String(Hex.encodeHex(SecurityUtils.aesEncryption(
				salt.substring(0, 16).getBytes(StandardCharsets.UTF_8), 
				salt.substring(16, 32).getBytes(StandardCharsets.UTF_8), 
				password.getBytes(StandardCharsets.UTF_8))));
	}
	/**
	 * @Title: decode
	 * @Description: 对应前台解密
	 * @param paramString
	 * @return
	 */
	public static String decode(String paramString) {
		if (paramString == null) {
			return "";
		}
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; i < paramString.length(); ++i) {
			char c = paramString.charAt(i);
			String str;
			switch (c) {
			case '~':
				str = paramString.substring(i + 1, i + 3);
				localStringBuffer.append((char) Integer.parseInt(str, 16));
				i += 2;
				break;
			case '^':
				str = paramString.substring(i + 1, i + 5);
				localStringBuffer.append((char) Integer.parseInt(str, 16));
				i += 4;
				break;
			default:
				localStringBuffer.append(c);
			}
		}
		return localStringBuffer.toString();
	}

	/**
	 * @Title: encode
	 * @Description: 对应前台加密
	 * @param paramString
	 * @return
	 */
	public static String encode(String paramString) {
		if (paramString == null) {
			return "";
		}
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; i < paramString.length(); i++) {
			int j = paramString.charAt(i);
			String str;

			str = Integer.toString(j, 16);
			for (int k = str.length(); k < 4; k++)
				str = "0" + str;
			localStringBuffer.append("^" + str);

		}
		return localStringBuffer.toString();
	}
	/**
	 * 对称加密秘钥解密
	 * @param encodeKey
	 * @param rsaKey
	 * @return
	 */
	public static String getDecodeKeyByRsaKey(String encodeKey, String rsaKey) {
		return encodeKey;
	}
	/**
	 * 对称加密秘钥加密
	 * @param decodeKey
	 * @param rsaKey
	 * @return
	 */
	public static String getEncodeKeyByRsaKey(String decodeKey, String rsaKey) {
		return decodeKey;
	}
	/**
	 * 对参数进行解密
	 * @param <T>
	 * @param encodeParam
	 * @param decodeKey
	 * @return 
	 * @throws Exception
	 */
	public static <T> T getDecodeParamByDecodeKey(String encodeParam, String decodeKey,Class<T> t) throws Exception{
		String decodeParam=new String(Base64Utils.decodeFromString(encodeParam),StandardCharsets.UTF_8);
		T param =ObjectJsonHelper.deserialize(decodeParam,t);
		return param;
	}
	/**
	 * 对参数进行加密
	 * @param decodeParam
	 * @param encodeKey
	 * @return
	 * @throws Exception
	 */
	public static String getEncodeParamByEecodeKey(Object decodeParam, String encodeKey) throws Exception{
		String serializeResult = ObjectJsonHelper.serialize(decodeParam);
		return new String(Base64Utils.encode(serializeResult.getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8);
	}
	public static void main(String[] args) throws Exception {
		String salt=RandomStringUtils.randomAlphanumeric(32);
		System.out.println(salt);
		String password="111";
		String encodeAESBySalt = SecurityUtils.encodeAESBySalt(password, salt);
		System.out.println(encodeAESBySalt);
		String decodeAESBySalt = SecurityUtils.decodeAESBySalt(encodeAESBySalt, salt);
		System.out.println(decodeAESBySalt);
	}
}
