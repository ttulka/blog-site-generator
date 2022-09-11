package com.ttulka.blog.jamstack

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class Sitemap(private val config: GenProps, private val posts: List<PostItem>) {
    companion object {
        private const val TEMPLATES: String = "/site/templates"
        private const val SITEMAP: String = "sitemap.xml"
    }

    fun generate(file: String) {
        val template = template()
        val post = template.fragment("post")

        val postsContent = with(StringBuilder()) {
            posts.forEach { append(post
                    .replace("loc", it.loc)
                    .replace("lastmod", it.lastmod.atStartOfDay(ZoneId.of("Europe/Berlin")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .asString()
                )}
            toString()
        }

        val content = template.replace("posts", postsContent).asString()
        writeToFile(file, content)
    }

    private fun template(): Template {
        return Template(readResourceAsText("$TEMPLATES/$SITEMAP"))
    }

    private fun writeToFile(file: String, content: String) {
        var path = config.targetDir.resolve(file)
        path.parent.createDirectories()
        path.writeText(content)
    }

    private fun readResourceAsText(path: String): String = object {}.javaClass.getResource(path)!!.readText()
}

data class PostItem(val loc: String, val lastmod: LocalDate)