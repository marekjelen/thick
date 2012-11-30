lib = File.expand_path('../lib/', __FILE__)
$: << lib unless $:.include?(lib)

require 'rubygems'
require 'bundler/setup'
require 'rspec/core/rake_task'

require 'thick/version'
require 'fileutils'

desc 'Default: run tests.'
task :default => :test

namespace :java do

  desc "Build java part"
  task :build do
    Dir.chdir("java")
    puts `~/bin/maven/bin/mvn package`
    Dir.chdir("..")
    FileUtils.copy('java/target/thick-0.0.1.jar', 'lib/jars/thick-0.0.1.jar')
  end

end

namespace :gem do

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

end

desc "Run tests"
RSpec::Core::RakeTask.new(:test) do |t|
  t.pattern = "./test/**/*_spec.rb"
end