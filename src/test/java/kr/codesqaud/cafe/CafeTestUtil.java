package kr.codesqaud.cafe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpSession;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentSavedRequest;
import kr.codesqaud.cafe.app.comment.service.CommentService;
import kr.codesqaud.cafe.app.question.controller.dto.QuestionSavedRequest;
import kr.codesqaud.cafe.app.question.service.QuestionService;
import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.repository.UserRepository;
import kr.codesqaud.cafe.app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CafeTestUtil {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private CommentService commentService;

    public Long writeQuestion(String title, String content, String userId) {
        User user = userService.findUser(userId).toEntity();
        QuestionSavedRequest dto = new QuestionSavedRequest(title, content, user.getId());
        return questionService.writeQuestion(dto).getId();
    }

    public Long writeComment(Long questionId, Long userId, String content) {
        return commentService.answerComment(new CommentSavedRequest(content, questionId, userId))
            .getId();
    }

    public Long signUp(User user) {
        return userRepository.save(user).getId();
    }

    public void login(String userId, String password, HttpSession session) {
        User user = userService.login(new UserLoginRequest(userId, password));
        session.setAttribute("user", new UserResponse(user));
    }

    public <T> String toJSON(T data) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(data);
    }
}
