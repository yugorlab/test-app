# Teyvat Terminal Android v0.3

A minimal offline Android WebView shell around Teyvat Script Terminal v0.3.

## Properties
- Works offline; no INTERNET permission.
- HTML/CSS/JS and the Teyvat font are bundled in `app/src/main/assets/index.html`.
- DOM storage is enabled, so the document and conversation log persist between app launches.
- Screen stays awake while the app is open.
- Native clipboard bridge is used by the terminal's Copy buttons.

## Build
Open this folder in Android Studio, let it install/sync Android SDK 35 and the Android Gradle Plugin, then use **Build > Build APK(s)**.

Application ID: `lab.yugor.teyvatterminal`
Minimum Android: 8.0 (API 26)
Target/compile SDK: 35

## Build on GitHub (no Android Studio required)
The project includes `.github/workflows/build-apk.yml`. Push the project to a GitHub repository, open the **Actions** tab, run **Build Android APK**, then download the `teyvat-terminal-v0.3-debug` artifact.

Teyvat Terminal — adaptive icon resources

Files:
- app/src/main/res/drawable/ic_launcher_foreground.png
- app/src/main/res/values/colors.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

How to use:
1. Copy the included app/src/main/res/... files into the same paths in your Android project.
2. Replace existing files when GitHub asks.
3. Commit the changes to main.
4. GitHub Actions should rebuild the APK automatically.
5. Install the new APK over the old one.

Notes:
- Background color is pure black (#000000).
- The supplied symbol was centered and reduced to fit a conservative adaptive-icon safe area.
- The foreground PNG remains transparent outside the symbol.

