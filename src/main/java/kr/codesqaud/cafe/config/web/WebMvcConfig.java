package kr.codesqaud.cafe.config.web;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.filter.VerifyUserFilter;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.interceptor.LoginInterceptor;
import kr.codesqaud.cafe.jwt.JwtFilter;
import kr.codesqaud.cafe.jwt.JwtProvider;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor())
			.order(Ordered.HIGHEST_PRECEDENCE)
			.addPathPatterns("/qna/**", "/users/**")
			.excludePathPatterns("/users", "/users/new");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**")
			.addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/fonts/**")
			.addResourceLocations("classpath:/static/fonts/");
		registry.addResourceHandler("/images/**")
			.addResourceLocations("classpath:/static/images/");
		registry.addResourceHandler("/js/**")
			.addResourceLocations("classpath:/static/js/");
	}

	@Bean
	public FilterRegistrationBean verifyUserFilter(ObjectMapper objectMapper, UserService userService) {
		FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(new VerifyUserFilter(objectMapper, userService));
		filterFilterRegistrationBean.setOrder(1);
		filterFilterRegistrationBean.addUrlPatterns("/login");
		return filterFilterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean jwtFilter(JwtProvider provider, ObjectMapper objectMapper, UserService userService) {
		FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
		filterFilterRegistrationBean.setFilter(new JwtFilter(provider, objectMapper, userService));
		filterFilterRegistrationBean.setOrder(2);
		filterFilterRegistrationBean.addUrlPatterns("/login");
		return filterFilterRegistrationBean;
	}
}
