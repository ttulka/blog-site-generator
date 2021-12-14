package com.ttulka.blog.jamstack

class Template(private val content: String) {

    fun replace(placeholder: String, value: String): Template {
        return Template(content.replace("{{$placeholder}}", value))
    }

    fun fragment(name: String): Template {
        val startTag = "{{{$name"
        val start = content.indexOf(startTag)
        val end = content.indexOf("}}}", start)
        return Template(content.substring(start + startTag.length, end))
    }

    fun asString(): String {
        return removeFragments(content)
    }

    private fun removeFragments(str: String): String {
        var result = str
        var found: Boolean
        do {
            found = false
            val start = result.indexOf("{{{")
            if (start > -1) {
                val end = result.indexOf("}}}", start)
                if (end > start) {
                    result = result.removeRange(start, end + 3)
                    found = true
                }
            }
        } while (found)
        return result
    }
}