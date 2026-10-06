import React, { useState } from 'react';
import { AppShell } from './components/AppShell';
import { type TabId } from './components/BottomNav';
import { TodayScreen } from './screens/TodayScreen';
import { TasksScreen } from './screens/TasksScreen';
import { NotesScreen } from './screens/NotesScreen';
import { SubjectsScreen } from './screens/SubjectsScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { TasksProvider, useTasks } from './context/TasksContext';
import { UserProfileProvider } from './context/UserProfileContext';
import { NotesProvider } from './context/NotesContext';
import { RemindersProvider } from './context/RemindersContext';
import { SubjectsProvider } from './context/SubjectsContext';
import { TaskModal } from './components/TaskModal';
import { NoteModal } from './components/NoteModal';
import { ReminderModal } from './components/ReminderModal';
import { SubjectModal } from './components/SubjectModal';
import { QuickAddMenu } from './components/QuickAddMenu';
import { WelcomeSheet } from './components/WelcomeSheet';

const AppContent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabId>('today');
  const { toastMessage } = useTasks();

  const renderActiveScreen = () => {
    switch (activeTab) {
      case 'today':
        return <TodayScreen key="today" onNavigateToNotes={() => setActiveTab('notes')} />;
      case 'tasks':
        return <TasksScreen key="tasks" />;
      case 'notes':
        return <NotesScreen key="notes" />;
      case 'subjects':
        return <SubjectsScreen key="subjects" />;
      case 'profile':
        return <ProfileScreen key="profile" />;
      default:
        return <TodayScreen key="today" onNavigateToNotes={() => setActiveTab('notes')} />;
    }
  };

  return (
    <AppShell activeTab={activeTab} onTabSelect={setActiveTab}>
      {renderActiveScreen()}

      {/* Global Quick Add Floating Action Menu */}
      <QuickAddMenu hidden={activeTab === 'profile'} />

      {/* Global Task Modal (Add & Edit Bottom Sheet) */}
      <TaskModal />

      {/* Global Note Modal (Add & Edit Bottom Sheet) */}
      <NoteModal />

      {/* Global Reminder Modal (Add & Edit Bottom Sheet) */}
      <ReminderModal />

      {/* Global Subject Modal (Add & Edit Bottom Sheet) */}
      <SubjectModal />

      {/* First-run setup: name, classes and bedtime feed tonight's plan */}
      <WelcomeSheet />

      {/* Global toast */}
      {toastMessage && (
        <div key={toastMessage} role="status" aria-live="polite" className="toast">
          {toastMessage}
        </div>
      )}
    </AppShell>
  );
};

export const App: React.FC = () => {
  return (
    <TasksProvider>
      <UserProfileProvider>
        <NotesProvider>
          <RemindersProvider>
            <SubjectsProvider>
              <AppContent />
            </SubjectsProvider>
          </RemindersProvider>
        </NotesProvider>
      </UserProfileProvider>
    </TasksProvider>
  );
};

export default App;
