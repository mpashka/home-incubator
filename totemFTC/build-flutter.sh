#!/bin/bash

root=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
flutter="$root/flutter_fe"
. "$root/file_bin.sh"
mkdir -p "$root/target"

deploy() { flavor=$1;
  echo "Deploy flutter flavor $flavor"

  [ -d "$root/target/flutter/$flavor" ] && rm -rf "$flutter/build/app" && mv "$root/target/flutter/$flavor" "$flutter/build/app"
  cd "$flutter"
  export RUN_PROFILE=$flavor
  "$flutter/pre-build.sh"
  flutter build apk --flavor "$flavor" || die "generate android $flavor"
  # We also have app-$flavor-debug.apk
  curl --header "Content-Type: application/vnd.android.package-archive" --data-binary "@$flutter/build/app/outputs/flutter-apk/app-$flavor-release.apk" "https://filebin.net/$file_bin/android-$flavor.apk"
  rm -rf "$root/target/flutter/$flavor"
  mkdir -p "$root/target/flutter"
  mv "$flutter/build/app" "$root/target/flutter/$flavor"

  cd "$flutter/build/web"
  cp "$flutter/assets/config/build-info.yaml" "$flutter/build/web/assets/assets/config/"
  tar -czf "$root/target/flutter-$flavor.tar.gz" \
      --exclude assets/assets/config/build-info.yaml \
      --transform 's/^.\///' \
      --transform 's/^index.html$/pwa.html/' \
      --transform "s/^build-info.yaml$/assets\/assets\/config\/build-info.yaml/" \
      -C "$flutter/build/web/" . -C "$flutter/assets/config/" "build-info.yaml"
  curl --header "Content-Type: application/tar+gzip" --data-binary "@$root/target/flutter-$flavor.tar.gz" "https://filebin.net/$file_bin/flutter-$flavor.tar.gz"
}

cd "$flutter"
#flutter clean
flutter pub run build_runner build --delete-conflicting-outputs || die "generate json serializer"
flutter build web --profile --dart-define=Dart2jsOptimization=O0 || die "generate web"

deploy demo
deploy totem
