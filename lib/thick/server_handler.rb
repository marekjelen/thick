module Thick

  class ServerHandler < Thick::Java::SimpleChannelUpstreamHandler

    def initialize(options)
      super()
      @options = options
    end

    def channelConnected(context, event)
    end

    def messageReceived(context, event)
      request = event.message
      case request
        when Thick::Java::HttpRequest
          on_request(context, request)
        when Thick::Java::HttpChunk
          on_chunk(context, request)
      end
    end

    def serve_file(context, file, from = 0, to = nil)
      raf = Thick::Java::RandomAccessFile.new(file, "r")
      to ||= File.size(file)
      region = Thick::Java::DefaultFileRegion.new(raf.channel, from, to)
      context.channel.write(region)
    end

    def on_request(context, request)

      # Serve file if exists in public directory
      file = File.join(@options[:directory], 'public', request.uri)
      return serve_file(context, file).addListener(Thick::Java::ChannelFutureListener::CLOSE) if !request.uri.index('..') && File.exists?(file) && !File.directory?(file)

      query_string = request.uri.split('?', 2)
      @env = {
          # Rack specific
          'rack.version' => [1,1],
          'rack.url_scheme' => 'http', # ToDo: support https?
          'rack.input' => Buffer.new,
          'rack.errors' => $stdout, # ToDo: Sure about that?
          'rack.multithread' => true,
          'rack.multiprocess' => false,
          'rack.run_once' => false,
          # HTTP specific
          'REQUEST_METHOD' => request.get_method.name,
          'SCRIPT_NAME' => '',
          'PATH_INFO' => query_string[0],
          'QUERY_STRING' => if query_string[1] && query_string[1] != '' then "?#{query_string[1]}" else '' end,
          'SERVER_NAME' => 'localhost', # ToDo: Be more precise!
          'SERVER_PORT' => '8080', # ToDo: Be more precise!
          # Thick specific
          'thick.async' => AsyncResponse.new(context),
          'thick.options' => @options
      }

      # Get content length if available
      length = Thick::Java::HttpHeaders.get_content_length(request)
      @env['CONTENT_LENGTH'] = length if length > 0

      # Extract headers and normalize it's names
      request.header_names.each do |name|
        rack_name = name.gsub('-', '_').upcase
        rack_name = "HTTP_#{rack_name}" unless %w(CONTENT_TYPE CONTENT_LENGTH).include?(rack_name)
        @env[rack_name] = request.get_header(name)
      end

      # Write request content into prepared IO
      @env['rack.input'].write(request.content)

      # No chunks expected, handle request
      handle_request(context) unless request.chunked?
    end

    def on_chunk(context, chunk)
      # Write request content into prepared IO
      @env['rack.input'].write(chunk.content)

      # No more chunks expected, handle request
      handle_request(context) if chunk.last?
    end

    def handle_request(context)
      response = @options[:application].call(@env)

      # Let the application make the response as it wants
      if @env['thick.async'].custom?
        @env['thick.async'].ready!
        return
      end

      # Unpack response
      status, headers, content = response

      # Create response and set status as requested by application
      @response = Thick::Java::DefaultHttpResponse.new(Thick::Java::HttpVersion::HTTP_1_1, Thick::Java::HttpResponseStatus.value_of(status.to_i))

      # Set headers as requested by application
      headers.each do |name, value|
        Thick::Java::HttpHeaders.set_header(@response, name, value)
      end

      if @env['thick.async'].async?
        if length = @env['thick.async'].content_length
          Thick::Java::HttpHeaders.set_content_length(@response, length)
        else
          @response.remove_header('Content-Length')
        end
        @response.chunked = true if @env['thick.async'].chunked?
      end

      context.channel.write(@response)

      # Set content as requested by application
      case
        when content.respond_to?(:to_path)
          serve_file(context, content.to_path)
        else
          content.each do |chunk|
            context.channel.write(Thick::Java::ChannelBuffers.copiedBuffer(chunk, Thick::Java::CharsetUtil::UTF_8))
          end
      end

      # When content is closable, close it
      content.close if content.respond_to?(:close)

      # Trigger async handler and marker if async response requested, close connection otherwise
      if @env['thick.async'].async?
        @env['thick.async'].ready!
      else
        @env['thick.async'].close
      end
    end

    def channelClosed(context, event)
      @env['thick.async'].closed!
    end

    def exceptionCaught(context, e)
      puts e.message if e.respond_to?(:message)
      puts e.backtrace if e.respond_to?(:backtrace)
    end

  end

end