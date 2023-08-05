package io.github.bruce0203.bsmeal1nfo

import java.io.File

data class Post(val title: String, val date: String, val content: String, val images: List<File>? = null)
