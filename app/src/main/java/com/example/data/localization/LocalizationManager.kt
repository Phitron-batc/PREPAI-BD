package com.example.data.localization

/**
 * Centralized Localization System for PREPAI BD.
 * Provides consistent Bangla and English copy throughout the application.
 */
object LocalizationManager {

    // General App Branding
    val appName = LocalizedString(en = "PREPAI BD", bn = "প্রেপ এআই বিডি")
    val appTagline = LocalizedString(en = "Smart AI Preparation for BCS & Govt Careers", bn = "স্মার্ট প্রস্তুতি, সফল ক্যারিয়ার")

    // Navigation Labels
    val navHome = LocalizedString(en = "Explore", bn = "ড্যাশবোর্ড")
    val navPractice = LocalizedString(en = "Practice", bn = "অনুশীলন")
    val navMockExams = LocalizedString(en = "Mock Exams", bn = "মডেল টেস্ট")
    val navAiTutor = LocalizedString(en = "AI Copilot", bn = "AI টিউটর")
    val navStudyPlanner = LocalizedString(en = "Planner", bn = "প্ল্যানার")
    val navAnalytics = LocalizedString(en = "Weakness", bn = "দুর্বলতা")
    val navJobCirculars = LocalizedString(en = "Jobs", bn = "সার্কুলার")
    val navProfile = LocalizedString(en = "Profile", bn = "প্রোফাইল")
    val navSettings = LocalizedString(en = "Settings", bn = "সেটিংস")

    // Common Actions
    val btnStart = LocalizedString(en = "Start", bn = "শুরু করুন")
    val btnContinue = LocalizedString(en = "Continue", bn = "চালিয়ে যান")
    val btnSubmit = LocalizedString(en = "Submit", bn = "সাবমিট করুন")
    val btnCancel = LocalizedString(en = "Cancel", bn = "বাতিল")
    val btnSave = LocalizedString(en = "Save Changes", bn = "সংরক্ষণ করুন")
    val btnDelete = LocalizedString(en = "Delete", bn = "মুছুন")
    val btnEdit = LocalizedString(en = "Edit", bn = "সম্পাদনা")
    val btnPrevious = LocalizedString(en = "Previous", bn = "আগেরটি")
    val btnNext = LocalizedString(en = "Next", bn = "পরবর্তী")
    val btnBookmark = LocalizedString(en = "Bookmark", bn = "বুকমার্ক")
    val btnSearch = LocalizedString(en = "Search", bn = "অনুসন্ধান")
    val btnFilter = LocalizedString(en = "Filter", bn = "ফিল্টার")
    val btnLogin = LocalizedString(en = "Sign In", bn = "লগইন করুন")
    val btnSignUp = LocalizedString(en = "Create Account", bn = "অ্যাকাউন্ট তৈরি করুন")
    val btnLogout = LocalizedString(en = "Log Out", bn = "লগ আউট")
    val btnForgotPassword = LocalizedString(en = "Forgot Password?", bn = "পাসওয়ার্ড ভুলে গেছেন?")

    // AI Tutor Modes
    val modeSimple = LocalizedString(en = "Simple Mode", bn = "সহজ ভাষা")
    val modeExamShortcuts = LocalizedString(en = "Exam Shortcuts", bn = "পরীক্ষার শর্টকাট")
    val modeDeepConcept = LocalizedString(en = "Deep Concept", bn = "গভীর ধারণা")
    val modeSocratic = LocalizedString(en = "Socratic Guide", bn = "সক্রেটিক মোড")
    val modeRealExamples = LocalizedString(en = "Real Examples", bn = "বাস্তব উদাহরণ")

    // Practice Center Strings
    val practiceTitle = LocalizedString(en = "Interactive MCQ Practice", bn = "ইন্টারেক্টিভ MCQ অনুশীলন")
    val practiceSubtitle = LocalizedString(
        en = "Subject-wise questions with instant verification, AI shortcuts & detailed explanations.",
        bn = "তাৎক্ষণিক সঠিক উত্তর যাচাই, AI শর্টকাট কৌশল ও বিস্তারিত ব্যাখ্যাসহ বিষয়ভিত্তিক প্র্যাকটিস।"
    )
    val noQuestionsFound = LocalizedString(en = "No questions found matching criteria.", bn = "কোনো প্রশ্ন পাওয়া যায়নি।")
    val explanationTitle = LocalizedString(en = "Detailed Explanation & AI Shortcut", bn = "বিস্তারিত সমাধান ও AI শর্টকাট")

