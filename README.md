LifeMate — An all-in-one personal life companion for Android. Features natural language reminders, expense tracking & dharkhata, document vault, lost item locator, emergency medical QR, student routines, and an offline-first Gemini AI assistant. Built with Jetpack Compose & Room.
📄 Markdown Description for your README.md
code
Markdown
# 🌟 Remix LifeMate

> **Your all-in-one daily life companion, financial ledger, personal safety vault, and intelligent assistant — designed for modern life.**

**Remix LifeMate** is a modern, privacy-conscious Android application built with **Jetpack Compose**, **Room Database**, and **Gemini AI**. It consolidates your daily organizational needs into a single unified dashboard, eliminating the need for half a dozen disconnected productivity, finance, and utility apps.

---

## ✨ Key Features

### 🧠 Smart Natural-Language Tasks & Reminders
- **Instant Input**: Type naturally (e.g., *"Pay electricity bill next Tuesday at 5 PM"* or *"Remind me to buy medicine tomorrow morning"*) and let the built-in parser extract dates, times, and priorities automatically.
- **Commitments & Habit Tracking**: Keep track of promises, personal deadlines, and recurring daily routines with zero friction.

### 💰 Money Manager & Dharkhata (Borrow / Lend Ledger)
- **Expense & Income Tracking**: Categorize spending, track monthly budgets, and analyze spending patterns.
- **Dharkhata (Borrow & Lend)**: Maintain a clear record of money lent to or borrowed from friends, family, and colleagues, complete with due dates and settlement histories.

### 🗄️ Document Vault & Expiry Alerts
- Store important documents, serial numbers, credentials, and IDs securely on-device.
- Automatic notifications before passports, licenses, warranties, or cards expire.

### 🚨 Personal Safety & Emergency QR Profile
- **Emergency Medical QR**: Instant scan-ready QR code displaying critical medical information, blood group, and emergency contact details for first responders.
- **Safe Arrival Check-in**: Set timed destination check-ins with quick alerts if you don't check in on time.

### 🔍 Lost Item Locator & Tagging
- Catalog valuable belongings, luggage, keys, and devices with photo records and unique identification codes to facilitate recovery if misplaced.

### 🎓 Student Routine & Study Tools
- Class timetables, assignment countdowns, study schedules, and exam routine management tailored for students and lifelong learners.

### 🤖 Intelligent AI Assistant (Gemini)
- Ask for daily planning advice, draft reminders, summarize expenses, or get actionable study schedules powered by Google Gemini.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) (Offline-first, 100% private on-device data)
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **AI Integration**: Google Gemini API (Firebase AI / REST integration)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
- **Utilities**: On-device QR generator, Moshi JSON serialization, Coil image loading

---

## 🔒 Privacy First

All personal logs, ledger entries, vault documents, and schedules are stored **locally on your device** via an encrypted SQLite Room database. Your personal life data stays strictly yours.
