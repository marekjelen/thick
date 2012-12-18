module Thick

  java_import 'cz.marekjelen.thick.WebSocketHandler'

  class WebSocket < WebSocketHandler

    def on_open
      raise NotImplementedError.new("You should implement '#on_open' method.")
    end

    def on_data(data)
      raise NotImplementedError.new("You should implement '#on_data(data)' method.")
    end

    def on_close
      raise NotImplementedError.new("You should implement '#on_close' method.")
    end

  end

end