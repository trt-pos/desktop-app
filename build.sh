#!/bin/bash
set -u

build-for-platform() {
  PLATFORM=$1
  
  OUTPUT_DIR="output/theroundtable-$PLATFORM-x64"
  FILES="app-files-$PLATFORM"
  JDK="$HOME/.jdks/openjdk-22.0.2_$PLATFORM-x64_bin/"
  
  rm -rf "OUTPUT_DIR"
  mkdir -p "$OUTPUT_DIR"
  
  cp -r "bin" "$OUTPUT_DIR"
  cp -r "styles" "$OUTPUT_DIR"
  cp -r "images" "$OUTPUT_DIR"
  
  cp -r "$FILES/." "$OUTPUT_DIR"
  cp -r "$JDK" "$OUTPUT_DIR/jdk"
  
  if [ "$PLATFORM" == "linux" ]; then
      (
          cd "app-launcher" || exit
          cross build --target x86_64-unknown-linux-gnu --release -p app_launcher
          mv "target/x86_64-unknown-linux-gnu/release/app_launcher" "../$OUTPUT_DIR/start"
      )
      (
        cd "post-install-bin" || exit
        cross build --target x86_64-unknown-linux-gnu --release -p post_install_bin
        mv "target/x86_64-unknown-linux-gnu/release/post_install_bin" "../$OUTPUT_DIR/post-install-bin"
      )
  elif [ "$PLATFORM" == "windows" ]; then
      (
          cd "app-launcher" || exit
          cross build --target x86_64-pc-windows-gnu --release -p app_launcher
          mv "target/x86_64-pc-windows-gnu/release/app_launcher.exe" "../$OUTPUT_DIR/start.exe"
      )
      (
        cd "post-install-bin" || exit
        cross build --target x86_64-pc-windows-gnu --release -p post_install_bin
        mv "target/x86_64-pc-windows-gnu/release/post_install_bin.exe" "../$OUTPUT_DIR/post-install-bin.exe"
      )
  fi
}

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

# izpack
if [ "$PLATFORM" == "all" ]; then
    izpack -h "$IZPACK_HOME" -l 9 izpack.xml -o output/installer.jar
fi

rm -rf "bin"