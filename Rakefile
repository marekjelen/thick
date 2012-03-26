lib = File.expand_path('../lib/', __FILE__)
$: << lib unless $:.include?(lib)

require 'thick/version'

desc "Build the gem"
task :build do
  puts `gem build thick.gemspec`
end

desc "Install the gem locally"
task :install => :build do
  puts `gem install thick-#{Thick::VERSION}.gem`
end