#!/bin/bash

root=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
. "$root/file_bin.sh"

# For testing
#rm -rf $root/target
target="$root/target/web"
tmp="$root/target/tmp"
mkdir -p $target
mkdir -p $tmp

#export DEMO_BUILD=true

# RUN_PROFILE = {totem|demo}
echo "Build profile $RUN_PROFILE"

# Flutter
curl "https://filebin.net/$file_bin/flutter-${RUN_PROFILE}.tar.gz" | tar -xz -C "$target"

# Quasar
curl "https://filebin.net/$file_bin/quasar-${RUN_PROFILE}.tar.gz" | tar -xz -C "$target"

# Android apk
curl "https://filebin.net/$file_bin/android-${RUN_PROFILE}.apk" -L -o "$target/anroid.apk"

cp -R "$root/docs/www/." "$target/"
