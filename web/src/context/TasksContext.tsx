import React, { createContext, useContext, useState, useEffect, useMemo, useCallback, useRef } from 'react';
import { 
  getTasks, 
  addTask as dbAddTask, 
  updateTask as dbUpdateTask, 
  deleteTask as dbDeleteTask, 
  completeTask as dbCompleteTask,
  type Task
} from '../lib/db';
import { toLocalDateStr, useNow } from '../lib/datetime';

interface TasksContextType {
  tasks: Task[];
  isLoading: boolean;
  activeTasks: Task[];
  completedTasks: Task[];
  todayStr: string;
  
  // CRUD Actions
  createTask: (taskData: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>) => Promise<Task>;
  modifyTask: (task: Task) => Promise<Task>;
  removeTask: (id: string) => Promise<void>;
  toggleTaskCompletion: (id: string) => Promise<void>;
  refresh: () => Promise<void>;

  // Modal / UI Controls
  isTaskModalOpen: boolean;
  editingTask: Task | null;
  initialTaskSubject?: string;
  openCreateTaskModal: (defaultSubject?: string) => void;
  openEditTaskModal: (task: Task) => void;
  closeTaskModal: () => void;

  isQuickAddOpen: boolean;
  openQuickAdd: () => void;
  closeQuickAdd: () => void;

  toastMessage: string | null;
  showToast: (msg: string) => void;
}

const TasksContext = createContext<TasksContextType | undefined>(undefined);

export const TasksProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const now = useNow(30000);

  // Modal states
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [initialTaskSubject, setInitialTaskSubject] = useState<string | undefined>(undefined);
  const [isQuickAddOpen, setIsQuickAddOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // One timer for the toast slot: comparing message text let an earlier timer
  // hide a repeat of the same message after only a fraction of its time.
  const toastTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const showToast = useCallback((msg: string) => {
    setToastMessage(msg);
    if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
    toastTimerRef.current = setTimeout(() => setToastMessage(null), 2800);
  }, []);

  const refresh = useCallback(async () => {
    try {
      const data = await getTasks();
      setTasks(data);
    } catch (err) {
      console.error('Failed to load tasks from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  // CRUD
  const createTask = useCallback(async (taskData: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>) => {
    const created = await dbAddTask(taskData);
    await refresh();
    showToast('Task added');
    return created;
  }, [refresh, showToast]);

  const modifyTask = useCallback(async (task: Task) => {
    const updated = await dbUpdateTask(task);
    await refresh();
    showToast('Task updated');
    return updated;
  }, [refresh, showToast]);

  const removeTask = useCallback(async (id: string) => {
    // Optimistic UI update
    setTasks(prev => prev.filter(t => t.id !== id));
    try {
      await dbDeleteTask(id);
      showToast('Task deleted');
    } catch (err) {
      console.error('Failed to delete task:', err);
      await refresh();
    }
  }, [refresh, showToast]);

  const toggleTaskCompletion = useCallback(async (id: string) => {
    const target = tasks.find(t => t.id === id);
    if (!target) return;
    const newStatus = !target.completed;
    const now = Date.now();
    // Instant zero-lag optimistic UI feedback
    setTasks(prev => prev.map(t => t.id === id
      ? { ...t, completed: newStatus, completedAt: newStatus ? now : undefined, updatedAt: now }
      : t));
    try {
      await dbCompleteTask(id, newStatus);
      showToast(newStatus ? 'Done. Plan updated.' : 'Task reopened');
    } catch (err) {
      console.error('Failed to complete task:', err);
      await refresh();
    }
  }, [tasks, refresh, showToast]);

  // Modal triggers
  const openCreateTaskModal = useCallback((defaultSubject?: string) => {
    setEditingTask(null);
    setInitialTaskSubject(defaultSubject);
    setIsTaskModalOpen(true);
    setIsQuickAddOpen(false);
  }, []);

  const openEditTaskModal = useCallback((task: Task) => {
    setEditingTask(task);
    setInitialTaskSubject(undefined);
    setIsTaskModalOpen(true);
    setIsQuickAddOpen(false);
  }, []);

  const closeTaskModal = useCallback(() => {
    setIsTaskModalOpen(false);
    setEditingTask(null);
    setInitialTaskSubject(undefined);
  }, []);

  const openQuickAdd = useCallback(() => {
    setIsQuickAddOpen(true);
  }, []);

  const closeQuickAdd = useCallback(() => {
    setIsQuickAddOpen(false);
  }, []);

  // Filtered views
  const activeTasks = useMemo(() => tasks.filter(t => !t.completed), [tasks]);
  const completedTasks = useMemo(() => tasks.filter(t => t.completed), [tasks]);

  // Recomputed from the ticking clock so an app left open overnight rolls over
  // to the new day instead of staying on the day it was launched.
  const todayStr = useMemo(() => toLocalDateStr(new Date(now)), [now]);

  const value = useMemo(() => ({
    tasks,
    isLoading,
    activeTasks,
    completedTasks,
    todayStr,
    createTask,
    modifyTask,
    removeTask,
    toggleTaskCompletion,
    refresh,
    isTaskModalOpen,
    editingTask,
    initialTaskSubject,
    openCreateTaskModal,
    openEditTaskModal,
    closeTaskModal,
    isQuickAddOpen,
    openQuickAdd,
    closeQuickAdd,
    toastMessage,
    showToast
  }), [
    tasks,
    isLoading,
    activeTasks,
    completedTasks,
    todayStr,
    createTask,
    modifyTask,
    removeTask,
    toggleTaskCompletion,
    refresh,
    isTaskModalOpen,
    editingTask,
    initialTaskSubject,
    openCreateTaskModal,
    openEditTaskModal,
    closeTaskModal,
    isQuickAddOpen,
    openQuickAdd,
    closeQuickAdd,
    toastMessage,
    showToast
  ]);

  return (
    <TasksContext.Provider value={value}>
      {children}
    </TasksContext.Provider>
  );
};

export function useTasks(): TasksContextType {
  const context = useContext(TasksContext);
  if (!context) {
    throw new Error('useTasks must be used within a TasksProvider');
  }
  return context;
}
