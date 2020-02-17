#!/usr/bin/env bash
# Similar to Scala Native updated to Xenial

# Enable strict mode and fail the script on non-zero exit code,
# unresolved variable or pipe failure.
set -euo pipefail
IFS=$'\n\t'

if [[ "$TRAVIS_OS_NAME" == "osx" ]]; then
    brew install re2
else
    # Per https://github.com/scala-native/scala-native/pull/1240/
    sudo apt-get update

    # Remove libunwind pre-bundled with clang
    sudo find /usr -name "*libunwind*" -delete

    # Use pre-bundled clang
    export PATH=/usr/local/clang-5.0.0/bin:$PATH
    export CXX=clang++

    # Install Boehm GC and libunwind
    sudo apt-get install libgc-dev libunwind8-dev libatlas-base-dev libre2-dev
fi
