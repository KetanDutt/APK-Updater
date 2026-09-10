package com.rtctek.apkupdater.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.rtctek.apkupdater.model.ui.Action
import com.rtctek.apkupdater.model.ui.AppsItem
import com.rtctek.apkupdater.model.ui.model
import com.rtctek.apkupdater.repository.AppsRepository
import com.rtctek.apkupdater.ui.ActionRow
import com.rtctek.apkupdater.ui.AppInfo
import com.rtctek.apkupdater.ui.CustomCard
import com.rtctek.apkupdater.util.app.AppPrefs
import com.rtctek.apkupdater.util.observe
import com.rtctek.apkupdater.viewmodel.AppsViewModel
import com.rtctek.apkupdater.viewmodel.MainViewModel
import com.google.accompanist.themeadapter.material.MdcTheme
import com.kryptoprefs.invoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class AppsFragment : Fragment() {

	private val repository: AppsRepository by inject()
	private val prefs: AppPrefs by inject()
	private val appsViewModel: AppsViewModel by viewModel()
	private val mainViewModel: MainViewModel by sharedViewModel()

	// MdcTheme reads the palette from the Activity's XML theme, which MainActivity
	// switches on theme changes (the activity is recreated), so no extra flag is needed.
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		ComposeView(requireContext()).apply { setContent { AppList() } }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		appsViewModel.items.observe<List<AppsItem>>(this) {
			mainViewModel.appsBadge.postValue(it.size)
		}
		updateApps()
	}

	private val onIgnoreClick = { app: AppsItem ->
		val ignoredApps = prefs.ignoredApps().toMutableList()
		if (ignoredApps.contains(app.packageName)) ignoredApps.remove(app.packageName) else ignoredApps.add(app.packageName)
		prefs.ignoredApps(ignoredApps)
		updateApps()
	}

	private fun updateApps() = viewLifecycleOwner.lifecycleScope.launch {
		// Enumerating and labelling all installed apps can take a while; keep it off the main thread.
		val apps = withContext(Dispatchers.IO) { repository.getApps().map { it.model } }
		appsViewModel.items.postValue(apps)
	}

	@Preview
	@Composable
	fun AppList() = MdcTheme {
		val state = appsViewModel.items.observeAsState()

		LazyColumn {
			items(state.value.orEmpty()) { AppCard(it) }
			item { Spacer(modifier = Modifier.size(60.dp)) }
		}
	}

	@Composable
	fun AppCard(app: AppsItem) = CustomCard(app.alpha) {
		Column(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
			AppInfo(app)
			ActionRow(actionOne = Action(stringResource(app.action)) { onIgnoreClick(app) })
		}
	}

}
