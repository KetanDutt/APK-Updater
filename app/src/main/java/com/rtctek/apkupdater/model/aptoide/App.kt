package com.rtctek.apkupdater.model.aptoide

import com.google.gson.annotations.SerializedName

data class App(
	val name: String = "",
	@SerializedName("package") val packageName: String = "",
	val icon: String? = "",
	val file: File = File(),
	val store: Store = Store()
)
