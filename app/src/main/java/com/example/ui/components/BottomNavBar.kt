package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun BottomNavBar(
    viewModel: PrepAiViewModel,
    currentScreen: AppScreen,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val currentRole = viewModel.userProfile.value?.role ?: UserRole.STUDENT

    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    NavigationBar(
        modifier = modifier.drawBehind {
            drawLine(
                color = outlineColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        val studentNavColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        val adminNavColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        if (currentRole == UserRole.ADMIN) {
            // Admin bottom items
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_DASHBOARD && viewModel.adminActiveTab.value == "OVERVIEW",
                onClick = {
                    viewModel.setAdminTab("OVERVIEW")
                    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                label = { Text(if (isBangla) "ওভারভিউ" else "Overview", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = adminNavColors,
                modifier = Modifier.testTag("nav_admin_overview")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_DASHBOARD && viewModel.adminActiveTab.value == "QUESTIONS",
                onClick = {
                    viewModel.setAdminTab("QUESTIONS")
                    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Questions") },
                label = { Text(if (isBangla) "প্রশ্নব্যাংক" else "Questions", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = adminNavColors,
                modifier = Modifier.testTag("nav_admin_questions")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_DASHBOARD && viewModel.adminActiveTab.value == "AI_QUEUE",
                onClick = {
                    viewModel.setAdminTab("AI_QUEUE")
                    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Queue") },
                label = { Text(if (isBangla) "AI কিউ" else "AI Queue", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = adminNavColors,
                modifier = Modifier.testTag("nav_admin_ai_queue")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.ADMIN_DASHBOARD && viewModel.adminActiveTab.value == "DOCUMENTS",
                onClick = {
                    viewModel.setAdminTab("DOCUMENTS")
                    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                },
                icon = { Icon(Icons.Default.Assignment, contentDescription = "Documents") },
                label = { Text(if (isBangla) "নথি / RAG" else "Docs", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = adminNavColors,
                modifier = Modifier.testTag("nav_admin_docs")
            )
        } else {
            // Student bottom items
            NavigationBarItem(
                selected = currentScreen == AppScreen.STUDENT_DASHBOARD,
                onClick = { viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD) },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "ড্যাশবোর্ড" else "Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_dashboard")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.PRACTICE_CENTER,
                onClick = { viewModel.navigateTo(AppScreen.PRACTICE_CENTER) },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Practice", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "অনুশীলন" else "Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_practice")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.MOCK_EXAMS,
                onClick = { viewModel.navigateTo(AppScreen.MOCK_EXAMS) },
                icon = { Icon(Icons.Default.Assignment, contentDescription = "Exams", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "মডেল টেস্ট" else "Exams", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_exams")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.AI_COPILOT,
                onClick = { viewModel.navigateTo(AppScreen.AI_COPILOT) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Copilot", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "AI টিউটর" else "Copilot", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_ai_copilot")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.WEAKNESS_DETECTOR || currentScreen == AppScreen.STUDY_PLANNER,
                onClick = { viewModel.navigateTo(AppScreen.WEAKNESS_DETECTOR) },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Analytics", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "দুর্বলতা" else "Weakness", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_analytics")
            )
            NavigationBarItem(
                selected = currentScreen == AppScreen.JOB_CIRCULARS,
                onClick = { viewModel.navigateTo(AppScreen.JOB_CIRCULARS) },
                icon = { Icon(Icons.Default.BusinessCenter, contentDescription = "Jobs", modifier = Modifier.size(22.dp)) },
                label = { Text(if (isBangla) "সার্কুলার" else "Jobs", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = studentNavColors,
                modifier = Modifier.testTag("nav_student_jobs")
            )
        }
    }
}
