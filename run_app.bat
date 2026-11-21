@echo off
setlocal

echo ===================================================
echo   Building and Running MeetWith on Connected Device
echo ===================================================

cd /d "%~dp0"

:: Set JAVA_HOME from Android Studio
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ADB=C:\Users\ajayk\AppData\Local\Android\Sdk\platform-tools\adb.exe"

echo [1/3] Checking connected devices...
"%ADB%" devices

echo.
echo [2/3] Building and installing APK...
call gradlew.bat installDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Build or installation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Launching MeetWith on device...
"%ADB%" shell am start -n com.example.zoom_clone/.ui.SplashActivity

echo.
echo ===================================================
echo   App launched successfully!
echo ===================================================
pause
