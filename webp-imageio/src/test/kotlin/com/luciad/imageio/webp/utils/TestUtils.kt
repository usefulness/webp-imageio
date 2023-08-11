package com.luciad.imageio.webp.utils

import com.luciad.imageio.webp.WebPImageReaderSpi
import com.luciad.imageio.webp.WebPImageWriterSpi
import com.luciad.imageio.webp.WebPWriteParam
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageInputStream
import javax.imageio.stream.MemoryCacheImageInputStream

internal fun Iterator<ImageReader>.requireWebpImageReader() = asSequence().single { it.originatingProvider is WebPImageReaderSpi }
internal fun Iterator<ImageWriter>.requireWebpImageWriter() = asSequence().single { it.originatingProvider is WebPImageWriterSpi }

internal fun readImage(webp: ByteArray, param: ImageReadParam? = null) =
    MemoryCacheImageInputStream(ByteArrayInputStream(webp)).use { input ->
        getImageReader(webp)
            .apply { this.input = input }
            .read(0, param)
    }

private fun getImageReader(data: ByteArray): ImageReader {
    val stream = MemoryCacheImageInputStream(ByteArrayInputStream(data))

    return ImageIO.getImageReaders(stream).requireWebpImageReader()
}

internal fun writeWebpImage(input: BufferedImage, target: ImageInputStream, params: WebPWriteParam.() -> Unit = { }) =
    ImageIO.getImageWritersByMIMEType("image/webp")
        .asSequence()
        .single()
        .run {
            output = target
            val updated = (defaultWriteParam as WebPWriteParam).apply(params)
            write(null, IIOImage(input, null, null), updated)
        }

internal fun writeWebpImage(input: BufferedImage, target: File, params: WebPWriteParam.() -> Unit = { }) =
    FileImageOutputStream(target).use { stream ->
        writeWebpImage(
            input = input,
            target = stream,
            params = params,
        )
    }
