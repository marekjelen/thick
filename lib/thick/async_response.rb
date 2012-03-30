module Thick

  class AsyncResponse

    def initialize(context)
      @context = context
      @ready = false
      @custom = false
      @async = false
      @block = nil
      @chunked = false
      @content_length = nil
      @closed = false
      @params = []
    end

    def call(chunk)
      chunk = Thick::Java::ChannelBuffers.copiedBuffer(chunk.to_s, Thick::Java::CharsetUtil::UTF_8)
      chunk = Thick::Java::DefaultHttpChunk.new(chunk) if @chunked
      @context.channel.write(chunk) unless closed?
    end

    def close
      if @chunked
        chunk = Thick::Java::ChannelBuffers.copiedBuffer("0\r\n\r\n", Thick::Java::CharsetUtil::UTF_8)
      else
        chunk = Thick::Java::ChannelBuffers::EMPTY_BUFFER
      end
      @context.channel.write(chunk).addListener(Thick::Java::ChannelFutureListener::CLOSE) unless closed?
      closed!
    end

    def ready!
      @ready = true
      @block.call(*@params) if @block
    end

    def ready?
      @ready
    end

    def custom!(*params, &block)
      @custom = true
      @block = block
      @params = params
    end

    def custom?
      @custom
    end

    def async!(*params, &block)
      @async = true
      @block = block
      @params = params
    end

    def async?
      @async
    end

    def chunked!
      @chunked = true
    end

    def chunked?
      @chunked
    end

    def content_length!(length)
      @content_length = length
    end

    def content_length
      @content_length
    end

    def closed!
      @closed = true
    end

    def closed?
      @closed
    end

  end

end