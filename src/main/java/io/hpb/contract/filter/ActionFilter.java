package io.hpb.contract.filter;

import java.io.IOException;
import java.time.LocalTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionFilter implements Filter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// get SysTime
		int hour = LocalTime.now().getHour();
		logger.info(LocalTime.now().toString());
		
		// Setting Limits on Run Time 0-4点
		if (hour < 4) {
			/*HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setCharacterEncoding("UTF-8");
			httpResponse.setContentType("application/json; charset=utf-8");
			
			Map<String, Object> messageMap = new HashMap<>();
			messageMap.put("status", "1");
			messageMap.put("message", "This interface can request a time of :0-4点");
			ObjectMapper objectMapper = new ObjectMapper();
			String writeValueAsString = objectMapper.writeValueAsString(messageMap);
			response.getWriter().write(writeValueAsString);*/
			chain.doFilter(request, response);
		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
		logger.info("Call destroy");
	}
}
