package kr.codesqaud.cafe.app.comment.repository;

import java.util.List;
import java.util.Optional;

import kr.codesqaud.cafe.app.comment.entity.Comment;
import kr.codesqaud.cafe.app.common.pagination.CommentCursor;

public interface CommentRepository {

	List<Comment> findAll(Long questionId);

	List<Comment> findAllByCursor(Long questionId, CommentCursor commentCursor);

	Optional<Comment> findById(Long id);

	Comment save(Comment comment);

	Comment modify(Comment comment);

	Comment deleteById(Long id);

	void deleteAllByQuestionId(Long questionId);

	int deleteAll();

	Long countByQuestionId(Long questionId);
}
