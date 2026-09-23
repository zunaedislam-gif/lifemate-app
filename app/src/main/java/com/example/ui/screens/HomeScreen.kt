package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: LifeMateViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToMoney: () -> Unit,
    onNavigateToDocs: () -> Unit,
    onNavigateToMore: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenQuickAction: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val allDharkhata by viewModel.allDharkhata.collectAsState()
    val allCommitments by viewModel.allCommitments.collectAsState()

    val lang = userProfile.language

    // Greeting logic
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> L10n.t("good_morning", lang)
        hour < 17 -> L10n.t("good_afternoon", lang)
        else -> L10n.t("good_evening", lang)
    }

    val totalSpentToday = todayExpenses.sumOf { it.amount }
    val toReceive = allDharkhata.filter { it.type == "RECEIVE" && !it.isSettled }.sumOf { it.amount }
    val toPay = allDharkhata.filter { it.type == "PAY" && !it.isSettled }.sumOf { it.amount }
    val pendingCommitmentsCount = allCommitments.count { !it.isFulfilled }
    val activeTasksCount = todayReminders.count { !it.isCompleted }

    // Quick natural language input state
    var naturalInput by remember { mutableStateOf("") }
    var quickActionFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Header with Greeting, Language Switcher & Profile
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$greeting, ${userProfile.name} 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = L10n.t("greeting_subtitle", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Language Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val next = if (lang == "en") "bn" else "en"
                                    viewModel.updateLanguage(next)
                                }
                                .testTag("language_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == "en") "বাংলা" else "EN",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Smart Input Box right on top
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = naturalInput,
                                onValueChange = { naturalInput = it },
                                placeholder = {
                                    Text(
                                        text = if (lang == "bn") "চা ২০, বা কাল ৯টায় রিমাইন্ডার..." else "e.g. 'Tea 20' or 'Tomorrow 9 AM doctor'...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_smart_input_field"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (naturalInput.isNotBlank()) {
                                        val query = naturalInput.trim()
                                        naturalInput = ""
                                        // Auto-detect: if it contains reminder patterns or expense patterns
                                        if (query.lowercase().startsWith("remind") || query.lowercase().contains("tomorrow") || query.lowercase().contains("am") || query.lowercase().contains("pm")) {
                                            viewModel.addSmartReminder(query) {
                                                quickActionFeedback = if (lang == "bn") "রিমাইন্ডার সংরক্ষিত হয়েছে!" else "Reminder created!"
                                            }
                                        } else if (query.matches(Regex(".+\\s+\\d+.*"))) {
                                            viewModel.addSmartExpense(query) {
                                                quickActionFeedback = if (lang == "bn") "খরচ যোগ করা হয়েছে!" else "Expense logged!"
                                            }
                                        } else {
                                            // Process via AI Assistant
                                            viewModel.askAi(query)
                                            onOpenAiChat()
                                        }
                                    } else {
                                        onOpenAiChat()
                                    }
                                },
                                modifier = Modifier.testTag("home_smart_input_submit")
                            ) {
                                Icon(
                                    imageVector = if (naturalInput.isNotBlank()) Icons.Default.Send else Icons.Default.SmartToy,
                                    contentDescription = "Submit",
                                    tint = PrimaryBlue
                                )
                            }
                        }
                    }

                    if (quickActionFeedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = quickActionFeedback ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Day Summary Statistics Cards (2x2 Grid)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = L10n.t("spent_today", lang),
                        value = "৳${totalSpentToday.toInt()}",
                        subtitle = "${todayExpenses.size} items",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = PrimaryContainerLight,
                        contentColor = PrimaryBlue,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_expenses"),
                        onClick = onNavigateToMoney
                    )
                    StatCard(
                        title = L10n.t("active_tasks", lang),
                        value = "$activeTasksCount",
                        subtitle = "Today's cues",
                        icon = Icons.Default.TaskAlt,
                        containerColor = SecondaryContainerLight,
                        contentColor = SecondaryTeal,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_tasks"),
                        onClick = onNavigateToTasks
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = L10n.t("pending_dues", lang),
                        value = "৳${toReceive.toInt()}",
                        subtitle = "To pay: ৳${toPay.toInt()}",
                        icon = Icons.Default.SwapHoriz,
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFD97706),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_dharkhata"),
                        onClick = onNavigateToMoney
                    )
                    StatCard(
                        title = L10n.t("commitments_due", lang),
                        value = "$pendingCommitmentsCount",
                        subtitle = "Promises pending",
                        icon = Icons.Default.Handshake,
                        containerColor = Color(0xFFEDE9FE),
                        contentColor = TertiaryIndigo,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_commitments"),
                        onClick = onNavigateToTasks
                    )
                }
            }
        }

        // 3. Quick Action Buttons Row (Horizontal Scroll)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = L10n.t("quick_actions", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        label = L10n.t("add_expense", lang),
                        icon = Icons.Default.AddShoppingCart,
                        color = PrimaryBlue,
                        modifier = Modifier.testTag("quick_action_expense")
                    ) { onOpenQuickAction("EXPENSE") }

                    QuickActionButton(
                        label = L10n.t("add_reminder", lang),
                        icon = Icons.Default.AlarmAdd,
                        color = SecondaryTeal,
                        modifier = Modifier.testTag("quick_action_reminder")
                    ) { onOpenQuickAction("REMINDER") }

                    QuickActionButton(
                        label = L10n.t("borrow_lend", lang),
                        icon = Icons.Default.Payments,
                        color = AccentAmber,
                        modifier = Modifier.testTag("quick_action_dharkhata")
                    ) { onOpenQuickAction("DHARKHATA") }

                    QuickActionButton(
                        label = L10n.t("add_document", lang),
                        icon = Icons.Default.Description,
                        color = TertiaryIndigo,
                        modifier = Modifier.testTag("quick_action_document")
                    ) { onOpenQuickAction("DOCUMENT") }

                    QuickActionButton(
                        label = L10n.t("add_item", lang),
                        icon = Icons.Default.LocationSearching,
                        color = AccentRose,
                        modifier = Modifier.testTag("quick_action_item")
                    ) { onOpenQuickAction("ITEM") }

                    QuickActionButton(
                        label = L10n.t("add_commitment", lang),
                        icon = Icons.Default.Verified,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.testTag("quick_action_commitment")
                    ) { onOpenQuickAction("COMMITMENT") }
                }
            }
        }

        // 4. AI Assistant Card Prompt
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenAiChat() }
                    .testTag("ai_assistant_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L10n.t("ai_assistant", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (lang == "bn") "রিমাইন্ডার, হিসাব ও প্রশ্নের উত্তর এআইকে জিজ্ঞেস করুন" else "Natural language commands & instant answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Today's Reminders & Tasks Section
        item {
            SectionHeader(
                title = L10n.t("upcoming_reminders", lang),
                actionText = "See all",
                onActionClick = onNavigateToTasks
            )
        }

        if (todayReminders.isEmpty()) {
            item {
                EmptyPlaceholder(
                    message = L10n.t("no_reminders", lang),
                    icon = Icons.Outlined.CheckCircle
                )
            }
        } else {
            items(todayReminders.take(4)) { reminder ->
                ReminderRowItem(
                    reminder = reminder,
                    onToggle = { viewModel.toggleReminder(reminder) },
                    onDelete = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }

        // 6. Today's Expenses Glance
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = L10n.t("expenses", lang) + " (Today)",
                actionText = "View Money",
                onActionClick = onNavigateToMoney
            )
        }

        if (todayExpenses.isEmpty()) {
            item {
                EmptyPlaceholder(
                    message = if (lang == "bn") "আজ কোনো খরচ রেকর্ড করা হয়নি。" else "No expenses logged today. Tap '+ Expense' to add.",
                    icon = Icons.Outlined.ReceiptLong
                )
            }
        } else {
            items(todayExpenses.take(3)) { expense ->
                ExpenseRowItem(
                    expense = expense,
                    onDelete = { viewModel.deleteExpense(expense.id) }
                )
            }
        }

        // 7. Pending Commitments Glance
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = L10n.t("commitments_due", lang),
                actionText = "Manage",
                onActionClick = onNavigateToTasks
            )
        }

        val pendingCommitments = allCommitments.filter { !it.isFulfilled }
        if (pendingCommitments.isEmpty()) {
            item {
                EmptyPlaceholder(
                    message = if (lang == "bn") "কোনো পেন্ডিং প্রতিশ্রুতি নেই。" else "No pending commitments right now.",
                    icon = Icons.Outlined.Handshake
                )
            }
        } else {
            items(pendingCommitments.take(3)) { commitment ->
                CommitmentRowItem(
                    commitment = commitment,
                    onToggle = { viewModel.toggleCommitment(commitment) },
                    onDelete = { viewModel.deleteCommitment(commitment.id) }
                )
            }
        }
    }
}

@Composable
fun ReminderRowItem(
    reminder: ReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(reminder.targetTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SecondaryTeal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = reminder.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "⏰ $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reminder.recurrence != "NONE") {
                        Text(
                            text = "🔄 ${reminder.recurrence.lowercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ExpenseRowItem(
    expense: ExpenseItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainerLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "-৳${expense.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentRose
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CommitmentRowItem(
    commitment: CommitmentItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = commitment.isFulfilled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = TertiaryIndigo)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commitment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (commitment.isFulfilled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (commitment.person.isNotBlank()) {
                        Text(
                            text = "👤 ${commitment.person}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TertiaryIndigo
                        )
                    }
                    Text(
                        text = "📅 ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(commitment.deadline))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
