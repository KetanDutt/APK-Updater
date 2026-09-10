package com.rtctek.apkupdater.repository

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.rtctek.apkupdater.model.ui.AppUpdate
import com.rtctek.apkupdater.repository.apkmirror.ApkMirrorUpdater
import com.rtctek.apkupdater.repository.apkpure.ApkPureUpdater
import com.rtctek.apkupdater.repository.aptoide.AptoideUpdater
import com.rtctek.apkupdater.repository.fdroid.FdroidRepository
import com.rtctek.apkupdater.repository.googleplay.GooglePlayRepository
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.ioScope
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdatesRepository: KoinComponent {

	private val prefs: AppPrefs by inject()
	private val apkMirrorUpdater: ApkMirrorUpdater by inject()
	private val apkPureUpdater: ApkPureUpdater by inject()
	private val aptoideUpdater: AptoideUpdater by inject()
	private val appsRepository: AppsRepository by inject()
	private val fdroidRepository: FdroidRepository by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()

	fun getUpdatesAsync() = ioScope.async {
		val mutex = Mutex()
		val updates = mutableListOf<AppUpdate>()
		val errors = mutableListOf<Throwable>()

		val apps = appsRepository.getPackageInfosFiltered(PackageManager.GET_SIGNATURES)
		val installedApps = appsRepository.getAppsFiltered(apps)

		val googlePlay = if (prefs.settings.googlePlay) googlePlayRepository.updateAsync(installedApps) else null
		val apkMirror = if (prefs.settings.apkMirror) apkMirrorUpdater.updateAsync(installedApps) else null
		val apkPure = if (prefs.settings.apkPure) apkPureUpdater.updateAsync(installedApps) else null
		val aptoide = if (prefs.settings.aptoide) aptoideUpdater.updateAsync(apps) else null
		val fdroid = if (prefs.settings.fdroid) fdroidRepository.updateAsync(installedApps) else null

		listOfNotNull(apkMirror, apkPure, aptoide, fdroid, googlePlay).forEach {
			it.await().fold(
				onSuccess = { mutex.withLock { updates.addAll(it) } },
				onFailure = { mutex.withLock { errors.add(it) } }
			)
		}

		if (prefs.settings.compareVersionName) filterUpdates(updates, apps)

		if (errors.isEmpty()) Result.success(updates.sortedBy { it.name }) else Result.failure(errors.first())
	}

	private fun filterUpdates(updates: MutableList<AppUpdate>, apps: Sequence<PackageInfo>) {
		val installed = apps.toList()
		updates.retainAll { update ->
			val versionName = installed.find { it.packageName == update.packageName }?.versionName
			if (versionName.isNullOrEmpty()) true
			// A non parseable version name must not break the whole check.
			else runCatching { Version(update.version).isHigherThan(Version(versionName)) }
				.getOrDefault(false)
				.also { if (!it) Log.d("UpdatesRepository", "Dropped update for ${update.packageName}: $update.version is not higher than $versionName") }
		}
	}

}
