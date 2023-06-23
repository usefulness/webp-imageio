package com.luciad.imageio.webp.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class NativeLoaderTest {

    private val log = Logger.getLogger(NativeLoaderTest::class.java.name)

    @BeforeEach
    fun setUp() {
        check(NativeLoader.initialize())
    }

    @AfterEach
    fun tearDown() {
        NativeLoader.cleanup()
    }

    @Test
    fun checkVersion() {
        log.info("Native version is: ${NativeLoader.version}")
        assertThat(NativeLoader.version).isNotEqualTo("unknown")
    }
}
