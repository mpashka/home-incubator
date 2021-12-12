#!/bin/bash

root=`pwd`
# For testing
#rm -rf $root/target
target="$root/target/web"
tmp="$root/target/tmp"
mkdir -p $target
mkdir -p $tmp

export DEMO_BUILD=true

cd $root/quasar-fe
npm install
./node_modules/.bin/quasar build
# out: $root/target/web

# Build flutter
#cd $root/flutter_fe
##flutter build web
#mv build/web/index.html build/web/flutter.html
#mv build/web/* $target
#mv build/web/icons/* $target/icons

# Download flutter
#curl https://cloclo58.cldmail.ru/public/get/7Y6pEVYEPvZNBBp6tcem3tU1kcpzsVnnxfYtHnax6PBsKA2eM2MjTrH68Af2GTvLtFrzTv/no/flutter.zip -o $tmp/flutter.zip
curl https://filebin.net/0t0hab6flpav3eet/flutter.zip -L -o $tmp/flutter.zip
unzip -o $tmp/flutter.zip -d $target
mv $target/index.html $target/flutter.html

# Copy quasar & demo
cp -R -t $target $root/quasar-fe/dist/spa/. $root/docs/demo/.

#cd $root/target/web
#zip -r ../deploy.zip * .well-known/*
