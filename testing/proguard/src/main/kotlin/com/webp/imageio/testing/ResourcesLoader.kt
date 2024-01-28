package com.webp.imageio.testing

private object ResourcesLoader

internal fun getResourceStream(resource: String) = checkNotNull(ResourcesLoader::class.java.classLoader.getResourceAsStream(resource)) {
    "Could not load resource $resource"
}
