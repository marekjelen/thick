module Thick

  class Buffer

    def initialize
      @buffers = []
      @buffer = nil
      @io = nil
    end

    def io
      @buffer ||= Thick::Java::ChannelBuffers.wrapped_buffer(*@buffers)
      @io ||= Thick::Java::ChannelBufferInputStream.new(@buffer).to_io.binmode
    end

    def write(data)
      raise ArgumentError unless data.kind_of?(Thick::Java::ChannelBuffer)
      @buffers << data
      @buffer = nil
      @io = nil
    end

    def gets
      self.io.gets
    end

    def read(length = nil, buffer = nil)
      buffer ? self.io.read(length, buffer) : self.io.read(length)
    end

    def each(&block)
      self.io.each(&block)
    end

    def rewind
      @buffer.reader_index(0) if @buffer
      @io = nil
    end

  end

end