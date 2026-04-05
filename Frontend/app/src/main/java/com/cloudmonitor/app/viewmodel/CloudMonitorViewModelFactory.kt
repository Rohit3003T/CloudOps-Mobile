package com.cloudmonitor.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for [CloudMonitorViewModel].
 *
 * Usage in Compose:
 *   val viewModel: CloudMonitorViewModel = viewModel(
 *       factory = CloudMonitorViewModelFactory(application)
 *   )
 *
 * When using `viewModel()` inside an Activity/Fragment via
 * `androidx.lifecycle.viewmodel.compose.viewModel()`, the factory is not
 * needed because AndroidViewModel automatically receives the Application
 * from the ViewModelStore owner. This factory is provided for explicit
 * instantiation and testing.
 */
class CloudMonitorViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudMonitorViewModel::class.java)) {
            return CloudMonitorViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
