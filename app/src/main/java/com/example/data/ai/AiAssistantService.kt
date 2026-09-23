package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.*
import com.example.data.nlp.SmartParser
import com.example.data.repository.LifeMateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

sealed class AiResponse {
    data class Text(val message: String, val actionTaken: String? = null) : AiResponse()
    data class ActionCompleted(val message: String, val details: String) : AiResponse()
}

class AiAssistantService(private val repository: LifeMateRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    suspend fun processQuery(
        query: String,
        reminders: List<ReminderItem>,
        expenses: List<ExpenseItem>,
        dharkhata: List<DharkhataItem>,
        commitments: List<CommitmentItem>,
        documents: List<VaultDocument>,
        lostItems: List<LostItem>,
        lang: String = "en"
    ): AiResponse = withContext(Dispatchers.IO) {
        val lower = query.lowercase(Locale.ROOT).trim()

        // 1. Action: Create Reminder
        if (lower.startsWith("remind") || lower.contains("remind me")) {
            val item = repository.addSmartReminder(query)
            val dateStr = dateFormatter.format(Date(item.targetTime))
            val msg = if (lang == "bn") {
                "✅ রিমাইন্ডার তৈরি করা হয়েছে: '${item.title}' ($dateStr)"
            } else {
                "✅ Reminder created: '${item.title}' for $dateStr (${item.category})"
            }
            return@withContext AiResponse.ActionCompleted(
                message = msg,
                details = "Category: ${item.category} | Recurrence: ${item.recurrence}"
            )
        }

        // 2. Action: Quick Expense Entry
        if (lower.startsWith("spent") || lower.startsWith("cost") || query.matches(Regex(".+\\s+\\d+(\\.\\d+)?.*"))) {
            val exp = repository.addSmartExpense(query)
            if (exp != null) {
                val msg = if (lang == "bn") {
                    "💰 খরচ যোগ করা হয়েছে: ${exp.title} - ৳${exp.amount.toInt()} [${exp.category}]"
                } else {
                    "💰 Added expense: ${exp.title} - ৳${exp.amount} (${exp.category})"
                }
                return@withContext AiResponse.ActionCompleted(message = msg, details = "Category: ${exp.category}")
            }
        }

        // 3. Query: Who owes me money? (Dharkhata)
        if (lower.contains("who owes") || lower.contains("owe me") || lower.contains("dharkhata") || lower.contains("পাওনা") || lower.contains("টাকা পাবে")) {
            val lent = dharkhata.filter { it.type == "RECEIVE" && !it.isSettled }
            if (lent.isEmpty()) {
                val msg = if (lang == "bn") "কোনো ব্যক্তি বর্তমানে আপনার কাছে টাকা দেনা নেই।" else "No one currently owes you money! All cleared."
                return@withContext AiResponse.Text(msg)
            }
            val total = lent.sumOf { it.amount }
            val listStr = lent.joinToString("\n") { "• ${it.personName}: ৳${it.amount.toInt()} (Due: ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.expectedReturnDate))})" }
            val msg = if (lang == "bn") {
                "💵 আপনি মোট ৳${total.toInt()} পাবেন:\n$listStr"
            } else {
                "💵 Total to receive: ৳$total\n$listStr"
            }
            return@withContext AiResponse.Text(msg)
        }

        // 4. Query: How much did I spend this month / today / where spending most?
        if (lower.contains("how much") || lower.contains("spend") || lower.contains("expense") || lower.contains("খরচ") || lower.contains("সবচেয়ে বেশি")) {
            val totalAll = expenses.sumOf { it.amount }
            val categoryMap = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
            val topCategory = categoryMap.maxByOrNull { it.value }

            if (lower.contains("most") || lower.contains("বেশি")) {
                if (topCategory != null) {
                    val msg = if (lang == "bn") {
                        "📊 আপনি সবচেয়ে বেশি খরচ করেছেন '${topCategory.key}' খাতে — মোট ৳${topCategory.value.toInt()}।"
                    } else {
                        "📊 You are spending the most money on '${topCategory.key}' — total ৳${topCategory.value.toInt()} (${(topCategory.value / (totalAll.coerceAtLeast(1.0)) * 100).toInt()}% of total expenses)."
                    }
                    return@withContext AiResponse.Text(msg)
                }
            }

            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val monthlyExpenses = expenses.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.get(Calendar.MONTH) == currentMonth
            }
            val monthTotal = monthlyExpenses.sumOf { it.amount }

            val msg = if (lang == "bn") {
                "💰 এই মাসের মোট খরচ: ৳${monthTotal.toInt()}\nসব সময়ের সর্বমোট: ৳${totalAll.toInt()}" +
                        (if (topCategory != null) "\nশীর্ষ খরচের খাত: ${topCategory.key} (৳${topCategory.value.toInt()})" else "")
            } else {
                "💰 Total spent this month: ৳$monthTotal\nAll-time total: ৳$totalAll" +
                        (if (topCategory != null) "\nTop category: ${topCategory.key} (৳${topCategory.value.toInt()})" else "")
            }
            return@withContext AiResponse.Text(msg)
        }

        // 5. Query: What do I have to do today?
        if (lower.contains("today") || lower.contains("what do i have") || lower.contains("pending") || lower.contains("আজকে") || lower.contains("কাজ")) {
            val pendingReminders = reminders.filter { !it.isCompleted }
            val pendingCommitments = commitments.filter { !it.isFulfilled }

            val sb = StringBuilder()
            if (lang == "bn") {
                sb.append("📋 আজকের জন্য আপনার কাজ ও রিমাইন্ডার:\n")
                if (pendingReminders.isEmpty() && pendingCommitments.isEmpty()) {
                    sb.append("আজ কোনো পেন্ডিং কাজ নেই!")
                } else {
                    pendingReminders.take(3).forEach {
                        sb.append("• রিমাইন্ডার: ${it.title} (${dateFormatter.format(Date(it.targetTime))})\n")
                    }
                    pendingCommitments.take(3).forEach {
                        sb.append("• প্রতিশ্রুতি: ${it.title} (${it.person})\n")
                    }
                }
            } else {
                sb.append("📋 Your Schedule & Commitments:\n")
                if (pendingReminders.isEmpty() && pendingCommitments.isEmpty()) {
                    sb.append("No pending tasks today! You're completely free.")
                } else {
                    pendingReminders.take(3).forEach {
                        sb.append("• Reminder: ${it.title} (${dateFormatter.format(Date(it.targetTime))})\n")
                    }
                    pendingCommitments.take(3).forEach {
                        sb.append("• Commitment: ${it.title} (${it.person})\n")
                    }
                }
            }
            return@withContext AiResponse.Text(sb.toString().trim())
        }

        // 6. Query: Where did I keep my [item]? (Lost Item Tracker)
        if (lower.contains("where did i") || lower.contains("where is") || lower.contains("keep my") || lower.contains("কোথায়")) {
            val matched = lostItems.find { item ->
                lower.contains(item.name.lowercase(Locale.ROOT))
            }
            if (matched != null) {
                val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(matched.updatedAt))
                val msg = if (lang == "bn") {
                    "🔍 আপনি আপনার '${matched.name}' রেখেছিলেন:\n📍 ${matched.lastKnownLocation}\n(সংরক্ষিত নোট: ${if (matched.notes.isNotBlank()) matched.notes else "নেই"}, শেষ আপডেট: $timeStr)"
                } else {
                    "🔍 Last known location of '${matched.name}':\n📍 ${matched.lastKnownLocation}\n(Notes: ${if (matched.notes.isNotBlank()) matched.notes else "None"}, Updated: $timeStr)"
                }
                return@withContext AiResponse.Text(msg)
            } else {
                val allList = lostItems.map { it.name }.joinToString(", ")
                val msg = if (lang == "bn") {
                    "এই জিনিসটি ট্র্যাকিং তালিকায় খুঁজে পাওয়া যায়নি। সংরক্ষিত জিনিসপত্র: $allList"
                } else {
                    "Item not found in saved records. Currently tracked items: $allList"
                }
                return@withContext AiResponse.Text(msg)
            }
        }

        // 7. Query: When does my passport / NID expire? (Document Vault)
        if (lower.contains("expire") || lower.contains("passport") || lower.contains("nid") || lower.contains("মেয়াদ")) {
            val doc = documents.find {
                lower.contains(it.title.lowercase(Locale.ROOT)) || lower.contains(it.category.lowercase(Locale.ROOT))
            }
            if (doc != null) {
                val expDate = doc.expiryDate?.let { SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(it)) } ?: "No expiry date set"
                val msg = if (lang == "bn") {
                    "📄 '${doc.title}' (${doc.category}):\nনম্বর: ${doc.documentNumber}\nমেয়াদের তারিখ: $expDate"
                } else {
                    "📄 '${doc.title}' (${doc.category}):\nDoc Number: ${doc.documentNumber}\nExpiry Date: $expDate"
                }
                return@withContext AiResponse.Text(msg)
            }
        }

        // 8. General AI Query: Invoke Gemini API if key is present
        val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (geminiKey.isNotBlank() && !geminiKey.contains("MY_GEMINI_API_KEY")) {
            try {
                val contextPrompt = """
                    You are LifeMate AI, a smart personal life organizer assistant for students, families, and professionals.
                    Current date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}
                    User data summary:
                    - Active Reminders (${reminders.size}): ${reminders.take(5).joinToString { it.title }}
                    - Total Expenses: ৳${expenses.sumOf { it.amount }}
                    - Dharkhata to receive: ৳${dharkhata.filter { it.type == "RECEIVE" && !it.isSettled }.sumOf { it.amount }}
                    - Commitments: ${commitments.filter { !it.isFulfilled }.joinToString { it.title }}
                    - Tracked Items: ${lostItems.joinToString { "${it.name} at ${it.lastKnownLocation}" }}
                    
                    Answer concisely, warmly, and helpfully in ${if (lang == "bn") "Bengali" else "English"}.
                    User question: $query
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", contextPrompt)))
                    }))
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$geminiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val root = JSONObject(respBody)
                    val reply = root.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext AiResponse.Text(reply.trim())
                }
            } catch (e: Exception) {
                // fallback to local intelligence below
            }
        }

        // Helpful local conversational fallback
        val defaultMsg = if (lang == "bn") {
            "🤖 লাইফমেট প্রস্তুত! আপনি বলতে পারেন:\n" +
            "• 'কাল সকাল ৯টায় রহিমকে ফোন দাও'\n" +
            "• 'চা ২০' অথবা 'রিকশা ৮০'\n" +
            "• 'এই মাসে কত খরচ করেছি?'\n" +
            "• 'কে কত টাকা পাবে?'\n" +
            "• 'আজকে আমার কি কাজ আছে?'\n" +
            "• 'পাসপোর্ট কোথায় রেখেছি?'"
        } else {
            "🤖 LifeMate Assistant is ready! You can try asking:\n" +
            "• 'Remind me tomorrow at 9 AM to call Rahim'\n" +
            "• 'Tea 20' or 'Rickshaw 80'\n" +
            "• 'How much did I spend this month?'\n" +
            "• 'Who owes me money?'\n" +
            "• 'What do I have to do today?'\n" +
            "• 'Where did I keep my passport?'"
        }
        AiResponse.Text(defaultMsg)
    }
}
