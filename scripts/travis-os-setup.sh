#!/usr/bin/env bash
# For Scala Native 0.4.0+
# Ubuntu bionic (18.04)

# Enable strict mode and fail the script on non-zero exit code,
# unresolved variable or pipe failure.
set -euo pipefail
IFS=$'\n\t'

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew install re2
else
    # Per https://github.com/scala-native/scala-native/pull/1240/
    sudo apt-get update

    # Install Boehm GC and libunwind
    sudo apt-get install -y clang-6 zlib1g-dev libgc-dev libre2-dev
fi
