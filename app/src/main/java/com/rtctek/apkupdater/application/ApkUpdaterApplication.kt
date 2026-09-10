package com.rtctek.apkupdater.application

import androidx.multidex.MultiDexApplication
import com.rtctek.apkupdater.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ApkUpdaterApplication : MultiDexApplication() {

	override fun onCreate() {
		super.onCreate()
		initKoin()
	}

	private fun initKoin() = startKoin {
		androidLogger()
		androidContext(this@ApkUpdaterApplication)
		modules(mainModule)
	}

}
