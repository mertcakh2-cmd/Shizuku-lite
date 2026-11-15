# Shizuku Lite

A small Android starter app for a lightweight, user-authorized Shizuku companion. This first version is an onboarding screen: it links to the official Shizuku app and setup guide, and clearly reports that it does not provide a privileged shell.

## Why it doesn't run `adb shell`

An ordinary Android app cannot grant itself ADB-shell privileges. Shizuku's model requires the device owner to start its service through an authorized setup path, after which apps must integrate with the service and request user-granted access. This project intentionally does not implement a shell broker or arbitrary command runner.

## Build

Open this project in Android Studio (with Android SDK Platform 35 installed), then build the `app` configuration. The APK output is normally under `app/build/outputs/apk/debug/` for a debug build.

## Next step

To add a real client integration, use Shizuku's published API and documentation, request access explicitly, and expose narrowly scoped, auditable operations. Do not claim shell access unless the service is actually connected and authorized.

- Shizuku setup guide: https://shizuku.rikka.app/guide/setup/
- Shizuku project: https://github.com/RikkaApps/Shizuku
