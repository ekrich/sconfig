#!/usr/bin/env bash
# For Scala Native 0.4.0+
# macOS or Linux - Ubuntu bionic (18.04)

# Enable strict mode and fail the script on non-zero exit code,
# unresolved variable or pipe failure.
set -euo pipefail
IFS=$'\n\t'

# Install for Boehm GC is optional
if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew install bdw-gc
else
    # Per https://github.com/scala-native/scala-native/pull/1240/
    sudo apt-get update

    # Install libraries
    sudo apt-get install -y clang-6.0 zlib1g-dev libgc-dev
fi
