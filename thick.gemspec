lib = File.expand_path('../lib/', __FILE__)
$: << lib unless $:.include?(lib)

require 'thick/version'

Gem::Specification.new do |s|

  s.name        = "thick"
  s.version     = Thick::VERSION
  s.authors     = ["Marek Jelen"]
  s.email       = ["marek@jelen.biz"]
  s.homepage    = "http://github.com/marekjelen/thick"
  s.summary     = "Very lightweight web server for JRuby"
  s.description = "Very lightweight web server for JRuby based on Netty library."

  s.files        = Dir.glob("{bin,lib}/**/*") + %w(LICENSE README.md ROADMAP.md CHANGELOG.md)
  s.executables  = ['thick']
  s.require_path = 'lib'

  s.add_dependency 'rack', '1.4.1'

end