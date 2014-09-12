require 'sinatra/base'

class Socket # < Thick::WebSocket
  def on_open
    puts "Opened"
  end

  def on_data(data)
    puts data.inspect
  end

  def on_close
    puts "Closed"
  end
end

class App < Sinatra::Base

  get '/' do
    'Hi'
  end

  post '/echo' do
    request.body.read
  end

  get '/websockets' do
    "<html>\n<head><title>Web Socket Test</title></head>\n" +
        "<body>\n" +
        "<script type=\"text/javascript\">" +
          "var socket; \n" +
          "socket = new WebSocket(\"ws://localhost:9292/websockets\");\n" +
          "socket.onopen = function(evt ){ socket.send('Hello') };" +
          "socket.onmessage = function(evt){ console.log(evt.data); socket.close() };" +
        "</script>\n" +
        "</body>\n" +
        "</html>\n"
  end

  put '/upload' do
    request.body.read
  end

  put '/thick/websockets' do
    env['thick.websocket'].set_handler(Socket.new)
    env['thick.websocket'].hand_shake("ws://localhost:9292/websockets")
  end

end

run App
