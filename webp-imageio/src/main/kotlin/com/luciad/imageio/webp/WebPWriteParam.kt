package com.luciad.imageio.webp

import com.luciad.imageio.webp.WebPWrapper.loadNativeLibrary
import java.util.Locale
import javax.imageio.ImageWriteParam

public open class WebPWriteParam(locale: Locale?) : ImageWriteParam(locale) {

    internal val encoderOptions = WebPEncoderOptions()
    private val defaultLossless = encoderOptions.lossless

    init {
        WebPWrapper.cleaner.register(this, encoderOptions)
        canWriteCompressed = true
        compressionTypes = CompressionType.imageIoCompressionTypes
        compressionMode = MODE_EXPLICIT
        (if (defaultLossless) CompressionType.Lossless else CompressionType.Lossy)
            .imageIoValue
            .let(::setCompressionType)
        compressionQuality = encoderOptions.compressionQuality / 100f
    }

    public override fun getCompressionQuality(): Float = super.getCompressionQuality()

    /**
     * For lossy, 0f gives the smallest size and 1f the largest.
     * For lossless, this parameter is the amount of effort put into the compression:
     * 0f is the fastest but gives larger files compared to the slowest, but best, 1f.
     */
    override fun setCompressionQuality(quality: Float) {
        super.setCompressionQuality(quality)
        encoderOptions.compressionQuality = quality * 100f
    }

    public var compressionType: CompressionType
        get() = CompressionType.entries.first { it.imageIoValue == getCompressionType() }
        set(value) {
            setCompressionType(value.imageIoValue)
        }

    override fun setCompressionType(compressionType: String) {
        super.setCompressionType(compressionType)

        encoderOptions.lossless = when (CompressionType.entries.firstOrNull { it.imageIoValue == compressionType }) {
            CompressionType.Lossless -> true
            CompressionType.Lossy -> false
            null -> error("unrecognised compression type=$compressionType")
        }
    }

    override fun unsetCompression() {
        super.unsetCompression()
        encoderOptions.lossless = defaultLossless
    }

    /**
     * quality/speed trade-off (0=fast, 6=slower-better)
     */
    public open var method: Int by encoderOptions::method

    /**
     * if non-zero, set the desired target size in bytes.
     * Takes precedence over the @[.setCompressionQuality] parameter.
     */
    public open var targetSize: Int by encoderOptions::targetSize

    /**
     * if non-zero, specifies the minimal distortion to try to achieve.
     * Takes precedence over [.setTargetSize].
     */
    public open var targetPSNR: Float by encoderOptions::targetPSNR

    /**
     * maximum number of segments to use
     */
    public open var segments: Int by encoderOptions::segments

    /**
     * Spatial Noise Shaping. 0=off, 100=maximum.
     */
    public open var snsStrength: Int by encoderOptions::snsStrength

    /**
     * range: [0 = off .. 100 = strongest]
     */
    public open var filterStrength: Int by encoderOptions::filterStrength

    /**
     * range: [0 = off .. 7 = least sharp]
     */
    public open var filterSharpness: Int by encoderOptions::filterSharpness

    /**
     * filtering type: 0 = simple, 1 = strong (only used if filter_strength > 0 or autofilter > 0)
     */
    public open var filterType: Int by encoderOptions::filterType

    /**
     * Auto adjust filter's strength
     */
    public open var autoAdjustFilterStrength: Boolean by encoderOptions::autoAdjustFilterStrength

    /**
     * Algorithm for encoding the alpha plane
     * (0 = none, 1 = compressed with WebP lossless). Default is 1.
     */
    public open var alphaCompressionAlgorithm: Int by encoderOptions::alphaCompressionAlgorithm

    /**
     * Predictive filtering method for alpha plane.
     * 0: none, 1: fast, 2: best. Default if 1.
     */
    public open var alphaFiltering: Int by encoderOptions::alphaFiltering

    /**
     * 0: smallest size, 100: lossless. Default is 100.
     */
    public open var alphaQuality: Int by encoderOptions::alphaQuality

    /**
     * Number of entropy-analysis passes
     */
    public open var entropyAnalysisPassCount: Int by encoderOptions::entropyAnalysisPassCount

    /**
     * if true, export the compressed picture back. In-loop filtering is not applied.
     */
    public open var showCompressed: Boolean by encoderOptions::showCompressed

    /**
     * Preprocessing filter
     * 0=none, 1=segment-smooth, 2=pseudo-random dithering
     */
    public open var preprocessing: Int by encoderOptions::preprocessing

    /**
     * log2(number of token partitions) in [0..3]. Default is set to 0 for easier progressive decoding.
     */
    public open var partitions: Int by encoderOptions::partitions

    /**
     * Quality degradation allowed to fit the 512k limit on prediction modes coding
     * 0: no degradation, 100: maximum possible degradation
     */
    public open var partitionLimit: Int by encoderOptions::partitionLimit

    /**
     * If true, compression parameters will be remapped to better match the expected output size from
     * JPEG compression. Generally, the output size will be similar but the degradation will be lower.
     */
    public open var emulateJpegSize: Boolean by encoderOptions::emulateJpegSize

    /**
     * If non-zero, try and use multi-threaded encoding.
     */
    public open var threadLevel: Int by encoderOptions::threadLevel

    /**
     * If set, reduce memory usage (but increase CPU use).
     */
    public open var lowMemory: Boolean by encoderOptions::lowMemory

    /**
     * Near lossless encoding
     * 0 = max loss, 100 = off (default)
     */
    public open var nearLossless: Int by encoderOptions::nearLossless

    /**
     * If non-zero, preserve the exact RGB values under transparent area.
     * Otherwise, discard this invisible RGB information for better compression.
     * The default value is false.
     */
    public open var exact: Boolean by encoderOptions::exact

    /**
     * reserved for future lossless feature
     */
    public open var useDeltaPalette: Boolean by encoderOptions::useDeltaPalette

    /**
     * if needed, use sharp (and slow) RGB->YUV conversion
     */
    public open var useSharpYUV: Boolean by encoderOptions::useSharpYUV

    /**
     * minimum permissible quality factor
     */
    public open var qMin: Int by encoderOptions::qMin

    /**
     * maximum permissible quality factor
     */
    public open var qMax: Int by encoderOptions::qMax

    public companion object {

        init {
            loadNativeLibrary()
        }

        public const val LOSSY_COMPRESSION: Int = 0
        public const val LOSSLESS_COMPRESSION: Int = 1

        public const val PREPROCESSING_FILTER_NONE: Int = 0
        public const val PREPROCESSING_FILTER_SEGMENT_SMOOTH: Int = 1
        public const val PREPROCESSING_FILTER_PSEUDO_RANDOM_DITHERING: Int = 2
    }
}
