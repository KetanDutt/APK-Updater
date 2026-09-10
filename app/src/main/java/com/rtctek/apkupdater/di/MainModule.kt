package com.rtctek.apkupdater.di

import com.rtctek.apkupdater.repository.AppsRepository
import com.rtctek.apkupdater.repository.SearchRepository
import com.rtctek.apkupdater.repository.UpdatesRepository
import com.rtctek.apkupdater.repository.apkmirror.ApkMirrorSearch
import com.rtctek.apkupdater.repository.apkmirror.ApkMirrorUpdater
import com.rtctek.apkupdater.repository.apkpure.ApkPureSearch
import com.rtctek.apkupdater.repository.apkpure.ApkPureUpdater
import com.rtctek.apkupdater.repository.aptoide.AptoideSearch
import com.rtctek.apkupdater.repository.aptoide.AptoideUpdater
import com.rtctek.apkupdater.repository.fdroid.FdroidRepository
import com.rtctek.apkupdater.repository.googleplay.GooglePlayRepository
import com.rtctek.apkupdater.util.app.AlarmUtil
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.app.InstallUtil
import com.rtctek.apkupdater.util.app.NotificationUtil
import com.rtctek.apkupdater.viewmodel.AppsViewModel
import com.rtctek.apkupdater.viewmodel.MainViewModel
import com.rtctek.apkupdater.viewmodel.SearchViewModel
import com.rtctek.apkupdater.viewmodel.UpdatesViewModel
import com.kryptoprefs.preferences.KryptoBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

	single { AppPrefs(get(), KryptoBuilder.nocrypt(get(), "${androidContext().packageName}_preferences")) }

	single { AppsRepository(get(), get()) }
	single { UpdatesRepository() }
	single { SearchRepository() }
	single { FdroidRepository() }
	single { GooglePlayRepository() }

	single { ApkMirrorUpdater(get()) }
	single { ApkPureUpdater(get()) }
	single { AptoideUpdater(get()) }

	single { ApkMirrorSearch() }
	single { ApkPureSearch() }
	single { AptoideSearch() }

	single { NotificationUtil(get()) }
	single { AlarmUtil(get(), get()) }
	single { InstallUtil() }

	viewModel { AppsViewModel() }
	viewModel { UpdatesViewModel() }
	viewModel { MainViewModel() }
	viewModel { SearchViewModel() }

}