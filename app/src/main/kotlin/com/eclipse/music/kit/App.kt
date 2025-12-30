@file:OptIn(ExperimentalAnimationApi::class)

package com.eclipse.music.kit

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eclipse.music.kit.navigation.Routes
import com.eclipse.music.kit.pages.HomePage
import com.eclipse.music.kit.pages.SettingsPage
import com.eclipse.music.kit.theme.MusicTikTheme

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    MusicTikTheme {
        val navController = rememberNavController()
        Scaffold {
            MainLayout(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainLayout(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomePage(navController)
        }
        composable(Routes.SETTINGS) {
            SettingsPage(navController)
        }
    }

}