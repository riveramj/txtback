(function() {
  $(document)
    .on('click', '.edit-question', function(event) {
      $('#reload-page').click();
      $('#edit-question').modal('show')
    })
})();
