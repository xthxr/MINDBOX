package com.example.mindbox.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val currentUserId: String? get() = auth.currentUser?.uid
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun signInAnonymously(): Result<FirebaseUser> = runCatching {
        auth.signInAnonymously().await().user!!
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user!!
    }

    suspend fun createAccountWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user!!
    }

    suspend fun linkAnonymousWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await().user!!
    }

    fun signOut() = auth.signOut()

    suspend fun ensureSignedIn(): String {
        if (currentUserId == null) {
            signInAnonymously()
        }
        return currentUserId ?: throw IllegalStateException("Could not authenticate with Firebase")
    }
}
