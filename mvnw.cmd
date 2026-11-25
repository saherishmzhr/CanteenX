@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@echo off
setlocal enabledelayedexpansion

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

if not defined JAVA_HOME (
  for %%I in (java.exe) do set "JAVA_EXEC=%%~$PATH:I"
  if not defined JAVA_EXEC (
    echo Error: JAVA_HOME is not set and java.exe was not found in PATH. >&2
    exit /b 1
  )
) else (
  set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
)

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPS=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_JAR%" (
  if not exist "%WRAPPER_PROPS%" (
    echo Error: Cannot find %WRAPPER_PROPS% >&2
    exit /b 1
  )
  if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
  set "JAR_URL="
  for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
    if "%%A"=="wrapperUrl" set "JAR_URL=%%B"
    if "%%A"=="wrapperVersion" set "WRAPPER_VER=%%B"
  )
  if not defined JAR_URL (
    if not defined WRAPPER_VER set "WRAPPER_VER=3.3.2"
    set "JAR_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/!WRAPPER_VER!/maven-wrapper-!WRAPPER_VER!.jar"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!JAR_URL!' -OutFile '%WRAPPER_JAR%' -UseBasicParsing } catch { exit 1 }"
  if errorlevel 1 (
    echo Error: Failed to download maven-wrapper.jar from !JAR_URL! >&2
    exit /b 1
  )
)

"%JAVA_EXEC%" %MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -jar "%WRAPPER_JAR%" %*
exit /b %ERRORLEVEL%
