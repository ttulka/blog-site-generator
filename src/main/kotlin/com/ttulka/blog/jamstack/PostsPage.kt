package com.ttulka.blog.jamstack

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PostsPage(config: GenProps,
                private val props: PageProps,
                private val posts: List<PostLink>,
                private val urlPath: String,
                private val currentPage: Int,
                private val pageCount: Int,
                private val startPage: Int = 0): Page(config, props) {

    override fun generate(file: String) {
        val layout = layout()
        val template = template("posts.html")
        val post = template.fragment("post")

        val postsContent = generatePosts(post, posts)

        val paginationType = if (pageCount == 1) "paginationSingle" else {
            when (currentPage) { 1 -> "paginationFirst"; pageCount -> "paginationLast" else -> "pagination" }
        }
        val paginationContent = template.fragment(paginationType)
            .replace("older", (urlPath + "/${currentPage + 1}/").replace("//", "/"))
            .replace("newer", (urlPath + if (currentPage - 1 == startPage) "" else "/${currentPage - 1}/").replace("//", "/"))
            .asString()

        val content = template
            .replace("posts", postsContent)
            .replace("pagination", paginationContent)
            .asString()

        writeToFile(file, layout.replace("content", content).asString())
    }

    private fun generatePosts(template: Template, posts: List<PostLink>): String {
        with(StringBuilder()) {
            posts.forEach { append(template
                .replace("title", it.title)
                .replace("createdAt", it.createdAt.format(DateTimeFormatter.ISO_DATE))
                .replace("author", it.author)
                .replace("summary", it.summary)
                .replace("uri", it.uri)
                .asString()
            )}
            return toString()
        }
    }
}

data class PostLink(val title: String, val uri: String, val createdAt: LocalDate, val author: String, val summary: String)