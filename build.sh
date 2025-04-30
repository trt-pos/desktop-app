#!/bin/bash
set -u

build-for-platform() {
  PLATFORM=$1
  
  BUILD_IDENTIFIER="theroundtable-$PLATFORM-x64"
  OUTPUT_DIR="output/$BUILD_IDENTIFIER"
  export APP_ZIP_PATH="../../../../$OUTPUT_DIR.zip"
  
  mkdir -p "$OUTPUT_DIR"
  
  cp -r "bin" "$OUTPUT_DIR"
  cp -r "styles" "$OUTPUT_DIR"
  cp -r "images" "$OUTPUT_DIR"
  
  if [ "$PLATFORM" == "linux" ]; then
      (
          cd "app-launcher" || exit
          cross build --target x86_64-unknown-linux-gnu --release -p app_launcher
          mv "target/x86_64-unknown-linux-gnu/release/app_launcher" "../$OUTPUT_DIR/start"
      )
      (
        cd "output" || exit
        zip -r "$BUILD_IDENTIFIER.zip" "$BUILD_IDENTIFIER/"
      )
      (
        cd "trt-installer" || exit
        cross build --target x86_64-unknown-linux-gnu --release -p bin
        VERSION=$(cargo metadata --format-version 1 --no-deps | jq -r '.packages[0].version')
        mv "target/x86_64-unknown-linux-gnu/release/bin" "../output/trt-installer-$VERSION"
      )
  elif [ "$PLATFORM" == "windows" ]; then
      (
          cd "app-launcher" || exit
          cross build --target x86_64-pc-windows-gnu --release -p app_launcher
          mv "target/x86_64-pc-windows-gnu/release/app_launcher.exe" "../$OUTPUT_DIR/start.exe"
      )
      (
        cd "output" || exit
        zip -r "$BUILD_IDENTIFIER.zip" "$BUILD_IDENTIFIER/"
      )
      (
        cd "trt-installer" || exit
        cross build --target x86_64-pc-windows-gnu --release -p bin
        VERSION=$(cargo metadata --format-version 1 --no-deps | jq -r '.packages[0].version')
        mv "target/x86_64-pc-windows-gnu/release/bin.exe" "../output/trt-installer-$VERSION.exe"
      )
  fi
}

rm -rf output

if [ "$#" -ne 1 ]; then
  echo "Uso: $0 <linux | windows | all> "
  exit 1
fi
  
if [ "$1" != "linux" ] && [ "$1" != "windows" ] && [ "$1" != "all" ]; then
  echo "Not supported platform: $1"
      exit 1
fi

mvn package -P desktop

# Asignar variables
PLATFORM=$1

if [ "$PLATFORM" == "linux" ] || [ "$PLATFORM" == "all" ]; then
  build-for-platform "linux" &
fi

if [ "$PLATFORM" == "windows" ] || [ "$PLATFORM" == "all" ]; then
  build-for-platform "windows" &
fi

wait

rm -rf "bin"