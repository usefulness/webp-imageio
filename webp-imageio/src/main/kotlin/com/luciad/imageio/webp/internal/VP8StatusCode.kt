package com.luciad.imageio.webp.internal

internal enum class VP8StatusCode {
    Ok,
    OutOfMemory,
    InvalidParam,
    BitstreamError,
    UnsupportedFeature,
    Suspended,
    UserAbort,
    NotEnoughData,
    ;

    companion object {

        fun getStatusCode(aValue: Int) = entries.getOrNull(aValue)
    }
}
