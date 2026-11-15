# Shizuku Lite

A lightweight Android client for the official Shizuku service. It includes a connection dashboard, setup links, and the real Shizuku permission request flow using the official Shizuku API. It does not bundle or impersonate the Shizuku service.

## Features

- Shows whether the Shizuku Binder service is connected.
- Shows whether this app has Shizuku permission.
- Requests access through Shizuku's own user-confirmed permission dialog.
- Links to the installed Shizuku app, official download, and setup guide.
- Uses no root request, arbitrary shell runner, or background command service.

## Setup

1. Install the official Shizuku app from https://shizuku.rikka.app/download/.
2. Start its service using the official setup guide: https://shizuku.rikka.app/guide/setup/.
3. Open Shizuku Lite and tap **Grant Shizuku access**.
4. Review the permission request in Shizuku.

On non-rooted devices, the service is started using ADB or Wireless debugging and may need to be restarted after a reboot. Android does not allow an ordinary app to grant itself ADB-shell privileges.

## Build

Open in Android Studio with Android SDK Platform 35 installed and build the `app` debug configuration. The debug APK is normally written to `app/build/outputs/apk/debug/`.

This app is a client of Shizuku, not a Shizuku replacement. Its integration uses the official `dev.rikka.shizuku:api` and `dev.rikka.shizuku:provider` libraries.
