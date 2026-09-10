package com.rtctek.apkupdater.repository.aptoide

import com.rtctek.apkupdater.R
import com.rtctek.apkupdater.model.ui.AppSearch
import com.rtctek.apkupdater.model.aptoide.ListSearchAppsRequest
import com.rtctek.apkupdater.model.aptoide.ListSearchAppsResponse
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.ioScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.inject

class AptoideSearch: KoinComponent {

	private val baseUrl = "https://ws75.aptoide.com/api/7/"
	private val appUpdates = "listSearchApps"
	private val source = R.drawable.aptoide_logo
	private val prefs: AppPrefs by inject()
	private val exclude get() = if(prefs.settings.excludeExperimental) "alpha,beta" else ""
	private val limit = "10"

	private fun listSearchApps(request: ListSearchAppsRequest) = Fuel
		.post(baseUrl + appUpdates)
		.jsonBody(request)
		.responseObject<ListSearchAppsResponse>()

	fun searchAsync(text: String) = ioScope.async {
		listSearchApps(ListSearchAppsRequest(text, limit, exclude)).third.fold(
			success = { Result.success(parseData(it)) },
			failure = { Result.failure(it) }
		)
	}

	private fun parseData(response: ListSearchAppsResponse) = response.datalist.list.map{ app ->
		AppSearch(
			app.name,
			app.file.path,
			app.icon?.replace("http:", "https:") ?: "",
			app.packageName,
			source
		)
	}

}