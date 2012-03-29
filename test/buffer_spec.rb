require File.expand_path('../helper', __FILE__)

describe Thick::Buffer do

  before do
    @buffer = Thick::Buffer.new
  end

  def buffer_from_string(data)
    Thick::Java::ChannelBuffers.copied_buffer(data, Thick::Java::CharsetUtil::UTF_8)
  end

  describe 'buffer accepts Netty buffers as input' do

    it 'writes Netty buffer into the buffer' do
      @buffer.write(buffer_from_string('some random data are written'))
    end

    it 'fails when string is being written into buffer' do
      proc { @buffer.write('test') }.must_raise(ArgumentError)
    end

  end

  describe 'buffer reads written data' do

    it 'reads data' do
      @buffer.write(buffer_from_string("some random\n data"))
      @buffer.read.must_equal("some random\n data")
      @buffer.read.must_equal('')
    end

    it 'reads data by chunks' do
      @buffer.write(buffer_from_string('some random'))
      @buffer.write(buffer_from_string("\n"))
      @buffer.write(buffer_from_string('data'))

      @buffer.read(4).must_equal('some')
      @buffer.read(1).must_equal(' ')
      @buffer.read(4).must_equal('rand')
      @buffer.read(3).must_equal("om\n")
      @buffer.read(4).must_equal('data')
      @buffer.read(4).must_equal(nil)
    end

    it 'reads data by line' do
      @buffer.write(buffer_from_string('some random'))
      @buffer.write(buffer_from_string("\n"))
      @buffer.write(buffer_from_string('data'))

      @buffer.gets.must_equal("some random\n")
      @buffer.gets.must_equal("data")
      @buffer.gets.must_equal(nil)
    end

    it 'iterates data by line' do
      input = ["some random\n", 'data']
      input.each { |data| @buffer.write(buffer_from_string(data)) }

      output = []
      @buffer.each { |chunk| output << chunk }

      output.must_equal(input)
    end

  end

  describe 'buffer is rewindable' do

    it 'allows rewinding to the beginning' do
      @buffer.write(buffer_from_string('some random data are written'))

      @buffer.read(4).must_equal('some')
      @buffer.rewind
      @buffer.read(11).must_equal('some random')
    end

  end

end
