package com.ttulka.blog.jamstack

import java.time.LocalDate

data class Config(val generator: Generator, val blog: Blog, val posts: List<Post>, val pages: List<String>)

data class Generator(val source: String, val target: String)
data class Blog(val title: String, val description: String, val author: String, val url: String)
data class Post(
    val title: String,
    val uri: String,
    val createdAt: LocalDate,
    val summary: String,
    val tags: List<String>,
    val highlight: Boolean?
)
