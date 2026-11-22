@echo off
chcp 65001 < nul >nul
cd /d "%~dp0"

call build.cmd < nul
if errorlevel 1 exit /b 1

java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp target\classes helpdesk.Main
exit /b %errorlevel%
