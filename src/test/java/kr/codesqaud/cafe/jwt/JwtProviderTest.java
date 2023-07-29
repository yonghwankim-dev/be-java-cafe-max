package kr.codesqaud.cafe.jwt;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtProviderTest {

	private JwtProvider jwtProvider = new JwtProvider();

	@Test
	@DisplayName("claims를 암호화합니다.")
	public void testCreateToken() {
		// given
		Map<String, Object> claims = new HashMap<>();
		claims.put("userid", "user1");
		claims.put("password", "user1user1@");
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		Date expireDate = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
		// when
		String token = jwtProvider.createToken(claims, expireDate);
		// then
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(token)
				.isEqualTo(
					"eyJhbGciOiJIUzM4NCJ9.eyJwYXNzd29yZCI6InVzZXIxdXNlcjFAIiwidXNlcmlkIjoidXNlcjEiLCJleHAiOjE2OTAxMjQ0MDB9.syQpTlhLQ2zwSaiBXdd4D5i9PdVga_tAmxW2Bak3cDoARJF_0Vm71Z4ow4hHsA7k");
			softAssertions.assertAll();
		});
	}

	@Test
	@DisplayName("암호화된 토큰을 파싱합니다.")
	public void testGetClaims() {
		// given
		Map<String, Object> claimsMap = new HashMap<>();
		claimsMap.put("userid", "user1");
		claimsMap.put("password", "user1user1@");
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		Date expireDate = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
		String token = jwtProvider.createToken(claimsMap, expireDate);
		// when
		Claims claims = jwtProvider.getClaims(token);
		// then
		String userid = claims.get("userid", String.class);
		String password = claims.get("password", String.class);
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(userid).isEqualTo("user1");
			softAssertions.assertThat(password).isEqualTo("user1user1@");
		});
	}

	@Test
	@DisplayName("accessToken과 refreshToken을 가진 Jwt 객체를 생성합니다.")
	public void testCreateJwt() {
		// given
		Map<String, Object> claimsMap = new HashMap<>();
		claimsMap.put("userid", "user1");
		claimsMap.put("password", "user1user1@");
		// when
		Jwt jwt = jwtProvider.createJwt(claimsMap);
		// then
		System.out.println(jwt.getAccessToken());
		System.out.println(jwt.getRefreshToken());
	}
}
