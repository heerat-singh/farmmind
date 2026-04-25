#!/usr/bin/env bash
# Local build helper — sets Java 21 + Maven from H drive
# Usage: ./build.sh [service] [maven-args...]
#   ./build.sh api-gateway test
#   ./build.sh api-gateway spring-boot:run
#   ./build.sh api-gateway clean package -DskipTests

export JAVA_HOME="h:/jdk21/jdk-21.0.4+7"
export MAVEN="h:/maven/apache-maven-3.9.6/bin/mvn.cmd"
export MAVEN_OPTS="-Dmaven.repo.local=h:/maven-repo"

SERVICE=${1:-api-gateway}
shift
ARGS=${@:-test}

cd "$(dirname "$0")/services/$SERVICE" && $MAVEN $MAVEN_OPTS $ARGS
