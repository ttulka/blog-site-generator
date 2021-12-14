package com.ttulka.blog.jamstack

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IndexPage(config: GenProps,
                private val props: PageProps,
                private val newest: List<IndexPostLink>,
                private val highlights: List<IndexPostLink>): Page(config, props) {

    override fun generate(file: String) {
        val layout = layout()
        val template = template("index.html")
            .replace("page.title", props.title)
            .replace("page.description", props.description)
        val post = template.fragment("post")

        val newestPosts = generatePosts(post, newest)
        val highlightPosts = generatePosts(post, highlights)

        val content = template
            .replace("newest", newestPosts)
            .replace("highlights", highlightPosts)
            .asString()

        writeToFile(file, layout.replace("content", content).asString())
    }

    private fun generatePosts(template: Template, posts: List<IndexPostLink>): String {
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

data class IndexPostLink(val title: String, val uri: String, val createdAt: LocalDate, val author: String, val summary: String)