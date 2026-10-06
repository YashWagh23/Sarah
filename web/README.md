# Sarah Web PWA 🎓⚡

The modern Progressive Web App (PWA) client for **Sarah: Personal Academic Operating System**.

Built with **React 19**, **TypeScript**, **Vite**, and **IndexedDB**. Designed to work 100% offline with zero external cloud dependencies.

---

## ⚡ Key Highlights

- **Tonight Planner** (`src/lib/planner.ts`): a pure function of *(tasks, profile, now)* that ranks open work and schedules focus sessions, breaks, classes, commute and dinner before bedtime. The Today screen renders its verdict, next move, timeline and "won't fit" list.
- **Instant Optimistic UI**: Checkbox toggles, note pins, energy switches, and snooze actions update instantaneously with async background persistence.
- **Offline-First PWA**: Workbox service worker precaches application assets for offline access; data persists in browser IndexedDB via `idb`.
- **Responsive Obsidian Aesthetics**: Tailored dark-mode theme, glassmorphic navigation, mobile safe-area insets, and iOS touch-delay elimination.
- **Data Portability**: Full JSON export and restore built into the Profile screen.

---

## 🛠️ Scripts

| Command | Description |
| :--- | :--- |
| `npm run dev` | Launch local Vite development server at `http://localhost:5173` |
| `npm run build` | Typecheck with `tsc` and generate production PWA bundle in `dist/` |
| `npm run preview` | Locally preview the production build |
| `npm run test` | Run the persistence suite and the Tonight Planner suite via `tsx` |

---

## 🧪 Testing

`test-persistence.ts` verifies:
1. Fresh install clean-state (0 demo items, blank profile).
2. Optimistic user mutations (tasks, notes, reminders, profile), including completion timestamps.
3. Cascade safety (safe fallback when enrolled subjects are removed).
4. Full database backup & restore roundtrips.

`test-planner.ts` pins the clock and verifies the planner: tonight's deadlines outrank far-off must-dos, dinner/college/commute are never double-booked, weekends free up the afternoon, bedtimes after midnight work, overloaded nights report a shortfall, rest mode defers non-urgent work, and long tasks split into focus blocks.

## 🧭 How the planner decides

1. **Free time** = now → bedtime, minus classes + commute (on class days) and a 30-minute dinner at 8 PM.
2. **Realistic focus** = free time × energy share (High 90%, Steady 80%, Low 60%, Rest 35%).
3. **Ranking**: tasks due tonight or before noon tomorrow come first, then by score — deadline urgency (overdue › today › tomorrow › ≤3 days › this week) + priority (Must › Should › Later) + a small quick-win bonus.
4. **Scheduling**: sessions are placed in rank order in blocks of 60/45/30/25 minutes (by energy) with breaks between; slivers under 20 minutes are skipped rather than starting a big task right before dinner.
5. **Verdict**: anything tonight-critical left unscheduled is listed under *Won't fit before bedtime*.

Run tests:
```bash
npm run test
```
