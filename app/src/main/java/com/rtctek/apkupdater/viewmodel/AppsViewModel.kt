package com.rtctek.apkupdater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rtctek.apkupdater.model.ui.AppsItem

class AppsViewModel : ViewModel() {

	val items = MutableLiveData<List<AppsItem>>()

}