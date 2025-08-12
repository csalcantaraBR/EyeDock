package com.eyedock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.camera.core.ExperimentalGetImage
import com.eyedock.app.navigation.EyeDockNavigation
import com.eyedock.app.ui.theme.EyeDockTheme

class MainActivity : ComponentActivity() {
    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EyeDockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EyeDockApp()
                }
            }
        }
    }
}

@Composable
@ExperimentalGetImage
fun EyeDockApp() {
    val navController = rememberNavController()
    EyeDockNavigation(navController = navController)
}

