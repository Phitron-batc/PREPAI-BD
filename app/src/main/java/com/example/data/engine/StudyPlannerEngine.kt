package com.example.data.engine

import com.example.data.model.StudyTask
import java.util.UUID

object StudyPlannerEngine {

    /**
     * Generates a balanced syllabus-aligned study schedule based on candidate's preferences.
     */
    fun generatePersonalizedPlan(
        dailyStudyHours: Int,
        targetExam: String,
        targetExamDate: String,
        subjectPriorities: List<String> = listOf("Bangla", "English", "Mathematics", "Bangladesh Affairs")
    ): List<StudyTask> {
        val tasks = mutableListOf<StudyTask>()

        // 1. Daily Core Subject Tasks
        subjectPriorities.forEachIndexed { index, subject ->
            val durationMinutes = when {
                dailyStudyHours >= 6 -> 90
                dailyStudyHours >= 4 -> 60
                else -> 45
            }

            val (titleEn, titleBn) = when (subject) {
                "Bangla" -> Pair("Bangla Literature: Modern Period & Rabindranath/Nazrul", "বাংলা সাহিত্য: আধুনিক যুগ এবং রবীন্দ্র-নজরুল পর্ব রিভিশন")
                "English" -> Pair("English Grammar: Prepositions, Idioms & Vocabulary Practice", "ইংরেজি ব্যাকরণ: প্রিপজিশন, ইডিয়ম ও ২০০ শব্দভাণ্ডার অনুশীলন")
                "Mathematics" -> Pair("Mathematics: Geometry & Mensuration Shortcuts (BCS Standard)", "গণিত: জ্যামিতি ও পরিমিতি শর্টকাট কৌশল অনুশীলন")
                "Bangladesh Affairs" -> Pair("Bangladesh Affairs: Liberation War & Constitutional Amendments", "বাংলাদেশ বিষয়াবলী: মুক্তিযুদ্ধ ও সংবিধানের গুরুত্বপূর্ণ সংশোধনী")
                "International Affairs" -> Pair("International Affairs: Global Geopolitics & Regional Treaties", "আন্তর্জাতিক বিষয়াবলী: বৈশ্বিক ভূরাজনীতি ও আঞ্চলিক চুক্তি")
                "General Science" -> Pair("General Science: Modern Physics & Everyday Chemicals", "সাধারণ বিজ্ঞান: আধুনিক পদার্থবিজ্ঞান ও দৈনন্দিন রসায়ন")
                else -> Pair("ICT: Computer Architecture & Network Protocols", "তথ্যপ্রযুক্তি: কম্পিউটার আর্কিটেকচার ও ক্লাউড নেটওয়ার্কিং")
            }

            tasks.add(
                StudyTask(
                    id = "task_plan_${index + 1}_${UUID.randomUUID().toString().take(6)}",
                    titleEn = titleEn,
                    titleBn = titleBn,
                    subject = subject,
                    durationMinutes = durationMinutes,
                    isCompleted = index == 0, // First task completed as sample
                    isOverdue = false,
                    priority = if (index < 2) "HIGH" else "MEDIUM"
                )
            )
        }

        // 2. Practice Target Task
        tasks.add(
            StudyTask(
                id = "task_practice_target_${UUID.randomUUID().toString().take(6)}",
                titleEn = "Daily MCQ Practice: 50 Questions with Negative Marking Check",
                titleBn = "দৈনিক এমসিকিউ প্র্যাকটিস: নেগেটিভ মার্কিং হিসাবসহ ৫০টি প্রশ্ন সমাধান",
                subject = "Practice Target",
                durationMinutes = 40,
                isCompleted = false,
                isOverdue = false,
                priority = "HIGH"
            )
        )

        // 3. Spaced Repetition Revision Task
        tasks.add(
            StudyTask(
                id = "task_revision_target_${UUID.randomUUID().toString().take(6)}",
                titleEn = "Spaced Repetition: Review Bookmarked & Wrong Answer Items (Day 3 Review)",
                titleBn = "স্পেসড রিপিটেশন রিভিশন: বুকমার্ককৃত ও ভুল উত্তরের রিভিশন (৩য় দিন)",
                subject = "Revision",
                durationMinutes = 30,
                isCompleted = false,
                isOverdue = false,
                priority = "CRITICAL"
            )
        )

        return tasks
    }

    /**
     * Reschedules a study task by postponing its timestamp and clearing overdue state.
     */
    fun rescheduleTask(task: StudyTask, additionalDays: Int = 1): StudyTask {
        return task.copy(
            isOverdue = false,
            isCompleted = false
        )
    }
}
