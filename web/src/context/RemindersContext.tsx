import React, { createContext, useContext, useState, useEffect, useMemo, useCallback, useRef } from 'react';
import { 
  getReminders, 
  addReminder as dbAddReminder, 
  updateReminder as dbUpdateReminder, 
  deleteReminder as dbDeleteReminder, 
  dismissReminder as dbDismissReminder, 
  snoozeReminder as dbSnoozeReminder,
  type Reminder 
} from '../lib/db';
import { useTasks } from './TasksContext';

interface RemindersContextType {
  reminders: Reminder[];
  activeReminders: Reminder[];
  isLoading: boolean;
  notificationPermission: NotificationPermission | 'unsupported';
  
  // Actions
  createReminder: (reminderData: Omit<Reminder, 'id' | 'createdAt' | 'updatedAt'>) => Promise<Reminder>;
  modifyReminder: (reminder: Reminder) => Promise<Reminder>;
  removeReminder: (id: string) => Promise<void>;
  dismiss: (id: string) => Promise<void>;
  snooze: (id: string, newTimeEpochMs: number) => Promise<void>;
  requestNotificationPermission: () => Promise<NotificationPermission | 'unsupported'>;
  refreshReminders: () => Promise<void>;

  // Modal Controls
  isReminderModalOpen: boolean;
  editingReminder: Reminder | null;
  openCreateReminderModal: (linkedTaskId?: string, defaultTitle?: string) => void;
  openEditReminderModal: (reminder: Reminder) => void;
  closeReminderModal: () => void;
}

const RemindersContext = createContext<RemindersContextType | undefined>(undefined);

