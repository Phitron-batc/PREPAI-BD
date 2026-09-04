package com.example.data.repository

import com.example.data.local.PrepAiDatabase
import com.example.data.local.SessionManager
import com.example.data.model.AiReviewQueueItem
import com.example.data.model.DifficultyLevel
import com.example.data.model.DocumentInfo
import com.example.data.model.ExamAttempt
import com.example.data.model.JobCircular
import com.example.data.model.MatchStatus
import com.example.data.model.MockExam
import com.example.data.model.Question
import com.example.data.model.ReviewStatus
import com.example.data.model.StudyTask
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.model.WeaknessItem
import com.example.data.remote.supabase.SupabaseAuthResult
import com.example.data.remote.supabase.SupabaseAuthService
import com.example.data.remote.supabase.SupabaseRestService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PrepAiRepository(
    private val database: PrepAiDatabase,
    private val authService: SupabaseAuthService = SupabaseAuthService(),
    private val restService: SupabaseRestService = SupabaseRestService(),
    private val sessionManager: SessionManager? = null
) {

    val userProfile: Flow<UserProfile?> = database.userDao().getUserProfile()
    val allQuestions: Flow<List<Question>> = database.questionDao().getAllQuestions()
    val bookmarkedQuestions: Flow<List<Question>> = database.questionDao().getBookmarkedQuestions()
    val studyTasks: Flow<List<StudyTask>> = database.studyDao().getAllTasks()
    val weaknessItems: Flow<List<WeaknessItem>> = database.studyDao().getAllWeaknesses()
    val examAttempts: Flow<List<ExamAttempt>> = database.studyDao().getAllAttempts()
    val jobCirculars: Flow<List<JobCircular>> = database.circularDao().getAllCirculars()
    val mockExams: Flow<List<MockExam>> = database.mockExamDao().getAllMockExams()

    suspend fun initializeSeedDataIfNeeded() {
        val existingQuestions = database.questionDao().getAllQuestions().firstOrNull()
        if (existingQuestions.isNullOrEmpty()) {
            database.userDao().insertOrUpdateUser(getInitialUser())
            database.questionDao().insertQuestions(getInitialQuestions())
            database.studyDao().insertTasks(getInitialStudyTasks())
            database.studyDao().insertWeaknesses(getInitialWeaknesses())
            database.circularDao().insertCirculars(getInitialJobCirculars())
            database.mockExamDao().insertMockExams(getMockExams())
        }

        // Try syncing remote questions if network and backend available
        syncRemoteQuestions()
    }

    suspend fun syncRemoteQuestions() {
        try {
            val remoteQuestions = restService.fetchQuestions(sessionManager?.getAccessToken())
            if (!remoteQuestions.isNullOrEmpty()) {
                database.questionDao().insertQuestions(remoteQuestions)
            }
        } catch (e: Exception) {
            // Silently fallback to local Room SQLite cache
        }
    }

    // -------------------------------------------------------------
    // Authentication & Session Persistence
    // -------------------------------------------------------------
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        targetExam: String
    ): SupabaseAuthResult {
        val result = authService.signUp(email, password, fullName, targetExam)
        if (result is SupabaseAuthResult.Success) {
            sessionManager?.saveSession(result.session)
            database.userDao().insertOrUpdateUser(result.userProfile)
        }
        return result
    }

    suspend fun signIn(email: String, password: String): SupabaseAuthResult {
        val result = authService.signIn(email, password)
        if (result is SupabaseAuthResult.Success) {
            sessionManager?.saveSession(result.session)
            database.userDao().insertOrUpdateUser(result.userProfile)
        }
        return result
    }

    suspend fun signOut() {
        val token = sessionManager?.getAccessToken()
        authService.signOut(token)
        sessionManager?.clearSession()
    }

    suspend fun sendPasswordReset(email: String): SupabaseAuthResult {
        return authService.sendPasswordReset(email)
    }

    fun isUserLoggedIn(): Boolean = sessionManager?.isLoggedIn() ?: false

    fun getCurrentSession(): UserSession? = sessionManager?.getSession()

    suspend fun restoreSession(): UserProfile? {
        val session = sessionManager?.getSession() ?: return null
        var user = database.userDao().getUserProfile(session.userId).firstOrNull()
        if (user == null) {
            user = UserProfile(
                id = session.userId,
                email = session.email,
                fullName = if (session.role == UserRole.ADMIN) "Admin" else "Student",
                role = session.role
            )
            database.userDao().insertOrUpdateUser(user)
        }
        return user
    }

    // -------------------------------------------------------------
    // User Profile
    // -------------------------------------------------------------
    suspend fun updateUser(user: UserProfile) {
        database.userDao().insertOrUpdateUser(user)
        // Background sync to Supabase
        restService.syncUserProfile(user, sessionManager?.getAccessToken())
    }

    // -------------------------------------------------------------
    // Bookmarks & Question Operations
    // -------------------------------------------------------------
    suspend fun toggleBookmark(questionId: String, currentStatus: Boolean) {
        database.questionDao().updateBookmark(questionId, !currentStatus)
    }

    suspend fun addQuestion(question: Question) {
        database.questionDao().insertQuestions(listOf(question))
        restService.insertQuestion(question, sessionManager?.getAccessToken())
    }

    suspend fun updateQuestion(question: Question) {
        database.questionDao().updateQuestion(question)
        restService.updateQuestion(question, sessionManager?.getAccessToken())
    }

    suspend fun deleteQuestion(questionId: String) {
        database.questionDao().deleteQuestion(questionId)
        restService.deleteQuestion(questionId, sessionManager?.getAccessToken())
    }

    // -------------------------------------------------------------
    // Study Tasks & Weakness Tracking
    // -------------------------------------------------------------
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        database.studyDao().updateTaskStatus(taskId, isCompleted)
    }

    suspend fun insertStudyTask(task: StudyTask) {
        database.studyDao().insertTask(task)
    }

    suspend fun saveStudyTasks(tasks: List<StudyTask>) {
        database.studyDao().insertTasks(tasks)
    }

    suspend fun deleteStudyTask(taskId: String) {
        database.studyDao().deleteTask(taskId)
    }

    // -------------------------------------------------------------
    // Circulars Management
    // -------------------------------------------------------------
    suspend fun saveJobCircular(circular: JobCircular) {
        database.circularDao().insertCirculars(listOf(circular))
    }

    suspend fun removeJobCircular(circularId: String) {
        database.circularDao().deleteCircular(circularId)
    }

    // -------------------------------------------------------------
    // Exam Attempts
    // -------------------------------------------------------------
    suspend fun saveExamAttempt(attempt: ExamAttempt) {
        database.studyDao().insertAttempt(attempt)
        restService.syncExamAttempt(attempt, sessionManager?.getAccessToken())
    }

    // -------------------------------------------------------------
    // Subscription Plans Foundation
    // -------------------------------------------------------------
    fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                tier = SubscriptionTier.FREE,
                nameEn = "Free Starter",
                nameBn = "ফ্রি স্টার্টার",
                priceBdt = 0,
                durationMonths = 12,
                featuresEn = listOf(
                    "Standard BCS & Bank MCQ bank",
                    "5 Daily AI questions",
                    "Basic speed mock test",
                    "Job circular alerts"
                ),
                featuresBn = listOf(
                    "স্ট্যান্ডার্ড বিসিএস ও ব্যাংক প্রশ্নব্যাংক",
                    "প্রতিদিন ৫টি এআই টিউটর উত্তর",
                    "বেসিক স্পিড মডেল টেস্ট",
                    "সরকারি চাকরির সার্কুলার অ্যালার্ট"
                )
            ),
            SubscriptionPlan(
                tier = SubscriptionTier.PREMIUM,
                nameEn = "PRO Aspirant (All-Access)",
                nameBn = "প্রো অ্যাসপির্যান্ট (অল-অ্যাক্সেস)",
                priceBdt = 499,
                durationMonths = 3,
                featuresEn = listOf(
                    "Unlimited Gemini AI Tutor & Deep-Dive explanations",
                    "Personalized Weakness Diagnosis engine",
                    "Real-time BPSC & Bangladesh Bank mock rankings",
                    "Offline question downloads & PDF export",
                    "Priority 24/7 mentor desk support"
                ),
                featuresBn = listOf(
                    "আনলিমিটেড জেমিনাই এআই টিউটর ও কনসেপ্ট বিশ্লেষণ",
                    "ব্যক্তিগত দুর্বলতা নির্ণয় ও রিভিশন শিডিউলার",
                    "লাইভ বিসিএস ও ব্যাংক মেধা তালিকা র‍্যাংকিং",
                    "অফলাইন প্রশ্ন ডাউনলোড ও পিডিএফ সুবিধা",
                    "২৪/৭ ডেডিকেটেড মেন্টর সাপোর্ট"
                )
            )
        )
    }

    fun getInitialUser(): UserProfile {
        return UserProfile(
            id = "user_default_1",
            fullName = "Tanvir Ahmed",
            email = "tanvir.bcs@prepaibd.com",
            role = UserRole.STUDENT,
            education = "B.Sc in Computer Science & Engineering",
            university = "University of Dhaka",
            graduationYear = "2024",
            targetExam = "46th BCS & Bangladesh Bank AD",
            targetExamDate = "Nov 2026",
            dailyStudyHours = 4,
            streakDays = 12,
            xpPoints = 2450,
            level = 5,
            readinessScore = 78,
            preferredLanguage = "BN"
        )
    }

    fun getInitialQuestions(): List<Question> {
        return listOf(
            Question(
                id = "q_bcs_01",
                examCategory = "BCS",
                subject = "Bangla",
                topic = "Bangla Literature",
                questionEn = "Who is the author of 'Charyapada', the earliest extant work in Bengali literature?",
                questionBn = "বাংলা সাহিত্যের প্রাচীনতম নিদর্শন ‘চর্যাপদ’-এর আদি পদকর্তা বা রচয়িতা কে?",
                optionsEn = listOf("Luipa", "Kanhapa", "Bhushukupa", "Shabarpa"),
                optionsBn = listOf("লুইপা", "কাহ্নপা", "ভূসুকুপা", "শবরপা"),
                correctIndex = 0,
                explanationEn = "Luipa (Luipa) is traditionally regarded as the first poet (Adi Kobi) of Charyapada. He composed the first song 'Ka'a tarubara pancha bi dala'.",
                explanationBn = "চর্যাপদের প্রথম পদটির রচয়িতা লুইপা। তাকে চর্যাপদের আদি কবি বা সিদ্ধাচার্য হিসেবে গণ্য করা হয়। চর্যাপদের সর্বাধিক পদ রচনা করেন কাহ্নপা (১৩টি)।",
                aiShortcut = "লুইপা = প্রথম পদকর্তা (পদ ১)। কাহ্নপা = সর্বাধিক পদ (১৩টি পদ)।",
                difficulty = DifficultyLevel.EASY,
                previousYearTag = "44th BCS Preliminary",
                isBookmarked = true
            ),
            Question(
                id = "q_bcs_02",
                examCategory = "BCS",
                subject = "Bangladesh Affairs",
                topic = "Constitution & Liberation War",
                questionEn = "Under which article of the Constitution of Bangladesh is the Right to Life and Personal Liberty guaranteed?",
                questionBn = "বাংলাদেশের সংবিধানের কোন অনুচ্ছেদে 'জীবন ও ব্যক্তি স্বাধীনতার অধিকার' নিশ্চিত করা হয়েছে?",
                optionsEn = listOf("Article 31", "Article 32", "Article 36", "Article 39"),
                optionsBn = listOf("অনুচ্ছেদ ৩১", "অনুচ্ছেদ ৩২", "অনুচ্ছেদ ৩৬", "অনুচ্ছেদ ৩৯"),
                correctIndex = 1,
                explanationEn = "Article 32 provides: 'No person shall be deprived of life or personal liberty, save in accordance with law.'",
                explanationBn = "সংবিধানের ৩২ নম্বর অনুচ্ছেদে বলা হয়েছে, আইনানুযায়ী ব্যতীত জীবন ও ব্যক্তি-স্বাধীনতা হইতে কোন ব্যক্তিকে বঞ্চিত করা যাইবে না। ৩১ অনুচ্ছেদ আইনের আশ্রয়লাভের অধিকার এবং ৩৯ বাক ও ভাব প্রকাশের স্বাধীনতা।",
                aiShortcut = "৩২ = জীবন ও ব্যক্তি স্বাধীনতা; ৩১ = আইনের আশ্রয়; ৩৯ = মতপ্রকাশ ও বাকস্বাধীনতা।",
                difficulty = DifficultyLevel.MEDIUM,
                previousYearTag = "45th BCS Preliminary",
                isBookmarked = false
            ),
            Question(
                id = "q_bank_01",
                examCategory = "BANK",
                subject = "Mathematics",
                topic = "Percentage & Profit-Loss",
                questionEn = "If the price of sugar increases by 25%, by what percent must a family reduce consumption to keep expenditure unchanged?",
                questionBn = "চিনির মূল্য ২৫% বৃদ্ধি পেলে, একটি পরিবার চিনির ব্যবহার শতকরা কত কমালে খরচ একই থাকবে?",
                optionsEn = listOf("15%", "20%", "25%", "33.33%"),
                optionsBn = listOf("১৫%", "২০%", "২৫%", "৩৩.৩৩%"),
                correctIndex = 1,
                explanationEn = "Reduction % = [r / (100 + r)] * 100 = [25 / 125] * 100 = (1/5) * 100 = 20%.",
                explanationBn = "শর্টকাট সূত্র: হ্রাসকৃত হার = [r / (১০০ + r)] × ১০০% = [২৫ / ১২৫] × ১০০% = ২০%। সুতরাং খরচ অপরিবর্তিত রাখতে ব্যবহার ২০% কমাতে হবে।",
                aiShortcut = "Shortcut Formula: [R / (100 + R)] * 100%. If R=25%, Answer is always 20%.",
                difficulty = DifficultyLevel.MEDIUM,
                previousYearTag = "Bangladesh Bank AD 2022",
                isBookmarked = true
            ),
            Question(
                id = "q_bank_02",
                examCategory = "BANK",
                subject = "English",
                topic = "Prepositions & Idioms",
                questionEn = "The committee decided to defer the decision ______ next Monday.",
                questionBn = "কমিটি আগামী সোমবার পর্যন্ত সিদ্ধান্ত স্থগিত রাখার সিদ্ধান্ত নিয়েছে। শূন্যস্থানে উপযুক্ত Preposition কোনটি?",
                optionsEn = listOf("until", "for", "at", "in"),
                optionsBn = listOf("until", "for", "at", "in"),
                correctIndex = 0,
                explanationEn = "'Defer to / until' means postpone or put off to a later time.",
                explanationBn = "'Defer until' বা 'defer to' অর্থ কোনো সময় পর্যন্ত স্থগিত রাখা। এখানে নির্দিষ্ট দিন (next Monday) পর্যন্ত সময়ের সীমা বুঝাতে 'until' ব্যবহৃত হয়।",
                aiShortcut = "Defer + until / to a future point in time.",
                difficulty = DifficultyLevel.MEDIUM,
                previousYearTag = "Combined 9 Banks Officer 2023",
                isBookmarked = false
            ),
            Question(
                id = "q_primary_01",
                examCategory = "PRIMARY",
                subject = "Bangla",
                topic = "Bangla Grammar (সন্ধি ও সমাস)",
                questionEn = "What is the correct Sandhi split for 'চলচ্চিত্র' (Chalachchitra)?",
                questionBn = "'চলচ্চিত্র' শব্দের সঠিক সন্ধি বিচ্ছেদ কোনটি?",
                optionsEn = listOf("চলৎ + চিত্র", "চল + চিত্র", "চলচ্ + চিত্র", "চলতি + চিত্র"),
                optionsBn = listOf("চলৎ + চিত্র", "চল + চিত্র", "চলচ্ + চিত্র", "চলতি + চিত্র"),
                correctIndex = 0,
                explanationEn = "According to Bangla consonant Sandhi rules: ৎ/দ্ + চ = চ্চ. Hence চলৎ + চিত্র = চলচ্চিত্র।",
                explanationBn = "ব্যঞ্জনসন্ধির নিয়ম অনুযায়ী, ত্/দ্-এর পর চ থাকলে ত্/দ্ স্থানে চ হয় এবং দুটি মিলে 'চ্চ' হয়। তাই চলৎ + চিত্র = চলচ্চিত্র।",
                aiShortcut = "ত্ + চ = চ্চ (যেমন: চলৎ + চিত্র = চলচ্চিত্র, উৎ + চারণ = উচ্চারণ)।",
                difficulty = DifficultyLevel.EASY,
                previousYearTag = "Primary Teacher Exam 2023",
                isBookmarked = true
            ),
            Question(
                id = "q_bcs_03",
                examCategory = "BCS",
                subject = "International Affairs",
                topic = "Global Institutions & Geopolitics",
                questionEn = "Where is the headquarters of the Asian Infrastructure Investment Bank (AIIB) situated?",
                questionBn = "এশীয় অবকাঠামো বিনিয়োগ ব্যাংক (AIIB)-এর সদর দপ্তর কোথায় অবস্থিত?",
                optionsEn = listOf("Shanghai", "Beijing", "Manila", "Tokyo"),
                optionsBn = listOf("সাংহাই", "বেইজিং", "ম্যানিলা", "টোকিও"),
                correctIndex = 1,
                explanationEn = "AIIB headquarters is in Beijing, China. (Note: NDB/BRICS bank is in Shanghai, ADB is in Manila).",
                explanationBn = "AIIB-এর সদর দপ্তর চীনের বেইজিংয়ে। নিউ ডেভেলপমেন্ট ব্যাংক (NDB)-এর সদর দপ্তর সাংহাই এবং এশীয় উন্নয়ন ব্যাংক (ADB)-এর সদর দপ্তর ম্যানিলা, ফিলিপাইন।",
                aiShortcut = "AIIB = বেইজিং; NDB (ব্রিকস) = সাংহাই; ADB = ম্যানিলা।",
                difficulty = DifficultyLevel.EASY,
                previousYearTag = "43rd BCS Preliminary",
                isBookmarked = false
            ),
            Question(
                id = "q_bcs_04",
                examCategory = "BCS",
                subject = "ICT",
                topic = "Computer Systems & Networking",
                questionEn = "What does the protocol HTTPS stand for, and which port does it typically use?",
                questionBn = "HTTPS প্রোটোকলের পূর্ণরূপ কী এবং এটি সাধারণত কোন পোর্ট ব্যবহার করে?",
                optionsEn = listOf(
                    "Hypertext Transfer Protocol Secure (Port 443)",
                    "Hypertext Transfer Protocol System (Port 80)",
                    "Hyperlink Transport Protocol Standard (Port 21)",
                    "High Tech Processing Standard (Port 8080)"
                ),
                optionsBn = listOf(
                    "Hypertext Transfer Protocol Secure (Port 443)",
                    "Hypertext Transfer Protocol System (Port 80)",
                    "Hyperlink Transport Protocol Standard (Port 21)",
                    "High Tech Processing Standard (Port 8080)"
                ),
                correctIndex = 0,
                explanationEn = "HTTPS stands for Hypertext Transfer Protocol Secure and encrypts HTTP traffic using TLS/SSL on standard port 443. (HTTP uses port 80).",
                explanationBn = "HTTPS = Hypertext Transfer Protocol Secure। এটি এনক্রিপ্টেড কমিউনিকেশনের জন্য পোর্ট ৪৪৩ (Port 443) ব্যবহার করে। সাধারণ HTTP পোর্ট ৮০ ব্যবহার করে।",
                aiShortcut = "HTTP = 80, HTTPS = 443, FTP = 21, DNS = 53.",
                difficulty = DifficultyLevel.MEDIUM,
                previousYearTag = "45th BCS Preliminary",
                isBookmarked = false
            ),
            Question(
                id = "q_bank_03",
                examCategory = "BANK",
                subject = "Mathematics",
                topic = "Ratio & Proportion",
                questionEn = "The ratio of boys and girls in a college is 7:5. If there are 2400 students in total, how many girls are there?",
                questionBn = "একটি কলেজে ছাত্র ও ছাত্রীর অনুপাত ৭:৫। মোট শিক্ষার্থীর সংখ্যা ২৪০০ জন হলে, ছাত্রীর সংখ্যা কত?",
                optionsEn = listOf("800", "1000", "1200", "1400"),
                optionsBn = listOf("৮০০ জন", "১০০০ জন", "১২০০ জন", "১৪০০ জন"),
                correctIndex = 1,
                explanationEn = "Total parts = 7 + 5 = 12 parts. Each part = 2400 / 12 = 200. Girls = 5 * 200 = 1000.",
                explanationBn = "অনুপাতের মোট ভাগ = ৭ + ৫ = ১২ ভাগ। ১ ভাগ = ২৪০০ ÷ ১২ = ২০০। ছাত্রীর সংখ্যা = ৫ × ২০০ = ১০০০ জন। ছাত্রের সংখ্যা = ৭ × ২০০ = ১৪০০ জন।",
                aiShortcut = "Girls = (5 / 12) * 2400 = 1000 students.",
                difficulty = DifficultyLevel.EASY,
                previousYearTag = "Sonali Bank Officer 2024",
                isBookmarked = false
            )
        )
    }

    fun getInitialStudyTasks(): List<StudyTask> {
        return listOf(
            StudyTask(
                id = "task_1",
                titleEn = "Bangla Grammar: Samas & Karok revision",
                titleBn = "বাংলা ব্যাকরণ: সমাস ও কারক রিভিশন",
                subject = "Bangla",
                durationMinutes = 45,
                isCompleted = true,
                priority = "HIGH"
            ),
            StudyTask(
                id = "task_2",
                titleEn = "English Vocabulary: Barron's High Frequency Words",
                titleBn = "ইংরেজি ভোকাবুলারি: ব্যারনস হাই ফ্রিকোয়েন্সি শব্দমালা",
                subject = "English",
                durationMinutes = 30,
                isCompleted = false,
                priority = "HIGH"
            ),
            StudyTask(
                id = "task_3",
                titleEn = "Math Practice: Ratio, Proportion & Profit-Loss",
                titleBn = "গণিত প্র্যাকটিস: অনুপাত ও শতকরা লাভ-ক্ষতি",
                subject = "Mathematics",
                durationMinutes = 60,
                isCompleted = false,
                priority = "HIGH"
            ),
            StudyTask(
                id = "task_4",
                titleEn = "Bangladesh Affairs: Liberation War & Constitution 1-47",
                titleBn = "বাংলাদেশ বিষয়াবলি: মুক্তিযুদ্ধ ও সংবিধান ১ম-৪৭ অনুচ্ছেদ",
                subject = "Bangladesh Affairs",
                durationMinutes = 30,
                isCompleted = false,
                priority = "MEDIUM"
            ),
            StudyTask(
                id = "task_5",
                titleEn = "Daily Mock Exam: 20 Questions Sprint",
                titleBn = "দৈনিক স্পিড টেস্ট: ২০টি বাছাইকৃত প্রশ্ন",
                subject = "Mixed",
                durationMinutes = 15,
                isCompleted = false,
                priority = "MEDIUM"
            )
        )
    }

    fun getInitialWeaknesses(): List<WeaknessItem> {
        return listOf(
            WeaknessItem(
                id = "w_math",
                subject = "Mathematics",
                topic = "Algebra & Percentage",
                accuracyPercent = 42,
                mistakeCount = 14,
                recommendationEn = "Your Mathematics accuracy dropped by 12% this week. Focus on Ratio, Percentage, and Profit & Loss equations.",
                recommendationBn = "এই সপ্তাহে আপনার গণিতের সঠিকতার হার ১২% কমেছে। শতকরা, অনুপাত ও লাভ-ক্ষতির শর্টকাট টেকনিকে বিশেষ জোর দিন।"
            ),
            WeaknessItem(
                id = "w_eng",
                subject = "English",
                topic = "Prepositions & Voice Change",
                accuracyPercent = 58,
                mistakeCount = 9,
                recommendationEn = "Frequent errors in Appropriate Prepositions and Passive constructions. Daily practice of 20 MCQs recommended.",
                recommendationBn = "Appropriate Prepositions এবং Passive রূপান্তরে বারবার ভুল হচ্ছে। প্রতিদিন ২০টি করে রুলস ভিত্তিক প্রশ্ন সমাধান করুন।"
            ),
            WeaknessItem(
                id = "w_bd",
                subject = "Bangladesh Affairs",
                topic = "Constitution Amendments",
                accuracyPercent = 64,
                mistakeCount = 6,
                recommendationEn = "Review constitutional amendment years and key articles (Articles 26-47 Fundamental Rights).",
                recommendationBn = "সংবিধানের গুরুত্বপূর্ণ অনুচ্ছেদ এবং প্রধান সংশোধনীগুলো রিভিশন সেন্টারে শিডিউল করা হয়েছে।"
            )
        )
    }

    fun getInitialJobCirculars(): List<JobCircular> {
        return listOf(
            JobCircular(
                id = "circ_46_bcs",
                organizationEn = "Bangladesh Public Service Commission (BPSC)",
                organizationBn = "বাংলাদেশ সরকারি কর্ম কমিশন (বিপিএসসি)",
                jobTitleEn = "46th BCS Examination (General & Technical Cadre)",
                jobTitleBn = "৪৬তম বিসিএস পরীক্ষা (সাধারণ ও কারিগরি ক্যাডার)",
                category = "BCS & PSC",
                vacancyCount = 3140,
                qualification = "Graduation / Post Graduation from recognized university",
                ageLimit = "21 to 32 years (Relaxed quota applied)",
                deadline = "15 October 2026",
                applyUrl = "https://bpsc.teletalk.com.bd",
                matchStatus = MatchStatus.SUITABLE,
                matchNotes = "Matches your CS graduation and age criteria. Target exam syllabus mapped."
            ),
            JobCircular(
                id = "circ_bb_ad",
                organizationEn = "Bangladesh Bank (Central Bank)",
                organizationBn = "বাংলাদেশ ব্যাংক (সেন্ট্রাল ব্যাংক)",
                jobTitleEn = "Assistant Director (General)",
                jobTitleBn = "সহকারী পরিচালক (জেনারেল)",
                category = "Bank",
                vacancyCount = 225,
                qualification = "Four-year Bachelor / Master's degree in any discipline",
                ageLimit = "30 years",
                deadline = "28 September 2026",
                applyUrl = "https://erecruitment.bb.org.bd",
                matchStatus = MatchStatus.SUITABLE,
                matchNotes = "Strong profile match. High competition in Mathematics and Analytical ability."
            ),
            JobCircular(
                id = "circ_primary",
                organizationEn = "Directorate of Primary Education (DPE)",
                organizationBn = "প্রাথমিক শিক্ষা অধিদপ্তর (ডিপিই)",
                jobTitleEn = "Assistant Teacher (Government Primary Schools)",
                jobTitleBn = "সহকারী শিক্ষক (সরকারি প্রাথমিক বিদ্যালয়)",
                category = "Primary & NTRCA",
                vacancyCount = 14200,
                qualification = "Bachelor's degree (minimum 2nd class or equivalent CGPA)",
                ageLimit = "21 to 30 years",
                deadline = "05 November 2026",
                applyUrl = "https://dpe.teletalk.com.bd",
                matchStatus = MatchStatus.SUITABLE,
                matchNotes = "Huge vacancies across all 64 districts. Strong Bangla & Math base required."
            ),
            JobCircular(
                id = "circ_railway",
                organizationEn = "Bangladesh Railway",
                organizationBn = "বাংলাদেশ রেলওয়ে",
                jobTitleEn = "Assistant Station Master & Guard Grade-II",
                jobTitleBn = "সহকারী স্টেশন মাস্টার ও গার্ড গ্রেড-২",
                category = "Railway & Govt",
                vacancyCount = 560,
                qualification = "Graduate degree in Science/Commerce/Arts",
                ageLimit = "18 to 30 years",
                deadline = "12 October 2026",
                applyUrl = "https://br.teletalk.com.bd",
                matchStatus = MatchStatus.REVIEW_NEEDED,
                matchNotes = "Medical fitness test has specific visual acuity requirements."
            )
        )
    }

    fun getMockExams(): List<MockExam> {
        return listOf(
            MockExam(
                id = "mock_bcs_full",
                titleEn = "46th BCS Preliminary Model Test #01",
                titleBn = "৪৬তম বিসিএস প্রিলিমিনারি পূর্ণাঙ্গ মডেল টেস্ট - ০১",
                examCategory = "BCS",
                durationMinutes = 120,
                totalMarks = 200,
                negativeMarkPerWrong = 0.5f,
                questionIds = listOf("q_bcs_01", "q_bcs_02", "q_bcs_03", "q_bcs_04", "q_bank_01", "q_bank_02", "q_bank_03", "q_primary_01"),
                isAdaptive = true
            ),
            MockExam(
                id = "mock_bank_ad",
                titleEn = "Bangladesh Bank AD Speed Sprint (Math & English)",
                titleBn = "বাংলাদেশ ব্যাংক এডি স্পিড টেস্ট (ম্যাথ ও ইংলিশ)",
                examCategory = "BANK",
                durationMinutes = 45,
                totalMarks = 80,
                negativeMarkPerWrong = 0.25f,
                questionIds = listOf("q_bank_01", "q_bank_02", "q_bank_03", "q_bcs_04"),
                isAdaptive = false
            ),
            MockExam(
                id = "mock_primary_quick",
                titleEn = "Primary Assistant Teacher Special Mock",
                titleBn = "প্রাথমিক শিক্ষক নিয়োগ স্পেশাল মডেল টেস্ট",
                examCategory = "PRIMARY",
                durationMinutes = 60,
                totalMarks = 80,
                negativeMarkPerWrong = 0.25f,
                questionIds = listOf("q_primary_01", "q_bcs_01", "q_bank_01"),
                isAdaptive = false
            )
        )
    }

    fun getAiReviewQueueItems(): List<AiReviewQueueItem> {
        return listOf(
            AiReviewQueueItem(
                id = "rev_01",
                sourceDocument = "MP3 Bangladesh Affairs 2025 Edition.pdf",
                chapter = "Chapter 4: Historical Movements (1947–1971)",
                question = Question(
                    id = "q_ai_gen_1",
                    examCategory = "BCS",
                    subject = "Bangladesh Affairs",
                    topic = "1952 Language Movement",
                    questionEn = "In which session did UNESCO proclaim 21st February as International Mother Language Day?",
                    questionBn = "ইউনেস্কো (UNESCO)-র কোন অধিবেশনে ২১শে ফেব্রুয়ারিকে আন্তর্জাতিক মাতৃভাষা দিবস হিসেবে স্বীকৃতি দেওয়া হয়?",
                    optionsEn = listOf("28th Session", "30th Session", "32nd Session", "35th Session"),
                    optionsBn = listOf("২৮তম অধিবেশন", "৩০তম অধিবেশন", "৩২তম অধিবেশন", "৩৫তম অধিবেশন"),
                    correctIndex = 1,
                    explanationEn = "In UNESCO's 30th General Conference on 17 November 1999, 21st February was proclaimed as International Mother Language Day.",
                    explanationBn = "১৯৯৯ সালের ১৭ নভেম্বর ইউনেস্কোর ৩০তম সাধারণ সম্মেলনে সর্বসম্মতভাবে ২১শে ফেব্রুয়ারিকে আন্তর্জাতিক মাতৃভাষা দিবস হিসেবে ঘোষণা করা হয়।",
                    aiShortcut = "১৯৯৯ সালের ১৭ নভেম্বর = ইউনেস্কোর ৩০তম অধিবেশন।",
                    difficulty = DifficultyLevel.MEDIUM,
                    previousYearTag = "AI Generated from MP3 Book",
                    isBookmarked = false,
                    isFromAiReview = true
                ),
                status = ReviewStatus.PENDING,
                reviewerNote = "Extracted from page 142. Verified with BPSC official syllabus."
            ),
            AiReviewQueueItem(
                id = "rev_02",
                sourceDocument = "Professor's Bank Math Shortcuts 2026.pdf",
                chapter = "Chapter 8: Time, Speed and Distance",
                question = Question(
                    id = "q_ai_gen_2",
                    examCategory = "BANK",
                    subject = "Mathematics",
                    topic = "Relative Speed & Trains",
                    questionEn = "A train 150m long passes a pole in 15 seconds. What is the speed of the train in km/h?",
                    questionBn = "১৫০ মিটার দীর্ঘ একটি ট্রেন ১৫ সেকেন্ডে একটি খুঁটি অতিক্রম করে। ট্রেনের গতিবেগ ঘণ্টায় কত কিমি?",
                    optionsEn = listOf("30 km/h", "36 km/h", "45 km/h", "54 km/h"),
                    optionsBn = listOf("৩০ কিমি/ঘণ্টা", "৩৬ কিমি/ঘণ্টা", "৪৫ কিমি/ঘণ্টা", "৫৪ কিমি/ঘণ্টা"),
                    correctIndex = 1,
                    explanationEn = "Speed = 150 / 15 = 10 m/s. In km/h = 10 * (18 / 5) = 36 km/h.",
                    explanationBn = "গতিবেগ = ১৫০ ÷ ১৫ = ১০ মিটার/সেকেন্ড। কিমি/ঘণ্টায় রূপান্তর = ১০ × (১৮/৫) = ৩৬ কিমি/ঘণ্টা।",
                    aiShortcut = "m/s থেকে km/h এ নিতে ১৮/৫ দিয়ে গুণ করুন: ১০ × (১৮/৫) = ৩৬ কিমি/ঘণ্টা।",
                    difficulty = DifficultyLevel.EASY,
                    previousYearTag = "AI Generated from Professor's Math",
                    isBookmarked = false,
                    isFromAiReview = true
                ),
                status = ReviewStatus.APPROVED,
                reviewerNote = "Approved by Senior Math Instructor. Formula shortcut verified."
            )
        )
    }

    fun getAdminDocuments(): List<DocumentInfo> {
        return listOf(
            DocumentInfo(
                id = "doc_1",
                title = "BPSC BCS Preliminary Master Syllabus & Guidelines",
                category = "Syllabus / Official Document",
                fileSize = "4.2 MB",
                pageCount = 38,
                chunkCount = 142,
                vectorStatus = "Indexed (RAG Ready)",
                copyrightCleared = true
            ),
            DocumentInfo(
                id = "doc_2",
                title = "MP3 Bangladesh Affairs Comprehensive Notes (Authorized Edition)",
                category = "General Knowledge",
                fileSize = "28.5 MB",
                pageCount = 420,
                chunkCount = 1240,
                vectorStatus = "Indexed (RAG Ready)",
                copyrightCleared = true
            ),
            DocumentInfo(
                id = "doc_3",
                title = "Bangladesh Bank Previous 10 Years Solved Questions",
                category = "Question Bank Archive",
                fileSize = "16.8 MB",
                pageCount = 280,
                chunkCount = 890,
                vectorStatus = "Indexed (RAG Ready)",
                copyrightCleared = true
            ),
            DocumentInfo(
                id = "doc_4",
                title = "Primary Assistant Teacher Model Questions 2026",
                category = "Model Test",
                fileSize = "8.4 MB",
                pageCount = 110,
                chunkCount = 340,
                vectorStatus = "OCR Extracted & Processing",
                copyrightCleared = true
            )
        )
    }
}
