package kr.codesqaud.cafe.app.question.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentResponse;
import kr.codesqaud.cafe.app.comment.service.CommentService;
import kr.codesqaud.cafe.app.common.pagination.Pagination;
import kr.codesqaud.cafe.app.question.controller.dto.QuestionResponse;
import kr.codesqaud.cafe.app.question.controller.dto.QuestionSavedRequest;
import kr.codesqaud.cafe.app.question.service.QuestionService;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Api(tags = "질문 게시글 API 정보를 제공하는 Controller")
@RestController
public class QuestionController {

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);
    private final QuestionService questionService;
    private final CommentService commentService;

    public QuestionController(QuestionService questionService, CommentService commentService) {
        this.questionService = questionService;
        this.commentService = commentService;
    }

    @ApiOperation(value = "질문 리스트 조회")
    @ApiImplicitParam(name = "page", value = "페이지 번호")
    @GetMapping({"/", "/qna"})
    public ModelAndView listQuestion(
        @RequestParam(value = "page", required = false, defaultValue = "1") String page) {
        log.info("page : {}", page);
        Long totalData = questionService.getTotalData();
        Long currentPage = parsePageNumber(page);
        Pagination pagination = new Pagination(totalData, currentPage);
        List<QuestionResponse> questions = questionService.getAllQuestionByPage(pagination);
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("questions", questions);
        mav.addObject("pagination", pagination);
        log.info("pagination : {}", pagination);
        return mav;
    }

    private Long parsePageNumber(String page) {
        try {
            long currentPage = Long.parseLong(page);
            return Math.max(currentPage, 1);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    @ApiOperation(value = "질문 추가")
    @ApiImplicitParam(name = "questionRequest", value = "질문 추가 저장 정보")
    @PostMapping("/qna")
    public QuestionResponse addQuestion(@Valid @RequestBody QuestionSavedRequest questionRequest) {
        return questionService.writeQuestion(questionRequest);
    }

    @ApiOperation(value = "질문 조회")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "cursor", value = "댓글 커서", paramType = "query")
    })
    @GetMapping("/qna/{id}")
    public ModelAndView detailQuestion(
        @PathVariable(value = "id") Long id,
        @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor) {
        log.info("cursor={}", cursor);
        ModelAndView mav = new ModelAndView("qna/detail");
        QuestionResponse question = questionService.findQuestion(id);
        List<CommentResponse> comments = commentService.getCommentsByCursor(id, cursor);
        int movedCursor = comments.size();
        mav.addObject("question", question);
        mav.addObject("comments", comments);
        mav.addObject("cursor", movedCursor);
        return mav;
    }

    // TODO : 인증 인터셉터 추가
    @ApiOperation(value = "질문 수정")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "수정 게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "questionRequest", value = "수정 게시글 정보", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보가 담긴 세션", paramType = "query")
    })
    @PutMapping("/qna/{id}")
    public QuestionResponse modifyQuestion(
        @PathVariable(value = "id") Long id,
        @Valid @RequestBody QuestionSavedRequest questionRequest,
        HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        QuestionResponse question = questionService.findQuestion(id);
        if (!question.getUserId().equals(user.getId())) {
            throw new RestApiException(UserErrorCode.PERMISSION_DENIED);
        }

        return questionService.modifyQuestion(id, questionRequest);
    }

    // TODO : 인증 인터셉터 추가
    @ApiOperation(value = "질문 삭제")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "삭제 게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보가 담긴 세션", paramType = "query")
    })
    @DeleteMapping("/qna/{id}")
    public QuestionResponse deleteQuestion(@PathVariable(value = "id") Long id, HttpSession session) {
        log.info(id.toString());
        UserResponse loginUser = (UserResponse) session.getAttribute("user");
        QuestionResponse question = questionService.findQuestion(id);
        if (!question.getUserId().equals(loginUser.getId())) {
            throw new RestApiException(UserErrorCode.PERMISSION_DENIED);
        }

        return questionService.delete(id);
    }

    @ApiOperation(value = "질문 추가 페이지")
    @GetMapping("/qna/new")
    public ModelAndView addQuestionForm() {
        return new ModelAndView("qna/new");
    }

    @ApiOperation(value = "질문 수정 페이지")
    @ApiImplicitParam(name = "id", value = "수정 게시글 등록번호")
    @GetMapping("/qna/{id}/edit")
    public ModelAndView editQuestionForm(@PathVariable(value = "id") Long id) {
        ModelAndView mav = new ModelAndView("qna/edit");
        mav.addObject("question", questionService.findQuestion(id));
        return mav;
    }
}
