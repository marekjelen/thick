# Thick

Thick is very lightweight web server for JRuby based on excellent Netty library.

[![Build Status](https://secure.travis-ci.org/marekjelen/thick.png?branch=master)](http://travis-ci.org/marekjelen/thick)

## Status

Works on several Sinatra and Rails applications. No real testing, yet.

## Speed

see PERFORMANCE.md

## Interfaces

Thick provides interfaces to control it's behaviour.

### WebSockets

Thick allows to handle WebSocket in a simple and (IMHO) not intrusive way.

When a request to WebSocket is received, the server send a PUT request to

    /thick/websockets

and provides two keys in the environment

    env['thick.websocket'].set_handler(Your::Handler)
    env['thick.websocket'].hand_shake("websocket URL")

Your::Handler should subclass Thick::WebSocket class. It is used to provide interface
between your application and the connection. The interface for communication from the
client to your application looks like

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

Two more methods are available to control communicate back to the client

  send("data") # sends data back to client
  close # closes the connection

The hand_shake("url") method URL asks the server to finish the hand shake and upgrade
the connection to WebSocket.

### Asynchronous responses

Asynchronous responses provide functionality to stream response to the client by chunks.

To enable streaming, run

    env['thick.response'].chunked # For chunked encoded stream
    env['thick.response'].streamed # For raw stream

and define "thick.async" callback in environment, so it reacts to "call" and takes one argument

    env['thick.async'] = proc do |response|
      # do something here
    end

now simply respond as usual. Once the headers and the basic content is sent, the callback will be invoked.

To write content to the stream use

    response.writeContent("some content")

and do not forget close the stream once done

    response.close

Example using Sinatra

    require 'sinatra/base'

    class App < Sinatra::Base

      get '/' do
        env['thick.async'] = proc do |response|
          (1..10).each do |i|
            response.writeContent(i.to_s)
            sleep(1)
          end
          response.close
        end
        env['thick.response'].chunked
        "0"
      end

    end

    run App

## Starting

    thick [-o <interface>] [-p <port>] [-E <environment>]

## Installation

    gem install thick

## License

MIT ... let me know if that's a problem for you.