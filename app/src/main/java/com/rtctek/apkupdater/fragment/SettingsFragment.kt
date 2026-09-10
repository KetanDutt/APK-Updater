package com.rtctek.apkupdater.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.rtctek.apkupdater.BuildConfig
import com.rtctek.apkupdater.R
import com.rtctek.apkupdater.util.app.AlarmUtil
import com.rtctek.apkupdater.util.launchUrl
import com.rtctek.apkupdater.viewmodel.MainViewModel
import eu.chainfire.libsuperuser.Shell
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsFragment : PreferenceFragmentCompat() {

	private val mainViewModel: MainViewModel by sharedViewModel()
	private val alarmUtil: AlarmUtil by inject()

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings, rootKey)

		findPreference<ListPreference>(getString(R.string.settings_check_for_updates_key))?.setOnPreferenceChangeListener { _, _ ->
			context?.let { alarmUtil.setupAlarm(it) }
			true
		}

		findPreference<SeekBarPreference>(getString(R.string.settings_update_hour_key))?.setOnPreferenceChangeListener { _, _ ->
			context?.let { alarmUtil.setupAlarm(it) }
			true
		}

		findPreference<ListPreference>(getString(R.string.settings_theme_key))?.setOnPreferenceChangeListener { _, _ ->
			activity?.recreate()
			true
		}

		findPreference<SwitchPreferenceCompat>(getString(R.string.settings_root_install_key))?.setOnPreferenceChangeListener { _, _ ->
			if (Shell.SU.available()) {
				true
			} else {
				mainViewModel.snackbar.postValue(getString(R.string.root_not_available))
				false
			}
		}

		// About / developer links
		findPreference<Preference>(getString(R.string.settings_about_key))?.apply {
			summary = getString(R.string.settings_about_version, BuildConfig.VERSION_NAME)
			setOnPreferenceClickListener {
				launchUrl(getString(R.string.settings_github_url))
				true
			}
		}
		findPreference<Preference>(getString(R.string.settings_original_project_key))?.setOnPreferenceClickListener {
			launchUrl(getString(R.string.settings_original_project_url))
			true
		}
		findPreference<Preference>(getString(R.string.settings_license_key))?.setOnPreferenceClickListener {
			launchUrl(getString(R.string.settings_license_url))
			true
		}
	}

}
