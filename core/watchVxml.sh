#!/bin/bash

./gradlew :vxml:test

# brew install fswatch before running below command
fswatch vxml/src/ | (while read; do ./gradlew :vxml:test; echo 'Watching for changes...' ; done)
