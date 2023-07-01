package kr.codesqaud.cafe.app.question.repository;

import kr.codesqaud.cafe.app.comment.repository.CommentRepository;
import kr.codesqaud.cafe.app.common.pagination.Pagination;
import kr.codesqaud.cafe.app.question.entity.Question;
import kr.codesqaud.cafe.app.user.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile("memory")
@Repository
public class MemoryQuestionRepository implements QuestionRepository {

    private final List<Question> store = new ArrayList<>();
    private static long sequence = 0;

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public MemoryQuestionRepository(UserRepository userRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Question> findAll() {
        return store.stream()
            .filter(question -> !question.getDeleted())
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Question> findAllByPage(Pagination pagination) {
        return store.stream()
            .filter(question -> !question.getDeleted())
            .sorted(Comparator.comparing(Question::getCreateTime).reversed())
            .map(getQuestionListMapper())
            .skip(pagination.getStartNumber() - 1)
            .limit(pagination.getEndNumber() - pagination.getStartNumber() + 1)
            .collect(Collectors.toUnmodifiableList());
    }

    private Function<Question, Question> getQuestionListMapper() {
        return question -> Question.builder()
            .id(question.getId())
            .title(question.getTitle())
            .createTime(question.getCreateTime())
            .modifyTime(question.getModifyTime())
            .deleted(question.getDeleted())
            .writer(question.getWriter())
            .build();
    }

    @Override
    public Long findQuestionCount() {
        return store.stream()
            .filter(question -> !question.getDeleted())
            .count();
    }

    @Override
    public Optional<Question> findById(Long id) {
        return store.stream()
            .filter(question -> !question.getDeleted())
            .filter(article -> article.getId().equals(id)).findFirst();
    }

    @Override
    public Question save(Question question) {
        Question newQuestion =
            Question.builder()
                .id(nextId())
                .title(question.getTitle())
                .content(question.getContent())
                .createTime(LocalDateTime.now())
                .deleted(question.getDeleted())
                .writer(userRepository.findById(question.getWriter().getId()).orElseThrow())
                .build();
        store.add(newQuestion);
        return newQuestion;
    }

    @Override
    public Question modify(Question question) {
        Question original = findById(question.getId()).orElseThrow();
        original.modify(question);
        return original;
    }

    @Override
    public Question deleteById(Long id) {
        Question delQuestion = findById(id).orElseThrow();
        delQuestion.delete();
        return delQuestion;
    }

    @Override
    public int deleteAll() {
        int delCount = store.size();
        store.clear();
        return delCount;
    }

    private synchronized Long nextId() {
        return ++sequence;
    }
}
