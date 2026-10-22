package com.ar.escaperoom.utils

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object FullscreenUtils {
    

    fun enableFullscreenMode(activity: ComponentActivity) {
        //edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        
        // ascunde system bars
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    

    fun showSystemBars(activity: ComponentActivity) {
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}
