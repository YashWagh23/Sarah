# Sarah Web PWA 🎓⚡

The modern Progressive Web App (PWA) client for **Sarah: Personal Academic Operating System**.

Built with **React 19**, **TypeScript**, **Vite**, and **IndexedDB**. Designed to work 100% offline with zero external cloud dependencies.

---

## ⚡ Key Highlights

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
| `npm run test` | Run automated persistence and clean state test suite via `tsx` |

---

## 🧪 Testing

The persistence test suite verifies:
1. Fresh install clean-state (0 demo items).
2. Optimistic user mutations (tasks, notes, reminders, profile).
3. Cascade safety (safe fallback when enrolled subjects are removed).
4. Full database backup & restore roundtrips.

Run tests:
```bash
npm run test
```
