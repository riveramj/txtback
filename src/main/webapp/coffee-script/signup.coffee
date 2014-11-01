Stripe.setPublishableKey 'pk_test_8CPavIa4sd82JvvmSJym4CqL'

$(document).ready ->
  $(".available-numbers")
    .on('click', ".number-entry", (event) ->
      $('.number-entry').removeClass('selected')
      $(this).addClass('selected')
      $(this).find('input[type="radio"]').prop('checked', true)
      event.stopPropagation()
    )
    .on('keydown', "#phone", (event) ->
      if event.which == 13
        this.blur()
        $(".number-search").click()
        event.preventDefault()
    )
    .on('keydown', "#area-code", (event) ->
      if event.which == 13
        this.blur()
        $(".number-search").click()
        event.preventDefault()
    )

   
