#!/bin/bash
set -u

OUTPUT_DIR="src/main/resources/org/lebastudios/theroundtable/bin"

mkdir "$OUTPUT_DIR"
  
(
  cd "trt-camelot" || exit
  
  cross build --target x86_64-unknown-linux-gnu --release -p server
  mv "target/x86_64-unknown-linux-gnu/release/server" "../$OUTPUT_DIR/camelot-linux"
  
  cross build --target x86_64-pc-windows-gnu --release -p server
  mv "target/x86_64-pc-windows-gnu/release/server.exe" "../$OUTPUT_DIR/camelot-win.exe"
)
  
  