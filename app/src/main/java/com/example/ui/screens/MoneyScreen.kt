package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DharkhataItem
import com.example.data.model.ExpenseItem
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MoneyScreen(viewModel: LifeMateViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val allDharkhata by viewModel.allDharkhata.collectAsState()
    val lang = userProfile.language

    var selectedTab by remember { mutableStateOf(0) } // 0 = Expenses, 1 = Dharkhata

    // Expense states
    var quickExpenseInput by remember { mutableStateOf("") }
    var showMostSpendingDialog by remember { mutableStateOf(false) }

    // Dharkhata filter
    var dharkhataFilter by remember { mutableStateOf("ALL") } // ALL, RECEIVE, PAY, SETTLED

    // Calculations for Expenses
    val todayTotal = todayExpenses.sumOf { it.amount }

    val calWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
    val weekTotal = allExpenses.filter { it.date >= calWeek.timeInMillis }.sumOf { it.amount }

    val calMonth = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
    val monthTotal = allExpenses.filter { it.date >= calMonth.timeInMillis }.sumOf { it.amount }

    val categoryTotals = allExpenses.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .toList()
        .sortedByDescending { it.second }

    val highestCategory = categoryTotals.firstOrNull()

    // Calculations for Dharkhata
    val totalToReceive = allDharkhata.filter { it.type == "RECEIVE" && !it.isSettled }.sumOf { it.amount }
    val totalToPay = allDharkhata.filter { it.type == "PAY" && !it.isSettled }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("money_screen")
    ) {
        // Tab Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryBlue
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(L10n.t("expenses", lang), fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(L10n.t("dharkhata", lang), fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            // === EXPENSE TRACKER TAB ===
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Entry Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (lang == "bn") "সহজ উপায়ে খরচ লিখুন (যেমন: চা ২০, রিকশা ৮০):" else "Quick Expense Entry (Auto-categorized):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = quickExpenseInput,
                                    onValueChange = { quickExpenseInput = it },
                                    placeholder = {
                                        Text(
                                            if (lang == "bn") "চা ২০, রিকশা ৮০, লাঞ্চ ১৫০..." else "Tea 20, Rickshaw 80, Lunch 150...",
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("expense_input_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (quickExpenseInput.isNotBlank()) {
                                            viewModel.addSmartExpense(quickExpenseInput) {
                                                quickExpenseInput = ""
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("expense_add_button")
                                ) {
                                    Text(if (lang == "bn") "যোগ" else "Add")
                                }
                            }
                        }
                    }
                }

                // 3 Period Stat Cards: Today, Weekly, Monthly
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp)),
                            color = PrimaryContainerLight
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (lang == "bn") "আজকের" else "Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "৳${todayTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp)),
                            color = SecondaryContainerLight
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (lang == "bn") "এই সপ্তাহের" else "7 Days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryTeal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "৳${weekTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryTeal
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp)),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (lang == "bn") "এই মাসের" else "30 Days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TertiaryIndigo
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "৳${monthTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TertiaryIndigo
                                )
                            }
                        }
                    }
                }

                // "Where am I spending the most money?" Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMostSpendingDialog = true }
                            .testTag("where_am_spending_most_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = L10n.t("where_spending_most", lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (highestCategory != null) {
                                    Text(
                                        text = "${highestCategory.first}: ৳${highestCategory.second.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                } else {
                                    Text(
                                        text = if (lang == "bn") "বিশ্লেষণ দেখতে ট্যাপ করুন" else "Tap to see spending breakdown",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Category-wise Breakdown Charts
                if (categoryTotals.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (lang == "bn") "খাতভিত্তিক খরচের চার্ট" else "Category Breakdown",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val maxExpense = categoryTotals.maxOfOrNull { it.second } ?: 1.0
                                categoryTotals.take(5).forEach { (category, amount) ->
                                    val progress = (amount / maxExpense).toFloat().coerceIn(0f, 1f)
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "৳${amount.toInt()}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(CircleShape),
                                            color = when (category) {
                                                "Food & Drinks" -> AccentAmber
                                                "Transportation" -> PrimaryBlue
                                                "Groceries" -> SecondaryTeal
                                                "Bills & Utilities" -> TertiaryIndigo
                                                else -> AccentRose
                                            },
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Expenses List
                item {
                    Text(
                        text = if (lang == "bn") "সাম্প্রতিক খরচের তালিকা" else "All Expenses (${allExpenses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (allExpenses.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = if (lang == "bn") "এখনও কোনো খরচ রেকর্ড করেননি" else "No expenses logged yet. Try 'Tea 20' above!",
                            icon = Icons.Outlined.Receipt
                        )
                    }
                } else {
                    items(allExpenses) { expense ->
                        val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(expense.date))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryContainerLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = expense.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "• $dateFormatted",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "৳${expense.amount.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRose
                                )
                                IconButton(onClick = { viewModel.deleteExpense(expense.id) }) {
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
                }
            }
        } else {
            // === DHARKHATA (BORROW & LEND) TAB ===
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Two Balance Cards: "To Receive" & "To Pay"
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = L10n.t("i_will_receive", lang),
                            value = "৳${totalToReceive.toInt()}",
                            subtitle = if (lang == "bn") "অন্যের কাছে পাওনা" else "Lent to others",
                            icon = Icons.Default.CallReceived,
                            containerColor = SecondaryContainerLight,
                            contentColor = SecondaryTeal,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dharkhata_receive_stat")
                        )
                        StatCard(
                            title = L10n.t("i_have_to_pay", lang),
                            value = "৳${totalToPay.toInt()}",
                            subtitle = if (lang == "bn") "নিজের দেনা" else "Borrowed from others",
                            icon = Icons.Default.CallMade,
                            containerColor = Color(0xFFFFE4E6),
                            contentColor = AccentRose,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dharkhata_pay_stat")
                        )
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "RECEIVE", "PAY", "SETTLED").forEach { f ->
                            FilterChip(
                                selected = dharkhataFilter == f,
                                onClick = { dharkhataFilter = f },
                                label = {
                                    Text(
                                        when (f) {
                                            "ALL" -> if (lang == "bn") "সব" else "All"
                                            "RECEIVE" -> if (lang == "bn") "পাওনা" else "To Receive"
                                            "PAY" -> if (lang == "bn") "দেনা" else "To Pay"
                                            else -> if (lang == "bn") "পরিশোধিত" else "Settled"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                val filteredDharkhata = allDharkhata.filter { item ->
                    when (dharkhataFilter) {
                        "ALL" -> !item.isSettled
                        "RECEIVE" -> item.type == "RECEIVE" && !item.isSettled
                        "PAY" -> item.type == "PAY" && !item.isSettled
                        "SETTLED" -> item.isSettled
                        else -> true
                    }
                }

                if (filteredDharkhata.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = if (lang == "bn") "এই ফিল্টারে কোনো দেনা-পাওনা নেই" else "No records in this tab. Tap '+ Dharkhata' on Home to add.",
                            icon = Icons.Outlined.Paid
                        )
                    }
                } else {
                    items(filteredDharkhata) { item ->
                        DharkhataCard(
                            item = item,
                            lang = lang,
                            onToggleSettled = { viewModel.toggleDharkhataSettled(item) },
                            onDelete = { viewModel.deleteDharkhata(item.id) },
                            onSendReminder = {
                                val msg = if (lang == "bn") {
                                    "প্রিয় ${item.personName}, আপনার কাছে লাইফমেটের মাধ্যমে ৳${item.amount.toInt()} পাওনা রয়েছে। অনুগ্রহ করে চেক করবেন।"
                                } else {
                                    "Hi ${item.personName}, this is a gentle reminder regarding ৳${item.amount.toInt()} recorded in LifeMate. Thank you!"
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send Payment Reminder"))
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog: "Where am I spending the most money?"
    if (showMostSpendingDialog) {
        AlertDialog(
            onDismissRequest = { showMostSpendingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(L10n.t("where_spending_most", lang), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    if (highestCategory != null) {
                        val total = allExpenses.sumOf { it.amount }.coerceAtLeast(1.0)
                        val percent = ((highestCategory.second / total) * 100).toInt()
                        Text(
                            text = if (lang == "bn") {
                                "📊 আপনি সবচেয়ে বেশি খরচ করেছেন '${highestCategory.first}' খাতে — মোট ৳${highestCategory.second.toInt()} ($percent% of all expenses)!\n\n💡 পরামর্শ: এই খাতে একটু নিয়ন্ত্রণ এনে আপনি সহজে সঞ্চয় বাড়াতে পারেন।"
                            } else {
                                "📊 Your highest spending category is '${highestCategory.first}' with a total of ৳${highestCategory.second.toInt()} ($percent% of all expenses)!\n\n💡 Tip: Consider setting a monthly budget for ${highestCategory.first} to boost your personal savings."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = if (lang == "bn") "এখনও পর্যাপ্ত খরচের ডাটা নেই।" else "Not enough expense data to generate insights yet. Log your daily meals, transport and shopping!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMostSpendingDialog = false }) {
                    Text(L10n.t("done", lang))
                }
            }
        )
    }
}

@Composable
fun DharkhataCard(
    item: DharkhataItem,
    lang: String,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit,
    onSendReminder: () -> Unit
) {
    val isReceive = item.type == "RECEIVE"
    val dueDateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(item.expectedReturnDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isReceive) SecondaryContainerLight else Color(0xFFFFE4E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReceive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isReceive) SecondaryTeal else AccentRose,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isReceive) (if (lang == "bn") "পাবো (Lent)" else "I will receive") else (if (lang == "bn") "দিতে হবে (Borrowed)" else "I have to pay"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isReceive) SecondaryTeal else AccentRose,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "৳${item.amount.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isReceive) SecondaryTeal else AccentRose
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: $dueDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isReceive && !item.isSettled) {
                        OutlinedButton(
                            onClick = onSendReminder,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remind", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onToggleSettled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isSettled) MaterialTheme.colorScheme.surfaceVariant else SecondaryTeal
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (item.isSettled) "Reopen" else L10n.t("mark_settled", lang),
                            fontSize = 12.sp,
                            color = if (item.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        )
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
    }
}
