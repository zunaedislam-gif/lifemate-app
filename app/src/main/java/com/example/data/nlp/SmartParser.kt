package com.example.data.nlp

import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedReminder(
    val title: String,
    val targetTime: Long,
    val recurrence: String, // NONE, DAILY, WEEKLY, MONTHLY
    val category: String
)

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val category: String
)

data class ParsedDharkhata(
    val personName: String,
    val amount: Double,
    val type: String // RECEIVE, PAY
)

data class ParsedCommitment(
    val title: String,
    val person: String,
    val deadline: Long,
    val category: String
)

object SmartParser {

    /**
     * Parse natural language reminder:
     * Examples:
     * "Tomorrow at 9 AM call Rahim"
     * "Remind me every month to pay internet bill"
     * "Friday 5 PM go to doctor"
     * "Tonight 8 PM call mother"
     */
    fun parseReminder(rawInput: String): ParsedReminder {
        val input = rawInput.trim()
        val lower = input.lowercase(Locale.ROOT)

        var recurrence = "NONE"
        if (lower.contains("every month") || lower.contains("monthly")) recurrence = "MONTHLY"
        else if (lower.contains("every week") || lower.contains("weekly")) recurrence = "WEEKLY"
        else if (lower.contains("every day") || lower.contains("daily")) recurrence = "DAILY"

        // Category determination
        val category = when {
            lower.contains("call") || lower.contains("phone") -> "Call"
            lower.contains("bill") || lower.contains("pay") || lower.contains("rent") -> "Bill"
            lower.contains("doctor") || lower.contains("medicine") || lower.contains("hospital") -> "Health"
            lower.contains("study") || lower.contains("exam") || lower.contains("class") || lower.contains("assignment") -> "Study"
            lower.contains("meeting") || lower.contains("client") || lower.contains("design") || lower.contains("office") -> "Work"
            else -> "General"
        }

        // Target time calculation
        val cal = Calendar.getInstance()
        var hour = 9
        var minute = 0
        var isPm = false

        // Check for time: e.g. "9 AM", "5 PM", "8:30 PM", "9am", "5pm", "17:00", "at 9"
        val timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val timeMatcher = timePattern.matcher(lower)
        var foundTime = false
        while (timeMatcher.find()) {
            val matchedStr = timeMatcher.group()
            val h = timeMatcher.group(1)?.toIntOrNull()
            val m = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val ampm = timeMatcher.group(3)?.lowercase(Locale.ROOT)

            if (h != null && h in 1..24) {
                hour = h
                minute = m
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
                if (ampm != null || lower.contains("at $h") || lower.contains("at $matchedStr")) {
                    foundTime = true
                    break
                }
            }
        }

        if (!foundTime) {
            if (lower.contains("morning")) { hour = 9; minute = 0 }
            else if (lower.contains("noon") || lower.contains("lunch")) { hour = 13; minute = 0 }
            else if (lower.contains("afternoon")) { hour = 16; minute = 0 }
            else if (lower.contains("evening") || lower.contains("night") || lower.contains("tonight")) { hour = 20; minute = 0 }
        }

        // Date determination
        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        } else if (lower.contains("tonight") || lower.contains("today")) {
            // keep today
        } else {
            // Check day names
            val days = mapOf(
                "sunday" to Calendar.SUNDAY,
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY
            )
            for ((dayName, dayConst) in days) {
                if (lower.contains(dayName)) {
                    val currentDay = cal.get(Calendar.DAY_OF_WEEK)
                    var daysToAdd = (dayConst - currentDay + 7) % 7
                    if (daysToAdd == 0) daysToAdd = 7
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    break
                }
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // If calculated time is in past, add 1 day
        if (cal.timeInMillis < System.currentTimeMillis() && !lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Clean title
        var cleanTitle = input
            .replace(Regex("(?i)remind\\s+me\\s+(to\\s+)?"), "")
            .replace(Regex("(?i)every\\s+(month|week|day)"), "")
            .replace(Regex("(?i)\\b(tomorrow|today|tonight|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b"), "")
            .replace(Regex("(?i)\\bat\\s+\\d{1,2}(:\\d{2})?\\s*(am|pm)?\\b"), "")
            .replace(Regex("(?i)\\b\\d{1,2}(:\\d{2})?\\s*(am|pm)\\b"), "")
            .trim()

        if (cleanTitle.startsWith("to ", ignoreCase = true)) {
            cleanTitle = cleanTitle.substring(3).trim()
        }
        if (cleanTitle.isBlank()) cleanTitle = input

        return ParsedReminder(
            title = cleanTitle.replaceFirstChar { it.uppercase() },
            targetTime = cal.timeInMillis,
            recurrence = recurrence,
            category = category
        )
    }

    /**
     * Parse natural language expense:
     * Examples:
     * "Tea 20"
     * "Rickshaw 80"
     * "Lunch 150"
     * "Bazar 550"
     */
    fun parseExpense(rawInput: String): ParsedExpense? {
        val input = rawInput.trim()
        val numPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = numPattern.matcher(input)
        var amount: Double? = null
        var numberStr = ""

        // Find last matching number as amount (e.g. "Lunch 150" -> 150)
        while (matcher.find()) {
            numberStr = matcher.group(1) ?: ""
            amount = numberStr.toDoubleOrNull()
        }

        if (amount == null) return null

        var title = input.replace(numberStr, "")
            .replace(Regex("(?i)(tk|taka|bdt|\\$|spent|cost|for)"), "")
            .trim()
        if (title.isBlank()) title = "Expense"

        val lower = title.lowercase(Locale.ROOT)
        val category = categorizeExpense(lower)

        return ParsedExpense(
            title = title.replaceFirstChar { it.uppercase() },
            amount = amount,
            category = category
        )
    }

    fun categorizeExpense(text: String): String {
        return when {
            text.contains("tea") || text.contains("cha") || text.contains("coffee") ||
            text.contains("lunch") || text.contains("dinner") || text.contains("breakfast") ||
            text.contains("burger") || text.contains("pizza") || text.contains("snack") ||
            text.contains("food") || text.contains("restaurant") || text.contains("cafe") ||
            text.contains("biryani") || text.contains("juice") -> "Food & Drinks"

            text.contains("rickshaw") || text.contains("uber") || text.contains("bus") ||
            text.contains("cng") || text.contains("metro") || text.contains("taxi") ||
            text.contains("train") || text.contains("petrol") || text.contains("fuel") ||
            text.contains("fare") || text.contains("transport") -> "Transportation"

            text.contains("bazar") || text.contains("bazaar") || text.contains("grocery") ||
            text.contains("vegetable") || text.contains("egg") || text.contains("milk") ||
            text.contains("rice") || text.contains("oil") || text.contains("fish") ||
            text.contains("meat") || text.contains("chicken") || text.contains("fruits") -> "Groceries"

            text.contains("internet") || text.contains("wifi") || text.contains("electricity") ||
            text.contains("current") || text.contains("bill") || text.contains("gas") ||
            text.contains("water") || text.contains("recharge") || text.contains("mobile") -> "Bills & Utilities"

            text.contains("shopping") || text.contains("shirt") || text.contains("pant") ||
            text.contains("dress") || text.contains("shoe") || text.contains("cloth") ||
            text.contains("watch") || text.contains("bag") -> "Shopping"

            text.contains("medicine") || text.contains("doctor") || text.contains("pharmacy") ||
            text.contains("hospital") || text.contains("clinic") || text.contains("test") -> "Health"

            text.contains("tuition") || text.contains("book") || text.contains("exam") ||
            text.contains("fee") || text.contains("course") || text.contains("college") ||
            text.contains("school") || text.contains("pen") || text.contains("khata") -> "Education"

            else -> "Other"
        }
    }

    fun parseDharkhata(rawInput: String): ParsedDharkhata? {
        val input = rawInput.trim()
        val numPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = numPattern.matcher(input)
        var amount: Double? = null
        var numStr = ""
        while (matcher.find()) {
            numStr = matcher.group(1) ?: ""
            amount = numStr.toDoubleOrNull()
        }
        if (amount == null) return null

        val lower = input.lowercase(Locale.ROOT)
        val isLent = lower.contains("gave") || lower.contains("lent") || lower.contains("to") || lower.contains("diasilam")
        val type = if (isLent) "RECEIVE" else "PAY"

        var name = input.replace(numStr, "")
            .replace(Regex("(?i)(gave|lent|borrowed|took|from|to|tk|taka|\\$)"), "")
            .trim()
        if (name.isBlank()) name = "Friend"

        return ParsedDharkhata(
            personName = name.replaceFirstChar { it.uppercase() },
            amount = amount,
            type = type
        )
    }

    fun parseCommitment(rawInput: String): ParsedCommitment {
        val input = rawInput.trim()
        val lower = input.lowercase(Locale.ROOT)

        val cal = Calendar.getInstance()
        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        } else if (lower.contains("friday")) {
            cal.add(Calendar.DAY_OF_YEAR, 2)
        } else if (lower.contains("tonight")) {
            cal.set(Calendar.HOUR_OF_DAY, 21)
        } else {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val category = when {
            lower.contains("client") || lower.contains("design") || lower.contains("project") || lower.contains("work") -> "Work"
            lower.contains("money") || lower.contains("pay") || lower.contains("return") -> "Finance"
            lower.contains("mother") || lower.contains("father") || lower.contains("brother") || lower.contains("sister") || lower.contains("family") -> "Family"
            else -> "Personal"
        }

        var person = ""
        val personRegex = Regex("(?i)(to|with|for)\\s+([A-Z][a-z]+)")
        val match = personRegex.find(input)
        if (match != null) {
            person = match.groupValues[2]
        }

        return ParsedCommitment(
            title = input.replaceFirstChar { it.uppercase() },
            person = person,
            deadline = cal.timeInMillis,
            category = category
        )
    }
}
