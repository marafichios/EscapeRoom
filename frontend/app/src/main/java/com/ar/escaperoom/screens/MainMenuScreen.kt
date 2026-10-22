package com.ar.escaperoom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onCreateRoom: (onRoomCodeReceived: (String) -> Unit) -> Unit,
    onJoinRoom: (String) -> Unit,
    onSeeChapters: () -> Unit,
    onProfileStats: () -> Unit
) {
    var showJoinDialog   by remember { mutableStateOf(false) }
    var roomCodeInput    by remember { mutableStateOf("") }
    var showHostDialog   by remember { mutableStateOf(false) }
    var currentRoomCode  by remember { mutableStateOf("...") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TITLU
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Blackwood",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 4.sp
                )
                Text(
                    text = "LOBBY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif,
                    color = Color.Gray,
                    letterSpacing = 8.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // BUTOANE
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                Button(
                    onClick = {
                        showHostDialog = true
                        onCreateRoom { code -> currentRoomCode = code }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor   = Color.Black
                    )
                ) {
                    Text("Create New Room", fontSize = 16.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text("Join Existing Room", fontSize = 16.sp, fontFamily = FontFamily.Serif)
                }

                OutlinedButton(
                    onClick = { onSeeChapters() },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text("See Chapters", fontSize = 16.sp, fontFamily = FontFamily.Serif)
                }

                OutlinedButton(
                    onClick = { onProfileStats() },
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text("My Profile & Stats", fontSize = 16.sp, fontFamily = FontFamily.Serif)
                }
            }
        }

        // DIALOGURI
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = Color(0xFF2C2C2C),
                title = { Text("Enter Room Code", color = Color.White, fontFamily = FontFamily.Serif) },
                text = {
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase().take(6) },
                        label = { Text("Code", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor   = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showJoinDialog = false; onJoinRoom(roomCodeInput) }) {
                        Text("Join", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showHostDialog) {
            AlertDialog(
                onDismissRequest = { showHostDialog = false },
                containerColor = Color(0xFF2C2C2C),
                title = {
                    Text(
                        "Room Created",
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tell your partner this code:", color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(currentRoomCode, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Waiting for player 2...", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                },
                // Popup se inchide singur
                confirmButton = { }
            )
        }
    }
}
