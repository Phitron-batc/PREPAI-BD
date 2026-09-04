package com.example.ui.screens.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun LandingScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Section
        item {
            HeroSection(viewModel = viewModel, isBangla = isBangla)
        }

        // Exam Badges Row
        item {
            ExamCategoriesBar(isBangla = isBangla)
        }

        // Problem vs Solution Section
        item {
            ProblemSolutionSection(isBangla = isBangla)
        }

        // Core Pillars / Features
        item {
            CoreFeaturesSection(viewModel = viewModel, isBangla = isBangla)
        }

        // Live Platform Metrics
        item {
            PlatformMetricsSection(isBangla = isBangla)
        }

        // Pricing Packages Architecture
        item {
            PricingPreviewSection(viewModel = viewModel, isBangla = isBangla)
        }

        // Student Reviews / Social Proof
        item {
            TestimonialsSection(isBangla = isBangla)
        }

        // Final Call to Action
        item {
            CtaBanner(viewModel = viewModel, isBangla = isBangla)
        }
    }
}

@Composable
private fun HeroSection(viewModel: PrepAiViewModel, isBangla: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Tagline chip
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "বাংলাদেশের প্রথম AI-চালিত জব প্রিপারেশন প্ল্যাটফর্ম" else "Bangladesh's #1 AI Job Prep Platform",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Display Headline
            Text(
                text = if (isBangla) "স্মার্ট প্রস্তুতি,\nসফল ক্যারিয়ার।" else "Prepare Smarter.\nBuild Your Career.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 40.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = if (isBangla)
                    "বিসিএস, বাংলাদেশ ব্যাংক এডি, প্রাথমিক শিক্ষক ও সরকারি চাকরির পূর্ণাঙ্গ সিলেবাস, এআই টিউটর, অ্যাডাপ্টিভ মডেল টেস্ট ও দুর্বলতা ডিটেক্টর — এক প্ল্যাটফর্মে।"
                else
                    "Complete syllabus intelligence, AI tutor, adaptive mock exams, and weakness detector for BCS, Bank, Primary & Govt jobs in Bangladesh.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Illustration Banner
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_prepai_banner),
                    contentDescription = "PREPAI BD AI Platform Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("hero_start_prep_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isBangla) "প্রস্তুতি শুরু করুন" else "Start Free Prep",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                OutlinedButton(
                    onClick = {
                        viewModel.setUserRole(UserRole.ADMIN)
                        viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("hero_admin_demo_button"),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (isBangla) "অ্যাডমিন কনসোল" else "Admin Portal",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamCategoriesBar(isBangla: Boolean) {
    val exams = listOf(
        "৪৬তম বিসিএস প্রিলি",
        "বাংলাদেশ ব্যাংক AD",
        "সমন্বিত ৯ ব্যাংক",
        "প্রাথমিক সহকারী শিক্ষক",
        "NTRCA শিক্ষক নিবন্ধন",
        "রেলওয়ে স্টেশন মাস্টার"
    )

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = if (isBangla) "টার্গেট পরীক্ষা নির্বাচন করুন" else "Supported Competitive Exams",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exams) { exam ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = exam,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemSolutionSection(isBangla: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (isBangla) "কেন প্রথাগত গাইড বই যথেষ্ট নয়?" else "The Traditional Prep Problem",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            FeatureBullet(
                icon = Icons.Default.Speed,
                title = if (isBangla) "অন্ধভাবে হাজার হাজার এমসিকিউ মুখস্থ নয়" else "No Blind Memorization",
                desc = if (isBangla) "কোন বিষয়টি আপনি পারেন আর কোনটিতে বারবার ভুল করছেন, PrepAI ঠিক সেখানেই ফোকাস করায়।" else "PrepAI pinpoints your exact drop in accuracy across Math, English or Constitution."
            )
            Spacer(modifier = Modifier.height(10.dp))
            FeatureBullet(
                icon = Icons.Default.Psychology,
                title = if (isBangla) "২৪/৭ ব্যক্তিগত AI মেন্টর" else "24/7 AI Personal Mentor",
                desc = if (isBangla) "যেকোনো প্রশ্নের ব্যাখ্যা, শর্টকাট সূত্র বা ব্যাকরণ নিয়ম তাৎক্ষণিক বাংলায় বুঝে নিন।" else "Instant concept breakdown, mathematical proof, and exam shortcuts on demand."
            )
            Spacer(modifier = Modifier.height(10.dp))
            FeatureBullet(
                icon = Icons.Default.Assignment,
                title = if (isBangla) "স্মার্ট রিভিশন (Spaced Repetition)" else "Spaced Repetition Engine",
                desc = if (isBangla) "১ম, ৩য়, ৭ম ও ১৫তম দিনে ভুলে যাওয়া বিষয়গুলো স্বয়ংক্রিয়ভাবে রিভিশনে মনে করিয়ে দেয়।" else "Smart review algorithm schedules previous errors right before you forget them."
            )
        }
    }
}

@Composable
private fun FeatureBullet(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CoreFeaturesSection(viewModel: PrepAiViewModel, isBangla: Boolean) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = if (isBangla) "প্ল্যাটফর্মের প্রধান সুবিধাসমূহ" else "Core Startup Capabilities",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "স্মার্ট প্র্যাকটিস" else "Smart Practice",
                subtitle = if (isBangla) "বিষয়ভিত্তিক ফিল্টার ও শর্টকাট" else "Topic & Year Filtering",
                icon = Icons.Default.AutoAwesome,
                onClick = { viewModel.navigateTo(AppScreen.PRACTICE_CENTER) }
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "মডেল টেস্ট" else "Mock Exams",
                subtitle = if (isBangla) "লাইভ টাইমার ও নেগেটিভ মার্কিং" else "Timed Adaptive Tests",
                icon = Icons.Default.Assignment,
                onClick = { viewModel.navigateTo(AppScreen.MOCK_EXAMS) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "দুর্বলতা ফিক্সার" else "Weakness Fixer",
                subtitle = if (isBangla) "ভুল উত্তরের স্বয়ংক্রিয় ডায়াগনসিস" else "Mistake Analytics",
                icon = Icons.Default.Speed,
                onClick = { viewModel.navigateTo(AppScreen.WEAKNESS_DETECTOR) }
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = if (isBangla) "জব সার্কুলার" else "Job Circulars",
                subtitle = if (isBangla) "যোগ্যতার ভিত্তিতে প্রোফাইল ম্যাচ" else "Eligibility Matching",
                icon = Icons.Default.School,
                onClick = { viewModel.navigateTo(AppScreen.JOB_CIRCULARS) }
            )
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlatformMetricsSection(isBangla: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (isBangla) "পরিসংখ্যান ও প্রভাব" else "Platform Reach & Impact",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                MetricItem(number = "15,000+", label = if (isBangla) "যাচাইকৃত প্রশ্ন" else "Verified MCQs")
                MetricItem(number = "98.4%", label = if (isBangla) "সিলেবাস কভারেজ" else "Syllabus Match")
                MetricItem(number = "+18%", label = if (isBangla) "গড় স্কোর বৃদ্ধি" else "Avg Score Gain")
            }
        }
    }
}

