package com.example.ui.screens.student

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PracticeConfig
import com.example.data.model.PracticeResult
import com.example.data.model.Question
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun PracticeCenterScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val isPracticeModeActive by viewModel.isPracticeModeActive.collectAsState()
    val practiceResult by viewModel.practiceResult.collectAsState()

    if (isPracticeModeActive) {
        ActivePracticeSessionView(
            viewModel = viewModel,
            isBangla = isBangla,
            modifier = modifier
        )
    } else {
        QuestionBankExplorerView(
            viewModel = viewModel,
            isBangla = isBangla,
            modifier = modifier
        )
    }

    practiceResult?.let { result ->
        PracticeResultDialog(
            result = result,
            isBangla = isBangla,
            onDismiss = {
                viewModel.clearPracticeResult()
                viewModel.exitPracticeMode()
            }
        )
    }
}

@Composable
private fun QuestionBankExplorerView(
    viewModel: PrepAiViewModel,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    val allQuestions by viewModel.allQuestions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val spacedItems by viewModel.spacedRepetitionItems.collectAsState()

    var showOnlyBookmarks by remember { mutableStateOf(false) }
    var showCustomPracticeDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "BCS", "BANK", "PRIMARY")
    val subjects = listOf("ALL", "Bangla", "English", "Mathematics", "Bangladesh Affairs", "ICT")

    val dueSpacedCount = spacedItems.size

    val filteredQuestions = allQuestions.filter { q ->
        val matchesCategory = selectedCategory == "ALL" || q.examCategory.equals(selectedCategory, ignoreCase = true)
        val matchesSubject = selectedSubject == "ALL" || q.subject.equals(selectedSubject, ignoreCase = true)
        val matchesBookmarks = !showOnlyBookmarks || q.isBookmarked
        val matchesSearch = searchQuery.isBlank() ||
                q.questionBn.contains(searchQuery, ignoreCase = true) ||
                q.questionEn.contains(searchQuery, ignoreCase = true) ||
                q.topic.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSubject && matchesBookmarks && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quick Action Launchers (Custom Practice, Spaced Revision, Fix Weakness)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = if (isBangla) "ইন্টেলিজেন্ট প্র্যাকটিস মোড" else "Intelligent Practice Drills",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCustomPracticeDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_custom_practice"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBangla) "কাস্টম টেস্ট" else "Custom Drill",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { viewModel.startSpacedRevisionPractice() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("btn_spaced_revision"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBangla) "স্পেসড ($dueSpacedCount)" else "Spaced ($dueSpacedCount)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { viewModel.startFixMyWeaknessPractice() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_fix_weakness"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBangla) "দুর্বলতা কাটান" else "Fix Weakness",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Tab Row: All Questions vs Bookmarks
        TabRow(
            selectedTabIndex = if (showOnlyBookmarks) 1 else 0,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = !showOnlyBookmarks,
                onClick = { showOnlyBookmarks = false },
                text = { Text(if (isBangla) "সকল প্রশ্নব্যাংক (${allQuestions.size})" else "Question Bank (${allQuestions.size})") }
            )
            Tab(
                selected = showOnlyBookmarks,
                onClick = { showOnlyBookmarks = true },
                text = { Text(if (isBangla) "বুকমার্কসমূহ" else "Bookmarks") }
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("practice_search_field"),
            placeholder = { Text(if (isBangla) "প্রশ্ন, টপিক বা সাল দিয়ে খুঁজুন..." else "Search question or topic...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Exam Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setCategoryFilter(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Subject Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subjects) { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { viewModel.setSubjectFilter(subject) },
                    label = { Text(subject) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        if (filteredQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBangla) "কোনো প্রশ্ন পাওয়া যায়নি" else "No questions found matching criteria",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredQuestions, key = { it.id }) { question ->
                    InteractivePracticeCard(
                        question = question,
                        isBangla = isBangla,
                        onToggleBookmark = { viewModel.toggleBookmark(question) }
                    )
                }
            }
        }
    }

    if (showCustomPracticeDialog) {
        CustomPracticeConfigDialog(
            isBangla = isBangla,
            onDismiss = { showCustomPracticeDialog = false },
            onConfirm = { config ->
                showCustomPracticeDialog = false
                viewModel.startCustomPractice(config)
            }
        )
    }
}

@Composable
private fun ActivePracticeSessionView(
    viewModel: PrepAiViewModel,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.practiceQuestions.collectAsState()
    val currentIndex by viewModel.practiceCurrentIndex.collectAsState()
    val selectedAnswers by viewModel.practiceSelectedAnswers.collectAsState()
    val showExplanation by viewModel.practiceShowExplanation.collectAsState()

    val currentQuestion = questions.getOrNull(currentIndex)
    val progress = if (questions.isNotEmpty()) (currentIndex + 1).toFloat() / questions.size else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Session Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "অনুশীলন সেশন" else "Practice Drill Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.exitPracticeMode() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isBangla) "বাহির হন" else "Exit", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { viewModel.finishPracticeSession() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isBangla) "সম্পন্ন" else "Finish", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Active Question Card & Option Area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (currentQuestion != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${currentQuestion.subject} • ${currentQuestion.topic}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleBookmark(currentQuestion) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentQuestion.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentQuestion.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBangla) currentQuestion.questionBn else currentQuestion.questionEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val options = if (isBangla) currentQuestion.optionsBn else currentQuestion.optionsEn
                        val prefixes = listOf("ক", "খ", "গ", "ঘ")
                        val enPrefixes = listOf("A", "B", "C", "D")
                        val selectedIndex = selectedAnswers[currentIndex] ?: -1
                        val isAnswered = selectedIndex != -1

                        options.forEachIndexed { optIndex, optionText ->
                            val isSelected = selectedIndex == optIndex
                            val isCorrect = optIndex == currentQuestion.correctIndex

                            val borderColor = when {
                                isAnswered && isCorrect -> MaterialTheme.colorScheme.primary
                                isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            }

                            val bgColor = when {
                                isAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .clickable(enabled = !isAnswered) {
                                        viewModel.selectPracticeAnswer(currentIndex, optIndex)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isBangla) prefixes.getOrElse(optIndex) { "${optIndex + 1}" } else enPrefixes.getOrElse(optIndex) { "${optIndex + 1}" },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isAnswered) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Close, contentDescription = "Wrong", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation Accordion
                        if (showExplanation && isAnswered) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBangla) "বিশ্লেষণ ও সঠিক সমাধান" else "Detailed Explanation & AI Insights",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isBangla) currentQuestion.explanationBn else currentQuestion.explanationEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (currentQuestion.aiShortcut.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = "💡 ${currentQuestion.aiShortcut}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.previousPracticeQuestion() },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "পূর্ববর্তী" else "Previous")
                }

                if (currentIndex < questions.size - 1) {
                    Button(
                        onClick = { viewModel.nextPracticeQuestion() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isBangla) "পরবর্তী" else "Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { viewModel.finishPracticeSession() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (isBangla) "ফলাফল দেখুন" else "View Results")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomPracticeConfigDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PracticeConfig) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("BCS") }
    var selectedSubject by remember { mutableStateOf("ALL") }
    var questionCount by remember { mutableIntStateOf(10) }
    var difficulty by remember { mutableStateOf("ALL") }

    val categories = listOf("BCS", "BANK", "PRIMARY")
    val subjects = listOf("ALL", "Bangla", "English", "Mathematics", "Bangladesh Affairs", "ICT")
    val countOptions = listOf(10, 20, 30, 50)
    val difficulties = listOf("ALL", "EASY", "MEDIUM", "HARD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "কাস্টম প্র্যাকটিস কনফিগার করুন" else "Configure Custom Practice Drill",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBangla) "পরীক্ষার ক্যাটাগরি:" else "Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text(
                    text = if (isBangla) "বিষয় নির্বাচন করুন:" else "Subject:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) }
                        )
                    }
                }

                Text(
                    text = if (isBangla) "প্রশ্নের সংখ্যা:" else "Question Count:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(countOptions) { cnt ->
                        FilterChip(
                            selected = questionCount == cnt,
                            onClick = { questionCount = cnt },
                            label = { Text("$cnt") }
                        )
                    }
                }

                Text(
                    text = if (isBangla) "কঠিনতা:" else "Difficulty Level:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(difficulties) { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        PracticeConfig(
                            examCategory = selectedCategory,
                            subject = selectedSubject,
                            questionCount = questionCount,
                            difficulty = difficulty
                        )
                    )
                }
            ) {
                Text(if (isBangla) "অনুশীলন শুরু করুন" else "Start Practice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
private fun PracticeResultDialog(
    result: PracticeResult,
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "অনুশীলন ফলাফল ও পারফরম্যান্স" else "Practice Performance Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Score", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${result.score}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Accuracy", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${result.accuracyPercent}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Correct: ${result.correctCount}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Wrong: ${result.wrongCount}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Skipped: ${result.skippedCount}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }

                if (result.subjectBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBangla) "বিষয়ভিত্তিক স্কোর:" else "Subject Breakdown:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    result.subjectBreakdown.forEach { (subj, pair) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = subj, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${pair.first}/${pair.second} (${if (pair.second > 0) (pair.first * 100 / pair.second) else 0}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isBangla) "ঠিক আছে" else "Done")
            }
        }
    )
}

