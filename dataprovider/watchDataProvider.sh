#!/bin/bash

./gradlew :dataprovider:test

# brew install fswatch before running below command
fswatch dataprovider/src/ | (while read; do ./gradlew :dataprovider:test; echo 'Watching for changes...' ; done)
