package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

enum class UserRole {
    STUDENT,
    ADMIN,
    CONTENT_MANAGER,
    INSTRUCTOR,
    SUPER_ADMIN
}

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

enum class MatchStatus {
    SUITABLE,
    REVIEW_NEEDED,
    MISMATCH
}

enum class QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    ARCHIVED
}

enum class QuestionPaletteState {
    ANSWERED,
    UNANSWERED,
    CURRENT,
    FLAGGED
}

enum class DocumentStatus {
    UPLOADED,
    PROCESSING,
    READY,
    FAILED
}

enum class CircularStatus {
    UPCOMING,
    ACTIVE,
    CLOSED,
    ARCHIVED
}

enum class EligibilityResult {
    LIKELY_ELIGIBLE,
    CHECK_REQUIREMENTS,
    LIKELY_NOT_ELIGIBLE
}

enum class SpacedStatus {
    DUE_TODAY,
    UPCOMING,
    OVERDUE
}

enum class NotificationType {
    CIRCULAR_ALERT,
    DAILY_STUDY_REMINDER,
    EXAM_REMINDER,
    REVISION_REMINDER,
    WEAKNESS_ALERT,
    SUBSCRIPTION_REMINDER
}

enum class PaymentProvider {
    BKASH,
    NAGAD,
    SSLCOMMERZ
}

enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEEDS_EDIT
}

enum class TutorMode {
    SIMPLE,
    EXAM_MODE,
    DEEP_LEARNING,
    SOCRATIC,
    EXAMPLE
}

enum class SubscriptionTier {
    FREE,
    BASIC,
    PREMIUM
}

data class SubscriptionPlan(
    val tier: SubscriptionTier,
    val nameEn: String,
    val nameBn: String,
    val priceBdt: Int,
    val durationMonths: Int,
    val featuresEn: List<String>,
    val featuresBn: List<String>
)

data class UserSession(
    val accessToken: String,
    val refreshToken: String = "",
    val userId: String,
    val email: String,
    val role: UserRole = UserRole.STUDENT,
    val expiresAt: Long = 0L
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String, val code: String? = null) : AuthState()
}

@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: String = "user_default_1",
    val fullName: String = "Tanvir Ahmed",
    val email: String = "tanvir.bcs@prepaibd.com",
    val role: UserRole = UserRole.STUDENT,
    val education: String = "B.Sc in Computer Science & Engineering",
    val university: String = "University of Dhaka",
    val graduationYear: String = "2024",
    val targetExam: String = "46th BCS & Bangladesh Bank AD",
    val targetExamDate: String = "Nov 2026",
    val dailyStudyHours: Int = 4,
    val streakDays: Int = 12,
    val xpPoints: Int = 2450,
    val level: Int = 5,
    val readinessScore: Int = 78,
    val preferredLanguage: String = "BN", // "BN" or "EN"
    val phone: String = "",
    val avatarUrl: String = "",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
) {
    val isPro: Boolean
        get() = subscriptionTier == SubscriptionTier.PREMIUM || subscriptionTier == SubscriptionTier.BASIC
}

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String,
    val examCategory: String, // "BCS", "BANK", "PRIMARY", "NTRCA", "GOVT"
    val subject: String, // "Bangla", "English", "Mathematics", "Bangladesh Affairs", "International Affairs", "General Science", "ICT"
    val topic: String,
    val subtopic: String = "",
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
    val isBookmarked: Boolean = false,
    val isFromAiReview: Boolean = false,
    val status: ReviewStatus = ReviewStatus.APPROVED,
    val questionStatus: QuestionStatus = QuestionStatus.APPROVED,
    val tags: List<String> = emptyList(),
    val sourceMetadata: String = "Verified PSC / Official Board",
    val createdBy: String = ""
)

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleBn: String,
    val subject: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val isOverdue: Boolean = false,
    val priority: String = "HIGH",
    val userId: String = "user_default_1"
)

