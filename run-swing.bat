@echo off
setlocal enabledelayedexpansion
echo ==============================================
echo CampusToCareer - Swing Desktop App Launcher
echo ==============================================

set LIB_DIR=swing-lib
set SRC_DIR=src\main\java\com\rit\placement
set OUT_DIR=swing-out

if not exist %LIB_DIR% mkdir %LIB_DIR%
if not exist %OUT_DIR% mkdir %OUT_DIR%

echo [1/3] Checking dependencies...
if not exist "%LIB_DIR%\mysql-connector-j-8.3.0.jar" (
    echo Downloading MySQL Connector J...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile '%LIB_DIR%\mysql-connector-j-8.3.0.jar'"
)

echo [2/3] Compiling Java source files...
set CP="%LIB_DIR%\*"

javac -d %OUT_DIR% -cp %CP% ^
  %SRC_DIR%\model\User.java ^
  %SRC_DIR%\util\DBConnection.java ^
  %SRC_DIR%\dao\UserDAO.java ^
  %SRC_DIR%\swing\SwingApp.java ^
  %SRC_DIR%\swing\LoginPanel.java ^
  %SRC_DIR%\swing\DashboardPanel.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Compilation Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [3/3] Launching Swing Application...

REM SET DATABASE ENVIRONMENT VARIABLES HERE (Fallback values if not set)
if "%DB_URL%"=="" set DB_URL=jdbc:mysql://localhost:3306/placement_system
if "%DB_USER%"=="" set DB_USER=root
if "%DB_PASSWORD%"=="" set DB_PASSWORD=Akshay@2006
REM The DB_PASSWORD fallback is changed to 'Akshay@2006' based on docker-compose.yml MYSQL_ROOT_PASSWORD for local testing

echo Using DB_URL: %DB_URL%
java -cp "%OUT_DIR%;%LIB_DIR%\*" com.rit.placement.swing.SwingApp

pause
