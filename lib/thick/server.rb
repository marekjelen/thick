module Thick

  def self.start(application, options)
    @server = Server.new(application)
    @server.start(:address => options[:address], :port => options[:port])
  end

  class Server

    def initialize(application)
      @factory = Thick::Java::NioServerSocketChannelFactory.new(Thick::Java::Executors.new_cached_thread_pool, Thick::Java::Executors.new_cached_thread_pool)
      @bootstrap = Thick::Java::ServerBootstrap.new(@factory)
      @bootstrap.pipeline_factory = PipelineFactory.new(application)
    end

    def start(options = {})
      @channel = @bootstrap.bind(Thick::Java::InetSocketAddress.new(options[:address], options[:port]))
      puts 'Server started'
      loop do
        # ToDo: Is this hack needed?
        sleep(100)
      end
    end

    def stop
      @channel.close
    end

  end

end