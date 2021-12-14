package com.ttulka.blog.jamstack

import kotlin.io.path.readText

class StaticPage(private val config: GenProps, props: PageProps, private val uri: String) : Page(config, props) {
    companion object {
        private const val STATIC: String = "static"
    }

    override fun generate(file: String) {
        val body = config.sourceDir.resolve(STATIC).resolve("$uri.html").readText()
        val content = layout()
            .replace("content", body)
            .asString()

        writeToFile(file, content)
    }
}
