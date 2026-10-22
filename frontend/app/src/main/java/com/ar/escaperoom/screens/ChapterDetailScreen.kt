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

private const val CHAPTER_ONE_DESC =
    "Two players, two locked rooms, one shared goal: free Elara before time runs out. " +
    "One of you explores Elara's frozen tower, the other Julian's cluttered workshop. " +
    "Neither of you sees the full picture: clues hidden on your side are the answers to your partner's puzzles, and the other way around.\n\n" +
    "You'll read clocks, crack a coded safe, weigh objects, decipher a parchment and feel your way through a hidden maze. " +
    "Only by talking to each other and passing items back and forth can you reach the final lock.\n\n" +
    "The real challenge isn't the puzzles. It's communication."

@Composable
fun ChapterDetailScreen(
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
                text = "CHAPTER I",
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The Frozen Tower",
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = CHAPTER_ONE_DESC,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                lineHeight = 29.sp,
                color = Color(0xFFE0DDD8),
                textAlign = TextAlign.Center
            )
        }

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
