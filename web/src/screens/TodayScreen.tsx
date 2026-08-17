import { 
  Sparkles, 
  Clock, 
  CheckCircle2, 
  Plus, 
  Flame, 
  BookOpen, 
  Coffee,
  Check
} from 'lucide-react';
import { useTasks } from '../context/TasksContext';
import { TaskCard } from '../components/TaskCard';
import { SUBJECT_COLORS } from '../lib/db';

export const TodayScreen: React.FC = () => {
  const { 
    activeTasks, 
    completedTasks, 
    mustDoTasks, 
    shouldDoTasks, 
    laterTasks, 
    nextActionTask, 
    toggleTaskCompletion, 
    openEditTaskModal, 
    openCreateTaskModal 
  } = useTasks();

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  const todayStr = new Date().toISOString().split('T')[0];
  const dueTodayCount = activeTasks.filter(t => t.deadline <= todayStr).length;
  const totalEstMinutes = activeTasks.reduce((acc, t) => acc + (t.estimatedMinutes || 0), 0);

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px'
      }}
    >
      {/* 1. Sarah Header & Greeting */}
      <section style={{ display: 'flex', flexDirection: 'column', gap: '3px', paddingTop: '4px' }}>
        <h2
          style={{
            fontSize: '25px',
            fontWeight: 800,
            color: 'var(--sarah-on-background)',
            letterSpacing: '-0.03em',
            margin: 0
          }}
        >
          {getGreeting()}, Yash
        </h2>
        <p
          style={{
            fontSize: '14px',
            color: 'var(--sarah-on-surface-variant)',
            margin: 0
          }}
        >
          Let's figure out tonight.
        </p>
      </section>

      {/* 2. Sarah's Next Move — Liquid Glass Card */}
      {nextActionTask ? (
        <section 
          className="glass-card btn-press"
          onClick={() => openEditTaskModal(nextActionTask)}
          style={{
            padding: '18px 20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '14px',
            position: 'relative',
            overflow: 'hidden',
            cursor: 'pointer'
          }}
        >
          {/* Ambient Radial Accent Glow */}
          <div 
            style={{
              position: 'absolute',
              top: '-25px',
              right: '-25px',
              width: '120px',
              height: '120px',
              borderRadius: '50%',
              background: 'radial-gradient(circle, rgba(94, 106, 210, 0.22) 0%, rgba(255,255,255,0) 70%)',
              pointerEvents: 'none'
            }}
          />

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '7px' }}>
              <Sparkles size={16} color="var(--sarah-primary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-primary)'
                }}
              >
                Sarah's Next Move
              </span>
            </div>

            {/* Urgency Badge */}
            {nextActionTask.deadline < todayStr ? (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 700,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(186, 26, 26, 0.12)',
                  color: 'var(--sarah-error)'
                }}
              >
                OVERDUE
              </span>
            ) : nextActionTask.priority === 'must' ? (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 700,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(186, 26, 26, 0.1)',
                  color: 'var(--sarah-error)'
                }}
              >
                MUST DO TONIGHT
              </span>
            ) : (
              <span
                style={{
                  fontSize: '10.5px',
                  fontWeight: 600,
                  padding: '3px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(68, 80, 183, 0.1)',
                  color: 'var(--sarah-primary)'
                }}
              >
                RECOMMENDED
              </span>
            )}
          </div>

          <div>
            <h3
              style={{
                fontSize: '18px',
                fontWeight: 700,
                color: 'var(--sarah-on-background)',
                letterSpacing: '-0.02em',
                marginBottom: '4px'
              }}
            >
              {nextActionTask.title}
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span
                style={{
                  width: '6px',
                  height: '6px',
                  borderRadius: '50%',
                  backgroundColor: SUBJECT_COLORS[nextActionTask.subject] || '#6366F1'
                }}
              />
              <span style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', fontWeight: 500 }}>
                {nextActionTask.subject}
              </span>
              {nextActionTask.description && (
                <>
                  <span style={{ fontSize: '10px', color: 'var(--sarah-outline)' }}>•</span>
                  <span style={{ fontSize: '12px', color: 'var(--sarah-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {nextActionTask.description}
                  </span>
                </>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '10px', paddingTop: '2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--sarah-secondary)', fontSize: '12px', fontWeight: 500 }}>
              <Clock size={14} />
              <span>{nextActionTask.estimatedMinutes} mins session</span>
            </div>

            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                toggleTaskCompletion(nextActionTask.id);
              }}
              className="btn-press"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                background: 'var(--sarah-primary)',
                color: '#FFFFFF',
                border: 'none',
                borderRadius: '12px',
                padding: '8px 16px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
                boxShadow: '0 4px 12px rgba(68, 80, 183, 0.3)'
              }}
            >
              <Check size={14} strokeWidth={2.5} />
              <span>Mark Done</span>
            </button>
          </div>
        </section>
      ) : (
        /* All caught up card */
        <section
          className="glass-card"
          style={{
            padding: '24px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '16px',
            backgroundColor: 'rgba(255, 255, 255, 0.88)'
          }}
        >
          <div
            style={{
              width: '46px',
              height: '46px',
              borderRadius: '14px',
              backgroundColor: 'rgba(16, 185, 129, 0.12)',
              color: '#059669',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0
            }}
          >
            <Coffee size={24} />
          </div>
          <div>
            <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              All Caught Up! 🌟
            </div>
            <div style={{ fontSize: '12.5px', color: 'var(--sarah-on-surface-variant)', marginTop: '2px' }}>
              No active tasks remaining tonight. Rest or capture a new one whenever ready.
            </div>
          </div>
        </section>
      )}

      {/* 3. Daily Summary Stats Bar */}
      <section
        className="surface-card"
        style={{
          padding: '12px 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          backgroundColor: '#FFFFFF'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: 'var(--sarah-primary)' }}>
            {dueTodayCount}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Due Today
          </span>
        </div>

        <div style={{ width: '1px', height: '26px', backgroundColor: 'var(--sarah-outline-variant)' }} />

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: '#059669' }}>
            {completedTasks.length}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Completed
          </span>
        </div>

        <div style={{ width: '1px', height: '26px', backgroundColor: 'var(--sarah-outline-variant)' }} />

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <span style={{ fontSize: '18px', fontWeight: 800, color: 'var(--sarah-on-background)' }}>
            {totalEstMinutes > 0 ? `${totalEstMinutes}m` : '0m'}
          </span>
          <span style={{ fontSize: '10.5px', fontWeight: 600, color: 'var(--sarah-secondary)', textTransform: 'uppercase' }}>
            Study Workload
          </span>
        </div>
      </section>

      {/* 4. Must Do Section */}
      {mustDoTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Flame size={14} color="var(--sarah-error)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-error)'
                }}
              >
                Must Do ({mustDoTasks.length})
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {mustDoTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* 5. Should Do Section */}
      {shouldDoTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <BookOpen size={14} color="var(--sarah-tertiary)" />
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-tertiary)'
                }}
              >
                Should Do ({shouldDoTasks.length})
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {shouldDoTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* 6. Later / Deferrable Section */}
      {laterTasks.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                letterSpacing: '0.08em',
                textTransform: 'uppercase',
                color: 'var(--sarah-secondary)'
              }}
            >
              Later ({laterTasks.length})
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {laterTasks.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                onToggle={toggleTaskCompletion}
                onEdit={openEditTaskModal}
              />
            ))}
          </div>
        </section>
      )}

      {/* Empty State when no active tasks */}
      {activeTasks.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: '30px 10px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px'
          }}
        >
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: '20px',
              backgroundColor: 'var(--sarah-surface-container-low)',
              color: 'var(--sarah-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <CheckCircle2 size={30} />
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              No Pending Tasks
            </div>
            <div style={{ fontSize: '12px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
              Tap below or use the floating + button to add an academic task.
            </div>
          </div>
          <button
            type="button"
            onClick={() => openCreateTaskModal()}
            className="btn-press"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'var(--sarah-primary)',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '12px',
              padding: '8px 16px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer',
              marginTop: '4px'
            }}
          >
            <Plus size={15} />
            <span>Add Task</span>
          </button>
        </div>
      )}
    </div>
  );
};
