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

import androidx.annotation.IntRange;

import javax.imageio.ImageReadParam;

public final class WebPReadParam extends ImageReadParam {
  private final WebPDecoderOptions fOptions;

  static {
    WebPWrapper.loadNativeLibrary();
  }

  public WebPReadParam() {
    fOptions = new WebPDecoderOptions();
  }

  /**
   * if true, skip the in-loop filtering
   */
  public void setBypassFiltering(boolean aBypassFiltering) {
    fOptions.setBypassFiltering(aBypassFiltering);
  }

  public boolean getBypassFiltering() {
    return fOptions.getBypassFiltering();
  }

  /**
   * if true, use faster pointwise upsampler
   */
  public void setNoFancyUpsampling(boolean aNoFancyUpsampling) {
    fOptions.setNoFancyUpsampling(aNoFancyUpsampling);
  }

  public boolean getNoFancyUpsampling() {
    return fOptions.getNoFancyUpsampling();
  }

  /**
   * if true, cropping is applied _first_
   */
  public void setUseCropping(boolean aUseCropping) {
    fOptions.setUseCropping(aUseCropping);
  }

  public boolean getUseCropping() {
    return fOptions.getUseCropping();
  }

  /**
   * top-left position for cropping. Will be snapped to even values.
   */
  public void setCropLeft(int aCropLeft) {
    fOptions.setCropLeft(aCropLeft);
  }

  public int getCropLeft() {
    return fOptions.getCropLeft();
  }

  /**
   * top-left position for cropping. Will be snapped to even values.
   */
  public void setCropTop(int aCropTop) {
    fOptions.setCropTop(aCropTop);
  }

  public int getCropTop() {
    return fOptions.getCropTop();
  }

  /**
   * dimension of the cropping area
   */
  public void setCropWidth(int aCropWidth) {
    fOptions.setCropWidth(aCropWidth);
  }

  public int getCropWidth() {
    return fOptions.getCropWidth();
  }

  /**
   * dimension of the cropping area
   */
  public void setCropHeight(int aCropHeight) {
    fOptions.setCropHeight(aCropHeight);
  }

  public int getCropHeight() {
    return fOptions.getCropHeight();
  }

  /**
   * if true, scaling is applied _afterward_
   */
  public void setUseScaling(boolean aUseScaling) {
    fOptions.setUseScaling(aUseScaling);
  }

  public boolean getUseScaling() {
    return fOptions.getUseScaling();
  }

  /**
   * final resolution
   */
  public void setScaledWidth(int aScaledWidth) {
    fOptions.setScaledWidth(aScaledWidth);
  }

  public int getScaledWidth() {
    return fOptions.getScaledWidth();
  }

  /**
   * final resolution
   */
  public void setScaledHeight(int aScaledHeight) {
    fOptions.setScaledHeight(aScaledHeight);
  }

  public int getScaledHeight() {
    return fOptions.getScaledHeight();
  }

  /**
   * if true, use multi-threaded decoding
   */
  public void setUseThreads(boolean aUseThreads) {
    fOptions.setUseThreads(aUseThreads);
  }

  public boolean getUseThreads() {
    return fOptions.getUseThreads();
  }

  /**
   * dithering strength (0=Off, 100=full)
   */
  public void setDitheringStrength(@IntRange(from = 0, to = 100) int aDitheringStrength) {
    fOptions.setDitheringStrength(aDitheringStrength);
  }

  public int getDitheringStrength() {
    return fOptions.getDitheringStrength();
  }

  /**
   * if true, flip output vertically
   */
  public void setFlipVertically(boolean aFlip) {
    fOptions.setFlip(aFlip);
  }

  public boolean getFlipVertically() {
    return fOptions.getFlip();
  }

  /**
   * alpha dithering strength in [0..100]
   */
  public void setAlphaDitheringStrength(@IntRange(from = 0, to = 100) int aAlphaDitheringStrength) {
    fOptions.setAlphaDitheringStrength(aAlphaDitheringStrength);
  }

  public int getAlphaDitheringStrength() {
    return fOptions.getAlphaDitheringStrength();
  }

  WebPDecoderOptions getDecoderOptions() {
    return fOptions;
  }
}
