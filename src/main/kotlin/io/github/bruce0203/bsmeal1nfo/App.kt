package io.github.bruce0203.bsmeal1nfo

import com.github.instagram4j.instagram4j.requests.media.MediaConfigureTimelineRequest
import java.text.SimpleDateFormat
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    publish()
}

fun publish() {
    val gitService = GitService()
    val postService = PostService(gitService)
    val client = login()
    val newPosts = postService.getPosts()

    newPosts.forEach {

        val image = if (it.images?.size == 0) createImg(it.title).readBytes() else it.images?.get(0)
        val options = MediaConfigureTimelineRequest.MediaConfigurePayload()
            .caption(
                """
                `${it.title}` 새로운 글이 업로드됐어요!
                ${
                    it.content.substring(
                        0,
                        200
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

