package kr.codesqaud.cafe.app.question.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kr.codesqaud.cafe.app.question.entity.Question;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryQuestionRepository implements QuestionRepository {

    private final List<Question> store = new ArrayList<>();
    private static long sequence = 0;

    @Override
    public List<Question> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public Optional<Question> findById(Long id) {
        return store.stream().filter(article -> article.getId().equals(id)).findFirst();
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
                .writer(question.getWriter())
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

    private synchronized Long nextId() {
        return ++sequence;
    }
}