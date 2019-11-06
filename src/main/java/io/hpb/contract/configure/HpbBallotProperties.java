package io.hpb.contract.configure;

import static io.hpb.contract.configure.HpbBallotProperties.BALLOT_PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * web3 property container.
 */
@Component
@ConfigurationProperties(prefix = BALLOT_PREFIX)
public class HpbBallotProperties {

    public static final String BALLOT_PREFIX = "ballot";
    private String password =null;
	private String keyPath =null;
	private String contractProxyAddress = null;
	private String ballotIndex = null;
	private String jobCron = null;
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getKeyPath() {
		return keyPath;
	}
	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}
	public String getContractProxyAddress() {
		return contractProxyAddress;
	}
	public void setContractProxyAddress(String contractProxyAddress) {
		this.contractProxyAddress = contractProxyAddress;
	}
	public String getBallotIndex() {
		return ballotIndex;
	}
	public void setBallotIndex(String ballotIndex) {
		this.ballotIndex = ballotIndex;
	}
	public String getJobCron() {
		return jobCron;
	}
	public void setJobCron(String jobCron) {
		this.jobCron = jobCron;
	}
	
}
