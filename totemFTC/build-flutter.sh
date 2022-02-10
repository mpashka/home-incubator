#!/bin/bash

file_bin=5d7m0ryfmsnbhcjs
root=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
mkdir -p $root/target

cd $root/flutter_fe
$root/flutter_fe/pre-build.sh
flutter pub run build_runner build
flutter build web --profile --dart-define=Dart2jsOptimization=O0
flutter build apk

cd $root/flutter_fe/build/web
mv $root/flutter_fe/build/web/index.html $root/flutter_fe/build/web/pwa.html
zip -r $root/target/flutter.zip *
curl --header "Content-Type: application/zip" --data-binary @$root/target/flutter.zip https://filebin.net/$file_bin/flutter.zip
curl --header "Content-Type: application/vnd.android.package-archive" --data-binary @$root/flutter_fe/build/app/outputs/flutter-apk/app-release.apk https://filebin.net/$file_bin/totemftc-release.apk
