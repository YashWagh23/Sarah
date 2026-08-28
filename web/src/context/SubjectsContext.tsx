import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { 
  getSubjects, 
  addSubject as dbAddSubject, 
  updateSubject as dbUpdateSubject, 
  deleteSubject as dbDeleteSubject, 
  type Subject,
  SUBJECT_PALETTE,
  SUBJECT_COLORS
} from '../lib/db';
import { useTasks } from './TasksContext';
import { useNotes } from './NotesContext';
import { useReminders } from './RemindersContext';

interface SubjectsContextType {
  subjects: Subject[];
  isLoading: boolean;
  subjectNames: string[];
  
  // Helpers
  getSubjectColor: (nameOrId: string) => string;
  getSubjectByName: (name: string) => Subject | undefined;
  
  // CRUD Actions
  createSubject: (subjectData: Omit<Subject, 'id' | 'createdAt' | 'updatedAt'>) => Promise<Subject>;
  modifySubject: (subject: Subject) => Promise<Subject>;
  removeSubject: (id: string) => Promise<void>;
  refreshSubjects: () => Promise<void>;

  // Modal Controls
  isSubjectModalOpen: boolean;
  editingSubject: Subject | null;
  openCreateSubjectModal: () => void;
  openEditSubjectModal: (subject: Subject) => void;
  closeSubjectModal: () => void;

  // Detail View State
  viewingSubject: Subject | null;
  openSubjectDetail: (subject: Subject) => void;
  closeSubjectDetail: () => void;
}

const SubjectsContext = createContext<SubjectsContextType | undefined>(undefined);

export const SubjectsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal states
  const [isSubjectModalOpen, setIsSubjectModalOpen] = useState(false);
  const [editingSubject, setEditingSubject] = useState<Subject | null>(null);

  // Detail view state
  const [viewingSubject, setViewingSubject] = useState<Subject | null>(null);

  const { showToast, refresh: refreshTasks } = useTasks();
  const { refreshNotes } = useNotes();
  const { refreshReminders } = useReminders();

  const refreshSubjects = useCallback(async () => {
    try {
      const data = await getSubjects();
      setSubjects(data);
      // Keep viewingSubject state updated if active
      setViewingSubject(current => {
        if (!current) return null;
        return data.find(s => s.id === current.id) || null;
      });
    } catch (err) {
      console.error('Failed to load subjects from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshSubjects();
  }, [refreshSubjects]);

  const subjectNames = useMemo(() => {
    return subjects.map(s => s.name);
  }, [subjects]);

  const getSubjectByName = useCallback((name: string): Subject | undefined => {
    return subjects.find(s => s.name.toLowerCase() === name.toLowerCase());
  }, [subjects]);

  const getSubjectColor = useCallback((nameOrId: string): string => {
    // 1. Check exact subject name match
    const byName = subjects.find(s => s.name.toLowerCase() === nameOrId.toLowerCase() || s.id === nameOrId);
    if (byName) return byName.color;

    // 2. Check predefined static map
    if (SUBJECT_COLORS[nameOrId]) return SUBJECT_COLORS[nameOrId];

    // 3. Fallback to hash from palette
    let hash = 0;
    for (let i = 0; i < nameOrId.length; i++) {
      hash = nameOrId.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % SUBJECT_PALETTE.length;
    return SUBJECT_PALETTE[index];
  }, [subjects]);

  // CRUD Actions
  const createSubject = useCallback(async (subjectData: Omit<Subject, 'id' | 'createdAt' | 'updatedAt'>) => {
    const created = await dbAddSubject(subjectData);
    await refreshSubjects();
    showToast(`Subject "${created.name}" created`);
    return created;
  }, [refreshSubjects, showToast]);

  const modifySubject = useCallback(async (subject: Subject) => {
    const updated = await dbUpdateSubject(subject);
    await refreshSubjects();
    showToast(`Subject "${updated.name}" updated`);
    return updated;
  }, [refreshSubjects, showToast]);

  const removeSubject = useCallback(async (id: string) => {
    const toDelete = subjects.find(s => s.id === id);
    await dbDeleteSubject(id);
    await refreshSubjects();
    await refreshTasks(); // Refresh tasks as orphan records moved to 'General'
    await refreshNotes(); // Refresh notes as orphan records moved to 'General'
    await refreshReminders(); // Refresh reminders as orphan records moved to 'General'
    if (viewingSubject?.id === id) {
      setViewingSubject(null);
    }
    showToast(`Subject "${toDelete?.name || ''}" deleted (items moved to General)`);
  }, [subjects, refreshSubjects, refreshTasks, refreshNotes, refreshReminders, viewingSubject, showToast]);

  // Modal Controls
  const openCreateSubjectModal = useCallback(() => {
    setEditingSubject(null);
    setIsSubjectModalOpen(true);
  }, []);

  const openEditSubjectModal = useCallback((subject: Subject) => {
    setEditingSubject(subject);
    setIsSubjectModalOpen(true);
  }, []);

  const closeSubjectModal = useCallback(() => {
    setIsSubjectModalOpen(false);
    setEditingSubject(null);
  }, []);

  // Detail View Controls
  const openSubjectDetail = useCallback((subject: Subject) => {
    setViewingSubject(subject);
  }, []);

  const closeSubjectDetail = useCallback(() => {
    setViewingSubject(null);
  }, []);

  const value = useMemo(() => ({
    subjects,
    isLoading,
    subjectNames,
    getSubjectColor,
    getSubjectByName,
    createSubject,
    modifySubject,
    removeSubject,
    refreshSubjects,
    isSubjectModalOpen,
    editingSubject,
    openCreateSubjectModal,
    openEditSubjectModal,
    closeSubjectModal,
    viewingSubject,
    openSubjectDetail,
    closeSubjectDetail
  }), [
    subjects,
    isLoading,
    subjectNames,
    getSubjectColor,
    getSubjectByName,
    createSubject,
    modifySubject,
    removeSubject,
    refreshSubjects,
    isSubjectModalOpen,
    editingSubject,
    openCreateSubjectModal,
    openEditSubjectModal,
    closeSubjectModal,
    viewingSubject,
    openSubjectDetail,
    closeSubjectDetail
  ]);

  return (
    <SubjectsContext.Provider value={value}>
      {children}
    </SubjectsContext.Provider>
  );
};

export function useSubjects(): SubjectsContextType {
  const context = useContext(SubjectsContext);
  if (!context) {
    throw new Error('useSubjects must be used within a SubjectsProvider');
  }
  return context;
}
