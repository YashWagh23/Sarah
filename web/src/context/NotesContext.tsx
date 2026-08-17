import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import { 
  getNotes, 
  addNote as dbAddNote, 
  updateNote as dbUpdateNote, 
  deleteNote as dbDeleteNote, 
  toggleNotePinned as dbToggleNotePinned,
  type AcademicNote,
  DEFAULT_SUBJECTS
} from '../lib/db';
import { useTasks } from './TasksContext';

interface NotesContextType {
  notes: AcademicNote[];
  isLoading: boolean;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  selectedSubject: string;
  setSelectedSubject: (subject: string) => void;
  
  pinnedNotes: AcademicNote[];
  unpinnedNotes: AcademicNote[];
  filteredNotes: AcademicNote[];
  allSubjects: string[];

  // CRUD Actions
  createNote: (noteData: Omit<AcademicNote, 'id' | 'createdAt' | 'updatedAt'>) => Promise<AcademicNote>;
  modifyNote: (note: AcademicNote) => Promise<AcademicNote>;
  removeNote: (id: string) => Promise<void>;
  togglePin: (id: string) => Promise<void>;
  refreshNotes: () => Promise<void>;

  // Modal Controls
  isNoteModalOpen: boolean;
  editingNote: AcademicNote | null;
  openCreateNoteModal: (defaultSubject?: string) => void;
  openEditNoteModal: (note: AcademicNote) => void;
  closeNoteModal: () => void;
}

const NotesContext = createContext<NotesContextType | undefined>(undefined);

export const NotesProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notes, setNotes] = useState<AcademicNote[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSubject, setSelectedSubject] = useState<string>('all');

  // Modal states
  const [isNoteModalOpen, setIsNoteModalOpen] = useState(false);
  const [editingNote, setEditingNote] = useState<AcademicNote | null>(null);

  const { showToast } = useTasks();

  const refreshNotes = useCallback(async () => {
    try {
      const data = await getNotes();
      setNotes(data);
    } catch (err) {
      console.error('Failed to load notes from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshNotes();
  }, [refreshNotes]);

  // Dynamic Subjects List (Defaults + Any custom subjects in notes)
  const allSubjects = useMemo(() => {
    const set = new Set<string>(DEFAULT_SUBJECTS);
    notes.forEach(n => {
      if (n.subject && n.subject.trim()) {
        set.add(n.subject.trim());
      }
    });
    return Array.from(set);
  }, [notes]);

  // CRUD
  const createNote = useCallback(async (noteData: Omit<AcademicNote, 'id' | 'createdAt' | 'updatedAt'>) => {
    const created = await dbAddNote(noteData);
    await refreshNotes();
    showToast('Note captured successfully');
    return created;
  }, [refreshNotes, showToast]);

  const modifyNote = useCallback(async (note: AcademicNote) => {
    const updated = await dbUpdateNote(note);
    await refreshNotes();
    showToast('Note updated');
    return updated;
  }, [refreshNotes, showToast]);

  const removeNote = useCallback(async (id: string) => {
    await dbDeleteNote(id);
    await refreshNotes();
    showToast('Note deleted');
  }, [refreshNotes, showToast]);

  const togglePin = useCallback(async (id: string) => {
    const target = notes.find(n => n.id === id);
    if (!target) return;
    const newPinned = !target.pinned;
    await dbToggleNotePinned(id, newPinned);
    await refreshNotes();
    showToast(newPinned ? '📌 Note pinned to top' : 'Note unpinned');
  }, [notes, refreshNotes, showToast]);

  // Modal Triggers
  const openCreateNoteModal = useCallback((defaultSubject?: string) => {
    setEditingNote(defaultSubject ? {
      id: '',
      title: '',
      content: '',
      subject: defaultSubject,
      pinned: false,
      createdAt: 0,
      updatedAt: 0
    } : null);
    setIsNoteModalOpen(true);
  }, []);

  const openEditNoteModal = useCallback((note: AcademicNote) => {
    setEditingNote(note);
    setIsNoteModalOpen(true);
  }, []);

  const closeNoteModal = useCallback(() => {
    setIsNoteModalOpen(false);
    setEditingNote(null);
  }, []);

  // Filtered & Grouped Notes
  const filteredNotes = useMemo(() => {
    return notes.filter(n => {
      // 1. Subject filter
      if (selectedSubject !== 'all' && n.subject.toLowerCase() !== selectedSubject.toLowerCase()) {
        return false;
      }
      // 2. Search query filter (title, content, subject)
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesTitle = n.title.toLowerCase().includes(q);
        const matchesContent = n.content.toLowerCase().includes(q);
        const matchesSubject = n.subject.toLowerCase().includes(q);
        if (!matchesTitle && !matchesContent && !matchesSubject) {
          return false;
        }
      }
      return true;
    });
  }, [notes, selectedSubject, searchQuery]);

  const pinnedNotes = useMemo(() => {
    return filteredNotes.filter(n => n.pinned);
  }, [filteredNotes]);

  const unpinnedNotes = useMemo(() => {
    return filteredNotes.filter(n => !n.pinned);
  }, [filteredNotes]);

  const value = useMemo(() => ({
    notes,
    isLoading,
    searchQuery,
    setSearchQuery,
    selectedSubject,
    setSelectedSubject,
    pinnedNotes,
    unpinnedNotes,
    filteredNotes,
    allSubjects,
    createNote,
    modifyNote,
    removeNote,
    togglePin,
    refreshNotes,
    isNoteModalOpen,
    editingNote,
    openCreateNoteModal,
    openEditNoteModal,
    closeNoteModal
  }), [
    notes,
    isLoading,
    searchQuery,
    setSearchQuery,
    selectedSubject,
    setSelectedSubject,
    pinnedNotes,
    unpinnedNotes,
    filteredNotes,
    allSubjects,
    createNote,
    modifyNote,
    removeNote,
    togglePin,
    refreshNotes,
    isNoteModalOpen,
    editingNote,
    openCreateNoteModal,
    openEditNoteModal,
    closeNoteModal
  ]);

  return (
    <NotesContext.Provider value={value}>
      {children}
    </NotesContext.Provider>
  );
};

export function useNotes(): NotesContextType {
  const context = useContext(NotesContext);
  if (!context) {
    throw new Error('useNotes must be used within a NotesProvider');
  }
  return context;
}
