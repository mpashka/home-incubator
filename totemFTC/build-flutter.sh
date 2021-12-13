#!/bin/bash

file_bin=0t0hab6flpav3eet
root=`pwd`
mkdir -p target

cd $root/flutter_fe
flutter build web
flutter build apk

cd $root/flutter_fe/build/web
zip -r $root/target/flutter.zip *
curl --header "Content-Type: application/zip" --data-binary @$root/target/flutter.zip https://filebin.net/$file_bin/flutter.zip
curl --header "Content-Type: application/vnd.android.package-archive" --data-binary @$root/flutter_fe/build/app/outputs/flutter-apk/app-release.apk https://filebin.net/$file_bin/totemftc-release.apk
