package io.github.bruce0203.bsmeal1nfo

import java.io.File
import java.lang.RuntimeException

class PostService(
    private val gitService: GitService
) {

    private fun readFiles(repoPath: String, targetFiles: List<String>): List<Post> {
        val filesContent = mutableListOf<Post>()
        for (fileRelativePath in targetFiles) {
            val fileAbsolutePath = File(repoPath, fileRelativePath)
            val fileContent = fileAbsolutePath.readText()
            val regexTitle = """title:\s*"([^"]+)"""".toRegex()
            val regexDate = """date:\s*"([^"]+)"""".toRegex()
            val regexCodeBlocks = """```[^`]*```""".toRegex()
            val regexHtmlTags = """<[^>]*>""".toRegex()
            val regexMarkdownSyntax = """[#*_]|\[.*\]\(.*\)""".toRegex()

            val titleMatch = regexTitle.find(fileContent)
            val dateMatch = regexDate.find(fileContent)

            val title = titleMatch?.groupValues?.getOrNull(1)
            val date = dateMatch?.groupValues?.getOrNull(1)

            if (title != null && date != null) {
                val content = fileContent
                    .replace(regexCodeBlocks, "") // Remove code blocks
                    .replace(regexHtmlTags, "")   // Remove HTML tags
                    .replace(regexMarkdownSyntax, "") // Remove Markdown syntax elements
                    .trim()                       // Trim leading and trailing spaces
                val regex = """<img\s+src="([^"]+)""".toRegex()
                val matches = regex.findAll(fileContent)
                val imagePaths = matches.map { it.groupValues[1].replace("@image", "/docs/images") }.toList()
                val images = imagePaths.map {
                    val imageFile = File("${GitService.localRepoPath}/${imagePaths}")
                    imageFile.readBytes()
                }

                filesContent.add(Post(title, date, content, images))
            }
        }
        return filesContent
    }

    fun getPosts(): List<Post> {

        // Clone the remote GitHub repository
        if (gitService.checkIfCloneRequired())
            gitService.cloneRemoteRepository()

        val latestCommitHash = gitService.getLatestCommitHash()
        val previousCommitHash =
            gitService.getPreviousCommitHash(latestCommitHash) ?: throw RuntimeException("이전 커밋이 없어요!")

        val changedFiles = gitService.getChangedFiles(previousCommitHash, latestCommitHash)

        // Filter files that are in the '/docs/posts/' directory
        val targetFiles = changedFiles.filter { it.startsWith("/docs/posts/") }

        return readFiles(GitService.localRepoPath, targetFiles)
    }


}