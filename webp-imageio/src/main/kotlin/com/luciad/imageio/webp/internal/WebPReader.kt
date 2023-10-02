@file:Suppress("PackageDirectoryMismatch", "InvalidPackageDeclaration")

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

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.DirectColorModel
import java.awt.image.WritableRaster
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.util.Hashtable
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadata
import javax.imageio.spi.ImageReaderSpi
import javax.imageio.stream.ImageInputStream

internal class WebPReader(originatingProvider: ImageReaderSpi) : ImageReader(originatingProvider) {

    private var cachedData: ByteArray? = null
    private var cachedHeader: Pair<Int, Int>? = null

    override fun setInput(input: Any, seekForwardOnly: Boolean, ignoreMetadata: Boolean) {
        super.setInput(input, seekForwardOnly, ignoreMetadata)
        cachedData = null
        cachedHeader = null
    }

    override fun getNumImages(allowSearch: Boolean): Int = 1

    private fun readHeader(): Pair<Int, Int> {
        cachedHeader?.let { return it }

        val data = readData()
        val (width, height) = WebPWrapper.getInfo(data, 0, data.size)

        return width to height
    }

    private fun readData(): ByteArray {
        cachedData?.let { return it }

        val input = getInput() as ImageInputStream
        val length = input.length()
        if (length > Int.MAX_VALUE) {
            throw IOException("Cannot read image of size $length")
        }
        if (input.streamPosition != 0L) {
            if (isSeekForwardOnly) {
                throw IOException()
            } else {
                input.seek(0)
            }
        }
        val data = if (length > 0) {
            ByteArray(length.toInt()).also(input::readFully)
        } else {
            ByteArrayOutputStream().use { out ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                }
                out.toByteArray()
            }
        }
        cachedData = data

        return data
    }

    private fun checkIndex(imageIndex: Int) {
        if (imageIndex != 0) {
            throw IndexOutOfBoundsException("Invalid image index")
        }
    }

    override fun getWidth(imageIndex: Int): Int {
        checkIndex(imageIndex)
        val (width, _) = readHeader()

        return width
    }

    override fun getHeight(imageIndex: Int): Int {
        checkIndex(imageIndex)
        val (_, height) = readHeader()

        return height
    }

    override fun getStreamMetadata(): IIOMetadata? = null

    override fun getImageMetadata(imageIndex: Int): IIOMetadata? = null

    override fun getImageTypes(imageIndex: Int) = listOf(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB))
        .iterator()

    override fun getDefaultReadParam() = WebPReadParam()

    override fun read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
        checkIndex(imageIndex)
        val data = readData()
        readHeader()
        val readParam = (param as? WebPReadParam) ?: defaultReadParam
        val outParams = IntArray(4)
        val pixels = WebPWrapper.decode(readParam.decoderOptions, data, 0, data.size, outParams)
        val width = outParams[1]
        val height = outParams[2]
        val alpha = outParams[3] != 0
        val colorModel = if (alpha) {
            DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, -0x1000000)
        } else {
            DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff, 0x00000000)
        }
        val sampleModel = colorModel.createCompatibleSampleModel(width, height)
        val db = DataBufferInt(pixels, width * height)
        val raster = WritableRaster.createWritableRaster(sampleModel, db, null)

        return BufferedImage(colorModel, raster, false, Hashtable<Any, Any>())
    }
}
