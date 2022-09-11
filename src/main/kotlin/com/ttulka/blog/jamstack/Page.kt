package com.ttulka.blog.jamstack

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

sealed class Page(private val config: GenProps, private val props: PageProps) {
    companion object {
        private const val TEMPLATES: String = "/site/templates"
        private const val LAYOUT: String = "layout.html"
    }

    abstract fun generate(file: String)

    fun template(name: String): Template {
        return Template(readResourceAsText("$TEMPLATES/$name"))
    }

    fun layout(): Template {
        return Template(readResourceAsText("$TEMPLATES/$LAYOUT"))
            .replace("page.title", props.title)
            .replace("page.description", props.description)
            .replace("page.author", props.author)
            .replace("page.url", props.url)
    }

    fun writeToFile(file: String, content: String) {
        var path = config.targetDir.resolve(file)
        path.parent.createDirectories()
        path.writeText(content)
    }

    fun readResourceAsText(path: String): String = object {}.javaClass.getResource(path)!!.readText()
}

data class GenProps(val sourceDir: Path, val targetDir: Path)

data class PageProps(val title: String, val description: String, val author: String, val url: String)