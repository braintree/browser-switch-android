
require 'rake'
require 'io/console'

task :default => :tests

desc "Run Android lint"
task :lint do
  sh "./gradlew clean lint"
end

desc "Run Android unit tests and tests on a device or emulator"
task :tests => [:unit_tests, :integration_tests]

desc "Run Android unit tests"
task :unit_tests => :lint do
  sh "./gradlew --continue test"
end

# TODO: run integration tests against older versions of Android (23 and lower)
# that do not support chrome custom tabs; and on newer versions of Android (24 and higher) that
# do support chrome custom tabs
desc "Run Android tests on a device or emulator"
task :integration_tests do
  output = `adb devices`
  if output.match(/device$/)
    sh "./gradlew --continue connectedAndroidTest"
  else
    puts "Please connect a device or start an emulator and try again"
    exit 1
  end
end

desc "Interactive release to publish new version"
task :release => :unit_tests do
  Rake::Task["assumptions"].invoke

  puts "What version are you releasing? (x.x.x format)"
  version = $stdin.gets.chomp

  update_version(version)
  update_readme_version(version)

  prompt_for_sonatype_username_and_password

  sh "./gradlew clean :browser-switch:publishToSonatype"
  sh "./gradlew closeAndReleaseRepository"

  post_release(version)
end

def post_release(version)
  puts "\nArchives are uploaded! Committing and tagging #{version} and preparing for the next development iteration"

  sh "git commit -am 'Release #{version}'"
  sh "git tag #{version} -a -m 'Release #{version}'"

  version_values = version.split('.')
  version_values[2] = version_values[2].to_i + 1
  update_version("#{version_values.join('.')}-SNAPSHOT")
  update_readme_snapshot_version(version_values.join('.'))
  increment_version_code
  sh "git commit -am 'Prepare for deployment'"

  puts "\nDone. Commits and tags have been created. If everything appears to be in order, hit ENTER to push."
  $stdin.gets

  sh "git push origin master #{version}"

  $stdin.gets
end

task :assumptions do
    puts "Release Assumptions"
    puts "* [ ] You are on the branch and commit you want to release."
    puts "* [ ] You have already merged hotfixes and pulled changes."
    puts "* [ ] You have already reviewed the diff between the current release and the last tag, noting breaking changes in the semver and CHANGELOG."
    puts "* [ ] Tests (rake integration_tests) are passing, manual verifications complete."

    puts "Ready to release? Press any key to continue. "
    $stdin.gets
end

def prompt_for_sonatype_username_and_password
  puts "Enter Sonatype username:"
  ENV["SONATYPE_USERNAME"] = $stdin.gets.chomp

  puts "Enter Sonatype password:"
  ENV["SONATYPE_PASSWORD"] = $stdin.noecho(&:gets).chomp
end

def increment_version_code
  new_build_file = ""
  File.foreach("browser-switch/build.gradle") do |line|
    if line.match(/versionCode (\d+)/)
      new_build_file += line.gsub(/versionCode \d+/, "versionCode #{$1.to_i + 1}")
    else
      new_build_file += line
    end
  end
  IO.write('browser-switch/build.gradle', new_build_file)
end

def update_version(version)
  IO.write("browser-switch/build.gradle",
    File.open("browser-switch/build.gradle") do |file|
      file.read.gsub(/version '\d+\.\d+\.\d+(-SNAPSHOT)?'/, "version '#{version}'")
    end
  )
end

def update_readme_version(version)
  IO.write("README.md",
    File.open("README.md") do |file|
      file.read.gsub(/:browser-switch:\d+\.\d+\.\d+'/, ":browser-switch:#{version}'")
    end
  )
end

def update_readme_snapshot_version(snapshot_version)
  IO.write("README.md",
    File.open("README.md") do |file|
      file.read.gsub(/:browser-switch:\d+\.\d+\.\d+-SNAPSHOT'/, ":browser-switch:#{snapshot_version}-SNAPSHOT'")
    end
  )
end

