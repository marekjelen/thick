module Thick

  class Loader

    include Thick::Java::ServerRubyInterface

    def initialize(options)
      @options = options
      ENV['RACK_ENV'] ||= ENV['RAILS_ENV'] ||= @options[:environment]
      @application = @options[:application] ||= Rack::Builder.parse_file(File.expand_path(@options[:file], @options[:directory]))[0]
    end

    def call(env)
      #noinspection RubyArgCount
      env['rack.input'] = Buffer.new(env['rack.input'])
      env['rack.errors'] = env['rack.errors'].to_io

      status, headers, body = @application.call(env)

      return if env['thick.response_bypass']

      response = env['thick.response']

      response.setStatus(status)

      hijack = headers.delete('rack.hijack')

      headers.each_pair do |name, value|
        response.headers.set(name, value)
      end

      if body.respond_to?(:to_path)
        response.send_file(body.to_path)
      else
        response.send
        body.each { |chunk| response.writeContent(chunk.to_s) }
        if hijack
          # Rack Hijack async response
          hijack.call(env['rack.hijakck_io'])
        elsif response.chunked?
          # Thick native async response
          env['thick.async'].call(response)
        else
          response.close
        end
      end
    rescue => e
      puts e.message
      puts e.backtrace
    end

    def convert(map, hsh = {})
      map.each { |key, value| hsh[key] = value }
      hsh
    end

  end

end