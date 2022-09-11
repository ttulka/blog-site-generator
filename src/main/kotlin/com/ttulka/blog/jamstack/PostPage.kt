package com.ttulka.blog.jamstack

import com.sksamuel.hoplite.ConfigLoader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.exists
import kotlin.io.path.readText

class PostPage(private val config: GenProps,
               private val props: PageProps,
               private val post: PostData) : Page(config, props) {
    companion object {
        private const val POSTS: String = "posts"
        private const val COMMENTS: String = "comments"
    }

    override fun generate(file: String) {
        val body = config.sourceDir.resolve(POSTS).resolve("${post.uri}.html").readText()

        val layout = layout()
        val template = template("post.html")
            .replace("uri", post.uri)
            .replace("title", post.title)
            .replace("createdAt", post.createdAt.format(DateTimeFormatter.ISO_DATE))
            .replace("author", post.author)
            .replace("summary", post.summary)
            .replace("body", body)

        val tag = template.fragment("tag")
        val tags = generateTags(tag, post.tags)

        var comment = template.fragment("comment")
        var answer = template.fragment("answer")
        val comments = generateComments(comment, answer, loadComments())

        val content = template
            .replace("tags", tags)
            .replace("comments", comments)
            .asString()

        writeToFile(file, layout.replace("content", content).asString())
    }

    private fun generateTags(template: Template, tags: List<String>): String {
        return with(StringBuilder()) {
            tags.forEach { append(template.replace("tag", it).asString()) }
            toString()
        }
    }

    private fun loadComments(): List<Comment> {
        val pathToJson = config.sourceDir.resolve(COMMENTS).resolve("${post.uri}.json")
        return if (pathToJson.exists())
            ConfigLoader().loadConfigOrThrow<Comments>(pathToJson.toString()).comments
        else listOf()
    }

    private fun generateComments(commentTpl: Template, answerTpl: Template, comments: List<Comment>): String {
        return with(StringBuilder()) {
            comments.forEach { comment ->
                val answers = with(StringBuilder()) {
                    comment.answers?.let { it.forEach { a -> append(answerTpl
                        .replace("answer.author", a.author)
                        .replace("answer.createdAt", a.createdAt.format(DateTimeFormatter.ISO_DATE))
                        .replace("answer.body", a.body)
                        .asString()
                    )}}
                    toString()
                }
                append(commentTpl
                    .replace("answers", answers)
                    .replace("comment.author", comment.author)
                    .replace("comment.createdAt", comment.createdAt.format(DateTimeFormatter.ISO_DATE))
                    .replace("comment.body", comment.body)
                    .asString()
                )
            }
            toString()
        }
    }

    data class Comments(val comments: List<Comment>)
    data class Comment(val author: String, val createdAt: LocalDate, val body: String, val answers: List<Answer>?)
    data class Answer(val author: String, val createdAt: LocalDate, val body: String)
}

data class PostData(val title: String, val uri: String, val createdAt: LocalDate, val author: String, val summary: String, val tags: List<String>)
