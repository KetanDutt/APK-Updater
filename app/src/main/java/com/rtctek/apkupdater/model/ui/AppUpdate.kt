package com.rtctek.apkupdater.model.ui

import android.content.Context
import android.content.pm.PackageInfo
import com.rtctek.apkupdater.model.apkmirror.AppExistsResponseApk
import com.rtctek.apkupdater.model.apkmirror.AppExistsResponseData
import com.rtctek.apkupdater.model.aptoide.App
import com.rtctek.apkupdater.util.adapter.Id
import com.rtctek.apkupdater.util.name

data class AppUpdate(
	val name: String = "",
	val packageName: String = "",
	val version: String = "",
	val versionCode: Int = 0,
	val oldVersion: String = "",
	val oldCode: Int = 0,
	val url: String = "",
	val source: Int = 0,
	var loading: Boolean = false
) : Id {

	// 32-bit stable id; keeps data class equality and DiffUtil behaviour consistent.
	override val id: Int get() = (packageName.hashCode() * 31 + versionCode) * 31 + source

	companion object {

		fun from(context: Context, info: PackageInfo, app: App, source: Int): AppUpdate =
			AppUpdate(
				info.name(context),
				app.packageName,
				app.file.vername,
				app.file.vercode.toIntOrNull() ?: 0,
				info.versionName ?: "null",
				info.versionCode,
				app.file.path,
				source
			)

		fun from(app: AppInstalled, data: AppExistsResponseData, apk: AppExistsResponseApk, url: String, source: Int): AppUpdate =
			AppUpdate(
				app.name,
				data.pname,
				data.release.version,
				apk.versionCode,
				app.version,
				app.versionCode,
				url,
				source
			)

	}
}
