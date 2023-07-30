package kr.codesqaud.cafe.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import kr.codesqaud.cafe.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtLoginInterceptor implements LoginInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(JwtLoginInterceptor.class);

	private final JwtProvider jwtProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {
		String accessToken = request.getHeader("Authorization").substring(7);
		logger.info("accessToken : {}", accessToken);
		Claims claims = jwtProvider.getClaims(accessToken);
		for (String key : claims.keySet()) {
			logger.info("{} : {}", key, claims.get(key));
		}
		return true;
	}
}
