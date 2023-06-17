package com.luciad.imageio.webp

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.ImageWriter
import javax.imageio.stream.MemoryCacheImageInputStream

internal val imageWriter get() = ImageIO.getImageWritersByMIMEType("image/webp").requireWebpImageWriter()

internal val imageReader get() = ImageIO.getImageReadersByMIMEType("image/webp").requireWebpImageReader()

internal fun getImageReader(data: ByteArray): ImageReader {
    val stream = MemoryCacheImageInputStream(ByteArrayInputStream(data))

    return ImageIO.getImageReaders(stream).requireWebpImageReader()
}

internal fun Iterator<ImageReader>.requireWebpImageReader() = asSequence().single { it.originatingProvider is WebPImageReaderSpi }
internal fun Iterator<ImageWriter>.requireWebpImageWriter() = asSequence().single { it.originatingProvider is WebPImageWriterSpi }

private object ResourcesLoader

internal fun readResource(resource: String) = getResourceStream(resource).use { stream ->
    ByteArrayOutputStream().use { out ->
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            out.write(buffer, 0, bytesRead)
        }
        out.toByteArray()
    }
}

internal fun getResourceStream(resource: String) = checkNotNull(ResourcesLoader::class.java.classLoader.getResourceAsStream(resource)) {
    "Could not load resource $resource"
}

internal fun decompress(webp: ByteArray) = checkNotNull(getImageReader(webp))
    .apply { input = MemoryCacheImageInputStream(ByteArrayInputStream(webp)) }
    .read(0)
