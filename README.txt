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
