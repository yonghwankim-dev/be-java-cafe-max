package kr.codesqaud.cafe.app.comment.repository;

import kr.codesqaud.cafe.app.comment.entity.Comment;
import kr.codesqaud.cafe.app.common.pagination.CommentCursor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile(value = "memory")
@Repository
public class MemoryCommentRepository implements CommentRepository {

    private static final List<Comment> store = new ArrayList<>();
    private static long sequence = 0;

    @Override
    public List<Comment> findAll(Long questionId) {
        return store.stream()
                .filter(comment -> !comment.getDeleted())
                .filter(comment -> comment.getQuestion().getId().equals(questionId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Comment> findAllByCursor(Long questionId, CommentCursor commentCursor) {
        return store.stream()
                .filter(comment -> !comment.getDeleted())
                .filter(comment -> comment.getQuestion().getId().equals(questionId))
                .skip(commentCursor.getStart())
                .limit(commentCursor.getEnd() - commentCursor.getStart())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return store.stream()
                .filter(comment -> !comment.getDeleted())
                .filter(comment -> comment.getId().equals(id))
                .findAny();
    }

    @Override
    public Comment save(Comment comment) {
        Comment newComment = Comment.builder()
                .id(nextId())
                .content(comment.getContent())
                .createTime(LocalDateTime.now())
                .modifyTime(null)
                .deleted(false)
                .question(comment.getQuestion())
                .writer(comment.getWriter())
                .build();
        store.add(newComment);
        return newComment;
    }

    @Override
    public Comment modify(Comment comment) {
        Comment original = findById(comment.getId()).orElseThrow();
        original.modify(comment);
        return original;
    }

    @Override
    public Comment deleteById(Long id) {
        Comment comment = findById(id).orElseThrow();
        comment.delete();
        return comment;
    }

    @Override
    public void deleteAllByQuestionId(Long questionId) {
        store.stream()
                .filter(comment -> comment.getQuestion().getId().equals(questionId))
                .forEach(Comment::delete);
    }

    @Override
    public int deleteAll() {
        store.forEach(Comment::delete);
        return store.size();
    }

    @Override
    public Long countByQuestionId(Long questionId) {
        return store.stream()
                .filter(comment -> comment.getQuestion().getId().equals(questionId))
                .count();
    }

    private synchronized Long nextId() {
        return ++sequence;
    }
}
