package com.luciad.imageio.webp;

class WebP {

  static native int[] decode(long aDecoderOptionsPointer, byte[] aData, int aOffset, int aLength, int[] aFlags, boolean aBigEndian);

  static native int getInfo(byte[] aData, int aOffset, int aLength, int[] aOut);

  static native byte[] encodeRGBA(long aConfig, byte[] aRgbaData, int aWidth, int aHeight, int aStride);

  static native byte[] encodeRGB(long aConfig, byte[] aRgbaData, int aWidth, int aHeight, int aStride);
}
