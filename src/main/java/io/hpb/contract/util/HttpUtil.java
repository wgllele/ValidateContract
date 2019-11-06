package io.hpb.contract.util;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.hpb.contract.common.RestTemplateConfig;
import io.hpb.contract.common.SpringBootContext;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class HttpUtil {
	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
	private static RestTemplate restTemplate;

	public static RestTemplate getRestTemplate() {
		if (restTemplate == null) {
			if (SpringBootContext.getAplicationContext() != null) {
				RestTemplateConfig restTemplateConfig = SpringBootContext.getBean("restTemplateConfig",
						RestTemplateConfig.class);
				restTemplate = restTemplateConfig.restTemplate();
			} else {
				restTemplate = new RestTemplateConfig().restTemplate();
			}
		}
		return restTemplate;
	}

	public static List<String> getForObject(String url, List<String> param) {
		return getRestTemplate().getForObject(url, List.class, param);
	}

	public static List<String> getForEntity(String url, List<String> param) {
		ResponseEntity<List> responseEntity1 = getRestTemplate().getForEntity(url, List.class, param);
		HttpStatus statusCode = responseEntity1.getStatusCode();
		log.info("statusCode:{}", statusCode);
		HttpHeaders header = responseEntity1.getHeaders();
		log.info("header:{}", header);
		return responseEntity1.getBody();
	}

	public static List<String> exchange(String url, List<String> param) throws Exception {
		ResponseEntity<List> responseEntity1 = getRestTemplate().exchange(RequestEntity.get(new URI(url)).build(),
				List.class);
		HttpStatus statusCode = responseEntity1.getStatusCode();
		log.info("statusCode:{}", statusCode);
		HttpHeaders header = responseEntity1.getHeaders();
		log.info("header:{}", header);
		return responseEntity1.getBody();
	}

	public static List<String> postForObject(String url, List<String> param) throws Exception {
		return getRestTemplate().postForObject(url, param, List.class);
	}

	public static List<String> postForEntity(String url, List<String> param) throws Exception {
		ResponseEntity<List> responseEntity1 = getRestTemplate().postForEntity(url, param, List.class);
		return responseEntity1.getBody();
	}

	public static List<String> postExchange(String url, List<String> param) throws Exception {
		RequestEntity<List<String>> requestEntity = RequestEntity.post(new URI(url)).body(param);
		ResponseEntity<List> responseEntity1 = getRestTemplate().exchange(requestEntity, List.class);
		return responseEntity1.getBody();
	}

	public static void main(String args[]) throws Exception {
		String salt="zmqa9QHTeIjlSwHLbYxN5Unc4ajPEvyu";
	
		String decodeAESBySalt = SecurityUtils.decodeAESBySalt("d8b02508b4d150b551f4e6b70ab76c9d", salt);
		System.out.println(decodeAESBySalt);
		
	}

}