@echo off
echo Installing WeSafe Debug APK...
echo.

REM Build the debug APK
echo Building APK...
gradlew assembleDebug

REM Check if build was successful
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build successful!
echo.

REM Try to install the APK (requires ADB in PATH)
echo Attempting to install APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% EQU 0 (
    echo.
    echo APK installed successfully!
    echo You can now test the WeSafe app on your device.
) else (
    echo.
    echo Could not install APK automatically.
    echo Please manually install: app\build\outputs\apk\debug\app-debug.apk
)

echo.
pause
