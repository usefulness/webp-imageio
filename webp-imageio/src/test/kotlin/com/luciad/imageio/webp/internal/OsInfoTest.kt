package com.luciad.imageio.webp.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class OsInfoTest {

    private val log = Logger.getLogger(OsInfoTest::class.java.name)

    @Test
    fun smokeTest() {
        log.info("Native lib folder path for current OS is: ${OSInfo.getNativeLibFolderPathForCurrentOS()}")
        assertThat(OSInfo.getNativeLibFolderPathForCurrentOS()).isNotEqualTo("/")
    }
}
