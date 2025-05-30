@file:Suppress("PackageDirectoryMismatch", "InvalidPackageDeclaration")

package com.luciad.imageio.webp

import com.luciad.imageio.webp.internal.VP8StatusCode.Companion.getStatusCode
import com.luciad.imageio.webp.WebP.decode
import com.luciad.imageio.webp.WebP.encodeRGB
import com.luciad.imageio.webp.WebP.encodeRGBA
import com.luciad.imageio.webp.WebP.getInfo
import com.luciad.imageio.webp.internal.NativeLoader.initialize
import com.luciad.imageio.webp.internal.VP8StatusCode
import java.io.IOException
import java.nio.ByteOrder

internal object WebPWrapper {

    private var NATIVE_LIBRARY_LOADED = false

    init {
        loadNativeLibrary()
    }

    @JvmStatic
    @Synchronized
    fun loadNativeLibrary() {
        if (!NATIVE_LIBRARY_LOADED) {
            runCatching { initialize() }
                .onFailure { it.printStackTrace() }
            NATIVE_LIBRARY_LOADED = true
        }
    }

    fun decode(options: WebPDecoderOptions, data: ByteArray, offset: Int, length: Int, out: IntArray): IntArray {
        require(offset + length <= data.size) { "Offset/length exceeds array size" }

        val pixels = decode(
            options.fPointer,
            data,
            offset,
            length,
            out,
            ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN,
        )
        when (val status = getStatusCode(out[0])) {
            VP8StatusCode.Ok -> return pixels
            VP8StatusCode.OutOfMemory -> throw OutOfMemoryError()
            else -> throw IOException("Decode returned code $status")
        }
    }

    fun getInfo(data: ByteArray, offset: Int, length: Int): IntArray {
        val out = IntArray(2)
        val result = getInfo(data, offset, length, out)
        if (result == 0) {
            throw IOException("Invalid WebP data")
        }

        return out
    }

    fun encodeRGBA(options: WebPEncoderOptions, rgbaData: ByteArray, width: Int, height: Int, stride: Int): ByteArray =
        encodeRGBA(options.pointer, rgbaData, width, height, stride)

    fun encodeRGB(options: WebPEncoderOptions, rgbaData: ByteArray, width: Int, height: Int, stride: Int): ByteArray =
        encodeRGB(options.pointer, rgbaData, width, height, stride)
}
