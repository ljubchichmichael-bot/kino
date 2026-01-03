package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class UAKinogo : MainAPI() {
    override var mainUrl = "https://uakinogo.online"
    override var name = "UAKinogo"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "uk"

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/index.php?do=search&subaction=search&story=$query").document
        return document.select("div.movie-item").mapNotNull {
            val title = it.selectFirst("div.movie-title")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst(".movie-img img")?.attr("src")
        val description = document.selectFirst(".movie-desc")?.text()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val iframeUrl = document.select("iframe").attr("src")
        if (iframeUrl.isNotEmpty()) {
            loadExtractor(iframeUrl, data, subtitleCallback, callback)
        }
        return true
    }
}
