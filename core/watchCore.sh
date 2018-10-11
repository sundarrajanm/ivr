#!/bin/bash

./gradlew :core:test

# brew install fswatch before running below command
fswatch . -e ".*" -i "\\.java$"  | (while read; do ./gradlew :core:test; echo 'Watching for changes...' ; done)
