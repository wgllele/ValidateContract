package io.hpb.contract.common;

/**
 * @author ThinkPad
 *	这里全部声明常量
 */
public interface ContractConstant {
	public static final String RETURN_CODE="RETURN_CODE";
	public static final String RETURN_MSG="RETURN_MSG";
	public static final String SUCCESS_CODE="000000";
	public static final String ERROR_CODE="999999";
	public static final String SUCCESS_MSG = "操作成功";
	public static final String NOSRCCODE = "请指定需要编译的solidity代码";
	public static final String PROCCESS_ID = "proccessId";
	public static final String SOLC_CMD_DEFAULT="docker run --rm -it --privileged=true --name solc ethereum/solc";
	public static final String SOLC_STABLE="stable";
}
