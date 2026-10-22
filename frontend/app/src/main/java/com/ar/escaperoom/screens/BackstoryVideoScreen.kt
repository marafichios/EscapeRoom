package com.ar.escaperoom.screens

import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ar.escaperoom.R

@Composable
fun BackstoryVideoScreen(
    onVideoComplete: () -> Unit,
    onSkip: () -> Unit = onVideoComplete   // implicit la fel ca finalul
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Lock orientare pe landscape
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { /* pastram landscape */ }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // fullscreen, no controls
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, //full pe ambele dimensiuni
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Set video URI
                    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.backstory}")
                    setVideoURI(videoUri)
                    
                    // user cannot pause
                    setMediaController(null)
                    
                    // Set completion listener
                    setOnCompletionListener {
                        onVideoComplete()
                    }
                    
                    // Auto-play
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.start()
                    }
                    
                    // err handling
                    setOnErrorListener { _, what, extra ->
                        Log.e("BackstoryVideo", "Error: what=$what, extra=$extra")
                        false
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Skip button overlay
        TextButton(
            onClick = {
                onSkip()
            },
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Skip",
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
