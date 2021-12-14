package com.ttulka.blog.jamstack

import com.sksamuel.hoplite.ConfigLoader
import java.io.File
import java.nio.file.Path
import kotlin.math.ceil

const val POSTS_ON_PAGE = 10

fun main(args: Array<String>) {
    val configFile = Path.of(args.getOrElse(0) { "./config.yaml" })
    println("Generating a blog static site... ${configFile.normalize().toAbsolutePath()}")

    val config = ConfigLoader().loadConfigOrThrow<Config>(configFile)
    val genProps = GenProps(Path.of(config.generator.source), Path.of(config.generator.target))

    cleanTarget(config.generator.target)

    copyAssets(config.generator.source, config.generator.target)

    generateIndex(config, genProps)
    generateStaticPages(config, genProps)
    generatePosts(config, genProps)
    generatePagination(config, genProps)
    generateTagsPagination(config, genProps)
    generateSitemap(config, genProps)
}

fun cleanTarget(target: String) {
    File(target).deleteRecursively()
}

fun copyAssets(source: String, target: String) {
    File(source).copyRecursively(File(target))
}

fun generateIndex(config: Config, genProps: GenProps) {
    val postLinkMap: (Post) -> IndexPostLink = { IndexPostLink(it.title, it.uri, it.createdAt, config.blog.author, it.summary) }

    IndexPage(genProps, PageProps(config.blog.title, config.blog.description, config.blog.author, config.blog.url),
        config.posts
            .take(3)
            .map(postLinkMap)
            .toList(),
        config.posts
            .filter { it.highlight != null && it.highlight }
            .map(postLinkMap)
            .toList()
    ).generate("index.html")
}

fun generatePosts(config: Config, genProps: GenProps) {
    config.posts.forEach {
        PostPage(genProps,
            PageProps(it.title, it.summary, config.blog.author, config.blog.url + it.uri),
            PostData(it.title, it.uri, it.createdAt, config.blog.author, it.summary, it.tags)
        ).generate("${it.uri}/index.html")
    }
}

fun generateStaticPages(config: Config, genProps: GenProps) {
    config.pages.forEach {
        StaticPage(genProps,
            PageProps(config.blog.title, config.blog.description, config.blog.author, config.blog.url),
            it
        ).generate("$it/index.html")
    }
}

fun generatePagination(config: Config, genProps: GenProps) {
    val pageCount = ceil(config.posts.size.toDouble() / POSTS_ON_PAGE).toInt()
    for (page in 1..pageCount) {
        PostsPage(genProps,
            PageProps(config.blog.title, config.blog.description, config.blog.author, "${config.blog.url}$page"),
            config.posts
                .drop((page - 1) * POSTS_ON_PAGE)
                .take(POSTS_ON_PAGE)
                .map { PostLink(it.title, it.uri, it.createdAt, config.blog.author, it.summary) }
                .toList(),
            "/",
            page,
            pageCount
        ).generate("$page/index.html")
    }
}

fun generateTagsPagination(config: Config, genProps: GenProps) {
    config.posts
        .flatMap { it.tags }
        .forEach { tag ->
        val posts = config.posts.filter { tag in it.tags }.toList()
        val pageCount = ceil(posts.size.toDouble() / POSTS_ON_PAGE).toInt()
        for (page in 1..pageCount) {
            val location = if (page == 1) "tag/$tag" else "tag/$tag/$page"
            PostsPage(genProps,
                PageProps(tag, tag, config.blog.author, "${config.blog.url}$location"),
                posts
                    .drop((page - 1) * POSTS_ON_PAGE)
                    .take(POSTS_ON_PAGE)
                    .map { PostLink(it.title, it.uri, it.createdAt, config.blog.author, it.summary) }
                    .toList(),
                "/tag/$tag",
                page,
                pageCount,
                startPage = 1
            ).generate("$location/index.html")
        }
    }
}

fun generateSitemap(config: Config, genProps: GenProps) {
    Sitemap(genProps, config.posts.map { PostItem("${config.blog.url}${it.uri}/", it.createdAt) })
        .generate("sitemap.xml")
}