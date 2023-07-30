package kr.codesqaud.cafe.app.user.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.CafeTestUtil;
import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CafeTestUtil util;

	private MockHttpSession session;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void beforeEach() {
		session = new MockHttpSession();
		User user = User.builder()
			.userId("yonghwan1107")
			.password("yonghwan1107")
			.name("김용환")
			.email("yonghwan1107@naver.com")
			.build();
		util.signUp(user);
	}

	@AfterEach
	public void clean() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("올바른 유저아이디와 올바른 패스워드가 주어지고 로그일할때 세션 저장소에 회원정보가 저장되는지 테스트")
	public void login_success() throws Exception {
		//given
		String userId = "yonghwan1107";
		String password = "yonghwan1107";
		String url = "/login";
		UserLoginRequest dto = new UserLoginRequest(userId, password);

		//when
		mockMvc.perform(post(url)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(util.toJSON(dto)))
			.andExpect(redirectedUrl("/"));
		//then
		UserResponse user = (UserResponse)session.getAttribute("user");
		assertThat(user.getUserId()).isEqualTo(userId);
		assertThat(user.getName()).isEqualTo("김용환");
		assertThat(user.getEmail()).isEqualTo("yonghwan1107@naver.com");
	}

	@Test
	@DisplayName("올바른 유저아이디와 틀린 비밀번호가 주어지고 로그인할때 비밀번호 불일치로 에러 응답을 받는지 테스트")
	public void login_fail1() throws Exception {
		//given
		String userId = "yonghwan1107";
		String password = "useaweiofjaw";
		String url = "/login";
		UserLoginRequest dto = new UserLoginRequest(userId, password);
		//when
		String jsonError = mockMvc.perform(post(url)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(util.toJSON(dto)))
			.andExpect(status().isBadRequest())
			.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
		Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
		Assertions.assertThat(errorMap.get("name")).isEqualTo("NOT_MATCH_LOGIN");
		Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("아이디 또는 비밀번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("부적절한 입력 형식의 아이디와 패스워드가 주어지고 로그인할때 에러 응답을 받는지 테스트")
	public void login_fail2() throws Exception {
		//given
		String userId = "";
		String password = "";
		String url = "/login";
		UserLoginRequest dto = new UserLoginRequest(userId, password);
		//when
		String jsonError = mockMvc.perform(post(url)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(util.toJSON(dto)))
			.andExpect(status().isBadRequest())
			.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
		Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
		Assertions.assertThat(errorMap.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
		Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
	}

	@Test
	@DisplayName("서버에 없는 회원 아이디가 주어지고 로그인할때 에러 응답을 받는지 테스트")
	public void login_fail3() throws Exception {
		//given
		String userId = "user10";
		String password = "user10user10";
		String url = "/login";
		UserLoginRequest dto = new UserLoginRequest(userId, password);
		//when
		String jsonError = mockMvc.perform(post(url)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(util.toJSON(dto)))
			.andExpect(status().isBadRequest())
			.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
		Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
		Assertions.assertThat(errorMap.get("name")).isEqualTo("NOT_MATCH_LOGIN");
		Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("아이디 또는 비밀번호가 일치하지 않습니다.");
	}
}
