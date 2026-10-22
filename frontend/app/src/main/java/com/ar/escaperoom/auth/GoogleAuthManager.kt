package com.ar.escaperoom.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthManager(private val activity: Activity) {

    private val credentialManager = CredentialManager.create(activity)

    private val webClientId = "649480512750-3jc090uloejv2bi3fhqltvbgn1qcvosv.apps.googleusercontent.com"

    suspend fun signIn(): String? {
        val token = tryGoogleIdOption()
        if (token != null) return token

        return trySignInWithGoogle()
    }

    private suspend fun tryGoogleIdOption(): String? {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            extractToken(result.credential)
        } catch (e: GetCredentialCancellationException) {
            throw e   // utilizatorul a anulat
        } catch (e: NoCredentialException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun trySignInWithGoogle(): String? {
        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            extractToken(result.credential)
        } catch (e: GetCredentialCancellationException) {
            throw e   // utilizatorul a anulat
        } catch (e: Exception) {
            null
        }
    }

    private fun extractToken(credential: androidx.credentials.Credential): String? {
        if (credential is GoogleIdTokenCredential) {
            return credential.idToken
        }

        return try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}