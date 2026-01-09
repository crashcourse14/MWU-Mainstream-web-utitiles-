#!/bin/bash
#DON'T RUN ANY OF THE JAVA FILES. RUN THIS FILE FOR IT TO COMPILE
#YOU'LL GET RUNTIME ERRORS IF YOU RUN A JAVA FILE INSTEAD OF THIS FILE


set -e

echo "Compiling..."

javac -d out $(find src/main/java -name "*.java")

echo "Running server..."

java -cp out com.server.Server