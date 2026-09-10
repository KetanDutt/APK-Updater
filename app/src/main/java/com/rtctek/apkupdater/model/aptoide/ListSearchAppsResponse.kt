package com.rtctek.apkupdater.model.aptoide

data class ListSearchAppsResponse(
	val datalist: DataList = DataList(),
	val info: Any? = null,
	val errors: Any? = null
)

data class DataList(val list: List<App> = emptyList())
