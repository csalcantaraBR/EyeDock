package com.eyedock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.eyedock.app.navigation.EyeDockNavigation
import com.eyedock.app.ui.theme.EyeDockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            EyeDockTheme {
                EyeDockApp()
            }
        }
    }
}

@Composable
fun EyeDockApp() {
    EyeDockNavigation()
}

