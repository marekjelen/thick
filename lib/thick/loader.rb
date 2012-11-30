module Thick

  class Loader

    include Thick::Java::ServerRubyInterface

    def initialize(options)
      @options = options
      ENV['RACK_ENV'] ||= ENV['RAILS_ENV'] ||= @options[:environment]
      @application = Rack::Builder.parse_file(@options[:file])[0]
    end

    def call(env)
      env['rack.input'] = Thick::Java::ByteBufInputStream.new(Thick::Java::Unpooled.wrappedBuffer(*env['rack.input']))
      env['rack.errors'] = env['rack.errors'].to_io
      status, headers, body = @application.call(env)
      response = env['thick.response']
      headers.each_pair do |name, value|
        response.setHeader(name, value)
      end
      body.inject("") {|last,part| last << part}
    rescue => e
      puts e.message
      puts e.backtrace
    end

  end

end