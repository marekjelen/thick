require 'sinatra/base'

class App < Sinatra::Base

  get '/' do
    'Hi'
  end

  get '/websockets' do
    "<html>\n<head><title>Web Socket Test</title></head>\n" +
        "<body>\n" +
        "<script type=\"text/javascript\">" +
          "var socket; \n" +
          "socket = new WebSocket(\"ws://localhost:9292/websockets\");\n" +
        "</script>\n" +
        "</body>\n" +
        "</html>\n"
  end

end

run App
