/*--------------------------------------------------------------------------
 *  Copyright 2007 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
// --------------------------------------
// SQLite JDBC Project
//
// SQLite.java
// Since: 2007/05/10
//
// $URL$
// $Author$
// --------------------------------------
package com.luciad.imageio.webp.internal

import com.luciad.imageio.webp.internal.OsInfo.archName
import com.luciad.imageio.webp.internal.OsInfo.nativeLibFolderPathForCurrentOS
import com.luciad.imageio.webp.internal.OsInfo.oSName
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Properties
import java.util.UUID

/**
 * The library files are automatically extracted from this project's package (JAR).
 *
 * usage: call [initialize] before using the library.
 *
 * @author leo
 */
internal object NativeLoader {
    private var extracted = false

    /**
     * Loads native library.
     *
     * @return True if native library is successfully loaded; false
     * otherwise.
     */
    @JvmStatic
    @Synchronized
    fun initialize(): Boolean {
        // only cleanup before the first extract
        if (!extracted) {
            cleanup()
        }
        loadNativeLibrary()
        return extracted
    }

    private val tempDir: File
        get() = File(
            findProperty("com.luciad.imageio.webp.tmpdir")
                ?: findProperty("java.io.tmpdir")
                ?: error("Neither `com.luciad.imageio.webp.tmpdir` nor `java.io.tmpdir` was found."),
        )

    /**
     * Deleted old native libraries e.g. on Windows the DLL file is not removed
     * on VM-Exit (bug #80)
     */
    @JvmStatic
    internal fun cleanup() {
        val tempFolder = tempDir.absolutePath
        val dir = File(tempFolder)

        dir.listFiles { _, name -> name.startsWith("webp-imageio-$version") && !name.endsWith(".lck") }
            ?.forEach { nativeLibFile ->
                val lckFile = File(nativeLibFile.absolutePath + ".lck")
                if (!lckFile.exists()) {
                    try {
                        nativeLibFile.delete()
                    } catch (e: SecurityException) {
                        System.err.println("Failed to delete old native lib ${e.message}")
                    }
                }
            }
    }

    private fun contentsEquals(in1: InputStream, in2: InputStream): Boolean {
        val input1 = if (in1 is BufferedInputStream) in1 else BufferedInputStream(in1)
        val input2 = if (in2 is BufferedInputStream) in2 else BufferedInputStream(in2)

        var ch = input1.read()
        while (ch != -1) {
            val ch2 = input2.read()
            if (ch != ch2) {
                return false
            }
            ch = input1.read()
        }
        val ch2 = input2.read()
        return ch2 == -1
    }

    /**
     * Extracts and loads the specified library file to the target folder
     *
     * @param libFolderForCurrentOS Library path.
     * @param libraryFileName       Library name.
     * @param targetFolder          Target folder.
     * @return
     */
    private fun extractAndLoadLibraryFile(libFolderForCurrentOS: String, libraryFileName: String, targetFolder: String): Boolean {
        val nativeLibraryFilePath = "$libFolderForCurrentOS/$libraryFileName"
        // Include architecture name in temporary filename in order to avoid conflicts
        // when multiple JVMs with different architectures running at the same time
        val uuid = UUID.randomUUID().toString()
        val extractedLibFileName = "webp-imageio-$version-$uuid-$libraryFileName"
        val extractedLckFileName = "$extractedLibFileName.lck"
        val extractedLibFile = File(targetFolder, extractedLibFileName)
        val extractedLckFile = File(targetFolder, extractedLckFileName)
        return try {
            // Extract a native library file into the target directory
            NativeLoader::class.java.getResourceAsStream(nativeLibraryFilePath).let(::checkNotNull).use { reader ->
                if (!extractedLckFile.exists()) {
                    FileOutputStream(extractedLckFile).close()
                }
                FileOutputStream(extractedLibFile).use { writer -> reader.copyTo(writer) }
            }

            // Delete the extracted lib file on JVM exit.
            extractedLibFile.deleteOnExit()
            extractedLckFile.deleteOnExit()

            // Set executable (x) flag to enable Java to load the native library
            extractedLibFile.setReadable(true)
            extractedLibFile.setWritable(true, true)
            extractedLibFile.setExecutable(true)

            // Check whether the contents are properly copied from the resource folder
            NativeLoader::class.java.getResourceAsStream(nativeLibraryFilePath).let(::checkNotNull).use { nativeIn ->
                FileInputStream(extractedLibFile).use { extractedLibIn ->
                    if (!contentsEquals(nativeIn, extractedLibIn)) {
                        error("Failed to write a native library file at $extractedLibFile")
                    }
                }
            }

            loadNativeLibrary(targetFolder, extractedLibFileName)
        } catch (e: IOException) {
            System.err.println(e.message)
            false
        }
    }

    /**
     * Loads native library using the given path and name of the library.
     *
     * @param path Path of the native library.
     * @param name Name  of the native library.
     * @return True for successfully loading; false otherwise.
     */
    private fun loadNativeLibrary(path: String, name: String): Boolean {
        val libPath = File(path, name)
        return if (libPath.exists()) {
            try {
                System.load(File(path, name).absolutePath)
                true
            } catch (e: UnsatisfiedLinkError) {
                System.err.println("Failed to load native library:$name. osinfo:$nativeLibFolderPathForCurrentOS")
                System.err.println(e)
                false
            }
        } else {
            false
        }
    }

    /**
     * Loads native library using given path and name of the library.
     *
     * @throws
     */
    private fun loadNativeLibrary() {
        if (extracted) {
            return
        }
        val triedPaths = mutableListOf<String>()
        val nativeLibraryName = System.mapLibraryName("webp-imageio")

        // Load the os-dependent library from the jar file
        val nativeLibraryPath = "/native/$nativeLibFolderPathForCurrentOS"
        val hasNativeLib = hasResource("$nativeLibraryPath/$nativeLibraryName")
        if (hasNativeLib) {
            // temporary library folder
            val tempFolder = tempDir.absolutePath
            // Try extracting the library from jar
            if (extractAndLoadLibraryFile(nativeLibraryPath, nativeLibraryName, tempFolder)) {
                extracted = true
                return
            } else {
                triedPaths.add(nativeLibraryPath)
            }
        }

        // As a last resort try from java.library.path
        val javaLibraryPath = findProperty("java.library.path").orEmpty()
        for (ldPath in javaLibraryPath.split(File.pathSeparator.toRegex()).dropLastWhile(String::isEmpty)) {
            if (ldPath.isEmpty()) {
                continue
            }
            if (loadNativeLibrary(ldPath, nativeLibraryName)) {
                extracted = true
                return
            } else {
                triedPaths.add(ldPath)
            }
        }
        extracted = false
        error(
            "No native library found for os.name=$oSName, os.arch=$archName, paths=[${
                triedPaths.joinToString(separator = File.pathSeparator)
            }]",
        )
    }

    private fun hasResource(path: String) = NativeLoader::class.java.getResource(path) != null

    /**
     * @return The version of the library.
     */
    @JvmStatic
    val version: String by lazy {
        try {
            val versionFile = NativeLoader::class.java.classLoader.getResource("webp-imageio.properties")
            if (versionFile != null) {
                val versionData = Properties().apply { load(versionFile.openStream()) }
                versionData.getProperty("webp_imageio_version", null)
                    .trim()
                    .replace("[^0-9.]".toRegex(), "")
            } else {
                null
            }
        } catch (e: IOException) {
            System.err.println(e)
            null
        } ?: "unknown"
    }
}
