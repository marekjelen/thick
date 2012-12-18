module Thick

  class Buffer

    java_import "io.netty.buffer.ByteBuf"
    java_import "io.netty.buffer.Unpooled"

    def initialize(buffer=nil)
      @buffer = buffer || Unpooled.buffer
    end

    def write(buffer)
      raise ArgumentError.new('ByteBuf is expected') unless buffer.kind_of?(ByteBuf)
      @buffer.writeBytes(buffer)
    end

    def rewind
      @buffer.reset_reader_index
    end

    def read(n=nil, buffer=nil)
      return n ? nil : '' unless @buffer.readable
      buffer ||= ""
      n ||= @buffer.readable_bytes
      buffer << java.lang.String.new(@buffer.read_bytes(n).array).to_s
    end

    def each(&block)
      while (data = gets)
        block.call( data || "")
      end
    end

    def gets
      return nil unless @buffer.readable
      nl = @buffer.bytes_before(10)
      if nl == -1
        nl = @buffer.readable_bytes
      else
        nl += 1
      end
      read(nl)
    end

  end

end