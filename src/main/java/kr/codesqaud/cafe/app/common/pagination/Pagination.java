package kr.codesqaud.cafe.app.common.pagination;

public class Pagination {

    private static final Long SIZE = 15L; // 한 페이지에 보여줄 게시글 수
    private static final Long PAGE_GROUP = 5L; // 한번에 표시할 페이지 버튼 수

    private final Long totalData; // 전체 데이터 개수
    private final Long currentPage; // 현재 페이지

    public Pagination(Long totalData, Long currentPage) {
        this.totalData = totalData;
        if (currentPage > getTotalPage()) {
            currentPage = getTotalPage();
        }
        this.currentPage = currentPage;
    }

    public Long getTotalPage() {
        return ((totalData - 1) / SIZE) + 1;
    }

    public Long getStartPage() {
        return ((currentPage - 1) / PAGE_GROUP) * PAGE_GROUP + 1;
    }

    public Long getEndPage() {
        Long endPage = (((currentPage - 1) / PAGE_GROUP) + 1) * PAGE_GROUP;
        if (getTotalPage() < endPage) {
            endPage = getTotalPage();
        }
        return endPage;
    }

    public boolean isExistPrev() {
        return getStartPage() != 1;
    }

    public boolean isExistNext() {
        return getEndPage() != getTotalPage();
    }

    public Long getStartNumber() {
        return (SIZE * (currentPage - 1)) + 1;
    }

    public Long getEndNumber() {
        return SIZE * currentPage;
    }

    public Long getPrevGroupStartPage() {
        return currentPage - PAGE_GROUP;
    }

    public Long getNextGroupStartPage() {
        return getEndPage() + 1;
    }

    public Long getTotalData() {
        return totalData;
    }

    public Long getCurrentPage() {
        return currentPage;
    }

    @Override
    public String toString() {
        return "Pagination{" +
            "totalData=" + totalData +
            ", currentPage=" + currentPage +
            '}';
    }
}
