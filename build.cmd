@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist target\classes mkdir target\classes
javac -encoding UTF-8 -d target\classes src\main\java\helpdesk\*.java src\main\java\helpdesk\console\*.java src\main\java\helpdesk\exception\*.java src\main\java\helpdesk\model\*.java src\main\java\helpdesk\repository\*.java src\main\java\helpdesk\service\*.java
if errorlevel 1 exit /b 1

if not exist target\test-classes mkdir target\test-classes
javac -encoding UTF-8 -cp target\classes -d target\test-classes src\test\java\helpdesk\*.java
if errorlevel 1 exit /b 1

echo Build OK: target\classes, target\test-classes
