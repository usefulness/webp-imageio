package com.luciad.imageio.webp.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NativeLoaderTest {

    @BeforeEach
    fun setUp() {
        NativeLoader.initialize()
    }

    @AfterEach
    fun tearDown() {
        NativeLoader.cleanup()
    }

    @Test
    fun checkVersion() {
        assertThat(NativeLoader.getVersion()).isNotEqualTo("unknown")
    }
}
