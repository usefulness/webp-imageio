package com.luciad.imageio.webp

public enum class CompressionType(internal val imageIoValue: String) {
    Lossy("Lossy"),
    Lossless("Lossless"),
    ;

    internal companion object {

        val imageIoCompressionTypes = values().map { it.imageIoValue }.toTypedArray()
    }
}
