package com.cloudmonitor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudmonitor.app.ui.navigation.AppNavGraph
import com.cloudmonitor.app.ui.theme.AwsDark
import com.cloudmonitor.app.ui.theme.CloudMonitorTheme
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AwsDark
                ) {
                    val viewModel: CloudMonitorViewModel = viewModel()
                    AppNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}
