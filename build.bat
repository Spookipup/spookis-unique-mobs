@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "FINAL_DIR=%ROOT%build\finalized"
set "BUILD_JAVA_HOME="

rem Run Gradle itself on JDK 21. Newer Minecraft targets can still use
rem Gradle Java toolchains, but Kotlin DSL currently warns on a JDK 25 launcher.
if defined JAVA21_HOME if exist "%JAVA21_HOME%\bin\java.exe" set "BUILD_JAVA_HOME=%JAVA21_HOME%"
if not defined BUILD_JAVA_HOME for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do if exist "%%~fJ\bin\java.exe" if not defined BUILD_JAVA_HOME set "BUILD_JAVA_HOME=%%~fJ"
if not defined BUILD_JAVA_HOME for /d %%J in ("C:\Program Files\Java\jdk-21*") do if exist "%%~fJ\bin\java.exe" if not defined BUILD_JAVA_HOME set "BUILD_JAVA_HOME=%%~fJ"

if not defined BUILD_JAVA_HOME (
  echo ERROR: JDK 21 was not found. Install JDK 21 or set JAVA21_HOME to its install folder.
  exit /b 1
)

set "JAVA_HOME=%BUILD_JAVA_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using build JDK: "%JAVA_HOME%"
"%JAVA_HOME%\bin\java.exe" -version 2>&1

set "MOD_VERSION="
set "MOD_CHANNEL_TAG="
set "MOD_ID="
set "MOD_ARCHIVE_NAME="
for /f "usebackq tokens=1,* delims==" %%A in ("%ROOT%gradle.properties") do (
  if "%%A"=="mod.id" set "MOD_ID=%%B"
  if "%%A"=="mod.archive_name" set "MOD_ARCHIVE_NAME=%%B"
  if "%%A"=="mod.version" set "MOD_VERSION=%%B"
  if "%%A"=="mod.channel_tag" set "MOD_CHANNEL_TAG=%%B"
)

if not defined MOD_VERSION (
  echo ERROR: Could not read mod.version from "%ROOT%gradle.properties".
  exit /b 1
)

if not defined MOD_ARCHIVE_NAME set "MOD_ARCHIVE_NAME=%MOD_ID%"
if not defined MOD_ARCHIVE_NAME (
  echo ERROR: Could not read mod.archive_name or mod.id from "%ROOT%gradle.properties".
  exit /b 1
)

set "MOD_BUILD_VERSION=%MOD_VERSION%%MOD_CHANNEL_TAG%"

if exist "%FINAL_DIR%" rmdir /s /q "%FINAL_DIR%"
mkdir "%FINAL_DIR%"

for /d %%D in ("%ROOT%versions\*") do (
  set "VERSION_DIR=%%~nxD"
  set "MC_VERSION="
  set "LOADER="

  for /f "tokens=1,2 delims=-" %%A in ("!VERSION_DIR!") do (
    set "MC_VERSION=%%A"
    set "LOADER=%%B"
  )

  if not defined LOADER (
    echo ERROR: Could not parse version folder: !VERSION_DIR!
    exit /b 1
  )

  set "JAR_NAME=%MOD_ARCHIVE_NAME%-!MOD_BUILD_VERSION!+!MC_VERSION!+!LOADER!.jar"
  set "SOURCE_JAR=%%~fD\build\libs\!JAR_NAME!"

  echo.
  echo Building !VERSION_DIR!...
  call "%ROOT%gradlew.bat" ":!VERSION_DIR!:build" -x javadoc -x javadocJar
  if errorlevel 1 exit /b !errorlevel!

  if not exist "!SOURCE_JAR!" (
    echo ERROR: Expected jar was not found: "!SOURCE_JAR!"
    exit /b 1
  )

  copy /Y "!SOURCE_JAR!" "%FINAL_DIR%\!JAR_NAME!" >nul
  if errorlevel 1 exit /b !errorlevel!
  echo Copied "!SOURCE_JAR!"
  echo     to "%FINAL_DIR%\!JAR_NAME!"
)

echo.
echo Final jars copied to "%FINAL_DIR%"
dir /b "%FINAL_DIR%\*.jar"
