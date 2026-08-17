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

export interface Reminder {
  id: string;
  title: string;
  message?: string;
  reminderAt: number; // Unix timestamp epoch in milliseconds
  taskId?: string; // Optional linked task ID
  subject?: string; // Optional linked subject
  completed: boolean;
  dismissed: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface Subject {
  id: string;
  name: string;
  code?: string;
  color: string;
  createdAt: number;
  updatedAt: number;
}

export const SUBJECT_PALETTE = [
  '#5E6AD2', // Primary Indigo
  '#10B981', // Emerald Green
  '#F59E0B', // Amber
  '#EC4899', // Rose Pink
  '#8B5CF6', // Purple
  '#3B82F6', // Sky Blue
  '#06B6D4', // Cyan
  '#64748B', // Slate
  '#6366F1'  // General Violet
];

export const SUBJECT_COLORS: Record<string, string> = {
  'Machine Learning': '#5E6AD2',
  'Operating Systems': '#10B981',
  'Algorithms': '#F59E0B',
  'Computer Networks': '#EC4899',
  'Database Systems': '#8B5CF6',
  'Database Management Systems': '#8B5CF6',
  'General': '#6366F1'
};

export const DEFAULT_SUBJECTS = [
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
  reminders: {
    key: string;
    value: Reminder;
    indexes: {
      'by-reminderAt': number;
      'by-dismissed': number;
      'by-completed': number;
      'by-taskId': string;
      'by-createdAt': number;
    };
  };
  subjects: {
    key: string;
    value: Subject;
    indexes: {
      'by-name': string;
      'by-createdAt': number;
    };
  };
}

const DB_NAME = 'sarah_pwa_db';
const DB_VERSION = 5;

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
        if (!db.objectStoreNames.contains('reminders')) {
          const reminderStore = db.createObjectStore('reminders', { keyPath: 'id' });
          reminderStore.createIndex('by-reminderAt', 'reminderAt');
          reminderStore.createIndex('by-dismissed', 'dismissed');
          reminderStore.createIndex('by-completed', 'completed');
          reminderStore.createIndex('by-taskId', 'taskId');
          reminderStore.createIndex('by-createdAt', 'createdAt');
        }
        if (!db.objectStoreNames.contains('subjects')) {
          const subjectStore = db.createObjectStore('subjects', { keyPath: 'id' });
          subjectStore.createIndex('by-name', 'name');
          subjectStore.createIndex('by-createdAt', 'createdAt');
        }
      },
    });
  }
  return dbPromise;
}

// ─── Targeted Production Demo Data Cleanup Migration ────────────────────────

const DEMO_TASK_TITLES = new Set([
  'Complete ML Assignment 3',
  'Review OS Synchronization Notes',
  'Read Graph Algorithms Chapter 4',
  'Configure TCP Socket Client Lab'
]);

const DEMO_NOTE_TITLES = new Set([
  'ML Lecture 8 — Backpropagation & Gradient Descent',
  'OS Process Synchronization & Semaphores',
  'Algorithms — Dijkstra vs Bellman-Ford',
  'DBMS ACID Properties & Normalization Rules'
]);

const DEMO_REMINDER_TITLES = new Set([
  'Submit Lab Assignment PDF',
  'Review Chapter 4 Graph Algorithms'
]);

const DEMO_SUBJECT_NAMES = new Set([
  'Machine Learning',
  'Operating Systems',
  'Algorithms',
  'Computer Networks',
  'Database Systems'
]);

let migrationRan = false;

export async function runProductionCleanupMigration(): Promise<void> {
  if (migrationRan) return;
  migrationRan = true;

  try {
    const db = await getDB();
    const isCleaned = await db.get('key_val', 'sarah_production_cleaned_v1');
    if (isCleaned) return;

    // 1. Remove only demo tasks
    const allTasks = await db.getAll('tasks');
    for (const task of allTasks) {
      if (DEMO_TASK_TITITIES_MATCH(task)) {
        await db.delete('tasks', task.id);
      }
    }

    // 2. Remove only demo notes
    const allNotes = await db.getAll('notes');
    for (const note of allNotes) {
      if (DEMO_NOTE_TITLES.has(note.title) || note.id.startsWith('note_1786978')) {
        await db.delete('notes', note.id);
      }
    }

    // 3. Remove only demo reminders
    const allReminders = await db.getAll('reminders');
    for (const reminder of allReminders) {
      if (DEMO_REMINDER_TITLES.has(reminder.title) || reminder.id.startsWith('reminder_1786978')) {
        await db.delete('reminders', reminder.id);
      }
    }

    // 4. Remove only demo subjects (preserve user-created subjects)
    const allSubjects = await db.getAll('subjects');
    for (const subject of allSubjects) {
      if (DEMO_SUBJECT_NAMES.has(subject.name) && subject.id.startsWith('subject_1786978')) {
        await db.delete('subjects', subject.id);
      }
    }

    await db.put('key_val', true, 'sarah_production_cleaned_v1');
  } catch (err) {
    console.warn('Production cleanup migration note:', err);
  }
}

