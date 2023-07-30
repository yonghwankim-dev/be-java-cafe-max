package kr.codesqaud.cafe.app.question.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import kr.codesqaud.cafe.app.comment.controller.dto.CommentResponse;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentSavedRequest;
import kr.codesqaud.cafe.app.comment.repository.CommentRepository;
import kr.codesqaud.cafe.app.comment.service.CommentService;
import kr.codesqaud.cafe.app.common.pagination.Pagination;
import kr.codesqaud.cafe.app.question.controller.dto.QuestionResponse;
import kr.codesqaud.cafe.app.question.controller.dto.QuestionSavedRequest;
import kr.codesqaud.cafe.app.question.entity.Question;
import kr.codesqaud.cafe.app.question.repository.QuestionRepository;
import kr.codesqaud.cafe.app.question.service.QuestionService;
import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.repository.UserRepository;
import kr.codesqaud.cafe.app.user.service.UserService;
import kr.codesqaud.cafe.errors.response.ErrorResponse.ValidationError;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class QuestionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private MockHttpSession httpSession;

	private Long userId;

	private Long questionId;

	@BeforeEach
	public void setup() {
		clean();
		objectMapper.registerModule(new JavaTimeModule());
		httpSession = new MockHttpSession();
		userId = signUp("yonghwan1107", "yonghwan1107", "김용환", "yonghwan1107@gmail.com");

		for (int i = 1; i <= 100; i++) {
			questionId = writeQuestion("제목" + i, "내용 " + i, "yonghwan1107");
			for (int j = 1; j <= 45; j++) {
				writeComment(this.questionId, userId, "댓글" + j);
			}
		}
	}

	@AfterEach
	public void afterEach() {
		clean();
	}

	private void clean() {
		if (httpSession != null) {
			httpSession.invalidate();
		}
		userRepository.deleteAll();
		questionRepository.deleteAll();
		commentRepository.deleteAll();
	}

	public Long writeQuestion(String title, String content, String userId) {
		User user = userService.findUser(userId).toEntity();
		QuestionSavedRequest dto = new QuestionSavedRequest(title, content, user.getId());
		return questionService.writeQuestion(dto).getId();
	}

	public void writeComment(Long questionId, Long userId, String content) {
		commentService.answerComment(new CommentSavedRequest(content, questionId, userId));
	}

	@Test
	@DisplayName("회원 객체와 제목, 내용이 주어지고 글쓰기 요청시 글쓰기가 되는지 테스트")
	public void write_success() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		String userId = "yonghwan1107";
		User user = userService.findUser(userId).toEntity();
		String writer = user.getName();
		String title = "제목1";
		String content = "내용1";
		QuestionSavedRequest dto = new QuestionSavedRequest(title, content, user.getId());
		String url = "/qna";
		//when
		String jsonArticle = mockMvc.perform(
				post(url).contentType(MediaType.APPLICATION_JSON).content(toJSON(dto))
					.session(httpSession)).andExpect(status().isOk()).andReturn().getResponse()
			.getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> questionMap = objectMapper.readValue(jsonArticle, typeReference);
		assertThat(questionMap.get("writer")).isEqualTo(writer);
		assertThat(questionMap.get("title")).isEqualTo(title);
		assertThat(questionMap.get("content")).isEqualTo(content);
	}

	@Test
	@DisplayName("부적절한 입력 형식의 제목이 주어지고 글쓰기 요청시 에러 응답을 받는지 테스트")
	public void write_fail1() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		User user = userService.findUser(userId).toEntity();
		String title = "";
		String content = "내용1";
		QuestionSavedRequest dto = new QuestionSavedRequest(title, content, user.getId());
		String url = "/qna";
		//when
		String jsonErrors = mockMvc.perform(
				post(url).contentType(MediaType.APPLICATION_JSON).content(toJSON(dto))
					.session(httpSession)).andExpect(status().isBadRequest()).andReturn().getResponse()
			.getContentAsString(StandardCharsets.UTF_8);
		//then
		List<ValidationError> errors = new ArrayList<>();
		errors.add(new ValidationError("title", "제목은 100자 이내여야 합니다."));

		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> errorMap = objectMapper.readValue(jsonErrors, typeReference);
		Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
		Assertions.assertThat(errorMap.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
		Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
	}

	@Test
	@DisplayName("비 로그인 상태에서 글쓰기 페이지 진입시 로그인 페이지로 리다이렉트 되는지 테스트")
	public void form_fail() throws Exception {
		//given
		String url = "/qna/new";
		//when & then
		mockMvc.perform(get(url)).andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("로그인한 회원이 특정 질문 조회 페이지로 접근했을때 페이지로 리다이렉션 되는지 테스트")
	public void detail_success() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		String url = "/qna/" + questionId;
		//when
		ModelMap modelMap = Objects.requireNonNull(
			mockMvc.perform(get(url).session(httpSession)).andExpect(status().isOk()).andReturn()
				.getModelAndView()).getModelMap();
		//then
		QuestionResponse question = (QuestionResponse)modelMap.get("question");
		List<CommentResponse> comments = (List<CommentResponse>)modelMap.get("comments");
		assertThat(question.getTitle()).isEqualTo("제목100");
		assertThat(question.getContent()).isEqualTo("내용 100");
		assertThat(question.getWriter()).isEqualTo("김용환");
		assertThat(comments.size()).isEqualTo(15);
	}

	@Test
	@DisplayName("비 로그인 상태에서 특정 게시물을 접속하려고 할때 로그인 페이지로 이동되는지 테스트")
	public void detail_fail1() throws Exception {
		//given
		String url = "/qna/1";
		//when & then
		mockMvc.perform(get(url)).andExpect(redirectedUrl("/login"));
	}

	@Test
	@DisplayName("수정된 제목과 내용이 주어질때 질문 게시글 수정 요청시 수정이 되는지 테스트")
	public void edit_success() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		User user = userService.findUser(userId).toEntity();
		String modifiedTitle = "변경된 제목1";
		String modifiedContent = "변경된 내용1";
		QuestionSavedRequest dto = new QuestionSavedRequest(modifiedTitle, modifiedContent,
			user.getId());
		String url = "/qna/" + questionId;
		//when
		String json = mockMvc.perform(
				put(url).content(toJSON(dto)).contentType(MediaType.APPLICATION_JSON)
					.session(httpSession)).andExpect(status().isOk()).andReturn().getResponse()
			.getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> questionMap = objectMapper.readValue(json, typeReference);
		assertThat(questionMap.get("title")).isEqualTo("변경된 제목1");
		assertThat(questionMap.get("content")).isEqualTo("변경된 내용1");
	}

	@Test
	@DisplayName("부적절한 제목 입력 형식이 주어지고 질문 수정 요청시 에러 응답하는지 테스트")
	public void edit_fail1() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		Question question = write("제목1", "내용1");
		QuestionSavedRequest dto = new QuestionSavedRequest("", "변경된 내용1",
			question.getWriter().getId());
		String url = "/qna/" + question.getId();
		//when
		String json = mockMvc.perform(
				put(url).content(toJSON(dto)).contentType(MediaType.APPLICATION_JSON)
					.session(httpSession)).andExpect(status().isBadRequest()).andReturn().getResponse()
			.getContentAsString(StandardCharsets.UTF_8);
		//then
		TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
		};
		HashMap<String, Object> errorMap = objectMapper.readValue(json, typeReference);
		Assertions.assertThat(errorMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
		Assertions.assertThat(errorMap.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
		Assertions.assertThat(errorMap.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
	}

	@Test
	@DisplayName("본인의 질문 게시글을 삭제 요청시 삭제되고 삭제된 질문 데이터를 응답받는지 테스트")
	public void delete_success() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		Question question = write("제목1", "내용1");
		String url = "/qna/" + question.getId();
		//when & then
		mockMvc.perform(delete(url)
				.session(httpSession))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("삭제할 질문게시글 등록번호가 주어지고 삭제 요청시 게시글과 댓글들이 deleted 상태가 되는지 테스트")
	public void givenQuestionId_whenDeleteQuestion_thenModifyQuestionAndCommentsToDeletedStatus()
		throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		String url = "/qna/" + questionId;
		//when
		mockMvc.perform(delete(url)
				.session(httpSession))
			.andExpect(status().isOk());
		//then
		boolean emptyQuestion = questionRepository.findById(questionId).isEmpty();
		boolean emptyComments = commentRepository.findAll(questionId).isEmpty();
		Assertions.assertThat(emptyQuestion).isTrue();
		Assertions.assertThat(emptyComments).isTrue();
	}

	@Test
	@DisplayName("다른 사람으로 로그인 후 다른 사람의 질문 게시글을 삭제 요청할때 에러 응답을 받는지 테스트")
	public void delete_fail1() throws Exception {
		//given
		Question question = write("제목1", "내용1");
		signUp("kim1107", "kim1107kim1107", "kim", "kim1107@naver.com");
		login("kim1107", "kim1107kim1107");
		String url = "/qna/" + question.getId();
		//when
		mockMvc.perform(delete(url).session(httpSession)).andExpect(status().isForbidden());
		//then
	}

	@Test
	@DisplayName("클라이언트가 서버에 없는 게시물을 요청할때 404 페이지가 응답되는지 테스트")
	public void givenNotExistQuestionId_whenListQuestion_thenRedirection() throws Exception {
		//given
		login("yonghwan1107", "yonghwan1107");
		long id = 9999L;
		String url = "/qna/" + id;
		//when & then
		mockMvc.perform(get(url).session(httpSession)).andExpect(status().isNotFound())
			.andExpect(view().name("error/404"));
	}

	@Test
	@DisplayName("페이지 번호가 주어지고 전체 질문 게시글 조회 요청시 페이지 번호에 따른 게시물만 가져오는지 테스트")
	public void pagination() throws Exception {
		//given
		String page = "2";
		//when
		Object result = Objects.requireNonNull(mockMvc.perform(get("/")
				.param("page", page))
			.andExpect(status().isOk())
			.andReturn().getModelAndView().getModelMap().get("questions"));
		List<QuestionResponse> questions = (List<QuestionResponse>)result;
		//then
		Assertions.assertThat(questions.size()).isEqualTo(15);
	}

	@Test
	@DisplayName("최대 페이지보다 큰 페이지번호가 주어지고 질문 게시글 목록 요청시 최대 페이지로 이동하는지 테스트")
	public void pagination_givenPageNumberLargerThanTheMaximumPage_whenListQuestion_thenMoveMaximumPage()
		throws Exception {
		//given
		String page = "99999";
		//when
		Map<String, Object> modelMap = mockMvc.perform(get("/")
				.param("page", page))
			.andExpect(status().isOk())
			.andReturn().getModelAndView().getModelMap();
		List<QuestionResponse> questions = (List<QuestionResponse>)modelMap.get("questions");
		Pagination pagination = (Pagination)modelMap.get("pagination");
		//then
		Assertions.assertThat(questions.size()).isEqualTo(10);
		Assertions.assertThat(pagination.getCurrentPage()).isEqualTo(7);
	}

	@ParameterizedTest
	@DisplayName("1페이지보다 작은수가 주어질때 질문 게시글 목록 요청시 첫번째 페이지로 이동하는지 테스트")
	@ValueSource(strings = {"0", "-1", "aweofijawoeifj"})
	public void pagination_givenPageNumberLessThanThe1Page_whenListQuestion_thenMove1Page(
		String page)
		throws Exception {
		//given

		//when
		Map<String, Object> modelMap = mockMvc.perform(get("/")
				.param("page", page))
			.andExpect(status().isOk())
			.andReturn().getModelAndView().getModelMap();
		List<QuestionResponse> questions = (List<QuestionResponse>)modelMap.get("questions");
		Pagination pagination = (Pagination)modelMap.get("pagination");
		//then
		Assertions.assertThat(questions.size()).isEqualTo(15);
		Assertions.assertThat(pagination.getCurrentPage()).isEqualTo(1);
	}

	private Long signUp(String userId, String password, String name, String email) {
		return userService.signUp(new UserSavedRequest(userId, password, name, email)).getId();
	}

	private void login(String userId, String password) throws Exception {
		mockMvc.perform(post("/login").content(toJSON(new UserLoginRequest(userId, password)))
			.contentType(MediaType.APPLICATION_JSON).session(httpSession));
	}

	private Question write(String title, String content) {
		Question question = Question.builder()
			.title(title)
			.content(content)
			.deleted(false)
			.writer(User.builder().id(userId).build())
			.build();
		return questionRepository.save(question);
	}

	private <T> String toJSON(T data) throws JsonProcessingException {
		return new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(data);
	}
}
