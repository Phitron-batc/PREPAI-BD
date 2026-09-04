package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import com.example.data.model.AiReviewQueueItem
import com.example.data.model.DifficultyLevel
import com.example.data.model.DocumentInfo
import com.example.data.model.Question
import com.example.data.model.ReviewStatus
import com.example.data.model.UserRole
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val activeTab by viewModel.adminActiveTab.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val aiQueue by viewModel.aiReviewQueue.collectAsState()
    val documents by viewModel.adminDocuments.collectAsState()
    val mockExams by viewModel.availableMockExams.collectAsState()
    val jobCirculars by viewModel.jobCirculars.collectAsState()

    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }

    val tabs = listOf(
        "OVERVIEW",
        "QUESTIONS",
        "AI_QUEUE",
        "DOCUMENTS",
        "STUDENTS",
        "EXAMS",
        "CIRCULARS",
        "ANALYTICS",
        "SETTINGS"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "PREPAI অ্যাডমিন ও কনটেন্ট হাব" else "PREPAI Admin & Content Hub",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBangla) "প্রশ্নব্যাংক, AI জেনারেশন কিউ ও ডক পাইপলাইন" else "Manage Questions, AI Generation Queue & RAG Docs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddQuestionDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("admin_add_question_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "নতুন প্রশ্ন" else "Add MCQ")
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 16.dp
                ) {
                    tabs.forEach { tabKey ->
                        Tab(
                            selected = activeTab == tabKey,
                            onClick = { viewModel.setAdminTab(tabKey) },
                            text = {
                                Text(
                                    when (tabKey) {
                                        "OVERVIEW" -> if (isBangla) "ওভারভিউ" else "Overview"
                                        "QUESTIONS" -> if (isBangla) "প্রশ্নব্যাংক (${allQuestions.size})" else "Questions (${allQuestions.size})"
                                        "AI_QUEUE" -> if (isBangla) "AI কিউ (${aiQueue.count { it.status == ReviewStatus.PENDING }})" else "Review Queue (${aiQueue.count { it.status == ReviewStatus.PENDING }})"
                                        "DOCUMENTS" -> if (isBangla) "নথি ও RAG (${documents.size})" else "Docs & RAG (${documents.size})"
                                        "STUDENTS" -> if (isBangla) "শিক্ষার্থী" else "Students"
                                        "EXAMS" -> if (isBangla) "মডেল টেস্ট (${mockExams.size})" else "Mock Exams (${mockExams.size})"
                                        "CIRCULARS" -> if (isBangla) "সার্কুলার (${jobCirculars.size})" else "Circulars (${jobCirculars.size})"
                                        "ANALYTICS" -> if (isBangla) "অ্যানালিটিক্স" else "Analytics"
                                        else -> if (isBangla) "সেটিংস" else "Settings"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTab == tabKey) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (activeTab) {
            "OVERVIEW" -> AdminOverviewTab(
                questionsCount = allQuestions.size,
                pendingQueueCount = aiQueue.count { it.status == ReviewStatus.PENDING },
                docsCount = documents.size,
                isBangla = isBangla
            )
            "QUESTIONS" -> AdminQuestionsTab(
                questions = allQuestions,
                isBangla = isBangla,
                onEdit = { questionToEdit = it },
                onDelete = { viewModel.deleteQuestion(it) }
            )
            "AI_QUEUE" -> AdminAiQueueTab(
                queue = aiQueue,
                isBangla = isBangla,
                onApprove = { viewModel.approveAiQuestion(it) },
                onReject = { viewModel.rejectAiQuestion(it) },
                onRequestRevision = { id, notes -> viewModel.requestAiQuestionRevision(id, notes) }
            )
            "DOCUMENTS" -> AdminDocumentsTab(
                documents = documents,
                isBangla = isBangla,
                onUploadDocument = { title, docType, category, pages, source ->
                    viewModel.uploadAndProcessDocument(title, docType, category, pages, source)
                },
                onDraftWithAi = { doc ->
                    viewModel.draftAiQuestionFromDocument(doc.title, "Syllabus Core", doc.category)
                    viewModel.setAdminActiveTab("AI_QUEUE")
                }
            )
            "STUDENTS" -> AdminStudentsTab(
                viewModel = viewModel,
                isBangla = isBangla
            )
            "EXAMS" -> AdminExamsTab(
                exams = mockExams,
                isBangla = isBangla
            )
            "CIRCULARS" -> AdminCircularsTab(
                circulars = jobCirculars,
                isBangla = isBangla
            )
            "ANALYTICS" -> AdminAnalyticsTab(
                questions = allQuestions,
                isBangla = isBangla
            )
            "SETTINGS" -> AdminSettingsTab(
                viewModel = viewModel,
                isBangla = isBangla
            )
        }
    }

    // Add MCQ Dialog
    if (showAddQuestionDialog) {
        AddQuestionDialog(
            isBangla = isBangla,
            onDismiss = { showAddQuestionDialog = false },
            onAdd = { newQuestion ->
                viewModel.addNewQuestion(newQuestion)
                showAddQuestionDialog = false
            }
        )
    }

    // Edit MCQ Dialog
    if (questionToEdit != null) {
        EditQuestionDialog(
            question = questionToEdit!!,
            isBangla = isBangla,
            onDismiss = { questionToEdit = null },
            onSave = { updatedQuestion ->
                viewModel.updateQuestion(updatedQuestion)
                questionToEdit = null
            }
        )
    }
}

@Composable
private fun AdminOverviewTab(
    questionsCount: Int,
    pendingQueueCount: Int,
    docsCount: Int,
    isBangla: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (isBangla) "প্ল্যাটফর্ম সারাংশ" else "Platform Overview & Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = if (isBangla) "মোট প্রশ্ন" else "Questions",
                    value = "$questionsCount",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    tint = MaterialTheme.colorScheme.primary
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = if (isBangla) "AI কিউ পেন্ডিং" else "AI Review Due",
                    value = "$pendingQueueCount",
                    icon = Icons.Default.AutoAwesome,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = if (isBangla) "ইনজেস্টেড ডক" else "RAG Docs",
                    value = "$docsCount",
                    icon = Icons.Default.Assignment,
                    tint = MaterialTheme.colorScheme.secondary
                )
                AdminStatCard(
                    modifier = Modifier.weight(1f),
                    title = if (isBangla) "অ্যাক্টিভ শিক্ষার্থী" else "Active Students",
                    value = "8,420+",
                    icon = Icons.Default.People,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "অ্যাডমিন নির্দেশিকা ও AI পাইপলাইন" else "Admin Guidelines & AI Ingestion",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla)
                            "১. AI পর্যালোচনায় প্রতিটি স্বয়ংক্রিয়ভাবে তৈরি প্রশ্নের সঠিকতা, ৪টি অপশন এবং পরীক্ষার শর্টকাট সূত্র যাচাই করে 'Approve' চাপুন।\n" +
                                    "২. নতুন বোর্ড পাঠ্যবই বা সরকারি গেজেট আপলোড করলে সিস্টেম ব্যাকগ্রাউন্ডে টেক্সট চাঙ্কিং ও এমসিকিউ এক্সট্র্যাক্ট করে।"
                        else
                            "1. Review pending AI questions for accuracy and syllabus alignment before publishing.\n" +
                                    "2. Ingested government gazettes and textbooks are converted into verified mock exam sets automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminQuestionsTab(
    questions: List<Question>,
    isBangla: Boolean,
    onEdit: (Question) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(questions, key = { it.id }) { q ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${q.examCategory} • ${q.subject}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onEdit(q) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onDelete(q.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = if (isBangla) q.questionBn else q.questionEn, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))

                    Spacer(modifier = Modifier.height(6.dp))
                    val correctOpt = if (isBangla) q.optionsBn.getOrNull(q.correctIndex) else q.optionsEn.getOrNull(q.correctIndex)
                    Text(
                        text = "Correct: ${correctOpt ?: "N/A"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminAiQueueTab(
    queue: List<AiReviewQueueItem>,
    isBangla: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onRequestRevision: (String, String) -> Unit
) {
    val pendingItems = queue.filter { it.status == ReviewStatus.PENDING || it.status == ReviewStatus.NEEDS_EDIT }
    var itemForRevision by remember { mutableStateOf<AiReviewQueueItem?>(null) }
    var revisionNotes by remember { mutableStateOf("") }

    if (pendingItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (isBangla) "AI রিভিউ কিউ সম্পূর্ণ খালি! সব প্রশ্ন পর্যালোচিত।" else "All AI questions reviewed and resolved!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(pendingItems, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "AI Generated • ${item.sourceDocument}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (item.status == ReviewStatus.NEEDS_EDIT) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = item.status.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (item.status == ReviewStatus.NEEDS_EDIT) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBangla) item.question.questionBn else item.question.questionEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val options = if (isBangla) item.question.optionsBn else item.question.optionsEn
                        options.forEachIndexed { idx, opt ->
                            val isCorrect = idx == item.question.correctIndex
                            Text(
                                text = "${idx + 1}. $opt ${if (isCorrect) "✓ (Correct)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (item.reviewerNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${item.reviewerNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "অনুমোদন" else "Approve")
                            }

                            OutlinedButton(
                                onClick = {
                                    itemForRevision = item
                                    revisionNotes = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "সংশোধন" else "Revise")
                            }

                            OutlinedButton(
                                onClick = { onReject(item.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "বাতিল" else "Reject")
                            }
                        }
                    }
                }
            }
        }
    }

    itemForRevision?.let { target ->
        AlertDialog(
            onDismissRequest = { itemForRevision = null },
            title = { Text(if (isBangla) "সংশোধনের নির্দেশ দিন" else "Request AI Revision") },
            text = {
                Column {
                    Text(
                        text = if (isBangla) "প্রশ্নটিতে কী কী পরিবর্তন আনতে হবে উল্লেখ করুন:" else "Specify editorial instructions for AI correction:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = revisionNotes,
                        onValueChange = { revisionNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. অপশন খ তে বানান ত্রুটি সংশোধন করুন এবং ব্যাখ্যা বড় করুন") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestRevision(target.id, revisionNotes)
                        itemForRevision = null
                    }
                ) {
                    Text(if (isBangla) "জমা দিন" else "Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForRevision = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminDocumentsTab(
    documents: List<DocumentInfo>,
    isBangla: Boolean,
    onUploadDocument: (String, String, String, Int, String) -> Unit,
    onDraftWithAi: (DocumentInfo) -> Unit
) {
    var showUploadDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "নথি আপলোড ও টেক্সট পার্সিং" else "Document Ingestion Pipeline",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isBangla) "পিডিএফ, অফিসিয়াল সিলেবাস ও প্রিভিয়াস পেপার" else "Ingest PDF syllabus & previous papers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = { showUploadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isBangla) "আপলোড" else "Upload")
                    }
                }
            }
        }

        items(documents) { doc ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = doc.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${doc.category} • ${doc.chunkCount} Chunks • ${doc.pageCount} Pages",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = doc.vectorStatus,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onDraftWithAi(doc) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "এই নথি থেকে AI প্রশ্ন ড্রাফট করুন" else "Draft Question with AI",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        var docTitle by remember { mutableStateOf("47th BCS Official Syllabus & Rules") }
        var docCategory by remember { mutableStateOf("BCS") }
        var docType by remember { mutableStateOf("OFFICIAL_SYLLABUS") }
        var pages by remember { mutableStateOf("18") }
        var sourceMetadata by remember { mutableStateOf("BPSC Official Gazette") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text(if (isBangla) "নতুন নথি আপলোড করুন" else "Upload Official Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docCategory,
                        onValueChange = { docCategory = it },
                        label = { Text("Category (BCS, BANK, PRIMARY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = docType,
                        onValueChange = { docType = it },
                        label = { Text("Type (OFFICIAL_SYLLABUS, PAST_PAPERS)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pages,
                        onValueChange = { pages = it },
                        label = { Text("Pages") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sourceMetadata,
                        onValueChange = { sourceMetadata = it },
                        label = { Text("Authorized Source") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUploadDocument(
                            docTitle,
                            docType,
                            docCategory,
                            pages.toIntOrNull() ?: 10,
                            sourceMetadata
                        )
                        showUploadDialog = false
                    }
                ) {
                    Text(if (isBangla) "আপলোড সম্পন্ন" else "Process & Index")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminUsersTab(
    viewModel: PrepAiViewModel,
    isBangla: Boolean
) {
    val currentUser = viewModel.userProfile.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isBangla) "ভূমিকা ও এক্সেস কন্ট্রোল" else "Role-Based Access Control",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBangla) "আপনার বর্তমান ভূমিকা নির্বাচন করুন:" else "Select Active Role for Live Demo:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setUserRole(UserRole.STUDENT) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser?.role == UserRole.STUDENT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Student", color = if (currentUser?.role == UserRole.STUDENT) Color.White else MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.setUserRole(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser?.role == UserRole.ADMIN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Admin", color = if (currentUser?.role == UserRole.ADMIN) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddQuestionDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onAdd: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Bangla") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctIndex by remember { mutableStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBangla) "নতুন প্রশ্ন যুক্ত করুন" else "Create New Question") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Statement") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Bangla, Math, English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Option 1 (ক)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Option 2 (খ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optC,
                    onValueChange = { optC = it },
                    label = { Text("Option 3 (গ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optD,
                    onValueChange = { optD = it },
                    label = { Text("Option 4 (ঘ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation & Shortcut") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        val newQ = Question(
                            id = "q_custom_${System.currentTimeMillis()}",
                            examCategory = "BCS",
                            subject = subject.ifBlank { "Bangla" },
                            topic = "Custom Practice",
                            questionBn = questionText,
                            questionEn = questionText,
                            optionsBn = listOf(optA.ifBlank { "Option 1" }, optB.ifBlank { "Option 2" }, optC.ifBlank { "Option 3" }, optD.ifBlank { "Option 4" }),
                            optionsEn = listOf(optA.ifBlank { "Option 1" }, optB.ifBlank { "Option 2" }, optC.ifBlank { "Option 3" }, optD.ifBlank { "Option 4" }),
                            correctIndex = correctIndex,
                            explanationBn = explanation.ifBlank { "ব্যাখ্যা যুক্ত করা হয়নি।" },
                            explanationEn = explanation.ifBlank { "No explanation provided." },
                            difficulty = DifficultyLevel.MEDIUM,
                            previousYearTag = "Admin Custom",
                            aiShortcut = "Admin Verified"
                        )
                        onAdd(newQ)
                    }
                }
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save MCQ")
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
private fun EditQuestionDialog(
    question: Question,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf(if (isBangla) question.questionBn else question.questionEn) }
    var subject by remember { mutableStateOf(question.subject) }
    var optA by remember { mutableStateOf(question.optionsBn.getOrElse(0) { "" }) }
    var optB by remember { mutableStateOf(question.optionsBn.getOrElse(1) { "" }) }
    var optC by remember { mutableStateOf(question.optionsBn.getOrElse(2) { "" }) }
    var optD by remember { mutableStateOf(question.optionsBn.getOrElse(3) { "" }) }
    var correctIndex by remember { mutableStateOf(question.correctIndex) }
    var explanation by remember { mutableStateOf(if (isBangla) question.explanationBn else question.explanationEn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBangla) "প্রশ্ন সম্পাদনা করুন" else "Edit Question") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Statement") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Option 1 (ক)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Option 2 (খ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optC,
                    onValueChange = { optC = it },
                    label = { Text("Option 3 (গ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optD,
                    onValueChange = { optD = it },
                    label = { Text("Option 4 (ঘ)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Correct Option Index: ${correctIndex + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 1, 2, 3).forEach { idx ->
                        FilterChip(
                            selected = correctIndex == idx,
                            onClick = { correctIndex = idx },
                            label = { Text("${idx + 1}") }
                        )
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = question.copy(
                        subject = subject,
                        questionBn = questionText,
                        questionEn = questionText,
                        optionsBn = listOf(optA, optB, optC, optD),
                        optionsEn = listOf(optA, optB, optC, optD),
                        correctIndex = correctIndex,
                        explanationBn = explanation,
                        explanationEn = explanation
                    )
                    onSave(updated)
                }
            ) {
                Text(if (isBangla) "হালনাগাদ করুন" else "Update MCQ")
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
private fun AdminStudentsTab(
    viewModel: PrepAiViewModel,
    isBangla: Boolean
) {
    val profile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isBangla) "নিবন্ধিত শিক্ষার্থী তালিকা ও পারফরম্যান্স" else "Enrolled Candidates & Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val studentList = listOf(
            Triple(profile?.fullName ?: "Tanvir Ahmed", profile?.targetExam ?: "46th BCS Preliminary", "Active • 9 Day Streak"),
            Triple("Nusrat Jahan", "Bangladesh Bank AD", "Active • 14 Day Streak"),
            Triple("Rafiqul Islam", "Primary Assistant Teacher", "Active • 5 Day Streak"),
            Triple("Anika Tabassum", "Combined 5-Banks Officer", "Inactive • 2 Days ago")
        )

        items(studentList) { student ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = student.first, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = student.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = student.third, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminExamsTab(
    exams: List<com.example.data.model.MockExam>,
    isBangla: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isBangla) "পরীক্ষা ও টেস্ট সিরিজ ব্যবস্থাপনা" else "Mock Exam Configurations",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(exams) { exam ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) exam.titleBn else exam.titleEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${exam.examCategory} • ${exam.durationMinutes} min • ${exam.totalMarks} Marks • Neg: -${exam.negativeMarkPerWrong}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminCircularsTab(
    circulars: List<com.example.data.model.JobCircular>,
    isBangla: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isBangla) "সরকারি চাকরির নিয়োগ বিজ্ঞপ্তি ও ডেডলাইন" else "Recruitment Circulars & Notifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(circulars) { c ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBangla) c.jobTitleBn else c.jobTitleEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isBangla) c.organizationBn else c.organizationEn} • ${c.vacancyCount} Vacancies • Deadline: ${c.deadline}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminAnalyticsTab(
    questions: List<Question>,
    isBangla: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isBangla) "প্ল্যাটফর্ম পারফরম্যান্স ও সিলেবাস কভারেজ" else "Platform Analytics & Question Health",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Subject Breakdown in Question Bank",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val subjects = questions.groupBy { it.subject }
                    subjects.forEach { (subject, list) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = subject, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${list.size} MCQs",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Candidate Accuracy Overview",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Average candidate accuracy across BCS Mock series is currently 74.2%. Hardest topic identified: Bangla Literature (Ancient Era).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsTab(
    viewModel: PrepAiViewModel,
    isBangla: Boolean
) {
    val currentUser = viewModel.userProfile.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (isBangla) "অ্যাডমিন পোর্টাল ও সিকিউরিটি কনফিগারেশন" else "Admin Portal & Engine Configuration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Role Switch",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setUserRole(UserRole.STUDENT) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser?.role == UserRole.STUDENT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Student Mode", color = if (currentUser?.role == UserRole.STUDENT) Color.White else MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.setUserRole(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser?.role == UserRole.ADMIN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Admin Mode", color = if (currentUser?.role == UserRole.ADMIN) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Database & AI Health",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Room SQLite Database: Connected (Local Pre-seeded Bank)\n" +
                            "• AI Tutor Gateway: Sandboxed & Fallback Guaranteed\n" +
                            "• RAG Chunk Engine: Ready for Phase 2 Document Sync",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
