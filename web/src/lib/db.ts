import { openDB, type DBSchema, type IDBPDatabase } from 'idb';

export type TaskPriority = 'must' | 'should' | 'later';

export interface Task {
  id: string;
  title: string;
  description?: string;
  subject: string;
  deadline: string; // ISO format 'YYYY-MM-DD'
  deadlineTime?: string; // 'HH:mm' optional
  priority: TaskPriority;
  estimatedMinutes: number;
  completed: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface AcademicNote {
  id: string;
  title: string;
  content: string;
  subject: string;
  pinned: boolean;
  createdAt: number;
  updatedAt: number;
}

export const SUBJECT_COLORS: Record<string, string> = {
  'Machine Learning': '#5E6AD2',
  'Operating Systems': '#10B981',
  'Algorithms': '#F59E0B',
  'Computer Networks': '#EC4899',
  'Database Systems': '#8B5CF6',
  'General': '#6366F1'
};

export const DEFAULT_SUBJECTS = [
  'Machine Learning',
  'Operating Systems',
  'Algorithms',
  'Computer Networks',
  'Database Systems',
  'General'
];

interface SarahPwaDB extends DBSchema {
  key_val: {
    key: string;
    value: string | number | boolean | object;
  };
  tasks: {
    key: string;
    value: Task;
    indexes: {
      'by-completed': number;
      'by-deadline': string;
      'by-priority': string;
    };
  };
  notes: {
    key: string;
    value: AcademicNote;
    indexes: {
      'by-subject': string;
      'by-pinned': number;
      'by-createdAt': number;
    };
  };
}

const DB_NAME = 'sarah_pwa_db';
const DB_VERSION = 3;

let dbPromise: Promise<IDBPDatabase<SarahPwaDB>> | null = null;

export function getDB(): Promise<IDBPDatabase<SarahPwaDB>> {
  if (!dbPromise) {
    dbPromise = openDB<SarahPwaDB>(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('key_val')) {
          db.createObjectStore('key_val');
        }
        if (!db.objectStoreNames.contains('tasks')) {
          const taskStore = db.createObjectStore('tasks', { keyPath: 'id' });
          taskStore.createIndex('by-completed', 'completed');
          taskStore.createIndex('by-deadline', 'deadline');
          taskStore.createIndex('by-priority', 'priority');
        }
        if (!db.objectStoreNames.contains('notes')) {
          const noteStore = db.createObjectStore('notes', { keyPath: 'id' });
          noteStore.createIndex('by-subject', 'subject');
          noteStore.createIndex('by-pinned', 'pinned');
          noteStore.createIndex('by-createdAt', 'createdAt');
        }
      },
    });
  }
  return dbPromise;
}

// ─── Default Initial Tasks & Notes ───────────────────────────────────────────

