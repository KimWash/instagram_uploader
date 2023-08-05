package io.github.bruce0203.bsmeal1nfo

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max


fun createImg(title: String): File {
    val png = File("output/dist.png")
    val img = object {}.javaClass.classLoader.getResourceAsStream("image/image.png")
    AddTextToImg.execute(img, "새로운 글\n${title}", png)
    val jpg = File("output/dist.jpg")
    pngToJpg(png, jpg)
    return jpg
}

fun squareImage(image: BufferedImage, type: Int): BufferedImage? {
    if (image.width == image.height && image.width >= 800 && image.type == type) return image
    val size = max(800, max(image.width, image.height))
    val res = BufferedImage(size, size, type)
    val g2 = res.createGraphics()
    val ratio = size / max(image.width, image.height).toDouble()
    val width = (image.width * ratio).toInt()
    val height = (image.height * ratio).toInt()
    g2.drawImage(image, res.width / 2 - width / 2, res.height / 2 - height / 2, width, height, null)
    g2.dispose()
    return res
}

fun pngToJpg(png: File, jpg: File) {
    val beforeImg = ImageIO.read(png)
    val afterImg = BufferedImage(beforeImg.width, beforeImg.height, BufferedImage.TYPE_INT_RGB)
    afterImg.createGraphics().drawImage(beforeImg, 0, 0, Color.white, null)
    ImageIO.write(afterImg, "jpg", jpg)
}