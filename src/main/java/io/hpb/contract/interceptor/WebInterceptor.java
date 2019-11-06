package io.hpb.contract.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class WebInterceptor implements HandlerInterceptor {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		logger.info("Call preHandle");
		Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
        }
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			if (response.getStatus() == 500) {
				modelAndView.setViewName("/error/error.html");
			} else if (response.getStatus() == 404) {
				modelAndView.setViewName("/error/error.html");
			}
		}
		logger.info("Call postHandle");
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		logger.info("Call afterCompletion");

	}

}
