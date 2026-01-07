@echo off
:: ========================================
:: ClassBuddy Clean Build & Start Script
:: ========================================

title ClassBuddy - Clean Build

echo.
echo ================================================
echo    ClassBuddy - Clean Build and Start
echo ================================================
echo.
echo [INFO] Performing clean build...
echo [INFO] This will download dependencies (first time only)
echo.

:: Check if Maven wrapper exists
if exist "mvnw.cmd" (
    echo [INFO] Using Maven Wrapper...
    echo.
    echo [STEP 1/3] Cleaning previous build...
    call mvnw.cmd clean
    
    echo.
    echo [STEP 2/3] Compiling project...
    call mvnw.cmd compile
    
    echo.
    echo [STEP 3/3] Starting ClassBuddy...
    call mvnw.cmd javafx:run
) else (
    echo [INFO] Using system Maven...
    echo.
    echo [STEP 1/3] Cleaning previous build...
    mvn clean
    
    echo.
    echo [STEP 2/3] Compiling project...
    mvn compile
    
    echo.
    echo [STEP 3/3] Starting ClassBuddy...
    mvn javafx:run
)

:: If Maven failed, provide helpful message
if errorlevel 1 (
    echo.
    echo ================================================
    echo [ERROR] Build or startup failed!
    echo ================================================
    echo.
    echo Common issues:
    echo 1. Java not found - Install Java 17 or higher
    echo 2. Maven not found - Install Maven or use mvnw.cmd
    echo 3. Dependency download failed - Check internet connection
    echo 4. Compilation errors - Check error messages above
    echo.
    echo Press any key to exit...
    pause > nul
)

exit /b
