module Thick

  class Loader

    def initialize(options)
      @options = options
      ENV['RACK_ENV'] ||= ENV['RAILS_ENV'] ||= @options[:environment]
      if @options[:reloader]
        @application = proc do |env|
          container = Thick::Java::ScriptingContainer.new(Thick::Java::LocalContextScope::SINGLETHREAD)
          container.setAttribute(Thick::Java::AttributeName::SHARING_VARIABLES, false)
          container.setCompatVersion(Thick::Java::CompatVersion::RUBY1_9)
          container.setRunRubyInProcess(false)
          runner = "require 'rack'; Rack::Builder.parse_file('#{@options[:file]}')"
          begin
            container.run_scriptlet(runner)[0].call(env)
          rescue => e
            [500, {'Content-Type' => 'text/plain'}, ["Dead ;)\n\n#{e.message}\n\n\t#{e.backtrace.join("\n\t")}"]]
          ensure
            container.clear
            container.finalize
          end

        end
      else
        @application = Rack::Builder.parse_file(@options[:file])[0]
      end
    end

    def call(env)
      @application.call(env)
    rescue => e
      puts e.message
      puts e.backtrace
    end

  end

end