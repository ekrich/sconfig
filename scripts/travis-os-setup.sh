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

    # Remove pre-bundled libunwind
    sudo find /usr -name "*libunwind*" -delete

    # Install Boehm GC and libunwind
    sudo apt-get install -y clang-5.0 zlib1g-dev libgc-dev libunwind8-dev libre2-dev
fi
