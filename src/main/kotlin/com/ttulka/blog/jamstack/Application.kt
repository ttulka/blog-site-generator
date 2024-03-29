package com.ttulka.blog.jamstack

import com.sksamuel.hoplite.ConfigLoader
import java.io.File
import java.net.URL
import java.nio.file.Path
import kotlin.math.ceil

const val POSTS_ON_PAGE = 10

fun main(args: Array<String>) {
    val configFile = Path.of(args.getOrElse(0) { "./config.yaml" }).toAbsolutePath().normalize()
    println("Loading a config file:\t\t$configFile")

    val config = ConfigLoader().loadConfigOrThrow<Config>(configFile.toString())
    val sourceDir = configFile.parent!!.resolve(config.generator.source).normalize()
    val targetDir = configFile.parent!!.resolve(config.generator.target).normalize()

    println("Generating a static site from:\t$sourceDir")

    val genProps = GenProps(sourceDir, targetDir)

    cleanTarget(targetDir)

    copyAssets(sourceDir, targetDir)
    copyRoot(targetDir)

    generateIndex(config, genProps)
    generateStaticPages(config, genProps)
    generatePosts(config, genProps)
    generatePagination(config, genProps)
    generateTagsPagination(config, genProps)
    generateSitemap(config, genProps)

    println("Blog jamstack generated to:\t$targetDir")
}

fun cleanTarget(targetDir: Path) {
    targetDir.toFile().deleteRecursively()
}

fun copyAssets(sourceDir: Path, targetDir: Path) {
    File({}.javaClass.getResource("/site/assets")!!.toURI()).copyRecursively(targetDir.resolve("assets").toFile())
    sourceDir.resolve("assets").toFile().copyRecursively(targetDir.resolve("assets").toFile())
}

fun copyRoot(targetDir: Path) {
    File({}.javaClass.getResource("/site/root")!!.toURI()).copyRecursively(targetDir.toFile())
}

fun generateIndex(config: Config, genProps: GenProps) {
    val postLinkMap: (Post) -> IndexPostLink = { IndexPostLink(it.title, it.uri, it.createdAt, config.blog.author, it.summary) }

    IndexPage(genProps, PageProps(config.blog.title, config.blog.description, config.blog.author, config.blog.url),
        config.posts
            .sortedByDescending { it.createdAt }
            .take(3)
            .map(postLinkMap)
            .toList(),
        config.posts
            .filter { it.highlight != null && it.highlight }
            .sortedByDescending { it.createdAt }
            .map(postLinkMap)
            .toList()
    ).generate("index.html")
}

fun generatePosts(config: Config, genProps: GenProps) {
    config.posts.sortedByDescending { it.createdAt }.forEach {
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
            config.posts.asSequence()
                .sortedByDescending { it.createdAt }
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
        .sortedByDescending { it.createdAt }
        .flatMap { it.tags }
        .forEach { tag ->
        val posts = config.posts.filter { tag in it.tags }.sortedByDescending { it.createdAt }.toList()
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
    Sitemap(genProps, config.posts.sortedByDescending { it.createdAt }
        .map { PostItem("${config.blog.url}${it.uri}/", it.createdAt) })
        .generate("sitemap.xml")
}