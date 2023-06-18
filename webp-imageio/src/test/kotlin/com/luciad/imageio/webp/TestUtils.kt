package com.luciad.imageio.webp

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.MemoryCacheImageInputStream

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

internal fun readImage(webp: ByteArray, param: ImageReadParam? = null) = checkNotNull(getImageReader(webp))
    .apply { input = MemoryCacheImageInputStream(ByteArrayInputStream(webp)) }
    .read(0, param)

private fun getImageReader(data: ByteArray): ImageReader {
    val stream = MemoryCacheImageInputStream(ByteArrayInputStream(data))

    return ImageIO.getImageReaders(stream).requireWebpImageReader()
}

internal fun writeWebpImage(input: BufferedImage, target: Any, params: WebPWriteParam.() -> Unit = { }) =
    ImageIO.getImageWritersByMIMEType("image/webp")
        .asSequence()
        .single()
        .apply { output = target }
        .run {
            val updated = (defaultWriteParam as WebPWriteParam).apply(params)
            write(null, IIOImage(input, null, null), updated)
        }

internal fun writeWebpImage(input: BufferedImage, target: File, params: WebPWriteParam.() -> Unit = { }) = writeWebpImage(
    input = input,
    target = FileImageOutputStream(target),
    params = params,
)
