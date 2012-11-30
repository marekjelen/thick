module Thick

  class Loader

    def initialize(options)
      @options = options
      ENV['RACK_ENV'] ||= ENV['RAILS_ENV'] ||= @options[:environment]
      @application = Rack::Builder.parse_file(@options[:file])[0]
    end

    def call(env)
      @application.call(env)
    rescue => e
      puts e.message
      puts e.backtrace
    end

  end

end