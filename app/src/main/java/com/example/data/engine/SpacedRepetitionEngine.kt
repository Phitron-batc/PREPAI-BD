package com.example.data.engine

import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.example.data.model.SpacedRepetitionItem
import com.example.data.model.SpacedStatus
import java.util.concurrent.TimeUnit

object SpacedRepetitionEngine {

    val INTERVALS_DAYS = listOf(1, 3, 7, 15)

    /**
     * Generates spaced repetition review items from candidate's bookmarks,
     * mistaken questions, and high-yield topics.
     */
    fun generateInitialSpacedItems(
        allQuestions: List<Question>,
        recentAttempts: List<ExamAttempt>
    ): List<SpacedRepetitionItem> {
        val now = System.currentTimeMillis()
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)
        val items = mutableListOf<SpacedRepetitionItem>()

        // 1. Prioritize bookmarked questions
        val bookmarked = allQuestions.filter { it.isBookmarked }
        bookmarked.forEachIndexed { idx, q ->
            val interval = INTERVALS_DAYS[idx % INTERVALS_DAYS.size]
            val reviewTime = now + (interval * oneDayMillis) - (idx * 3600000L * 4) // distribute spread
            items.add(
                SpacedRepetitionItem(
                    id = "sr_bm_${q.id}",
                    questionId = q.id,
                    questionTitle = q.questionBn.ifBlank { q.questionEn },
                    subject = q.subject,
                    topic = q.topic,
                    intervalDay = interval,
                    nextReviewDate = reviewTime,
                    repetitionCount = 1,
                    easeFactor = 2.5f,
                    status = determineStatus(reviewTime, now)
                )
            )
        }

        // 2. Add high-yield / previous year questions
        val highYield = allQuestions.filter { it.previousYearTag.isNotBlank() && !it.isBookmarked }
        highYield.take(8).forEachIndexed { idx, q ->
            val interval = if (idx < 3) 1 else 3
            val reviewTime = if (idx == 0) now - 3600000L else now + (interval * oneDayMillis)
            items.add(
                SpacedRepetitionItem(
                    id = "sr_hy_${q.id}",
                    questionId = q.id,
                    questionTitle = q.questionBn.ifBlank { q.questionEn },
                    subject = q.subject,
                    topic = q.topic,
                    intervalDay = interval,
                    nextReviewDate = reviewTime,
                    repetitionCount = 0,
                    easeFactor = 2.4f,
                    status = determineStatus(reviewTime, now)
                )
            )
        }

        return items
    }

    /**
     * Computes the current status of a spaced repetition item.
     */
    fun determineStatus(nextReviewDate: Long, currentTime: Long = System.currentTimeMillis()): SpacedStatus {
        val diffHours = (nextReviewDate - currentTime) / (1000 * 60 * 60)
        return when {
            diffHours < -12 -> SpacedStatus.OVERDUE
            diffHours in -12..12 -> SpacedStatus.DUE_TODAY
            else -> SpacedStatus.UPCOMING
        }
    }

    /**
     * Advances an item to the next repetition interval (1 -> 3 -> 7 -> 15).
     */
    fun advanceRepetition(
        item: SpacedRepetitionItem,
        wasCorrect: Boolean
    ): SpacedRepetitionItem {
        val now = System.currentTimeMillis()
        val oneDayMillis = TimeUnit.DAYS.toMillis(1)

        val nextIndex = if (wasCorrect) {
            val currIdx = INTERVALS_DAYS.indexOf(item.intervalDay)
            if (currIdx == -1 || currIdx >= INTERVALS_DAYS.size - 1) {
                INTERVALS_DAYS.size - 1
            } else {
                currIdx + 1
            }
        } else {
            0 // reset to Day 1 interval on mistake
        }

        val nextIntervalDay = INTERVALS_DAYS[nextIndex]
        val nextReviewDate = now + (nextIntervalDay * oneDayMillis)
        val newRepCount = if (wasCorrect) item.repetitionCount + 1 else 0
        val newEaseFactor = if (wasCorrect) {
            (item.easeFactor + 0.1f).coerceAtMost(3.0f)
        } else {
            (item.easeFactor - 0.2f).coerceAtLeast(1.3f)
        }

        return item.copy(
            intervalDay = nextIntervalDay,
            nextReviewDate = nextReviewDate,
            repetitionCount = newRepCount,
            easeFactor = newEaseFactor,
            status = determineStatus(nextReviewDate, now)
        )
    }

    /**
     * Filters items by status.
     */
    fun filterByStatus(
        items: List<SpacedRepetitionItem>,
        status: SpacedStatus
    ): List<SpacedRepetitionItem> {
        val now = System.currentTimeMillis()
        return items.filter { determineStatus(it.nextReviewDate, now) == status }
    }
}