    // Mock Exam Strings
    val mockExamTitle = LocalizedString(en = "Live Mock Exams & Speed Tests", bn = "লাইভ মডেল টেস্ট ও স্পিড এক্সাম")
    val mockExamSubtitle = LocalizedString(
        en = "Practice in a real examination environment with negative marking, question palette, and instant score analysis.",
        bn = "বিসিএস এবং ব্যাংক পরীক্ষার অনুরূপ রিয়েল এক্সাম এনভায়রনমেন্ট, কাউন্টডাউন টাইমার ও নেগেটিভ মার্কিং সহ প্র্যাকটিস করুন।"
    )
    val negativeMarkingNote = LocalizedString(
        en = "Negative marking applies per incorrect answer.",
        bn = "প্রতিটি ভুল উত্তরের জন্য নেগেটিভ মার্কিং প্রযোজ্য।"
    )
    val submitConfirmTitle = LocalizedString(en = "Confirm Exam Submission", bn = "পরীক্ষা সাবমিট নিশ্চিতকরণ")
    val timeRemaining = LocalizedString(en = "Time Remaining", bn = "অবশিষ্ট সময়")

    // Exam Results
    val resultTitle = LocalizedString(en = "Exam Results & Performance", bn = "পরীক্ষার ফলাফল ও পারফরম্যান্স")
    val scoreAchieved = LocalizedString(en = "Total Score (After Negative Marking)", bn = "অর্জিত মোট নম্বর (নেগেটিভ মার্কিং সহ)")
    val correctAnswers = LocalizedString(en = "Correct", bn = "সঠিক")
    val wrongAnswers = LocalizedString(en = "Wrong", bn = "ভুল")
    val skippedQuestions = LocalizedString(en = "Skipped", bn = "বাদ দেওয়া")
    val accuracyRate = LocalizedString(en = "Accuracy", bn = "সঠিকতার হার")

    // Study Planner Strings
    val plannerTitle = LocalizedString(en = "AI Study Routine & Syllabus Tracker", bn = "AI স্টাডি রুটিন ও সিলেবাস ট্র্যাকার")
    val dailyGoal = LocalizedString(en = "Daily Study Goal", bn = "দৈনিক পড়ার লক্ষ্য")
    val streakDays = LocalizedString(en = "Day Streak", bn = "দিনের ধারাবাহিকতা")
    val todaysTasks = LocalizedString(en = "Today's Targeted Tasks", bn = "আজকের নির্ধারিত টাস্ক")
    val roadmapTitle = LocalizedString(en = "90-Day BCS & Bank Syllabus Milestones", bn = "৯০ দিনের বিসিএস ও ব্যাংক প্রস্তুতি রোডম্যাপ")

    // Admin Strings
    val adminPortalTitle = LocalizedString(en = "PREPAI Admin & Content Hub", bn = "PREPAI অ্যাডমিন ও কনটেন্ট হাব")
    val tabOverview = LocalizedString(en = "Overview", bn = "ওভারভিউ")
    val tabQuestionBank = LocalizedString(en = "Question Bank", bn = "প্রশ্নব্যাংক")
    val tabAiQueue = LocalizedString(en = "Review Queue", bn = "AI কিউ")
    val tabDocuments = LocalizedString(en = "Documents & RAG", bn = "নথি / RAG")
    val tabStudents = LocalizedString(en = "Students", bn = "শিক্ষার্থী")
    val tabExams = LocalizedString(en = "Mock Exams", bn = "মডেল টেস্ট")
    val tabCirculars = LocalizedString(en = "Job Circulars", bn = "সার্কুলার")
    val tabAnalytics = LocalizedString(en = "Analytics", bn = "অ্যানালিটিক্স")
    val tabSettings = LocalizedString(en = "Settings", bn = "সেটিংস")

    // Auth Strings
    val welcomeBack = LocalizedString(en = "Welcome Back to PrepAI", bn = "প্রেপ এআই বিডিতে স্বাগতম")
    val createAccountPrompt = LocalizedString(en = "Create your free student account", bn = "আপনার ফ্রি শিক্ষার্থী অ্যাকাউন্ট তৈরি করুন")
    val emailLabel = LocalizedString(en = "Email Address", bn = "ইমেইল অ্যাড্রেস")
    val passwordLabel = LocalizedString(en = "Password", bn = "পাসওয়ার্ড")
    val fullNameLabel = LocalizedString(en = "Full Name", bn = "পূর্ণ নাম")
    val targetExamLabel = LocalizedString(en = "Target Examination", bn = "টার্গেট পরীক্ষা")

    // Phase 2 Badges
    val backendNotice = LocalizedString(
        en = "Local Sandbox Mode — Backend/Cloud Database synchronization configured for Phase 2.",
        bn = "লোকাল স্যান্ডবক্স মোড — ফেজ ২ এর জন্য ক্লাউড ডাটাবেজ প্রস্তুত।"
    )
}

/**
 * Data holder for bilingual text.
 */
data class LocalizedString(val en: String, val bn: String) {
    fun get(isBangla: Boolean): String = if (isBangla) bn else en
    fun get(langCode: String): String = if (langCode == "BN") bn else en
}
