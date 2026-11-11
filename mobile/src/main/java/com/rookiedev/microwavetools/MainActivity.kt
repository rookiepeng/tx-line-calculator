package com.rookiedev.microwavetools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rookiedev.microwavetools.ui.TxLineApp
import com.rookiedev.microwavetools.ui.theme.TxLineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TxLineTheme {
                TxLineApp()
            }
        }
    }
}
