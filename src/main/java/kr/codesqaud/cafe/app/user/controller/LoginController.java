package kr.codesqaud.cafe.app.user.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(tags = "로그인 서비스를 제공하는 Controller")
@Controller
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "로그인 페이지")
    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    @ApiOperation(value = "로그인")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "requestDto", value = "로그인 정보", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보 저장 세션", paramType = "query")
    })
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginRequest requestDto,
        HttpSession session) {
        User user = userService.login(requestDto);
        session.setAttribute("user", new UserResponse(user));
        return "redirect:/";
    }

    @ApiOperation(value = "로그아웃")
    @ApiImplicitParam(name = "session", value = "로그인 세션", paramType = "query")
    @PostMapping("/logout")
    @ResponseBody
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "/login";
    }
}
