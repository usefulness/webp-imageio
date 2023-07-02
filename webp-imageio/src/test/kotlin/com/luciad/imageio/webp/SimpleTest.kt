package com.luciad.imageio.webp

import com.luciad.imageio.webp.utils.readImage
import com.luciad.imageio.webp.utils.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleTest {

    /**
     * For some reason this reproduces the issue from https://github.com/usefulness/webp-imageio/issues/111
     */
    @Test
    fun readLossy() {
        val image = readImage(readResource("lossy.webp"))

        assertThat(image.width).isEqualTo(1024)
        assertThat(image.height).isEqualTo(752)
    }
}
