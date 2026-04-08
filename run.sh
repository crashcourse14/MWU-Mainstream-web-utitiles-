#!/bin/bash
set -e

echo "Cleaning old build..."
rm -rf build
mkdir -p build

echo "Compiling framework..."
javac -d build $(find src/main/java -name "*.java")

echo "Compiling example..."
javac -cp "build:src/main/java" -d build examples/hello-world/src/HelloWorld.java

echo "Running server..."
java -cp build examples.HelloWorld