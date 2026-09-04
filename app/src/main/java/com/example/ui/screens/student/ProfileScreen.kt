package com.example.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PaymentProvider
import com.example.data.model.PaymentResult
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PrepAiViewModel

@Composable
fun ProfileScreen(
    viewModel: PrepAiViewModel,
    modifier: Modifier = Modifier
) {
    val isBangla = viewModel.currentLanguage.value == "BN"
    val profile by viewModel.userProfile.collectAsState()
    val attempts by viewModel.examAttempts.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var paymentResultDialogText by remember { mutableStateOf<String?>(null) }

    var editName by remember { mutableStateOf(profile?.fullName ?: "Tanvir Ahmed") }
    var editTarget by remember { mutableStateOf(profile?.targetExam ?: "46th BCS Preliminary") }
    var editHours by remember { mutableIntStateOf(profile?.dailyStudyHours ?: 4) }

    val unreadNotifCount = notifications.count { !it.isRead }
    val isUserPro = profile?.isPro == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Identity Card
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Notifications button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Box {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            if (unreadNotifCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Avatar with Halo
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(96.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_prepai_tutor),
                        contentDescription = "Candidate Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = profile?.fullName ?: "Tanvir Ahmed",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = profile?.email ?: "tanvir.bcs@prepai.bd",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = profile?.targetExam ?: "46th BCS Preliminary",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metric Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(
                        icon = Icons.Default.LocalFireDepartment,
                        label = if (isBangla) "ধারাবাহিকতা" else "Streak",
                        value = "${profile?.streakDays ?: 9} Days"
                    )
                    MetricItem(
                        icon = Icons.Default.Star,
                        label = if (isBangla) "অর্জিত XP" else "XP Points",
                        value = "${profile?.xpPoints ?: 2450} XP"
                    )
                    MetricItem(
                        icon = Icons.Default.AssignmentTurnedIn,
                        label = if (isBangla) "মডেল টেস্ট" else "Exams Taken",
                        value = "${attempts.size}"
                    )
                    MetricItem(
                        icon = Icons.Default.School,
                        label = if (isBangla) "দৈনিক পাঠ" else "Daily Hours",
                        value = "${profile?.dailyStudyHours ?: 4} hrs"
                    )
                }
            }
        }

        // Subscription Tier & Plan Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "সাবস্ক্রিপশন প্ল্যান" else "Subscription Plan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUserPro) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (isUserPro) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isUserPro) (if (isBangla) "PRO সাবস্ক্রাইবার" else "PRO Aspirant") else (if (isBangla) "ফ্রি স্টার্টার" else "Free Starter"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isUserPro) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isUserPro) {
                    Text(
                        text = if (isBangla)
                            "আপনার PRO অ্যাকাউন্ট সক্রিয় রয়েছে। আনলিমিটেড AI কোপাইলট, পূর্ণাঙ্গ মেধা তালিকা ও সকল মডেল টেস্ট আনলকড।"
                        else
                            "Your PRO Aspirant subscription is active. Enjoy unlimited AI copilot queries, 200-mark model tests, and live merit rankings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBangla) "সক্রিয় সুবিধা ও পারমিশন:" else "Active Unlocked Perks:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBangla)
                                    "✓ আনলিমিটেড জেমিনাই ফ্ল্যাশ ডিপ টিউটরিং\n✓ সকল ২০০ নম্বরের স্পেশাল বিসিএস মডেল টেস্ট\n✓ পার্সোনালাইজড ৯০ দিনের সিলেবাস রুটিন\n✓ লাইভ মেধা স্কোর ও ক্যাডার কম্পিটিশন ইনডেক্স"
                                else
                                    "✓ Unlimited Gemini Flash deep tutoring\n✓ Full 200-mark BCS mock exam series\n✓ Adaptive 90-day syllabus routine\n✓ Live merit scores & cadre competitiveness",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (isBangla)
                            "স্ট্যান্ডার্ড প্রশ্নব্যাংক এবং সীমিত দৈনিক এআই উত্তর সক্রিয় রয়েছে। আনলিমিটেড এআই ও ফুল টেস্টের জন্য আপগ্রেড করুন।"
                        else
                            "Standard question bank and limited AI queries active. Upgrade to unlock full 200-mark tests and live merit rankings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Upgrade Prompt Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBangla) "PRO Aspirant (অল-অ্যাক্সেস)" else "PRO Aspirant (All-Access)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isBangla) "৳৪৯৯ / ৩ মাস • আনলিমিটেড এআই ও মেধা র‍্যাংকিং" else "BDT 499 / 3 Mo • Unlimited AI & Live Rankings",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = { showUpgradeDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("profile_upgrade_pro_btn")
                            ) {
                                Text(
                                    text = if (isBangla) "আপগ্রেড" else "Upgrade",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Database & Security Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isBangla)
                            "পোস্টগ্রেএসকিউএল (সুপাবেস আরএলএস) ব্যাকএন্ড সক্রিয়"
                        else
                            "PostgreSQL (Supabase RLS) Security Enforced",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Role Switch & Admin portal shortcut
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isBangla) "রোল ও অ্যাক্সেস পোর্টাল" else "Role & Portal Access",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBangla)
                        "শিক্ষার্থী এবং অ্যাডমিন মোডের মধ্যে স্যুইচ করে প্ল্যাটফর্ম ম্যানেজমেন্ট ও কোয়ালিটি কন্ট্রোল করুন।"
                    else
                        "Switch between Student candidate view and Admin Content Hub for quality control.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setUserRole(UserRole.ADMIN)
                            viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_switch_admin")
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "অ্যাডমিন হাব" else "Admin Hub", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_logout_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "লগ আউট" else "Log Out", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (isBangla) "প্রোফাইল আপডেট করুন" else "Edit Student Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isBangla) "পূর্ণ নাম" else "Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_edit_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editTarget,
                        onValueChange = { editTarget = it },
                        label = { Text(if (isBangla) "টার্গেট পরীক্ষা" else "Target Exam") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editHours.toString(),
                        onValueChange = { editHours = it.toIntOrNull() ?: editHours },
                        label = { Text(if (isBangla) "দৈনিক পড়ার লক্ষ্য (ঘণ্টা)" else "Daily Study Hours") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(editName, editTarget, editHours)
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("profile_save_btn")
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Upgrade Dialog
    if (showUpgradeDialog) {
        var selectedDuration by remember { mutableIntStateOf(3) }
        var selectedProvider by remember { mutableStateOf(PaymentProvider.BKASH) }
        var phoneNumber by remember { mutableStateOf("01711223344") }

        val plans = listOf(
            SubscriptionPlan(
                tier = SubscriptionTier.BASIC,
                nameEn = "1-Month Starter",
                nameBn = "১ মাসের স্টার্টার",
                priceBdt = 199,
                durationMonths = 1,
                featuresEn = listOf("All Question Banks", "Standard Mock Tests"),
                featuresBn = listOf("সকল প্রশ্নব্যাংক", "স্ট্যান্ডার্ড মডেল টেস্ট")
            ),
            SubscriptionPlan(
                tier = SubscriptionTier.PREMIUM,
                nameEn = "3-Month BCS Pro",
                nameBn = "৩ মাসের বিসিএস প্রো",
                priceBdt = 499,
                durationMonths = 3,
                featuresEn = listOf("Unlimited AI Copilot", "200-Mark Tests", "Live Merit"),
                featuresBn = listOf("আনলিমিটেড এআই", "২০০ নম্বরের ফুল টেস্ট", "লাইভ মেধা তালিকা")
            ),
            SubscriptionPlan(
                tier = SubscriptionTier.PREMIUM,
                nameEn = "12-Month Aspirant",
                nameBn = "১২ মাসের ফুল এসপির্যান্ট",
                priceBdt = 1499,
                durationMonths = 12,
                featuresEn = listOf("All Access", "Priority AI", "Career Consultation"),
                featuresBn = listOf("অল অ্যাক্সেস", "অগ্রাধিকার এআই", "ক্যাডার গাইডলাইন")
            )
        )

        val activePlan = plans.find { it.durationMonths == selectedDuration } ?: plans[1]

        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "PRO মেম্বারশিপ আপগ্রেড" else "Upgrade to PRO Aspirant")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBangla) "প্ল্যান নির্বাচন করুন:" else "Select Subscription Plan:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        plans.forEach { p ->
                            FilterChip(
                                selected = selectedDuration == p.durationMonths,
                                onClick = { selectedDuration = p.durationMonths },
                                label = { Text("${p.durationMonths}M (৳${p.priceBdt})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBangla) "পেমেন্ট মাধ্যম:" else "Payment Method:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentProvider.values().forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                                label = { Text(provider.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(if (isBangla) "মোবাইল নম্বর (bKash/Nagad)" else "Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "মোট প্রদেয়: ৳${activePlan.priceBdt} (${activePlan.durationMonths} মাস)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.processSubscriptionPayment(
                                plan = activePlan,
                                provider = selectedProvider,
                                phone = phoneNumber,
                                isSandbox = true
                            ) { result ->
                                showUpgradeDialog = false
                                when (result) {
                                    is PaymentResult.Success -> {
                                        paymentResultDialogText = "অভিনন্দন! আপনার PRO মেম্বারশিপ সফলভাবে সক্রিয় হয়েছে।\nTransaction ID: ${result.transactionId}"
                                    }
                                    is PaymentResult.RequiresMerchantSetup -> {
                                        paymentResultDialogText = result.message
                                    }
                                    is PaymentResult.Failed -> {
                                        paymentResultDialogText = result.reason
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBangla) "টেস্ট সিমুলেশন এক্টিভেশন (Sandbox)" else "Test Sandbox Activation")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.processSubscriptionPayment(
                                plan = activePlan,
                                provider = selectedProvider,
                                phone = phoneNumber,
                                isSandbox = false
                            ) { result ->
                                showUpgradeDialog = false
                                when (result) {
                                    is PaymentResult.Success -> {
                                        paymentResultDialogText = "অভিনন্দন! আপনার PRO মেম্বারশিপ সফলভাবে সক্রিয় হয়েছে।\nTransaction ID: ${result.transactionId}"
                                    }
                                    is PaymentResult.RequiresMerchantSetup -> {
                                        paymentResultDialogText = result.message
                                    }
                                    is PaymentResult.Failed -> {
                                        paymentResultDialogText = result.reason
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBangla) "লাইভ পেমেন্ট গেটওয়ে" else "Live Payment Gateway")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Payment Result Notice Dialog
    paymentResultDialogText?.let { text ->
        AlertDialog(
            onDismissRequest = { paymentResultDialogText = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "পেমেন্ট স্ট্যাটাস" else "Payment Status")
                }
            },
            text = {
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(onClick = { paymentResultDialogText = null }) {
                    Text(if (isBangla) "ঠিক আছে" else "OK")
                }
            }
        )
    }

    // Notifications Viewer Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "নোটিফিকেশন সেন্টার" else "Notification Center")
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text(if (isBangla) "কোনো নতুন নোটিফিকেশন নেই" else "No new notifications")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!notif.isRead) {
                                        IconButton(onClick = { viewModel.markNotificationRead(notif.id) }) {
                                            Icon(Icons.Default.Check, contentDescription = "Mark as read", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
