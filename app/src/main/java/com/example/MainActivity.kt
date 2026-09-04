package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.landing.LandingScreen
import com.example.ui.screens.student.ActiveExamScreen
import com.example.ui.screens.student.AiCopilotScreen
import com.example.ui.screens.student.ExamResultScreen
import com.example.ui.screens.student.JobCircularsScreen
import com.example.ui.screens.student.MockExamsScreen
import com.example.ui.screens.student.PracticeCenterScreen
import com.example.ui.screens.student.ProfileScreen
import com.example.ui.screens.student.SettingsScreen
import com.example.ui.screens.student.StudentDashboardScreen
import com.example.ui.screens.student.StudyPlannerScreen
import com.example.ui.screens.student.WeaknessDetectorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PrepAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                PrepAiMainApp(
                    viewModel = viewModel,
                    currentScreen = currentScreen
                )
            }
        }
    }
}

@Composable
fun PrepAiMainApp(
    viewModel: PrepAiViewModel,
    currentScreen: AppScreen
) {
    // Back navigation handling
    BackHandler(enabled = currentScreen != AppScreen.LANDING && currentScreen != AppScreen.STUDENT_DASHBOARD && currentScreen != AppScreen.SPLASH) {
        when (currentScreen) {
            AppScreen.ONBOARDING -> viewModel.navigateTo(AppScreen.LANDING)
            AppScreen.LOGIN -> viewModel.navigateTo(AppScreen.ONBOARDING)
            AppScreen.SIGN_UP -> viewModel.navigateTo(AppScreen.LOGIN)
            AppScreen.FORGOT_PASSWORD -> viewModel.navigateTo(AppScreen.LOGIN)
            AppScreen.ACTIVE_EXAM -> viewModel.navigateTo(AppScreen.MOCK_EXAMS)
            AppScreen.EXAM_RESULT -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            AppScreen.ADMIN_DASHBOARD -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            AppScreen.PROFILE -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            AppScreen.STUDY_PLANNER -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            AppScreen.ANALYTICS -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
            else -> viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD)
        }
    }

    val isAuthOrSplash = currentScreen in listOf(
        AppScreen.SPLASH,
        AppScreen.ONBOARDING,
        AppScreen.LOGIN,
        AppScreen.SIGN_UP,
        AppScreen.FORGOT_PASSWORD,
        AppScreen.LANDING
    )
    val showHeader = !isAuthOrSplash && currentScreen != AppScreen.ACTIVE_EXAM
    val showBottomNav = !isAuthOrSplash && currentScreen != AppScreen.ACTIVE_EXAM && currentScreen != AppScreen.EXAM_RESULT

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showHeader) {
                AppHeader(viewModel = viewModel)
            }
        },
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    viewModel = viewModel,
                    currentScreen = currentScreen
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            AppScreen.SPLASH -> SplashScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.SIGN_UP -> SignUpScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.LANDING -> LandingScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.AUTH -> LoginScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            AppScreen.STUDENT_DASHBOARD -> StudentDashboardScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.PRACTICE_CENTER -> PracticeCenterScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.MOCK_EXAMS -> MockExamsScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.ACTIVE_EXAM -> ActiveExamScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize().padding(innerPadding))
            AppScreen.EXAM_RESULT -> ExamResultScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.AI_COPILOT -> AiCopilotScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.WEAKNESS_DETECTOR -> WeaknessDetectorScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.STUDY_PLANNER -> StudyPlannerScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.JOB_CIRCULARS -> JobCircularsScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.ANALYTICS -> WeaknessDetectorScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel, modifier = contentModifier)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = contentModifier)
        }
    }
}
