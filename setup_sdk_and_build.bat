@echo off
REM setup_sdk_and_build.bat
REM Download Android command-line tools to a project-local SDK, install packages, accept licenses, and run Gradle assembleDebug.
REM Logs: full_auto_run_user.txt will contain full stdout/stderr.

setlocal enabledelayedexpansion
set LOG=%CD%\full_auto_run_user.txt
necho === START %DATE% %TIME% > "%LOG%"
necho PROJECT_DIR=%CD% >> "%LOG%"
necho SDKROOT=%CD%\android-sdk >> "%LOG%"

n:: Create SDK folder
nif not exist "%CD%\android-sdk" mkdir "%CD%\android-sdk" >> "%LOG%" 2>&1
n
n:: Download command-line tools zip into the SDK folder
necho Downloading command-line tools... >> "%LOG%"
powershell -NoProfile -Command "Try { Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-latest.zip' -OutFile '%CD%\android-sdk\commandlinetools.zip' -UseBasicParsing -ErrorAction Stop; Write-Output 'DOWNLOAD_OK' } Catch { Write-Output 'DOWNLOAD_FAILED: ' + $_.Exception.Message; Exit 1 }" >> "%LOG%" 2>&1

n:: Extract
necho Extracting... >> "%LOG%"
powershell -NoProfile -Command "Try { Remove-Item -Recurse -Force '%CD%\android-sdk\cmdline-tools\tmp' -ErrorAction SilentlyContinue; Expand-Archive -LiteralPath '%CD%\android-sdk\commandlinetools.zip' -DestinationPath '%CD%\android-sdk\cmdline-tools\tmp' -Force; Write-Output 'EXTRACT_OK' } Catch { Write-Output 'EXTRACT_FAILED: ' + $_.Exception.Message; Exit 2 }" >> "%LOG%" 2>&1

n:: Normalize layout to cmdline-tools\latest
necho Normalizing cmdline-tools layout... >> "%LOG%"
powershell -NoProfile -Command "$tmp = Join-Path '%CD%\android-sdk' 'cmdline-tools\tmp'; $inner = Get-ChildItem -Path $tmp -Directory -ErrorAction SilentlyContinue | Select-Object -First 1; if($inner) { robocopy $inner.FullName '%CD%\android-sdk\cmdline-tools\latest' /E /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null } else { robocopy $tmp '%CD%\android-sdk\cmdline-tools\latest' /E /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null }" >> "%LOG%" 2>&1
n
necho Listing latest\bin >> "%LOG%"
if exist "%CD%\android-sdk\cmdline-tools\latest\bin" (
  dir "%CD%\android-sdk\cmdline-tools\latest\bin" /b >> "%LOG%" 2>&1
) else (
  echo NO_BIN_FOLDER >> "%LOG%"
)

n:: Find sdkmanager
necho Locating sdkmanager... >> "%LOG%"
where /r "%CD%\android-sdk" sdkmanager.bat >> "%LOG%" 2>&1 || echo SDKMANAGER_NOT_FOUND >> "%LOG%"

nset SDKMAN=%CD%\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat
nif not exist "%SDKMAN%" (
  echo sdkmanager not found at %SDKMAN% >> "%LOG%"
  echo ERROR: sdkmanager not found. Check %CD%\android-sdk\cmdline-tools\latest\bin >> "%LOG%"
  echo See RUN_ANDROID_BUILD.md for manual steps.
  exit /b 3
)

n:: Install required packages
necho Installing platform-tools, platforms;android-33, build-tools;33.0.2... >> "%LOG%"
"%SDKMAN%" --sdk_root="%CD%\android-sdk" "platform-tools" "platforms;android-33" "build-tools;33.0.2" >> "%LOG%" 2>&1
nif errorlevel 1 (
  echo sdkmanager install returned error >> "%LOG%"
  echo Check network, proxy, or run RUN_ANDROID_BUILD.md manually. >> "%LOG%"
  exit /b 4
)

n:: Accept licenses
necho Accepting licenses... >> "%LOG%"
echo y| "%SDKMAN%" --sdk_root="%CD%\android-sdk" --licenses >> "%LOG%" 2>&1
n
n:: Run Gradle build
necho Running gradlew assembleDebug... >> "%LOG%"
"%CD%\gradlew.bat" assembleDebug --no-daemon --stacktrace >> "%LOG%" 2>&1
nif errorlevel 1 (
  echo Gradle build failed, see full_auto_run_user.txt for details >> "%LOG%"
  echo Gradle build failed. Check %CD%\full_auto_run_user.txt >> "%LOG%"
  exit /b 5
)
n
necho BUILD SUCCESS. APK likely at android\build\outputs\apk\debug >> "%LOG%"
echo === END %DATE% %TIME% >> "%LOG%"

echo Done. Logs written to full_auto_run_user.txt. If errors occurred, open that file and paste its contents here.
pause

