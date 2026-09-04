package com.example.data.remote.supabase

import com.example.data.model.DifficultyLevel
import com.example.data.model.ExamAttempt
import com.example.data.model.JobCircular
import com.example.data.model.MatchStatus
import com.example.data.model.MockExam
import com.example.data.model.Question
import com.example.data.model.ReviewStatus
import com.example.data.model.StudyTask
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRestService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchQuestions(accessToken: String?): List<Question>? = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext null
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/questions?status=eq.APPROVED&select=*"
            val reqBuilder = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .get()

            val response = client.newCall(reqBuilder.build()).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val jsonArray = JSONArray(body)
            val result = mutableListOf<Question>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val optEnArr = item.optJSONArray("options_en") ?: JSONArray()
                val optBnArr = item.optJSONArray("options_bn") ?: JSONArray()
                val optsEn = mutableListOf<String>()
                val optsBn = mutableListOf<String>()
                for (j in 0 until optEnArr.length()) optsEn.add(optEnArr.getString(j))
                for (j in 0 until optBnArr.length()) optsBn.add(optBnArr.getString(j))

                val diffStr = item.optString("difficulty", "MEDIUM")
                val diff = try { DifficultyLevel.valueOf(diffStr) } catch (e: Exception) { DifficultyLevel.MEDIUM }

                result.add(
                    Question(
                        id = item.getString("id"),
                        examCategory = item.optString("exam_category", "BCS"),
                        subject = item.optString("subject", "Bangla"),
                        topic = item.optString("topic", ""),
                        questionEn = item.optString("question_en", ""),
                        questionBn = item.optString("question_bn", ""),
                        optionsEn = optsEn,
                        optionsBn = optsBn,
                        correctIndex = item.optInt("correct_index", 0),
                        explanationEn = item.optString("explanation_en", ""),
                        explanationBn = item.optString("explanation_bn", ""),
                        aiShortcut = item.optString("ai_shortcut", ""),
                        difficulty = diff,
                        previousYearTag = item.optString("previous_year_tag", ""),
                        isBookmarked = false,
                        status = ReviewStatus.APPROVED
                    )
                )
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertQuestion(question: Question, accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext false
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/questions"
            val payload = JSONObject().apply {
                put("id", question.id)
                put("exam_category", question.examCategory)
                put("subject", question.subject)
                put("topic", question.topic)
                put("question_en", question.questionEn)
                put("question_bn", question.questionBn)
                put("options_en", JSONArray(question.optionsEn))
                put("options_bn", JSONArray(question.optionsBn))
                put("correct_index", question.correctIndex)
                put("explanation_en", question.explanationEn)
                put("explanation_bn", question.explanationBn)
                put("ai_shortcut", question.aiShortcut)
                put("difficulty", question.difficulty.name)
                put("previous_year_tag", question.previousYearTag)
                put("status", question.status.name)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateQuestion(question: Question, accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext false
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/questions?id=eq.${question.id}"
            val payload = JSONObject().apply {
                put("exam_category", question.examCategory)
                put("subject", question.subject)
                put("topic", question.topic)
                put("question_en", question.questionEn)
                put("question_bn", question.questionBn)
                put("options_en", JSONArray(question.optionsEn))
                put("options_bn", JSONArray(question.optionsBn))
                put("correct_index", question.correctIndex)
                put("explanation_en", question.explanationEn)
                put("explanation_bn", question.explanationBn)
                put("ai_shortcut", question.aiShortcut)
                put("difficulty", question.difficulty.name)
                put("status", question.status.name)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .patch(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteQuestion(questionId: String, accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext false
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/questions?id=eq.$questionId"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .delete()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncExamAttempt(attempt: ExamAttempt, accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext false
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/exam_attempts"
            val payload = JSONObject().apply {
                put("id", attempt.id)
                put("exam_id", attempt.examId)
                put("exam_title", attempt.examTitle)
                put("score", attempt.score)
                put("total_questions", attempt.totalQuestions)
                put("correct_count", attempt.correctCount)
                put("wrong_count", attempt.wrongCount)
                put("skipped_count", attempt.skippedCount)
                put("accuracy_percent", attempt.accuracyPercent)
                put("time_spent_seconds", attempt.timeSpentSeconds)
                put("timestamp", attempt.timestamp)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncUserProfile(profile: UserProfile, accessToken: String?): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isLiveBackendConfigured()) return@withContext false
        try {
            val url = "${SupabaseConfig.projectUrl}/rest/v1/profiles?id=eq.${profile.id}"
            val payload = JSONObject().apply {
                put("full_name", profile.fullName)
                put("target_exam", profile.targetExam)
                put("daily_study_hours", profile.dailyStudyHours)
                put("streak_days", profile.streakDays)
                put("xp_points", profile.xpPoints)
                put("level", profile.level)
                put("readiness_score", profile.readinessScore)
                put("preferred_language", profile.preferredLanguage)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.anonKey)
                .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.anonKey}")
                .addHeader("Content-Type", "application/json")
                .patch(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
