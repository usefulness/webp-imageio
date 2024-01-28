@file:JvmName("Main")

package com.webp.imageio.testing

import javax.imageio.ImageIO

fun main() {
    val input = getResourceStream("lossless.webp")
    val image = ImageIO.read(input)

    if(image?.width == 400 && image.height == 301) {
        println("All good 👍")
    } else {
        error("The image didn't load correctly. width=${image?.width}, height=${image?.height}")
    }
}
