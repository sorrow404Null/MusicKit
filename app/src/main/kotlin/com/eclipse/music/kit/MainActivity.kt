package com.eclipse.music.kit

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.eclipse.music.kit.utils.AndroidAppContext
import com.eclipse.music.kit.utils.LanguageUtils
import com.eclipse.music.kit.utils.data.SettingsRepository
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val repo = SettingsRepository(newBase)
        val language = runBlocking { repo.load().language }
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase, language))
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            AndroidAppContext.init(this)
            App()
        }
    }
}
