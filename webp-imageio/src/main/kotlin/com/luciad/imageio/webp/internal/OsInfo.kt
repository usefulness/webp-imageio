/*--------------------------------------------------------------------------
 *  Copyright 2008 Taro L. Saito
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
// sqlite-jdbc Project
//
// OSInfo.java
// Since: May 20, 2008
//
// $URL$
// $Author$
// --------------------------------------
package com.luciad.imageio.webp.internal

import java.io.IOException

internal object OsInfo {
    private const val X86 = "x86"
    private const val X86_64 = "x86_64"
    private const val IA64_32 = "ia64_32"
    private const val IA64 = "ia64"
    private const val PPC = "ppc"
    private const val PPC64 = "ppc64"

    @Suppress("ktlint:standard:value-argument-comment")
    private val archMapping = mapOf(
        // x86 mappings
        X86 to X86,
        "i386" to X86,
        "i486" to X86,
        "i586" to X86,
        "i686" to X86,
        "pentium" to X86,

        // x86_64 mappings
        X86_64 to X86_64,
        "amd64" to X86_64,
        "em64t" to X86_64,
        "universal" to X86_64, // Needed for openjdk to Mac,

        // Itenium 64-bit mappings
        IA64 to IA64,
        "ia64w" to IA64,

        // Itenium 32-bit mappings, usually an HP-UX construct
        IA64_32 to IA64_32,
        "ia64n" to IA64_32,

        // PowerPC mappings
        PPC to PPC,
        "power" to PPC,
        "powerpc" to PPC,
        "power_pc" to PPC,
        "power_rs" to PPC,

        // TODO: PowerPC 64bit mappings
        PPC64 to PPC64,
        "power64" to PPC64,
        "powerpc64" to PPC64,
        "power_pc64" to PPC64,
        "power_rs64" to PPC64,
        "ppc64el" to PPC64,
        "ppc64le" to PPC64,
    )

    @JvmStatic
    fun main(arg: Array<String>) {
        println("osName=$oSName")
        println("archName=$archName")
        println("hardwareName=$hardwareName")
        println("isAlpine=$isAlpine")
        println("nativeLibFolderPathForCurrentOS=$nativeLibFolderPathForCurrentOS")
    }

    @JvmStatic
    val nativeLibFolderPathForCurrentOS
        get() = "$oSName/$archName"

    @JvmStatic
    val oSName
        get() = translateOSNameToFolderName(findProperty("os.name").orEmpty())

    private val isAndroid
        get() = findProperty("java.runtime.name").orEmpty().lowercase().contains("android")

    private val isAlpine: Boolean
        get() {
            return runCatching {
                val (_, osReleaseOutput) = runProcess("cat /etc/os-release | grep ^ID")

                osReleaseOutput.contains("alpine", ignoreCase = true)
            }
                .getOrDefault(false)
        }

    @Suppress("TooGenericExceptionCaught")
    private val hardwareName: String
        get() = runCatching { runProcess("uname", "-m").second }
            .onFailure { System.err.println("Error while running uname -m: ${it.message}") }
            .getOrDefault("unknown")

    private fun resolveArmArchType(): String {
        if (findProperty("os.name").orEmpty().contains("Linux")) {
            val armType = hardwareName

            // armType (uname -m) can be armv5t, armv5te, armv5tej, armv5tejl, armv6, armv7, armv7l, aarch64, i686
            @Suppress("ktlint:standard:blank-line-between-when-conditions")
            when {
                armType.startsWith("armv6") -> return "armv6" // Raspberry PI
                armType.startsWith("armv7l") -> return "armv7l"
                armType.startsWith("armv7") -> return "armv7" // Generic
                armType.startsWith("armv5") -> return "arm" // Use armv5, soft-float ABI
                armType == "aarch64" -> return "arm64" // Use arm64
                else -> Unit
            }

            // Java 1.8 introduces a system property to determine armel or armhf
            // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8005545
            val abi = findProperty("sun.arch.abi").orEmpty()
            if (abi.startsWith("gnueabihf")) {
                return "armv7"
            }

            // For java7, we still need to if run some shell commands to determine ABI of JVM
            val javaHome = findProperty("java.home").orEmpty()
            try {
                // determine if first JVM found uses ARM hard-float ABI
                val (readelfExitCode, _) = runProcess("which", "readelf")
                if (readelfExitCode == 0) {
                    val cmdArray = arrayOf(
                        "/bin/sh",
                        "-c",
                        "find '$javaHome' -name 'libjvm.so' | head -1 | xargs readelf -A | grep 'Tag_ABI_VFP_args: VFP registers'",
                    )
                    val (abiFinderExitCode, _) = runProcess(*cmdArray)
                    if (abiFinderExitCode == 0) {
                        return "armv7"
                    }
                } else {
                    System.err.println(
                        "WARNING! readelf not found. Cannot check if running on an armhf system, armel architecture will be presumed.",
                    )
                }
            } catch (_: IOException) {
                // ignored: fall back to "arm" arch (soft-float ABI)
            } catch (_: InterruptedException) {
            }
        }
        // Use armv5, soft-float ABI
        return "arm"
    }

    @JvmStatic
    val archName: String?
        get() {
            var osArch = findProperty("os.arch").orEmpty()
            // For Android
            if (isAndroid) {
                return "android-arm"
            }
            if (osArch.startsWith("arm")) {
                osArch = resolveArmArchType()
            } else {
                val lc = osArch.lowercase()
                if (archMapping.containsKey(lc)) {
                    return archMapping[lc]
                }
            }
            return translateArchNameToFolderName(osArch)
        }

    private fun translateOSNameToFolderName(osName: String) = when {
        osName.contains("Windows") -> "Windows"
        osName.contains("Mac") || osName.contains("Darwin") -> "Mac"
        isAlpine -> "Linux-Alpine"
        osName.contains("Linux") -> "Linux"
        osName.contains("AIX") -> "AIX"
        else -> osName.replace("\\W".toRegex(), "")
    }

    private fun translateArchNameToFolderName(archName: String) = archName.replace("\\W".toRegex(), "")
}
