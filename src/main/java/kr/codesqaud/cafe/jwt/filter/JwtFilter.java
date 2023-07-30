package kr.codesqaud.cafe.jwt.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.entity.AuthenticateUser;
import kr.codesqaud.cafe.app.user.filter.VerifyUserFilter;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.jwt.Jwt;
import kr.codesqaud.cafe.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;
	private final UserService userService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException {
		Object attribute = request.getAttribute(VerifyUserFilter.AUTHENTICATE_USER);
		if (attribute instanceof AuthenticateUser) {
			Map<String, Object> claims = new HashMap<>();
			AuthenticateUser authenticateUser = (AuthenticateUser)attribute;
			String authenticateUserJson = objectMapper.writeValueAsString(authenticateUser);
			claims.put(VerifyUserFilter.AUTHENTICATE_USER, authenticateUserJson);
			Jwt jwt = jwtProvider.createJwt(claims);
			userService.updateRefreshToken(authenticateUser.getUserId(), jwt.getRefreshToken());
			logger.info("jwt : {}", jwt);

			HttpServletResponse httpServletResponse = (HttpServletResponse)response;
			httpServletResponse.addHeader("Authorization", jwt.createAccessTokenHeaderValue());
			httpServletResponse.addCookie(jwt.createRefreshTokenCookie());

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(authenticateUserJson);
			return;
		}
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		httpServletResponse.sendError(HttpStatus.BAD_REQUEST.value());
	}
}
