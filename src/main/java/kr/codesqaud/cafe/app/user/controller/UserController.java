package kr.codesqaud.cafe.app.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Api(tags = "사용자 API 정보를 제공하는 Controller")
@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "전체 회원을 조회하는 메소드")
    @GetMapping("/users")
    public ModelAndView listUser() {
        ModelAndView mav = new ModelAndView("user/list");
        mav.addObject("users", userService.getAllUsers());
        return mav;
    }

    @ApiOperation(value = "회원을 생성하는 메소드")
    @ApiImplicitParam(name = "userRequest", value = "생성할 회원의 정보를 담은 객체")
    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody UserSavedRequest userRequest) {
        return userService.signUp(userRequest);
    }

    @ApiOperation(value = "특정한 회원을 조회하는 메소드")
    @ApiImplicitParam(name = "id", value = "조회하고자 하는 회원 아이디")
    @GetMapping("/users/{id}")
    public ModelAndView detailUser(@PathVariable(value = "id") Long id) {
        ModelAndView mav = new ModelAndView("user/detail");
        mav.addObject("user", userService.findUser(id));
        return mav;
    }

    // TODO : 인증 부분 인터셉터로 빼기
    @ApiOperation(value = "회원 정보를 수정하는 메소드")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "수정하고자 하는 회원 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "userRequest", value = "수정된 회원 정보", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보가 담긴 세션", paramType = "query")
    })
    @PutMapping("/users/{id}")
    public UserResponse modifyUser(@PathVariable(value = "id") Long id,
        @Valid @RequestBody UserSavedRequest userRequest, HttpSession session) {
        // 현재 로그인한 사용자의 id와 url로 받은 id가 동일하지 않으면 예외 발생
        UserResponse loginUser = (UserResponse) session.getAttribute("user");
        if (!id.equals(loginUser.getId())) {
            throw new RestApiException(UserErrorCode.PERMISSION_DENIED);
        }
        UserResponse modifiedUser = userService.modifyUser(id, userRequest);
        // 회원정보 수정시 기존 세션에 저장되어 있는 유저 정보 갱신
        session.setAttribute("user", modifiedUser);
        return modifiedUser;
    }

    @ApiOperation(value = "회원 가입 페이지")
    @GetMapping("/users/new")
    public ModelAndView addUserForm() {
        return new ModelAndView("user/new");
    }

    @ApiOperation(value = "회원 수정 페이지")
    @ApiImplicitParam(name = "id", value = "수정하고자 하는 회원 등록번호")
    @GetMapping("/users/{id}/edit")
    public ModelAndView modifyUserForm(@PathVariable(value = "id") Long id) {
        ModelAndView mav = new ModelAndView("user/edit");
        mav.addObject("user", userService.findUser(id));
        return mav;
    }
}
