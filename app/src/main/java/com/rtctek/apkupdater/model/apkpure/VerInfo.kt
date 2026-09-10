package com.rtctek.apkupdater.model.apkpure

import org.jsoup.nodes.Element

class VerInfo(element: Element, packageName: String) {
	val packageName = packageName
	val versionName: String
	val versionCode: Int
	val minApiLevel: Int
	val architectures: List<String>
	val downloadLink: String

	// Parsing is intentionally lenient: a single malformed HTML block must not take
	// down the whole APKPure update check.
	init {
		val versionNameAndCode = element.getElementsByClass("ver-info-top").text().split(" ").takeLast(2)
		versionName = versionNameAndCode.getOrNull(0) ?: ""
		versionCode = versionNameAndCode.getOrNull(1)?.removeSurrounding("(", ")")?.toIntOrNull() ?: 0
		val paragraphs = element.getElementsByTag("p")
		minApiLevel = paragraphs.getOrNull(2)?.text()?.split(" ")?.lastOrNull()?.removeSuffix(")")?.toIntOrNull() ?: 0
		architectures = paragraphs.getOrNull(5)?.text()?.replace(",", "")?.split(" ") ?: emptyList()
		downloadLink = element.getElementsByClass(" down").attr("href")
	}
}
