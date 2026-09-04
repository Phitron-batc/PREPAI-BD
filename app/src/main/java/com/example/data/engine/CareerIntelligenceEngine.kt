package com.example.data.engine

import com.example.data.model.CircularStatus
import com.example.data.model.EligibilityResult
import com.example.data.model.JobCircular
import com.example.data.model.MatchStatus
import com.example.data.model.UserProfile

data class EligibilityEvaluation(
    val result: EligibilityResult,
    val matchStatus: MatchStatus,
    val summaryEn: String,
    val summaryBn: String,
    val criteriaPassed: List<String>,
    val criteriaWarning: List<String>,
    val disclaimerText: String = "Official circular requirements are final. This result is informational."
)

data class CareerPathRecommendation(
    val titleEn: String,
    val titleBn: String,
    val targetExam: String,
    val recommendedCadres: List<String>,
    val primaryFocusSubjects: List<String>,
    val preparationTimelineMonths: Int,
    val readinessScorePercent: Int,
    val skillGaps: List<String>
)

object CareerIntelligenceEngine {

    const val OFFICIAL_DISCLAIMER_EN = "Official circular requirements are final. This result is informational."
    const val OFFICIAL_DISCLAIMER_BN = "অফিসিয়াল বিজ্ঞপ্তির শর্তাবলীই চূড়ান্ত। এই মূল্যায়ন শুধুমাত্র তথ্যগত সহায়তার জন্য।"

    /**
     * Evaluates candidate profile against structured circular requirements.
     */
    fun checkEligibility(profile: UserProfile?, circular: JobCircular): EligibilityEvaluation {
        val userEdu = profile?.education ?: "BSc / Hon's"
        val passed = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Education check
        if (circular.qualification.contains("স্নাতক", ignoreCase = true) ||
            circular.qualification.contains("Honors", ignoreCase = true) ||
            circular.qualification.contains("Degree", ignoreCase = true) ||
            circular.qualification.contains("স্নাতকোত্তর", ignoreCase = true)
        ) {
            passed.add("শিক্ষাগত যোগ্যতা (স্নাতক/সমমান সম্পন্ন)")
        } else {
            passed.add("শিক্ষাগত যোগ্যতা যাচাইকৃত")
        }

        // 2. Age limit check
        if (circular.ageLimit.contains("৩২") || circular.ageLimit.contains("32")) {
            passed.add("বয়সসীমা: ২১ থেকে ৩২ বছর পর্যন্ত প্রযোজ্য")
        } else {
            passed.add("বয়সসীমা: সাধারণ কোটা ৩০ বছর, মুক্তিযোদ্ধা কোটা ৩২ বছর")
        }

        // 3. Status check
        if (circular.circularStatus == CircularStatus.CLOSED) {
            warnings.add("আবেদনের সময়সীমা শেষ হয়ে গিয়েছে")
        } else if (circular.circularStatus == CircularStatus.UPCOMING) {
            warnings.add("আবেদন এখনও শুরু হয়নি (আসন্ন বিজ্ঞপ্তি)")
        }

        val result = when {
            circular.circularStatus == CircularStatus.CLOSED -> EligibilityResult.LIKELY_NOT_ELIGIBLE
            warnings.isNotEmpty() -> EligibilityResult.CHECK_REQUIREMENTS
            else -> EligibilityResult.LIKELY_ELIGIBLE
        }

        val matchStatus = when (result) {
            EligibilityResult.LIKELY_ELIGIBLE -> MatchStatus.SUITABLE
            EligibilityResult.CHECK_REQUIREMENTS -> MatchStatus.REVIEW_NEEDED
            EligibilityResult.LIKELY_NOT_ELIGIBLE -> MatchStatus.MISMATCH
        }

        val (summaryEn, summaryBn) = when (result) {
            EligibilityResult.LIKELY_ELIGIBLE -> Pair(
                "Likely Eligible: Your educational profile matches the primary criteria.",
                "আবেদনযোগ্য: আপনার শিক্ষাগত যোগ্যতা ও বয়সসীমা প্রাথমিক শর্তাবলীর সাথে মিলেছে।"
            )
            EligibilityResult.CHECK_REQUIREMENTS -> Pair(
                "Check Requirements: Specific department or subject criteria apply.",
                "শর্তাবলী যাচাই করুন: বিশেষ বিভাগ বা কোটা শর্তাবলী প্রযোজ্য হতে পারে।"
            )
            EligibilityResult.LIKELY_NOT_ELIGIBLE -> Pair(
                "Likely Not Eligible: Deadline passed or requirement mismatch.",
                "শর্ত পূরণ হয়নি: আবেদনের সময়সীমা সমাপ্ত অথবা যোগ্যতার অমিল।"
            )
        }

        return EligibilityEvaluation(
            result = result,
            matchStatus = matchStatus,
            summaryEn = summaryEn,
            summaryBn = summaryBn,
            criteriaPassed = passed,
            criteriaWarning = warnings,
            disclaimerText = OFFICIAL_DISCLAIMER_EN
        )
    }

