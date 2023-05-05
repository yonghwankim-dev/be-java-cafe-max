package kr.codesqaud.cafe.app.comment.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import kr.codesqaud.cafe.app.comment.entity.Comment;
import kr.codesqaud.cafe.app.common.pagination.CommentCursor;
import kr.codesqaud.cafe.app.question.entity.Question;
import kr.codesqaud.cafe.app.user.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate template;

    public JdbcCommentRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Comment> findAll(Long questionId) {
        return template.query(
            "SELECT c.ID, c.CONTENT, c.CREATETIME, c.USERID, u.NAME, c.QUESTIONID "
                + "FROM comment c "
                + "INNER JOIN question q ON c.QUESTIONID = q.ID "
                + "INNER JOIN users u ON c.USERID = u.ID "
                + "WHERE q.ID = ? and c.DELETED = false "
                + "ORDER BY createTime", commentRowMapper(), questionId);
    }

    @Override
    public List<Comment> findAllByCursor(Long questionId, CommentCursor commentCursor) {
        return template.query("SELECT rn, ID, CONTENT, CREATETIME, USERID, NAME, QUESTIONID "
                + "FROM (SELECT ROWNUM rn, c.ID, c.CONTENT, c.CREATETIME, c.USERID, u.NAME, c.QUESTIONID "
                + "      FROM comment c "
                + "               INNER JOIN question q ON c.QUESTIONID = q.ID "
                + "               INNER JOIN users u ON c.USERID = u.ID "
                + "      WHERE q.ID = ? "
                + "        AND c.DELETED = false "
                + "        AND ROWNUM <= ?) as c "
                + "WHERE rn > ?", commentRowMapper(), questionId, commentCursor.getEnd(),
            commentCursor.getStart());
    }

    @Override
    public Optional<Comment> findById(Long id) {
        List<Comment> result = template.query(
            "SELECT c.ID, c.CONTENT, c.CREATETIME, c.USERID, u.NAME, c.QUESTIONID "
                + "FROM comment c "
                + "INNER JOIN question q ON c.QUESTIONID = q.ID "
                + "INNER JOIN users u ON c.USERID = u.ID "
                + "WHERE c.ID = ? "
                + "and c.DELETED = false", commentRowMapper(), id);
        return result.stream().findAny();
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comment(content, questionId, userId) VALUES(?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(con -> getPreparedStatement(comment, con, sql), keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public Comment modify(Comment comment) {
        template.update("UPDATE comment SET content = ? WHERE id = ?", comment.getContent(),
            comment.getId());
        return comment;
    }

    @Override
    public Comment deleteById(Long id) {
        Comment delComment = findById(id).orElseThrow();
        template.update("DELETE FROM comment WHERE id = ?", id);
        return delComment;
    }

    @Override
    public void deleteAllByQuestionId(Long questionId) {
        template.update("UPDATE comment SET deleted = true WHERE questionId = ?", questionId);
    }

    @Override
    public Long countByQuestionId(Long questionId) {
        return template.queryForObject(
            "SELECT COUNT(*) FROM comment WHERE questionId = ?", Long.class, questionId);
    }

    private PreparedStatement getPreparedStatement(Comment comment, Connection con, String sql)
        throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"ID"});
        pstmt.setString(1, comment.getContent());
        pstmt.setLong(2, comment.getQuestion().getId());
        pstmt.setLong(3, comment.getWriter().getId());
        return pstmt;
    }

    private RowMapper<Comment> commentRowMapper() {
        return (rs, rowNum) ->
            Comment.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .createTime(rs.getTimestamp("createTime").toLocalDateTime())
                .writer(User.builder()
                    .id(rs.getLong("userid"))
                    .name(rs.getString("name"))
                    .build())
                .question(Question.builder()
                    .id(rs.getLong("questionId"))
                    .build())
                .build();
    }


}
