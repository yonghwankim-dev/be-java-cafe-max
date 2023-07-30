package kr.codesqaud.cafe.app.user.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Objects;

import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.entity.Role;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.entity.UserRole;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.errors.errorcode.CommonErrorCode;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import kr.codesqaud.cafe.errors.handler.GlobalExceptionHandler;
import kr.codesqaud.cafe.jwt.JwtProvider;

@WebMvcTest(value = {UserController.class, JwtProvider.class})
class UserControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	private MockHttpSession session;

	private UserSavedRequest userSavedRequest;

	private User user;

	@BeforeEach
	public void setup() {
		session = new MockHttpSession();
		mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
			.setControllerAdvice(GlobalExceptionHandler.class)
			.alwaysDo(print())
			.build();
		// 유저 요청 데이터 생성
		userSavedRequest = new UserSavedRequest("user1", "user1user1@", "김용환", "user1@naver.com");
		// 유저 생성
		user = User.builder()
			.id(1L)
			.userId("user1")
			.password("user1user1@")
			.name("김용환")
			.email("user1@naver.com")
			.build();
		user.addRole(new UserRole(user, Role.USER));
	}

	@AfterEach
	public void clean() {
		session.invalidate();
	}

	private String toJSON(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("올바른 회원정보가 주어지고 회원가입 요청시 회원가입이 되는지 테스트")
	public void signup_success() throws Exception {
		//given
		String url = "/users";
		UserResponse userResponse = new UserResponse(user);
		//mocking
		Mockito.when(userService.signUp(Mockito.any(UserSavedRequest.class))).thenReturn(userResponse);
		//when
		ResultActions resultActions = mockMvc.perform(post(url)
				.contentType(APPLICATION_JSON)
				.content(toJSON(userSavedRequest))
				.accept(APPLICATION_JSON))
			.andExpect(status().isOk());
		//then
		resultActions.andExpect(jsonPath("id").value(Matchers.equalTo(1)))
			.andExpect(jsonPath("userId").value(Matchers.equalTo("user1")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("김용환")))
			.andExpect(jsonPath("email").value(Matchers.equalTo("user1@naver.com")));

	}

	@Test
	@DisplayName("중복된 아이디가 주어지고 회원가입 요청시 에러 응답을 받는지 테스트")
	public void signup_fail1() throws Exception {
		// given
		String duplicateUserId = "user1";
		String password = "user1user1@";
		String name = "김용환";
		String email = "user1@naver.com";
		String url = "/users";
		UserSavedRequest dto = new UserSavedRequest(duplicateUserId, password, name, email);
		// mocking
		Mockito.when(userService.signUp(Mockito.any(UserSavedRequest.class)))
			.thenThrow(new RestApiException(UserErrorCode.ALREADY_EXIST_USERID));
		// when
		ResultActions resultActions = mockMvc.perform(post(url)
			.contentType(APPLICATION_JSON)
			.content(toJSON(dto)));
		// then
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("httpStatus").value(Matchers.equalTo("CONFLICT")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("ALREADY_EXIST_USERID")))
			.andExpect(jsonPath("errorMessage").value(Matchers.equalTo("이미 존재하는 아이디입니다.")));
	}

	@Test
	@DisplayName("중복된 이메일이 주어지고 회원가입 요청시 에러 응답을 받는지 테스트")
	public void signup_fail2() throws Exception {
		// given
		String duplicateUserId = "user1";
		String password = "user1user1@";
		String name = "김용환";
		String email = "user1@naver.com";
		String url = "/users";
		UserSavedRequest dto = new UserSavedRequest(duplicateUserId, password, name, email);
		// mocking
		Mockito.when(userService.signUp(Mockito.any(UserSavedRequest.class)))
			.thenThrow(new RestApiException(UserErrorCode.ALREADY_EXIST_EMAIL));
		// when
		ResultActions resultActions = mockMvc.perform(post(url)
			.contentType(APPLICATION_JSON)
			.content(toJSON(dto)));

		// then
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("httpStatus").value(Matchers.equalTo("CONFLICT")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("ALREADY_EXIST_EMAIL")))
			.andExpect(jsonPath("errorMessage").value(Matchers.equalTo("이미 존재하는 이메일입니다.")));
	}

	@Test
	@DisplayName("부적절한 입력 형식의 유저아이디, 패스워드, 이름, 이메일이 주어지고 회원가입 요청시 "
		+ "에러 응답 코드를 받는지 테스트")
	public void signup_fail3() throws Exception {
		//given
		String userId = "a";
		String password = "u";
		String name = "김용일!@#$";
		String email = "user1";
		UserSavedRequest dto = new UserSavedRequest(userId, password, name, email);
		String url = "/users";
		//mocking
		Mockito.when(userService.signUp(Mockito.any(UserSavedRequest.class)))
			.thenThrow(new RestApiException(CommonErrorCode.INVALID_INPUT_FORMAT));
		//when
		ResultActions resultActions = mockMvc.perform(post(url)
			.contentType(APPLICATION_JSON)
			.content(toJSON(dto)));
		//then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("httpStatus").value(Matchers.equalTo("BAD_REQUEST")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("INVALID_INPUT_FORMAT")))
			.andExpect(jsonPath("errorMessage").value(Matchers.equalTo("유효하지 않은 입력 형식입니다.")));
	}

	@Test
	@DisplayName("특정 회원의 id가 주어지고 회원의 프로필이 검색되는지 테스트")
	public void profile() throws Exception {
		// given
		Long id = 1L;
		String url = "/users/" + id;
		UserResponse userResponse = new UserResponse(user);
		// mocking
		Mockito.when(userService.findUser(Mockito.any(Long.class))).thenReturn(userResponse);
		// when
		UserResponse actual = (UserResponse)Objects.requireNonNull(mockMvc.perform(get(url)
				.session(session))
			.andReturn().getModelAndView()).getModel().get("user");
		// then
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(actual.getId()).isEqualTo(1);
			softAssertions.assertThat(actual.getUserId()).isEqualTo("user1");
			softAssertions.assertThat(actual.getName()).isEqualTo("김용환");
			softAssertions.assertThat(actual.getEmail()).isEqualTo("user1@naver.com");
		});

	}

	@Test
	@DisplayName("비밀번호, 이름, 이메일이 주어지고 유저아이디가 주어질때 회원정보 수정이 되는지 테스트")
	public void modify_success() throws Exception {
		//given
		Long id = 1L;
		String url = "/users/" + id;
		UserSavedRequest dto = new UserSavedRequest("user1", "user1user1@", "홍길동", "user2@naver.com");
		UserResponse userResponse = new UserResponse(user);
		UserResponse modifyUserResponse = new UserResponse(User.builder()
			.id(user.getId())
			.userId(user.getUserId())
			.name(dto.getName())
			.email(dto.getEmail())
			.build());
		session.setAttribute("user", userResponse);
		// mocking
		Mockito.when(userService.modifyUser(Mockito.any(Long.class), Mockito.any(UserSavedRequest.class)))
			.thenReturn(modifyUserResponse);
		//when
		ResultActions resultActions = mockMvc.perform(put(url)
			.contentType(APPLICATION_JSON)
			.content(toJSON(dto))
			.session(session));
		//then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(Matchers.equalTo(1)))
			.andExpect(jsonPath("userId").value(Matchers.equalTo("user1")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("홍길동")))
			.andExpect(jsonPath("email").value(Matchers.equalTo("user2@naver.com")));
	}

	@Test
	@DisplayName("회원 수정 이메일 중복으로 인한 테스트")
	public void modify_fail() throws Exception {
		// given
		String url = "/users/" + 1;
		UserSavedRequest dto = new UserSavedRequest("user1", "user1user1@", "홍길동", "user1@naver.com");
		UserResponse userResponse = new UserResponse(user);
		session.setAttribute("user", userResponse);
		// mocking
		Mockito.when(userService.modifyUser(Mockito.any(Long.class), Mockito.any(UserSavedRequest.class)))
			.thenThrow(new RestApiException(UserErrorCode.ALREADY_EXIST_EMAIL));
		// when
		ResultActions resultActions = mockMvc.perform(put(url)
			.session(session)
			.contentType(APPLICATION_JSON)
			.content(toJSON(dto)));
		// then
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("httpStatus").value(Matchers.equalTo("CONFLICT")))
			.andExpect(jsonPath("name").value(Matchers.equalTo("ALREADY_EXIST_EMAIL")))
			.andExpect(jsonPath("errorMessage").value(Matchers.equalTo("이미 존재하는 이메일입니다.")));
	}

	@Test
	@DisplayName("클라이언트가 존재하지 않는 회원 등록번호를 이용하여 특정 회원 조회 요청시 404 페이지로 이동합니다.")
	public void givenNotExistUserId_whenListUser_then404() throws Exception {
		// given
		long id = 9999L;
		String url = "/users/" + id;
		// mocking
		Mockito.when(userService.findUser(Mockito.any(Long.class)))
			.thenThrow(new RestApiException(UserErrorCode.NOT_FOUND_USER));
		// when & then
		mockMvc.perform(get(url)
				.session(session))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("name").value(Matchers.equalTo("NOT_FOUND_USER")))
			.andExpect(jsonPath("httpStatus").value(Matchers.equalTo("NOT_FOUND")))
			.andExpect(jsonPath("errorMessage").value(Matchers.equalTo("회원을 찾을 수 없습니다.")));

	}

}
