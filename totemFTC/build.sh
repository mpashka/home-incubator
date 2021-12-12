#!/bin/bash

root=`pwd`
target="$root/target/web"
tmp="$root/target/tmp"
mkdir -p $target
mkdir -p $tmp

export DEMO_BUILD=true

cd $root/quasar-fe
npm install
./node_modules/.bin/quasar build
mv dist/spa/* $target
mv dist/spa/.well-known $target
# out: $root/target/web

#cd $root/flutter_fe
##flutter build web
#mv build/web/index.html build/web/flutter.html
#mv build/web/* $target
#mv build/web/icons/* $target/icons
#

cp $root/docs/demo/* $target/

curl https://cloclo58.cldmail.ru/public/get/7Y6pEVYEPvZNBBp6tcem3tU1kcpzsVnnxfYtHnax6PBsKA2eM2MjTrH68Af2GTvLtFrzTv/no/flutter.zip -o $tmp/flutter.zip
unzip -o $tmp/flutter.zip -d $target

#cd $root/target/web
#zip -r ../deploy.zip * .well-known/*
