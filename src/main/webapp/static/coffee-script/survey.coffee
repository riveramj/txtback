$(document).ready ->

  $("#new-question").keypress((event) ->
    length = $("#new-question").val().length + 1
    $("#question-length").text length
  )

  $(document)
    .on('click', '.edit-question', (event) ->
      $('#reload-page').click
      $('#edit-question').modal 'show'
    )