@Entity(tableName = "weakness_items")
data class WeaknessItem(
    @PrimaryKey val id: String,
    val subject: String,
    val topic: String,
    val accuracyPercent: Int,
    val mistakeCount: Int,
    val recommendationEn: String,
    val recommendationBn: String,
    val userId: String = "user_default_1"
)

@Entity(tableName = "job_circulars")
data class JobCircular(
    @PrimaryKey val id: String,
    val organizationEn: String,
    val organizationBn: String,
    val jobTitleEn: String,
    val jobTitleBn: String,
    val category: String,
    val circularNumber: String = "",
    val vacancyCount: Int,
    val publicationDate: String = "",
    val applicationStart: String = "",
    val deadline: String,
    val qualification: String,
    val ageLimit: String,
    val salary: String = "",
    val officialSourceRef: String = "Official Gazette / Ministry Notice",
    val applyUrl: String,
    val matchStatus: MatchStatus = MatchStatus.SUITABLE,
    val matchNotes: String = "",
    val circularStatus: CircularStatus = CircularStatus.ACTIVE,
    val isActive: Boolean = true
)

@Entity(tableName = "mock_exams")
data class MockExam(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleBn: String,
    val examCategory: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarkPerWrong: Float = 0.5f,
    val questionIds: List<String>,
    val isAdaptive: Boolean = false,
    val status: ReviewStatus = ReviewStatus.APPROVED
)

@Entity(tableName = "exam_attempts")
data class ExamAttempt(
    @PrimaryKey val id: String,
    val examId: String,
    val examTitle: String,
    val score: Float,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val accuracyPercent: Int,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "user_default_1"
)


data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val messageText: String,
    val mode: TutorMode = TutorMode.SIMPLE,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiReviewQueueItem(
    val id: String,
    val sourceDocument: String,
    val chapter: String,
    val question: Question,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val reviewerNote: String = "",
    val generatedDate: String = "Today, 09:30 AM"
) {
    val reviewNotes: String
        get() = reviewerNote
}

data class DocumentInfo(
    val id: String,
    val title: String,
    val documentType: String = "Syllabus / Guide", // PDF, Notes, Syllabus, Official circulars, Licensed educational materials
    val category: String,
    val fileSize: String,
    val pageCount: Int,
    val chunkCount: Int,
    val uploadDate: String = "04 Sep 2026",
    val processingStatus: DocumentStatus = DocumentStatus.READY,
    val vectorStatus: String = "Indexed (RAG Ready)", // "Indexed (RAG Ready)", "Processing", "Queued"
    val sourceMetadata: String = "Ministry / PSC Publication",
    val copyrightCleared: Boolean = true
)

data class SpacedRepetitionItem(
    val id: String,
    val questionId: String,
    val questionTitle: String,
    val subject: String,
    val topic: String,
    val intervalDay: Int, // 1, 3, 7, 15
    val nextReviewDate: Long,
    val repetitionCount: Int,
    val easeFactor: Float = 2.5f,
    val status: SpacedStatus
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionUrl: String = ""
)

sealed class PaymentResult {
    data class Success(val transactionId: String, val amount: Int, val plan: SubscriptionPlan) : PaymentResult()
    data class Failed(val reason: String) : PaymentResult()
    data class RequiresMerchantSetup(val provider: PaymentProvider, val message: String) : PaymentResult()
}

data class PracticeConfig(
    val examCategory: String = "ALL",
    val subject: String = "ALL",
    val topic: String = "ALL",
    val difficulty: String = "ALL",
    val questionCount: Int = 10
) {
    val category: String
        get() = examCategory
}

data class PracticeResult(
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val accuracyPercent: Int,
    val timeSpentSeconds: Int,
    val subjectBreakdown: Map<String, Pair<Int, Int>> = emptyMap() // subject -> (correct, total)
) {
    val score: Float
        get() = (correctCount.toFloat() - (wrongCount * 0.5f)).coerceAtLeast(0f)
}

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
    private val jsonAdapter = moshi.adapter<List<String>>(listStringType)

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return jsonAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return try {
            jsonAdapter.fromJson(data) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
