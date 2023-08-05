package io.github.bruce0203.bsmeal1nfo

data class Post(val title: String, val date: String, val content: String, val images: List<ByteArray>? = null)
