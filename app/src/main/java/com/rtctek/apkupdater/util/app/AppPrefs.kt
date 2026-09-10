package com.rtctek.apkupdater.util.app

import android.content.Context
import com.rtctek.apkupdater.R
import com.rtctek.apkupdater.model.ui.AppUpdate
import com.kryptoprefs.context.KryptoContext
import com.kryptoprefs.gson.json
import com.kryptoprefs.preferences.KryptoPrefs

class AppPrefs(context: Context, prefs: KryptoPrefs): KryptoContext(prefs) {

	val ignoredApps = json(context.getString(R.string.prefs_ignored_apps), emptyList<String>())
	val updates = json(context.getString(R.string.prefs_updates), emptyList<AppUpdate>())
	val settings = PreferenceFragmentPrefs(context, prefs.sharedPreferences())
	val selfUpdateCheck = long("selfUpdateCheck", 0)
	val lastFdroid = string("lastFdroid", "")

}
