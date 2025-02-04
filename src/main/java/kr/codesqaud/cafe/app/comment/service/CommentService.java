package kr.codesqaud.cafe.app.comment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.codesqaud.cafe.app.comment.controller.dto.CommentResponse;
import kr.codesqaud.cafe.app.comment.controller.dto.CommentSavedRequest;
import kr.codesqaud.cafe.app.comment.entity.Comment;
import kr.codesqaud.cafe.app.comment.repository.CommentRepository;
import kr.codesqaud.cafe.app.common.pagination.CommentCursor;
import kr.codesqaud.cafe.errors.errorcode.CommentErrorCode;
import kr.codesqaud.cafe.errors.exception.ResourceNotFoundException;

@Service
public class CommentService {

	private final CommentRepository commentRepository;

	public CommentService(CommentRepository commentRepository) {
		this.commentRepository = commentRepository;
	}

	@Transactional
	public CommentResponse answerComment(CommentSavedRequest commentSavedRequest) {
		Comment savedComment = commentRepository.save(commentSavedRequest.toEntity());
		return new CommentResponse(savedComment);
	}

	public List<CommentResponse> getComments(Long questionId) {
		List<Comment> comments = commentRepository.findAll(questionId);

		return comments.stream()
			.map(CommentResponse::new)
			.collect(Collectors.toUnmodifiableList());
	}

	public List<CommentResponse> getCommentsByCursor(Long questionId, Long cursor) {
		CommentCursor commentCursor = new CommentCursor(cursor);
		return commentRepository.findAllByCursor(questionId, commentCursor).stream()
			.map(CommentResponse::new)
			.collect(Collectors.toUnmodifiableList());
	}

	public CommentResponse getComment(Long id) {
		Comment findComment = commentRepository.findById(id).orElseThrow(() -> {
			throw new ResourceNotFoundException(CommentErrorCode.NOT_FOUND_COMMENT);
		});

		return new CommentResponse(findComment);
	}

	@Transactional
	public CommentResponse modifyComment(Long id, CommentSavedRequest commentRequest) {
		Comment original = commentRepository.findById(id).orElseThrow();
		original.modify(commentRequest.toEntity());
		Comment modifyComment = commentRepository.modify(original);
		return new CommentResponse(modifyComment);
	}

	@Transactional
	public CommentResponse deleteComment(Long id) {
		Comment delComment = commentRepository.deleteById(id);
		return new CommentResponse(delComment);
	}

	public Long getTotalData(Long questionId) {
		return commentRepository.countByQuestionId(questionId);
	}
}
