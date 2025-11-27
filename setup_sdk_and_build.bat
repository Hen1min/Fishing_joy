@echo off
REM setup_sdk_and_build.bat - trimmed version
REM Android APK build steps removed to keep this script focused on non-Android builds (e.g. Windows exe).

setlocal enabledelayedexpansion
set LOG=%CD%\full_auto_run_user.txt
echo === START %DATE% %TIME% > "%LOG%"
echo PROJECT_DIR=%CD% >> "%LOG%"
echo NOTE: Android APK build steps have been removed from this script. >> "%LOG%"
echo If you need to build an APK, use Android Studio or the original build steps in setup_sdk_and_build.bat.orig. >> "%LOG%"
echo === END %DATE% %TIME% >> "%LOG%"
echo Done. Android APK export steps removed. Logs written to full_auto_run_user.txt.
pause