@Composable
private fun InteractivePracticeCard(
    question: Question,
    isBangla: Boolean,
    onToggleBookmark: () -> Unit
) {
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header tags: Category & Subject, Previous Year, Bookmark button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = question.subject,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (question.previousYearTag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = question.previousYearTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (question.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = if (isBangla) question.questionBn else question.questionEn,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options list
            val options = if (isBangla) question.optionsBn else question.optionsEn
            val prefixes = listOf("ক", "খ", "গ", "ঘ")
            val enPrefixes = listOf("A", "B", "C", "D")

            options.forEachIndexed { index, optionText ->
                val isSelected = selectedOptionIndex == index
                val isAnswered = selectedOptionIndex != -1
                val isCorrect = index == question.correctIndex

                val borderColor = when {
                    isAnswered && isCorrect -> MaterialTheme.colorScheme.primary
                    isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                }

                val bgColor = when {
                    isAnswered && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable(enabled = !isAnswered) {
                            selectedOptionIndex = index
                            showExplanation = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBangla) prefixes.getOrElse(index) { "${index + 1}" } else enPrefixes.getOrElse(index) { "${index + 1}" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isAnswered) {
                            if (isCorrect) {
                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            } else if (isSelected) {
                                Icon(Icons.Default.Close, contentDescription = "Wrong", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Explanation & AI Shortcut accordion
            if (selectedOptionIndex != -1) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "বিস্তারিত সমাধান ও AI শর্টকাট" else "Detailed Explanation & AI Shortcut",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) question.explanationBn else question.explanationEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (question.aiShortcut.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "💡 ${question.aiShortcut}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
