$(document).ready ->
  $(".available-numbers")
    .on('click', ".number-entry", (event) ->
      $('.number-entry').removeClass('selected')
      $(this).addClass('selected')
      $(this).find('input[type="radio"]').prop('checked', true)
      event.stopPropagation()
    )
