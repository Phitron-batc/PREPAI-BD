package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

data class OnboardingStep(
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val tagEn: String,
    val tagBn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun OnboardingScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            titleEn = "Adaptive Question Bank & Instant AI Verification",
            titleBn = "স্মার্ট প্রশ্নব্যাংক ও তাৎক্ষণিক AI ব্যাখ্যা",
            descriptionEn = "Practice thousands of syllabus-aligned MCQs for BCS, Bank AD, and Primary exams with step-by-step reasoning and speed shortcuts.",
            descriptionBn = "বিসিএস, বাংলাদেশ ব্যাংক ও প্রাথমিক শিক্ষক নিয়োগের সিলেবাসভিত্তিক প্রশ্ন প্র্যাকটিস করুন ইনস্ট্যান্ট ব্যাখ্যা ও AI শর্টকাট সহ।",
            tagEn = "Topic-Wise Practice",
            tagBn = "বিষয়ভিত্তিক অনুশীলন",
            icon = Icons.Default.MenuBook
        ),
        OnboardingStep(
            titleEn = "Live Exam Simulation with Negative Marking",
            titleBn = "রিয়েল মডেল টেস্ট ও নেগেটিভ মার্কিং",
            descriptionEn = "Experience real examination conditions with timer countdown, question palette, review flags, and standard negative scoring (0.50 / 0.25).",
            descriptionBn = "বিসিএস প্রিলিমিনারি ও ব্যাংক নিয়োগের মতো সঠিক টাইমার, প্রশ্ন প্যালেট ও নেগেটিভ মার্কিং সহ রিয়েলিস্টিক মডেল টেস্ট দিন।",
            tagEn = "Standard Exam Engine",
            tagBn = "পরীক্ষা সিমুলেশন",
            icon = Icons.Default.Assignment
        ),
        OnboardingStep(
            titleEn = "24/7 AI Copilot in 5 Pedagogical Modes",
            titleBn = "২৪/৭ AI টিউটর ও ৫টি শিখন মোড",
            descriptionEn = "Switch freely between Simple Explanation, Exam Shortcuts, Deep Concepts, Socratic Guide, and Real Examples to master tough topics.",
            descriptionBn = "সহজ ভাষা, পরীক্ষার শর্টকাট, গভীর ধারণা, সক্রেটিক গাইড ও বাস্তব উদাহরণ — ৫টি বিশেষ মোডে যেকোনো জটিল কনসেপ্ট সহজে বুঝুন।",
            tagEn = "Pedagogical Modes",
            tagBn = "ব্যক্তিগত এআই গাইড",
            icon = Icons.Default.AutoAwesome
        )
    )

    val currentStep = steps[currentStepIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Skip button & Progress dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.indices.forEach { index ->
                    val isCurrent = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isCurrent) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            TextButton(
                onClick = { viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD) },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = if (isBangla) "স্কিপ করুন" else "Skip",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Center visual card
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon halo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentStep.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Pill Tag
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (isBangla) currentStep.tagBn else currentStep.tagEn,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title with Artistic Serif Italic
                Text(
                    text = if (isBangla) currentStep.titleBn else currentStep.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = if (isBangla) currentStep.descriptionBn else currentStep.descriptionEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom Controls: Previous & Next / Get Started
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (currentStepIndex < steps.size - 1) {
                        currentStepIndex++
                    } else {
                        viewModel.navigateTo(AppScreen.LOGIN)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentStepIndex < steps.size - 1) {
                        if (isBangla) "পরবর্তী ধাপ" else "Next Step"
                    } else {
                        if (isBangla) "লগইন করুন বা শুরু করুন" else "Get Started"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            if (currentStepIndex > 0) {
                OutlinedButton(
                    onClick = { currentStepIndex-- },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(if (isBangla) "আগের ধাপ" else "Previous")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isBangla) "ইতিমধ্যেই অ্যাকাউন্ট আছে? " else "Already have an account? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isBangla) "লগইন করুন" else "Sign In",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.LOGIN) }
                    )
                }
            }
        }
    }
}
