package io.github.bruce0203.bsmeal1nfo

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.File

class GitService {
    companion object {
        val localRepoPath: String = System.getenv("REPO_PATH")
    }

    private val gitUsername = System.getenv("GITHUB_USERNAME")
//    private val gitAccessToken = System.getenv("GITHUB_TOKEN")
    private val gitRepository = System.getenv("GITHUB_REPOSITORY")
    private val remoteRepoURI = "https://github.com/${gitUsername}/${gitRepository}.git"

    private val git = Git.open(File(localRepoPath))

    init {
        println(
            """
            ---------
            ${gitUsername}의 ${gitRepository}에 접근합니다. URL: $remoteRepoURI
            $localRepoPath 에서 엽니다. 
            ---------
        """.trimIndent()
        )

        println("""
            ${git.repository.directory},
            ${git.repository.repositoryState},
            ${git.repository.branch},
            ${git.repository.remoteNames},
        """.trimIndent())
    }


    fun getLatestCommitHash(): String {
        git.checkout().setName("dev").call() // Checkout the 'dev' branch
        val latestCommit: RevCommit = git.log().setMaxCount(1).call().iterator().next()
        return latestCommit.id.name
    }

    fun getPreviousCommitHash(latestCommitHash: String): String? {
        git.checkout().setName("dev").call() // Checkout the 'dev' branch
        val walk = RevWalk(git.repository)
        val latestCommit: RevCommit = walk.parseCommit(ObjectId.fromString(latestCommitHash))
        val previousCommit: RevCommit? = walk.parseCommit(latestCommit.getParent(0).id)
        walk.close()
        return previousCommit?.id?.name
    }

    fun getChangedFiles(oldCommit: String, newCommit: String): List<String> {
        git.checkout().setName("dev").call() // Checkout the 'dev' branch

        val oldTreeIter = CanonicalTreeParser().apply {
            val oldTreeId: ObjectId = git.repository.resolve("$oldCommit^{tree}")
            this.reset(git.repository.newObjectReader(), oldTreeId)
        }

        val newTreeIter = CanonicalTreeParser().apply {
            val newTreeId: ObjectId = git.repository.resolve("$newCommit^{tree}")
            this.reset(git.repository.newObjectReader(), newTreeId)
        }

        val diffEntries: List<DiffEntry> = git.diff()
            .setOldTree(oldTreeIter)
            .setNewTree(newTreeIter)
            .call()

        return diffEntries.filter {
            println(it.changeType)
            it.changeType == DiffEntry.ChangeType.ADD
        }
            .map { it.newPath }
    }

    fun fetchRemoteChanges() {
        git.fetch().call() // Fetch changes from the remote repository
    }

    fun pullRemoteChanges() {
        git.pull().setRemoteBranchName("dev")
            .call() // Pull changes from the remote repository and merge them into the local branch
    }

}