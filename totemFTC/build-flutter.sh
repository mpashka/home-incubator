#!/bin/bash

root=`pwd`
mkdir -p target

cd $root/flutter_fe
flutter build web

cd $root/flutter_fe/build/web
zip -r $root/target/flutter.zip *
