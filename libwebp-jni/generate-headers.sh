#!/usr/bin/env bash
set -ex -o pipefail

rm -rf src/main/c/*.h

javac -h src/main/c/ \
 ../webp-imageio/src/main/java/com/luciad/imageio/webp/WebPEncoderOptions.java \
 ../webp-imageio/src/main/java/com/luciad/imageio/webp/WebPDecoderOptions.java \
 ../webp-imageio/src/main/java/com/luciad/imageio/webp/WebP.java \
 --release 8 \
 -d ../webp-imageio/build/classes/javaheaders/main
