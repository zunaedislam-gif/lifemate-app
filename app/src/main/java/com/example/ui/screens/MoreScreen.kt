package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MoreScreen(viewModel: LifeMateViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val emergencyProfile by viewModel.emergencyProfile.collectAsState()
    val latestSafeArrival by viewModel.latestSafeArrival.collectAsState()
    val allLostItems by viewModel.allLostItems.collectAsState()
    val studentSchedule by viewModel.studentSchedule.collectAsState()
    val studentAssignments by viewModel.studentAssignments.collectAsState()
    val studentAttendance by viewModel.studentAttendance.collectAsState()

    val lang = userProfile.language

    // Sub-view navigation state inside More tab:
    // "MAIN", "STUDENT", "LOST_ITEMS", "EMERGENCY", "SAFE_ARRIVAL", "SETTINGS"
    var activeSubView by remember { mutableStateOf("MAIN") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_screen")
    ) {
        // Sub-screen header if not in MAIN
        if (activeSubView != "MAIN") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeSubView = "MAIN" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = when (activeSubView) {
                        "STUDENT" -> L10n.t("student_mode", lang)
                        "LOST_ITEMS" -> L10n.t("lost_items", lang)
                        "EMERGENCY" -> L10n.t("emergency_card", lang)
                        "SAFE_ARRIVAL" -> L10n.t("safe_arrival", lang)
                        else -> L10n.t("settings", lang)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when (activeSubView) {
            "MAIN" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = L10n.t("more", lang),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (lang == "bn") "জরুরি কার্ড, সেফটি ট্র্যাকার, স্টুডেন্ট টুলস ও সেটিংস" else "Emergency card, safety tools, student hub & settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        MoreFeatureCard(
                            title = L10n.t("emergency_card", lang),
                            description = if (lang == "bn") "রক্তের গ্রুপ, জরুরি কন্টাক্ট ও স্ক্যানযোগ্য কিউআর কোড" else "Blood group, emergency contacts & shareable QR code",
                            icon = Icons.Default.QrCode2,
                            color = AccentRose,
                            onClick = { activeSubView = "EMERGENCY" }
                        )
                    }

                    item {
                        MoreFeatureCard(
                            title = L10n.t("safe_arrival", lang),
                            description = if (lang == "bn") "কোথাও যাওয়ার সময় সেফটি টাইমার ও পরিবারকে অ্যালার্ট" else "Safety countdown & alert family if unconfirmed",
                            icon = Icons.Default.Shield,
                            color = SecondaryTeal,
                            onClick = { activeSubView = "SAFE_ARRIVAL" }
                        )
                    }

                    item {
                        MoreFeatureCard(
                            title = L10n.t("lost_items", lang),
                            description = if (lang == "bn") "চাবি, ওয়ালেট, পাসপোর্ট কোথায় রেখেছেন খুঁজে পান" else "Track keys, wallet, chargers & last known locations",
                            icon = Icons.Default.FindInPage,
                            color = PrimaryBlue,
                            onClick = { activeSubView = "LOST_ITEMS" }
                        )
                    }

                    item {
                        MoreFeatureCard(
                            title = L10n.t("student_mode", lang),
                            description = if (lang == "bn") "ক্লাস রুটিন, অ্যাসাইনমেন্ট, উপস্থিতি ও পড়ার টাইমার" else "Class routine, assignments, attendance & CGPA tools",
                            icon = Icons.Default.School,
                            color = TertiaryIndigo,
                            onClick = { activeSubView = "STUDENT" }
                        )
                    }

                    item {
                        MoreFeatureCard(
                            title = L10n.t("settings", lang),
                            description = if (lang == "bn") "ভাষা পরিবর্তন (বাংলা/English), ডার্ক মোড ও ভল্ট পিন" else "Language (EN/বাংলা), Dark theme, Vault PIN & Profile",
                            icon = Icons.Default.Settings,
                            color = Color(0xFF64748B),
                            onClick = { activeSubView = "SETTINGS" }
                        )
                    }
                }
            }

            "EMERGENCY" -> {
                EmergencyCardScreen(emergencyProfile, lang, onSave = { viewModel.saveEmergencyProfile(it) })
            }

            "SAFE_ARRIVAL" -> {
                SafeArrivalScreen(latestSafeArrival, lang,
                    onStart = { dest, name, phone, mins -> viewModel.startSafeArrival(dest, name, phone, mins) },
                    onArrived = { viewModel.confirmSafeArrival(it) }
                )
            }

            "LOST_ITEMS" -> {
                LostItemsScreen(allLostItems, lang,
                    onAdd = { name, cat, loc, notes -> viewModel.addLostItem(name, cat, loc, notes) },
                    onDelete = { viewModel.deleteLostItem(it) }
                )
            }

            "STUDENT" -> {
                StudentModeScreen(
                    schedules = studentSchedule,
                    assignments = studentAssignments,
                    attendances = studentAttendance,
                    lang = lang,
                    onAddSchedule = { d, s, t, r -> viewModel.addSchedule(d, s, t, r) },
                    onDeleteSchedule = { viewModel.deleteSchedule(it) },
                    onAddAssignment = { s, t, due -> viewModel.addAssignment(s, t, due) },
                    onToggleAssignment = { viewModel.toggleAssignment(it) },
                    onDeleteAssignment = { viewModel.deleteAssignment(it) },
                    onUpdateAttendance = { s, att, tot, id -> viewModel.addOrUpdateAttendance(s, att, tot, id) },
                    onDeleteAttendance = { viewModel.deleteAttendance(it) }
                )
            }

            "SETTINGS" -> {
                SettingsScreen(userProfile, lang,
                    onUpdateLanguage = { viewModel.updateLanguage(it) },
                    onUpdateDarkMode = { viewModel.updateDarkMode(it) },
                    onUpdatePin = { viewModel.updateVaultPin(it) },
                    onUpdateUser = { name, email, role -> viewModel.updateUser(name, email, role) }
                )
            }
        }
    }
}

