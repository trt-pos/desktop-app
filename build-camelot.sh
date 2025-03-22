#!/bin/bash
set -u

PLATFORM=$1
OUTPUT_DIR="src/main/resources/org/lebastudios/theroundtable/camelot"

mkdir "$OUTPUT_DIR"
  
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <linux | windows> "
  exit 1
fi
  
if [ "$1" != "linux" ] && [ "$1" != "windows" ]; then
  echo "Not supported platform: $1"
      exit 1
fi
  
(
  cd "trt-camelot" || exit
  if [ "$PLATFORM" == "linux" ]; then
      cross build --target x86_64-unknown-linux-gnu --release -p server
      mv "target/x86_64-unknown-linux-gnu/release/server" "../$OUTPUT_DIR/camelot"
  elif [ "$PLATFORM" == "windows" ]; then
      cross build --target x86_64-pc-windows-gnu --release -p server
      mv "target/x86_64-pc-windows-gnu/release/server.exe" "../$OUTPUT_DIR/camelot.exe"
  fi
)
  
  