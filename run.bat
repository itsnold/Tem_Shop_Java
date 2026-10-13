@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -cp "lib\jorbis-0.0.17.jar" -d build src\main\java\com\temshop\*.java src\main\java\com\jcraft\jorbis\*.java
if errorlevel 1 exit /b 1
java -cp "build;lib\jorbis-0.0.17.jar" com.temshop.TemShopApp
