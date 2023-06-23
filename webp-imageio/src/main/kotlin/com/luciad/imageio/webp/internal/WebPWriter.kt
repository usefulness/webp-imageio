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

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.ComponentSampleModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.awt.image.DirectColorModel
import java.awt.image.RenderedImage
import java.awt.image.SinglePixelPackedSampleModel
import java.io.IOException
import javax.imageio.IIOImage
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.spi.ImageWriterSpi
import javax.imageio.stream.ImageOutputStream

internal class WebPWriter(originatingProvider: ImageWriterSpi?) : ImageWriter(originatingProvider) {

    override fun getDefaultWriteParam() = WebPWriteParam(getLocale())

    override fun convertImageMetadata(inData: IIOMetadata?, imageType: ImageTypeSpecifier, param: ImageWriteParam?): IIOMetadata? = null

    override fun convertStreamMetadata(inData: IIOMetadata?, param: ImageWriteParam?): IIOMetadata? = null

    override fun getDefaultImageMetadata(imageType: ImageTypeSpecifier, param: ImageWriteParam?): IIOMetadata? = null

    override fun getDefaultStreamMetadata(param: ImageWriteParam?): IIOMetadata? = null

    override fun write(streamMetadata: IIOMetadata?, image: IIOImage, param: ImageWriteParam?) {
        val writeParam = (param as? WebPWriteParam) ?: defaultWriteParam
        val output = getOutput() as ImageOutputStream
        val ri = image.renderedImage
        val encodedData = encode(writeParam.encoderOptions, ri)
        output.write(encodedData)
        output.flush()
    }

    @Suppress("VariableMinLength")
    companion object {

        private fun encode(options: WebPEncoderOptions, image: RenderedImage): ByteArray {
            // This prevents the JVM/GC from attempting to GC (during periods of high load) when it no longer sees any of the
            // variables being referred to any further, despite the underlying WebP library directly using them.
            // https://bitbucket.org/luciad/webp-imageio/pull-requests/3/prevent-webpencoderoptionss-finalizer/diff
            val encoderThreadLocal = ThreadLocal<WebPEncoderOptions>()
            return try {
                encoderThreadLocal.set(options)
                val encodeAlpha = hasTranslucency(image)
                if (encodeAlpha) {
                    val rgbaData = getRGBA(image)
                    WebPWrapper.encodeRGBA(options, rgbaData, image.width, image.height, image.width * 4)
                } else {
                    val rgbData = getRGB(image)
                    WebPWrapper.encodeRGB(options, rgbData, image.width, image.height, image.width * 3)
                }
            } finally {
                encoderThreadLocal.remove()
            }
        }

        private fun hasTranslucency(aRi: RenderedImage): Boolean {
            return aRi.colorModel.hasAlpha()
        }

        private fun getShift(aMask: Int): Int {
            var shift = 0
            while (aMask shr shift and 0x1 == 0) {
                shift++
            }
            return shift
        }

        private fun getRGB(aRi: RenderedImage): ByteArray {
            val width = aRi.width
            val height = aRi.height
            val colorModel = aRi.colorModel
            return if (colorModel is ComponentColorModel) {
                val sampleModel = aRi.sampleModel as ComponentSampleModel
                when (sampleModel.transferType) {
                    DataBuffer.TYPE_BYTE -> extractComponentRGBByte(
                        width,
                        height,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferByte,
                    )

                    DataBuffer.TYPE_INT -> extractComponentRGBInt(
                        width,
                        height,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferInt,
                    )

                    else -> throw IOException("Incompatible image: $aRi")
                }
            } else if (colorModel is DirectColorModel) {
                val sampleModel = aRi.sampleModel as SinglePixelPackedSampleModel
                val type = sampleModel.transferType
                if (type == DataBuffer.TYPE_INT) {
                    extractDirectRGBInt(
                        width,
                        height,
                        colorModel,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferInt,
                    )
                } else {
                    throw IOException("Incompatible image: $aRi")
                }
            } else {
                val i = BufferedImage(aRi.width, aRi.height, BufferedImage.TYPE_INT_RGB)
                val g = i.createGraphics()
                g.drawRenderedImage(aRi, AffineTransform())
                g.dispose()
                getRGB(i)
            }
        }

        private fun extractDirectRGBInt(
            aWidth: Int,
            aHeight: Int,
            aColorModel: DirectColorModel,
            aSampleModel: SinglePixelPackedSampleModel,
            aDataBuffer: DataBufferInt,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 3)
            val rMask = aColorModel.redMask
            val gMask = aColorModel.greenMask
            val bMask = aColorModel.blueMask
            val rShift = getShift(rMask)
            val gShift = getShift(gMask)
            val bShift = getShift(bMask)
            val bank = aDataBuffer.bankData[0]
            val scanlineStride = aSampleModel.scanlineStride
            var scanIx = 0
            var b = 0
            var y = 0
            while (y < aHeight) {
                var pixIx = scanIx
                var x = 0
                while (x < aWidth) {
                    val pixel = bank[pixIx++]
                    out[b] = (pixel and rMask ushr rShift).toByte()
                    out[b + 1] = (pixel and gMask ushr gShift).toByte()
                    out[b + 2] = (pixel and bMask ushr bShift).toByte()
                    x++
                    b += 3
                }
                scanIx += scanlineStride
                y++
            }
            return out
        }

