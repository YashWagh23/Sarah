import React, { useMemo, useState } from 'react';
import { Plus, Search, X } from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { useSubjects } from '../context/SubjectsContext';
import { TaskCard } from '../components/TaskCard';
import { EmptyState, SectionHead, Segmented } from '../components/ui';
import { taskDueMs } from '../lib/planner';
import { type Task } from '../lib/db';

type View = 'open' | 'done';

const GROUPS: Array<{ id: string; title: string; test: (days: number, overdue: boolean) => boolean }> = [
  { id: 'overdue', title: 'Overdue', test: (_d, overdue) => overdue },
  { id: 'today', title: 'Today', test: d => d <= 0 },
  { id: 'tomorrow', title: 'Tomorrow', test: d => d === 1 },
  { id: 'week', title: 'Next 7 days', test: d => d <= 7 },
  { id: 'later', title: 'Later', test: () => true }
];

function daysUntil(task: Task): number {
  const start = new Date();
  start.setHours(0, 0, 0, 0);
  const due = new Date(taskDueMs(task));
  due.setHours(0, 0, 0, 0);
  return Math.round((due.getTime() - start.getTime()) / 86400000);
}

export const TasksScreen: React.FC = () => {
  const { tasks, activeTasks, completedTasks, toggleTaskCompletion, openEditTaskModal, openCreateTaskModal } = useTasks();
  const { subjects, getSubjectColor } = useSubjects();

  const [view, setView] = useState<View>('open');
  const [subjectFilter, setSubjectFilter] = useState<string>('all');
  const [query, setQuery] = useState('');

  // Chips cover every subject a task is filed under, including free-typed ones
  // and the "General" fallback, which have no Subject record.
  const subjectChips = useMemo(() => {
    const byKey = new Map<string, { key: string; name: string; label: string; color: string; count: number }>();
    for (const sub of subjects) {
      byKey.set(sub.name.toLowerCase(), { key: sub.name.toLowerCase(), name: sub.name, label: sub.code || sub.name, color: sub.color, count: 0 });
    }
    for (const task of tasks) {
      const name = task.subject?.trim();
      if (!name) continue;
      const key = name.toLowerCase();
      const entry = byKey.get(key);
      if (entry) entry.count++;
      else byKey.set(key, { key, name, label: name, color: getSubjectColor(name), count: 1 });
    }
    return Array.from(byKey.values())
      .filter(e => e.count > 0 || subjectFilter.toLowerCase() === e.key)
      .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name));
  }, [subjects, tasks, getSubjectColor, subjectFilter]);

  const matches = (task: Task) => {
    if (subjectFilter !== 'all' && task.subject.toLowerCase() !== subjectFilter.toLowerCase()) return false;
    const q = query.trim().toLowerCase();
    if (!q) return true;
    return task.title.toLowerCase().includes(q)
      || task.subject.toLowerCase().includes(q)
      || Boolean(task.description?.toLowerCase().includes(q));
  };

  const openGroups = useMemo(() => {
    const now = Date.now();
    const sorted = activeTasks.filter(matches).sort((a, b) => taskDueMs(a) - taskDueMs(b));
    const buckets = GROUPS.map(g => ({ ...g, items: [] as Task[] }));
    for (const task of sorted) {
      const overdue = taskDueMs(task) < now;
      const days = daysUntil(task);
      buckets.find(b => b.test(days, overdue))!.items.push(task);
    }
    return buckets.filter(b => b.items.length > 0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTasks, subjectFilter, query]);

  const doneTasks = useMemo(
    () => completedTasks.filter(matches).sort((a, b) => (b.completedAt ?? b.updatedAt) - (a.completedAt ?? a.updatedAt)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [completedTasks, subjectFilter, query]
  );

  const isFiltered = Boolean(query.trim()) || subjectFilter !== 'all';
  const shownCount = view === 'open' ? openGroups.reduce((n, g) => n + g.items.length, 0) : doneTasks.length;

  return (
    <div className="screen">
      <header className="screen-head">
        <div>
          <h2 className="screen-title">Tasks</h2>
          <p className="screen-sub">{activeTasks.length} open, {completedTasks.length} done</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => openCreateTaskModal(subjectFilter !== 'all' ? subjectFilter : undefined)}>
          <Plus size={16} />
          New task
        </button>
      </header>

      <div className="stack-12">
        <label className="search">
          <Search size={17} />
          <span className="visually-hidden">Search tasks</span>
          <input type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search tasks" />
          {query && (
            <button type="button" className="icon-btn" style={{ width: 28, height: 28 }} aria-label="Clear search" onClick={() => setQuery('')}>
              <X size={15} />
            </button>
          )}
        </label>

        <Segmented<View>
          label="Show"
          value={view}
          onChange={setView}
          options={[
            { id: 'open', label: `Open  ${activeTasks.length}` },
            { id: 'done', label: `Done  ${completedTasks.length}` }
          ]}
        />

        {subjectChips.length > 1 && (
          <div className="chip-row" role="group" aria-label="Filter by subject">
            <button type="button" className="chip" aria-pressed={subjectFilter === 'all'} onClick={() => setSubjectFilter('all')}>
              All subjects
            </button>
            {subjectChips.map(chip => {
              const isOn = subjectFilter.toLowerCase() === chip.key;
              return (
                <button
                  key={chip.key}
                  type="button"
                  className="chip"
                  aria-pressed={isOn}
                  onClick={() => setSubjectFilter(isOn ? 'all' : chip.name)}
                >
                  <span className="swatch" style={{ background: chip.color }} />
                  {chip.label}
                  <span className="chip-count">{chip.count}</span>
                </button>
              );
            })}
          </div>
        )}
      </div>

      {view === 'open' && openGroups.map(group => (
        <section key={group.id} aria-labelledby={`g-${group.id}`} className="stack-8">
          <SectionHead id={`g-${group.id}`} title={group.title} count={group.items.length} />
          <div className="list">
            {group.items.map(task => (
              <TaskCard key={task.id} task={task} showDate={group.id !== 'today'} onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />
            ))}
          </div>
        </section>
      ))}

      {view === 'done' && doneTasks.length > 0 && (
        <div className="list">
          {doneTasks.map(task => (
            <TaskCard key={task.id} task={task} onToggle={toggleTaskCompletion} onEdit={openEditTaskModal} />
          ))}
        </div>
      )}

      {shownCount === 0 && (
        isFiltered ? (
          <EmptyState
            title="Nothing matches"
            body="Try another search or subject."
            action={(
              <button type="button" className="btn btn-secondary" onClick={() => { setQuery(''); setSubjectFilter('all'); }}>
                Clear filters
              </button>
            )}
          />
        ) : view === 'open' ? (
          <EmptyState
            title={completedTasks.length > 0 ? 'Nothing open' : 'No tasks yet'}
            body={completedTasks.length > 0
              ? 'Every task is done. Add the next one when it lands.'
              : 'Add an assignment with its deadline and a time estimate. Today will plan around it.'}
            action={(
              <button type="button" className="btn btn-primary" onClick={() => openCreateTaskModal()}>
                <Plus size={16} />
                New task
              </button>
            )}
          />
        ) : (
          <EmptyState title="Nothing done yet" body="Finished tasks collect here." />
        )
      )}
    </div>
  );
};
