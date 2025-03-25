#!/bin/bash
set -u

OUTPUT_DIR="src/main/resources/org/lebastudios/theroundtable/bin"

mkdir "$OUTPUT_DIR"
  
(
  cd "trt-camelot" || exit
  
  cross build --target x86_64-unknown-linux-gnu --release -p camelot &
  cross build --target x86_64-pc-windows-gnu --release -p camelot &
  
  wait 
  
  mv "target/x86_64-unknown-linux-gnu/release/camelot" "../$OUTPUT_DIR/camelot-linux"
  mv "target/x86_64-pc-windows-gnu/release/camelot.exe" "../$OUTPUT_DIR/camelot-win.exe"
)
  
  