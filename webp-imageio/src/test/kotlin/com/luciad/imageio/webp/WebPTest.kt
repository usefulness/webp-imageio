package com.luciad.imageio.webp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.Random
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.MemoryCacheImageInputStream
import javax.imageio.stream.MemoryCacheImageOutputStream

class WebPTest {

    @Test
    fun testFindReaderByMimeType() {
        val readers = ImageIO.getImageReadersByMIMEType("image/webp")

        assertThat(readers.requireWebpImageReader()).isNotNull()
    }

    @Test
    fun testFindReaderByFormatName() {
        val readers = ImageIO.getImageReadersByFormatName("webp")

        assertThat(readers.requireWebpImageReader()).isNotNull()
    }

    @Test
    fun testFindReaderBySuffix() {
        val readers = ImageIO.getImageReadersBySuffix("webp")

        assertThat(readers.requireWebpImageReader()).isNotNull()
    }

    @Test
    fun testFindWriterByMimeType() {
        val writer = ImageIO.getImageWritersByMIMEType("image/webp")

        assertThat(writer.requireWebpImageWriter()).isNotNull()
    }

    @Test
    fun testFindWriterByFormatName() {
        val writer = ImageIO.getImageWritersByFormatName("webp")
        assertThat(writer.requireWebpImageWriter()).isNotNull()
    }

    @Test
    fun testFindWriterBySuffix() {
        val writer = ImageIO.getImageWritersBySuffix("webp")
        assertThat(writer.requireWebpImageWriter()).isNotNull()
    }

    @Test
    fun testDecompressLossy() {
        val webpData = readResource("lossy.webp")
        val image = decompress(webpData)

        assertThat(image.width).isEqualTo(1024)
        assertThat(image.height).isEqualTo(752)
    }

    @Test
    fun testDecompressLossless() {
        val webpData = readResource("lossless.webp")
        val image = decompress(webpData)

        assertThat(image.width).isEqualTo(400)
        assertThat(image.height).isEqualTo(301)
    }

    @Test
    fun testDecompressLossyAlpha() {
        val webpData = readResource("lossy_alpha.webp")
        val image = decompress(webpData)

        assertThat(image.width).isEqualTo(400)
        assertThat(image.height).isEqualTo(301)
    }

    @Test
    fun testCompress() {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
        val data = ByteArrayOutputStream().use { out ->
            MemoryCacheImageOutputStream(out).use { imageOut ->
                imageWriter
                    .apply { output = imageOut }
                    .write(image)
            }
            out.toByteArray()
        }

        assertThat(data.size).isNotEqualTo(0)
        assertThat('R'.code).isEqualTo(data[0].toInt() and 0xFF)
        assertThat('I'.code).isEqualTo(data[1].toInt() and 0xFF)
        assertThat('F'.code).isEqualTo(data[2].toInt() and 0xFF)
        assertThat('F'.code).isEqualTo(data[3].toInt() and 0xFF)
    }

    @Test
    fun testRoundTrip() {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
        val rng = Random(42)
        val buffer = (image.data.dataBuffer as DataBufferInt).data
        for (i in buffer.indices) {
            buffer[i] = rng.nextInt()
        }
        val out = ByteArrayOutputStream()
        val imageOut = MemoryCacheImageOutputStream(out)
        val writer = imageWriter
        writer.output = imageOut
        val writeParam = writer.defaultWriteParam as WebPWriteParam
        writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
        writeParam.compressionType = "Lossless"
        writer.write(null, IIOImage(image, null, null), writeParam)
        imageOut.close()
        out.close()
        val webpData = out.toByteArray()
        val reader = imageReader
        reader.input = MemoryCacheImageInputStream(ByteArrayInputStream(webpData))
        val decodedImage = reader.read(0)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                assertThat(image.getRGB(x, y)).isEqualTo(decodedImage.getRGB(x, y))
            }
        }
    }

    @Test
    @Disabled("useSharpYUV doesn't make any difference")
    fun sharpYuv(@TempDir tempDir: Path) {
        val inputImage = ImageIO.read(getResourceStream("test4.png"))
        val outputFileWithSharpYuv = tempDir.resolve("output_sharp_yuv.webp").toFile()
        val outputFileDefault = tempDir.resolve("output_default.webp").toFile()

        val writerWithSharpYuv = imageWriter.apply { output = FileImageOutputStream(outputFileWithSharpYuv) }
        val writerDefault = imageWriter.apply { output = FileImageOutputStream(outputFileDefault) }

        val writeParamWithSharpYuv = WebPWriteParam(writerWithSharpYuv.locale).apply {
            useSharpYUV = true
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionType = compressionTypes[WebPWriteParam.LOSSY_COMPRESSION]
            compressionQuality = 0.95f
        }
        val writeParamDefault = WebPWriteParam(writerDefault.locale).apply {
            useSharpYUV = false
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionType = compressionTypes[WebPWriteParam.LOSSY_COMPRESSION]
            compressionQuality = 0.95f
        }

        writerWithSharpYuv.write(null, IIOImage(inputImage, null, null), writeParamWithSharpYuv)
        writerDefault.write(null, IIOImage(inputImage, null, null), writeParamDefault)

        val outputImageWithYuv = ImageIO.read(outputFileWithSharpYuv)
        val outputImageDefault = ImageIO.read(outputFileDefault)
        assertThat(outputImageWithYuv).isNotNull()
        assertThat(outputImageWithYuv).usingComparator(::imagesComparator).isNotEqualTo(outputImageDefault)
        assertThat(outputFileWithSharpYuv.length()).isNotEqualTo(outputFileDefault.length())
    }

    private fun imagesComparator(img1: BufferedImage, img2: BufferedImage): Int {
        if (img1.width == img2.width && img1.height == img2.height) {
            for (x in 0 until img1.width) {
                for (y in 0 until img1.height) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return img1.getRGB(x, y) - img2.getRGB(x, y)
                    }
                }
            }
            return 0
        } else {
            error("they are not equal at all 🤷‍")
        }
    }
}
