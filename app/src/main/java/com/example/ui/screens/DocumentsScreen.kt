package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import com.example.data.model.VaultDocument
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.theme.*
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DocumentsScreen(viewModel: LifeMateViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val allDocs by viewModel.allDocuments.collectAsState()
    val lang = userProfile.language

    // PIN lock state
    val hasPin = userProfile.vaultPin.isNotBlank()
    var isUnlocked by remember { mutableStateOf(!hasPin) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Search and Category filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Add document dialog state
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "NID", "Passport", "Birth Certificate", "Driving License", "Certificates", "CV", "Other")

    // Filter documents
    val filteredDocs = allDocs.filter { doc ->
        val matchesCategory = selectedCategory == "All" || doc.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.documentNumber.contains(searchQuery, ignoreCase = true) ||
                doc.category.contains(searchQuery, ignoreCase = true) ||
                doc.notes.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Expiry check (documents expiring in less than 60 days)
    val now = System.currentTimeMillis()
    val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000
    val expiringSoonDocs = allDocs.filter { doc ->
        doc.expiryDate != null && doc.expiryDate > now && (doc.expiryDate - now) < sixtyDaysMillis
    }

    if (!isUnlocked) {
        // Vault PIN Lock Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = L10n.t("document_vault", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = L10n.t("pin_prompt", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4) enteredPin = it
                            pinError = false
                        },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        isError = pinError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_pin_input")
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "bn") "ভুল পিন! আবার চেষ্টা করুন।" else "Incorrect PIN. Try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (enteredPin == userProfile.vaultPin) {
                                isUnlocked = true
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_unlock_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (lang == "bn") "আনলক করুন" else "Unlock Vault")
                    }
                }
            }
        }
    } else {
        // Vault Main Screen
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 80.dp)
                        .testTag("add_doc_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Document")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .testTag("documents_screen"),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(L10n.t("search_docs", lang), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("doc_search_input"),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }

                // Category Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                // Expiry Alerts Banner if any doc expiring soon
                if (expiringSoonDocs.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${expiringSoonDocs.size} ${L10n.t("expiry_warning", lang)}!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = expiringSoonDocs.joinToString(", ") { "${it.title} (${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.expiryDate ?: 0))})" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }

                // Document Count & List
                item {
                    Text(
                        text = "${L10n.t("document_vault", lang)} (${filteredDocs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (filteredDocs.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            message = if (lang == "bn") "কোনো ডকুমেন্ট পাওয়া যায়নি। নিচে + বাটনে ট্যাপ করে যোগ করুন।" else "No documents in vault. Tap + button below to add your NID, Passport, Certificates.",
                            icon = Icons.Outlined.FolderShared
                        )
                    }
                } else {
                    items(filteredDocs) { doc ->
                        DocumentCard(
                            doc = doc,
                            lang = lang,
                            onCopyNumber = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Document Number", doc.documentNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${doc.documentNumber}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteDocument(doc.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Document Dialog
    if (showAddDialog) {
        var docTitle by remember { mutableStateOf("") }
        var docCat by remember { mutableStateOf("NID") }
        var docNumber by remember { mutableStateOf("") }
        var docNotes by remember { mutableStateOf("") }
        var hasExpiry by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (lang == "bn") "নতুন ডকুমেন্ট যোগ করুন" else "Add New Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Title (e.g. Smart NID, Bangladeshi Passport)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Category dropdown row
                    Text("Category:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("NID", "Passport", "Birth Certificate", "Driving License", "Certificates", "CV", "Other").forEach { c ->
                            FilterChip(
                                selected = docCat == c,
                                onClick = { docCat = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = { Text("Document / ID Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasExpiry,
                            onCheckedChange = { hasExpiry = it }
                        )
                        Text("Has Expiry Date (set 5 years)", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = docNotes,
                        onValueChange = { docNotes = it },
                        label = { Text("Notes / Safe Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank()) {
                            val expiry = if (hasExpiry) System.currentTimeMillis() + (5L * 365 * 86400000L) else null
                            viewModel.addDocument(
                                title = docTitle,
                                category = docCat,
                                docNumber = docNumber,
                                expiryDate = expiry,
                                notes = docNotes
                            ) {
                                showAddDialog = false
                            }
                        }
                    }
                ) {
                    Text(L10n.t("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(L10n.t("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    doc: VaultDocument,
    lang: String,
    onCopyNumber: () -> Unit,
    onDelete: () -> Unit
) {
    val expiryStr = doc.expiryDate?.let { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it)) }

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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (doc.category) {
                                "Passport" -> Icons.Default.FlightTakeoff
                                "Driving License" -> Icons.Default.DirectionsCar
                                "Certificates" -> Icons.Default.School
                                else -> Icons.Default.Badge
                            },
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = doc.category,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (doc.documentNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyNumber() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No: ${doc.documentNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (expiryStr != null) {
                    Text(
                        text = "Expires: $expiryStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Lifetime Document",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTeal
                    )
                }

                if (doc.notes.isNotBlank()) {
                    Text(
                        text = doc.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
