package com.luciad.imageio.webp.internal

import java.io.InputStream

internal fun findProperty(key: String): String? = System.getProperty(key)

internal fun runProcess(vararg args: String) = Runtime.getRuntime().exec(args).run {
    val exitCode = waitFor()
    exitCode to inputStream.use(InputStream::readBytes)
        .decodeToString()
        .trim()
}
