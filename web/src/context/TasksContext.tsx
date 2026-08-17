import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { 
  getTasks, 
  addTask as dbAddTask, 
  updateTask as dbUpdateTask, 
  deleteTask as dbDeleteTask, 
  completeTask as dbCompleteTask,
  type Task
} from '../lib/db';

interface TasksContextType {
  tasks: Task[];
  isLoading: boolean;
  activeTasks: Task[];
  completedTasks: Task[];
  todayTasks: Task[];
  mustDoTasks: Task[];
  shouldDoTasks: Task[];
  laterTasks: Task[];
  nextActionTask: Task | null;
  
  // CRUD Actions
  createTask: (taskData: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>) => Promise<Task>;
  modifyTask: (task: Task) => Promise<Task>;
  removeTask: (id: string) => Promise<void>;
  toggleTaskCompletion: (id: string) => Promise<void>;
  refresh: () => Promise<void>;

  // Modal / UI Controls
  isTaskModalOpen: boolean;
  editingTask: Task | null;
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

  // Modal states
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [isQuickAddOpen, setIsQuickAddOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = useCallback((msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(prev => (prev === msg ? null : prev));
    }, 2800);
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
    showToast('Task added successfully');
    return created;
  }, [refresh, showToast]);

  const modifyTask = useCallback(async (task: Task) => {
    const updated = await dbUpdateTask(task);
    await refresh();
    showToast('Task updated');
    return updated;
  }, [refresh, showToast]);

  const removeTask = useCallback(async (id: string) => {
    await dbDeleteTask(id);
    await refresh();
    showToast('Task deleted');
  }, [refresh, showToast]);

  const toggleTaskCompletion = useCallback(async (id: string) => {
    const target = tasks.find(t => t.id === id);
    if (!target) return;
    const newStatus = !target.completed;
    await dbCompleteTask(id, newStatus);
    await refresh();
    showToast(newStatus ? 'Task completed! 🎉' : 'Task reopened');
  }, [tasks, refresh, showToast]);

  // Modal triggers
  const openCreateTaskModal = useCallback((_defaultSubject?: string) => {
    setEditingTask(null);
    setIsTaskModalOpen(true);
    setIsQuickAddOpen(false);
  }, []);

  const openEditTaskModal = useCallback((task: Task) => {
    setEditingTask(task);
    setIsTaskModalOpen(true);
    setIsQuickAddOpen(false);
  }, []);

  const closeTaskModal = useCallback(() => {
    setIsTaskModalOpen(false);
    setEditingTask(null);
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

  const todayStr = useMemo(() => {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }, []);

  const todayTasks = useMemo(() => {
    return tasks.filter(t => t.deadline <= todayStr && !t.completed);
  }, [tasks, todayStr]);

  const mustDoTasks = useMemo(() => {
    return activeTasks.filter(t => t.priority === 'must');
  }, [activeTasks]);

  const shouldDoTasks = useMemo(() => {
    return activeTasks.filter(t => t.priority === 'should');
  }, [activeTasks]);

  const laterTasks = useMemo(() => {
    return activeTasks.filter(t => t.priority === 'later');
  }, [activeTasks]);

  // Deterministic Next Move Engine
  // 1. Overdue incomplete tasks
  // 2. Nearest deadline today
  // 3. Highest priority (must -> should -> later)
  const nextActionTask = useMemo(() => {
    if (activeTasks.length === 0) return null;

    // Check overdue
    const overdue = activeTasks.filter(t => t.deadline < todayStr);
    if (overdue.length > 0) {
      // Sort by priority (must > should > later)
      const priorityWeights: Record<string, number> = { must: 3, should: 2, later: 1 };
      return [...overdue].sort((a, b) => priorityWeights[b.priority] - priorityWeights[a.priority])[0];
    }

    // Check due today
    const dueToday = activeTasks.filter(t => t.deadline === todayStr);
    if (dueToday.length > 0) {
      const priorityWeights: Record<string, number> = { must: 3, should: 2, later: 1 };
      return [...dueToday].sort((a, b) => {
        const pDiff = priorityWeights[b.priority] - priorityWeights[a.priority];
        if (pDiff !== 0) return pDiff;
        // If times exist, compare times
        if (a.deadlineTime && b.deadlineTime) {
          return a.deadlineTime.localeCompare(b.deadlineTime);
        }
        return 0;
      })[0];
    }

    // Otherwise, highest priority nearest future deadline
    const priorityWeights: Record<string, number> = { must: 3, should: 2, later: 1 };
    return [...activeTasks].sort((a, b) => {
      const pDiff = priorityWeights[b.priority] - priorityWeights[a.priority];
      if (pDiff !== 0) return pDiff;
      return a.deadline.localeCompare(b.deadline);
    })[0];
  }, [activeTasks, todayStr]);

  const value = useMemo(() => ({
    tasks,
    isLoading,
    activeTasks,
    completedTasks,
    todayTasks,
    mustDoTasks,
    shouldDoTasks,
    laterTasks,
    nextActionTask,
    createTask,
    modifyTask,
    removeTask,
    toggleTaskCompletion,
    refresh,
    isTaskModalOpen,
    editingTask,
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
    todayTasks,
    mustDoTasks,
    shouldDoTasks,
    laterTasks,
    nextActionTask,
    createTask,
    modifyTask,
    removeTask,
    toggleTaskCompletion,
    refresh,
    isTaskModalOpen,
    editingTask,
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
