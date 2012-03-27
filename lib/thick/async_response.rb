module Thick

  class AsyncResponse

    def initialize(context)
      @context = context
      @ready = false
      @custom = false
      @async = false
      @block = nil
      @params = []
    end

    def call(chunk)
      @context.channel.write(Thick::Java::ChannelBuffers.copiedBuffer(chunk.to_s, Thick::Java::CharsetUtil::UTF_8))
    end

    def close
      @context.channel.write(Thick::Java::ChannelBuffers::EMPTY_BUFFER).addListener(Thick::Java::ChannelFutureListener::CLOSE)
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

  end

end