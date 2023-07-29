package kr.codesqaud.cafe.app.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.codesqaud.cafe.app.auth.controller.dto.TokenRefreshDto;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.errors.errorcode.LoginErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import kr.codesqaud.cafe.jwt.Jwt;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AuthController {

	private final UserService userService;

	@PostMapping("/auth/refresh/token")
	public Jwt tokenRefresh(@RequestBody TokenRefreshDto tokenRefreshDto) {
		Jwt jwt = userService.refreshToken(tokenRefreshDto.getRefreshToken());
		if (jwt == null) {
			throw new RestApiException(LoginErrorCode.UNAUTHORIZED);
		}
		return jwt;
	}
}
