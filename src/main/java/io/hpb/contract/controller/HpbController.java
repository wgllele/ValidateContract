package io.hpb.contract.controller;

import static io.hpb.contract.solidity.compiler.SolidityCompiler.Options.ABI;
import static io.hpb.contract.solidity.compiler.SolidityCompiler.Options.BIN;
import static io.hpb.contract.solidity.compiler.SolidityCompiler.Options.INTERFACE;
import static io.hpb.contract.solidity.compiler.SolidityCompiler.Options.METADATA;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hpb.contract.solidity.compiler.CompilationResult;
import io.hpb.contract.solidity.compiler.SolidityCompiler;
import io.hpb.contract.util.ObjectJsonHelper;
import io.hpb.web3.abi.FunctionReturnDecoder;
import io.hpb.web3.abi.TypeReference;
import io.hpb.web3.abi.Utils;
import io.hpb.web3.abi.datatypes.Type;
import io.hpb.web3.protocol.admin.Admin;
import io.hpb.web3.protocol.core.DefaultBlockParameterName;
import io.hpb.web3.protocol.core.methods.response.AbiDefinition;
import io.hpb.web3.protocol.core.methods.response.HpbGetCode;
import io.hpb.web3.protocol.core.methods.response.HpbGetTransactionReceipt;
import io.hpb.web3.protocol.core.methods.response.TransactionReceipt;
import io.hpb.web3.spring.autoconfigure.Web3Properties;
import io.hpb.web3.utils.Numeric;
//import io.hpb.web3.protocol.admin.Admin;
import io.swagger.annotations.ApiOperation;
@RestController
@RequestMapping("/")
public class HpbController{
	private static final String _OPTIMIZE = "200";
	private static final String _META = "a165627a7a72305820";
	private static final String _META1 ="a265627a7a72315820";
	private static final String _META2 ="a265627a7a72305820";
	public Logger log = LoggerFactory.getLogger(HpbController.class);
	@Autowired
	private Admin admin;
	@Autowired
    private Web3Properties web3Properties;
	@Autowired
	private SolidityCompiler solidityCompiler;
	private final String cpath="/opt/contract/";
	@ApiOperation(value="合约编译",notes = "合约编译"
			+ " reqStrList [  Parameter1：合约源代码,"
			+ "Parameter2：合约名称],Parameter3：合约版本号，"
			+ "Parameter4：合约是否优化,200代表优化]")
	@PostMapping("/compileContractCmd")
	public List<Object> compileContractCmd(@RequestBody List<String> reqStrList) throws Exception {
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String contract =java.net.URLDecoder.decode(
					reqStrList.get(0),StandardCharsets.UTF_8.name());
			String contractName = reqStrList.get(1);
			String name=new HmacUtils(HmacAlgorithms.HMAC_MD5, contractName).hmacHex(contract);
			File cfile = new File(cpath+name+".sol");
			FileUtils.writeStringToFile(cfile, contract, StandardCharsets.UTF_8,false);
			String solcVersion = reqStrList.get(2);
			if(web3Properties.getSolcCmd()!=null) {
				solidityCompiler.setDockerSolcCmd(web3Properties.getSolcCmd());
				solidityCompiler.setSolcVersion(solcVersion);
			}
			String optimize = reqStrList.get(3);
			SolidityCompiler.Result res = solidityCompiler.compileSrc(
					cfile,_OPTIMIZE.equals(optimize.trim()), true, ABI, BIN, INTERFACE, METADATA);
			if(res.isFailed()) {
				list.add(res.errors);
				log.info("Err: '{}'", res.errors);
			}else {
				list.add(res.output);
				//log.info("Out: '{}'" ,res.output);
			}
	        try {
				FileUtils.deleteQuietly(cfile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	@SuppressWarnings("rawtypes")
	@ApiOperation(value="合约验证",notes = "合约验证方法"
			+ " reqStrList [  Parameter1：合约源代码,"
			+ "Parameter2：合约名称],Parameter3：合约版本号，"
			+ "Parameter4：合约是否优化,200代表优化，Parameter5：合约发布交易HASH]")
	@PostMapping("/validateContractCmd")
	public List<Object> validateContractCmd(
			@RequestBody List<String> reqStrList) throws Exception {
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String contract =java.net.URLDecoder.decode(
					reqStrList.get(0),StandardCharsets.UTF_8.name());
			String contractName = reqStrList.get(1);
			String name=new HmacUtils(HmacAlgorithms.HMAC_MD5, contractName).hmacHex(contract);
			File cfile = new File(cpath+name+".sol");
			FileUtils.writeStringToFile(cfile, contract, StandardCharsets.UTF_8,false);
			String solcVersion = reqStrList.get(2);
			if(web3Properties.getSolcCmd()!=null) {
				solidityCompiler.setDockerSolcCmd(web3Properties.getSolcCmd());
				solidityCompiler.setSolcVersion(solcVersion);
			}
			String optimize = reqStrList.get(3);
			SolidityCompiler.Result res = solidityCompiler.compileSrc(
					cfile,_OPTIMIZE.equals(optimize.trim()), true, ABI, BIN, INTERFACE, METADATA);
			if(res.isFailed()) {
				list.add(res.errors);
				log.info("Err: '{}'", res.errors);
			}else {
				//log.info("Out: '{}'" ,res.output);
				CompilationResult result = CompilationResult.parse(res.output);
				CompilationResult.ContractMetadata metadata = result.getContract(contractName);
				String transactionHash = reqStrList.get(4);
				String bin = metadata.bin;
				HpbGetTransactionReceipt transactionReceipt = admin.hpbGetTransactionReceipt(transactionHash).send();
				TransactionReceipt receipt = transactionReceipt.getResult();
				if(receipt.isStatusOK()) {
					String contractAddress = receipt.getContractAddress();
					HpbGetCode hpbGetCode = admin.hpbGetCode(contractAddress,
							DefaultBlockParameterName.LATEST).send();
					String code = Numeric.cleanHexPrefix(hpbGetCode.getCode());
					String[] split1 = new String[] {"",""};
					String[] split2 = new String[] {"",""};
					if(code.contains(_META)) {
						split1 = code.split(_META);
						split2 = bin.split(_META);
					}else if(code.contains(_META1)) {
						split1 = code.split(_META1);
						split2 = bin.split(_META1);
					}else if(code.contains(_META2)) {
						split1 = code.split(_META2);
						split2 = bin.split(_META2);
					}
					String binCode = split2[0];
					String dataCode = split1[0];
					if(binCode.endsWith(dataCode)){
						list.add(res.output);
						list.add("合约验证成功");
						try {
							AbiDefinition[] abiDefinition = ObjectJsonHelper.deserialize(metadata.abi, AbiDefinition[].class);
							for(AbiDefinition abi:abiDefinition) {
								if(abi.getType().equals("constructor")) {
									List<AbiDefinition.NamedType> namedTypes=abi.getInputs();
									if(namedTypes!=null&&namedTypes.size()>0) {
										List<TypeReference<?>> inputParameterTypes =Utils.buildTypeReferenceTypes(namedTypes);
										List<TypeReference<Type>> convert = Utils.convert(inputParameterTypes);
										List<Type> decode = FunctionReturnDecoder.decode(
												Numeric.cleanHexPrefix(split2[1].replace(split1[1], "")), convert);
										list.add(decode);
									}
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else {
						list.add("合约不匹配");
						Map<String,String> map=new HashMap<String,String>();
						if(dataCode.length()<binCode.length()) {
							map.put("源码编译的bin", binCode.substring(
									binCode.length()-dataCode.length(),
									binCode.length()));
							map.put("链上合约的bin", dataCode);
						}else {
							map.put("源码编译的bin", binCode);
							map.put("链上合约的bin", dataCode);
						}
						list.add(map);
					}
				}else {
					list.add("合约不存在");
				}
			
			}
			try {
				FileUtils.deleteQuietly(cfile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	@ApiOperation(value="合约测试",notes = "合约测试")
	@PostMapping("/runGetVersionOutput")
	public List<Object> runGetVersionOutput(
			@RequestBody List<String> reqStrList,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		List<Object> list=new ArrayList<Object>();
		if(reqStrList!=null&&reqStrList.size()>0) {
			String solcVersion = reqStrList.get(0);
			if(web3Properties.getSolcCmd()!=null) {
				solidityCompiler.setDockerSolcCmd(web3Properties.getSolcCmd());
				solidityCompiler.setSolcVersion(solcVersion);
			}
			String runGetVersionOutput = solidityCompiler.runGetVersionOutput();
			log.info(runGetVersionOutput);
			list.add(runGetVersionOutput);
		}
		return list;
	}
}