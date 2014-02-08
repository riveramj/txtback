txtback = window.txtback = {}

txtback.event = (name, data) ->
  event = $.Event name
  event[prop] = data[prop] for prop of data

  $(document).trigger event

