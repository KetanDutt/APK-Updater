package com.rtctek.apkupdater.fragment

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.rtctek.apkupdater.R
import com.rtctek.apkupdater.databinding.FragmentUpdatesBinding
import com.rtctek.apkupdater.databinding.ViewAppsBinding
import com.rtctek.apkupdater.model.ui.AppUpdate
import com.rtctek.apkupdater.repository.googleplay.GooglePlayRepository
import com.rtctek.apkupdater.util.adapter.BindAdapter
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.app.InstallUtil
import com.rtctek.apkupdater.util.getAccentColor
import com.rtctek.apkupdater.util.iconUri
import com.rtctek.apkupdater.util.ioScope
import com.rtctek.apkupdater.util.launchUrl
import com.rtctek.apkupdater.util.observe
import com.rtctek.apkupdater.viewmodel.MainViewModel
import com.rtctek.apkupdater.viewmodel.UpdatesViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class UpdatesFragment : Fragment() {

	private val updatesViewModel: UpdatesViewModel by sharedViewModel()
	private val mainViewModel: MainViewModel by sharedViewModel()
	private val installer: InstallUtil by inject()
	private val prefs: AppPrefs by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()
	private val binding by lazy { FragmentUpdatesBinding.inflate(layoutInflater) }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		binding.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.recyclerView.layoutManager = LinearLayoutManager(context)
		val adapter = BindAdapter(R.layout.view_apps, onBind)
		binding.recyclerView.adapter = adapter
		(binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
		updatesViewModel.items.observe(this) {
			it?.let {
				adapter.items = it
				mainViewModel.updatesBadge.postValue(it.size)
			}
		}
	}

	private val onBind = { view: View, app: AppUpdate ->
		runCatching {
			val viewBinding = ViewAppsBinding.bind(view)
			viewBinding.name.text = app.name
			viewBinding.packageName.text = app.packageName
			viewBinding.version.text = getString(R.string.update_version_version_code, app.oldVersion, app.oldCode, app.version, app.versionCode)
			viewBinding.actionOne.text = getString(R.string.action_install)
			if (app.loading) {
				viewBinding.progress.visibility = View.VISIBLE
				viewBinding.actionOne.visibility = View.INVISIBLE
			} else {
				viewBinding.progress.visibility = View.INVISIBLE
				viewBinding.actionOne.visibility = View.VISIBLE
				viewBinding.actionOne.text = getString(R.string.action_install)
				viewBinding.actionOne.setOnClickListener { if (app.url.endsWith("apk") || app.url == "play") downloadAndInstall(app) else launchUrl(app.url) }
			}
			viewBinding.source.setColorFilter(view.context.getAccentColor(), PorterDuff.Mode.MULTIPLY)
			Glide.with(view).load(app.source).into(viewBinding.source)

			// The app may have been uninstalled since the check; a missing icon
			// must not break the rest of the row.
			runCatching {
				view.context.packageManager.getApplicationInfo(app.packageName, 0).icon
			}.getOrNull()?.let { iconId ->
				Glide.with(view).load(iconUri(app.packageName, iconId)).into(viewBinding.icon)
			}
		}.onFailure { Log.e("UpdatesFragment", "onBind", it) }.let { }
	}

	private fun downloadAndInstall(app: AppUpdate) = ioScope.launch {
		runCatching {
			updatesViewModel.setLoading(app.id, true)
			val url = if (app.url == "play") googlePlayRepository.getDownloadUrl(app.packageName, app.versionCode, app.oldCode) else app.url
			val file = installer.downloadAsync(requireActivity(), url) { _, _ -> updatesViewModel.setLoading(app.id, true) }
			if (installer.install(requireActivity(), file, app.id)) {
				updatesViewModel.setLoading(app.id, false)
				updatesViewModel.remove(app.id)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_success))
			} else if (prefs.settings.rootInstall) {
				updatesViewModel.setLoading(app.id, false)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_failure, getString(R.string.app_install_failure_unknown)))
			}
		}.onFailure {
			updatesViewModel.setLoading(app.id, false)
			mainViewModel.snackbar.postValue(it.message ?: "downloadAndInstall error.")
		}
	}

}
