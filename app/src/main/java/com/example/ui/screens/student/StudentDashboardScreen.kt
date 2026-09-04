package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyTask
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun StudentDashboardScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val userProfile by viewModel.userProfile.collectAsState()
    val tasks by viewModel.studyTasks.collectAsState()
    val weaknesses by viewModel.weaknessItems.collectAsState()

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size.coerceAtLeast(1)
    val progressPercent = ((completedTasksCount.toFloat() / totalTasksCount) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Welcome & Target Profile
        item {
            ProfileSummaryHeader(
                name = userProfile?.fullName ?: "Tanvir Ahmed",
                targetExam = userProfile?.targetExam ?: "46th BCS & Bangladesh Bank AD",
                readinessScore = userProfile?.readinessScore ?: 78,
                streakDays = userProfile?.streakDays ?: 12,
                isBangla = isBangla
            )
        }

        // Daily Progress & Streak Widget
        item {
            DailyProgressWidget(
                progressPercent = progressPercent,
                completedTasks = completedTasksCount,
                totalTasks = totalTasksCount,
                dailyHours = userProfile?.dailyStudyHours ?: 4,
                isBangla = isBangla
            )
        }

        // AI Recommendation Alert Card (e.g. Math dropped by 12%)
        item {
            AiRecommendationBanner(
                weakness = weaknesses.firstOrNull(),
                onFixClick = { viewModel.navigateTo(AppScreen.WEAKNESS_DETECTOR) },
                isBangla = isBangla
            )
        }

        // Quick Launch Hub (Practice, Exams, AI Copilot, Circulars)
        item {
            QuickLaunchHub(viewModel = viewModel, isBangla = isBangla)
        }

        // Today's Study Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "আজকের স্টাডি টাস্ক" else "Today's Study Tasks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$completedTasksCount/$totalTasksCount Done",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(tasks) { task ->
            StudyTaskItem(
                task = task,
                isBangla = isBangla,
                onToggle = { viewModel.toggleTask(task.id, task.isCompleted) }
            )
        }

        // Subject Readiness Breakdown
        item {
            SubjectReadinessSection(isBangla = isBangla)
        }
    }
}

@Composable
private fun ProfileSummaryHeader(
    name: String,
    targetExam: String,
    readinessScore: Int,
    streakDays: Int,
    isBangla: Boolean
) {
    // Featured Hero Card mirroring "Artistic Flair" signature layout
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
        ) {
            // Ambient artistic glow circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 220.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-20).dp, y = 90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.06f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Editorial Tracked Overline
                Text(
                    text = if (isBangla) "টার্গেট পরীক্ষা ও প্রস্তুতি" else "TARGET EXAM & MASTERY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Serif Italic Display Name
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Target exam & badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = targetExam,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "ব্যক্তিগতকৃত AI অধ্যয়ন পরিকল্পনা" else "Personalized AI Study Path",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    // Streak & Readiness badges with artistic squircle button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak Pill
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streakDays d",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Readiness Pill
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = "Readiness",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$readinessScore%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyProgressWidget(
    progressPercent: Int,
    completedTasks: Int,
    totalTasks: Int,
    dailyHours: Int,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "আজকের অগ্রগতি" else "TODAY'S STUDY PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isBangla) "দৈনিক লক্ষ্য: $dailyHours ঘণ্টা" else "Daily Target: ${dailyHours}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isBangla) "$completedTasks সম্পন্ন / মোট $totalTasks" else "$completedTasks completed of $totalTasks",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AiRecommendationBanner(
    weakness: com.example.data.model.WeaknessItem?,
    onFixClick: () -> Unit,
    isBangla: Boolean
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBangla) "AI রিকমেন্ডেশন: গণিতের এক্যুরেসি ড্রপ করেছে" else "AI ACCURACY ALERT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isBangla)
                    (weakness?.recommendationBn ?: "এই সপ্তাহে আপনার গণিতের সঠিকতার হার ১২% কমেছে। শতকরা, অনুপাত ও লাভ-ক্ষতির শর্টকাট টেকনিকে বিশেষ জোর দিন।")
                else
                    (weakness?.recommendationEn ?: "Your Mathematics accuracy dropped by 12% this week. Recommended: Practice Ratio, Percentage, Profit and Loss."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onFixClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fix_my_weakness_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "দুর্বলতা ফিক্স করুন (FIX MY WEAKNESS)" else "FIX MY WEAKNESS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun QuickLaunchHub(viewModel: PrepAiViewModel, isBangla: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "কুইক একশন হাব" else "QUICK LAUNCH HUB",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isBangla) "সবগুলো দেখুন" else "View All",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "অনুশীলন" else "Practice",
                subtitle = if (isBangla) "টপিকভিত্তিক" else "Topic Drill",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = MaterialTheme.colorScheme.primary,
                onClick = { viewModel.navigateTo(AppScreen.PRACTICE_CENTER) }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "মডেল টেস্ট" else "Mock Exam",
                subtitle = if (isBangla) "লাইভ র‍্যাংকিং" else "Timed BCS",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { viewModel.navigateTo(AppScreen.MOCK_EXAMS) }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "AI টিউটর" else "AI Tutor",
                subtitle = if (isBangla) "তাৎক্ষণিক সাহায্য" else "24/7 Solve",
                icon = Icons.Default.AutoAwesome,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { viewModel.navigateTo(AppScreen.AI_COPILOT) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StudyTaskItem(
    task: StudyTask,
    isBangla: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Task Status",
                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) task.titleBn else task.titleEn,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = "⏱ ${task.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${task.subject}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectReadinessSection(isBangla: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBangla) "বিষয়ভিত্তিক প্রস্তুতি রেটিং" else "Subject Readiness Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            SubjectProgressRow(name = if (isBangla) "বাংলা ভাষা ও সাহিত্য" else "Bangla Language & Literature", percent = 82)
            Spacer(modifier = Modifier.height(8.dp))
            SubjectProgressRow(name = if (isBangla) "বাংলাদেশ বিষয়াবলি" else "Bangladesh Affairs", percent = 76)
            Spacer(modifier = Modifier.height(8.dp))
            SubjectProgressRow(name = if (isBangla) "গাণিতিক যুক্তি ও মানসিক দক্ষতা" else "Mathematics & Mental Ability", percent = 54)
            Spacer(modifier = Modifier.height(8.dp))
            SubjectProgressRow(name = if (isBangla) "ইংরেজি ভাষা ও ব্যাকরণ" else "English Language & Grammar", percent = 68)
            Spacer(modifier = Modifier.height(8.dp))
            SubjectProgressRow(name = if (isBangla) "আইসিটি ও কম্পিউটার বিজ্ঞান" else "ICT & General Science", percent = 74)
        }
    }
}

@Composable
private fun SubjectProgressRow(name: String, percent: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (percent < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (percent < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