@Composable
private fun MetricItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PricingPreviewSection(viewModel: PrepAiViewModel, isBangla: Boolean) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = if (isBangla) "সাবস্ক্রিপশন প্ল্যানসমূহ" else "Flexible Monetization Plans",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (isBangla) "বিকাশ ও নগদ সাপোর্টেড পেমেন্ট অ্যাবস্ট্রাকশন লেয়ার" else "Compatible with Bangladesh bKash & Nagad Gateways",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Premium Package Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "PREPAI প্রো মেম্বারশিপ" else "PREPAI PRO (All Access)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = if (isBangla) "জনপ্রিয়" else "Popular",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "৳ ৩৫০",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBangla) " / মাস" else " / month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                PlanPerk(text = if (isBangla) "আনলিমিটেড এআই টিউটর ও প্রশ্ন সলভার" else "Unlimited AI Copilot & Concept Explainer")
                PlanPerk(text = if (isBangla) "সকল পূর্ণাঙ্গ বিসিএস ও ব্যাংক মডেল টেস্ট" else "All Full-length BCS & Bank Mock Tests")
                PlanPerk(text = if (isBangla) "দুর্বলতা ডিটেকশন ও স্পেসড রিপিটেশন রিভিশন" else "Weakness Diagnosis & Spaced Repetition")
                PlanPerk(text = if (isBangla) "অফলাইন প্র্যাকটিস মোড ও পিডিএফ ডাউনলোড" else "Offline Practice Mode & Notes")

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isBangla) "ফ্রি ট্রায়াল শুরু করুন" else "Start Free 7-Day Trial")
                }
            }
        }
    }
}

@Composable
private fun PlanPerk(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TestimonialsSection(isBangla: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "4.9/5 Student Rating", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isBangla)
                    "“বিসিএস প্রিলির গণিত আর ইংরেজি গ্রামারে আমি সবসময় আতঙ্কে থাকতাম। PrepAI-এর দুর্বলতা ফিক্সার ঠিক ধরে দিয়েছে কোথায় সমস্যা। এর এআই টিউটরের বাংলা ব্যাখ্যা সত্যিই অসাধারণ!”"
                else
                    "“I used to struggle with Math and Grammar. PrepAI diagnosed my weak topics instantly and the AI Tutor's step-by-step shortcuts boosted my confidence.”",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBangla) "— মেহরাব হোসেন, ৪৬তম বিসিএস ক্যাডার প্রত্যাশী" else "— Mehrab Hossain, 46th BCS Candidate",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CtaBanner(viewModel: PrepAiViewModel, isBangla: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isBangla) "আপনার স্বপ্নের ক্যারিয়ার গড়ার সময় এখনই" else "Ready to Ace Your Competitive Exam?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBangla) "ফ্রি অ্যাকাউন্ট তৈরি করে আজই নিজের প্রস্তুতি লেভেল জেনে নিন।" else "Join thousands of candidates preparing smarter across Bangladesh.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { viewModel.navigateTo(AppScreen.STUDENT_DASHBOARD) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = if (isBangla) "ড্যাশবোর্ডে প্রবেশ করুন" else "Go to Student Dashboard",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
