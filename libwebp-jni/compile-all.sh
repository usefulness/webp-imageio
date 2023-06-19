#!/usr/bin/env bash
set -e -o pipefail

# Ensure clean build environments
rm -rf build

./dockcross/dockcross-linux-armv5 bash -c './compile.sh Linux arm'
./dockcross/dockcross-linux-armv6-lts bash -c './compile.sh Linux armv6'
./dockcross/dockcross-linux-armv7-lts bash -c './compile.sh Linux armv7'
./dockcross/dockcross-linux-arm64-lts bash -c './compile.sh Linux aarch64'
./dockcross/dockcross-manylinux-x86 bash -c './compile.sh Linux x86'
./dockcross/dockcross-manylinux-x64 bash -c './compile.sh Linux x86_64'
./dockcross/dockcross-linux-ppc64le bash -c './compile.sh Linux ppc64'

./dockcross/dockcross-windows-static-x86 bash -c './compile.sh Windows x86'
./dockcross/dockcross-windows-static-x64 bash -c './compile.sh Windows x86_64'

docker run --rm -v $(pwd):/workdir -e CROSS_TRIPLE=x86_64-apple-darwin gotson/crossbuild ./compile.sh Mac x86_64 /workdir/multiarch-darwin.cmake
docker run --rm -v $(pwd):/workdir -e CROSS_TRIPLE=aarch64-apple-darwin gotson/crossbuild ./compile.sh Mac aarch64 /workdir/multiarch-darwin.cmake

# Ensure clean target
rm -r ../webp-imageio/src/main/resources/native
cp -r build/native ../webp-imageio/src/main/resources/
