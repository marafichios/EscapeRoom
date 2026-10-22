package com.ar.escaperoom.api

import com.ar.escaperoom.api.dtos.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*


//apeluri http pt backend cu client ktor si header ngrok
object ApiService {

    private const val BASE = "https://bethel-struthious-binomially.ngrok-free.dev"
    private const val NGROK = "ngrok-skip-browser-warning"
    private val client get() = KtorClient.httpClient

    //atasez token
    private fun HttpRequestBuilder.withAuth() {
        bearerAuth(TokenHolder.token ?: "")
    }

    // SESIUNI

    suspend fun getSessions(playerId: String): List<GameSessionResponse> = try {
        client.get("$BASE/api/sessions/$playerId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiListResponse<GameSessionResponse>>().data ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun getBestSession(playerId: String): GameSessionResponse? = try {
        client.get("$BASE/api/sessions/best/$playerId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiSingleResponse<GameSessionResponse>>().data
    } catch (e: Exception) { null }

    suspend fun hasCompletedChapter(playerId: String, chapterId: String): Boolean = try {
        val resp = client.get("$BASE/api/sessions/completed/$playerId/$chapterId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiSingleResponse<Map<String, Boolean>>>()
        resp.data?.get("completed") == true
    } catch (e: Exception) { false }

    // CLASAMENT

    // paginare
    suspend fun getLeaderboard(chapterId: String, page: Int = 0, size: Int = 50): List<LeaderboardEntryResponse> = try {
        client.get("$BASE/api/sessions/leaderboard/$chapterId") {
            header(NGROK, "true")
            withAuth()
            parameter("page", page)
            parameter("size", size)
        }.body<ApiListResponse<LeaderboardEntryResponse>>().data ?: emptyList()
    } catch (e: Exception) { emptyList() }

    // PROFIL USERI

    suspend fun getUserProfile(email: String): UserSearchResult? = try {
        client.get("$BASE/api/users/$email") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiSingleResponse<UserSearchResult>>().data
    } catch (e: Exception) { null }

    // CAUT USERI

    suspend fun searchUsers(name: String): List<UserSearchResult> {
        if (name.length < 2) return emptyList()
        return try {
            client.get("$BASE/api/users/search") {
                header(NGROK, "true")
                withAuth()
                parameter("name", name)
            }.body<ApiListResponse<UserSearchResult>>().data ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // INVITATII

    suspend fun getPendingInvites(playerId: String): List<InviteResponse> = try {
        client.get("$BASE/api/invites/pending/$playerId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiListResponse<InviteResponse>>().data ?: emptyList()
    } catch (e: Exception) { emptyList() }

    //invitatii active pt polling
    suspend fun getSentInvites(playerId: String): List<InviteResponse> = try {
        client.get("$BASE/api/invites/sent/$playerId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiListResponse<InviteResponse>>().data ?: emptyList()
    } catch (e: Exception) { emptyList() }

    //host verifica daca user1 anuleaza
    suspend fun getInvite(inviteId: String): InviteResponse? = try {
        client.get("$BASE/api/invites/$inviteId") {
            header(NGROK, "true")
            withAuth()
        }.body<ApiSingleResponse<InviteResponse>>().data
    } catch (e: Exception) { null }

    suspend fun sendInvite(request: SendInviteRequest): Boolean = try {
        client.post("$BASE/api/invites") {
            contentType(ContentType.Application.Json)
            header(NGROK, "true")
            withAuth()
            setBody(request)
        }.status.isSuccess()
    } catch (e: Exception) { false }

    //accept - se trm cod camera creata de host
    suspend fun acceptInvite(inviteId: String, roomCode: String): AcceptInviteResponse? = try {
        client.put("$BASE/api/invites/$inviteId/accept") {
            contentType(ContentType.Application.Json)
            header(NGROK, "true")
            withAuth()
            setBody(AcceptInviteRequest(roomCode))
        }.body<ApiSingleResponse<AcceptInviteResponse>>().data
    } catch (e: Exception) { null }

    suspend fun declineInvite(inviteId: String): Boolean = try {
        client.put("$BASE/api/invites/$inviteId/decline") {
            header(NGROK, "true")
            withAuth()
        }.status.isSuccess()
    } catch (e: Exception) { false }

    // user1 renunta dupa ce user2 accepta
    suspend fun cancelInvite(inviteId: String): Boolean = try {
        client.put("$BASE/api/invites/$inviteId/cancel") {
            header(NGROK, "true")
            withAuth()
        }.status.isSuccess()
    } catch (e: Exception) { false }

    // user1 se alatura
    suspend fun markInviteJoined(inviteId: String): Boolean = try {
        client.put("$BASE/api/invites/$inviteId/joined") {
            header(NGROK, "true")
            withAuth()
        }.status.isSuccess()
    } catch (e: Exception) { false }
}
