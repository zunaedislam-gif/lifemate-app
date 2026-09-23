package com.example.data.repository

import com.example.data.local.LifeMateDao
import com.example.data.model.*
import com.example.data.nlp.SmartParser
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LifeMateRepository(private val dao: LifeMateDao) {

    // User Profile
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    suspend fun saveProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    // Reminders
    val allReminders: Flow<List<ReminderItem>> = dao.getAllReminders()

    fun getTodayReminders(): Flow<List<ReminderItem>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return dao.getTodayReminders(cal.timeInMillis)
    }

    suspend fun insertReminder(item: ReminderItem) = dao.insertReminder(item)
    suspend fun updateReminder(item: ReminderItem) = dao.updateReminder(item)
    suspend fun deleteReminder(id: Long) = dao.deleteReminderById(id)

    suspend fun addSmartReminder(naturalText: String): ReminderItem {
        val parsed = SmartParser.parseReminder(naturalText)
        val item = ReminderItem(
            title = parsed.title,
            targetTime = parsed.targetTime,
            recurrence = parsed.recurrence,
            category = parsed.category
        )
        val id = dao.insertReminder(item)
        return item.copy(id = id)
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseItem>> = dao.getAllExpenses()

    fun getTodayExpenses(): Flow<List<ExpenseItem>> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return dao.getExpensesBetween(startCal.timeInMillis, endCal.timeInMillis)
    }

    suspend fun insertExpense(item: ExpenseItem) = dao.insertExpense(item)
    suspend fun deleteExpense(id: Long) = dao.deleteExpenseById(id)

    suspend fun addSmartExpense(naturalText: String): ExpenseItem? {
        val parsed = SmartParser.parseExpense(naturalText) ?: return null
        val item = ExpenseItem(
            title = parsed.title,
            amount = parsed.amount,
            category = parsed.category
        )
        val id = dao.insertExpense(item)
        return item.copy(id = id)
    }

    // Dharkhata (Borrow & Lend)
    val allDharkhata: Flow<List<DharkhataItem>> = dao.getAllDharkhata()
    suspend fun insertDharkhata(item: DharkhataItem) = dao.insertDharkhata(item)
    suspend fun updateDharkhata(item: DharkhataItem) = dao.updateDharkhata(item)
    suspend fun deleteDharkhata(id: Long) = dao.deleteDharkhataById(id)

    // Commitments
    val allCommitments: Flow<List<CommitmentItem>> = dao.getAllCommitments()
    suspend fun insertCommitment(item: CommitmentItem) = dao.insertCommitment(item)
    suspend fun updateCommitment(item: CommitmentItem) = dao.updateCommitment(item)
    suspend fun deleteCommitment(id: Long) = dao.deleteCommitmentById(id)

    suspend fun addSmartCommitment(naturalText: String): CommitmentItem {
        val parsed = SmartParser.parseCommitment(naturalText)
        val item = CommitmentItem(
            title = parsed.title,
            person = parsed.person,
            deadline = parsed.deadline,
            category = parsed.category
        )
        val id = dao.insertCommitment(item)
        return item.copy(id = id)
    }

    // Vault Documents
    val allDocuments: Flow<List<VaultDocument>> = dao.getAllDocuments()
    suspend fun insertDocument(doc: VaultDocument) = dao.insertDocument(doc)
    suspend fun deleteDocument(id: Long) = dao.deleteDocumentById(id)

    // Lost Items
    val allLostItems: Flow<List<LostItem>> = dao.getAllLostItems()
    suspend fun insertLostItem(item: LostItem) = dao.insertLostItem(item)
    suspend fun deleteLostItem(id: Long) = dao.deleteLostItemById(id)

    // Emergency Profile
    val emergencyProfile: Flow<EmergencyProfile?> = dao.getEmergencyProfile()
    suspend fun saveEmergencyProfile(profile: EmergencyProfile) = dao.saveEmergencyProfile(profile)

    // Safe Arrival
    val latestSafeArrival: Flow<SafeArrivalSession?> = dao.getLatestSafeArrival()
    suspend fun insertSafeArrival(session: SafeArrivalSession) = dao.insertSafeArrival(session)
    suspend fun updateSafeArrival(session: SafeArrivalSession) = dao.updateSafeArrival(session)

    // Student section
    val studentSchedule: Flow<List<StudentSchedule>> = dao.getStudentSchedule()
    suspend fun insertStudentSchedule(schedule: StudentSchedule) = dao.insertStudentSchedule(schedule)
    suspend fun deleteStudentSchedule(id: Long) = dao.deleteStudentSchedule(id)

    val studentAssignments: Flow<List<StudentAssignment>> = dao.getStudentAssignments()
    suspend fun insertStudentAssignment(assignment: StudentAssignment) = dao.insertStudentAssignment(assignment)
    suspend fun updateStudentAssignment(assignment: StudentAssignment) = dao.updateStudentAssignment(assignment)
    suspend fun deleteStudentAssignment(id: Long) = dao.deleteStudentAssignment(id)

    val studentAttendance: Flow<List<StudentAttendance>> = dao.getStudentAttendance()
    suspend fun insertStudentAttendance(attendance: StudentAttendance) = dao.insertStudentAttendance(attendance)
    suspend fun updateStudentAttendance(attendance: StudentAttendance) = dao.updateStudentAttendance(attendance)
    suspend fun deleteStudentAttendance(id: Long) = dao.deleteStudentAttendance(id)
}
