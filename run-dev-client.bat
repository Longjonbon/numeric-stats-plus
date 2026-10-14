@echo off
rem Opens a RuneLite dev client with Player Numeric Stats loaded
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot"
call "%~dp0gradlew.bat" run
