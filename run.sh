#!/bin/bash

set -e

echo "Compiling..."

javac -d out $(find src/main/java -name "*.java")

echo "Running server..."

java -cp out com.server.Server