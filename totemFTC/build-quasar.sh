#!/bin/bash

root=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
. "$root/file_bin.sh"
mkdir -p "$root/target"

export BUILD_HOSTNAME="$HOSTNAME"
deploy() { flavor=$1;
  cd "$root/quasar-fe"
  export RUN_PROFILE="$flavor"
  #npm install
  ./node_modules/.bin/quasar build || die "build node for $flavor"
  tar -czf "$root/target/quasar-$flavor.tar.gz" -C "$root/quasar-fe/dist/spa-$flavor/" .
  curl --header "Content-Type: application/tar+gzip" --data-binary "@$root/target/quasar-$flavor.tar.gz" "https://filebin.net/$file_bin/quasar-$flavor.tar.gz"
}

deploy "demo"
deploy "totem"
