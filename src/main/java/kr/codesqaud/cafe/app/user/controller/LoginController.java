package kr.codesqaud.cafe.app.user.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.service.UserService;

@Controller
public class LoginController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private final UserService userService;

	public LoginController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String loginForm() {
		return "user/login";
	}

	@PostMapping("/login")
	public String login(@Valid @RequestBody UserLoginRequest requestDto,
		HttpSession session) {
		User user = userService.login(requestDto);
		session.setAttribute("user", new UserResponse(user));
		return "redirect:/";
	}

	@PostMapping("/logout")
	@ResponseBody
	public String logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
		return "/login";
	}
}
