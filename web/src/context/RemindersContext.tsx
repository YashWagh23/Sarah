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
  openCreateReminderModal: (linkedTaskId?: string, defaultTitle?: string, defaultSubject?: string) => void;
  openEditReminderModal: (reminder: Reminder) => void;
  closeReminderModal: () => void;
}

const RemindersContext = createContext<RemindersContextType | undefined>(undefined);

/** How long after its due time a reminder may still raise an alert. */
const ALERT_WINDOW_MS = 15 * 60 * 1000;

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
    const fire = () => {
      const now = Date.now();
      activeReminders.forEach(reminder => {
        // Only reminders that came due recently are announced. Without this window
        // every reload re-alerts every stale reminder, because the "already alerted"
        // set lives in memory and starts empty each session.
        const dueAgo = now - reminder.reminderAt;
        const isFreshlyDue = dueAgo >= 0 && dueAgo <= ALERT_WINDOW_MS;

        if (isFreshlyDue && !alertedIdsRef.current.has(reminder.id)) {
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
    };

    fire(); // catch anything that came due while this effect was re-created
    const checkInterval = setInterval(fire, 20000);

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
    // Optimistic UI update
    setReminders(prev => prev.filter(r => r.id !== id));
    alertedIdsRef.current.delete(id);
    try {
      await dbDeleteReminder(id);
      showToast('Reminder deleted');
    } catch (err) {
      console.error('Failed to delete reminder:', err);
      await refreshReminders();
    }
  }, [refreshReminders, showToast]);

  const dismiss = useCallback(async (id: string) => {
    // Instant zero-lag optimistic UI update
    setReminders(prev => prev.map(r => r.id === id ? { ...r, dismissed: true, updatedAt: Date.now() } : r));
    alertedIdsRef.current.add(id);
    showToast('Reminder dismissed');
    try {
      await dbDismissReminder(id);
    } catch (err) {
      console.error('Failed to dismiss reminder:', err);
      await refreshReminders();
    }
  }, [refreshReminders, showToast]);

  const snooze = useCallback(async (id: string, newTimeEpochMs: number) => {
    // Instant zero-lag optimistic UI update
    setReminders(prev => prev.map(r => r.id === id ? { ...r, reminderAt: newTimeEpochMs, dismissed: false, updatedAt: Date.now() } : r));
    alertedIdsRef.current.delete(id);
    
    const minutesFromNow = Math.round((newTimeEpochMs - Date.now()) / 60000);
    if (minutesFromNow < 60) {
      showToast(`Snoozed for ${minutesFromNow}m`);
    } else {
      showToast('Reminder snoozed');
    }

    try {
      await dbSnoozeReminder(id, newTimeEpochMs);
    } catch (err) {
      console.error('Failed to snooze reminder:', err);
      await refreshReminders();
    }
  }, [refreshReminders, showToast]);

  // Modal Triggers
  const openCreateReminderModal = useCallback((linkedTaskId?: string, defaultTitle?: string, defaultSubject?: string) => {
    const inOneHour = Date.now() + 3600000;
    setEditingReminder({
      id: '',
      title: defaultTitle || '',
      message: '',
      reminderAt: inOneHour,
      taskId: linkedTaskId || undefined,
      subject: defaultSubject || undefined,
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
