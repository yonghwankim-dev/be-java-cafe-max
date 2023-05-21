package kr.codesqaud.cafe.app.comment.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentResponse;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentSavedRequest;
import kr.codesqaud.cafe.app.comment.service.CommentService;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.PermissionDeniedException;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Api(tags = "댓글 API 정보를 제공하는 Controller")
@RestController
@RequestMapping("/qna/{id}")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @ApiOperation(value = "댓글 목록 조회")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "questionId", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "cursor", value = "댓글 커서", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보 세션", paramType = "query")
    })
    @GetMapping("/comments")
    public ResponseEntity<Map<String, Object>> listComment(
        @PathVariable(value = "id") Long questionId,
        @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
        HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("user");
        Map<String, Object> commentMap = new ConcurrentHashMap<>();
        List<CommentResponse> comments = commentService.getCommentsByCursor(questionId, cursor);
        Long totalData = commentService.getTotalData(questionId);
        Long movedCursor = cursor + comments.size();
        commentMap.put("comments", comments);
        commentMap.put("totalData", totalData);
        commentMap.put("cursor", movedCursor);
        commentMap.put("requestUserId", loginUser.getId());
        return ResponseEntity.ok().body(commentMap);
    }

    @ApiOperation(value = "댓글 추가")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "questionId", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "commentRequest", value = "댓글 추가 정보", paramType = "query")
    })
    @PostMapping("/comments")
    public CommentResponse createComment(
        @PathVariable(value = "id") Long questionId,
        @Valid @RequestBody CommentSavedRequest commentRequest) {
        log.info("questionId : {}", questionId);
        log.info("commentRequest : {}", commentRequest);
        return commentService.answerComment(commentRequest);
    }

    // TOOD: 권한 인터셉터 필요
    @ApiOperation(value = "댓글 수정")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "questionId", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "commentId", value = "댓글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "commentRequest", value = "댓글 수정 정보", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보 세션", paramType = "query")
    })
    @PutMapping("/comments/{commentId}")
    public CommentResponse modifyComment(@PathVariable(value = "id") Long questionId,
        @PathVariable(value = "commentId") Long commentId,
        @Valid @RequestBody CommentSavedRequest commentRequest,
        HttpSession session) {
        log.info("questionId : {}, commentRequest : {}", questionId, commentRequest);

        UserResponse user = (UserResponse) session.getAttribute("user");
        CommentResponse comment = commentService.getComment(commentId);
        if (!comment.getUserId().equals(user.getId())) {
            throw new RestApiException(UserErrorCode.PERMISSION_DENIED);
        }

        return commentService.modifyComment(commentId, commentRequest);
    }

    @ApiOperation(value = "댓글 삭제")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "questionId", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "commentId", value = "댓글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보 세션", paramType = "query")
    })
    @DeleteMapping("/comments/{commentId}")
    public CommentResponse deleteComment(@PathVariable(value = "id") Long questionId,
        @PathVariable(value = "commentId") Long commentId,
        HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        CommentResponse comment = commentService.getComment(commentId);
        if (!comment.getUserId().equals(user.getId())) {
            throw new RestApiException(UserErrorCode.PERMISSION_DENIED);
        }

        return commentService.deleteComment(commentId);
    }

    @ApiOperation(value = "댓글 수정 페이지")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "questionId", value = "게시글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "commentId", value = "댓글 등록번호", paramType = "query"),
        @ApiImplicitParam(name = "session", value = "로그인 정보 세션", paramType = "query")
    })
    @GetMapping("/comments/{commentId}/edit")
    public ModelAndView editCommentForm(@PathVariable(value = "id") Long questionId,
        @PathVariable(value = "commentId") Long commentId,
        HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("user");
        CommentResponse comment = commentService.getComment(commentId);
        if (!comment.getUserId().equals(user.getId())) {
            throw new PermissionDeniedException(UserErrorCode.PERMISSION_DENIED);
        }

        ModelAndView mav = new ModelAndView("comment/edit");
        mav.addObject("comment", commentService.getComment(commentId));
        return mav;
    }
}
