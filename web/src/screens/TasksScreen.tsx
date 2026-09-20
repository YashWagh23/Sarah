import React, { useMemo, useState } from 'react';
import { 
  Plus, 
  Search, 
  CheckCircle2, 
  ChevronDown, 
  ChevronRight,
  Sparkles,
  X 
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';
import { TaskCard } from '../components/TaskCard';

export const TasksScreen: React.FC = () => {
  const { 
    tasks, 
    activeTasks, 
    completedTasks, 
    toggleTaskCompletion, 
    openEditTaskModal, 
    openCreateTaskModal,
    removeTask,
    todayStr
  } = useTasks();

  const { subjects, getSubjectColor } = useSubjects();

  const [activeStatusFilter, setActiveStatusFilter] = useState<'all' | 'due_today' | 'in_progress' | 'completed'>('all');
  const [selectedSubjectFilter, setSelectedSubjectFilter] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [isCompletedSectionOpen, setIsCompletedSectionOpen] = useState(true);

  const dueTodayCount = activeTasks.filter(t => t.deadline <= todayStr).length;

  const statusFilters = [
    { id: 'all', label: `All (${tasks.length})` },
    { id: 'due_today', label: `Due Today (${dueTodayCount})` },
    { id: 'in_progress', label: `In Progress (${activeTasks.length})` },
    { id: 'completed', label: `Completed (${completedTasks.length})` }
  ] as const;

  // Filter tasks based on status, subject, and search query
  const filteredTasks = useMemo(() => {
    return tasks.filter((task) => {
      // 1. Search query filter
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchesTitle = task.title.toLowerCase().includes(q);
        const matchesSubject = task.subject.toLowerCase().includes(q);
        const matchesDesc = task.description?.toLowerCase().includes(q) || false;
        if (!matchesTitle && !matchesSubject && !matchesDesc) {
          return false;
        }
      }

      // 2. Status filter
      if (activeStatusFilter === 'completed' && !task.completed) return false;
      if (activeStatusFilter === 'in_progress' && task.completed) return false;
      if (activeStatusFilter === 'due_today' && (task.deadline > todayStr || task.completed)) return false;

      // 3. Subject filter
      if (selectedSubjectFilter !== 'all' && task.subject.toLowerCase() !== selectedSubjectFilter.toLowerCase()) {
        return false;
      }

      return true;
    });
  }, [tasks, activeStatusFilter, selectedSubjectFilter, searchQuery, todayStr]);

  // Chips cover every subject a task is actually filed under — including free-typed
  // ones and the "General" fallback, which have no Subject record to iterate.
  const subjectFilters = useMemo(() => {
    const byKey = new Map<string, { key: string; name: string; label: string; color: string; count: number }>();

    for (const sub of subjects) {
      const key = sub.name.toLowerCase();
      byKey.set(key, {
        key,
        name: sub.name,
        label: sub.code || sub.name,
        color: sub.color || getSubjectColor(sub.name),
        count: 0
      });
    }

    for (const task of tasks) {
      const name = task.subject?.trim();
      if (!name) continue;
      const key = name.toLowerCase();
      const existing = byKey.get(key);
      if (existing) {
        existing.count++;
      } else {
        byKey.set(key, { key, name, label: name, color: getSubjectColor(name), count: 1 });
      }
    }

    return Array.from(byKey.values())
      .filter(entry => entry.count > 0 || selectedSubjectFilter.toLowerCase() === entry.key)
      .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name));
  }, [subjects, tasks, getSubjectColor, selectedSubjectFilter]);

  const activeFiltered = filteredTasks.filter(t => !t.completed);
  const completedFiltered = filteredTasks.filter(t => t.completed);

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '14px'
      }}
    >
      {/* Top Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0, letterSpacing: '-0.02em' }}>
            Academic Tasks
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
            {activeTasks.length} pending • {completedTasks.length} completed
          </p>
        </div>
        <button
          type="button"
          onClick={() => openCreateTaskModal()}
          className="btn-press"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            background: 'var(--sarah-primary)',
            color: 'var(--sarah-on-primary)',
            border: 'none',
            borderRadius: '12px',
            padding: '8px 14px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            boxShadow: '0 3px 10px rgba(var(--sarah-primary-rgb), 0.28)',
            whiteSpace: 'nowrap',
            flexShrink: 0
          }}
        >
          <Plus size={15} strokeWidth={2.5} />
          <span>New Task</span>
        </button>
      </div>

      {/* Search Bar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: 'var(--sarah-surface-card)',
          border: '1px solid var(--sarah-outline-variant)',
          borderRadius: '14px',
          padding: '9px 12px',
          boxShadow: '0 2px 6px rgba(0, 0, 0, 0.02)'
        }}
      >
        <Search size={16} color="var(--sarah-secondary)" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search by task title, subject or notes..."
          style={{
            border: 'none',
            outline: 'none',
            background: 'none',
            fontSize: '13.5px',
            width: '100%',
            color: 'var(--sarah-on-background)'
          }}
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => setSearchQuery('')}
            style={{
              background: 'var(--sarah-surface-container-high)',
              border: 'none',
              borderRadius: '50%',
              width: '18px',
              height: '18px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--sarah-secondary)'
            }}
          >
            <X size={11} />
          </button>
        )}
      </div>

      {/* 1. Status Filter Chips */}
      <div 
        style={{ 
          display: 'flex', 
          gap: '8px', 
          overflowX: 'auto', 
          paddingBottom: '2px' 
        }}
        className="scroll-container"
      >
        {statusFilters.map((f) => {
          const isSelected = activeStatusFilter === f.id;
          return (
            <button
              key={f.id}
              type="button"
              onClick={() => setActiveStatusFilter(f.id)}
              className="btn-press"
              style={{
                border: 'none',
                borderRadius: '16px',
                padding: '7px 14px',
                fontSize: '12px',
                fontWeight: isSelected ? 700 : 500,
                cursor: 'pointer',
                whiteSpace: 'nowrap',
                backgroundColor: isSelected ? 'var(--sarah-primary)' : 'var(--sarah-surface-card)',
                color: isSelected ? 'var(--sarah-on-primary)' : 'var(--sarah-on-surface-variant)',
                boxShadow: isSelected ? '0 2px 8px rgba(var(--sarah-primary-rgb), 0.25)' : '0 1px 3px rgba(0, 0, 0, 0.04)',
                transition: 'all 0.15s ease'
              }}
            >
              {f.label}
            </button>
          );
        })}
      </div>

      {/* 2. Subject Filter Chips */}
      {subjectFilters.length > 0 && (
        <div 
          style={{ 
            display: 'flex', 
            gap: '6px', 
            overflowX: 'auto', 
            paddingBottom: '2px' 
          }}
          className="scroll-container"
        >
          <button
            type="button"
            onClick={() => setSelectedSubjectFilter('all')}
            aria-pressed={selectedSubjectFilter === 'all'}
            className="btn-press"
            style={{
              border: selectedSubjectFilter === 'all' ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
              borderRadius: '14px',
              padding: '4px 10px',
              fontSize: '11.5px',
              fontWeight: selectedSubjectFilter === 'all' ? 700 : 500,
              cursor: 'pointer',
              whiteSpace: 'nowrap',
              backgroundColor: selectedSubjectFilter === 'all' ? 'rgba(var(--sarah-primary-rgb), 0.1)' : 'transparent',
              color: selectedSubjectFilter === 'all' ? 'var(--sarah-primary)' : 'var(--sarah-secondary)'
            }}
          >
            All Courses
          </button>

          {subjectFilters.map((entry) => {
            const isSelected = selectedSubjectFilter.toLowerCase() === entry.key;
            return (
              <button
                key={entry.key}
                type="button"
                aria-pressed={isSelected}
                onClick={() => setSelectedSubjectFilter(isSelected ? 'all' : entry.name)}
                className="btn-press"
                style={{
                  border: isSelected ? `1.5px solid ${entry.color}` : '1px solid var(--sarah-outline-variant)',
                  borderRadius: '14px',
                  padding: '4px 10px',
                  fontSize: '11.5px',
                  fontWeight: isSelected ? 700 : 500,
                  cursor: 'pointer',
                  whiteSpace: 'nowrap',
                  backgroundColor: isSelected ? `${entry.color}22` : 'transparent',
                  color: isSelected ? entry.color : 'var(--sarah-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px'
                }}
              >
                <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: entry.color }} />
                <span>{entry.label} ({entry.count})</span>
              </button>
            );
          })}
        </div>
      )}

      {/* Active Tasks List */}
      {activeStatusFilter !== 'completed' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {activeFiltered.length > 0 ? (
            activeFiltered.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                showDate={true}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
                onDelete={removeTask}
              />
            ))
          ) : (
            // Only shown when completed results exist below; otherwise the single
            // full empty state at the bottom of the screen covers it.
            completedFiltered.length > 0 && (
              <div style={{ textAlign: 'center', padding: '30px 10px', color: 'var(--sarah-secondary)', fontSize: '13px' }}>
                No active tasks matching this filter.
              </div>
            )
          )}
        </div>
      )}

      {/* Completed Tasks Collapsible Section */}
      {(activeStatusFilter === 'all' || activeStatusFilter === 'completed') && completedFiltered.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '10px' }}>
          <button
            type="button"
            onClick={() => setIsCompletedSectionOpen(!isCompletedSectionOpen)}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              background: 'none',
              border: 'none',
              padding: '6px 2px',
              cursor: 'pointer',
              color: 'var(--sarah-secondary)'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CheckCircle2 size={15} color="var(--sarah-success)" />
              <span style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                Completed ({completedFiltered.length})
              </span>
            </div>
            {isCompletedSectionOpen ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
          </button>

          {isCompletedSectionOpen && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {completedFiltered.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  showDate={true}
                  onToggle={toggleTaskCompletion}
                  onEdit={openEditTaskModal}
                  onDelete={removeTask}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Empty State when zero total tasks match */}
      {filteredTasks.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: '40px 10px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px'
          }}
        >
          <div
            style={{
              width: '52px',
              height: '52px',
              borderRadius: '16px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <Sparkles size={24} />
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {searchQuery || selectedSubjectFilter !== 'all' ? 'No matching tasks found' : 'No tasks yet.'}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
              {searchQuery || selectedSubjectFilter !== 'all' 
                ? 'Try adjusting your search query or subject filter.' 
                : 'Start by adding your first task or study session.'}
            </div>
          </div>
          {searchQuery || selectedSubjectFilter !== 'all' ? (
            <button
              type="button"
              onClick={() => {
                setSearchQuery('');
                setSelectedSubjectFilter('all');
                setActiveStatusFilter('all');
              }}
              className="btn-press"
              style={{
                background: 'var(--sarah-surface-container-high)',
                border: 'none',
                borderRadius: '10px',
                padding: '7px 14px',
                fontSize: '12px',
                fontWeight: 600,
                color: 'var(--sarah-on-background)',
                cursor: 'pointer'
              }}
            >
              Reset Filters
            </button>
          ) : (
            <button
              type="button"
              onClick={() => openCreateTaskModal()}
              className="btn-press"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                background: 'var(--sarah-primary)',
                color: 'var(--sarah-on-primary)',
                border: 'none',
                borderRadius: '12px',
                padding: '8px 16px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer'
              }}
            >
              <Plus size={15} />
              <span>Create Task</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
};
