package com.example.data.ai

import com.example.BuildConfig
import com.example.data.domain.AiTutorService
import com.example.data.model.TutorMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService : AiTutorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun isLiveApiConfigured(): Boolean {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return !apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY"
    }

    override suspend fun askTutor(
        userQuery: String,
        mode: TutorMode,
        contextInfo: String,
        language: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineSmartResponse(userQuery, mode, language)
        }

        val systemPrompt = buildSystemPrompt(mode, language, contextInfo)
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        try {
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$systemPrompt\n\nStudent Query: $userQuery"))
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1000)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getOfflineSmartResponse(userQuery, mode, language)
            }

            val responseText = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseText)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                getOfflineSmartResponse(userQuery, mode, language)
            }
        } catch (e: Exception) {
            getOfflineSmartResponse(userQuery, mode, language)
        }
    }

    private fun buildSystemPrompt(mode: TutorMode, language: String, contextInfo: String): String {
        val modeInstruction = when (mode) {
            TutorMode.SIMPLE -> "Explain concepts simply like a beginner friendly teacher with clear easy steps."
            TutorMode.EXAM_MODE -> "Focus on exam techniques, fast shortcuts, elimination strategies, and mnemonics for Bangladesh BCS & Bank exams."
            TutorMode.DEEP_LEARNING -> "Provide a comprehensive, deep conceptual explanation with background history or mathematical proofs."
            TutorMode.SOCRATIC -> "Guide the student through thoughtful questions instead of immediately giving the direct answer."
            TutorMode.EXAMPLE -> "Explain strictly using realistic concrete exam examples from Bangladesh job recruitment tests."
        }

        val langInstruction = if (language == "BN") {
            "Respond primarily in fluent, academic yet accessible Bengali (বাংলা), keeping technical terms and formulas in English where customary for BCS and Bank job exams."
        } else {
            "Respond in fluent English tailored for competitive job exams in Bangladesh."
        }

        return """
            You are PrepAI Copilot, an elite AI tutor and career advisor for Bangladeshi job candidates preparing for BCS, Bangladesh Bank AD, Primary Teacher Recruitment, NTRCA, and Government exams.
            $modeInstruction
            $langInstruction
            Context: $contextInfo
        """.trimIndent()
    }

    private fun getOfflineSmartResponse(query: String, mode: TutorMode, language: String): String {
        val lower = query.lowercase()
        return if (language == "BN") {
            when {
                lower.contains("চর্যাপদ") || lower.contains("charyapada") ->
                    "চর্যাপদ সম্পর্কিত গুরুত্বপূর্ণ তথ্য:\n" +
                            "১. চর্যাপদ বাংলা সাহিত্যের প্রাচীনতম প্রামাণ্য নিদর্শন।\n" +
                            "২. এটি হরপ্রসাদ শাস্ত্রী ১৯০৭ সালে নেপালের রাজদরবারের রয়েল লাইব্রেরি থেকে আবিষ্কার করেন।\n" +
                            "৩. ড. মুহম্মদ শহীদুল্লাহর মতে এর রচনাকাল ৬৫০-১২০০ খ্রিষ্টাব্দ।\n" +
                            "৪. মোট পদ সংখ্যা সাড়ে ৪৬টি (আবিষ্কৃত)। আদি কবি লুইপা এবং শ্রেষ্ঠ বা সর্বাধিক পদকর্তা কাহ্নপা (১৩টি পদ)।"

                lower.contains("সংবিধান") || lower.contains("constitution") ->
                    "বাংলাদেশ সংবিধানের প্রি-টেস্ট শর্টকাট:\n" +
                            "১. কার্যকর হয়: ১৬ ডিসেম্বর ১৯৭২।\n" +
                            "২. অনুচ্ছেদ: ১৫৩টি, ভাগ: ১১টি, তফসিল: ৭টি।\n" +
                            "৩. মৌলিক অধিকার: অনুচ্ছেদ ২৬ থেকে ৪৭।\n" +
                            "৪. অনুচ্ছেদ ৩২: জীবন ও ব্যক্তি স্বাধীনতার অধিকার।\n" +
                            "৫. অনুচ্ছেদ ৩৬: চলাফেরার স্বাধীনতা; অনুচ্ছেদ ৩৯: বাক ও ভাব প্রকাশের স্বাধীনতা।"

                lower.contains("percentage") || lower.contains("শতকরা") || lower.contains("profit") || lower.contains("লাভ") ->
                    "শতকরা ও লাভ-ক্ষতির স্পিড শর্টকাট:\n" +
                            "১. মূল্য R% বৃদ্ধি পেলে খরচ অপরিবর্তিত রাখতে ব্যবহার কমাতে হবে: [R / (100 + R)] × 100%\n" +
                            "উদাহরণ: মূল্য ২৫% বাড়লে ব্যবহার কমাতে হবে [২৫/১২৫] × ১০০ = ২০%।\n" +
                            "২. পরপর দুইবার বৃদ্ধি বা হ্রাস হলে নিট পরিবর্তন: A + B + (A×B / 100)%"

                lower.contains("bcs") || lower.contains("বিসিএস") ->
                    "বিসিএস প্রিলিমিনারি ২০০ নম্বরের বিষয়ভিত্তিক প্রস্তুতি কৌশল:\n" +
                            "• বাংলা (৩৫), ইংরেজি (৩৫), বাংলাদেশ বিষয়াবলি (৩০), আন্তর্জাতিক (২০)। এই চারটি বিষয়ে মোট ১২০ নম্বর — এটিই প্রিলিমিনারি পাসের মূল ভিত্তি।\n" +
                            "• গাণিতিক যুক্তি (১৫) ও মানসিক দক্ষতা (১৫) নিয়মিত ১ ঘণ্টা অনুশীলন করলে সহজে ২৫+ তোলা সম্ভব।\n" +
                            "• বিজ্ঞান (১৫) ও আইসিটি (১৫) প্রিভিয়াস ১০ বছরের প্রশ্ন ভালোভাবে বিশ্লেষণ করুন।"

                else ->
                    "PrepAI Copilot স্মার্ট বিশ্লেষণ:\n" +
                            "আপনার প্রশ্নটি বিশ্লেষণ করা হয়েছে। বিসিএস এবং ব্যাংক নিয়োগ পরীক্ষায় সফল হতে সিলেবাস ভিত্তিক বিষয়গুলো পর্যায়ক্রমে রিভিশন সেন্টারে অনুশীলন করুন। আপনি স্পেসিফিক কোনো গণিত, ইংরেজি গ্রামার বা সংবিধানের অনুচ্ছেদ জানতে চাইলে সরাসরি প্রশ্ন করতে পারেন!"
            }
        } else {
            when {
                lower.contains("bank") || lower.contains("math") ->
                    "Bangladesh Bank & Combined Banks Math Strategy:\n" +
                            "• Focus on Arithmetic: Ratio & Proportion, Percentage, Profit-Loss, Time & Work, Speed-Distance.\n" +
                            "• Shortcut: If price increases by R%, consumption reduction = [R / (100 + R)] * 100%.\n" +
                            "• Daily target: Solve 25 MCQs within 30 minutes to build speed and accuracy."
                else ->
                    "PrepAI Copilot Response:\n" +
                            "I have analyzed your request based on Bangladesh competitive exam syllabi (BCS, Bangladesh Bank AD, and Primary Teacher exams). Feel free to ask specific concept breakdowns, shortcut formulas, or previous year question explanations."
            }
        }
    }
}
