#!/bin/bash

set -e

echo "Make sure to update the version in build.gradle and the README and add a CHANGELOG entry"
echo "Press enter when you are ready to release."
read

echo "Enter Sonatype username:"
read username
export SONATYPE_USERNAME=$(echo "${username}")

echo "Enter Sonatype password:"
read -s password
export SONATYPE_PASSWORD=$(echo "${password}")

./gradlew --info clean lint test
./gradlew :browser-switch:uploadArchives :browser-switch:closeAndPromoteRepository

echo "Release complete. Be sure to commit, tag and push your changes."
echo "After the tag has been pushed, update the releases tab on GitHub with the changes for this release."
echo "\n"
read
