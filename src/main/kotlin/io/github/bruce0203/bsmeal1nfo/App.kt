package io.github.bruce0203.bsmeal1nfo

import com.github.instagram4j.instagram4j.requests.media.MediaConfigureTimelineRequest
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import javax.imageio.ImageIO
import kotlin.system.exitProcess
import kotlin.math.*

fun main(args: Array<String>) {
    publish()
}

fun publish() {
    val gitService = GitService()
    val postService = PostService(gitService)
    val client = login()
    val newPosts = postService.getPosts()


    newPosts.forEach {

        val image = (if (it.images?.size == 0) createImg(it.title).readBytes() else {
            val imgFile = it.images?.get(0)
            val jpgFile = File("${imgFile?.nameWithoutExtension}.jpg")
            pngToJpg(imgFile!!, jpgFile)
            ImageIO.write(squareImage(ImageIO.read(jpgFile), BufferedImage.TYPE_INT_RGB), "jpg", jpgFile)
            jpgFile.readBytes()
        })
        val options = MediaConfigureTimelineRequest.MediaConfigurePayload()
                .caption(
                        """
                `${it.title}` 새로운 글이 업로드됐어요!
                
                ${
                            it.content.substring(
                                    0,
                                    200.coerceAtMost(it.content.length)
                            )
                        }
                ...

                더 보려면 프로필의 블로그 링크에 접속해주세요!
                이 글은 봇에 의해 자동 작성된 글입니다. - Developed by KimWash
            """.trimIndent()
                )

        client.actions()
                .timeline()
                .uploadPhoto(image, options)
                .thenAccept {
                    println(
                            """
                    --------------------------
                   "Successfully uploaded!"
                    --------------------------
                """.trimIndent()
                    )
                }
                .join() // block current thread until complete
    }
    exitProcess(0)
}

