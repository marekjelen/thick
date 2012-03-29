lib = File.expand_path('../lib/', __FILE__)
$: << lib unless $:.include?(lib)

require 'thick/version'

desc "Build the gem"
task :build do
  puts `gem build thick.gemspec`
end

desc "Install the gem locally"
task :install => :build do
  puts `gem install thick-#{Thick::VERSION}-java.gem`
end

desc "Release the gem"
task :release => :build do
  puts `gem push thick-#{Thick::VERSION}-java.gem`
end
