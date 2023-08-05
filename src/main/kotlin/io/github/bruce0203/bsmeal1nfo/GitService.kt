package io.github.bruce0203.bsmeal1nfo

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
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
    private val gitAccessToken = System.getenv("GITHUB_TOKEN")
    private val gitRepository = System.getenv("GITHUB_REPOSITORY")


    fun checkIfCloneRequired(): Boolean {
        val directory = File(localRepoPath)
        return directory.exists() && directory.isDirectory && directory.listFiles()?.isNotEmpty() == true
    }

    fun cloneRemoteRepository() {
        val remoteRepoURI = "https://github.com/${gitUsername}/${gitRepository}.git"

        val git: Git = Git.cloneRepository()
            .setURI(remoteRepoURI)
            .setDirectory(File(localRepoPath))
            // Use access token for HTTPS or SSH for authentication
            .setCredentialsProvider(UsernamePasswordCredentialsProvider(gitAccessToken, ""))
            .call()

        git.close()
    }


    fun getLatestCommitHash(): String {
        val repo: Repository = FileRepository(localRepoPath)
        val git = Git(repo)
        val latestCommit: RevCommit = git.log().setMaxCount(1).call().iterator().next()
        return latestCommit.id.name
    }

    fun getPreviousCommitHash(latestCommitHash: String): String? {
        val repo: Repository = FileRepository(localRepoPath)
        val walk = RevWalk(repo)
        val latestCommit: RevCommit = walk.parseCommit(ObjectId.fromString(latestCommitHash))
        val previousCommit: RevCommit? = walk.parseCommit(latestCommit.getParent(0).id)
        walk.close()
        return previousCommit?.id?.name
    }

    fun getChangedFiles(oldCommit: String, newCommit: String): List<String> {
        val repo: Repository = FileRepository(localRepoPath)
        val git = Git(repo)

        val oldTreeIter = CanonicalTreeParser().apply {
            val oldTreeId: ObjectId = repo.resolve("$oldCommit^{tree}")
            this.reset(repo.newObjectReader(), oldTreeId)
        }

        val newTreeIter = CanonicalTreeParser().apply {
            val newTreeId: ObjectId = repo.resolve("$newCommit^{tree}")
            this.reset(repo.newObjectReader(), newTreeId)
        }

        val diffEntries: List<DiffEntry> = git.diff()
            .setOldTree(oldTreeIter)
            .setNewTree(newTreeIter)
            .call()

        return diffEntries.map { it.newPath }
    }

}