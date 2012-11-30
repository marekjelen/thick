module Thick

  def self.create(options)
    options = {
        :address => '0.0.0.0',
        :port => 9292,
        :environment => 'development',
        :directory => Dir.getwd,
        :file => 'config.ru'
    }.merge(options)

    puts "* Starting Thick: #{options.inspect}"

    env = Thick::Java::ServerEnvironment.new
    env.address = options[:address]
    env.port = options[:port]

    env.application = Loader.new(options)
    Thick::Java::Server.new(env).start

  end

end