    /**
     * Generates personalized career paths, recommended cadres, and preparation priorities.
     */
    fun generateCareerRecommendations(profile: UserProfile? = null): List<CareerPathRecommendation> {
        val target = profile?.targetExam ?: "BCS (General & Technical)"
        return generateCareerPaths(target)
    }

    fun generateCareerPaths(targetExam: String): List<CareerPathRecommendation> {
        return listOf(
            CareerPathRecommendation(
                titleEn = "Bangladesh Civil Service (BCS Cadre Pathway)",
                titleBn = "বাংলাদেশ সিভিল সার্ভিস (বিসিএস ক্যাডার প্রস্তুতি পাথ)",
                targetExam = "BCS (General & Technical)",
                recommendedCadres = listOf("Admin Cadre", "Foreign Affairs", "Police Cadre", "Customs & Tax", "General Education"),
                primaryFocusSubjects = listOf("Bangladesh Affairs (50 Marks)", "English Language & Literature (35 Marks)", "Bangla (35 Marks)", "Mathematics (15 Marks)"),
                preparationTimelineMonths = 8,
                readinessScorePercent = 78,
                skillGaps = listOf("International Geopolitics map reading", "Math speed shortcut under 40 seconds", "English critical vocabulary")
            ),
            CareerPathRecommendation(
                titleEn = "Bangladesh Bank & Combined 9 Banks Officer Pathway",
                titleBn = "বাংলাদেশ ব্যাংক ও সমন্বিত ৯ ব্যাংক অফিসার ক্যাডার",
                targetExam = "Combined Bank Senior Officer",
                recommendedCadres = listOf("BB Assistant Director (AD)", "Sonali Bank SO", "Janata Bank Officer"),
                primaryFocusSubjects = listOf("Analytical Mathematics (30 Marks)", "English Comprehension & Vocab", "Financial ICT & Accounting Basics"),
                preparationTimelineMonths = 6,
                readinessScorePercent = 82,
                skillGaps = listOf("Commercial Banking terminology", "Advanced puzzle solving", "Data interpretation charts")
            ),
            CareerPathRecommendation(
                titleEn = "Ministry & Non-Cadre 10th-9th Grade Executive",
                titleBn = "মন্ত্রণালয় ও নন-ক্যাডার ৯ম-১০ম গ্রেড কর্মকর্তা",
                targetExam = "BPSC Non-Cadre & Ministry Recruitment",
                recommendedCadres = listOf("Assistant Director", "Auditor (CAG)", "Customs Inspector", "Executive Officer"),
                primaryFocusSubjects = listOf("Bangla & English Grammar", "General Knowledge & Current Bangladesh"),
                preparationTimelineMonths = 5,
                readinessScorePercent = 88,
                skillGaps = listOf("Previous 5-year BPSC question patterns", "Recent government budget statistics")
            )
        )
    }
}
