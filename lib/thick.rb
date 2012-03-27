require 'rack'

require 'java'

require File.expand_path('../jars/netty-3.4.0.Alpha2.jar', __FILE__)

require 'thick/java'
require 'thick/version'
require 'thick/buffer'
require 'thick/server_handler'
require 'thick/pipeline_factory'
require 'thick/server'