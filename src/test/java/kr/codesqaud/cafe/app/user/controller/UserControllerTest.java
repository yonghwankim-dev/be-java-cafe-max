package kr.codesqaud.cafe.app.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import kr.codesqaud.cafe.CafeTestUtil;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.repository.UserRepository;
import kr.codesqaud.cafe.app.user.service.UserService;
import org.assertj.core.api.Assertions;
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

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CafeTestUtil util;

    private MockHttpSession session;

    private Long id;

    private User user;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
        user = User.builder()
            .userId("yonghwan1107")
            .password("yonghwan1107")
            .name("김용환")
            .email("yonghwan1107@naver.com")
            .build();
        id = util.signUp(user);
    }

    @Test
    @DisplayName("올바른 회원정보가 주어지고 회원가입 요청시 회원가입이 되는지 테스트")
    public void signup_success() throws Exception {
        //given
        String url = "/users";
        UserSavedRequest dto =
            new UserSavedRequest("kim1107", "kim1107kim1107@", "김용환", "kim1107@naver.com");
        //when
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto)))
            .andExpect(status().isOk());
        //then
        User user = userRepository.findByUserId("kim1107").orElseThrow();
        assertThat(user.getUserId()).isEqualTo("kim1107");
        assertThat(user.getPassword()).isEqualTo("kim1107kim1107@");
        assertThat(user.getName()).isEqualTo("김용환");
        assertThat(user.getEmail()).isEqualTo("kim1107@naver.com");
    }

    @Test
    @DisplayName("중복된 아이디가 주어지고 회원가입 요청시 에러 응답을 받는지 테스트")
    public void signup_fail1() throws Exception {
        //given
        String duplicateUserId = "yonghwan1107";
        String password = "yonghwan1107";
        String name = "김용환";
        String email = "yonghwan1107@naver.com";
        String url = "/users";
        UserSavedRequest dto = new UserSavedRequest(duplicateUserId, password, name, email);
        //when
        String jsonError = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto)))
            .andExpect(status().isConflict())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
        Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("CONFLICT");
        Assertions.assertThat(errorMap.get("name")).isEqualTo("ALREADY_EXIST_USERID");
        Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("중복된 이메일이 주어지고 회원가입 요청시 에러 응답을 받는지 테스트")
    @Transactional
    public void signup_fail2() throws Exception {
        //given
        String userId = "kimyonghwan1107";
        String password = "yonghwan1107";
        String name = "김용환";
        String duplicatedEmail = "yonghwan1107@naver.com";
        String url = "/users";
        UserSavedRequest dto = new UserSavedRequest(userId, password, name, duplicatedEmail);
        //when
        String jsonError = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto)))
            .andExpect(status().isConflict())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
        Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("CONFLICT");
        Assertions.assertThat(errorMap.get("name")).isEqualTo("ALREADY_EXIST_EMAIL");
        Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("이미 존재하는 이메일입니다.");
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
        //when
        String jsonError = mockMvc.perform(post(url)
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
    @DisplayName("특정 회원의 id가 주어지고 회원의 프로필이 검색되는지 테스트")
    public void profile() throws Exception {
        //given
        util.login(user.getUserId(), user.getPassword(), session);
        String url = "/users/" + id;
        //when
        UserResponse actual =
            (UserResponse) Objects.requireNonNull(mockMvc.perform(get(url)
                    .session(session))
                .andExpect(status().isOk())
                .andReturn().getModelAndView()).getModelMap().get("user");
        //then
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getUserId()).isEqualTo("yonghwan1107");
        assertThat(actual.getName()).isEqualTo("김용환");
        assertThat(actual.getEmail()).isEqualTo("yonghwan1107@naver.com");
    }

    @Test
    @DisplayName("비밀번호, 이름, 이메일이 주어지고 유저아이디가 주어질때 회원정보 수정이 되는지 테스트")
    public void modify_success() throws Exception {
        //given
        util.login(user.getUserId(), user.getPassword(), session);
        String userId = "yonghwan1107";
        String password = "yonghwan1107";
        String modifiedName = "홍길동";
        String modifiedEmail = "yonghwan1234@naver.com";
        String url = "/users/" + id;
        UserSavedRequest dto = new UserSavedRequest(userId, password, modifiedName, modifiedEmail);
        //when
        mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto))
                .session(session))
            .andExpect(status().isOk());
        //then
        User actual = userService.findUser(id).toEntity();
        assertThat(actual.getName()).isEqualTo(modifiedName);
        assertThat(actual.getEmail()).isEqualTo(modifiedEmail);
    }

    @Test
    @DisplayName("회원 수정 이메일 중복으로 인한 테스트")
    public void modify_fail() throws Exception {
        //given
        util.signUp(User.builder()
            .userId("kim1107")
            .password("kim1107")
            .name("김용환")
            .email("kim1107@naver.com")
            .build());
        util.login("yonghwan1107", "yonghwan1107", session);
        String userId = "yonghwan1107";
        String password = "yonghwan1107";
        String modifiedName = "김용환";
        String duplicatedEmail = "kim1107@naver.com";
        String url = "/users/" + id;
        UserSavedRequest dto =
            new UserSavedRequest(userId, password, modifiedName, duplicatedEmail);
        //when
        String jsonError = mockMvc.perform(put(url)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto)))
            .andExpect(status().isConflict())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
        Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("CONFLICT");
        Assertions.assertThat(errorMap.get("name")).isEqualTo("ALREADY_EXIST_EMAIL");
        Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 하지 않는 상태로 회원 정보 수정 페이지 접근할때 로그인 페이지로 리다이렉션 하는지 테스트")
    public void modify_fail2() throws Exception {
        //given
        String url = "/users/" + id;
        //when & then
        mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("다른 사람으로 로그인하였는데 다른 회원의 정보를 수정하려고 할때 에러 페이지로 리다이렉션 하고 에러 메시지를 받는지 테스트")
    public void modify_fail3() throws Exception {
        //given
        util.signUp(User.builder()
            .userId("kim1107")
            .password("kim1107")
            .name("김용환")
            .email("kim1107@naver.com")
            .build());
        util.login("kim1107", "kim1107", session);
        String userId = "yonghwan1107";
        String password = "yonghwan1107";
        String modifiedName = "홍길동";
        String modifiedEmail = "yonghwan1234@naver.com";
        String url = "/users/" + id;
        UserSavedRequest dto = new UserSavedRequest(userId, password, modifiedName, modifiedEmail);
        //when
        String jsonError = mockMvc.perform(put(url)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(util.toJSON(dto)))
            .andExpect(status().isForbidden())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> errorMap = objectMapper.readValue(jsonError, typeReference);
        assertThat(errorMap.get("name")).isEqualTo("PERMISSION_DENIED");
        assertThat(errorMap.get("httpStatus")).isEqualTo("FORBIDDEN");
        assertThat(errorMap.get("errorMessage")).isEqualTo("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("클라이언트가 존재하지 않는 회원 등록번호를 이용하여 특정 회원 조회 요청시 404 페이지로 이동합니다.")
    public void givenNotExistUserId_whenListUser_then404() throws Exception {
        //given
        util.login(user.getUserId(), user.getPassword(), session);
        long id = 9999L;
        String url = "/users/" + id;
        //when & then
        mockMvc.perform(get(url)
                .session(session))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error/404"));
    }

}
