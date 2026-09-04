package com.example.data.remote.supabase

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SupabaseAuthResult {
    data class Success(val session: UserSession, val userProfile: UserProfile) : SupabaseAuthResult()
    data class PasswordResetSent(val email: String) : SupabaseAuthResult()
    data class Error(val message: String, val statusCode: Int? = null) : SupabaseAuthResult()
}

class SupabaseAuthService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        targetExam: String
    ): SupabaseAuthResult = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) {
            // Local fallback simulated auth
            val mockId = "user_" + java.util.UUID.randomUUID().toString().take(8)
            val session = UserSession(
                accessToken = "mock_token_$mockId",
                refreshToken = "mock_refresh_$mockId",
                userId = mockId,
                email = email,
                role = UserRole.STUDENT,
                expiresAt = System.currentTimeMillis() + 86400000L
            )
            val profile = UserProfile(
                id = mockId,
                email = email,
                fullName = fullName,
                targetExam = targetExam,
                role = UserRole.STUDENT,
                dailyStudyHours = 4,
                streakDays = 1,
                xpPoints = 100,
                level = 1,
                readinessScore = 45,
                preferredLanguage = "BN"
            )
            return@withContext SupabaseAuthResult.Success(session, profile)
        }

        try {
            val url = "${SupabaseConfig.projectUrl}/auth/v1/signup"
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", fullName)
                    put("target_exam", targetExam)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token", "")
                val refreshToken = json.optString("refresh_token", "")
                val userObj = json.optJSONObject("user") ?: json
                val userId = userObj.optString("id", java.util.UUID.randomUUID().toString())
                val userEmail = userObj.optString("email", email)
                val metaObj = userObj.optJSONObject("user_metadata")

                val session = UserSession(
                    accessToken = accessToken.ifBlank { "token_$userId" },
                    refreshToken = refreshToken,
                    userId = userId,
                    email = userEmail,
                    role = UserRole.STUDENT,
                    expiresAt = System.currentTimeMillis() + 86400000L
                )

                val profile = UserProfile(
                    id = userId,
                    email = userEmail,
                    fullName = metaObj?.optString("full_name", fullName) ?: fullName,
                    targetExam = metaObj?.optString("target_exam", targetExam) ?: targetExam,
                    role = UserRole.STUDENT,
                    dailyStudyHours = 4,
                    streakDays = 1,
                    xpPoints = 100,
                    level = 1,
                    readinessScore = 45,
                    preferredLanguage = "BN"
                )

                SupabaseAuthResult.Success(session, profile)
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                SupabaseAuthResult.Error(errorMsg, response.code)
            }
        } catch (e: IOException) {
            SupabaseAuthResult.Error("Network failure connecting to authentication server: ${e.localizedMessage}")
        } catch (e: Exception) {
            SupabaseAuthResult.Error("Authentication error: ${e.localizedMessage}")
        }
    }

    suspend fun signIn(email: String, password: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) {
            val isAdmin = email.contains("admin", ignoreCase = true)
            val role = if (isAdmin) UserRole.ADMIN else UserRole.STUDENT
            val mockId = if (isAdmin) "admin_master_1" else "user_default_1"
            val session = UserSession(
                accessToken = "mock_token_$mockId",
                refreshToken = "mock_refresh_$mockId",
                userId = mockId,
                email = email,
                role = role,
                expiresAt = System.currentTimeMillis() + 86400000L
            )
            val profile = UserProfile(
                id = mockId,
                email = email,
                fullName = if (isAdmin) "Admin Commander" else "Tanvir Ahmed",
                role = role,
                targetExam = if (isAdmin) "Administration" else "46th BCS & Bangladesh Bank AD",
                dailyStudyHours = 4,
                streakDays = 12,
                xpPoints = 2450,
                level = 5,
                readinessScore = 78,
                preferredLanguage = "BN"
            )
            return@withContext SupabaseAuthResult.Success(session, profile)
        }

        try {
            val url = "${SupabaseConfig.projectUrl}/auth/v1/token?grant_type=password"
            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.getString("access_token")
                val refreshToken = json.optString("refresh_token", "")
                val userObj = json.getJSONObject("user")
                val userId = userObj.getString("id")
                val userEmail = userObj.optString("email", email)
                val userMeta = userObj.optJSONObject("user_metadata")

                val isAdmin = email.contains("admin", ignoreCase = true)
                val role = if (isAdmin) UserRole.ADMIN else UserRole.STUDENT

                val session = UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId,
                    email = userEmail,
                    role = role,
                    expiresAt = System.currentTimeMillis() + (json.optLong("expires_in", 3600) * 1000)
                )

                val profile = UserProfile(
                    id = userId,
                    email = userEmail,
                    fullName = userMeta?.optString("full_name", "Student") ?: "Student",
                    targetExam = userMeta?.optString("target_exam", "46th BCS Preliminary") ?: "46th BCS Preliminary",
                    role = role,
                    dailyStudyHours = 4,
                    streakDays = 1,
                    xpPoints = 100,
                    level = 1,
                    readinessScore = 50,
                    preferredLanguage = "BN"
                )

                SupabaseAuthResult.Success(session, profile)
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                SupabaseAuthResult.Error(errorMsg, response.code)
            }
        } catch (e: IOException) {
            SupabaseAuthResult.Error("Network connection error. Operating in offline mode.", -1)
        } catch (e: Exception) {
            SupabaseAuthResult.Error("Sign in error: ${e.localizedMessage}")
        }
    }

    suspend fun sendPasswordReset(email: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) {
            return@withContext SupabaseAuthResult.PasswordResetSent(email)
        }

        try {
            val url = "${SupabaseConfig.projectUrl}/auth/v1/recover"
            val payload = JSONObject().apply {
                put("email", email)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                SupabaseAuthResult.PasswordResetSent(email)
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                SupabaseAuthResult.Error(errorMsg, response.code)
            }
        } catch (e: IOException) {
            SupabaseAuthResult.Error("Failed to reach server: ${e.localizedMessage}")
        } catch (e: Exception) {
            SupabaseAuthResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun signOut(accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured() || accessToken.isNullOrBlank()) {
            return@withContext true
        }

        try {
            val url = "${SupabaseConfig.projectUrl}/auth/v1/logout"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .post("{}".toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true // Local logout succeeds even if network fails
        }
    }

    private fun parseErrorMessage(responseBody: String, statusCode: Int): String {
        return try {
            val json = JSONObject(responseBody)
            when {
                json.has("error_description") -> json.getString("error_description")
                json.has("msg") -> json.getString("msg")
                json.has("message") -> json.getString("message")
                else -> "Authentication failed with HTTP code $statusCode"
            }
        } catch (e: Exception) {
            "Request failed (status $statusCode): $responseBody"
        }
    }
}
