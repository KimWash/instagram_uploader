package io.github.bruce0203.bsmeal1nfo

import java.io.File

class PostService(
    private val gitService: GitService
) {

    private fun readFiles(repoPath: String, targetFiles: List<String>): List<Post> {
        val filesContent = mutableListOf<Post>()
        FileAssert.printDirectoryTree(File(repoPath))
        for (fileRelativePath in targetFiles) {
            val fileAbsolutePath = File(repoPath, fileRelativePath)
            val fileContent = fileAbsolutePath.readText()
            val regexTitle = """title:\s*"([^"]+)"""".toRegex()
            val regexDate = """date:\s*"([^"]+)"""".toRegex()
            val regexCodeBlocks = """```[^`]*```""".toRegex()
            val regexHtmlTags = """<[^>]*>""".toRegex()
            val regexMarkdownSyntax = """[#*_]|\[.*\]\(.*\)""".toRegex()
            val regexMultiLineBreak = """\n{3,}""".toRegex()
            val regexIndent = """^\s+""".toRegex()
            val regexLatex = """\$(.*?)\$""".toRegex()


            val titleMatch = regexTitle.find(fileContent)
            val dateMatch = regexDate.find(fileContent)

            val title = titleMatch?.groupValues?.getOrNull(1)
            val date = dateMatch?.groupValues?.getOrNull(1)

            if (title != null && date != null) {
                val contentWithoutHeader = fileContent.split("---").getOrNull(2)?.trim() ?: ""

                val content = contentWithoutHeader
                    .replace(regexCodeBlocks, "") // Remove code blocks
                    .replace(regexHtmlTags, "")   // Remove HTML tags
                    .replace(regexMarkdownSyntax, "") // Remove Markdown syntax elements
                    .replace(regexMultiLineBreak, "\n\n") // Replace multiple consecutive newlines with two newlines
                    .replace(
                        regexIndent,
                        ""
                    ) // Remove indentation by replacing leading spaces at the start of each line
                    .replace(regexLatex) { result ->
                        result.groupValues[1] // Remove LaTeX expressions and keep the inner content
                    }
                    .trim()
                // Trim leading and trailing spaces
                val regex = """<img\s+src="([^"]+)""".toRegex()
                val matches = regex.findAll(fileContent)

                val imagePaths = matches.map { it.groupValues[1].replace("@image", "docs/images") }.toList()
                val images = imagePaths.map {
                    File("${GitService.localRepoPath}/${it}")
                }

                filesContent.add(Post(title, date, content, images))
            }
        }
        return filesContent
    }

    fun getPosts(): List<Post> {

        // Clone the remote GitHub repository
//        if (gitService.checkIfCloneRequired())
//            gitService.cloneRemoteRepository()

        gitService.fetchRemoteChanges()
        gitService.pullRemoteChanges()

        val latestCommitHash = gitService.getLatestCommitHash()
        val previousCommitHash =
            gitService.getPreviousCommitHash(latestCommitHash) ?: throw RuntimeException("이전 커밋이 없어요!")

        val changedFiles = gitService.getChangedFiles(previousCommitHash, latestCommitHash)

        // Filter files that are in the '/docs/posts/' directory
        val targetFiles = changedFiles.filter {
            it.startsWith("docs/posts/") && it.endsWith(".md")
        }

        return readFiles(GitService.localRepoPath, targetFiles)
    }


}