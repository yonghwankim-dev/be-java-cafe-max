package kr.codesqaud.cafe.app.user.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserVerifyResponseDto;
import kr.codesqaud.cafe.app.user.entity.AuthenticateUser;
import kr.codesqaud.cafe.app.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class VerifyUserFilter implements Filter {
	public static final String AUTHENTICATE_USER = "authenticateUser";
	private static final Logger logger = LoggerFactory.getLogger(VerifyUserFilter.class);
	private final ObjectMapper objectMapper;

	private final UserService userService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		if ((httpServletRequest.getMethod().equals("POST"))) {
			try {
				UserLoginRequest userLoginRequest = objectMapper.readValue(request.getReader(), UserLoginRequest.class);
				UserVerifyResponseDto verifyResponse = userService.verifyUser(userLoginRequest);
				if (verifyResponse.isValid()) {
					logger.info("authenticateuser : {}", userLoginRequest.getUserId());
					request.setAttribute(AUTHENTICATE_USER,
						new AuthenticateUser(userLoginRequest.getUserId(), verifyResponse.getUserRole()));
				} else {
					throw new IllegalArgumentException();
				}
				chain.doFilter(request, response);
			} catch (Exception e) {
				logger.error("Fail User Verify");
				HttpServletResponse httpServletResponse = (HttpServletResponse)response;
				httpServletResponse.sendError(HttpStatus.BAD_REQUEST.value());
			}
		} else if (httpServletRequest.getMethod().equals("GET")) {
			request.getRequestDispatcher("/login").forward(request, response);
		}
	}
}
