package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "John Doe",
    val email: String = "user@lifemate.app",
    val role: String = "General", // "General", "Student", "Family", "Professional"
    val language: String = "en", // "en" or "bn"
    val isDarkMode: Boolean? = null, // null for system
    val vaultPin: String = "",
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetTime: Long,
    val recurrence: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY
    val category: String = "General", // Call, Bill, Health, Study, Work, General
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Food & Tea, Transport, Groceries, Utilities, Shopping, Health, Other
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "dharkhata")
data class DharkhataItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: String, // "RECEIVE" (I lent to them), "PAY" (I borrowed from them)
    val date: Long = System.currentTimeMillis(),
    val expectedReturnDate: Long = System.currentTimeMillis() + 7 * 86400000L,
    val isSettled: Boolean = false,
    val phone: String = "",
    val notes: String = ""
)

@Entity(tableName = "commitments")
data class CommitmentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val person: String = "",
    val deadline: Long,
    val category: String = "Personal", // Work, Personal, Family, Finance
    val isFulfilled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_documents")
data class VaultDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // NID, Passport, Birth Certificate, Driving License, Certificate, CV, Other
    val documentNumber: String = "",
    val expiryDate: Long? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lost_items")
data class LostItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Item", // Keys, Wallet, Charger, Books, Documents, Bags, Other
    val lastKnownLocation: String,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_profile")
data class EmergencyProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "LifeMate User",
    val bloodGroup: String = "O+",
    val primaryContactName: String = "Emergency Contact",
    val primaryContactPhone: String = "01700000000",
    val primaryContactRelation: String = "Parent/Spouse",
    val secondaryContactName: String = "",
    val secondaryContactPhone: String = "",
    val secondaryContactRelation: String = "",
    val allergies: String = "None known",
    val medicalNotes: String = "None",
    val isPublicBloodGroup: Boolean = true,
    val isPublicEmergencyContact: Boolean = true,
    val isPublicMedicalInfo: Boolean = true
)

@Entity(tableName = "safe_arrival")
data class SafeArrivalSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destination: String,
    val trustedContactName: String,
    val trustedContactPhone: String,
    val deadline: Long,
    val isArrived: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_schedule")
data class StudentSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: String, // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    val subject: String,
    val time: String,
    val room: String = ""
)

@Entity(tableName = "student_assignments")
data class StudentAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val title: String,
    val dueDate: Long,
    val isCompleted: Boolean = false
)

@Entity(tableName = "student_attendance")
data class StudentAttendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val attendedClasses: Int = 0,
    val totalClasses: Int = 0
)
