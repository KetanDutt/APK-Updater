package com.rtctek.apkupdater.model.ui

import com.rtctek.apkupdater.util.adapter.Id

data class AppSearch(
	val name: String,
	val url: String = "",
	val iconurl: String = "",
	val developer: String = "",
	val source: Int = 0,
	val packageName: String = "",
	val versionCode: Int = 0,
	var loading: Boolean = false
) : Id {

	// 32-bit hash of the identifying fields; collisions are negligible for list sizes.
	override val id: Int get() = "$name$url$iconurl$developer$source$packageName$versionCode".hashCode()

	companion object
}
