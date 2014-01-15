require 'rack'

require 'java'

java.lang.System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")

Dir[File.expand_path('../jars/*.jar', __FILE__)].each do |jar|
  require jar
end

require 'thick/java'
require 'thick/version'
require 'thick/thick'
require 'thick/loader'
require 'thick/buffer'
require 'thick/websocket'
