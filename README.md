# ScreenLoop Auto Clicker — Android project source

This is a starter Android Studio project for a user-configured tap-and-swipe accessibility tool.

## Current features
- AccessibilityService gesture dispatch for taps and vertical swipes.
- Floating control panel: Mark point, Test swipe, Start, Pause/Resume, Stop, Hide.
- Saves marked coordinates and settings in app SharedPreferences.
- Configurable target cycle count, swipe start/end percentages, and post-swipe wait.
- Uses the device display bounds at runtime for percentage-based swipe calibration.
- Stops automatically after the configured number of cycles.

## Build an APK
1. Install Android Studio with Android SDK Platform 35 and a compatible JDK.
2. Open this folder as a project and allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install the debug APK on your own Android device.
5. In Android settings, explicitly enable the ScreenLoop accessibility service and allow display over other apps.

## Important notes
- This environment did not have Android SDK/Gradle installed, so an APK binary has not been compiled here. The included project source is intended to be opened and built in Android Studio.
- The initial swipe endpoints are 85% and 10%, which produce a 75%-of-height swipe. A literal 90%-of-height swipe needs endpoints 90 percentage points apart (for example 95% to 5%). The app exposes endpoints for device calibration.
- Android gesture coordinates and usable screen bounds can vary by OS version, navigation mode, and overlays. Use Test swipe first and calibrate.
- Automation may be blocked or prohibited by some games/apps. Use only where permitted; this tool does not bypass anti-cheat or app protections.
- This is an initial project, not a production-hardened release. Test carefully before unattended use.

## Build without installing Android Studio (GitHub Actions)
1. Create a GitHub repository and upload the contents of this `ScreenLoop` folder (the `app`, Gradle files, and `.github` folder must be at the repository root).
2. Open the repository's **Actions** tab and select **Build ScreenLoop APK**.
3. Choose **Run workflow** if it has not run automatically, then open the completed run.
4. Under **Artifacts**, download `ScreenLoop-debug-apk` and extract it. The APK inside is `app-debug.apk`.
5. Transfer the APK to your Android phone and install it. Android may ask you to allow installation from that source.

This cloud workflow compiles the project; it does not mean an APK has already been built in this chat. The output is a debug APK for testing, not a Play Store release.