export const RemindersProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [notificationPermission, setNotificationPermission] = useState<NotificationPermission | 'unsupported'>('default');

  // Modal states
  const [isReminderModalOpen, setIsReminderModalOpen] = useState(false);
  const [editingReminder, setEditingReminder] = useState<Reminder | null>(null);

  const { showToast } = useTasks();
  const alertedIdsRef = useRef<Set<string>>(new Set());

  // Check initial notification support & permission
  useEffect(() => {
    if (typeof window !== 'undefined' && 'Notification' in window) {
      setNotificationPermission(Notification.permission);
    } else {
      setNotificationPermission('unsupported');
    }
  }, []);

  const refreshReminders = useCallback(async () => {
    try {
      const data = await getReminders();
      setReminders(data);
    } catch (err) {
      console.error('Failed to load reminders from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshReminders();
  }, [refreshReminders]);

  // Request browser notification permission on user action
  const requestNotificationPermission = useCallback(async () => {
    if (typeof window === 'undefined' || !('Notification' in window)) {
      setNotificationPermission('unsupported');
      showToast('Browser notifications are not supported on this device');
      return 'unsupported';
    }

    try {
      const permission = await Notification.requestPermission();
      setNotificationPermission(permission);
      if (permission === 'granted') {
        showToast('🔔 Browser notifications enabled');
      } else if (permission === 'denied') {
        showToast('Notifications blocked in browser settings');
      }
      return permission;
    } catch (err) {
      console.error('Notification permission error:', err);
      return 'unsupported';
    }
  }, [showToast]);

  // Active reminders (not dismissed & not completed)
  const activeReminders = useMemo(() => {
    return reminders
      .filter(r => !r.dismissed && !r.completed)
      .sort((a, b) => a.reminderAt - b.reminderAt);
  }, [reminders]);

  // Client-side In-App Reminder Watcher
  useEffect(() => {
    const checkInterval = setInterval(() => {
      const now = Date.now();
      activeReminders.forEach(reminder => {
        // If within alert window and not yet alerted this session
        if (reminder.reminderAt <= now && !alertedIdsRef.current.has(reminder.id)) {
          alertedIdsRef.current.add(reminder.id);

          // 1. In-app toast alert
          showToast(`⏰ Reminder: ${reminder.title}`);

          // 2. System browser notification if permission granted
          if (
            typeof window !== 'undefined' && 
            'Notification' in window && 
            Notification.permission === 'granted'
          ) {
            try {
              new Notification(`Sarah: ${reminder.title}`, {
                body: reminder.message || 'Academic reminder is due now.',
                icon: './favicon.png'
              });
            } catch (e) {
              console.warn('Could not spawn browser notification:', e);
            }
          }
        }
      });
    }, 20000); // check every 20s

    return () => clearInterval(checkInterval);
  }, [activeReminders, showToast]);

  // CRUD Actions
  const createReminder = useCallback(async (reminderData: Omit<Reminder, 'id' | 'createdAt' | 'updatedAt'>) => {
    const created = await dbAddReminder(reminderData);
    await refreshReminders();
    showToast('Reminder set');
    return created;
  }, [refreshReminders, showToast]);

  const modifyReminder = useCallback(async (reminder: Reminder) => {
    const updated = await dbUpdateReminder(reminder);
    // Reset alert tracking if time moved into future
    if (updated.reminderAt > Date.now()) {
      alertedIdsRef.current.delete(updated.id);
    }
    await refreshReminders();
    showToast('Reminder updated');
    return updated;
  }, [refreshReminders, showToast]);

  const removeReminder = useCallback(async (id: string) => {
    await dbDeleteReminder(id);
    alertedIdsRef.current.delete(id);
    await refreshReminders();
    showToast('Reminder deleted');
  }, [refreshReminders, showToast]);

  const dismiss = useCallback(async (id: string) => {
    await dbDismissReminder(id);
    alertedIdsRef.current.add(id);
    await refreshReminders();
    showToast('Reminder dismissed');
  }, [refreshReminders, showToast]);

  const snooze = useCallback(async (id: string, newTimeEpochMs: number) => {
    await dbSnoozeReminder(id, newTimeEpochMs);
    alertedIdsRef.current.delete(id);
    await refreshReminders();
    
    const minutesFromNow = Math.round((newTimeEpochMs - Date.now()) / 60000);
    if (minutesFromNow < 60) {
      showToast(`Snoozed for ${minutesFromNow}m`);
    } else {
      showToast('Reminder snoozed');
    }
  }, [refreshReminders, showToast]);

  // Modal Triggers
  const openCreateReminderModal = useCallback((linkedTaskId?: string, defaultTitle?: string) => {
    const inOneHour = Date.now() + 3600000;
    setEditingReminder({
      id: '',
      title: defaultTitle || '',
      message: '',
      reminderAt: inOneHour,
      taskId: linkedTaskId || undefined,
      completed: false,
      dismissed: false,
      createdAt: 0,
      updatedAt: 0
    });
    setIsReminderModalOpen(true);
  }, []);

  const openEditReminderModal = useCallback((reminder: Reminder) => {
    setEditingReminder(reminder);
    setIsReminderModalOpen(true);
  }, []);

  const closeReminderModal = useCallback(() => {
    setIsReminderModalOpen(false);
    setEditingReminder(null);
  }, []);

  const value = useMemo(() => ({
    reminders,
    activeReminders,
    isLoading,
    notificationPermission,
    createReminder,
    modifyReminder,
    removeReminder,
    dismiss,
    snooze,
    requestNotificationPermission,
    refreshReminders,
    isReminderModalOpen,
    editingReminder,
    openCreateReminderModal,
    openEditReminderModal,
    closeReminderModal
  }), [
    reminders,
    activeReminders,
    isLoading,
    notificationPermission,
    createReminder,
    modifyReminder,
    removeReminder,
    dismiss,
    snooze,
    requestNotificationPermission,
    refreshReminders,
    isReminderModalOpen,
    editingReminder,
    openCreateReminderModal,
    openEditReminderModal,
    closeReminderModal
  ]);

  return (
    <RemindersContext.Provider value={value}>
      {children}
    </RemindersContext.Provider>
  );
};

export function useReminders(): RemindersContextType {
  const context = useContext(RemindersContext);
  if (!context) {
    throw new Error('useReminders must be used within a RemindersProvider');
  }
  return context;
}
