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

import java.awt.image.ComponentColorModel
import java.awt.image.ComponentSampleModel
import java.awt.image.DataBuffer
import java.awt.image.DirectColorModel
import java.awt.image.SinglePixelPackedSampleModel
import java.util.Locale
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriter
import javax.imageio.spi.ImageWriterSpi
import javax.imageio.stream.ImageOutputStream

public open class WebPImageWriterSpi :
    ImageWriterSpi(
        "Luciad",
        "1.0",
        arrayOf("WebP", "webp"),
        arrayOf("webp"),
        arrayOf("image/webp"),
        WebPReader::class.java.name,
        arrayOf<Class<*>>(ImageOutputStream::class.java),
        arrayOf(WebPImageReaderSpi::class.java.name),
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

    override fun canEncodeImage(type: ImageTypeSpecifier): Boolean {
        val colorModel = type.colorModel
        val sampleModel = type.sampleModel
        val transferType = sampleModel.transferType
        if (colorModel is ComponentColorModel) {
            if (sampleModel !is ComponentSampleModel) {
                return false
            }
            if (transferType != DataBuffer.TYPE_BYTE && transferType != DataBuffer.TYPE_INT) {
                return false
            }
        } else if (colorModel is DirectColorModel) {
            if (sampleModel !is SinglePixelPackedSampleModel) {
                return false
            }
            if (transferType != DataBuffer.TYPE_INT) {
                return false
            }
        }
        val sampleSize = sampleModel.sampleSize
        for (i in sampleSize.indices) {
            if (sampleSize[i] > 8) {
                return false
            }
        }
        return true
    }

    override fun createWriterInstance(extension: Any?): ImageWriter = WebPWriter(this)

    override fun getDescription(locale: Locale): String = "WebP Writer"
}
