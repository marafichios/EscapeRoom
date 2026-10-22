package com.ar.escaperoom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ar.escaperoom.api.ApiService
import com.ar.escaperoom.api.dtos.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val BG       = Color(0xFF1A1A1A)
private val HI       = Color(0xFFE0DDD8)
private val MID      = Color(0xFF999690)
private val DIM      = Color(0xFF4A4845)
private val RULE     = Color(0xFF2B2927)
private const val LB_PAGE_SIZE = 50
private val SERIF    = FontFamily.Serif


@Composable
fun ProfileStatsScreen(
    userEmail: String,
    userName: String,
    onBack: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onAcceptInvite: (InviteResponse) -> Unit = {}   // accept invitatie flux in gameactivity
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Stats", "Leaderboard", "Search")

    var sessions        by remember { mutableStateOf<List<GameSessionResponse>>(emptyList()) }
    var bestSession     by remember { mutableStateOf<GameSessionResponse?>(null) }
    var leaderboard     by remember { mutableStateOf<List<LeaderboardEntryResponse>>(emptyList()) }
    var lbPage          by remember { mutableStateOf(0) }      // urmatoarea pagina de incarcat
    var lbHasMore       by remember { mutableStateOf(true) }   // mai sunt pagini sau nu
    var lbLoading       by remember { mutableStateOf(false) }  // o pagina se incarca acum
    var searchQuery     by remember { mutableStateOf("") }
    var searchResults   by remember { mutableStateOf<List<UserSearchResult>>(emptyList()) }
    var pendingInvites  by remember { mutableStateOf<List<InviteResponse>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    // Emailurile pt cei la care am trm invitatii
    var invitedEmails   by remember { mutableStateOf<Set<String>>(emptySet()) }

    // poze de prf useri1
    var inviterPics     by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    //invitatii pe care le am scos ca sa nu reapara la poll urm
    var dismissedInviteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // invitatii trimise si refuzate
    var declineHandledIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        sessions          = ApiService.getSessions(userEmail)
        bestSession       = ApiService.getBestSession(userEmail)
        profilePictureUrl = ApiService.getUserProfile(userEmail)?.profilePicture
        isLoading         = false
    }

    // Polling pt invitatii primite
    LaunchedEffect(Unit) {
        while (true) {
            val fresh = ApiService.getPendingInvites(userEmail)
                .filter { it.id !in dismissedInviteIds }
            pendingInvites = fresh
            // poze prf
            val missing = fresh.map { it.fromPlayerId }.distinct().filter { it !in inviterPics }
            if (missing.isNotEmpty()) {
                inviterPics = inviterPics + missing.associateWith { email ->
                    ApiService.getUserProfile(email)?.profilePicture
                }
            }
            kotlinx.coroutines.delay(2500)
        }
    }

    //prima pagina de leaderboard cand intru pe tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && leaderboard.isEmpty() && lbHasMore && !lbLoading) {
            lbLoading = true
            val first = ApiService.getLeaderboard("chapter1", 0, LB_PAGE_SIZE)
            leaderboard = first
            lbHasMore  = first.size == LB_PAGE_SIZE
            lbPage     = 1
            lbLoading  = false
        }
    }

    //pt declined dau notif in timp real
    //resetez buton invite
    LaunchedEffect(Unit) {
        var firstPoll = true
        while (true) {
            if (userEmail.isNotBlank()) {
                val sent = ApiService.getSentInvites(userEmail)
                val resolved = sent.filter {
                    (it.status == "DECLINED" || it.status == "CANCELLED") && it.id !in declineHandledIds
                }
                if (resolved.isNotEmpty()) {
                    declineHandledIds = declineHandledIds + resolved.map { it.id }
                    invitedEmails = invitedEmails - resolved.map { it.toPlayerId }.toSet()
                    // Notific doar refuzuri noi
                    if (!firstPoll) {
                        resolved.firstOrNull { it.status == "DECLINED" }?.let {
                            val who = it.toPlayerName.ifBlank { it.toPlayerId }
                            android.widget.Toast.makeText(
                                context, "$who declined your invite", android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            firstPoll = false
            kotlinx.coroutines.delay(1500)
        }
    }

    // Debounce search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            kotlinx.coroutines.delay(300)
            // exclud contul meu la search
            searchResults = ApiService.searchUsers(searchQuery)
                .filter { it.email != userEmail }
        } else {
            searchResults = emptyList()
        }
    }

    //landscape
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2826)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 16.sp,
                            fontFamily = SERIF,
                            color = MID
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SERIF,
                    letterSpacing = 0.5.sp,
                    color = HI
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$userEmail · joined recently",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    color = DIM
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "← BACK",
                    fontSize = 11.sp,
                    fontFamily = SERIF,
                    letterSpacing = 1.sp,
                    color = DIM,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBack() }
                )
            }

            //tab bar
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
            ) {
                tabs.forEachIndexed { i, label ->
                    val active = selectedTab == i
                    Column(
                        // pun pe latime text
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedTab = i }
                            .padding(end = 28.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            fontSize = 11.sp,
                            fontFamily = SERIF,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 2.sp,
                            color = if (active) HI else DIM,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (active) HI.copy(alpha = 0.7f) else Color.Transparent)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(RULE)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // CONTENT TAB
            when (selectedTab) {
                0 -> StatsTab(
                    bestSession = bestSession,
                    sessions    = sessions,
                    isLoading   = isLoading
                )
                1 -> LeaderboardTab(
                    entries       = leaderboard,
                    isLoading     = lbLoading && leaderboard.isEmpty(),
                    isLoadingMore = lbLoading && leaderboard.isNotEmpty(),
                    hasMore       = lbHasMore,
                    onLoadMore    = {
                        if (!lbLoading && lbHasMore) {
                            lbLoading = true
                            scope.launch {
                                val next = ApiService.getLeaderboard("chapter1", lbPage, LB_PAGE_SIZE)
                                leaderboard = leaderboard + next
                                lbHasMore = next.size == LB_PAGE_SIZE
                                lbPage += 1
                                lbLoading = false
                            }
                        }
                    }
                )
                2 -> SearchTab(
                    userEmail      = userEmail,
                    userName       = userName,
                    searchQuery    = searchQuery,
                    onQueryChange  = { searchQuery = it },
                    searchResults  = searchResults,
                    pendingInvites = pendingInvites,
                    inviterPics    = inviterPics,
                    invitedEmails  = invitedEmails,
                    onInvite       = { user ->
                        // marchez ca invitat si dezactiv buton
                        invitedEmails = invitedEmails + user.email
                        scope.launch {
                            ApiService.sendInvite(
                                SendInviteRequest(
                                    fromPlayerId   = userEmail,
                                    fromPlayerName = userName,
                                    toPlayerId     = user.email,
                                    toPlayerName   = user.username,
                                    roomCode       = "pending"
                                )
                            )
                        }
                        // toast confirmare
                        android.widget.Toast.makeText(
                            context, "Invite sent!", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onAccept       = { invite ->
                        // Scot din lista si pornesc fluxu
                        dismissedInviteIds = dismissedInviteIds + invite.id
                        pendingInvites = pendingInvites.filter { it.id != invite.id }
                        onAcceptInvite(invite)
                    },
                    onDecline = { invite ->
                        // dispare din lista
                        dismissedInviteIds = dismissedInviteIds + invite.id
                        pendingInvites = pendingInvites.filter { it.id != invite.id }
                        scope.launch { ApiService.declineInvite(invite.id) }
                    }
                )
            }
        }
    }
}

// STATS

@Composable
private fun StatsTab(
    bestSession: GameSessionResponse?,
    sessions: List<GameSessionResponse>,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        // BEST RUN
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "CHAPTER I — BEST RUN",
                fontSize = 9.sp,
                fontFamily = SERIF,
                letterSpacing = 2.sp,
                color = DIM
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Text("...", fontSize = 38.sp, fontFamily = SERIF, color = DIM)
            } else if (bestSession != null) {
                Text(
                    text = formatDuration(bestSession.durationSeconds),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SERIF,
                    letterSpacing = 0.5.sp,
                    color = HI,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${bestSession.hintsUsed} hints  ·  ${bestSession.chatMessages} messages  ·  ${formatVoice(bestSession.voiceSeconds)} voice",
                    fontSize = 13.sp,
                    fontFamily = SERIF,
                    color = MID
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "with ${bestSession.partnerName}  ·  ${bestSession.completedAt.take(10)}",
                    fontSize = 12.sp,
                    fontFamily = SERIF,
                    color = DIM
                )
            } else {
                Text(
                    text = "No runs yet.",
                    fontSize = 16.sp,
                    fontFamily = SERIF,
                    color = DIM
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Chapters II – V  coming soon",
                fontSize = 11.sp,
                fontFamily = SERIF,
                color = DIM.copy(alpha = 0.5f)
            )
        }

        // RECENT RUNS
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RECENT RUNS",
                fontSize = 9.sp,
                fontFamily = SERIF,
                letterSpacing = 2.sp,
                color = DIM
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty() && !isLoading) {
                Text("No runs yet.", fontSize = 13.sp, fontFamily = SERIF, color = DIM)
            } else {
                // Cele mai RECENTE
                val recent = sessions.sortedByDescending { it.completedAt }.take(5)
                recent.forEachIndexed { i, s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s.completedAt.take(10)}  ·  with ${s.partnerName}",
                            fontSize = 13.sp,
                            fontFamily = SERIF,
                            color = MID
                        )
                        Text(
                            text = formatDuration(s.durationSeconds),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Default,
                            color = if (i == 0) HI else MID
                        )
                    }
                    if (i < recent.size - 1) {
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(RULE))
                    }
                }
            }
        }
    }
}