        private fun extractComponentRGBInt(
            aWidth: Int,
            aHeight: Int,
            aSampleModel: ComponentSampleModel,
            aDataBuffer: DataBufferInt,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 3)
            val bankIndices = aSampleModel.bankIndices
            val rBank = aDataBuffer.bankData[bankIndices[0]]
            val gBank = aDataBuffer.bankData[bankIndices[1]]
            val bBank = aDataBuffer.bankData[bankIndices[2]]
            val bankOffsets = aSampleModel.bandOffsets
            var rScanIx = bankOffsets[0]
            var gScanIx = bankOffsets[1]
            var bScanIx = bankOffsets[2]
            val pixelStride = aSampleModel.pixelStride
            val scanlineStride = aSampleModel.scanlineStride
            var b = 0
            var y = 0
            while (y < aHeight) {
                var rPixIx = rScanIx
                var gPixIx = gScanIx
                var bPixIx = bScanIx
                var x = 0
                while (x < aWidth) {
                    out[b] = rBank[rPixIx].toByte()
                    rPixIx += pixelStride
                    out[b + 1] = gBank[gPixIx].toByte()
                    gPixIx += pixelStride
                    out[b + 2] = bBank[bPixIx].toByte()
                    bPixIx += pixelStride
                    x++
                    b += 3
                }
                rScanIx += scanlineStride
                gScanIx += scanlineStride
                bScanIx += scanlineStride
                y++
            }
            return out
        }

        private fun extractComponentRGBByte(
            aWidth: Int,
            aHeight: Int,
            aSampleModel: ComponentSampleModel,
            aDataBuffer: DataBufferByte,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 3)
            val bankIndices = aSampleModel.bankIndices
            val rBank = aDataBuffer.bankData[bankIndices[0]]
            val gBank = aDataBuffer.bankData[bankIndices[1]]
            val bBank = aDataBuffer.bankData[bankIndices[2]]
            val bankOffsets = aSampleModel.bandOffsets
            var rScanIx = bankOffsets[0]
            var gScanIx = bankOffsets[1]
            var bScanIx = bankOffsets[2]
            val pixelStride = aSampleModel.pixelStride
            val scanlineStride = aSampleModel.scanlineStride
            var b = 0
            var y = 0
            while (y < aHeight) {
                var rPixIx = rScanIx
                var gPixIx = gScanIx
                var bPixIx = bScanIx
                var x = 0
                while (x < aWidth) {
                    out[b] = rBank[rPixIx]
                    rPixIx += pixelStride
                    out[b + 1] = gBank[gPixIx]
                    gPixIx += pixelStride
                    out[b + 2] = bBank[bPixIx]
                    bPixIx += pixelStride
                    x++
                    b += 3
                }
                rScanIx += scanlineStride
                gScanIx += scanlineStride
                bScanIx += scanlineStride
                y++
            }
            return out
        }

        private fun getRGBA(aRi: RenderedImage): ByteArray {
            val width = aRi.width
            val height = aRi.height
            val colorModel = aRi.colorModel
            return if (colorModel is ComponentColorModel) {
                val sampleModel = aRi.sampleModel as ComponentSampleModel
                when (sampleModel.transferType) {
                    DataBuffer.TYPE_BYTE -> extractComponentRGBAByte(
                        width,
                        height,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferByte,
                    )

                    DataBuffer.TYPE_INT -> extractComponentRGBAInt(
                        width,
                        height,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferInt,
                    )

                    else -> throw IOException("Incompatible image: $aRi")
                }
            } else if (colorModel is DirectColorModel) {
                val sampleModel = aRi.sampleModel as SinglePixelPackedSampleModel
                val type = sampleModel.transferType
                if (type == DataBuffer.TYPE_INT) {
                    extractDirectRGBAInt(
                        width,
                        height,
                        colorModel,
                        sampleModel,
                        aRi.data.dataBuffer as DataBufferInt,
                    )
                } else {
                    throw IOException("Incompatible image: $aRi")
                }
            } else {
                val i = BufferedImage(aRi.width, aRi.height, BufferedImage.TYPE_INT_ARGB)
                val g = i.createGraphics()
                g.drawRenderedImage(aRi, AffineTransform())
                g.dispose()
                getRGBA(i)
            }
        }

        private fun extractDirectRGBAInt(
            aWidth: Int,
            aHeight: Int,
            aColorModel: DirectColorModel,
            aSampleModel: SinglePixelPackedSampleModel,
            aDataBuffer: DataBufferInt,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 4)
            val rMask = aColorModel.redMask
            val gMask = aColorModel.greenMask
            val bMask = aColorModel.blueMask
            val aMask = aColorModel.alphaMask
            val rShift = getShift(rMask)
            val gShift = getShift(gMask)
            val bShift = getShift(bMask)
            val aShift = getShift(aMask)
            val bank = aDataBuffer.bankData[0]
            val scanlineStride = aSampleModel.scanlineStride
            var scanIx = 0
            var b = 0
            var y = 0
            while (y < aHeight) {
                var pixIx = scanIx
                var x = 0
                while (x < aWidth) {
                    val pixel = bank[pixIx++]
                    out[b] = (pixel and rMask ushr rShift).toByte()
                    out[b + 1] = (pixel and gMask ushr gShift).toByte()
                    out[b + 2] = (pixel and bMask ushr bShift).toByte()
                    out[b + 3] = (pixel and aMask ushr aShift).toByte()
                    x++
                    b += 4
                }
                scanIx += scanlineStride
                y++
            }
            return out
        }

        private fun extractComponentRGBAInt(
            aWidth: Int,
            aHeight: Int,
            aSampleModel: ComponentSampleModel,
            aDataBuffer: DataBufferInt,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 4)
            val bankIndices = aSampleModel.bankIndices
            val rBank = aDataBuffer.bankData[bankIndices[0]]
            val gBank = aDataBuffer.bankData[bankIndices[1]]
            val bBank = aDataBuffer.bankData[bankIndices[2]]
            val aBank = aDataBuffer.bankData[bankIndices[3]]
            val bankOffsets = aSampleModel.bandOffsets
            var rScanIx = bankOffsets[0]
            var gScanIx = bankOffsets[1]
            var bScanIx = bankOffsets[2]
            var aScanIx = bankOffsets[3]
            val pixelStride = aSampleModel.pixelStride
            val scanlineStride = aSampleModel.scanlineStride
            var b = 0
            var y = 0
            while (y < aHeight) {
                var rPixIx = rScanIx
                var gPixIx = gScanIx
                var bPixIx = bScanIx
                var aPixIx = aScanIx
                var x = 0
                while (x < aWidth) {
                    out[b] = rBank[rPixIx].toByte()
                    rPixIx += pixelStride
                    out[b + 1] = gBank[gPixIx].toByte()
                    gPixIx += pixelStride
                    out[b + 2] = bBank[bPixIx].toByte()
                    bPixIx += pixelStride
                    out[b + 3] = aBank[aPixIx].toByte()
                    aPixIx += pixelStride
                    x++
                    b += 4
                }
                rScanIx += scanlineStride
                gScanIx += scanlineStride
                bScanIx += scanlineStride
                aScanIx += scanlineStride
                y++
            }
            return out
        }

        private fun extractComponentRGBAByte(
            aWidth: Int,
            aHeight: Int,
            aSampleModel: ComponentSampleModel,
            aDataBuffer: DataBufferByte,
        ): ByteArray {
            val out = ByteArray(aWidth * aHeight * 4)
            val bankIndices = aSampleModel.bankIndices
            val rBank = aDataBuffer.bankData[bankIndices[0]]
            val gBank = aDataBuffer.bankData[bankIndices[1]]
            val bBank = aDataBuffer.bankData[bankIndices[2]]
            val aBank = aDataBuffer.bankData[bankIndices[3]]
            val bankOffsets = aSampleModel.bandOffsets
            var rScanIx = bankOffsets[0]
            var gScanIx = bankOffsets[1]
            var bScanIx = bankOffsets[2]
            var aScanIx = bankOffsets[3]
            val pixelStride = aSampleModel.pixelStride
            val scanlineStride = aSampleModel.scanlineStride
            var b = 0
            var y = 0
            while (y < aHeight) {
                var rPixIx = rScanIx
                var gPixIx = gScanIx
                var bPixIx = bScanIx
                var aPixIx = aScanIx
                var x = 0
                while (x < aWidth) {
                    out[b] = rBank[rPixIx]
                    rPixIx += pixelStride
                    out[b + 1] = gBank[gPixIx]
                    gPixIx += pixelStride
                    out[b + 2] = bBank[bPixIx]
                    bPixIx += pixelStride
                    out[b + 3] = aBank[aPixIx]
                    aPixIx += pixelStride
                    x++
                    b += 4
                }
                rScanIx += scanlineStride
                gScanIx += scanlineStride
                bScanIx += scanlineStride
                aScanIx += scanlineStride
                y++
            }
            return out
        }
    }
}
