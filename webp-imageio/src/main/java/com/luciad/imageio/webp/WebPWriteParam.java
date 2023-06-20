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

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

public class WebPWriteParam extends ImageWriteParam {

  static {
    WebPWrapper.loadNativeLibrary();
  }

  public static final int LOSSY_COMPRESSION = 0;
  public static final int LOSSLESS_COMPRESSION = 1;

  private final boolean fDefaultLossless;

  private final WebPEncoderOptions fOptions;

  public WebPWriteParam(Locale aLocale) {
    super(aLocale);
    fOptions = new WebPEncoderOptions();
    fDefaultLossless = fOptions.getLossless();
    canWriteCompressed = true;
    compressionTypes = new String[]{
        "Lossy",
        "Lossless"
    };
    compressionType = compressionTypes[fDefaultLossless ? LOSSLESS_COMPRESSION : LOSSY_COMPRESSION];
    compressionQuality = fOptions.getCompressionQuality() / 100f;
    compressionMode = MODE_EXPLICIT;
  }

  @Override
  public float getCompressionQuality() {
    return super.getCompressionQuality();
  }

  /**
   * For lossy, 0f gives the smallest size and 1f the largest.
   * For lossless, this parameter is the amount of effort put into the compression:
   * 0f is the fastest but gives larger files compared to the slowest, but best, 1f.
   */
  @Override
  public void setCompressionQuality(@FloatRange(from = 0f, to = 1f) float quality) {
    super.setCompressionQuality(quality);
    fOptions.setCompressionQuality(quality * 100f);
  }

  @Override
  public void setCompressionType(String compressionType) {
    super.setCompressionType(compressionType);
    for (int i = 0; i < compressionTypes.length; i++) {
      if (compressionTypes[i].equals(compressionType)) {
        fOptions.setLossless(i == LOSSLESS_COMPRESSION);
        break;
      }
    }

  }

  @Override
  public void unsetCompression() {
    super.unsetCompression();
    fOptions.setLossless(fDefaultLossless);
  }

  public int getMethod() {
    return fOptions.getMethod();
  }

  /**
   * quality/speed trade-off (0=fast, 6=slower-better)
   */
  public void setMethod(@IntRange(from = 0, to = 6) int aMethod) {
    fOptions.setMethod(aMethod);
  }

  public int getTargetSize() {
    return fOptions.getTargetSize();
  }

  /**
   * if non-zero, set the desired target size in bytes.
   * Takes precedence over the @{@link #setCompressionQuality(float)} parameter.
   */
  public void setTargetSize(int aTargetSize) {
    fOptions.setTargetSize(aTargetSize);
  }

  public float getTargetPSNR() {
    return fOptions.getTargetPSNR();
  }

  /**
   * if non-zero, specifies the minimal distortion to try to achieve.
   * Takes precedence over {@link #setTargetSize(int) }.
   */
  public void setTargetPSNR(float aTargetPSNR) {
    fOptions.setTargetPSNR(aTargetPSNR);
  }

  public int getSegments() {
    return fOptions.getSegments();
  }

  /**
   * maximum number of segments to use
   */
  public void setSegments(@IntRange(from = 1, to = 4) int aSegments) {
    fOptions.setSegments(aSegments);
  }

  public int getSnsStrength() {
    return fOptions.getSnsStrength();
  }

  /**
   * Spatial Noise Shaping. 0=off, 100=maximum.
   */
  public void setSnsStrength(@IntRange(from = 0, to = 100) int aSnsStrength) {
    fOptions.setSnsStrength(aSnsStrength);
  }

  public int getFilterStrength() {
    return fOptions.getFilterStrength();
  }

  /**
   * range: [0 = off .. 100 = strongest]
   */
  public void setFilterStrength(@IntRange(from = 0, to = 100) int aFilterStrength) {
    fOptions.setFilterStrength(aFilterStrength);
  }

  public int getFilterSharpness() {
    return fOptions.getFilterSharpness();
  }


  /**
   * range: [0 = off .. 7 = least sharp]
   */
  public void setFilterSharpness(@IntRange(from = 0, to = 7) int aFilterSharpness) {
    fOptions.setFilterSharpness(aFilterSharpness);
  }

  public int getFilterType() {
    return fOptions.getFilterType();
  }

  /**
   * filtering type: 0 = simple, 1 = strong (only used if filter_strength > 0 or autofilter > 0)
   */
  public void setFilterType(@IntRange(from = 0, to = 1) int aFilterType) {
    fOptions.setFilterType(aFilterType);
  }

  public boolean getAutoAdjustFilterStrength() {
    return fOptions.getAutoAdjustFilterStrength();
  }

  /**
   * Auto adjust filter's strength
   */
  public void setAutoAdjustFilterStrength(boolean value) {
    fOptions.setAutoAdjustFilterStrength(value);
  }

  public int getAlphaCompressionAlgorithm() {
    return fOptions.getAlphaCompressionAlgorithm();
  }

  /**
   * Algorithm for encoding the alpha plane
   * (0 = none, 1 = compressed with WebP lossless). Default is 1.
   */
  public void setAlphaCompressionAlgorithm(@IntRange(from = 0, to = 1) int aAlphaCompressionAlgorithm) {
    fOptions.setAlphaCompressionAlgorithm(aAlphaCompressionAlgorithm);
  }

