package com.luciad.imageio.webp.utils

import java.io.ByteArrayOutputStream

private object ResourcesLoader

internal fun readResource(resource: String) = getResourceStream(resource).use { stream ->
    ByteArrayOutputStream().use { out ->
        stream.copyTo(out)
        out.toByteArray()
    }
}

internal fun getResourceStream(resource: String) = checkNotNull(ResourcesLoader::class.java.classLoader.getResourceAsStream(resource)) {
    "Could not load resource $resource"
}
