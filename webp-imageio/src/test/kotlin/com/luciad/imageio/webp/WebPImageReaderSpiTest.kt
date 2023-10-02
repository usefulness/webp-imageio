package com.luciad.imageio.webp

import com.luciad.imageio.webp.utils.getImageReader
import com.luciad.imageio.webp.utils.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import javax.imageio.stream.MemoryCacheImageInputStream

class WebPImageReaderSpiTest {

    @Test
    fun throwsProperException() {
        val result = runCatching {
            val resource = readResource("lossy.webp")
            MemoryCacheImageInputStream(ByteArrayInputStream(resource)).use { input ->
                getImageReader(resource)
                    .apply { this.input = input }
                    .read(2, null)
            }
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IndexOutOfBoundsException::class.java)
    }
}
