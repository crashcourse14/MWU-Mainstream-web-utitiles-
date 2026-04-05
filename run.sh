#!/bin/bash
set -e

echo "Cleaning old build..."
rm -rf out
mkdir -p out

echo "Compiling..."
javac -d out $(find src/main/java -name "*.java")

echo "Running server..."
java -cp out com.server.ModernServer