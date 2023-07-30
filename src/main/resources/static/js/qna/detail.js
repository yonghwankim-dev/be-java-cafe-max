$(document).ready(function () {
    $("#form").submit(async function (e) {
        e.preventDefault()

        const id = $("#id").val()

        $.ajax({
            type: "DELETE",
            url: `/qna/${id}`,
        }).done(function (resp) {
            alert(`${resp.title} 게시글을 삭제하였습니다.`)
            location.href = `/`
        }).fail(function (response) {
            const errorResponse = response.responseJSON
            alert(errorResponse.errorMessage)
        })
    })

    $(".submit-write button[type=submit]").on("click", addAnswer);
    $(".qna-comment-slipp-articles").on("click",
        ".link-modify-article", showModifyAnswer);
    $(".qna-comment-slipp-articles").on("click",
        ".delete-answer-form button[type=submit]", deleteAnswer);

    $("#moreComment").on("click", showMoreComment);
})

function addAnswer(e) {
    e.preventDefault(); //submit 이 자동으로 동작하는 것을 막는다.

    let jsonData = {}
    $("form[name=answer]").serializeArray().map(function (x) {
        jsonData[x.name] = x.value;
    });
    const urlPath = $(".submit-write").attr("action");

    $.ajax({
        type: 'post',
        url: urlPath,
        data: JSON.stringify(jsonData),
        contentType: 'application/json; charset=utf-8',
        error: function (resp) {
            alert(resp.responseJSON.errorMessage)
        },
        success: function (data) {
            const answerTemplate = $("#answerTemplate").html();
            const template = answerTemplate.format(
                data.writerName,
                data.createTime,
                data.content,
                data.questionId,
                data.id);
            $(".qna-comment-slipp-articles").append(template);
            $("textarea[name=content]").val("");
        }
    });
}

function showModifyAnswer(e) {
    console.log($(this).attr("href"))
    location.href = $(this).attr("href")
}

function deleteAnswer(e) {
    e.preventDefault();

    const deleteBtn = $(this);
    const urlPath = $(".delete-answer-form").attr("action");

    $.ajax({
        type: 'delete',
        url: urlPath,
        dataType: 'json',
        error: function (xhr, status) {
            console.log("error");
        },
        success: function (data) {
            deleteBtn.closest("article").remove();
        }
    });
}

function showMoreComment(e) {
    e.preventDefault()
    const cursor = $("#moreComment").data("cursor");
    const urlPath = $("#moreCommentForm").attr("action")

    $.ajax({
        type: 'get',
        url: `${urlPath}?cursor=${cursor}`,
        error: function (resp) {
            alert(resp.responseJSON.errorMessage)
        },
        success: function (resp) {
            console.log(resp)
            const comments = resp.comments
            const totalData = resp.totalData
            const cursor = resp.cursor
            const requestUserId = resp.requestUserId

            comments.forEach((comment) => {
                let template = null
                if (comment.userId === requestUserId) {
                    const answerTemplate = $("#answerTemplate").html();
                    template = answerTemplate.format(
                        comment.writerName,
                        comment.createTime,
                        comment.content,
                        comment.questionId,
                        comment.id)
                } else {
                    const answerTemplate = $("#answerTemplateWithoutUtil").html();
                    template = answerTemplate.format(
                        comment.writerName,
                        comment.createTime,
                        comment.content
                    )
                }
                $(".qna-comment-slipp-articles").append(template);
            })

            if (cursor >= totalData) {
                $("#moreComment").hide()
            } else {
                $("#moreComment").data("cursor", cursor)
            }
        }
    })
}
