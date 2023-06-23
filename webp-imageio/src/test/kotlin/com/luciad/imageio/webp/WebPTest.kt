package com.luciad.imageio.webp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.Random
import javax.imageio.ImageIO
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
    fun readLossy() {
        val image = readImage(readResource("lossy.webp"))

        assertThat(image.width).isEqualTo(1024)
        assertThat(image.height).isEqualTo(752)
    }

    @Test
    fun readLossless() {
        val image = readImage(readResource("lossless.webp"))

        assertThat(image.width).isEqualTo(400)
        assertThat(image.height).isEqualTo(301)
    }

    @Test
    fun readLossyWithAlpha() {
        val image = readImage(readResource("lossy_alpha.webp"))

        assertThat(image.width).isEqualTo(400)
        assertThat(image.height).isEqualTo(301)
    }

    @Test
    fun testCompress() {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
        val data = ByteArrayOutputStream().use { out ->
            MemoryCacheImageOutputStream(out).use { imageOut ->
                writeWebpImage(
                    input = image,
                    target = imageOut,
                )
            }
            out.toByteArray()
        }

        assertThat(data.size).isNotEqualTo(0)
        assertThat(data[0].toInt() and 0xFF).isEqualTo('R'.code)
        assertThat(data[1].toInt() and 0xFF).isEqualTo('I'.code)
        assertThat(data[2].toInt() and 0xFF).isEqualTo('F'.code)
        assertThat(data[3].toInt() and 0xFF).isEqualTo('F'.code)
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

        writeWebpImage(
            input = image,
            target = imageOut,
            params = {
                compressionType = CompressionType.Lossless
            },
        )
        imageOut.close()
        out.close()
        val webpData = out.toByteArray()

        val decodedImage = readImage(webpData)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                assertThat(image.getRGB(x, y)).isEqualTo(decodedImage.getRGB(x, y))
            }
        }
    }

    @Test
    fun canUseAllReadOptions() {
        val readParam = WebPReadParam().apply {
            bypassFiltering = true
            noFancyUpsampling = true
            useCropping = true
            cropLeft = 1
            cropTop = 1
            cropWidth = 380
            cropHeight = 260
            useScaling = true
            scaledWidth = 200
            scaledHeight = 150
            useThreads = true
            ditheringStrength = 20
            flipVertically = true
            alphaDitheringStrength = 1
        }
        val output = readImage(
            webp = readResource("lossless.webp"),
            param = readParam,
        )

        assertThat(output).isNotNull()
        assertThat(output.width).isEqualTo(200)
        assertThat(output.height).isEqualTo(150)
    }

    @Test
    fun canUseAllWriteOptions(@TempDir tempDir: Path) {
        val inputImage = ImageIO.read(getResourceStream("test4.png"))
        val outputFile = tempDir.resolve("output.webp").toFile()

        writeWebpImage(
            input = inputImage,
            target = outputFile,
            params = {
                compressionQuality = 1f
                compressionType = CompressionType.Lossy
                method = 6
                targetSize = 0
                targetPSNR = 0f
                segments = 4
                snsStrength = 2
                filterStrength = 2
                filterSharpness = 7
                filterType = 1
                autoAdjustFilterStrength = false
                alphaCompressionAlgorithm = 1
                alphaFiltering = 1
                alphaQuality = 100
                entropyAnalysisPassCount = 7
                showCompressed = false
                preprocessing = 0
                partitions = 0
                partitionLimit = 0
                emulateJpegSize = false
                threadLevel = 0
                lowMemory = false
                nearLossless = 100
                exact = false
                useDeltaPalette = false
                useSharpYUV = true
                qMin = 0
                qMax = 0
            },
        )
    }

    @Test
    fun sharpYuv(@TempDir tempDir: Path) {
        val inputImage = ImageIO.read(getResourceStream("test4.png"))
        val outputFileWithSharpYuv = tempDir.resolve("output_sharp_yuv.webp").toFile()
        val outputFileDefault = tempDir.resolve("output_default.webp").toFile()

        fun WebPWriteParam.prepareParams() {
            compressionType = CompressionType.Lossy
            compressionQuality = 0.95f
        }
        writeWebpImage(
            input = inputImage,
            target = outputFileWithSharpYuv,
            params = {
                prepareParams()
                useSharpYUV = true
            },
        )
        writeWebpImage(
            input = inputImage,
            target = outputFileDefault,
            params = {
                prepareParams()
                useSharpYUV = false
            },
        )

        val outputImageWithYuv = ImageIO.read(outputFileWithSharpYuv)
        val referenceWithYuv = ImageIO.read(getResourceStream("test4_sharp.webp"))
        val outputImageDefault = ImageIO.read(outputFileDefault)
        val referenceDefault = ImageIO.read(getResourceStream("test4_default.webp"))

        assertThat(outputImageDefault).usingComparator(::imagesComparator).isEqualTo(referenceDefault)
        assertThat(outputImageWithYuv).usingComparator(::imagesComparator).isEqualTo(referenceWithYuv)
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
