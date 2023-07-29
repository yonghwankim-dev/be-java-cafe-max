package kr.codesqaud.cafe.jwt;

import javax.servlet.http.Cookie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Jwt {
	private static final int REFRESH_TOKEN_MAXAGE = 60 * 60 * 24 * 30; // 1달

	private final String accessToken;
	private final String refreshToken;

	public Cookie createRefreshTokenCookie() {
		Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setMaxAge(REFRESH_TOKEN_MAXAGE); // 1달
		return refreshTokenCookie;
	}

	public String createAccessTokenHeaderValue() {
		return "Bearer " + accessToken;
	}
}
