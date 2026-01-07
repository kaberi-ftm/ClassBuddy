@echo off
:: ========================================
:: ClassBuddy Quick Start Script
:: ========================================

title ClassBuddy - Starting...

echo.
echo ================================================
echo    ClassBuddy - Classroom Management System
echo ================================================
echo.
echo [INFO] Starting ClassBuddy...
echo.

:: Check if Maven wrapper exists
if exist "mvnw.cmd" (
    echo [INFO] Using Maven Wrapper...
    call mvnw.cmd clean javafx:run
) else (
    echo [INFO] Using system Maven...
    mvn clean javafx:run
)

:: If Maven failed, provide helpful message
if errorlevel 1 (
    echo.
    echo ================================================
    echo [ERROR] Failed to start ClassBuddy!
    echo ================================================
    echo.
    echo Possible solutions:
    echo 1. Make sure Java 17+ is installed
    echo 2. Make sure Maven is installed
    echo 3. Check your internet connection for dependencies
    echo.
    echo Press any key to exit...
    pause > nul
)

exit /b
