module Thick

  class PipelineFactory

    include Thick::Java::ChannelPipelineFactory

    def initialize(application)
      @application = application
      @executor = Thick::Java::ExecutionHandler.new(Thick::Java::OrderedMemoryAwareThreadPoolExecutor.new(20, 0, 0))
    end

    def getPipeline
      pipeline = Thick::Java::Channels.pipeline

      pipeline.add_last('http_decoder', Thick::Java::HttpRequestDecoder.new)
      pipeline.add_last('http_encoder', Thick::Java::HttpResponseEncoder.new)

      pipeline.add_last('decompressor', Thick::Java::HttpContentDecompressor.new)

      pipeline.add_last('chunked_writer', Thick::Java::ChunkedWriteHandler.new)

      pipeline.add_last('executor', @executor)

      pipeline.add_last('handler', ServerHandler.new(@application))

      pipeline
    end

  end

end