$(document).ready ->
  $(".number-entry")
    .on('click', (event) ->
      $('.number-entry').removeClass('selected')
      $(this).addClass('selected')
      $(this).find('input[type="radio"]').prop('checked', true)
    )