// CLASAMENT

@Composable
private fun LeaderboardTab(
    entries: List<LeaderboardEntryResponse>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CHAPTER I",
            fontSize = 9.sp,
            fontFamily = SERIF,
            letterSpacing = 2.sp,
            color = DIM
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Text("Loading...", fontSize = 13.sp, fontFamily = SERIF, color = DIM)
            return@Column
        }

        if (entries.isEmpty()) {
            Text("No runs recorded yet.", fontSize = 14.sp, fontFamily = SERIF, color = DIM)
            return@Column
        }

        val listState = rememberLazyListState()

        // Infinite scroll
        LaunchedEffect(listState, entries.size, hasMore, isLoadingMore) {
            snapshotFlow {
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            }.collect { lastVisible ->
                if (hasMore && !isLoadingMore && lastVisible >= entries.size - 3) {
                    onLoadMore()
                }
            }
        }

        LazyColumn(state = listState) {
            itemsIndexed(entries) { index, entry ->
                LeaderboardRow(rank = index + 1, entry = entry)
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(RULE))
            }
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MID, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntryResponse) {
    var expanded by remember { mutableStateOf(false) }
    val rankColor = when (rank) {
        1    -> Color(0xFFB8963A)
        2    -> Color(0xFF8A8A8A)
        else -> DIM
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontSize = 11.sp,
                fontFamily = FontFamily.Default,
                color = rankColor,
                modifier = Modifier.width(22.dp)
            )
            Text(
                // nume afisat
                text = "${entry.playerName.ifBlank { entry.playerId }}  ",
                fontSize = 15.sp,
                fontFamily = SERIF,
                color = HI,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "&",
                fontSize = 12.sp,
                fontFamily = SERIF,
                color = DIM
            )
            Text(
                text = "  ${entry.partnerName}",
                fontSize = 15.sp,
                fontFamily = SERIF,
                color = HI,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatDuration(entry.durationSeconds),
                fontSize = 13.sp,
                fontFamily = FontFamily.Default,
                color = MID
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${entry.hintsUsed} hints  ·  ${entry.chatMessages} messages  ·  ${formatVoice(entry.voiceSeconds)} voice  ·  ${entry.completedAt.take(10)}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Default,
                color = DIM,
                modifier = Modifier.padding(start = 22.dp)
            )
        }
    }
}

