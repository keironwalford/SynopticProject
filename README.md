# MeshMessagingApp & nRF52840 DK Firmware

This repository contains:

- **MeshMessagingApp** — An Android application that enables messaging over Bluetooth Mesh.
- **nRF Firmware** — Custom firmware that must be flashed onto Nordic Semiconductor nRF52840 Development Kits (DKs) to relay messages through a Bluetooth Mesh network.

## MeshMessagingApp

The MeshMessagingApp is a Bluetooth Mesh chat application for Android.  
It can be run as a project directly through Android Studio.

### To run:
1. Clone this repository.
2. Open the `MeshMessagingApp` folder in Android Studio.
3. Build and deploy the app to an Android device running Android 8.0 (API level 26) or higher.
4. Make sure the Android device has Bluetooth enabled.

## nRF Firmware

The `nRF Firmware` folder contains firmware for the Bluetooth Mesh Chat Client designed for the nRF52840 DK.

The firmware includes:
- Mesh provisioning
- Message relaying

### Flashing Instructions:
1. Install nRF Connect for Desktop and add the Programmer app.
2. Compile the firmware using nRF Connect SDK.
3. Connect your nRF52840 DK via USB.
4. Open the Programmer app.
5. Select the connected device and flash the compiled HEX file.
