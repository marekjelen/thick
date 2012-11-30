# Thick

Thick is very lightweight web server for JRuby based on excellent Netty library.

[![Build Status](https://secure.travis-ci.org/marekjelen/thick.png?branch=master)](http://travis-ci.org/marekjelen/thick)

## Status

Works on several Sinatra and Rails applications. No real testing, yet.

## Speed

see PERFORMANCE.md

## Interfaces

Thick provides interfaces to control it's behaviour.

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