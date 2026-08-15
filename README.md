# Sarah 🎓⚡

**Personal Academic Operating System for College Students**

Sarah is an intelligent, offline-first Android application built with Kotlin, Jetpack Compose, and Material 3. It answers the central academic question:
> *"Given everything I have going on and the time/energy I actually have, what should I do next?"*

---

## ✨ Features (Phase 1: Foundation)

- **🧠 Deterministic Feasibility Engine**: Real-time evaluation of study capacity against pending deadlines and planned bedtime.
- **⚡ 4-State Dynamic Energy Model**: Instant recalibration of study pace (`High`, `Normal`, `Low`, `Exhausted`).
- **🎯 Smart Task Triage**: Distinguishes between `MUST DO TONIGHT`, `SHOULD DO TONIGHT`, and `CAN DEFER`.
- **📅 Adaptive Evening Agenda**: Chronological focus blocks with restorative break scheduling.
- **📚 Curriculum & Attendance Tracking**: Enrolled subjects with faculty notes, weekly hours, and attendance targets.
- **🔒 Offline-First & Private**: Local Room database ensures all academic data remains strictly on your device.
- **🎨 Modern Dark Design**: Ambient obsidian UI with high-contrast status badges.

---

## 🛠️ Tech Stack

- **Platform**: Android (Min SDK 26, Target SDK 34)
- **Language**: Kotlin 1.9.24
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: Clean Architecture (Domain / Data / UI) + MVVM + Unidirectional Data Flow
- **Persistence**: Room Database (SQLite) + Reactive Coroutines / Flow + SharedPreferences
- **Build System**: Gradle 8.7 + Android Gradle Plugin 8.4.2

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/YashWagh23/Sarah.git
   ```
2. Open in **Android Studio** (Koala / Ladybug or newer with JDK 17).
3. Build & Run on an Android device or emulator (API 26+).