function DEMO_TASK_TITITIES_MATCH(task: Task): boolean {
  if (DEMO_TASK_TITLES.has(task.title)) return true;
  if (task.id.startsWith('task_1786978') && DEMO_TASK_TITLES.has(task.title)) return true;
  return false;
}

// ─── Subjects CRUD Operations ───────────────────────────────────────────────

export async function getSubjects(): Promise<Subject[]> {
  await runProductionCleanupMigration();
  const db = await getDB();
  const allSubjects = await db.getAll('subjects');
  return allSubjects.sort((a, b) => a.createdAt - b.createdAt);
}

export async function getSubject(id: string): Promise<Subject | undefined> {
  const db = await getDB();
  return db.get('subjects', id);
}

export async function addSubject(subjectData: Omit<Subject, 'id' | 'createdAt' | 'updatedAt'>): Promise<Subject> {
  const db = await getDB();
  const now = Date.now();
  const newSubject: Subject = {
    ...subjectData,
    id: `subject_${now}_${Math.random().toString(36).substring(2, 7)}`,
    createdAt: now,
    updatedAt: now
  };
  await db.put('subjects', newSubject);
  return newSubject;
}

export async function updateSubject(subject: Subject): Promise<Subject> {
  const db = await getDB();
  const updated: Subject = {
    ...subject,
    updatedAt: Date.now()
  };
  await db.put('subjects', updated);
  return updated;
}

export async function deleteSubject(id: string): Promise<void> {
  const db = await getDB();
  const subjectToDelete = await db.get('subjects', id);
  if (!subjectToDelete) return;

  // Gracefully fallback all tasks, notes, and reminders from deleted subject to 'General'
  const subjectName = subjectToDelete.name;
  
  const allTasks = await db.getAll('tasks');
  for (const task of allTasks) {
    if (task.subject === subjectName) {
      await db.put('tasks', { ...task, subject: 'General', updatedAt: Date.now() });
    }
  }

  const allNotes = await db.getAll('notes');
  for (const note of allNotes) {
    if (note.subject === subjectName) {
      await db.put('notes', { ...note, subject: 'General', updatedAt: Date.now() });
    }
  }

  const allReminders = await db.getAll('reminders');
  for (const reminder of allReminders) {
    if (reminder.subject === subjectName) {
      await db.put('reminders', { ...reminder, subject: 'General', updatedAt: Date.now() });
    }
  }

  await db.delete('subjects', id);
}

// ─── Task CRUD Operations ───────────────────────────────────────────────────

export async function getTasks(): Promise<Task[]> {
  await runProductionCleanupMigration();
  const db = await getDB();
  const allTasks = await db.getAll('tasks');
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
  await runProductionCleanupMigration();
  const db = await getDB();
  const allNotes = await db.getAll('notes');

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

// ─── Reminders CRUD Operations ──────────────────────────────────────────────

export async function getReminders(): Promise<Reminder[]> {
  await runProductionCleanupMigration();
  const db = await getDB();
  const allReminders = await db.getAll('reminders');

  // Sort chronologically by reminderAt ascending
  return allReminders.sort((a, b) => a.reminderAt - b.reminderAt);
}

export async function getReminder(id: string): Promise<Reminder | undefined> {
  const db = await getDB();
  return db.get('reminders', id);
}

export async function addReminder(reminderData: Omit<Reminder, 'id' | 'createdAt' | 'updatedAt'>): Promise<Reminder> {
  const db = await getDB();
  const now = Date.now();
  const newReminder: Reminder = {
    ...reminderData,
    id: `reminder_${now}_${Math.random().toString(36).substring(2, 7)}`,
    createdAt: now,
    updatedAt: now
  };
  await db.put('reminders', newReminder);
  return newReminder;
}

export async function updateReminder(reminder: Reminder): Promise<Reminder> {
  const db = await getDB();
  const updated: Reminder = {
    ...reminder,
    updatedAt: Date.now()
  };
  await db.put('reminders', updated);
  return updated;
}

export async function deleteReminder(id: string): Promise<void> {
  const db = await getDB();
  await db.delete('reminders', id);
}

export async function dismissReminder(id: string): Promise<Reminder | undefined> {
  const db = await getDB();
  const existing = await db.get('reminders', id);
  if (!existing) return undefined;
  const updated: Reminder = {
    ...existing,
    dismissed: true,
    updatedAt: Date.now()
  };
  await db.put('reminders', updated);
  return updated;
}

export async function snoozeReminder(id: string, newTimeEpochMs: number): Promise<Reminder | undefined> {
  const db = await getDB();
  const existing = await db.get('reminders', id);
  if (!existing) return undefined;
  const updated: Reminder = {
    ...existing,
    reminderAt: newTimeEpochMs,
    dismissed: false,
    updatedAt: Date.now()
  };
  await db.put('reminders', updated);
  return updated;
}

// ─── Local Data Initialization ──────────────────────────────────────────────

export interface PersistenceStatus {
  isReady: boolean;
}

export async function initializeAndTrackPersistence(): Promise<PersistenceStatus> {
  await getDB();
  await runProductionCleanupMigration();
  return {
    isReady: true
  };
}
