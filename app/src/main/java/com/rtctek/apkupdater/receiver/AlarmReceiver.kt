package com.rtctek.apkupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.rtctek.apkupdater.repository.UpdatesRepository
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.app.NotificationUtil
import com.rtctek.apkupdater.util.ioScope
import com.kryptoprefs.invoke
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

	private val updatesRepository: UpdatesRepository by inject()
	private val notificationUtil: NotificationUtil by inject()
	private val prefs: AppPrefs by inject()

	private var pendingResult: PendingResult? = null

	override fun onReceive(context: Context, intent: Intent?) {
		// Background broadcasts must return quickly (10s hard limit on Android 8+).
		// goAsync() extends that to 60s, which is enough for the update check.
		// goAsync() only exists on API 26+; on older versions the coroutine simply keeps running.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) pendingResult = goAsync()
		ioScope.launch {
			try {
				updatesRepository.getUpdatesAsync().await().fold(
					onSuccess = {
						prefs.updates(it)
						notificationUtil.showUpdateNotification(it.size)
					},
					onFailure = { Log.e("AlarmReceiver", "onReceive", it) }
				)
			} finally {
				pendingResult?.finish()
			}
		}
	}

}
