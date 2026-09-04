package com.example.data.engine

import com.example.data.model.DifficultyLevel
import com.example.data.model.ExamAttempt
import com.example.data.model.MockExam
import com.example.data.model.PracticeConfig
import com.example.data.model.PracticeResult
import com.example.data.model.Question
import com.example.data.model.QuestionPaletteState
import com.example.data.model.QuestionStatus
import java.util.UUID

object ExamEngine {

    /**
     * Filters student-visible questions. ONLY APPROVED questions are available to students.
     */
    fun filterApprovedQuestions(
        questions: List<Question>,
        category: String = "ALL",
        subject: String = "ALL",
        topic: String = "ALL",
        difficulty: String = "ALL",
        searchQuery: String = "",
        bookmarkedOnly: Boolean = false
    ): List<Question> {
        return questions.filter { q ->
            val isApproved = q.questionStatus == QuestionStatus.APPROVED
            val matchesCategory = category == "ALL" || q.examCategory.equals(category, ignoreCase = true)
            val matchesSubject = subject == "ALL" || q.subject.equals(subject, ignoreCase = true)
            val matchesTopic = topic == "ALL" || q.topic.equals(topic, ignoreCase = true)
            val matchesDifficulty = difficulty == "ALL" || q.difficulty.name.equals(difficulty, ignoreCase = true)
            val matchesBookmark = !bookmarkedOnly || q.isBookmarked
            val matchesSearch = searchQuery.isBlank() ||
                    q.questionBn.contains(searchQuery, ignoreCase = true) ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    q.tags.any { it.contains(searchQuery, ignoreCase = true) }

            isApproved && matchesCategory && matchesSubject && matchesTopic && matchesDifficulty && matchesBookmark && matchesSearch
        }
    }

    /**
     * Paginate list of questions.
     */
    fun <T> paginate(items: List<T>, pageIndex: Int, pageSize: Int = 10): Pair<List<T>, Int> {
        if (items.isEmpty()) return Pair(emptyList(), 0)
        val totalPages = (items.size + pageSize - 1) / pageSize
        val safePage = pageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        val startIndex = safePage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(items.size)
        return Pair(items.subList(startIndex, endIndex), totalPages)
    }

    /**
     * Prepares questions for a custom practice session.
     */
    fun createPracticeSession(
        allQuestions: List<Question>,
        config: PracticeConfig
    ): List<Question> {
        val eligible = filterApprovedQuestions(
            questions = allQuestions,
            category = config.examCategory,
            subject = config.subject,
            topic = config.topic,
            difficulty = config.difficulty
        )
        return if (eligible.size <= config.questionCount) {
            eligible.shuffled()
        } else {
            eligible.shuffled().take(config.questionCount)
        }
    }

    /**
     * Evaluates a completed practice session.
     */
    fun evaluatePracticeSession(
        questions: List<Question>,
        selectedAnswers: Map<Int, Int>, // question index -> selected option index
        timeSpentSeconds: Int
    ): PracticeResult {
        var correct = 0
        var wrong = 0
        var skipped = 0
        val subjectStats = mutableMapOf<String, Pair<Int, Int>>() // subject -> (correctCount, totalCount)

        questions.forEachIndexed { index, question ->
            val userOption = selectedAnswers[index]
            val isCorrect = userOption != null && userOption == question.correctIndex
            val isAnswered = userOption != null

            if (!isAnswered) {
                skipped++
            } else if (isCorrect) {
                correct++
            } else {
                wrong++
            }

            val currentSubj = subjectStats.getOrDefault(question.subject, Pair(0, 0))
            val newCorrect = if (isCorrect) currentSubj.first + 1 else currentSubj.first
            val newTotal = currentSubj.second + 1
            subjectStats[question.subject] = Pair(newCorrect, newTotal)
        }

        val total = questions.size
        val accuracy = if (correct + wrong > 0) ((correct.toFloat() / (correct + wrong)) * 100).toInt() else 0

        return PracticeResult(
            totalQuestions = total,
            correctCount = correct,
            wrongCount = wrong,
            skippedCount = skipped,
            accuracyPercent = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            subjectBreakdown = subjectStats
        )
    }

    /**
     * Determines question state for the Question Palette in Mock Exams.
     */
    fun getPaletteState(
        index: Int,
        currentIndex: Int,
        selectedAnswers: Map<Int, Int>,
        markedForReview: Set<Int>
    ): QuestionPaletteState {
        return when {
            index == currentIndex -> QuestionPaletteState.CURRENT
            markedForReview.contains(index) -> QuestionPaletteState.FLAGGED
            selectedAnswers.containsKey(index) -> QuestionPaletteState.ANSWERED
            else -> QuestionPaletteState.UNANSWERED
        }
    }

    /**
     * Evaluates a Mock Exam attempt with accurate negative marking and timing.
     */
    fun evaluateMockExam(
        exam: MockExam,
        questions: List<Question>,
        selectedAnswers: Map<Int, Int>,
        timeSpentSeconds: Int,
        userId: String = "user_default_1",
        examTitleOverride: String? = null
    ): ExamAttempt {
        var correct = 0
        var wrong = 0
        var skipped = 0

        questions.forEachIndexed { index, question ->
            val answer = selectedAnswers[index]
            when {
                answer == null -> skipped++
                answer == question.correctIndex -> correct++
                else -> wrong++
            }
        }

        val rawScore = (correct * 1.0f) - (wrong * exam.negativeMarkPerWrong)
        val score = rawScore.coerceAtLeast(0f)
        val accuracy = if (correct + wrong > 0) {
            ((correct.toFloat() / (correct + wrong)) * 100).toInt()
        } else {
            0
        }

        return ExamAttempt(
            id = "attempt_${UUID.randomUUID().toString().take(8)}",
            examId = exam.id,
            examTitle = examTitleOverride ?: exam.titleBn,
            score = score,
            totalQuestions = questions.size,
            correctCount = correct,
            wrongCount = wrong,
            skippedCount = skipped,
            accuracyPercent = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
    }

    /**
     * Calculates difficulty breakdown from an exam attempt or question list.
     */
    fun calculateDifficultyPerformance(
        questions: List<Question>,
        selectedAnswers: Map<Int, Int>
    ): Map<DifficultyLevel, Pair<Int, Int>> {
        val result = mutableMapOf<DifficultyLevel, Pair<Int, Int>>()
        DifficultyLevel.values().forEach { diff ->
            result[diff] = Pair(0, 0)
        }

        questions.forEachIndexed { index, question ->
            val answer = selectedAnswers[index]
            val isCorrect = answer != null && answer == question.correctIndex
            val curr = result[question.difficulty] ?: Pair(0, 0)
            val newCorrect = if (isCorrect) curr.first + 1 else curr.first
            result[question.difficulty] = Pair(newCorrect, curr.second + 1)
        }
        return result
    }
}
