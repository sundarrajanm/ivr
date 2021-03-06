#!/usr/bin/env bash

./gradlew clean test

# brew install fswatch before running below command
fswatch . -e ".*" -i "\\.java$" | (while read; do ./gradlew test; echo 'Watching for changes...' ; done)