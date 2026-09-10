package com.rtctek.apkupdater.model.ui

import com.rtctek.apkupdater.util.adapter.Id
import com.rtctek.apkupdater.util.crc.crc16

data class AppSearch(
	val name: String,
	val url: String = "",
	val iconurl: String = "",
	val developer: String = "",
	val source: Int = 0,
	val packageName: String = "",
	val versionCode: Int = 0,
	override val id: Int = crc16("$name$url$iconurl$developer$source$packageName$versionCode"),
	var loading: Boolean = false
): Id { companion object }