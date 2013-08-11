(function() {
  $(document)
    .on('click', '.edit-question', function() {
      $('#reload-page').click();
      $('#edit-question').modal('show');
    })
})();
