/*
 * Copyright 2013 Luciad (http://www.luciad.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luciad.imageio.webp;

import static com.luciad.imageio.webp.WebPCleaner.cleaner;

final class WebPEncoderOptions implements Runnable {

  private long fPointer;

  public WebPEncoderOptions() {
    fPointer = createConfig();
    if (fPointer == 0) {
      throw new OutOfMemoryError();
    } else {
      cleaner.register(this,this);
    }
  }

  @Override
  public void run() {
    deleteConfig(fPointer);
    fPointer = 0L;
  }

  private static native long createConfig();

  private static native void deleteConfig(long aPointer);

  long getPointer() {
    return fPointer;
  }

  public boolean getLossless() {
    return getLossless(fPointer);
  }

  public void setLossless(boolean aLossless) {
    setLossless(fPointer, aLossless);
  }

  public float getCompressionQuality() {
    return getQuality(fPointer);
  }

  public void setCompressionQuality(float aQuality) {
    setQuality(fPointer, aQuality);
  }

  public int getMethod() {
    return getMethod(fPointer);
  }

  public void setMethod(int aMethod) {
    setMethod(fPointer, aMethod);
  }

  public int getTargetSize() {
    return getTargetSize(fPointer);
  }

  public void setTargetSize(int aTargetSize) {
    setTargetSize(fPointer, aTargetSize);
  }

  public float getTargetPSNR() {
    return getTargetPSNR(fPointer);
  }

  public void setTargetPSNR(float aTargetPSNR) {
    setTargetPSNR(fPointer, aTargetPSNR);
  }

  public int getSegments() {
    return getSegments(fPointer);
  }

  public void setSegments(int aSegments) {
    setSegments(fPointer, aSegments);
  }

  public int getSnsStrength() {
    return getSnsStrength(fPointer);
  }

  public void setSnsStrength(int aSnsStrength) {
    setSnsStrength(fPointer, aSnsStrength);
  }

  public int getFilterStrength() {
    return getFilterStrength(fPointer);
  }


  public void setFilterStrength(int aFilterStrength) {
    setFilterStrength(fPointer, aFilterStrength);
  }

  public int getFilterSharpness() {
    return getFilterSharpness(fPointer);
  }

  public void setFilterSharpness(int aFilterSharpness) {
    setFilterSharpness(fPointer, aFilterSharpness);
  }

  public int getFilterType() {
    return getFilterType(fPointer);
  }

  public void setFilterType(int aFilterType) {
    setFilterType(fPointer, aFilterType);
  }

  public boolean getAutoAdjustFilterStrength() {
    return getAutofilter(fPointer);
  }

  public void setAutoAdjustFilterStrength(boolean aAutofilter) {
    setAutofilter(fPointer, aAutofilter);
  }

  public int getAlphaCompressionAlgorithm() {
    return getAlphaCompression(fPointer);
  }


  public void setAlphaCompressionAlgorithm(int aAlphaCompression) {
    setAlphaCompression(fPointer, aAlphaCompression);
  }

  public int getAlphaFiltering() {
    return getAlphaFiltering(fPointer);
  }


  public void setAlphaFiltering(int aAlphaFiltering) {
    setAlphaFiltering(fPointer, aAlphaFiltering);
  }

  public int getAlphaQuality() {
    return getAlphaQuality(fPointer);
  }

  public void setAlphaQuality(int aAlphaQuality) {
    setAlphaQuality(fPointer, aAlphaQuality);
  }

  public int getEntropyAnalysisPassCount() {
    return getPass(fPointer);
  }

  public void setEntropyAnalysisPassCount(int aPass) {
    setPass(fPointer, aPass);
  }

  public boolean getShowCompressed() {
    return getShowCompressed(fPointer);
  }

  public void setShowCompressed(boolean aShowCompressed) {
    setShowCompressed(fPointer, aShowCompressed);
  }

  public int getPreprocessing() {
    return getPreprocessing(fPointer);
  }

  public void setPreprocessing(int aPreprocessing) {
    setPreprocessing(fPointer, aPreprocessing);
  }

  public int getPartitions() {
    return getPartitions(fPointer);
  }


  public void setPartitions(int aPartitions) {
    setPartitions(fPointer, aPartitions);
  }

  public int getPartitionLimit() {
    return getPartitionLimit(fPointer);
  }

  public void setPartitionLimit(int aPartitionLimit) {
    setPartitionLimit(fPointer, aPartitionLimit);
  }

  public boolean getEmulateJpegSize() {
    return getEmulateJpegSize(fPointer);
  }

  public void setEmulateJpegSize(boolean aEmulateJpegSize) {
    setEmulateJpegSize(fPointer, aEmulateJpegSize);
  }

  public int getThreadLevel() {
    return getThreadLevel(fPointer);
  }

  public void setThreadLevel(int aThreadLevel) {
    setThreadLevel(fPointer, aThreadLevel);
  }

  public boolean getLowMemory() {
    return getLowMemory(fPointer);
  }

  public void setLowMemory(boolean aLowMemory) {
    setLowMemory(fPointer, aLowMemory);
  }

  public int getNearLossless() {
    return getNearLossless(fPointer);
  }


  public void setNearLossless(int aNearLossless) {
    setNearLossless(fPointer, aNearLossless);
  }


  public boolean getExact() {
    return getExact(fPointer);
  }


  public void setExact(boolean aExact) {
    setExact(fPointer, aExact);
  }

  public boolean getUseDeltaPalette() {
    return getUseDeltaPalette(fPointer);
  }

  public void setUseDeltaPalette(boolean aUseDeltaPalette) {
    setUseDeltaPalette(fPointer, aUseDeltaPalette);
  }

  public boolean getUseSharpYUV() {
    return getUseSharpYUV(fPointer);
  }

  public void setUseSharpYUV(boolean aUseSharpYUV) {
    setUseSharpYUV(fPointer, aUseSharpYUV);
  }


  public int getQMin() {
    return getQMin(fPointer);
  }


  public void setQMin(int aQMin) {
    setQMin(fPointer, aQMin);
  }

  public int getQMax() {
    return getQMax(fPointer);
  }

  public void setQMax(int aQMax) {
    setQMax(fPointer, aQMax);
  }

  private static native boolean getLossless(long aPointer);

  private static native void setLossless(long aPointer, boolean aLossless);

  private static native float getQuality(long aPointer);

  private static native void setQuality(long aPointer, float aQuality);

  private static native int getMethod(long aPointer);

  private static native void setMethod(long aPointer, int aMethod);

  private static native int getTargetSize(long aPointer);

  private static native void setTargetSize(long aPointer, int aTargetSize);

  private static native float getTargetPSNR(long aPointer);

  private static native void setTargetPSNR(long aPointer, float aTargetPSNR);

  private static native int getSegments(long aPointer);

  private static native void setSegments(long aPointer, int aSegments);

  private static native int getSnsStrength(long aPointer);

  private static native void setSnsStrength(long aPointer, int aSnsStrength);

  private static native int getFilterStrength(long aPointer);

  private static native void setFilterStrength(long aPointer, int aFilterStrength);

  private static native int getFilterSharpness(long aPointer);

  private static native void setFilterSharpness(long aPointer, int aFilterSharpness);

  private static native int getFilterType(long aPointer);

  private static native void setFilterType(long aPointer, int aFilterType);

  private static native boolean getAutofilter(long aPointer);

  private static native void setAutofilter(long aPointer, boolean aAutofilter);

  private static native int getAlphaCompression(long aPointer);

  private static native void setAlphaCompression(long aPointer, int aAlphaCompression);

  private static native int getAlphaFiltering(long aPointer);

  private static native void setAlphaFiltering(long aPointer, int aAlphaFiltering);

  private static native int getAlphaQuality(long aPointer);

  private static native void setAlphaQuality(long aPointer, int aAlphaQuality);

  private static native int getPass(long aPointer);

  private static native void setPass(long aPointer, int aPass);

  private static native boolean getShowCompressed(long aPointer);

  private static native void setShowCompressed(long aPointer, boolean aShowCompressed);

  private static native int getPreprocessing(long aPointer);

  private static native void setPreprocessing(long aPointer, int aPreprocessing);

  private static native int getPartitions(long aPointer);

  private static native void setPartitions(long aPointer, int aPartitions);

  private static native int getPartitionLimit(long aPointer);

  private static native void setPartitionLimit(long aPointer, int aPartitionLimit);

  private static native boolean getEmulateJpegSize(long aPointer);

  private static native void setEmulateJpegSize(long aPointer, boolean aEmulateJpegSize);

  private static native int getThreadLevel(long aPointer);

  private static native void setThreadLevel(long aPointer, int aThreadLevel);

  private static native boolean getLowMemory(long aPointer);

  private static native void setLowMemory(long aPointer, boolean aLowMemory);

  private static native int getNearLossless(long aPointer);

  private static native void setNearLossless(long aPointer, int aNearLossless);

  private static native boolean getExact(long aPointer);

  private static native void setExact(long aPointer, boolean aExact);

  private static native boolean getUseDeltaPalette(long aPointer);

  private static native void setUseDeltaPalette(long aPointer, boolean aUseDeltaPalette);

  private static native boolean getUseSharpYUV(long aPointer);

  private static native void setUseSharpYUV(long aPointer, boolean aUseSharpYUV);

  private static native int getQMin(long aPointer);

  private static native void setQMin(long aPointer, int aQMin);

  private static native int getQMax(long aPointer);

  private static native void setQMax(long aPointer, int aQMax);
}
