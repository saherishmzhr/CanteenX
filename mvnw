#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven wrapper startup batch script, version 3.3.2
#
# Required ENV vars:
#   JAVA_HOME - location of a JDK home dir, contains bin/java
#
# Optional ENV vars:
#   MAVEN_PROJECTBASEDIR - the root directory of the project that contains .mvn
#   MAVEN_SKIP_RC - flag to disable loading mavenrc files
# ----------------------------------------------------------------------------

set -e
POSIXLY_CORRECT=1

warn() {
  echo "$*" >&2
}

die() {
  warn "$*"
  exit 1
}

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true;;
  Darwin*) darwin=true;;
  MINGW*) mingw=true;;
esac

if [ -z "$MAVEN_PROJECTBASEDIR" ] ; then
  hashdirs="."
  bf=false
  until [ "$bf" = true ] || [ "$hashdirs" = "/" ] ; do
    if [ -f "$hashdirs/.mvn/maven.config" ] || [ -f "$hashdirs/.mvn/jvm.config" ] || [ -d "$hashdirs/.mvn" ] ; then
      MAVEN_PROJECTBASEDIR=$(
        cd "$hashdirs"
        pwd
      )
      bf=true
    else
      hashdirs="../$hashdirs"
    fi
  done
fi

if [ -z "$MAVEN_PROJECTBASEDIR" ] ; then
  MAVEN_PROJECTBASEDIR=$(pwd)
fi

# For Cygwin & MinGW, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$MAVEN_PROJECTBASEDIR" ] && MAVEN_PROJECTBASEDIR=$(cygpath --unix "$MAVEN_PROJECTBASEDIR")
fi

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi
fi

if $darwin ; then
  [ -z "$JAVA_HOME" ] && [ -d /Library/Java/Home ] && export JAVA_HOME=/Library/Java/Home
  [ -z "$JAVA_HOME" ] && [ -x /usr/libexec/java_home ] && export JAVA_HOME=$(/usr/libexec/java_home)
fi

if [ -z "$JAVA_HOME" ] ; then
  if [ -n "$JAVACMD" ] ; then
    JAVA_EXEC="$JAVACMD"
  else
    JAVA_EXEC="java"
  fi
else
  if $cygwin ; then
    JAVA_HOME=$(cygpath --windows "$JAVA_HOME")
  fi
  JAVA_EXEC="$JAVA_HOME/bin/java"
fi

if ! command -v "$JAVA_EXEC" >/dev/null 2>&1 && [ ! -x "$JAVA_EXEC" ]; then
  die "Error: JAVA_HOME is not set and 'java' could not be found in PATH."
fi

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPS="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Download maven-wrapper.jar if not present
if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$WRAPPER_PROPS" ]; then
    die "Error: Cannot find $WRAPPER_PROPS"
  fi
  mkdir -p "$(dirname "$WRAPPER_JAR")"
  JAR_URL=$(awk -F= '/^wrapperUrl=/{print $2}' "$WRAPPER_PROPS" | tr -d '')
  if [ -z "$JAR_URL" ]; then
    WRAPPER_VER=$(awk -F= '/^wrapperVersion=/{print $2}' "$WRAPPER_PROPS" | tr -d '')
    [ -z "$WRAPPER_VER" ] && WRAPPER_VER="3.3.2"
    JAR_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/${WRAPPER_VER}/maven-wrapper-${WRAPPER_VER}.jar"
  fi
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$JAR_URL" -o "$WRAPPER_JAR" || die "Failed to download maven-wrapper.jar from $JAR_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$JAR_URL" -O "$WRAPPER_JAR" || die "Failed to download maven-wrapper.jar from $JAR_URL"
  else
    die "Error: Neither curl nor wget is available to download maven-wrapper.jar."
  fi
fi

exec "$JAVA_EXEC"   $MAVEN_OPTS   -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR"   -jar "$WRAPPER_JAR"   "$@"
