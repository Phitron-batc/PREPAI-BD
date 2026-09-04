package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import com.example.data.model.UserSession

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ROLE, session.role.name)
            .putLong(KEY_EXPIRES_AT, session.expiresAt)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getSession(): UserSession? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val roleStr = prefs.getString(KEY_ROLE, UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.STUDENT
        }
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        return UserSession(
            accessToken = token,
            refreshToken = refreshToken,
            userId = userId,
            email = email,
            role = role,
            expiresAt = expiresAt
        )
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserRole(): UserRole {
        val roleStr = prefs.getString(KEY_ROLE, UserRole.STUDENT.name)
        return try {
            UserRole.valueOf(roleStr ?: UserRole.STUDENT.name)
        } catch (e: Exception) {
            UserRole.STUDENT
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getSession() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "prepai_bd_secure_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
