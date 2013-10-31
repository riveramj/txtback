(function() {
  $(document)
    .on('click', '.edit-question', function() {
      $('#reload-page').click();
      $('#edit-question').modal('show');
    })
  $("#new-question").keypress(function() {
    var length = $("#new-question").val().length + 1
    $("#question-length").text(length);
  }); 
})();
