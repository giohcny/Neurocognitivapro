package com.neurocognitiva.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.neurocognitiva.pro.ui.AppRoot
import com.neurocognitiva.pro.ui.theme.NeuroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeuroTheme {
                AppRoot()
            }
        }
    }
}
