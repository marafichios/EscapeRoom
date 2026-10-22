package com.ar.escaperoom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.escaperoom.R
import com.ar.escaperoom.auth.GoogleAuthManager
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    authManager: GoogleAuthManager,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
) {
    // latimea ecranului
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // factor de scalare bazat pe un ecran standard de 400dp
    val scaleFactor = (screenWidth / 400f).coerceAtMost(1f)

    // scalarea la dimensiunile mari
    val blackwoodSize = (50 * scaleFactor).sp
    val manorSize = (68 * scaleFactor).sp
    val manorSpacing = (10 * scaleFactor).sp

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets(0.dp))
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.welcome_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Title section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Blackwood",
                    fontSize = blackwoodSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    letterSpacing = 5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "MANOR",
                    fontSize = manorSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    letterSpacing = manorSpacing,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // line
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.8f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "1884",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif,
                    color = Color.White.copy(alpha = 0.95f),
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Tagline
                Text(
                    text = "A forbidden love.\nA secret buried in time.\nTwo players. One chance.",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Serif,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Bottom Section with Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Connecting...",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp
                    )
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    val token = authManager.signIn()

                                    if (token != null) {
                                        onTokenReceived(token)
                                    } else {
                                        onError("Login failed")
                                    }
                                } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                                    // userul a inchis fereastra "Choose an account" NU e err
                                } catch (e: Exception) {
                                    onError("Login exception: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.95f),
                            contentColor = Color(0xFF2C1810)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 16.dp
                        )
                    ) {
                        Text(
                            text = "Enter the Manor",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Sign in with Google to begin",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}