// SEARCH

@Composable
private fun SearchTab(
    userEmail: String,
    userName: String,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<UserSearchResult>,
    pendingInvites: List<InviteResponse>,
    inviterPics: Map<String, String?>,
    invitedEmails: Set<String>,
    onInvite: (UserSearchResult) -> Unit,
    onAccept: (InviteResponse) -> Unit,
    onDecline: (InviteResponse) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        //search + rezultate
        Column(modifier = Modifier.weight(1f)) {
            // Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = SERIF,
                        color = HI
                    ),
                    cursorBrush = SolidColor(MID),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search by name",
                                    fontSize = 14.sp,
                                    fontFamily = SERIF,
                                    color = DIM
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(RULE))
            Spacer(modifier = Modifier.height(12.dp))

            // Rezultate
            if (searchResults.isEmpty() && searchQuery.length >= 2) {
                Text("No players found.", fontSize = 13.sp, fontFamily = SERIF, color = DIM)
            } else {
                LazyColumn {
                    itemsIndexed(searchResults) { i, user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262422)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user.profilePicture.isNullOrBlank()) {
                                    AsyncImage(
                                        model = user.profilePicture,
                                        contentDescription = "Profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        fontSize = 13.sp,
                                        fontFamily = SERIF,
                                        color = MID
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.username,
                                    fontSize = 14.sp,
                                    fontFamily = SERIF,
                                    color = HI
                                )
                            }
                            // Buton Invite
                            val alreadyInvited = invitedEmails.contains(user.email)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (alreadyInvited) Color(0xFF1F1E1C) else Color(0xFF2A2826))
                                    .then(
                                        if (alreadyInvited) Modifier
                                        else Modifier.clickable { onInvite(user) }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (alreadyInvited) "Invited" else "Invite",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Default,
                                    letterSpacing = 1.sp,
                                    color = if (alreadyInvited) DIM else MID
                                )
                            }
                        }
                        if (i < searchResults.size - 1) {
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(RULE))
                        }
                    }
                }
            }
        }

        //invite-uri primite
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PENDING INVITES",
                fontSize = 9.sp,
                fontFamily = SERIF,
                letterSpacing = 2.sp,
                color = DIM
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (pendingInvites.isEmpty()) {
                Text("No pending invites.", fontSize = 13.sp, fontFamily = SERIF, color = DIM)
            } else {
                pendingInvites.forEach { invite ->
                    PendingInviteRow(
                        invite     = invite,
                        avatarUrl  = inviterPics[invite.fromPlayerId],
                        onAccept   = { onAccept(invite) },
                        onDecline  = { onDecline(invite) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PendingInviteRow(
    invite: InviteResponse,
    avatarUrl: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val minutesLeft = ((invite.expiresAt - System.currentTimeMillis()) / 60_000).coerceAtLeast(0)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262422)),
                contentAlignment = Alignment.Center
            ) {
              if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
              } else {
                Text(
                    text = invite.fromPlayerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 12.sp,
                    fontFamily = SERIF,
                    color = MID
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invite.fromPlayerName,
                    fontSize = 14.sp,
                    fontFamily = SERIF,
                    color = HI
                )
                Text(
                    text = "expires in ${minutesLeft} min",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Default,
                    color = DIM
                )
            }
            // Accept
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE8E5E0))
                    .clickable { onAccept() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Accept",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 1.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Decline
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF252321))
                    .clickable { onDecline() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Decline",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 1.sp,
                    color = DIM
                )
            }
        }
    }
}

// UTIL

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

// Sub un min aifsez secunde
private fun formatVoice(seconds: Int): String {
    if (seconds < 60) return "$seconds sec"
    val m = seconds / 60
    val s = seconds % 60
    return if (s == 0) "$m min" else "$m min $s sec"
}
