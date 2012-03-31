module Thick
  class Controller

    def initialize(options, server)
      @options = options
      @server = server
      @factory = Thick::Java::NioServerSocketChannelFactory.new(Thick::Java::Executors.new_cached_thread_pool, Thick::Java::Executors.new_cached_thread_pool)
      @bootstrap = Thick::Java::ServerBootstrap.new(@factory)
      @bootstrap.pipeline_factory = Controller::PipelineFactory.new(@options)
    end

    def start
      @channel = @bootstrap.bind(Thick::Java::InetSocketAddress.new('127.0.0.1', 9393))
      puts "* Controller started on 127.0.0.1:9393"
      self
    end

    def stop
      @channel.close
      self
    end

    class PipelineFactory

      include Thick::Java::ChannelPipelineFactory

      def initialize(options)
        @options = options
      end

      def getPipeline
        pipeline = Thick::Java::Channels.pipeline
        pipeline.add_last('decoder', Thick::Java::HttpRequestDecoder.new)
        pipeline.add_last('aggregator', Thick::Java::HttpChunkAggregator.new(65536))
        pipeline.add_last('encoder', Thick::Java::HttpResponseEncoder.new)
        pipeline.add_last('handler', Controller::Handler.new(@options))
        pipeline
      end

    end

    class Handler < Thick::Java::SimpleChannelUpstreamHandler

      def messageReceived(context, event)
        message = event.message
        case message
          when Thick::Java::HttpRequest
            handle_request(context, message)
          when Thick::Java::WebSocketFrame
            handle_websocket_frame(context, message)
        end
      end

      def handle_request(context, request)
        url = "ws://" + request.get_header(Thick::Java::HttpHeaders::Names::HOST) + '/controller'
        factory = Thick::Java::WebSocketServerHandshakerFactory.new(url, nil, false)
        @handshaker = factory.new_handshaker(request)
        if @handshaker
          @handshaker.handshake(context, request)
        else
          factory.send_unsupported_webSocket_version_response(context)
        end
      end

      def handle_websocket_frame(context, frame)
        case frame
          when Thick::Java::CloseWebSocketFrame
            @handshaker.close(context, frame)
          when Thick::Java::PingWebSocketFrame
            context.channel.write(Thick::Java::PongWebSocketFrame.new(frame.binary_data))
          when Thick::Java::TextWebSocketFrame
            handle_websocket_message(context, frame.text)
        end
      end

      def websocket_send(context, message)
        context.channel.write(Thick::Java::TextWebSocketFrame.new(message.to_s))
      end

      def handle_websocket_message(context, message)

      end

    end

  end

end