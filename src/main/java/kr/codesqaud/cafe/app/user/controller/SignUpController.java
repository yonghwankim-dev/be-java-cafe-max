package kr.codesqaud.cafe.app.user.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class SignUpController {

	private final UserService userService;

	@PostMapping("/signUp")
	public UserResponse signUp(@Valid @RequestBody UserSavedRequest userRequest) {
		return userService.signUp(userRequest);
	}

}
