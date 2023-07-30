package kr.codesqaud.cafe.jwt.config;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.filter.VerifyUserFilter;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.jwt.JwtProvider;
import kr.codesqaud.cafe.jwt.filter.JwtAuthorizationFilter;
import kr.codesqaud.cafe.jwt.filter.JwtFilter;

@Configuration
public class JwtConfig {
	@Bean
	public FilterRegistrationBean verifyUserFilter(ObjectMapper objectMapper, UserService userService) {
		FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(new VerifyUserFilter(objectMapper, userService));
		filterFilterRegistrationBean.setOrder(1);
		filterFilterRegistrationBean.addUrlPatterns("/jwt/login");
		return filterFilterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean jwtFilter(JwtProvider provider, ObjectMapper objectMapper, UserService userService) {
		FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(new JwtFilter(provider, objectMapper, userService));
		filterFilterRegistrationBean.setOrder(2);
		filterFilterRegistrationBean.addUrlPatterns("/jwt/login");
		return filterFilterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean jwtAuthorizationFilter(JwtProvider provider, ObjectMapper objectMapper) {
		FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(new JwtAuthorizationFilter(provider, objectMapper));
		filterFilterRegistrationBean.addUrlPatterns("/jwt/auth/*");
		return filterFilterRegistrationBean;
	}
}
