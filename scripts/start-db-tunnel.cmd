@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-db-tunnel.ps1" %*
exit /b %ERRORLEVEL%
