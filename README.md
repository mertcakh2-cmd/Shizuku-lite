# Shizuku Lite

A lightweight Android client for the official Shizuku service. The dashboard uses the official Shizuku API, displays service and permission state, and can send an optional branded status notification using the Shizuku Lite icon inspired by the supplied image.

## Features

- Connection and permission status dashboard.
- User-approved Shizuku permission request flow.
- Open Shizuku or visit its official setup/download pages.
- Optional status notification with a dedicated Android notification channel.
- No root request, arbitrary shell runner, or background command service.

## Setup

1. Install the official Shizuku app from https://shizuku.rikka.app/download/.
2. Start its service using the official setup guide: https://shizuku.rikka.app/guide/setup/.
3. Open Shizuku Lite and tap **Grant Shizuku access**.
4. Review the permission request in Shizuku.
5. Tap **Send status notification** to post a notification; Android 13+ will ask for notification permission if needed.

## Build

Open in Android Studio with Android SDK Platform 35 installed and build the `app` debug configuration. The debug APK is normally written to `app/build/outputs/apk/debug/`.

Shizuku Lite is a client of Shizuku, not a replacement for its service. The app icon is a vector recreation inspired by the image supplied in the project conversation.
