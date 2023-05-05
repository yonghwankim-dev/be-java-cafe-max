package kr.codesqaud.cafe.app.comment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import kr.codesqaud.cafe.CafeTestUtil;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentResponse;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentSavedRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.entity.User;
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

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private CafeTestUtil util;

    private MockHttpSession session;

    private Long questionId;

    private Long userId;

    private Long commentId;

    private User otherUser;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
        User loginUser = User.builder()
            .userId("yonghwan1107")
            .password("yonghwan1107")
            .name("김용환")
            .email("yonghwan1107@naver.com")
            .build();
        otherUser = User.builder()
            .userId("kim1107")
            .password("yonghwan1107")
            .name("김용환")
            .email("yonghwan1107@naver.com")
            .build();

        // 회원생성
        this.userId = util.signUp(loginUser);
        util.signUp(otherUser);
        // 로그인
        util.login("yonghwan1107", "yonghwan1107", session);
        // 게시글 생성
        this.questionId = util.writeQuestion("제목1", "댓글1", "yonghwan1107");
        // 댓글 생성
        for (int i = 1; i <= 45; i++) {
            this.commentId = util.writeComment(questionId, userId, "댓글" + i);
        }
    }

    @Test
    @DisplayName("댓글 내용이 주어졌을때 댓글 작성 요청시 댓글이 달아지고 해당 질문 게시글로 이동되는지 테스트")
    public void createComment_success() throws Exception {
        //given
        CommentSavedRequest dto = new CommentSavedRequest("댓글1", questionId, userId);
        String url = String.format("/qna/%d/comments", questionId);

        //when
        String json = mockMvc.perform(post(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> map = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(map.get("content")).isEqualTo("댓글1");
    }

    @Test
    @DisplayName("부적절한 댓글 내용 입력이 주어지고 댓글 작성 요청시 에러 응답하는지 테스트")
    public void createComment_fail1() throws Exception {
        //given
        String content = "";
        CommentSavedRequest dto = new CommentSavedRequest(content, questionId, userId);
        String url = String.format("/qna/%d/comments", questionId);
        //when
        String json = mockMvc.perform(post(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> map = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(map.get("httpStatus")).isEqualTo("BAD_REQUEST");
        Assertions.assertThat(map.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
        Assertions.assertThat(map.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
    }

    @Test
    @DisplayName("3000글자가 넘는 댓글 내용 입력이 주어지고 댓글 작성 요청시 에러 응답하는지 테스트")
    public void createComment_fail2() throws Exception {
        //given
        String content = "a".repeat(3001);
        CommentSavedRequest dto = new CommentSavedRequest(content, questionId, userId);
        String url = String.format("/qna/%d/comments", questionId);
        //when
        String json = mockMvc.perform(post(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> map = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(map.get("httpStatus")).isEqualTo("BAD_REQUEST");
        Assertions.assertThat(map.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
        Assertions.assertThat(map.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
    }

    @Test
    @DisplayName("질문 게시글 등록번호가 주어지고 게시물에 대한 댓글들을 요청시 댓글들이 응답되는지 테스트")
    public void listComment_success() throws Exception {
        //given
        String url = String.format("/qna/%d/comments", questionId);
        //when
        String json = mockMvc.perform(get(url)
                .session(session))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        System.out.println("json " + json);
        HashMap<String, Object> map = objectMapper.readValue(json, typeReference);
        List<CommentResponse> comments = (List<CommentResponse>) map.get("comments");
        Assertions.assertThat(comments.size()).isEqualTo(15);
    }

    @Test
    @DisplayName("수정된 댓글 정보가 주어지고 댓글 수정 요청시 댓글이 수정되는지 테스트")
    public void modifyComment_success() throws Exception {
        //given
        String url = String.format("/qna/%d/comments/%d", questionId, commentId);
        CommentSavedRequest dto = new CommentSavedRequest("수정된 댓글1", questionId, userId);
        //when
        String json = mockMvc.perform(put(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> commentMap = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(commentMap.get("content")).isEqualTo("수정된 댓글1");
    }

    @Test
    @DisplayName("부적절한 입력 형식의 댓글 정보가 주어지고 댓글 수정 요청시 댓글이 수정되는지 테스트")
    public void modifyComment_fail1() throws Exception {
        //given
        String url = String.format("/qna/%d/comments/%d", questionId, commentId);
        CommentSavedRequest dto = new CommentSavedRequest("", questionId, userId);
        //when
        String json = mockMvc.perform(put(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> commentMap = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(commentMap.get("httpStatus")).isEqualTo("BAD_REQUEST");
        Assertions.assertThat(commentMap.get("name")).isEqualTo("INVALID_INPUT_FORMAT");
        Assertions.assertThat(commentMap.get("errorMessage")).isEqualTo("유효하지 않은 입력 형식입니다.");
    }

    @Test
    @DisplayName("권한없는 유저가 댓글 수정을 요청시 에러 응답을 받는지 테스트")
    public void modifyComment_fail2() throws Exception {
        //given
        String url = String.format("/qna/%d/comments/%d", questionId, commentId);
        CommentSavedRequest dto = new CommentSavedRequest("수정된 댓글1", questionId, userId);
        session.setAttribute("user", new UserResponse(otherUser));
        //when
        String json = mockMvc.perform(put(url)
                .content(util.toJSON(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session))
            .andExpect(status().isForbidden())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> commentMap = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(commentMap.get("httpStatus")).isEqualTo("FORBIDDEN");
        Assertions.assertThat(commentMap.get("name")).isEqualTo("PERMISSION_DENIED");
        Assertions.assertThat(commentMap.get("errorMessage")).isEqualTo("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("삭제할 게시글의 댓글 등록번호가 주어지고 댓글 삭제 요청시 댓글이 삭제되는지 테스트")
    public void deleteComment_success() throws Exception {
        //given
        String url = String.format("/qna/%d/comments/%d", questionId, commentId);
        //when
        String json = mockMvc.perform(delete(url)
                .session(session))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> commentMap = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(commentMap.get("id").toString()).isEqualTo(commentId.toString());
    }

    @Test
    @DisplayName("다른 회원이 댓글 삭제 요청시 에러 응답을 받는지 테스트")
    public void deleteComment_fail() throws Exception {
        //given
        String url = String.format("/qna/%d/comments/%d", questionId, commentId);
        session.setAttribute("user", new UserResponse(otherUser));
        //when
        String json = mockMvc.perform(delete(url)
                .session(session))
            .andExpect(status().isForbidden())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        //then
        TypeReference<HashMap<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, Object> commentMap = objectMapper.readValue(json, typeReference);
        Assertions.assertThat(commentMap.get("httpStatus")).isEqualTo("FORBIDDEN");
        Assertions.assertThat(commentMap.get("name")).isEqualTo("PERMISSION_DENIED");
        Assertions.assertThat(commentMap.get("errorMessage")).isEqualTo("접근 권한이 없습니다.");
    }

}
