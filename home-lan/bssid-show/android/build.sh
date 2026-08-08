#!/bin/bash
# Сборка APK штатными утилитами Android SDK, без Gradle.
#
# ponytail: приложение — три активности и провайдер виджета, ради него не стоит тянуть
# Gradle с AGP (их ещё и надо совместить с JDK 25, который тут единственный). Потолок:
# нет инкрементальной сборки и интеграции с Android Studio. Апгрейд — обычный Gradle-проект,
# если понадобится отладчик из IDE.
#
#   ./build.sh          собрать build/app.apk
#   ./build.sh install  собрать и поставить на подключённый по adb телефон
#   ./build.sh receive  принять один CSV замеров с телефона
#   ./build.sh check    прогнать самопроверки карты BSSID→комната и HTTP-приёмника

set -euo pipefail
cd "$(dirname "$0")"

if [ "${1:-}" = receive ]; then
    shift
    exec ./receive-survey.py "$@"
fi

SDK=${ANDROID_HOME:-/opt/java/impl/android-sdk}
BT=$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)
PLATFORM="$SDK/platforms/android-36/android.jar"
# Не $JAVA_HOME: на этой машине он показывает на JDK 8, а нужен 17+. Берём java из PATH.
JDK=${ANDROID_JDK:-$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")}
MIN_SDK=29
TARGET_SDK=36
OUT=build

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes"

"$BT/aapt2" compile --dir app/res -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/unsigned.apk" -I "$PLATFORM" \
    --manifest app/AndroidManifest.xml --java "$OUT/gen" \
    --min-sdk-version $MIN_SDK --target-sdk-version $TARGET_SDK \
    "$OUT/res.zip"

# android.jar на classpath, java.* берутся из JDK — так же делает и AGP.
"$JDK/bin/javac" -source 17 -target 17 -nowarn -encoding UTF-8 \
    -cp "$PLATFORM" -d "$OUT/classes" \
    $(find app/java "$OUT/gen" -name '*.java')

if [ "${1:-}" = check ]; then
    "$JDK/bin/java" -cp "$OUT/classes" org.mpashka.bssidshow.Rooms
    exec python3 ./receive-survey.py --check
fi

"$BT/d8" --min-api $MIN_SDK --lib "$PLATFORM" --output "$OUT" \
    $(find "$OUT/classes" -name '*.class')

(cd "$OUT" && zip -q unsigned.apk classes.dex)

KS=$OUT/../debug.keystore
if [ ! -f "$KS" ]; then
    "$JDK/bin/keytool" -genkeypair -keystore "$KS" -storepass android -keypass android \
        -alias key -keyalg RSA -keysize 2048 -validity 10000 -dname CN=bssid-show
fi

"$BT/zipalign" -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
    --out "$OUT/app.apk" "$OUT/aligned.apk"
rm -f "$OUT/aligned.apk" "$OUT/unsigned.apk" "$OUT/res.zip"
echo "готово: $OUT/app.apk"

if [ "${1:-}" = install ]; then
    adb install -r "$OUT/app.apk"
fi
