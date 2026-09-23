package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeMateDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, targetTime ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND targetTime <= :endOfDay ORDER BY targetTime ASC")
    fun getTodayReminders(endOfDay: Long): Flow<List<ReminderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(item: ReminderItem): Long

    @Update
    suspend fun updateReminder(item: ReminderItem)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expenses WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(item: ExpenseItem): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    // Dharkhata / Borrow & Lend
    @Query("SELECT * FROM dharkhata ORDER BY isSettled ASC, expectedReturnDate ASC")
    fun getAllDharkhata(): Flow<List<DharkhataItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDharkhata(item: DharkhataItem): Long

    @Update
    suspend fun updateDharkhata(item: DharkhataItem)

    @Query("DELETE FROM dharkhata WHERE id = :id")
    suspend fun deleteDharkhataById(id: Long)

    // Commitments
    @Query("SELECT * FROM commitments ORDER BY isFulfilled ASC, deadline ASC")
    fun getAllCommitments(): Flow<List<CommitmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(item: CommitmentItem): Long

    @Update
    suspend fun updateCommitment(item: CommitmentItem)

    @Query("DELETE FROM commitments WHERE id = :id")
    suspend fun deleteCommitmentById(id: Long)

    // Documents
    @Query("SELECT * FROM vault_documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<VaultDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: VaultDocument): Long

    @Query("DELETE FROM vault_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    // Lost Items
    @Query("SELECT * FROM lost_items ORDER BY updatedAt DESC")
    fun getAllLostItems(): Flow<List<LostItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLostItem(item: LostItem): Long

    @Query("DELETE FROM lost_items WHERE id = :id")
    suspend fun deleteLostItemById(id: Long)

    // Emergency Profile
    @Query("SELECT * FROM emergency_profile WHERE id = 1 LIMIT 1")
    fun getEmergencyProfile(): Flow<EmergencyProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEmergencyProfile(profile: EmergencyProfile)

    // Safe Arrival
    @Query("SELECT * FROM safe_arrival ORDER BY startedAt DESC LIMIT 1")
    fun getLatestSafeArrival(): Flow<SafeArrivalSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeArrival(session: SafeArrivalSession): Long

    @Update
    suspend fun updateSafeArrival(session: SafeArrivalSession)

    // Student section
    @Query("SELECT * FROM student_schedule ORDER BY id ASC")
    fun getStudentSchedule(): Flow<List<StudentSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentSchedule(schedule: StudentSchedule): Long

    @Query("DELETE FROM student_schedule WHERE id = :id")
    suspend fun deleteStudentSchedule(id: Long)

    @Query("SELECT * FROM student_assignments ORDER BY isCompleted ASC, dueDate ASC")
    fun getStudentAssignments(): Flow<List<StudentAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAssignment(assignment: StudentAssignment): Long

    @Update
    suspend fun updateStudentAssignment(assignment: StudentAssignment)

    @Query("DELETE FROM student_assignments WHERE id = :id")
    suspend fun deleteStudentAssignment(id: Long)

    @Query("SELECT * FROM student_attendance ORDER BY subject ASC")
    fun getStudentAttendance(): Flow<List<StudentAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAttendance(attendance: StudentAttendance): Long

    @Update
    suspend fun updateStudentAttendance(attendance: StudentAttendance)

    @Query("DELETE FROM student_attendance WHERE id = :id")
    suspend fun deleteStudentAttendance(id: Long)
}
