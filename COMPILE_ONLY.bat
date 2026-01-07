@echo off
:: ========================================
:: ClassBuddy Compile Only Script
:: ========================================

title ClassBuddy - Compile Only

echo.
echo ================================================
echo    ClassBuddy - Compile Project
echo ================================================
echo.
echo [INFO] Compiling without running...
echo.

:: Requires Java 21+

:: Check if Maven wrapper exists
if exist "mvnw.cmd" (
    echo [INFO] Using Maven Wrapper...
    call mvnw.cmd clean compile
) else (
    echo [INFO] Using system Maven...
    mvn clean compile
)

if errorlevel 1 (
    echo.
    echo ================================================
    echo [ERROR] Compilation failed!
    echo ================================================
    echo.
    echo Please check the error messages above.
    echo.
) else (
    echo.
    echo ================================================
    echo [SUCCESS] Compilation completed successfully!
    echo ================================================
    echo.
    echo You can now run START.bat to launch the app.
    echo.
)

echo Press any key to exit...
pause > nul
exit /b
