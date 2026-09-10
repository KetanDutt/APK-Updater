package com.rtctek.apkupdater.util.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rtctek.apkupdater.receiver.AlarmReceiver
import org.koin.core.KoinComponent
import java.util.Calendar

class AlarmUtil(private val context: Context, private val prefs: AppPrefs): KoinComponent {

	private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

	fun setupAlarm(context: Context) = if (isEnabled()) enableAlarm(context) else cancelAlarm()

	private fun cancelAlarm() {
		val pendingIntent = getPendingIntent(context)
		alarmManager.cancel(pendingIntent)
	}

	private fun enableAlarm(context: Context, interval: Long = getInterval()) {
		val pendingIntent = getPendingIntent(context)

		val now = System.currentTimeMillis()
		val time = Calendar.getInstance().apply { timeInMillis = now }

		when (prefs.settings.checkForUpdates) {
			"0" -> { // Daily
				setHour(time)
				if (time.timeInMillis < now) {
					time.add(Calendar.MILLISECOND, intervalInMs(interval))
				}
			}
			"1" -> { // Weekly
				setHour(time)
				time.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
				if (time.timeInMillis < now) {
					time.add(Calendar.MILLISECOND, intervalInMs(interval))
				}
			}
			else -> {
				setHour(time, time.get(Calendar.HOUR_OF_DAY))
				time.add(Calendar.MILLISECOND, intervalInMs(interval))
			}
		}

		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, interval, pendingIntent)
	}

	private fun getPendingIntent(context: Context): PendingIntent {
		// FLAG_IMMUTABLE is required on Android 12+ for fixed intents.
		val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		} else {
			PendingIntent.FLAG_UPDATE_CURRENT
		}
		return PendingIntent.getBroadcast(context, 0, Intent(context, AlarmReceiver::class.java), flags)
	}

	// Calendar.add takes an Int; keep the value in a safe range for very large intervals.
	private fun intervalInMs(interval: Long): Int =
		if (interval > Int.MAX_VALUE.toLong()) Int.MAX_VALUE else interval.toInt()

	private fun getInterval() = when (prefs.settings.checkForUpdates) {
		"0" -> AlarmManager.INTERVAL_DAY
		"1" -> AlarmManager.INTERVAL_DAY * 7
		"2" -> AlarmManager.INTERVAL_HOUR
		"3" -> AlarmManager.INTERVAL_HOUR * 12
		"4" -> AlarmManager.INTERVAL_HOUR * 6
		else -> AlarmManager.INTERVAL_DAY
	}

	private fun setHour(time: Calendar, hour: Int = prefs.settings.updateHour) = time.apply {
		set(Calendar.HOUR_OF_DAY, hour)
		set(Calendar.MINUTE, 0)
		set(Calendar.SECOND, 0)
		set(Calendar.MILLISECOND, 0)
	}

	private fun isEnabled() = prefs.settings.checkForUpdates != "5"

}
