package kr.codesqaud.cafe.app.question.controller.dto;

import kr.codesqaud.cafe.app.question.entity.Question;
import kr.codesqaud.cafe.util.LocalDateTimeUtil;

public class QuestionResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String createTime;
    private final Long userId;
    private final String writer;
    private final int commentCount;

    public QuestionResponse(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.content = question.getContent();
        this.createTime = LocalDateTimeUtil.formatLocalDateTime(question.getCreateTime());
        this.userId = question.getWriter().getId();
        this.writer = question.getWriter().getName();
        this.commentCount = question.getCommentCount();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public String getWriter() {
        return writer;
    }

    public int getCommentCount() {
        return commentCount;
    }

    @Override
    public String toString() {
        return "QuestionResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createTime='" + createTime + '\'' +
                ", userId=" + userId +
                ", writer='" + writer + '\'' +
                ", commentCount=" + commentCount +
                '}';
    }
}
