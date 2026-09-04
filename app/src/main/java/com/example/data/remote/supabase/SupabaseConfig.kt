package com.example.data.remote.supabase

import com.example.BuildConfig

object SupabaseConfig {
    val projectUrl: String = try {
        BuildConfig.SUPABASE_URL.trimEnd('/')
    } catch (e: Throwable) {
        "https://your-project.supabase.co"
    }

    val anonKey: String = try {
        BuildConfig.SUPABASE_ANON_KEY
    } catch (e: Throwable) {
        "your-anon-key"
    }

    /**
     * Checks if Supabase has been configured with real project credentials.
     * When placeholders are present or network is absent, the app operates in
     * resilient offline/local database mode without throwing unhandled exceptions.
     */
    fun isLiveBackendConfigured(): Boolean {
        return projectUrl.startsWith("https://") &&
                !projectUrl.contains("your-project") &&
                anonKey.isNotBlank() &&
                !anonKey.contains("your-anon-key")
    }
}
