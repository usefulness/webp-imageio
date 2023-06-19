package com.luciad.imageio.webp.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OsInfoTest {

    @Test
    fun smokeTest() {
        assertThat(OSInfo.getNativeLibFolderPathForCurrentOS()).isNotEqualTo("/")
    }
}
