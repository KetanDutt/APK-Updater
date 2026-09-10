package com.rtctek.apkupdater.model.aptoide

data class ListAppUpdatesResponse(
	val list: List<App> = emptyList(),
	val info: Any? = null,
	val errors: Any? = null
)
