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
import com.example.data.model.CommitmentItem
import com.example.data.model.ReminderItem
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.theme.*
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(viewModel: LifeMateViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val allCommitments by viewModel.allCommitments.collectAsState()
    val lang = userProfile.language

    var selectedTab by remember { mutableStateOf(0) } // 0 = Reminders, 1 = Commitments
    var reminderInput by remember { mutableStateOf("") }
    var commitmentInput by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasks_screen")
    ) {
        // Top Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryBlue
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (lang == "bn") "স্মার্ট রিমাইন্ডার" else "Smart Reminders", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Alarm, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (lang == "bn") "প্রতিশ্রুতি (Commitments)" else "Commitments", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Handshake, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            // Reminders Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Natural Language Quick Add Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (lang == "bn") "স্বাভাবিক ভাষায় রিমাইন্ডার লিখুন:" else "Write reminder naturally (Date & time auto-detected):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = reminderInput,
                                onValueChange = { reminderInput = it },
                                placeholder = {
                                    Text(
                                        if (lang == "bn") "যেমন: কাল সকাল ৯টায় রহিমকে ফোন দাও" else "e.g. Tomorrow at 9 AM call Rahim",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reminder_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (reminderInput.isNotBlank()) {
                                        viewModel.addSmartReminder(reminderInput)
                                        reminderInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("reminder_add_button")
                            ) {
                                Text(if (lang == "bn") "যোগ" else "Add")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Chips
                val categories = listOf("All", "Pending", "Done", "Call", "Bill", "Health", "Study", "Work")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtered List
                val filteredReminders = allReminders.filter { item ->
                    when (selectedCategoryFilter) {
                        "All" -> true
                        "Pending" -> !item.isCompleted
                        "Done" -> item.isCompleted
                        else -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
                    }
                }

                if (filteredReminders.isEmpty()) {
                    EmptyPlaceholder(
                        message = if (lang == "bn") "কোনো রিমাইন্ডার পাওয়া যায়নি" else "No reminders in this filter",
                        icon = Icons.Outlined.NotificationsOff
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredReminders) { item ->
                            ReminderCard(
                                item = item,
                                onToggle = { viewModel.toggleReminder(item) },
                                onDelete = { viewModel.deleteReminder(item.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // Commitments Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Quick Add Commitment
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (lang == "bn") "প্রতিশ্রুতি বা ডেডলাইন লিখুন:" else "Record promise or commitment:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = commitmentInput,
                                onValueChange = { commitmentInput = it },
                                placeholder = {
                                    Text(
                                        if (lang == "bn") "যেমন: কাল ক্লায়েন্টকে ডিজাইন পাঠাব" else "e.g. Send client design tomorrow",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("commitment_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (commitmentInput.isNotBlank()) {
                                        viewModel.addSmartCommitment(commitmentInput) {
                                            commitmentInput = ""
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("commitment_add_button")
                            ) {
                                Text(if (lang == "bn") "যোগ" else "Add")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (allCommitments.isEmpty()) {
                    EmptyPlaceholder(
                        message = if (lang == "bn") "কোনো প্রতিশ্রুতি সংরক্ষিত নেই" else "No commitments recorded yet. Stay on top of your promises!",
                        icon = Icons.Outlined.Handshake
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCommitments) { commitment ->
                            CommitmentCard(
                                commitment = commitment,
                                onToggle = { viewModel.toggleCommitment(commitment) },
                                onDelete = { viewModel.deleteCommitment(commitment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCard(
    item: ReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEE, dd MMM - hh:mm a", Locale.getDefault()).format(Date(item.targetTime))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SecondaryTeal)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "📅 $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.recurrence != "NONE") {
                        Text(
                            text = "🔄 ${item.recurrence}",
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
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CommitmentCard(
    commitment: CommitmentItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(commitment.deadline))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = commitment.isFulfilled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = TertiaryIndigo)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commitment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (commitment.isFulfilled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (commitment.person.isNotBlank()) {
                        Text(
                            text = "👤 ${commitment.person}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TertiaryIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = commitment.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Deadline: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
