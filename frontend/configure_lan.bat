@echo off
title ProcureFlow Client LAN Configurator
color 0b
echo ===================================================
echo   ProcureFlow Client LAN Connection Configurator
echo ===================================================
echo.
echo This utility helps connect this frontend client to
echo the central Host Server running the database.
echo.
set /p hostip="Enter the Host Server LAN IP (e.g., 192.168.1.100): "

if "%hostip%"=="" (
    echo.
    echo ERROR: IP Address cannot be blank. Config aborted.
    pause
    exit /b
)

echo api.base_url=http://%hostip%:8080> config.properties

echo.
echo ===================================================
echo SUCCESS!
echo Linked to Host Server: http://%hostip%:8080
echo Saved configuration directly to config.properties
echo ===================================================
echo.
pause
