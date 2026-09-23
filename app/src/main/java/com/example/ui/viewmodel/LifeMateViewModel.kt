package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAssistantService
import com.example.data.ai.AiResponse
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.LifeMateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChatMessage(
    val sender: String, // "user", "assistant"
    val text: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class LifeMateViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = LifeMateRepository(db.dao())
    private val aiService = AiAssistantService(repository)

    // User Profile
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Reminders
    val allReminders: StateFlow<List<ReminderItem>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayReminders: StateFlow<List<ReminderItem>> = repository.getTodayReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val allExpenses: StateFlow<List<ExpenseItem>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayExpenses: StateFlow<List<ExpenseItem>> = repository.getTodayExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dharkhata
    val allDharkhata: StateFlow<List<DharkhataItem>> = repository.allDharkhata
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Commitments
    val allCommitments: StateFlow<List<CommitmentItem>> = repository.allCommitments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Documents
    val allDocuments: StateFlow<List<VaultDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lost items
    val allLostItems: StateFlow<List<LostItem>> = repository.allLostItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Emergency profile
    val emergencyProfile: StateFlow<EmergencyProfile> = repository.emergencyProfile
        .map { it ?: EmergencyProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmergencyProfile())

    // Safe arrival
    val latestSafeArrival: StateFlow<SafeArrivalSession?> = repository.latestSafeArrival
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Student Section
    val studentSchedule: StateFlow<List<StudentSchedule>> = repository.studentSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentAssignments: StateFlow<List<StudentAssignment>> = repository.studentAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentAttendance: StateFlow<List<StudentAttendance>> = repository.studentAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            sender = "assistant",
            text = "Hello! I am your LifeMate AI. You can write reminders, record expenses (e.g. 'Tea 20'), ask 'Who owes me money?', or 'Where did I keep my passport?'"
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        // Seed default profile or starter entries if database is pristine
        viewModelScope.launch {
            repository.userProfile.firstOrNull()?.let {
                if (it == null) {
                    repository.saveProfile(UserProfile(name = "Rahim Ahmed", email = "rahim@lifemate.app", role = "Student & Professional", language = "en"))
                }
            }
            repository.emergencyProfile.firstOrNull()?.let {
                if (it == null) {
                    repository.saveEmergencyProfile(
                        EmergencyProfile(
                            fullName = "Rahim Ahmed",
                            bloodGroup = "B+",
                            primaryContactName = "Mother (Nasima)",
                            primaryContactPhone = "+8801711223344",
                            primaryContactRelation = "Mother",
                            secondaryContactName = "Hasan (Brother)",
                            secondaryContactPhone = "+8801811556677",
                            secondaryContactRelation = "Brother",
                            allergies = "Penicillin, Dust",
                            medicalNotes = "Asthma inhaler in backpack"
                        )
                    )
                }
            }
        }
    }

    // Actions
    fun addSmartReminder(text: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.addSmartReminder(text)
            onDone?.invoke()
        }
    }

    fun toggleReminder(item: ReminderItem) {
        viewModelScope.launch {
            repository.updateReminder(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch { repository.deleteReminder(id) }
    }

    fun addSmartExpense(text: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.addSmartExpense(text)
            onDone?.invoke()
        }
    }

    fun addCustomExpense(title: String, amount: Double, category: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertExpense(ExpenseItem(title = title, amount = amount, category = category))
            onDone?.invoke()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch { repository.deleteExpense(id) }
    }

    fun addDharkhata(person: String, amount: Double, type: String, phone: String, dueDate: Long, notes: String = "", onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertDharkhata(
                DharkhataItem(
                    personName = person,
                    amount = amount,
                    type = type,
                    phone = phone,
                    expectedReturnDate = dueDate,
                    notes = notes
                )
            )
            onDone?.invoke()
        }
    }

    fun toggleDharkhataSettled(item: DharkhataItem) {
        viewModelScope.launch {
            repository.updateDharkhata(item.copy(isSettled = !item.isSettled))
        }
    }

    fun deleteDharkhata(id: Long) {
        viewModelScope.launch { repository.deleteDharkhata(id) }
    }

    fun addCommitment(title: String, person: String, deadline: Long, category: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertCommitment(
                CommitmentItem(
                    title = title,
                    person = person,
                    deadline = deadline,
                    category = category
                )
            )
            onDone?.invoke()
        }
    }

    fun addSmartCommitment(text: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.addSmartCommitment(text)
            onDone?.invoke()
        }
    }

    fun toggleCommitment(item: CommitmentItem) {
        viewModelScope.launch {
            repository.updateCommitment(item.copy(isFulfilled = !item.isFulfilled))
        }
    }

    fun deleteCommitment(id: Long) {
        viewModelScope.launch { repository.deleteCommitment(id) }
    }

    fun addDocument(title: String, category: String, docNumber: String, expiryDate: Long?, notes: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertDocument(
                VaultDocument(
                    title = title,
                    category = category,
                    documentNumber = docNumber,
                    expiryDate = expiryDate,
                    notes = notes
                )
            )
            onDone?.invoke()
        }
    }

    fun deleteDocument(id: Long) {
        viewModelScope.launch { repository.deleteDocument(id) }
    }

    fun addLostItem(name: String, category: String, location: String, notes: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertLostItem(
                LostItem(
                    name = name,
                    category = category,
                    lastKnownLocation = location,
                    notes = notes
                )
            )
            onDone?.invoke()
        }
    }

    fun deleteLostItem(id: Long) {
        viewModelScope.launch { repository.deleteLostItem(id) }
    }

    fun saveEmergencyProfile(profile: EmergencyProfile, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.saveEmergencyProfile(profile)
            onDone?.invoke()
        }
    }

    fun startSafeArrival(destination: String, contactName: String, contactPhone: String, minutes: Int, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val deadline = System.currentTimeMillis() + (minutes * 60 * 1000L)
            repository.insertSafeArrival(
                SafeArrivalSession(
                    destination = destination,
                    trustedContactName = contactName,
                    trustedContactPhone = contactPhone,
                    deadline = deadline,
                    isArrived = false
                )
            )
            onDone?.invoke()
        }
    }

    fun confirmSafeArrival(session: SafeArrivalSession) {
        viewModelScope.launch {
            repository.updateSafeArrival(session.copy(isArrived = true))
        }
    }

    // Student tools
    fun addSchedule(day: String, subject: String, time: String, room: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertStudentSchedule(StudentSchedule(dayOfWeek = day, subject = subject, time = time, room = room))
            onDone?.invoke()
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch { repository.deleteStudentSchedule(id) }
    }

    fun addAssignment(subject: String, title: String, dueDate: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertStudentAssignment(StudentAssignment(subject = subject, title = title, dueDate = dueDate))
            onDone?.invoke()
        }
    }

    fun toggleAssignment(assignment: StudentAssignment) {
        viewModelScope.launch {
            repository.updateStudentAssignment(assignment.copy(isCompleted = !assignment.isCompleted))
        }
    }

    fun deleteAssignment(id: Long) {
        viewModelScope.launch { repository.deleteStudentAssignment(id) }
    }

    fun addOrUpdateAttendance(subject: String, attended: Int, total: Int, id: Long = 0, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (id > 0) {
                repository.updateStudentAttendance(StudentAttendance(id = id, subject = subject, attendedClasses = attended, totalClasses = total))
            } else {
                repository.insertStudentAttendance(StudentAttendance(subject = subject, attendedClasses = attended, totalClasses = total))
            }
            onDone?.invoke()
        }
    }

    fun deleteAttendance(id: Long) {
        viewModelScope.launch { repository.deleteStudentAttendance(id) }
    }

    // User settings
    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val curr = userProfile.value
            repository.saveProfile(curr.copy(language = lang))
        }
    }

    fun updateDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            val curr = userProfile.value
            repository.saveProfile(curr.copy(isDarkMode = isDark))
        }
    }

    fun updateVaultPin(pin: String) {
        viewModelScope.launch {
            val curr = userProfile.value
            repository.saveProfile(curr.copy(vaultPin = pin))
        }
    }

    fun updateUser(name: String, email: String, role: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val curr = userProfile.value
            repository.saveProfile(curr.copy(name = name, email = email, role = role))
            onDone?.invoke()
        }
    }

    // AI Query
    fun askAi(query: String) {
        val userMsg = ChatMessage(sender = "user", text = query)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val lang = userProfile.value.language
            val response = aiService.processQuery(
                query = query,
                reminders = allReminders.value,
                expenses = allExpenses.value,
                dharkhata = allDharkhata.value,
                commitments = allCommitments.value,
                documents = allDocuments.value,
                lostItems = allLostItems.value,
                lang = lang
            )
            _isAiLoading.value = false
            when (response) {
                is AiResponse.Text -> {
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = "assistant", text = response.message)
                }
                is AiResponse.ActionCompleted -> {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        sender = "assistant",
                        text = response.message,
                        details = response.details
                    )
                }
            }
        }
    }
}
