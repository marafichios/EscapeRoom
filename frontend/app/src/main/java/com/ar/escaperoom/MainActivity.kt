package com.ar.escaperoom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.ar.escaperoom.activities.GameActivity
import com.ar.escaperoom.api.KtorClient
import com.ar.escaperoom.api.TokenHolder
import com.ar.escaperoom.api.dtos.AuthRequest
import com.ar.escaperoom.api.dtos.AuthResponse
import com.ar.escaperoom.auth.GoogleAuthManager
import com.ar.escaperoom.screens.BackstoryVideoScreen
import com.ar.escaperoom.screens.Screen
import com.ar.escaperoom.screens.WelcomeScreen
import com.ar.escaperoom.utils.FullscreenUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        FullscreenUtils.enableFullscreenMode(this)

        val authManager = GoogleAuthManager(this)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            var currentScreen by rememberSaveable { mutableStateOf(Screen.Welcome.name) }
            var currentUserEmail by rememberSaveable { mutableStateOf("") }
            var currentUserName  by rememberSaveable { mutableStateOf("") }

            when (currentScreen) {
                Screen.Welcome.name -> {
                    WelcomeScreen(
                        authManager = authManager,
                        onTokenReceived = { token ->
                            coroutineScope.launch {
                                val result = sendTokenToBackend(token)
                                if (result != null) {
                                    currentUserEmail = result.first
                                    currentUserName  = result.second
                                    currentScreen    = Screen.BackstoryVideo.name
                                }
                            }
                        },
                        onError = { message ->
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Screen.BackstoryVideo.name -> {
                    BackstoryVideoScreen(
                        onVideoComplete = {
                            val intent = Intent(this@MainActivity, GameActivity::class.java).apply {
                                putExtra("userEmail", currentUserEmail)
                                putExtra("userName",  currentUserName)
                            }
                            startActivity(intent)
                            // Disable animatie tranzitie activitate
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                            } else {
                                @Suppress("DEPRECATION")
                                overridePendingTransition(0, 0)
                            }
                            finish() //main activity done
                        }
                    )
                }
            }
        }
    }

    // Pair(email, name) sau null daca autentificarea nu merge
    private suspend fun sendTokenToBackend(token: String): Pair<String, String>? {
        return try {
            val response: AuthResponse =
                KtorClient.httpClient.post("https://bethel-struthious-binomially.ngrok-free.dev/authenticate") {
                    contentType(ContentType.Application.Json)
                    header("ngrok-skip-browser-warning", "true")
                    setBody(AuthRequest(token))
                }.body()

            withContext(Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Welcome, ${response.username ?: "fellow player"}!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val email = response.email ?: return null
            val name  = response.username ?: email
            TokenHolder.token = response.token
            Pair(email, name)
        } catch (e: Exception) {
            Log.e("Ktor_Error", "Err ngrok: ${e.message}", e)
            withContext(Main) {
                Toast.makeText(this@MainActivity, "Server connection error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }
}