package com.ttulka.blog.jamstack

import java.nio.file.Path
import kotlin.io.path.useLines
import kotlin.io.path.writeText

fun main() {
    val source = Path.of("bodies.dat")
    val target = Path.of("src/test/resources/site/posts/")

    source.useLines { lines ->
        val headerRegex = Regex("^===([a-zA-Z0-9-]+)===$")
        var f = Path.of("_")
        var sb = StringBuilder()
        lines.forEach { line ->
            if (headerRegex.matches(line)) {
                if (sb.isNotBlank()) {
                    f.writeText(sb.toString())
                }
                val uri = headerRegex.find(line)!!.groupValues[1]
                f = target.resolve("$uri.html").toAbsolutePath()
                sb = StringBuilder()
            } else {
                sb.append("$line\n")
            }
        }
    }
}