package kr.codesqaud.cafe.jwt.filter;

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
import org.springframework.util.PatternMatchUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import kr.codesqaud.cafe.app.user.entity.AuthenticateUser;
import kr.codesqaud.cafe.app.user.entity.Role;
import kr.codesqaud.cafe.app.user.filter.VerifyUserFilter;
import kr.codesqaud.cafe.errors.errorcode.LoginErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import kr.codesqaud.cafe.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthorizationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

	private final String[] whiteListUris = {"/", "/css/*", "/js/*", "/fonts/*", "/login", "/auth/refresh/token",
		"/users/new",
		"/signUp"};

	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;

		if (whiteListCheck(httpServletRequest.getRequestURI())) {
			chain.doFilter(request, response);
			return;
		}
		if (!isContainToken(httpServletRequest)) {
			httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "인증 오류");
			return;
		}
		try {
			String token = getToken(httpServletRequest);
			AuthenticateUser authenticateUser = getAuthenticateUser(token);
			verifyAuthorization(httpServletRequest.getRequestURI(), authenticateUser);
			logger.info("값 : {}", authenticateUser.getUserId());
			httpServletRequest.setAttribute("user", authenticateUser);
			chain.doFilter(request, response);
		} catch (JsonParseException e) {
			logger.error("JsonParseException");
			httpServletResponse.sendError(HttpStatus.BAD_REQUEST.value());
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
			logger.error("JwtException");
			httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "인증 오류");
		} catch (ExpiredJwtException e) {
			logger.error("JwtTokenExpired");
			httpServletResponse.sendError(HttpStatus.FORBIDDEN.value(), "토큰이 만료 되었습니다");
		} catch (RestApiException e) {
			logger.error("AuthorizationException");
			httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다");
		}
	}

	private boolean isContainToken(HttpServletRequest httpServletRequest) {
		String authorization = httpServletRequest.getHeader("Authorization");
		return authorization != null && authorization.startsWith("Bearer ");
	}

	private boolean whiteListCheck(String requestURI) {
		return PatternMatchUtils.simpleMatch(whiteListUris, requestURI);
	}

	private String getToken(HttpServletRequest httpServletRequest) {
		String authorization = httpServletRequest.getHeader("Authorization");
		return authorization.substring(7);
	}

	private AuthenticateUser getAuthenticateUser(String token) throws JsonProcessingException {
		Claims claims = jwtProvider.getClaims(token);
		String authenticateUserJson = claims.get(VerifyUserFilter.AUTHENTICATE_USER, String.class);
		return objectMapper.readValue(authenticateUserJson, AuthenticateUser.class);
	}

	private void verifyAuthorization(String requestURI, AuthenticateUser authenticateUser) {
		if (PatternMatchUtils.simpleMatch("*/admin*", requestURI) &&
			!authenticateUser.getRoles().contains(Role.ADMIN)) {
			throw new RestApiException(LoginErrorCode.UNAUTHORIZED);
		}
	}
}
