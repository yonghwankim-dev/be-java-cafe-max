$(document).ready(function () {
  $("#form").submit(async function (e) {
    e.preventDefault()
    await clearErrorMessage()

    const data = {
      "userId": $("#userId").val(),
      "password": $("#password").val(),
      "name": $("#name").val(),
      "email": $("#email").val()
    }
    const id = $("#id").val()
    const urlPath = "/users/" + id + "/update"

    $.ajax({
      type: 'put',
      url: urlPath,
      data: JSON.stringify(data),
      contentType: 'application/json; charset=utf-8',
    }).done(function () {
      alert("회원정보 수정이 완료되었습니다.")
      location.href = "/login"
    }).fail(function (response) {
      const errorResponse = response.responseJSON
      // 유저 이메일 중복
      if (errorResponse.name === 'ALREADY_EXIST_EMAIL') {
        $("#emailError").text(errorResponse.errorMessage)
      }
      // 유저 입력 형식 오류
      if (errorResponse.name === 'INVALID_INPUT_FORMAT') {
        errorResponse.errors.forEach(item => {
          $(`#${item.field}Error`).text(item.message)
        })
      }
      // 비밀번호 일치하지 않음
      if (errorResponse.name === 'NOT_MATCH_PASSWORD') {
        $("#passwordError").text(errorResponse.errorMessage)
      }
    })
  })

  function clearErrorMessage() {
    $("#form p").text("")
  }
})
