package com.luciad.imageio.webp

import com.luciad.imageio.webp.util.OSInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OsInfoTest {

    @Test
    fun smokeTest() {
        assertThat(OSInfo.getNativeLibFolderPathForCurrentOS()).isNotEqualTo("/")
    }
}
