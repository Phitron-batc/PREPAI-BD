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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PrepAiViewModel
import java.util.Locale

@Composable
fun ActiveExamScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val examState by viewModel.activeExamState.collectAsState()
    val exam = examState.exam ?: return

    var showSubmitDialog by remember { mutableStateOf(false) }

    val remainingMinutes = examState.remainingSeconds / 60
    val remainingSecs = examState.remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", remainingMinutes, remainingSecs)

    val currentQuestion = examState.questions.getOrNull(examState.currentIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header: Exam Title & Countdown Timer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) exam.titleBn else exam.titleEn,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        text = "Q ${examState.currentIndex + 1} of ${examState.questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Timer Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (examState.remainingSeconds < 300) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Timer",
                            tint = if (examState.remainingSeconds < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (examState.remainingSeconds < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Question Palette (Scrollable row of question numbers)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(examState.questions) { index, _ ->
                val isAnswered = examState.selectedAnswers.containsKey(index)
                val isMarkedForReview = examState.markedForReview.contains(index)
                val isCurrent = examState.currentIndex == index

                val bgColor = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isMarkedForReview -> MaterialTheme.colorScheme.tertiary
                    isAnswered -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surface
                }

                val textColor = when {
                    isCurrent || isMarkedForReview || isAnswered -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable { viewModel.setExamQuestionIndex(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                }
            }
        }

        // Current Question Card & Options
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
                                    text = currentQuestion.subject,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Mark for Review action
                            val isMarked = examState.markedForReview.contains(examState.currentIndex)
                            TextButton(onClick = { viewModel.toggleMarkForReview() }) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (isMarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMarked) "Marked" else "Review",
                                    color = if (isMarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBangla) currentQuestion.questionBn else currentQuestion.questionEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options
                        val options = if (isBangla) currentQuestion.optionsBn else currentQuestion.optionsEn
                        val prefixes = listOf("ক", "খ", "গ", "ঘ")
                        val enPrefixes = listOf("A", "B", "C", "D")
                        val selectedOption = examState.selectedAnswers[examState.currentIndex]

                        options.forEachIndexed { optIndex, optText ->
                            val isSelected = selectedOption == optIndex

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.selectExamOption(optIndex) }
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isBangla) prefixes[optIndex] else enPrefixes[optIndex],
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar: Prev, Next, Submit
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.setExamQuestionIndex(examState.currentIndex - 1) },
                    enabled = examState.currentIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "আগের প্রশ্ন" else "Prev")
                }

                Button(
                    onClick = { showSubmitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("submit_exam_button")
                ) {
                    Text(if (isBangla) "সাবমিট করুন" else "Submit Exam")
                }

                Button(
                    onClick = { viewModel.setExamQuestionIndex(examState.currentIndex + 1) },
                    enabled = examState.currentIndex < examState.questions.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isBangla) "পরের প্রশ্ন" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val answeredCount = examState.selectedAnswers.size
        val totalCount = examState.questions.size
        val unattemptedCount = totalCount - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(if (isBangla) "পরীক্ষা সাবমিট নিশ্চিতকরণ" else "Confirm Exam Submission") },
            text = {
                Column {
                    Text(
                        text = if (isBangla)
                            "আপনি $totalCount টির মধ্যে $answeredCount টি প্রশ্নের উত্তর দিয়েছেন। $unattemptedCount টি প্রশ্ন অনুত্তরিত রয়েছে।"
                        else
                            "You have answered $answeredCount of $totalCount questions. $unattemptedCount remain unanswered."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "আপনি কি নিশ্চিতভাবে সাবমিট করতে চান?" else "Are you sure you want to end and submit the exam?",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isBangla) "হ্যাঁ, সাবমিট করুন" else "Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text(if (isBangla) "ফিরে যান" else "Continue Exam")
                }
            }
        )
    }
}
