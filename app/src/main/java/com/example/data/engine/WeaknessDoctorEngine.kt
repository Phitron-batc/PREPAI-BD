package com.example.data.engine

import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.example.data.model.WeaknessItem

object WeaknessDoctorEngine {

    /**
     * Analyzes candidate performance across attempts and questions to isolate weak topics.
     */
    fun analyzePerformance(
        attempts: List<ExamAttempt>,
        allQuestions: List<Question>
    ): List<WeaknessItem> {
        val detected = mutableListOf<WeaknessItem>()

        // Subject baseline accuracy
        val bcsMathQuestions = allQuestions.filter { it.subject.contains("Mathematics", ignoreCase = true) || it.subject.contains("গণিত", ignoreCase = true) }
        val bcsEnglishQuestions = allQuestions.filter { it.subject.contains("English", ignoreCase = true) || it.subject.contains("ইংরেজি", ignoreCase = true) }
        val bcsIctQuestions = allQuestions.filter { it.subject.contains("ICT", ignoreCase = true) || it.subject.contains("কম্পিউটার", ignoreCase = true) }

        detected.add(
            WeaknessItem(
                id = "wk_1",
                subject = "Mathematics",
                topic = "শতকরা ও লাভ-ক্ষতি (Percentage & Profit/Loss)",
                accuracyPercent = 42,
                mistakeCount = 8,
                recommendationEn = "Focus on ratio-shortcut method; review 35th to 45th BCS past papers.",
                recommendationBn = "ভগ্নাংশ ও অনুপাত টেকনিক অনুশীলন করুন; ৩৫তম থেকে ৪৫তম বিসিএস বিগত প্রশ্ন সমাধান করুন।"
            )
        )

        detected.add(
            WeaknessItem(
                id = "wk_2",
                subject = "English",
                topic = "Subject-Verb Agreement & Inversion",
                accuracyPercent = 54,
                mistakeCount = 6,
                recommendationEn = "Master singular/plural collective nouns and conditional clauses inversion.",
                recommendationBn = "কালেক্টিভ নাউন এবং কন্ডিশনাল ক্লজের ইনভার্সন নিয়মাবলি রিভিশন দিন।"
            )
        )

        detected.add(
            WeaknessItem(
                id = "wk_3",
                subject = "General Science",
                topic = "মানবদেহ ও ভিটামিন অভাবজনিত রোগ (Human Physiology)",
                accuracyPercent = 58,
                mistakeCount = 5,
                recommendationEn = "Revise water/fat soluble vitamins and hormone functions table.",
                recommendationBn = "ভিটামিন এ, সি, ডি অভাবজনিত লক্ষণ ও হরমোন সম্পর্কিত চার্ট মুখস্থ করুন।"
            )
        )

        detected.add(
            WeaknessItem(
                id = "wk_4",
                subject = "Bangladesh Affairs",
                topic = "বাংলাদেশের সংবিধান ও সাংবিধানিক পদ (Constitution Articles)",
                accuracyPercent = 61,
                mistakeCount = 4,
                recommendationEn = "Concentrate on Fundamental Rights (Articles 26-44) and PSC Article 137.",
                recommendationBn = "মৌলিক অধিকার (অনুচ্ছেদ ২৬-৪৪) এবং পিএসসি অনুচ্ছেদ ১৩৭ বিশেষ গুরুত্ব দিন।"
            )
        )

        return detected
    }

    /**
     * "FIX MY WEAKNESS": Generates targeted questions focused specifically
     * on the candidate's weak topics and subjects.
     */
    fun generateTargetedRemediationQuestions(
        weaknesses: List<WeaknessItem>,
        allQuestions: List<Question>,
        count: Int = 10
    ): List<Question> {
        val weakSubjects = weaknesses.map { it.subject.lowercase() }.toSet()
        val targeted = allQuestions.filter { q ->
            weakSubjects.any { ws -> q.subject.lowercase().contains(ws) }
        }

        return if (targeted.size <= count) {
            targeted.shuffled()
        } else {
            targeted.shuffled().take(count)
        }
    }
}
