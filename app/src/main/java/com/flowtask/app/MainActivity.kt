package com.flowtask.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flowtask.app.core.designsystem.theme.FlowTaskTheme
import com.flowtask.app.core.navigation.FlowTaskApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowTaskTheme {
                FlowTaskApp()
            }
        }
    }
}
