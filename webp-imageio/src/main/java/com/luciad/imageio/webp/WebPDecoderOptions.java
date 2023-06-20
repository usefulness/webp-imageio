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

final class WebPDecoderOptions implements AutoCloseable {

  long fPointer;

  public WebPDecoderOptions() {
    fPointer = createDecoderOptions();
    if (fPointer == 0) {
      throw new OutOfMemoryError();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  @Override
  public void close() {
    deleteDecoderOptions(fPointer);
    fPointer = 0L;
  }

  public boolean getBypassFiltering() {
    return getBypassFiltering(fPointer);
  }

  public void setBypassFiltering(boolean aBypassFiltering) {
    setBypassFiltering(fPointer, aBypassFiltering);
  }

  public boolean getNoFancyUpsampling() {
    return getNoFancyUpsampling(fPointer);
  }


  public void setNoFancyUpsampling(boolean aFancyUpsampling) {
    setNoFancyUpsampling(fPointer, aFancyUpsampling);
  }

  public boolean getUseCropping() {
    return getUseCropping(fPointer);
  }

  public void setUseCropping(boolean aUseCropping) {
    setUseCropping(fPointer, aUseCropping);
  }

  public int getCropLeft() {
    return getCropLeft(fPointer);
  }

  public void setCropLeft(int aCropLeft) {
    setCropLeft(fPointer, aCropLeft);
  }

  public int getCropTop() {
    return getCropTop(fPointer);
  }

  public void setCropTop(int aCropTop) {
    setCropTop(fPointer, aCropTop);
  }

  public int getCropWidth() {
    return getCropWidth(fPointer);
  }

  public void setCropWidth(int aCropWidth) {
    setCropWidth(fPointer, aCropWidth);
  }

  public int getCropHeight() {
    return getCropHeight(fPointer);
  }

  public void setCropHeight(int aCropHeight) {
    setCropHeight(fPointer, aCropHeight);
  }

  public boolean getUseScaling() {
    return getUseScaling(fPointer);
  }

  public void setUseScaling(boolean aUseScaling) {
    setUseScaling(fPointer, aUseScaling);
  }

  public int getScaledWidth() {
    return getScaledWidth(fPointer);
  }

  public void setScaledWidth(int aScaledWidth) {
    setScaledWidth(fPointer, aScaledWidth);
  }

  public int getScaledHeight() {
    return getScaledHeight(fPointer);
  }

  public void setScaledHeight(int aScaledHeight) {
    setScaledHeight(fPointer, aScaledHeight);
  }

  public boolean getUseThreads() {
    return getUseThreads(fPointer);
  }

  public void setUseThreads(boolean aUseThreads) {
    setUseThreads(fPointer, aUseThreads);
  }

  public int getDitheringStrength() {
    return getDitheringStrength(fPointer);
  }

  public void setDitheringStrength(int aDitheringStrength) {
    setDitheringStrength(fPointer, aDitheringStrength);
  }

  public boolean getFlip() {
    return getFlip(fPointer);
  }

  public void setFlip(boolean aFlip) {
    setFlip(fPointer, aFlip);
  }

  public int getAlphaDitheringStrength() {
    return getAlphaDitheringStrength(fPointer);
  }

  public void setAlphaDitheringStrength(int aAlphaDitheringStrength) {
    setAlphaDitheringStrength(fPointer, aAlphaDitheringStrength);
  }


  private static native long createDecoderOptions();

  private static native void deleteDecoderOptions(long aPointer);

  private static native boolean getBypassFiltering(long aPointer);

  private static native void setBypassFiltering(long aPointer, boolean aBypassFiltering);

  private static native boolean getNoFancyUpsampling(long aPointer);

  private static native void setNoFancyUpsampling(long aPointer, boolean aNoFancyUpsampling);

  private static native boolean getUseCropping(long aPointer);

  private static native int getCropLeft(long aPointer);

  private static native void setCropLeft(long aPointer, int aCropLeft);

  private static native int getCropTop(long aPointer);

  private static native void setCropTop(long aPointer, int aCropTop);

  private static native void setUseCropping(long aPointer, boolean aUseCropping);

  private static native int getCropWidth(long aPointer);

  private static native void setCropWidth(long aPointer, int aCropWidth);

  private static native int getCropHeight(long aPointer);

  private static native void setCropHeight(long aPointer, int aCropHeight);

  private static native boolean getUseScaling(long aPointer);

  private static native int getScaledWidth(long aPointer);

  private static native void setScaledWidth(long aPointer, int aScaledWidth);

  private static native void setUseScaling(long aPointer, boolean aUseScaling);

  private static native int getScaledHeight(long aPointer);

  private static native void setScaledHeight(long aPointer, int aScaledHeight);

  private static native boolean getUseThreads(long aPointer);

  private static native void setUseThreads(long aPointer, boolean aUseThreads);

  private static native int getDitheringStrength(long aPointer);

  private static native void setDitheringStrength(long aPointer, int aDitheringStrength);

  private static native boolean getFlip(long aPointer);

  private static native void setFlip(long aPointer, boolean aFlip);

  private static native int getAlphaDitheringStrength(long aPointer);

  private static native void setAlphaDitheringStrength(long aPointer, int aAlphaDitheringStrength);
}