function getTodayDateStr(): string {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getTomorrowDateStr(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const INITIAL_SEED_TASKS: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>[] = [
  {
    title: 'Complete ML Assignment 3',
    description: 'Solve neural network backpropagation equations and train model.',
    subject: 'Machine Learning',
    deadline: getTodayDateStr(),
    deadlineTime: '23:59',
    priority: 'must',
    estimatedMinutes: 45,
    completed: false
  },
  {
    title: 'Review OS Synchronization Notes',
    description: 'Semaphores, mutex locks, and Dining Philosophers problem solution.',
    subject: 'Operating Systems',
    deadline: getTodayDateStr(),
    deadlineTime: '21:00',
    priority: 'must',
    estimatedMinutes: 30,
    completed: false
  },
  {
    title: 'Read Graph Algorithms Chapter 4',
    description: 'Dijkstra and Bellman-Ford shortest path proofs.',
    subject: 'Algorithms',
    deadline: getTomorrowDateStr(),
    deadlineTime: '18:00',
    priority: 'should',
    estimatedMinutes: 40,
    completed: false
  },
  {
    title: 'Configure TCP Socket Client Lab',
    description: 'Implement multi-threaded client connection handler.',
    subject: 'Computer Networks',
    deadline: getTomorrowDateStr(),
    deadlineTime: '20:00',
    priority: 'later',
    estimatedMinutes: 60,
    completed: false
  }
];

const INITIAL_SEED_NOTES: Omit<AcademicNote, 'id' | 'createdAt' | 'updatedAt'>[] = [
  {
    title: 'ML Lecture 8 — Backpropagation & Gradient Descent',
    content: 'Key equations: chain rule applied to multi-layer perceptron cost function dE/dW = dE/dOut * dOut/dNet * dNet/dW. Don\'t forget to normalize input feature vectors.',
    subject: 'Machine Learning',
    pinned: true
  },
  {
    title: 'OS Process Synchronization & Semaphores',
    content: 'Dining philosophers problem: prevent deadlock by requiring odd philosophers to pick left fork first and even philosophers right fork first. Compare mutex vs binary semaphore.',
    subject: 'Operating Systems',
    pinned: true
  },
  {
    title: 'Algorithms — Dijkstra vs Bellman-Ford',
    content: 'Dijkstra: greedy, O((V+E)logV) with Fibonacci heap, cannot handle negative weight edges. Bellman-Ford: dynamic programming, O(V*E), detects negative weight cycles.',
    subject: 'Algorithms',
    pinned: false
  },
  {
    title: 'DBMS ACID Properties & Normalization Rules',
    content: '1NF (atomic attributes), 2NF (no partial dependency), 3NF (no transitive dependency), BCNF (every determinant is a candidate key). Isolation levels: Read Uncommitted, Read Committed, Repeatable Read, Serializable.',
    subject: 'Database Systems',
    pinned: false
  }
];

// ─── Task CRUD Operations ───────────────────────────────────────────────────

export async function getTasks(): Promise<Task[]> {
  const db = await getDB();
  let allTasks = await db.getAll('tasks');

  // Seed default tasks if store is brand new
  if (allTasks.length === 0) {
    const isSeeded = await db.get('key_val', 'sarah_has_seeded_initial_tasks');
    if (!isSeeded) {
      const seededTasks: Task[] = [];
      const now = Date.now();
      for (let i = 0; i < INITIAL_SEED_TASKS.length; i++) {
        const item = INITIAL_SEED_TASKS[i];
        const task: Task = {
          ...item,
          id: `task_${now}_${i + 1}`,
          createdAt: now - (INITIAL_SEED_TASKS.length - i) * 60000,
          updatedAt: now - (INITIAL_SEED_TASKS.length - i) * 60000
        };
        await db.put('tasks', task);
        seededTasks.push(task);
      }
      await db.put('key_val', true, 'sarah_has_seeded_initial_tasks');
      allTasks = seededTasks;
    }
  }

  // Sort by createdAt descending
  return allTasks.sort((a, b) => b.createdAt - a.createdAt);
}

export async function getTask(id: string): Promise<Task | undefined> {
  const db = await getDB();
  return db.get('tasks', id);
}

export async function addTask(taskData: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>): Promise<Task> {
  const db = await getDB();
  const now = Date.now();
  const newTask: Task = {
    ...taskData,
    id: `task_${now}_${Math.random().toString(36).substring(2, 7)}`,
    createdAt: now,
    updatedAt: now
  };
  await db.put('tasks', newTask);
  return newTask;
}

export async function updateTask(task: Task): Promise<Task> {
  const db = await getDB();
  const updated: Task = {
    ...task,
    updatedAt: Date.now()
  };
  await db.put('tasks', updated);
  return updated;
}

export async function deleteTask(id: string): Promise<void> {
  const db = await getDB();
  await db.delete('tasks', id);
}

export async function completeTask(id: string, completed: boolean): Promise<Task | undefined> {
  const db = await getDB();
  const existing = await db.get('tasks', id);
  if (!existing) return undefined;
  const updated: Task = {
    ...existing,
    completed,
    updatedAt: Date.now()
  };
  await db.put('tasks', updated);
  return updated;
}

// ─── Academic Notes CRUD Operations ─────────────────────────────────────────

export async function getNotes(): Promise<AcademicNote[]> {
  const db = await getDB();
  let allNotes = await db.getAll('notes');

  // Seed default notes if store is brand new
  if (allNotes.length === 0) {
    const isSeeded = await db.get('key_val', 'sarah_has_seeded_initial_notes');
    if (!isSeeded) {
      const seededNotes: AcademicNote[] = [];
      const now = Date.now();
      for (let i = 0; i < INITIAL_SEED_NOTES.length; i++) {
        const item = INITIAL_SEED_NOTES[i];
        const note: AcademicNote = {
          ...item,
          id: `note_${now}_${i + 1}`,
          createdAt: now - (INITIAL_SEED_NOTES.length - i) * 60000,
          updatedAt: now - (INITIAL_SEED_NOTES.length - i) * 60000
        };
        await db.put('notes', note);
        seededNotes.push(note);
      }
      await db.put('key_val', true, 'sarah_has_seeded_initial_notes');
      allNotes = seededNotes;
    }
  }

  // Sort: Pinned first, then newest createdAt
  return allNotes.sort((a, b) => {
    if (a.pinned !== b.pinned) {
      return a.pinned ? -1 : 1;
    }
    return b.createdAt - a.createdAt;
  });
}

export async function getNote(id: string): Promise<AcademicNote | undefined> {
  const db = await getDB();
  return db.get('notes', id);
}

export async function addNote(noteData: Omit<AcademicNote, 'id' | 'createdAt' | 'updatedAt'>): Promise<AcademicNote> {
  const db = await getDB();
  const now = Date.now();
  const newNote: AcademicNote = {
    ...noteData,
    id: `note_${now}_${Math.random().toString(36).substring(2, 7)}`,
    createdAt: now,
    updatedAt: now
  };
  await db.put('notes', newNote);
  return newNote;
}

export async function updateNote(note: AcademicNote): Promise<AcademicNote> {
  const db = await getDB();
  const updated: AcademicNote = {
    ...note,
    updatedAt: Date.now()
  };
  await db.put('notes', updated);
  return updated;
}

export async function deleteNote(id: string): Promise<void> {
  const db = await getDB();
  await db.delete('notes', id);
}

export async function toggleNotePinned(id: string, pinned: boolean): Promise<AcademicNote | undefined> {
  const db = await getDB();
  const existing = await db.get('notes', id);
  if (!existing) return undefined;
  const updated: AcademicNote = {
    ...existing,
    pinned,
    updatedAt: Date.now()
  };
  await db.put('notes', updated);
  return updated;
}

// ─── Diagnostics & Session Persistence ──────────────────────────────────────

const TEST_KEY = 'sarah_persistence_test_val';
const RELOAD_COUNT_KEY = 'sarah_session_reload_count';
const LAST_SAVED_KEY = 'sarah_last_saved_timestamp';

export interface PersistenceStatus {
  testValue: string;
  reloadCount: number;
  lastSavedAt: string;
  isReady: boolean;
}

export async function initializeAndTrackPersistence(): Promise<PersistenceStatus> {
  const db = await getDB();
  
  const existingCount = (await db.get('key_val', RELOAD_COUNT_KEY)) as number | undefined;
  const newCount = (existingCount ?? 0) + 1;
  await db.put('key_val', newCount, RELOAD_COUNT_KEY);

  let testVal = (await db.get('key_val', TEST_KEY)) as string | undefined;
  if (!testVal) {
    testVal = 'Sarah PWA Local Storage Active';
    await db.put('key_val', testVal, TEST_KEY);
  }

  const nowStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  await db.put('key_val', nowStr, LAST_SAVED_KEY);

  return {
    testValue: testVal,
    reloadCount: newCount,
    lastSavedAt: nowStr,
    isReady: true
  };
}

export async function updateTestValue(newVal: string): Promise<void> {
  const db = await getDB();
  await db.put('key_val', newVal, TEST_KEY);
  const nowStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  await db.put('key_val', nowStr, LAST_SAVED_KEY);
}

export async function getTestValue(): Promise<string> {
  const db = await getDB();
  const val = await db.get('key_val', TEST_KEY);
  return (val as string) || '';
}
