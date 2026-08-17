import React, { useState } from 'react';
import { AppShell } from './components/AppShell';
import { type TabId } from './components/BottomNav';
import { TodayScreen } from './screens/TodayScreen';
import { TasksScreen } from './screens/TasksScreen';
import { NotesScreen } from './screens/NotesScreen';
import { SubjectsScreen } from './screens/SubjectsScreen';
import { ProfileScreen } from './screens/ProfileScreen';
import { TasksProvider, useTasks } from './context/TasksContext';
import { TaskModal } from './components/TaskModal';
import { QuickAddMenu } from './components/QuickAddMenu';

const AppContent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabId>('today');
  const { toastMessage } = useTasks();

  const renderActiveScreen = () => {
    switch (activeTab) {
      case 'today':
        return <TodayScreen key="today" />;
      case 'tasks':
        return <TasksScreen key="tasks" />;
      case 'notes':
        return <NotesScreen key="notes" />;
      case 'subjects':
        return <SubjectsScreen key="subjects" />;
      case 'profile':
        return <ProfileScreen key="profile" />;
      default:
        return <TodayScreen key="today" />;
    }
  };

  return (
    <AppShell activeTab={activeTab} onTabSelect={setActiveTab}>
      {renderActiveScreen()}

      {/* Global Quick Add Floating Action Menu */}
      <QuickAddMenu />

      {/* Global Task Modal (Add & Edit Bottom Sheet) */}
      <TaskModal />

      {/* Global Toast Notification */}
      {toastMessage && (
        <div
          style={{
            position: 'fixed',
            top: '64px',
            left: '50%',
            transform: 'translateX(-50%)',
            backgroundColor: 'rgba(26, 28, 29, 0.92)',
            color: '#FFFFFF',
            padding: '8px 18px',
            borderRadius: '20px',
            fontSize: '12.5px',
            fontWeight: 600,
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.2)',
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            zIndex: 120,
            pointerEvents: 'none',
            whiteSpace: 'nowrap',
            animation: 'fadeIn 0.2s ease'
          }}
        >
          {toastMessage}
        </div>
      )}
    </AppShell>
  );
};

export const App: React.FC = () => {
  return (
    <TasksProvider>
      <AppContent />
    </TasksProvider>
  );
};

export default App;
