package com.cloudops.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudops.mobile.ui.CloudOpsNavGraph
import com.cloudops.mobile.ui.theme.CloudOpsMobileTheme
import com.cloudops.mobile.viewmodel.MainViewModel
import com.cloudops.mobile.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudOpsMobileTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(applicationContext)
                )
                CloudOpsNavGraph(viewModel = viewModel)
            }
        }
    }
}
