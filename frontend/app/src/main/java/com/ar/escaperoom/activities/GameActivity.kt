package com.ar.escaperoom.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.escaperoom.api.ApiService
import com.ar.escaperoom.api.KtorClient
import com.ar.escaperoom.api.TokenHolder
import com.ar.escaperoom.api.dtos.CreateRoomRequest
import com.ar.escaperoom.api.dtos.InviteResponse
import com.ar.escaperoom.api.dtos.JoinRoomRequest
import com.ar.escaperoom.api.dtos.RoomResponse
import com.ar.escaperoom.screens.*
import com.ar.escaperoom.utils.FullscreenUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameActivity : ComponentActivity() {

    private var userEmail = ""
    private var userName  = ""

    // true la callback Unity
    @Volatile private var returnedFromUnity = false

    // increment cand termina unity
    private var composeResetKey by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        FullscreenUtils.enableFullscreenMode(this)

        userEmail = intent.getStringExtra("userEmail") ?: ""
        userName  = intent.getStringExtra("userName")  ?: ""


        //marchez ca se termina unity
        GameCallback.onGameFinished = { _ ->
            returnedFromUnity = true
        }

        setContent {
            //recreez UI si tot ce am in compose fara sa dau restart la activity
            key(composeResetKey) {
            val coroutineScope = rememberCoroutineScope()
            // starea se resetează cand key se schimba
            var currentScreen by remember { mutableStateOf(Screen.MainMenu.name) }

            // INVITATII

            // user1: invite trimis e acceptat deci popup
            var acceptedInvite by remember { mutableStateOf<InviteResponse?>(null) }
            val handledInviteIds = remember { mutableStateListOf<String>() }

            //user 2 dupa accept
            var hostMode by remember { mutableStateOf(0) }   // 0=niciunul, 1=astept, 2=user1 cancel
            var hostCode by remember { mutableStateOf("") }  // codul camera
            var hostInviter by remember { mutableStateOf("") } // numele invitatorului
            var hostInviteId by remember { mutableStateOf("") } // id invite
            var hostJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) } // job asteptare


            //poll user1 sa vad cand user2 accepta
            LaunchedEffect(Unit) {
                while (true) {
                    if (userEmail.isNotBlank() && acceptedInvite == null) {
                        val accepted = ApiService.getSentInvites(userEmail).firstOrNull {
                            it.status == "ACCEPTED" &&
                            it.id !in handledInviteIds &&
                            it.roomCode.isNotBlank() && it.roomCode != "pending"
                        }
                        if (accepted != null) acceptedInvite = accepted
                    }
                    kotlinx.coroutines.delay(1500)
                }
            }

            //user1 cancel, inchid popup plus notif
            LaunchedEffect(acceptedInvite?.id) {
                val inv = acceptedInvite ?: return@LaunchedEffect
                while (acceptedInvite?.id == inv.id) {
                    kotlinx.coroutines.delay(1500)
                    val fresh = ApiService.getInvite(inv.id)
                    if (fresh?.status == "CANCELLED") {
                        handledInviteIds.add(inv.id)
                        acceptedInvite = null
                        val who = inv.toPlayerName.ifBlank { inv.toPlayerId }
                        Toast.makeText(this@GameActivity,
                            "$who cancelled the game", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
            }

            when (currentScreen) {

                Screen.MainMenu.name -> {
                    MainMenuScreen(
                        onCreateRoom = { onRoomCodeReceived ->
                            coroutineScope.launch {
                                val response = createRoomOnServer(userEmail)
                                if (response != null) {
                                    onRoomCodeReceived(response.roomCode)
                                    waitForPlayerAndLaunch(response.roomCode)
                                }
                            }
                        },
                        onJoinRoom = { code ->
                            coroutineScope.launch {
                                val success = joinRoomOnServer(userEmail, code)
                                if (success) {
                                    Toast.makeText(this@GameActivity, "Joined room $code!", Toast.LENGTH_SHORT).show()
                                    launchUnityGame(code)
                                }
                            }
                        },
                        onSeeChapters  = { currentScreen = Screen.Chapters.name },
                        onProfileStats = { currentScreen = Screen.ProfileStats.name }
                    )
                }

                Screen.Chapters.name -> {
                    ChaptersScreen(
                        userEmail  = userEmail,
                        onSeeStory  = { currentScreen = Screen.BackstoryVideo.name },
                        onReadStory = { currentScreen = Screen.ReadStory.name },
                        onOpenChapterOne = { currentScreen = Screen.ChapterDetail.name },
                        onBack = { currentScreen = Screen.MainMenu.name }
                    )
                }

                // chapter 1 desc
                Screen.ChapterDetail.name -> {
                    ChapterDetailScreen(
                        onBack = { currentScreen = Screen.Chapters.name }
                    )
                }

                // video
                Screen.BackstoryVideo.name -> {
                    BackstoryVideoScreen(
                        onVideoComplete = { currentScreen = Screen.MainMenu.name },
                        onSkip          = { currentScreen = Screen.Chapters.name }
                    )
                }

                // povestea text
                Screen.ReadStory.name -> {
                    ReadStoryScreen(
                        onBack = { currentScreen = Screen.Chapters.name }
                    )
                }

                Screen.ProfileStats.name -> {
                    ProfileStatsScreen(
                        userEmail  = userEmail,
                        userName   = userName,
                        onBack     = { currentScreen = Screen.MainMenu.name },
                        onJoinRoom = { roomCode ->
                            coroutineScope.launch {
                                val success = joinRoomOnServer(userEmail, roomCode)
                                if (success) launchUnityGame(roomCode)
                            }
                        },
                        onAcceptInvite = { invite ->
                            // user 2 accepta creaza camera pune codul la invitatie
                            // asteapta rasp user1
                            hostJob = coroutineScope.launch {
                                val room = createRoomOnServer(userEmail)
                                if (room == null) return@launch
                                val ok = ApiService.acceptInvite(invite.id, room.roomCode)
                                if (ok == null) {
                                    Toast.makeText(this@GameActivity,
                                        "Invite expired or already processed",
                                        Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                hostInviter  = invite.fromPlayerName
                                hostCode     = room.roomCode
                                hostInviteId = invite.id
                                hostMode     = 1   // astept user1
                                when (waitForJoinOrCancel(room.roomCode, invite.id)) {
                                    "PLAYING"   -> { hostMode = 0; launchUnityGame(room.roomCode) }
                                    "CANCELLED" -> { hostMode = 2 } // user1 renunta
                                    else        -> { hostMode = 0 } // timeout
                                }
                            }
                        }
                    )
                }
            }

            // dialog user1, user2 accepta
            acceptedInvite?.let { inv ->
                val partner = inv.toPlayerName.ifBlank { inv.toPlayerId }
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = Color(0xFF2C2C2C),
                    title = {
                        Text("$partner accepted your invite!", color = Color.White,
                            fontFamily = FontFamily.Serif, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Room code:", color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text(inv.roomCode, color = Color.White, fontSize = 28.sp,
                                 fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Do you still want to play?", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            handledInviteIds.add(inv.id)
                            acceptedInvite = null
                            coroutineScope.launch {
                                ApiService.markInviteJoined(inv.id)
                                if (joinRoomOnServer(userEmail, inv.roomCode)) launchUnityGame(inv.roomCode)
                            }
                        }) { Text("Yes, join", color = Color.White, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            handledInviteIds.add(inv.id)
                            acceptedInvite = null
                            coroutineScope.launch { ApiService.cancelInvite(inv.id) }
                        }) { Text("No", color = Color.Gray) }
                    }
                )
            }

            //dialog user2
            //hostMode 1 = astept user1; 2 = user1 anuleaza
            if (hostMode != 0) {
                val waiting = hostMode == 1
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = Color(0xFF2C2C2C),
                    title = {
                        Text(
                            if (waiting) "Invite accepted" else "Invite declined",
                            color = Color.White, fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        if (waiting) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(hostCode, color = Color.White, fontSize = 28.sp,
                                     fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                                Spacer(Modifier.height(12.dp))
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Waiting for $hostInviter to join...", color = Color.Gray,
                                    fontSize = 12.sp, textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            Text("$hostInviter doesn't want to play anymore.", color = Color.Gray,
                                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            // stop polling
                            hostJob?.cancel()
                            hostJob = null
                            // anulez invitatia si user1 primeste notif daca inca se asteapta
                            if (waiting && hostInviteId.isNotBlank()) {
                                val id = hostInviteId
                                coroutineScope.launch { ApiService.cancelInvite(id) }
                            }
                            hostMode = 0
                        }) {
                            Text(if (waiting) "Cancel" else "OK", color = Color.White)
                        }
                    }
                )
            }
            } // end key(composeResetKey)
        }
    }

    override fun onResume() {
        super.onResume()
        FullscreenUtils.enableFullscreenMode(this)
        if (returnedFromUnity) {
            returnedFromUnity = false
            //inc key sa recreez UI
            composeResetKey++
        }
    }

    // NETWORK


    //host asteapta alaturarea sau anularea
    private suspend fun waitForJoinOrCancel(roomCode: String, inviteId: String): String {
        val maxAttempts = 150   // 5 minute
        repeat(maxAttempts) {
            kotlinx.coroutines.delay(2000)
            try {
                val room = KtorClient.httpClient.get(
                    "https://bethel-struthious-binomially.ngrok-free.dev/api/rooms/$roomCode"
                ) {
                    header("ngrok-skip-browser-warning", "true")
                    bearerAuth(TokenHolder.token ?: "")
                }.body<RoomResponse>()
                if (room.status == com.ar.escaperoom.api.dtos.RoomStatus.PLAYING) return "PLAYING"
            } catch (_: Exception) {}

            val invite = ApiService.getInvite(inviteId)
            if (invite?.status == "CANCELLED") return "CANCELLED"
        }
        return "TIMEOUT"
    }

    private suspend fun waitForPlayerAndLaunch(roomCode: String) {
        val maxAttempts = 150
        repeat(maxAttempts) {
            kotlinx.coroutines.delay(2000)
            try {
                val room = KtorClient.httpClient.get(
                    "https://bethel-struthious-binomially.ngrok-free.dev/api/rooms/$roomCode"
                ) {
                    header("ngrok-skip-browser-warning", "true")
                    bearerAuth(TokenHolder.token ?: "")
                }.body<RoomResponse>()
                if (room.status == com.ar.escaperoom.api.dtos.RoomStatus.PLAYING) {
                    withContext(Main) { launchUnityGame(roomCode) }
                    return
                }
            } catch (_: Exception) {}
        }
    }

    private fun launchUnityGame(roomCode: String) {
        returnedFromUnity = true
        val intent = Intent(this, MyUnityActivity::class.java).apply {
            putExtra("roomCode",  roomCode)
            putExtra("userEmail", userEmail)
            putExtra("userName",  userName)
        }
        startActivity(intent)
    }

    private suspend fun createRoomOnServer(hostEmail: String): RoomResponse? {
        return try {
            KtorClient.httpClient.post("https://bethel-struthious-binomially.ngrok-free.dev/api/rooms/create") {
                contentType(ContentType.Application.Json)
                header("ngrok-skip-browser-warning", "true")
                bearerAuth(TokenHolder.token ?: "")
                setBody(CreateRoomRequest(hostEmail))
            }.body<RoomResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Main) {
                Toast.makeText(this@GameActivity, "Eroare la creare cameră: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }

    private suspend fun joinRoomOnServer(guestEmail: String, roomCode: String): Boolean {
        return try {
            KtorClient.httpClient.post("https://bethel-struthious-binomially.ngrok-free.dev/api/rooms/join") {
                contentType(ContentType.Application.Json)
                header("ngrok-skip-browser-warning", "true")
                bearerAuth(TokenHolder.token ?: "")
                setBody(JoinRoomRequest(guestEmail, roomCode))
            }.body<RoomResponse>()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            val mesaj = when {
                e.message?.contains("not found", ignoreCase = true) == true -> "The code does not exist."
                e.message?.contains("full",      ignoreCase = true) == true -> "The room is already full."
                else -> "Invalid code. Please try again."
            }
            withContext(Main) {
                Toast.makeText(this@GameActivity, mesaj, Toast.LENGTH_LONG).show()
            }
            false
        }
    }
}
