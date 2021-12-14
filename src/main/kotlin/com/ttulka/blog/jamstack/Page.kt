package com.ttulka.blog.jamstack

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

abstract sealed class Page(private val config: GenProps, private val props: PageProps) {
    companion object {
        private const val TEMPLATES: String = "templates"
        private const val LAYOUT: String = "layout.html"
    }

    abstract fun generate(file: String)

    fun template(name: String): Template {
        return Template(config.sourceDir.resolve(TEMPLATES).resolve(name).readText())
    }

    fun layout(): Template {
        return Template(config.sourceDir.resolve(TEMPLATES).resolve(LAYOUT).readText())
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
}

data class GenProps(val sourceDir: Path, val targetDir: Path)

data class PageProps(val title: String, val description: String, val author: String, val url: String)