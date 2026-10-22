package com.ar.escaperoom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val STORY_TEXT =
    "In the year 1884, a dark shadow fell upon the cold walls of BlackWood Manor. " +
    "Punished for a forbidden love, the heiress Elara was locked away in the freezing tower. " +
    "Stripped of all hope, she waited in the bitter cold... believing she had been abandoned forever. " +
    "But Julian, her beloved clockmaker, forged one final, desperate plan. " +
    "He hid the key to her salvation inside a complex mechanical box. " +
    "Tragically, its secret remained unsolved, freezing alongside her shattered heart. " +
    "Now their destiny rests with you. Communicate, solve the mistery... and save her."

@Composable
fun ReadStoryScreen(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "THE STORY",
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = STORY_TEXT,
                fontFamily = FontFamily.Serif,
                fontSize = 19.sp,
                lineHeight = 30.sp,
                color = Color(0xFFE0DDD8),
                textAlign = TextAlign.Center
            )
        }

        // buton Back
        Text(
            text = "← BACK",
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .clickable { onBack() }
        )
    }
}