  public int getAlphaFiltering() {
    return fOptions.getAlphaFiltering();
  }

  /**
   * Predictive filtering method for alpha plane.
   * 0: none, 1: fast, 2: best. Default if 1.
   */
  public void setAlphaFiltering(@IntRange(from = 0, to = 2) int aAlphaFiltering) {
    fOptions.setAlphaFiltering(aAlphaFiltering);
  }

  public int getAlphaQuality() {
    return fOptions.getAlphaQuality();
  }

  /**
   * 0: smallest size, 100: lossless. Default is 100.
   */
  public void setAlphaQuality(@IntRange(from = 0, to = 100) int aAlphaQuality) {
    fOptions.setAlphaQuality(aAlphaQuality);
  }

  public int getEntropyAnalysisPassCount() {
    return fOptions.getEntropyAnalysisPassCount();
  }

  /**
   * Number of entropy-analysis passes
   */
  public void setEntropyAnalysisPassCount(@IntRange(from = 1, to = 10) int aEntropyAnalysisPassCount) {
    fOptions.setEntropyAnalysisPassCount(aEntropyAnalysisPassCount);
  }

  public boolean getShowCompressed() {
    return fOptions.getShowCompressed();
  }

  /**
   * if true, export the compressed picture back. In-loop filtering is not applied.
   */
  public void setShowCompressed(boolean value) {
    fOptions.setShowCompressed(value);
  }

  public int getPreprocessing() {
    return fOptions.getPreprocessing();
  }

  /**
   * Preprocessing filter
   * 0=none, 1=segment-smooth, 2=pseudo-random dithering
   */
  public void setPreprocessing(@IntRange(from = 0, to = 2) int aPreprocessing) {
    fOptions.setPreprocessing(aPreprocessing);
  }

  public int getPartitions() {
    return fOptions.getPartitions();
  }

  /**
   * log2(number of token partitions) in [0..3]. Default is set to 0 for easier progressive decoding.
   */
  public void setPartitions(@IntRange(from = 0, to = 3) int aPartitions) {
    fOptions.setPartitions(aPartitions);
  }

  public int getPartitionLimit() {
    return fOptions.getPartitionLimit();
  }

  /**
   * Quality degradation allowed to fit the 512k limit on prediction modes coding
   * 0: no degradation, 100: maximum possible degradation
   */
  public void setPartitionLimit(@IntRange(from = 0, to = 100) int aPartitionLimit) {
    fOptions.setPartitionLimit(aPartitionLimit);
  }

  public boolean getEmulateJpegSize() {
    return fOptions.getEmulateJpegSize();
  }

  /**
   * If true, compression parameters will be remapped to better match the expected output size from
   * JPEG compression. Generally, the output size will be similar but the degradation will be lower.
   */
  public void setEmulateJpegSize(boolean value) {
    fOptions.setEmulateJpegSize(value);
  }

  public int getThreadLevel() {
    return fOptions.getThreadLevel();
  }

  /**
   * If non-zero, try and use multi-threaded encoding.
   */
  public void setThreadLevel(int aThreadLevel) {
    fOptions.setThreadLevel(aThreadLevel);
  }

  public boolean getLowMemory() {
    return fOptions.getLowMemory();
  }

  /**
   * If set, reduce memory usage (but increase CPU use).
   */
  public void setLowMemory(boolean aLowMemory) {
    fOptions.setLowMemory(aLowMemory);
  }

  public int getNearLossless() {
    return fOptions.getNearLossless();
  }

  /**
   * Near lossless encoding
   * 0 = max loss, 100 = off (default)
   */
  public void setNearLossless(@IntRange(from = 0, to = 100) int aNearLossless) {
    fOptions.setNearLossless(aNearLossless);
  }

  public boolean getExact() {
    return fOptions.getExact();
  }

  /**
   * If non-zero, preserve the exact RGB values under transparent area.
   * Otherwise, discard this invisible RGB information for better compression.
   * The default value is false.
   */
  public void setExact(boolean value) {
    fOptions.setExact(value);
  }

  public boolean getUseDeltaPalette() {
    return fOptions.getUseDeltaPalette();
  }

  /**
   * reserved for future lossless feature
   */
  public void setUseDeltaPalette(boolean value) {
    fOptions.setUseDeltaPalette(value);
  }

  public boolean getUseSharpYUV() {
    return fOptions.getUseSharpYUV();
  }

  /**
   * if needed, use sharp (and slow) RGB->YUV conversion
   */
  public void setUseSharpYUV(boolean value) {
    fOptions.setUseSharpYUV(value);
  }

  public int getQMin() {
    return fOptions.getQMin();
  }

  /**
   * minimum permissible quality factor
   */
  public void setQMin(int aQMin) {
    fOptions.setQMin(aQMin);
  }

  public int getQMax() {
    return fOptions.getQMax();
  }

  /**
   * maximum permissible quality factor
   */
  public void setQMax(int aQMax) {
    fOptions.setQMax(aQMax);
  }

  WebPEncoderOptions getEncoderOptions() {
    return fOptions;
  }
}
