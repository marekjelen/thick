module Thick

  def self.start(options)
    @options = {
        :address => '0.0.0.0',
        :port => 9292,
        :environment => 'development',
        :reloader => false,
        :controller => false,
        :directory => Dir.getwd,
        :file => 'config.ru'
    }.merge(options)

    puts "* Starting Thick: #{@options.inspect}"

    @options[:application] = Loader.new(@options)
    @server = Server.new(@options).start

    if @options[:controller]
      @controller = Controller.new(@options, @server)
      @controller.start
    end

    loop do
      sleep(100) # ToDo: Is this hack needed?
    end
  end

end