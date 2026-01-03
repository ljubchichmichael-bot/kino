package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import org.jsoup.nodes.Element

class UAKinogo : MainAPI() {
    override var mainUrl = "https://uakinogo.online"
    override var name = "UAKinogo"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)
    override var lang = "uk"

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        val items = document.select("div.movie-item").mapNotNull { it.toSearchResult() }
        return newHomePageResponse("Останні оновлення", items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/index.php?do=search&subaction=search&story=$query"
        val document = app.get(url).document
        return document.select("div.movie-item").mapNotNull { it.toSearchResult() }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.movie-title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val poster = this.selectFirst("img")?.attr("src")?.let { fixUrl(it) }
        return newMovieSearchResponse(title, href, TvType.Movie) { this.posterUrl = poster }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: return null
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = fixUrl(document.selectFirst(".movie-img img")?.attr("src") ?: "")
            this.plot = document.selectFirst(".movie-desc")?.text()
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        val iframeUrl = document.select("iframe").attr("src")
        if (iframeUrl.isNotEmpty()) {
            loadExtractor(iframeUrl, data, subtitleCallback, callback)
        }
        return true
    }
}