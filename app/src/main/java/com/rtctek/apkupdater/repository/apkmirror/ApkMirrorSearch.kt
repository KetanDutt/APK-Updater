package com.rtctek.apkupdater.repository.apkmirror

import com.rtctek.apkupdater.R
import com.rtctek.apkupdater.model.ui.AppSearch
import com.rtctek.apkupdater.util.ioScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import org.koin.core.KoinComponent

class ApkMirrorSearch: KoinComponent {

	private val baseUrl = "https://www.apkmirror.com"
	private val searchQuery = "/?post_type=app_release&searchtype=app&s="
	private val source = R.drawable.apkmirror_logo

	private fun search(text: String): List<AppSearch> {
		val doc = Jsoup.connect("$baseUrl$searchQuery$text").get()
		val row = doc.select("div.appRow")
		val a = row.select("a.byDeveloper")
		val h5 = row.select("h5.appRowTitle").take(a.size)
		val img = row.select("img")
		return (0 until a.size).mapNotNull { i ->
			// A single malformed row must not fail the whole search.
			val title = h5.getOrNull(i)?.attr("title").orEmpty()
			val link = h5.getOrNull(i)?.selectFirst("a")?.attr("href").orEmpty()
			if (title.isEmpty() || link.isEmpty()) null else
				AppSearch(
					title,
					"$baseUrl$link",
					"$baseUrl${img.getOrNull(i)?.attr("src").orEmpty()}".replace("=32", "=64"),
					a[i].text(),
					source
				)
		}
	}

	fun searchAsync(text: String) = ioScope.async { runCatching { search(text) } }

}
