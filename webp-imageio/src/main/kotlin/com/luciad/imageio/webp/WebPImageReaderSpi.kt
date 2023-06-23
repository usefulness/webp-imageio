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
package com.luciad.imageio.webp

import java.nio.ByteOrder
import java.util.Locale
import javax.imageio.ImageReader
import javax.imageio.spi.ImageReaderSpi
import javax.imageio.stream.ImageInputStream

public open class WebPImageReaderSpi : ImageReaderSpi(
    "Luciad",
    "1.0",
    arrayOf("WebP", "webp"),
    arrayOf("webp"),
    arrayOf("image/webp"),
    WebPReader::class.java.name,
    arrayOf<Class<*>>(ImageInputStream::class.java),
    arrayOf(WebPImageWriterSpi::class.java.name),
    false,
    null,
    null,
    null,
    null,
    false,
    null,
    null,
    null,
    null,
) {
    override fun createReaderInstance(extension: Any?): ImageReader = WebPReader(this)

    override fun canDecodeInput(source: Any?): Boolean {
        if (source !is ImageInputStream) {
            return false
        }
        val readBytes = ByteArray(4)
        val oldByteOrder = source.byteOrder
        source.mark()
        source.byteOrder = ByteOrder.LITTLE_ENDIAN
        try {
            source.readFully(readBytes)
            if (!readBytes.contentEquals(RIFF)) {
                return false
            }
            val chunkLength = source.readUnsignedInt()
            val streamLength = source.length()
            if (streamLength != -1L && streamLength != chunkLength + 8) {
                return false
            }
            source.readFully(readBytes)
            if (!readBytes.contentEquals(WEBP)) {
                return false
            }
            source.readFully(readBytes)
            if (!readBytes.contentEquals(VP8_) && !readBytes.contentEquals(VP8L) && !readBytes.contentEquals(VP8X)) {
                return false
            }
        } finally {
            source.byteOrder = oldByteOrder
            source.reset()
        }
        return true
    }

    override fun getDescription(locale: Locale?): String = "WebP Reader"
}

private val RIFF = byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte())
private val WEBP = byteArrayOf('W'.code.toByte(), 'E'.code.toByte(), 'B'.code.toByte(), 'P'.code.toByte())
private val VP8_ = byteArrayOf('V'.code.toByte(), 'P'.code.toByte(), '8'.code.toByte(), ' '.code.toByte())
private val VP8L = byteArrayOf('V'.code.toByte(), 'P'.code.toByte(), '8'.code.toByte(), 'L'.code.toByte())
private val VP8X = byteArrayOf('V'.code.toByte(), 'P'.code.toByte(), '8'.code.toByte(), 'X'.code.toByte())