@Composable
fun MoreFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

// ==========================================
// 1. EMERGENCY PROFILE & QR CODE
// ==========================================
@Composable
fun EmergencyCardScreen(
    profile: EmergencyProfile,
    lang: String,
    onSave: (EmergencyProfile) -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    var fullName by remember(profile) { mutableStateOf(profile.fullName) }
    var bloodGroup by remember(profile) { mutableStateOf(profile.bloodGroup) }
    var primaryContactName by remember(profile) { mutableStateOf(profile.primaryContactName) }
    var primaryContactPhone by remember(profile) { mutableStateOf(profile.primaryContactPhone) }
    var allergies by remember(profile) { mutableStateOf(profile.allergies) }
    var medicalNotes by remember(profile) { mutableStateOf(profile.medicalNotes) }

    var isPublicBlood by remember(profile) { mutableStateOf(profile.isPublicBloodGroup) }
    var isPublicPhone by remember(profile) { mutableStateOf(profile.isPublicEmergencyContact) }

    // Generate public QR text payload based on user privacy choices
    val qrPayload = buildString {
        append("LIFEMATE EMERGENCY PROFILE:\n")
        append("Name: $fullName\n")
        if (isPublicBlood) append("Blood Group: $bloodGroup\n")
        if (isPublicPhone) append("Emergency Contact: $primaryContactName ($primaryContactPhone)\n")
        append("Medical/Allergies: $allergies\n")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // QR Code Display Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "bn") "জরুরি কিউআর কোড" else "Scan in Emergency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lang == "bn") "যে কেউ স্ক্যান করলে শুধু আপনার অনুমোদিত তথ্য দেখতে পাবে" else "Scanning reveals only information you mark as public",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    QrCodeView(data = qrPayload, size = 180.dp)
                }
            }
        }

        // Details / Edit Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "bn") "মেডিকেল ও কন্টাক্ট তথ্য" else "Emergency Medical Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            if (isEditing) {
                                onSave(
                                    profile.copy(
                                        fullName = fullName,
                                        bloodGroup = bloodGroup,
                                        primaryContactName = primaryContactName,
                                        primaryContactPhone = primaryContactPhone,
                                        allergies = allergies,
                                        medicalNotes = medicalNotes,
                                        isPublicBloodGroup = isPublicBlood,
                                        isPublicEmergencyContact = isPublicPhone
                                    )
                                )
                            }
                            isEditing = !isEditing
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = PrimaryBlue
                            )
                        }
                    }

                    if (!isEditing) {
                        InfoRow(label = "Full Name", value = fullName)
                        InfoRow(label = "Blood Group", value = bloodGroup, highlight = true)
                        InfoRow(label = "Emergency Contact", value = "$primaryContactName ($primaryContactPhone)")
                        InfoRow(label = "Allergies", value = allergies)
                        InfoRow(label = "Medical Notes", value = medicalNotes)

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$primaryContactPhone")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Emergency Contact")
                        }
                    } else {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group (e.g. O+, A+, B+)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = primaryContactName,
                            onValueChange = { primaryContactName = it },
                            label = { Text("Emergency Contact Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = primaryContactPhone,
                            onValueChange = { primaryContactPhone = it },
                            label = { Text("Emergency Contact Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("Allergies") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = medicalNotes,
                            onValueChange = { medicalNotes = it },
                            label = { Text("Medical Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isPublicBlood, onCheckedChange = { isPublicBlood = it })
                            Text("Show Blood Group in QR scan", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isPublicPhone, onCheckedChange = { isPublicPhone = it })
                            Text("Show Emergency Phone in QR scan", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) AccentRose else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ==========================================
// 2. SAFE ARRIVAL CHECK-IN
// ==========================================
@Composable
fun SafeArrivalScreen(
    session: SafeArrivalSession?,
    lang: String,
    onStart: (String, String, String, Int) -> Unit,
    onArrived: (SafeArrivalSession) -> Unit
) {
    var destination by remember { mutableStateOf("University Campus") }
    var contactName by remember { mutableStateOf("Family") }
    var contactPhone by remember { mutableStateOf("01711223344") }
    var durationMinutes by remember { mutableStateOf(30) }

    val isOngoing = session != null && !session.isArrived && session.deadline > System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (lang == "bn") "নিরাপদে পৌঁছানোর চেক-ইন" else "Safe Arrival Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lang == "bn") "যেমন: বিশ্ববিদ্যালয়ে যাওয়ার সময় চালু করুন। সময়মতো কনফার্ম না করলে পরিবারকে সতর্ক করুন।" else "Start a safety countdown when commuting. If unconfirmed, easily notify your trusted family contact.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isOngoing && session != null) {
                        val remainingMinutes = ((session.deadline - System.currentTimeMillis()) / (60 * 1000)).coerceAtLeast(0)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SecondaryContainerLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Destination: ${session.destination}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryTeal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⏳ $remainingMinutes mins remaining",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SecondaryTeal
                                )
                                Text(
                                    text = "Trusted Contact: ${session.trustedContactName} (${session.trustedContactPhone})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryTeal.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onArrived(session) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(L10n.t("arrived_safely", lang))
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("Destination (e.g. University, Office, Home)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Trusted Contact Name (e.g. Father, Spouse)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Trusted Contact Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Duration (Minutes): $durationMinutes mins", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = durationMinutes.toFloat(),
                            onValueChange = { durationMinutes = it.toInt() },
                            valueRange = 10f..120f,
                            steps = 10
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onStart(destination, contactName, contactPhone, durationMinutes) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (lang == "bn") "সেফটি টাইমার শুরু করুন" else "Start Safe Arrival Timer")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. LOST ITEM TRACKER
// ==========================================
@Composable
fun LostItemsScreen(
    items: List<LostItem>,
    lang: String,
    onAdd: (String, String, String, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = items.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.lastKnownLocation.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(L10n.t("search_item_hint", lang), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${L10n.t("lost_items", lang)} (${filtered.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { showAddDialog = true }) {
                Text(L10n.t("add_item", lang))
            }
        }

        if (filtered.isEmpty()) {
            EmptyPlaceholder(
                message = if (lang == "bn") "কোনো সংরক্ষিত জিনিসপত্র পাওয়া যায়নি।" else "No items tracked yet. Save locations for your keys, passport, and wallet!",
                icon = Icons.Outlined.SearchOff
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { item ->
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
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "📍 ${item.lastKnownLocation}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryBlue
                                )
                                if (item.notes.isNotBlank()) {
                                    Text(
                                        text = item.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDelete(item.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Keys") }
        var location by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (lang == "bn") "জিনিসপত্র সংরক্ষণ করুন" else "Track Important Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name (e.g. Car Keys, Passport, Charger)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Last Known Location (e.g. Study desk top drawer)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && location.isNotBlank()) {
                        onAdd(name, category, location, notes)
                        showAddDialog = false
                    }
                }) {
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

// ==========================================
// 4. STUDENT MODE SCREEN
// ==========================================
@Composable
fun StudentModeScreen(
    schedules: List<StudentSchedule>,
    assignments: List<StudentAssignment>,
    attendances: List<StudentAttendance>,
    lang: String,
    onAddSchedule: (String, String, String, String) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onAddAssignment: (String, String, Long) -> Unit,
    onToggleAssignment: (StudentAssignment) -> Unit,
    onDeleteAssignment: (Long) -> Unit,
    onUpdateAttendance: (String, Int, Int, Long) -> Unit,
    onDeleteAttendance: (Long) -> Unit
) {
    var studentTab by remember { mutableStateOf(0) } // 0: Routine, 1: Assignments, 2: Attendance, 3: Study Timer / CGPA

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = studentTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TertiaryIndigo
        ) {
            Tab(selected = studentTab == 0, onClick = { studentTab = 0 }, text = { Text("Routine", fontSize = 12.sp) })
            Tab(selected = studentTab == 1, onClick = { studentTab = 1 }, text = { Text("Tasks", fontSize = 12.sp) })
            Tab(selected = studentTab == 2, onClick = { studentTab = 2 }, text = { Text("Attendance", fontSize = 12.sp) })
            Tab(selected = studentTab == 3, onClick = { studentTab = 3 }, text = { Text("Study Tools", fontSize = 12.sp) })
        }

        when (studentTab) {
            0 -> {
                // Class Routine
                var showAddRoutine by remember { mutableStateOf(false) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weekly Routine (${schedules.size})", fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddRoutine = true }, shape = RoundedCornerShape(10.dp)) {
                                Text("+ Class")
                            }
                        }
                    }

                    if (schedules.isEmpty()) {
                        item { EmptyPlaceholder("No classes added. Tap '+ Class' to build routine.", Icons.Default.CalendarToday) }
                    } else {
                        items(schedules) { sch ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sch.subject, fontWeight = FontWeight.Bold)
                                        Text("${sch.dayOfWeek} • ${sch.time} • ${sch.room}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { onDeleteSchedule(sch.id) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddRoutine) {
                    var day by remember { mutableStateOf("Monday") }
                    var subject by remember { mutableStateOf("") }
                    var time by remember { mutableStateOf("09:00 AM - 10:30 AM") }
                    var room by remember { mutableStateOf("Room 301") }

                    AlertDialog(
                        onDismissRequest = { showAddRoutine = false },
                        title = { Text("Add Class Schedule") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (e.g. Data Structures)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day (e.g. Monday, Tuesday)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g. 10:00 AM - 11:30 AM)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room / Lab") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (subject.isNotBlank()) {
                                    onAddSchedule(day, subject, time, room)
                                    showAddRoutine = false
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showAddRoutine = false }) { Text("Cancel") } }
                    )
                }
            }

            1 -> {
                // Assignments
                var showAddAssn by remember { mutableStateOf(false) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Assignments & Exams (${assignments.size})", fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddAssn = true }, shape = RoundedCornerShape(10.dp)) {
                                Text("+ Task")
                            }
                        }
                    }

                    if (assignments.isEmpty()) {
                        item { EmptyPlaceholder("No student assignments. Tap '+ Task' to add.", Icons.Default.Assignment) }
                    } else {
                        items(assignments) { assn ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = assn.isCompleted, onCheckedChange = { onToggleAssignment(assn) })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(assn.title, fontWeight = FontWeight.Bold)
                                        Text(assn.subject, style = MaterialTheme.typography.bodySmall, color = TertiaryIndigo)
                                    }
                                    IconButton(onClick = { onDeleteAssignment(assn.id) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddAssn) {
                    var sub by remember { mutableStateOf("") }
                    var title by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAddAssn = false },
                        title = { Text("Add Assignment / Exam") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = sub, onValueChange = { sub = it }, label = { Text("Subject (e.g. Physics)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g. Lab Report 2)") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (title.isNotBlank()) {
                                    onAddAssignment(sub, title, System.currentTimeMillis() + 86400000L * 3)
                                    showAddAssn = false
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showAddAssn = false }) { Text("Cancel") } }
                    )
                }
            }

            2 -> {
                // Attendance Tracker
                var showAddAtt by remember { mutableStateOf(false) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subject Attendance (Target: 75%+)", fontWeight = FontWeight.Bold)
                            Button(onClick = { showAddAtt = true }, shape = RoundedCornerShape(10.dp)) {
                                Text("+ Subject")
                            }
                        }
                    }

                    if (attendances.isEmpty()) {
                        item { EmptyPlaceholder("Track attendance to ensure you meet 75% target.", Icons.Default.CheckCircle) }
                    } else {
                        items(attendances) { att ->
                            val pct = if (att.totalClasses > 0) (att.attendedClasses * 100) / att.totalClasses else 100
                            val isSafe = pct >= 75
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(att.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("$pct%", fontWeight = FontWeight.ExtraBold, color = if (isSafe) AccentEmerald else AccentRose)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = if (isSafe) AccentEmerald else AccentRose
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${att.attendedClasses} / ${att.totalClasses} classes attended", style = MaterialTheme.typography.bodySmall)
                                        Row {
                                            IconButton(onClick = {
                                                onUpdateAttendance(att.subject, att.attendedClasses + 1, att.totalClasses + 1, att.id)
                                            }) { Icon(Icons.Default.AddCircle, contentDescription = "Present", tint = AccentEmerald) }
                                            IconButton(onClick = {
                                                onUpdateAttendance(att.subject, att.attendedClasses, att.totalClasses + 1, att.id)
                                            }) { Icon(Icons.Default.RemoveCircle, contentDescription = "Absent", tint = AccentRose) }
                                            IconButton(onClick = { onDeleteAttendance(att.id) }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddAtt) {
                    var sub by remember { mutableStateOf("") }
                    var att by remember { mutableStateOf("15") }
                    var tot by remember { mutableStateOf("18") }
                    AlertDialog(
                        onDismissRequest = { showAddAtt = false },
                        title = { Text("Add Subject Attendance") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = sub, onValueChange = { sub = it }, label = { Text("Subject Name") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = att, onValueChange = { att = it }, label = { Text("Attended Classes") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = tot, onValueChange = { tot = it }, label = { Text("Total Classes") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (sub.isNotBlank()) {
                                    onUpdateAttendance(sub, att.toIntOrNull() ?: 0, tot.toIntOrNull() ?: 0, 0)
                                    showAddAtt = false
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showAddAtt = false }) { Text("Cancel") } }
                    )
                }
            }

            3 -> {
                // Study Pomodoro & CGPA Calculator
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Study Timer Card
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏱️ Focus Study Timer (Pomodoro)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("25:00", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { /* Start timer */ }) { Text("Start Focus (25m)") }
                                    OutlinedButton(onClick = { /* Break */ }) { Text("Break (5m)") }
                                }
                            }
                        }
                    }

                    // CGPA Estimator Card
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            var credit1 by remember { mutableStateOf("3") }
                            var gpa1 by remember { mutableStateOf("3.75") }
                            var credit2 by remember { mutableStateOf("3") }
                            var gpa2 by remember { mutableStateOf("4.00") }

                            val c1 = credit1.toDoubleOrNull() ?: 0.0
                            val g1 = gpa1.toDoubleOrNull() ?: 0.0
                            val c2 = credit2.toDoubleOrNull() ?: 0.0
                            val g2 = gpa2.toDoubleOrNull() ?: 0.0
                            val totalCredits = (c1 + c2).coerceAtLeast(1.0)
                            val cgpa = ((c1 * g1) + (c2 * g2)) / totalCredits

                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("🎓 Quick CGPA Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = credit1, onValueChange = { credit1 = it }, label = { Text("Course 1 Credits") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = gpa1, onValueChange = { gpa1 = it }, label = { Text("Grade Point") }, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = credit2, onValueChange = { credit2 = it }, label = { Text("Course 2 Credits") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = gpa2, onValueChange = { gpa2 = it }, label = { Text("Grade Point") }, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    color = PrimaryContainerLight,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Calculated CGPA", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue)
                                        Text(String.format(Locale.getDefault(), "%.2f", cgpa), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    profile: UserProfile,
    lang: String,
    onUpdateLanguage: (String) -> Unit,
    onUpdateDarkMode: (Boolean?) -> Unit,
    onUpdatePin: (String) -> Unit,
    onUpdateUser: (String, String, String) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var role by remember(profile) { mutableStateOf(profile.role) }
    var pin by remember(profile) { mutableStateOf(profile.vaultPin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(L10n.t("settings", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role (Student / Professional / Family)") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = { onUpdateUser(name, email, role) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(L10n.t("save", lang))
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Language & Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Language")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = profile.language == "en",
                                onClick = { onUpdateLanguage("en") },
                                label = { Text("English") }
                            )
                            FilterChip(
                                selected = profile.language == "bn",
                                onClick = { onUpdateLanguage("bn") },
                                label = { Text("বাংলা") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode")
                        Switch(
                            checked = profile.isDarkMode == true,
                            onCheckedChange = { onUpdateDarkMode(it) }
                        )
                    }

                    Divider()

                    Text("Document Vault Privacy PIN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4) pin = it
                        },
                        label = { Text("4-digit Security PIN (empty for none)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(onClick = { onUpdatePin(pin) }) {
                        Text("Update Security PIN")
                    }
                }
            }
        }
    }
}
