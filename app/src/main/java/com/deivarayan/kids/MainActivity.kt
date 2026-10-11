package com.deivarayan.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deivarayan.kids.ui.navigation.KidsNavHost
import com.deivarayan.kids.ui.theme.KidsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KidsNavHost()
                }
            }
        }
    }
}
