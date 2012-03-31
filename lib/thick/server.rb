module Thick

  class Server

    def initialize(options)
      @options = options
      @factory = Thick::Java::NioServerSocketChannelFactory.new(Thick::Java::Executors.new_cached_thread_pool, Thick::Java::Executors.new_cached_thread_pool)
      @bootstrap = Thick::Java::ServerBootstrap.new(@factory)
      @bootstrap.pipeline_factory = PipelineFactory.new(@options)
    end

    def start
      @channel = @bootstrap.bind(Thick::Java::InetSocketAddress.new(@options[:address], @options[:port]))
      puts "* Server started on #{@options[:address]}:#{@options[:port]}"
      self
    end

    def stop
      @channel.close
      self
    end

  end

end