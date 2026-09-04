package com.example.data.domain

import com.example.data.model.DifficultyLevel
import com.example.data.model.MatchStatus
import com.example.data.model.ReviewStatus
import com.example.data.model.TutorMode
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// DOMAIN MODELS
// ---------------------------------------------------------------------------

/**
 * Domain representation of an authenticated user.
 */
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole = UserRole.STUDENT,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Detailed profile and preparation metrics for a student.
 */
data class StudentProfile(
    val userId: String,
    val fullName: String,
    val email: String,
    val education: String,
    val university: String,
    val graduationYear: String,
    val targetExam: String,
    val targetExamDate: String,
    val dailyStudyHours: Int,
    val streakDays: Int,
    val xpPoints: Int,
    val level: Int,
    val readinessScore: Int,
    val preferredLanguage: String = "BN",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
)

/**
 * Single multiple-choice option.
 */
data class Option(
    val index: Int,
    val textEn: String,
    val textBn: String
)

/**
 * Subject entity within the competitive exam syllabus.
 */
data class Subject(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val category: String,
    val totalQuestionsCount: Int = 0,
    val iconName: String = "menu_book"
)

/**
 * Specific topic under a subject.
 */
data class Topic(
    val id: String,
    val subjectId: String,
    val nameEn: String,
    val nameBn: String,
    val questionCount: Int = 0,
    val weightPercent: Int = 10
)

/**
 * Supported competitive exam categories in Bangladesh.
 */
enum class ExamCategory(val code: String, val titleEn: String, val titleBn: String) {
    BCS("BCS", "BCS Preliminary", "বিসিএস প্রিলিমিনারি"),
    BANK("BANK", "Bank Recruitment (AD/Officer)", "বাংলাদেশ ব্যাংক ও কম্বাইন্ড ব্যাংক"),
    PRIMARY("PRIMARY", "Primary Assistant Teacher", "প্রাথমিক শিক্ষক নিয়োগ"),
    NTRCA("NTRCA", "NTRCA Teachers Registration", "শিক্ষক নিবন্ধন (NTRCA)"),
    GOVT("GOVT", "Govt Ministries (Non-Cadre)", "মন্ত্রণালয় ও অন্যান্য সরকারি চাকরি")
}

/**
 * Question entity representing a standard 4-option MCQ with bilingual content and AI explanation.
 */
data class DomainQuestion(
    val id: String,
    val category: ExamCategory,
    val subject: String,
    val topic: String,
    val questionEn: String,
    val questionBn: String,
    val optionsEn: List<String>,
    val optionsBn: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationBn: String,
    val aiShortcut: String,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val previousYearTag: String = "",
    val isBookmarked: Boolean = false
)

/**
 * Association of question with an exam session.
 */
data class ExamQuestion(
    val questionId: String,
    val marks: Float = 1.0f,
    val negativeMarks: Float = 0.5f
)

/**
 * Mock exam specification.
 */
data class DomainMockExam(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val category: ExamCategory,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarkPerWrong: Float = 0.5f,
    val questionIds: List<String>,
    val isAdaptive: Boolean = false
)

/**
 * Record of a candidate's completed or active exam attempt.
 */
data class DomainExamAttempt(
    val id: String,
    val examId: String,
    val examTitle: String,
    val score: Float,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val accuracyPercent: Int,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Detailed score card and performance breakdown of an exam attempt.
 */
data class ExamResult(
    val attemptId: String,
    val examTitle: String,
    val totalMarks: Float,
    val earnedScore: Float,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val unansweredCount: Int,
    val negativeDeduction: Float,
    val accuracyPercent: Int,
    val percentileRanking: Float,
    val weakTopics: List<String>
)

/**
 * Student bookmark reference for quick review.
 */
data class Bookmark(
    val id: String,
    val userId: String,
    val questionId: String,
    val savedAt: Long = System.currentTimeMillis()
)

/**
 * Structured syllabus roadmap & daily study planning.
 */
data class StudyPlan(
    val id: String,
    val userId: String,
    val targetExam: String,
    val dailyGoalMinutes: Int,
    val weeklyMilestones: List<String>,
    val completedMilestones: List<String>
)

/**
 * Message in an AI conversation thread.
 */
data class AIConversation(
    val id: String,
    val userId: String,
    val mode: TutorMode,
    val query: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Commercial subscription status for PrepAI BD candidates.
 */
enum class SubscriptionTier(val titleEn: String, val titleBn: String, val priceBdt: Int) {
    FREE("Free Plan", "ফ্রি প্ল্যান", 0),
    PRO("BCS / Bank Pro", "বিসিএস ও ব্যাংক প্রো", 499),
    PREMIUM_LIFETIME("Executive All-Access", "এক্সিকিউটিভ অল-অ্যাক্সেস", 1299)
}

data class Subscription(
    val userId: String,
    val tier: SubscriptionTier,
    val isActive: Boolean,
    val expiryDate: String,
    val paymentReference: String? = null
)

// ---------------------------------------------------------------------------
// REPOSITORY ABSTRACTION (Phase 2 Supabase / Remote Database Ready)
// ---------------------------------------------------------------------------

interface IPrepAiRepository {
    fun getUserProfile(userId: String): Flow<StudentProfile?>
    suspend fun saveUserProfile(profile: StudentProfile)
    fun getQuestions(category: String?, subject: String?): Flow<List<DomainQuestion>>
    suspend fun addQuestion(question: DomainQuestion)
    suspend fun updateQuestion(question: DomainQuestion)
    suspend fun deleteQuestion(questionId: String)
    suspend fun toggleBookmark(questionId: String, currentStatus: Boolean)
    fun getMockExams(): List<DomainMockExam>
    suspend fun recordExamAttempt(attempt: DomainExamAttempt)
}

// ---------------------------------------------------------------------------
// AI TUTOR SERVICE ABSTRACTION
// ---------------------------------------------------------------------------

interface AiTutorService {
    suspend fun askTutor(
        userQuery: String,
        mode: TutorMode,
        contextInfo: String = "",
        language: String = "BN"
    ): String

    fun isLiveApiConfigured(): Boolean
}
