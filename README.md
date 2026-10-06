# Sarah 🎓⚡

**Personal Academic Operating System for College Students**

Sarah is an intelligent, offline-first personal academic operating system designed for college students. Available as a **Progressive Web App (PWA)** and a **Native Android Application**, Sarah answers the central question students face every day:

> *"Given everything I have going on and the time/energy I actually have, what should I do next?"*

---

## 🌟 Key Pillars

- 🔒 **100% Offline-First & Private**: No mandatory cloud accounts or external tracking. Your tasks, notes, courses, and schedules are stored locally (IndexedDB on Web, Room SQLite on Android) and never leave your device.
- ⚡ **Zero-Lag Optimistic UI**: Checkbox completions, note pins, energy level switches, and reminder actions update instantly on touch with asynchronous background persistence.
- 🧠 **Tonight Planner**: Scores every open task by deadline urgency, priority and size, then lays real focus sessions and breaks on the clock between now and bedtime — around classes, commute and dinner — and flags deadlines that won't fit.
- 🔋 **4-State Dynamic Energy Model**: `High`, `Steady`, `Low` and `Rest` change focus-block length, break length and how much of your free time is realistically plannable; `Rest` keeps only work that can't wait.
- 🎨 **Apple-Inspired Dark Aesthetic**: Premium obsidian-style UI with fluid spring transitions, ambient cards, and high-contrast status chips.
- 💾 **Universal Data Backup & Portability**: One-click JSON backup export and restore across devices without cloud vendor lock-in.

---

## ✨ Features

### 🎯 Today: Your Evening, Planned
- **Tonight verdict** — *On track*, *Tight*, *Overloaded*, *Rest mode* or *Past bedtime*, with planned focus vs. realistic capacity and free time before bed.
- **Do this next** — the single highest-value task, with why (e.g. "Overdue by 2 days", "Due tomorrow at 9:00 AM") and when it's scheduled.
- **Tonight's plan** — a timeline of sessions, breaks, classes, commute, dinner and bedtime. Long tasks split into energy-sized focus blocks ("part 1/3").
- **Won't fit before bedtime** — deadlines that can't be finished tonight, including partial fits ("only 45m of 1h fits"), so you can trim scope or ask for an extension early.
- **Can wait** — everything else, ordered by urgency. The plan recomputes instantly as you finish tasks, change energy, or as time passes.
- Overdue tasks are flagged everywhere; the Tasks list is ordered by deadline.
- Link tasks directly to enrolled courses or custom-typed subjects.

### 📚 Course & Curriculum Management
- Organize courses with course codes, faculty information, and credit/weekly schedules.
- Add tasks, notes, and reminders directly from inside course detail views.
- Clean cascade handling: deleting a course safely preserves all linked tasks, notes, and reminders by moving them to a fallback subject.

### 📝 Academic Lecture Notes
- Fast note creation with markdown and code formatting support.
- Pin critical notes and formula cheat-sheets to the top.
- Full text-selection support for copying mathematical formulas, code snippets, and lecture notes.

### ⏰ Smart Deadline Reminders & Alerts
- Browser and device notification support for assignment deadlines and exam dates.
- Interactive snooze (`+15m`, `+1h`, `Tomorrow`) and dismiss actions.
- Dropdown menus with elevated stacking contexts for seamless mobile interaction without layout clipping.

### 👤 Profile & Schedule Customization
- A first-run welcome sheet asks for your name, when classes end, commute, bedtime and which days you have classes (weekends are free by default).
- Configure your target bedtime (after-midnight bedtimes supported), wake-up time, class days, and daily study goal.
- Toggle energy states anytime from the navigation header or profile screen.
- Manage browser push notification permissions directly from the app.

---

## 🛠️ Architecture & Tech Stack

### Web Progressive Web App (`web/`)
- **Framework**: React 19 + TypeScript
- **Bundler & Tooling**: Vite + Vite PWA Plugin (Workbox Service Worker)
- **Styling**: Vanilla CSS with modern CSS custom properties, glassmorphism, responsive safe-area insets, and touch latency elimination (`touch-action: manipulation`)
- **Local Storage**: IndexedDB via `idb`
- **Icons**: Lucide React
- **Testing**: Automated persistence and clean-state verification test suite with `tsx` & `fake-indexeddb`

### Android Application (`app/` & `shared/`)
- **Platform**: Android (Min SDK 26, Target SDK 34)
- **Language**: Kotlin 1.9.24 / Kotlin Multiplatform (KMP)
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: Clean Architecture (Domain / Data / UI) + MVVM + Unidirectional Data Flow
- **Persistence**: Room Database (SQLite) + Reactive Coroutines / Flow + SharedPreferences

---

## 🚀 Getting Started

### Web PWA

1. Navigate to the `web` directory:
   ```bash
   cd web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the local development server:
   ```bash
   npm run dev
   ```
   Open `http://localhost:5173` in your browser.

4. Run the automated test suites (persistence + Tonight Planner):
   ```bash
   npm run test
   ```

5. Build for production:
   ```bash
   npm run build
   ```
   The compiled PWA and service worker precache bundle will be output to `web/dist/`.

---

### Android Native App

1. Open the repository root in **Android Studio** (Koala / Ladybug or newer with JDK 17).
2. Allow Gradle to sync dependencies.
3. Select the `app` run configuration.
4. Run on a physical Android device or emulator running Android 8.0 (API 26) or higher.

---

## 🧪 Testing & Data Integrity

Sarah is built with strict zero-loss data persistence principles. The web test suites guarantee:
- **Clean State**: Fresh installations start with 0 demo data records.
- **Relational Integrity**: Deleting a subject safely migrates tasks and notes without dropping records.
- **Optimistic State Consistency**: React state transitions match local database states identically.
- **Backup & Restore**: Full JSON roundtrip export and import without field degradation.
- **Planner correctness**: deadline ranking, college vs. weekend days, dinner and commute blocks, past-midnight bedtimes, overloaded nights, rest mode, and session splitting (`web/test-planner.ts`).

Run tests anytime with:
```bash
cd web && npm run test
```

---

## 📄 License

This project is licensed under the MIT License.
