package kr.codesqaud.cafe.app.common.pagination;

public class CommentCursor {

    private static final Long SIZE = 15L;

    private final Long start;

    public CommentCursor(Long start) {
        this.start = start;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return start + SIZE;
    }

    @Override
    public String toString() {
        return "CommentCursor{" +
            "start=" + start +
            '}';
    }
}
