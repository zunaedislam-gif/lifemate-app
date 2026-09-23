package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel

@Composable
fun QuickActionModal(
    actionType: String?,
    viewModel: LifeMateViewModel,
    onDismiss: () -> Unit
) {
    if (actionType == null) return
    val userProfile by viewModel.userProfile.collectAsState()
    val lang = userProfile.language

    when (actionType) {
        "EXPENSE" -> {
            var input by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "দ্রুত খরচ যোগ করুন" else "Quick Expense Entry") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (lang == "bn") "স্বাভাবিক ভাষায় লিখুন, যেমন:\n• চা ২০\n• রিকশা ৮০\n• লাঞ্চ ১৫০" else "Write naturally, e.g.:\n• Tea 20\n• Rickshaw 80\n• Lunch 150",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Expense & Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.addSmartExpense(input) { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }

        "REMINDER" -> {
            var input by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "স্মার্ট রিমাইন্ডার তৈরি করুন" else "Smart Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (lang == "bn") "তারিখ ও সময় নিজে থেকেই শনাক্ত হবে:\n• কাল সকাল ৯টায় রহিমকে ফোন দাও\n• শুক্রবার বিকেল ৫টায় ডাক্তারের কাছে যাও" else "Date and time will be automatically detected:\n• Tomorrow at 9 AM call Rahim\n• Friday 5 PM go to doctor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Reminder note") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.addSmartReminder(input) { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }

        "DHARKHATA" -> {
            var person by remember { mutableStateOf("") }
            var amount by remember { mutableStateOf("") }
            var isLending by remember { mutableStateOf(true) } // true: I will receive, false: I have to pay
            var phone by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "ধারখাতা লেনদেন এন্ট্রি" else "Record Borrow / Lend") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isLending,
                                onClick = { isLending = true },
                                label = { Text(if (lang == "bn") "আমি পাবো (পাওনা)" else "I will receive") }
                            )
                            FilterChip(
                                selected = !isLending,
                                onClick = { isLending = false },
                                label = { Text(if (lang == "bn") "আমাকে দিতে হবে (দেনা)" else "I have to pay") }
                            )
                        }
                        OutlinedTextField(
                            value = person,
                            onValueChange = { person = it },
                            label = { Text("Person Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount (৳)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (person.isNotBlank() && amt > 0) {
                                val due = System.currentTimeMillis() + (7L * 24 * 3600 * 1000)
                                viewModel.addDharkhata(
                                    person = person,
                                    amount = amt,
                                    type = if (isLending) "RECEIVE" else "PAY",
                                    phone = phone,
                                    dueDate = due
                                ) { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }

        "DOCUMENT" -> {
            var title by remember { mutableStateOf("") }
            var docNumber by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("NID") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "ডকুমেন্ট ভল্টে যোগ করুন" else "Add to Document Vault") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Smart NID, Passport)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = docNumber,
                            onValueChange = { docNumber = it },
                            label = { Text("Document / ID Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addDocument(title, category, docNumber, null, "") { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }

        "ITEM" -> {
            var name by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "জিনিসপত্রের অবস্থান ট্র্যাক করুন" else "Track Lost Item") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Item Name (e.g. Passport, Office Keys)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Last Known Location (e.g. Drawer, Bag)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && location.isNotBlank()) {
                                viewModel.addLostItem(name, "General", location, "") { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }

        "COMMITMENT" -> {
            var title by remember { mutableStateOf("") }
            var person by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(if (lang == "bn") "নতুন প্রতিশ্রুতি / ডেডলাইন" else "New Commitment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Promise / Commitment Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = person,
                            onValueChange = { person = it },
                            label = { Text("Person / Client (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val deadline = System.currentTimeMillis() + (3L * 86400000L)
                                viewModel.addCommitment(title, person, deadline, "General") { onDismiss() }
                            }
                        }
                    ) { Text(L10n.t("save", lang)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(L10n.t("cancel", lang)) }
                }
            )
        }
    }
}
