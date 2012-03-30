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
      expect { @buffer.write('test') }.to raise_error(ArgumentError)
    end

  end

  describe 'buffer reads written data' do

    it 'reads data' do
      @buffer.write(buffer_from_string("some random\n data"))
      @buffer.read.should == "some random\n data"
      @buffer.read.should == ('')
    end

    it 'reads data by chunks' do
      @buffer.write(buffer_from_string('some random'))
      @buffer.write(buffer_from_string("\n"))
      @buffer.write(buffer_from_string('data'))

      @buffer.read(4).should == ('some')
      @buffer.read(1).should == (' ')
      @buffer.read(4).should == ('rand')
      @buffer.read(3).should == ("om\n")
      @buffer.read(4).should == ('data')
      @buffer.read(4).should == (nil)
    end

    it 'reads data by line' do
      @buffer.write(buffer_from_string('some random'))
      @buffer.write(buffer_from_string("\n"))
      @buffer.write(buffer_from_string('data'))

      @buffer.gets.should == ("some random\n")
      @buffer.gets.should == ("data")
      @buffer.gets.should == (nil)
    end

    it 'iterates data by line' do
      input = ["some random\n", 'data']
      input.each { |data| @buffer.write(buffer_from_string(data)) }

      output = []
      @buffer.each { |chunk| output << chunk }

      output.should == (input)
    end

  end

  describe 'buffer is rewindable' do

    it 'allows rewinding to the beginning' do
      @buffer.write(buffer_from_string('some random data are written'))

      @buffer.read(4).should == ('some')
      @buffer.rewind
      @buffer.read(11).should == ('some random')
    end

  end

end
