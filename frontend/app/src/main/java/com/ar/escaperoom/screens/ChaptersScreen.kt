package com.ar.escaperoom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.escaperoom.R
import com.ar.escaperoom.api.ApiService

@Composable
fun ChaptersScreen(
    userEmail: String,
    onSeeStory: () -> Unit,
    onReadStory: () -> Unit,
    onOpenChapterOne: () -> Unit = {},
    onBack: () -> Unit
) {
    var chapterOneCompleted by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(userEmail) {
        val completedElara  = ApiService.hasCompletedChapter(userEmail, "elara")
        val completedJulian = ApiService.hasCompletedChapter(userEmail, "julian")
        chapterOneCompleted = completedElara || completedJulian
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "THE STORY",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedButton(
                    onClick = onSeeStory,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text("SEE STORY", fontFamily = FontFamily.Serif, letterSpacing = 2.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onReadStory,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text("READ STORY", fontFamily = FontFamily.Serif, letterSpacing = 2.sp)
                }
            }

            // separator
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.5.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            // capitole 5
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f),
                verticalArrangement = Arrangement.Center
            ) {
                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chapter I
                    ChapterItem(
                        number    = "I",
                        status    = when (chapterOneCompleted) {
                            true  -> ChapterStatus.COMPLETED
                            false -> ChapterStatus.AVAILABLE
                            null  -> ChapterStatus.AVAILABLE
                        },
                        onClick   = onOpenChapterOne
                    )
                    // Chapters II–V
                    listOf("II", "III", "IV", "V").forEach { num ->
                        ChapterItem(number = num, status = ChapterStatus.LOCKED)
                    }
                }
            }
        }

        // Buton Back
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Text("← BACK", color = Color.Gray, fontFamily = FontFamily.Serif)
        }
    }
}

//Status

enum class ChapterStatus { AVAILABLE, COMPLETED, LOCKED }

@Composable
fun ChapterItem(number: String, status: ChapterStatus, onClick: (() -> Unit)? = null) {
    val clickable = onClick != null && status != ChapterStatus.LOCKED
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(180.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C2C2C))
                .then(if (clickable) Modifier.clickable { onClick!!() } else Modifier)
        ) {
            Image(
                painter = painterResource(id = R.drawable.papyrus),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (status == ChapterStatus.LOCKED) 0.35f else 1f
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (status) {
                            ChapterStatus.COMPLETED -> Color(0xFF1A1A1A).copy(alpha = 0.75f)
                            ChapterStatus.LOCKED    -> Color(0xFF1A1A1A).copy(alpha = 0.75f)
                            ChapterStatus.AVAILABLE -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (status) {
                    ChapterStatus.COMPLETED ->
                        Text("✓", fontSize = 14.sp, color = Color(0xFF7EC88A), fontWeight = FontWeight.Bold)
                    ChapterStatus.LOCKED ->
                        Text("🔒", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    ChapterStatus.AVAILABLE -> { }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CHAPTER $number",
            color = when (status) {
                ChapterStatus.LOCKED -> Color.White.copy(alpha = 0.3f)
                else                 -> Color.White
            },
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 3.sp
        )
    }
}
