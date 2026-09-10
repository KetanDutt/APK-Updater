package com.rtctek.apkupdater.model.ui

import android.net.Uri
import com.rtctek.apkupdater.util.adapter.Id

data class AppInstalled(
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Int,
	val iconUri: Uri = Uri.EMPTY,
	val ignored: Boolean = false
) : Id {

	// Stable, unique per package so DiffUtil keeps rows consistent across updates.
	override val id: Int get() = packageName.hashCode()
}
