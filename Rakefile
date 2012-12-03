lib = File.expand_path('../lib/', __FILE__)
$: << lib unless $:.include?(lib)

require 'rspec/core/rake_task'

require 'thick/version'
require 'fileutils'

desc 'Default: run tests.'
task :default => :test

namespace :java do

  desc "Build java part"
  task :build do
    Dir.chdir(File.expand_path("../java", __FILE__))
    puts `mvn package`
    Dir.chdir(File.dirname(__FILE__))
    FileUtils.copy('java/target/thick-0.0.1.jar', 'lib/jars/')
  end

end

namespace :gem do

  desc "Build the gem"
  task :build => 'java:build' do
    Dir.chdir(File.expand_path("..", __FILE__))
    puts `gem build thick.gemspec`
  end

  desc "Install the gem locally"
  task :install => :build do
    Dir.chdir(File.expand_path("..", __FILE__))
    puts `gem install thick-#{Thick::VERSION}-java.gem`
  end

  desc "Release the gem"
  task :release => :build do
    Dir.chdir(File.expand_path("..", __FILE__))
    puts `gem push thick-#{Thick::VERSION}-java.gem`
  end

end

desc "Run tests"
RSpec::Core::RakeTask.new(:test) do |t|
  t.pattern = File.expand_path("../test/**/*_spec.rb", __FILE__)
end
