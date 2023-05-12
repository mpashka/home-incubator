emulator -list-avds
emulator -avd {emulator_name} -writable-system
emulator -avd 7.0_x86 -verbose -writable-system -selinux disabled
adb disable-verity
adb reboot
adb root
#adb shell stop && adb shell start

???
android update sdk --no-ui
