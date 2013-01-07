require File.expand_path('../../../thick', __FILE__)

module Rack
  module Handler

    class Thick

      def self.run(app, options={})

        env = ::Thick::Java::ServerEnvironment.new
        env.address = options[:Host]
        env.port = options[:Port]

        env.application = ::Thick::Loader.new({:application => app, :environment => options[:environment]})

        ::Thick::Java::Server.new(env).start

      end

    end

    def self.valid_options
      {
          "Host=HOST" => "Hostname to listen on (default: localhost)",
          "Port=PORT" => "Port to listen on (default: 8080)"
      }
    end

    register 'thick', 'Rack::Handler::Thick'

  end
end