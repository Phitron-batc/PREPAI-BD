package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.PrepAiDatabase
import com.example.data.local.SessionManager
import com.example.data.model.AiReviewQueueItem
import com.example.data.model.AuthState
import com.example.data.model.ChatMessage
import com.example.data.model.DifficultyLevel
import com.example.data.model.DocumentInfo
import com.example.data.model.ExamAttempt
import com.example.data.model.JobCircular
import com.example.data.model.MockExam
import com.example.data.model.Question
import com.example.data.model.ReviewStatus
import com.example.data.model.StudyTask
import com.example.data.engine.CareerIntelligenceEngine
import com.example.data.engine.CareerPathRecommendation
import com.example.data.engine.EligibilityEvaluation
import com.example.data.engine.ExamEngine
import com.example.data.engine.NotificationEngine
import com.example.data.engine.RagKnowledgeEngine
import com.example.data.engine.RagQueryResponse
import com.example.data.engine.SpacedRepetitionEngine
import com.example.data.engine.StudyPlannerEngine
import com.example.data.engine.WeaknessDoctorEngine
import com.example.data.model.AppNotification
import com.example.data.model.CircularStatus
import com.example.data.model.NotificationType
import com.example.data.model.PaymentProvider
import com.example.data.model.PaymentResult
import com.example.data.model.PracticeConfig
import com.example.data.model.PracticeResult
import com.example.data.model.QuestionStatus
import com.example.data.model.SpacedRepetitionItem
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionTier
import com.example.data.model.TutorMode
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.WeaknessItem
import com.example.data.remote.supabase.SupabaseAuthResult
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.repository.PrepAiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    SIGN_UP,
    FORGOT_PASSWORD,
    LANDING,
    AUTH,
    STUDENT_DASHBOARD,
    PRACTICE_CENTER,
    MOCK_EXAMS,
    ACTIVE_EXAM,
    EXAM_RESULT,
    AI_COPILOT,
    WEAKNESS_DETECTOR,
    STUDY_PLANNER,
    ANALYTICS,
    JOB_CIRCULARS,
    PROFILE,
    SETTINGS,
    ADMIN_DASHBOARD
}

data class ActiveExamState(
    val exam: MockExam? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // question index -> selected option index
    val markedForReview: Set<Int> = emptySet(),
    val remainingSeconds: Int = 1800,
    val isSubmitted: Boolean = false,
    val lastAttempt: ExamAttempt? = null
)

class PrepAiViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = PrepAiDatabase.getInstance(application)
    private val repository = PrepAiRepository(database, sessionManager = sessionManager)
    private val geminiService = GeminiAiService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLiveBackendConnected: StateFlow<Boolean> = MutableStateFlow(SupabaseConfig.isLiveBackendConfigured()).asStateFlow()
    val subscriptionPlans = repository.getSubscriptionPlans()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allQuestions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyTasks: StateFlow<List<StudyTask>> = repository.studyTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weaknessItems: StateFlow<List<WeaknessItem>> = repository.weaknessItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobCirculars: StateFlow<List<JobCircular>> = repository.jobCirculars
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examAttempts: StateFlow<List<ExamAttempt>> = repository.examAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentLanguage = MutableStateFlow("BN") // "BN" or "EN"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Practice Center state
    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSubject = MutableStateFlow("ALL")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Mock Exams & Active Exam
    val availableMockExams = MutableStateFlow(repository.getMockExams())
    private val _activeExamState = MutableStateFlow(ActiveExamState())
    val activeExamState: StateFlow<ActiveExamState> = _activeExamState.asStateFlow()
    private var examTimerJob: Job? = null

    // AI Copilot state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                messageText = "স্বাগতম! আমি PrepAI Copilot। বিসিএস, বাংলাদেশ ব্যাংক, প্রাইমারি শিক্ষক নিয়োগ বা যেকোনো সরকারি চাকরির প্রস্তুতিতে আপনাকে সাহায্য করতে আমি প্রস্তুত। যেকোনো প্রশ্ন, গণিত শর্টকাট বা রিভিশন রুটিন সম্পর্কে জিজ্ঞাসা করুন।",
                mode = TutorMode.SIMPLE
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentTutorMode = MutableStateFlow(TutorMode.SIMPLE)
    val currentTutorMode: StateFlow<TutorMode> = _currentTutorMode.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Admin state
    val aiReviewQueue = MutableStateFlow(repository.getAiReviewQueueItems())
    val adminDocuments = MutableStateFlow(repository.getAdminDocuments())
    private val _adminActiveTab = MutableStateFlow("OVERVIEW") // OVERVIEW, QUESTIONS, AI_QUEUE, DOCUMENTS, CIRCULARS, USERS
    val adminActiveTab: StateFlow<String> = _adminActiveTab.asStateFlow()

    // Practice Mode State
    private val _practiceConfig = MutableStateFlow(PracticeConfig())
    val practiceConfig: StateFlow<PracticeConfig> = _practiceConfig.asStateFlow()

    private val _isPracticeModeActive = MutableStateFlow(false)
    val isPracticeModeActive: StateFlow<Boolean> = _isPracticeModeActive.asStateFlow()

    private val _practiceQuestions = MutableStateFlow<List<Question>>(emptyList())
    val practiceQuestions: StateFlow<List<Question>> = _practiceQuestions.asStateFlow()

    private val _practiceCurrentIndex = MutableStateFlow(0)
    val practiceCurrentIndex: StateFlow<Int> = _practiceCurrentIndex.asStateFlow()

    private val _practiceSelectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val practiceSelectedAnswers: StateFlow<Map<Int, Int>> = _practiceSelectedAnswers.asStateFlow()

    private val _practiceShowExplanation = MutableStateFlow(false)
    val practiceShowExplanation: StateFlow<Boolean> = _practiceShowExplanation.asStateFlow()

    private var practiceStartTimeMs: Long = 0L

    private val _practiceResult = MutableStateFlow<PracticeResult?>(null)
    val practiceResult: StateFlow<PracticeResult?> = _practiceResult.asStateFlow()

    // Spaced Repetition State
    private val _spacedRepetitionItems = MutableStateFlow<List<SpacedRepetitionItem>>(emptyList())
    val spacedRepetitionItems: StateFlow<List<SpacedRepetitionItem>> = _spacedRepetitionItems.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow(NotificationEngine.getInitialNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Career Recommendations
    val careerRecommendations: MutableStateFlow<List<CareerPathRecommendation>> = MutableStateFlow(CareerIntelligenceEngine.generateCareerRecommendations(null))

    init {
        viewModelScope.launch {
            NotificationEngine.initNotificationChannels(application)
            repository.initializeSeedDataIfNeeded()
            val restoredUser = repository.restoreSession()
            if (restoredUser != null) {
                _authState.value = AuthState.Authenticated(restoredUser)
            }
            val questions = repository.allQuestions.firstOrNull() ?: emptyList()
            val attempts = repository.examAttempts.firstOrNull() ?: emptyList()
            _spacedRepetitionItems.value = SpacedRepetitionEngine.generateInitialSpacedItems(questions, attempts)
            careerRecommendations.value = CareerIntelligenceEngine.generateCareerRecommendations(restoredUser)
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "BN") "EN" else "BN"
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setUserRole(role: UserRole) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.getInitialUser()
            val updated = current.copy(role = role)
            repository.updateUser(updated)
        }
    }

    fun updateUserProfile(name: String, targetExam: String, dailyHours: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.getInitialUser()
            val updated = current.copy(
                fullName = name,
                targetExam = targetExam,
                dailyStudyHours = dailyHours
            )
            repository.updateUser(updated)
        }
    }

    fun toggleBookmark(question: Question) {
        viewModelScope.launch {
            repository.toggleBookmark(question.id, question.isBookmarked)
        }
    }

    fun toggleTask(taskId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, !currentStatus)
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTutorMode(mode: TutorMode) {
        _currentTutorMode.value = mode
    }

    fun setAdminTab(tab: String) {
        _adminActiveTab.value = tab
    }

    fun setAdminActiveTab(tab: String) {
        setAdminTab(tab)
    }

    // AI Chat Interaction
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(isUser = true, messageText = text, mode = _currentTutorMode.value)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val aiResponseText = geminiService.askTutor(
                userQuery = text,
                mode = _currentTutorMode.value,
                language = _currentLanguage.value
            )
            val aiMsg = ChatMessage(isUser = false, messageText = aiResponseText, mode = _currentTutorMode.value)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    // Mock Exam flow
    fun startExam(exam: MockExam) {
        val questionsList = allQuestions.value.filter { exam.questionIds.contains(it.id) }
            .ifEmpty { allQuestions.value.take(exam.questionIds.size.coerceAtLeast(3)) }

        _activeExamState.value = ActiveExamState(
            exam = exam,
            questions = questionsList,
            currentIndex = 0,
            selectedAnswers = emptyMap(),
            markedForReview = emptySet(),
            remainingSeconds = exam.durationMinutes * 60,
            isSubmitted = false,
            lastAttempt = null
        )
        _currentScreen.value = AppScreen.ACTIVE_EXAM
        startTimer()
    }

    private fun startTimer() {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_activeExamState.value.remainingSeconds > 0 && !_activeExamState.value.isSubmitted) {
                delay(1000)
                _activeExamState.value = _activeExamState.value.copy(
                    remainingSeconds = _activeExamState.value.remainingSeconds - 1
                )
            }
            if (_activeExamState.value.remainingSeconds <= 0 && !_activeExamState.value.isSubmitted) {
                submitExam()
            }
        }
    }

    fun selectExamOption(optionIndex: Int) {
        val current = _activeExamState.value
        val updatedAnswers = current.selectedAnswers.toMutableMap()
        updatedAnswers[current.currentIndex] = optionIndex
        _activeExamState.value = current.copy(selectedAnswers = updatedAnswers)
    }

    fun setExamQuestionIndex(index: Int) {
        if (index in _activeExamState.value.questions.indices) {
            _activeExamState.value = _activeExamState.value.copy(currentIndex = index)
        }
    }

    fun toggleMarkForReview() {
        val current = _activeExamState.value
        val updatedMarks = current.markedForReview.toMutableSet()
        if (updatedMarks.contains(current.currentIndex)) {
            updatedMarks.remove(current.currentIndex)
        } else {
            updatedMarks.add(current.currentIndex)
        }
        _activeExamState.value = current.copy(markedForReview = updatedMarks)
    }

    fun submitExam() {
        examTimerJob?.cancel()
        val current = _activeExamState.value
        val exam = current.exam ?: return

        val timeSpent = (exam.durationMinutes * 60) - current.remainingSeconds
        val examTitle = if (_currentLanguage.value == "BN") exam.titleBn else exam.titleEn
        val attempt = ExamEngine.evaluateMockExam(
            exam = exam,
            questions = current.questions,
            selectedAnswers = current.selectedAnswers,
            timeSpentSeconds = timeSpent,
            examTitleOverride = examTitle
        )

        viewModelScope.launch {
            repository.saveExamAttempt(attempt)
        }

        _activeExamState.value = current.copy(
            isSubmitted = true,
            lastAttempt = attempt
        )
        _currentScreen.value = AppScreen.EXAM_RESULT
    }

    // Admin Actions
    fun approveAiQuestion(itemId: String) {
        val queue = aiReviewQueue.value.toMutableList()
        val index = queue.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = queue[index]
            queue[index] = item.copy(status = ReviewStatus.APPROVED)
            aiReviewQueue.value = queue
            viewModelScope.launch {
                repository.addQuestion(item.question)
            }
        }
    }

    fun rejectAiQuestion(itemId: String) {
        val queue = aiReviewQueue.value.toMutableList()
        val index = queue.indexOfFirst { it.id == itemId }
        if (index != -1) {
            queue[index] = queue[index].copy(status = ReviewStatus.REJECTED)
            aiReviewQueue.value = queue
        }
    }

    fun addNewQuestion(question: Question) {
        viewModelScope.launch {
            repository.addQuestion(question)
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            repository.updateQuestion(question)
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            repository.deleteQuestion(questionId)
        }
    }

    fun isAiLiveApiConfigured(): Boolean {
        return geminiService.isLiveApiConfigured()
    }

    fun addStudyTask(
        titleEn: String,
        titleBn: String,
        subject: String,
        durationMinutes: Int,
        priority: String = "HIGH"
    ) {
        val newTask = StudyTask(
            id = "task_${System.currentTimeMillis()}",
            titleEn = titleEn,
            titleBn = titleBn,
            subject = subject,
            durationMinutes = durationMinutes,
            isCompleted = false,
            isOverdue = false,
            priority = priority
        )
        viewModelScope.launch {
            repository.insertStudyTask(newTask)
        }
    }

    fun deleteStudyTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteStudyTask(taskId)
        }
    }

    fun login(email: String, role: UserRole = UserRole.STUDENT) {
        setUserRole(role)
        if (role == UserRole.ADMIN) {
            navigateTo(AppScreen.ADMIN_DASHBOARD)
        } else {
            navigateTo(AppScreen.STUDENT_DASHBOARD)
        }
    }

    fun loginWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.signIn(email, password)
            when (result) {
                is SupabaseAuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.userProfile)
                    if (result.userProfile.role == UserRole.ADMIN) {
                        navigateTo(AppScreen.ADMIN_DASHBOARD)
                    } else {
                        navigateTo(AppScreen.STUDENT_DASHBOARD)
                    }
                    onResult(true, null)
                }
                is SupabaseAuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    onResult(false, result.message)
                }
                else -> {
                    _authState.value = AuthState.Error("Unexpected response")
                    onResult(false, "Unexpected response")
                }
            }
        }
    }

    fun signUp(fullName: String, email: String, targetExam: String) {
        signUpWithEmailPassword(fullName, email, "default_pass", targetExam)
    }

    fun signUpWithEmailPassword(
        fullName: String,
        email: String,
        password: String,
        targetExam: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.signUp(email, password, fullName, targetExam)
            when (result) {
                is SupabaseAuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.userProfile)
                    navigateTo(AppScreen.STUDENT_DASHBOARD)
                    onResult(true, null)
                }
                is SupabaseAuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                    onResult(false, result.message)
                }
                else -> {
                    _authState.value = AuthState.Error("Unexpected response")
                    onResult(false, "Unexpected response")
                }
            }
        }
    }

    fun sendPasswordReset(
        email: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.sendPasswordReset(email)
            when (result) {
                is SupabaseAuthResult.PasswordResetSent -> onResult(true, null)
                is SupabaseAuthResult.Error -> onResult(false, result.message)
                else -> onResult(true, null)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            _authState.value = AuthState.Idle
            navigateTo(AppScreen.LOGIN)
        }
    }

    // -------------------------------------------------------------
    // Phase 3: Core Practice Engine
    // -------------------------------------------------------------
    fun startCustomPractice(config: PracticeConfig) {
        _practiceConfig.value = config
        val approved = ExamEngine.filterApprovedQuestions(
            questions = allQuestions.value,
            category = config.examCategory,
            subject = config.subject,
            topic = config.topic,
            difficulty = config.difficulty
        )
        val selected = if (approved.size <= config.questionCount) approved.shuffled() else approved.shuffled().take(config.questionCount)
        _practiceQuestions.value = selected
        _practiceCurrentIndex.value = 0
        _practiceSelectedAnswers.value = emptyMap()
        _practiceShowExplanation.value = false
        _practiceResult.value = null
        practiceStartTimeMs = System.currentTimeMillis()
        _isPracticeModeActive.value = true
        _currentScreen.value = AppScreen.PRACTICE_CENTER
    }

    fun selectPracticeAnswer(optionIndex: Int) {
        val currentAnswers = _practiceSelectedAnswers.value.toMutableMap()
        currentAnswers[_practiceCurrentIndex.value] = optionIndex
        _practiceSelectedAnswers.value = currentAnswers
        _practiceShowExplanation.value = true

        val currentQ = _practiceQuestions.value.getOrNull(_practiceCurrentIndex.value)
        if (currentQ != null) {
            val wasCorrect = optionIndex == currentQ.correctIndex
            advanceSpacedRepetition(currentQ.id, wasCorrect)
        }
    }

    fun selectPracticeAnswer(questionIndex: Int, optionIndex: Int) {
        _practiceCurrentIndex.value = questionIndex
        selectPracticeAnswer(optionIndex)
    }

    fun nextPracticeQuestion() {
        if (_practiceCurrentIndex.value < _practiceQuestions.value.size - 1) {
            _practiceCurrentIndex.value += 1
            _practiceShowExplanation.value = _practiceSelectedAnswers.value.containsKey(_practiceCurrentIndex.value)
        } else {
            finishPracticeSession()
        }
    }

    fun prevPracticeQuestion() {
        if (_practiceCurrentIndex.value > 0) {
            _practiceCurrentIndex.value -= 1
            _practiceShowExplanation.value = _practiceSelectedAnswers.value.containsKey(_practiceCurrentIndex.value)
        }
    }

    fun finishPracticeSession() {
        val elapsedSeconds = ((System.currentTimeMillis() - practiceStartTimeMs) / 1000).toInt().coerceAtLeast(1)
        val result = ExamEngine.evaluatePracticeSession(
            questions = _practiceQuestions.value,
            selectedAnswers = _practiceSelectedAnswers.value,
            timeSpentSeconds = elapsedSeconds
        )
        _practiceResult.value = result
    }

    fun clearPracticeResult() {
        _practiceResult.value = null
    }

    fun previousPracticeQuestion() {
        prevPracticeQuestion()
    }

    fun exitPracticeMode() {
        _isPracticeModeActive.value = false
        _practiceQuestions.value = emptyList()
        _practiceResult.value = null
    }

    // -------------------------------------------------------------
    // Phase 3: Spaced Repetition Engine
    // -------------------------------------------------------------
    fun startSpacedRevisionPractice() {
        val dueQuestionIds = _spacedRepetitionItems.value
            .filter { it.status == com.example.data.model.SpacedStatus.DUE_TODAY || it.status == com.example.data.model.SpacedStatus.OVERDUE }
            .map { it.questionId }
            .toSet()

        val revisionQuestions = allQuestions.value.filter { it.id in dueQuestionIds }
        if (revisionQuestions.isNotEmpty()) {
            _practiceQuestions.value = revisionQuestions
            _practiceCurrentIndex.value = 0
            _practiceSelectedAnswers.value = emptyMap()
            _practiceShowExplanation.value = false
            _practiceResult.value = null
            practiceStartTimeMs = System.currentTimeMillis()
            _isPracticeModeActive.value = true
            _currentScreen.value = AppScreen.PRACTICE_CENTER
        } else {
            startCustomPractice(PracticeConfig(questionCount = 10))
        }
    }

    fun advanceSpacedRepetition(questionId: String, wasCorrect: Boolean) {
        val currentList = _spacedRepetitionItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.questionId == questionId }
        if (index != -1) {
            currentList[index] = SpacedRepetitionEngine.advanceRepetition(currentList[index], wasCorrect)
            _spacedRepetitionItems.value = currentList
        }
    }

    // -------------------------------------------------------------
    // Phase 3: Weakness Doctor Engine
    // -------------------------------------------------------------
    fun startFixMyWeaknessPractice() {
        val targeted = WeaknessDoctorEngine.generateTargetedRemediationQuestions(
            weaknesses = weaknessItems.value,
            allQuestions = allQuestions.value,
            count = 10
        )
        if (targeted.isNotEmpty()) {
            _practiceQuestions.value = targeted
            _practiceCurrentIndex.value = 0
            _practiceSelectedAnswers.value = emptyMap()
            _practiceShowExplanation.value = false
            _practiceResult.value = null
            practiceStartTimeMs = System.currentTimeMillis()
            _isPracticeModeActive.value = true
            _currentScreen.value = AppScreen.PRACTICE_CENTER
        } else {
            startCustomPractice(PracticeConfig(questionCount = 10))
        }
    }

    // -------------------------------------------------------------
    // Phase 3: Study Planner Engine
    // -------------------------------------------------------------
    fun generateCustomStudyPlan(
        dailyHours: Int,
        targetExam: String,
        examDate: String,
        subjectPriorities: List<String>
    ) {
        viewModelScope.launch {
            val generatedTasks = StudyPlannerEngine.generatePersonalizedPlan(
                dailyStudyHours = dailyHours,
                targetExam = targetExam,
                targetExamDate = examDate,
                subjectPriorities = subjectPriorities
            )
            repository.saveStudyTasks(generatedTasks)
        }
    }

    fun rescheduleStudyTask(taskId: String) {
        viewModelScope.launch {
            val currentTasks = studyTasks.value
            val task = currentTasks.find { it.id == taskId }
            if (task != null) {
                repository.insertStudyTask(task.copy(isCompleted = false, isOverdue = false))
            }
        }
    }

    // -------------------------------------------------------------
    // Phase 4: Document Ingestion & RAG Knowledge Engine
    // -------------------------------------------------------------
    fun uploadAndProcessDocument(
        title: String,
        docType: String,
        category: String,
        pageCount: Int,
        source: String
    ) {
        val newDoc = RagKnowledgeEngine.processDocumentPipeline(
            title = title,
            documentType = docType,
            category = category,
            pageCount = pageCount,
            sourceMetadata = source
        )
        val updatedDocs = adminDocuments.value.toMutableList()
        updatedDocs.add(0, newDoc)
        adminDocuments.value = updatedDocs
    }

    fun draftAiQuestionFromDocument(docTitle: String, chapter: String, subject: String) {
        val draftItem = RagKnowledgeEngine.draftAiQuestionFromDocument(
            documentTitle = docTitle,
            chapter = chapter,
            subject = subject
        )
        val updatedQueue = aiReviewQueue.value.toMutableList()
        updatedQueue.add(0, draftItem)
        aiReviewQueue.value = updatedQueue
    }

    fun requestAiQuestionRevision(itemId: String, notes: String) {
        val updatedQueue = RagKnowledgeEngine.requestRevision(aiReviewQueue.value, itemId, notes)
        aiReviewQueue.value = updatedQueue
    }

    fun queryKnowledgeRag(query: String, onResult: (RagQueryResponse) -> Unit) {
        viewModelScope.launch {
            val response = RagKnowledgeEngine.queryRagKnowledge(
                query = query,
                geminiService = geminiService,
                indexedDocs = adminDocuments.value
            )
            onResult(response)
        }
    }

    // -------------------------------------------------------------
    // Phase 5: Career Intelligence & Job Circulars Engine
    // -------------------------------------------------------------
    fun evaluateEligibility(circular: JobCircular): EligibilityEvaluation {
        return CareerIntelligenceEngine.checkEligibility(userProfile.value, circular)
    }

    fun addJobCircular(circular: JobCircular) {
        viewModelScope.launch {
            repository.saveJobCircular(circular)
        }
    }

    fun updateJobCircular(circular: JobCircular) {
        viewModelScope.launch {
            repository.saveJobCircular(circular)
        }
    }

    fun archiveJobCircular(circularId: String) {
        viewModelScope.launch {
            val circular = jobCirculars.value.find { it.id == circularId }
            if (circular != null) {
                repository.saveJobCircular(circular.copy(circularStatus = CircularStatus.ARCHIVED))
            }
        }
    }

    // -------------------------------------------------------------
    // Phase 6: Notification & Alerts Engine
    // -------------------------------------------------------------
    fun markNotificationRead(id: String) {
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = updated
    }

    fun triggerTestNotification(type: NotificationType) {
        NotificationEngine.showLocalNotification(
            context = getApplication(),
            notification = AppNotification(
                id = "notif_test_${System.currentTimeMillis()}",
                title = if (_currentLanguage.value == "BN") "নতুন সার্কুলার এলার্ট" else "New Job Circular Alert",
                message = if (_currentLanguage.value == "BN") "৪৭তম বিসিএস প্রিলিমিনারি বিজ্ঞপ্তি প্রকাশিত হয়েছে!" else "47th BCS Preliminary Circular published. Check eligibility now!",
                type = type,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // -------------------------------------------------------------
    // Phase 6: Payment Gateway Engine
    // -------------------------------------------------------------
    fun processSubscriptionPayment(
        plan: SubscriptionPlan,
        provider: PaymentProvider,
        phone: String,
        isSandbox: Boolean = false,
        onResult: (PaymentResult) -> Unit
    ) {
        viewModelScope.launch {
            if (isSandbox) {
                val success = PaymentResult.Success(
                    transactionId = "TXN_MFS_${System.currentTimeMillis().toString().takeLast(6)}",
                    amount = plan.priceBdt,
                    plan = plan
                )
                val current = userProfile.value ?: repository.getInitialUser()
                repository.updateUser(current.copy(subscriptionTier = plan.tier))
                onResult(success)
                return@launch
            }

            val gateway = when (provider) {
                PaymentProvider.BKASH -> com.example.data.engine.BkashPaymentGateway()
                PaymentProvider.NAGAD -> com.example.data.engine.NagadPaymentGateway()
                PaymentProvider.SSLCOMMERZ -> com.example.data.engine.SslCommerzPaymentGateway()
            }
            val result = gateway.initiatePayment(plan, phone)
            if (result is PaymentResult.Success) {
                val current = userProfile.value ?: repository.getInitialUser()
                repository.updateUser(current.copy(subscriptionTier = plan.tier))
            }
            onResult(result)
        }
    }
}

