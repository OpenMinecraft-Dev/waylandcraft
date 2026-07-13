#!/usr/bin/env sh

cd native || exit
cargo build "$@"
cd ..
./gradlew build
