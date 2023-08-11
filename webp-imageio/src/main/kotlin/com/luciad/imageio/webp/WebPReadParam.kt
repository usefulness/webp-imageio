package com.luciad.imageio.webp

import com.luciad.imageio.webp.WebPWrapper.loadNativeLibrary
import javax.imageio.ImageReadParam

public class WebPReadParam : ImageReadParam() {

    internal val decoderOptions = WebPDecoderOptions()

    init {
        WebPWrapper.cleaner.register(this, decoderOptions)
    }

    /**
     * if true, skip the in-loop filtering
     */
    public var bypassFiltering: Boolean by decoderOptions::bypassFiltering

    /**
     * if true, use faster pointwise upsampler
     */
    public var noFancyUpsampling: Boolean by decoderOptions::noFancyUpsampling

    /**
     * if true, cropping is applied _first_
     */
    public var useCropping: Boolean by decoderOptions::useCropping

    /**
     * top-left position for cropping. Will be snapped to even values.
     */
    public var cropLeft: Int by decoderOptions::cropLeft

    /**
     * top-left position for cropping. Will be snapped to even values.
     */
    public var cropTop: Int by decoderOptions::cropTop

    /**
     * dimension of the cropping area
     */
    public var cropWidth: Int by decoderOptions::cropWidth

    /**
     * dimension of the cropping area
     */
    public var cropHeight: Int by decoderOptions::cropHeight

    /**
     * if true, scaling is applied _afterward_
     */
    public var useScaling: Boolean by decoderOptions::useScaling

    /**
     * final resolution
     */
    public var scaledWidth: Int by decoderOptions::scaledWidth

    /**
     * final resolution
     */
    public var scaledHeight: Int by decoderOptions::scaledHeight

    /**
     * if true, use multi-threaded decoding
     */
    public var useThreads: Boolean by decoderOptions::useThreads

    /**
     * dithering strength (0=Off, 100=full)
     */
    public var ditheringStrength: Int by decoderOptions::ditheringStrength

    /**
     * if true, flip output vertically
     */
    public var flipVertically: Boolean by decoderOptions::flip

    /**
     * alpha dithering strength in [0..100]
     */
    public var alphaDitheringStrength: Int by decoderOptions::alphaDitheringStrength

    private companion object {

        init {
            loadNativeLibrary()
        }
    }
}
