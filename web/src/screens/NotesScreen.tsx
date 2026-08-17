import React from 'react';
import { 
  FileText, 
  Plus, 
  Sparkles, 
  Search
} from 'lucide-react';

export const NotesScreen: React.FC = () => {
  const notes = [
    {
      id: 1,
      title: 'ML Lecture 8 — Backpropagation & Gradient Descent',
      snippet: 'Key equations for chain rule application across multi-layer perceptrons. Assignment 3 due Friday.',
      date: 'Today, 2:15 PM',
      subject: 'Machine Learning',
      color: '#5E6AD2',
      extractedTasks: 2
    },
    {
      id: 2,
      title: 'OS Process Synchronization & Semaphores',
      snippet: 'Dining philosophers problem, mutex locks vs binary semaphores, deadlock condition prevention.',
      date: 'Yesterday',
      subject: 'Operating Systems',
      color: '#10B981',
      extractedTasks: 1
    },
    {
      id: 3,
      title: 'Algorithms — Dijkstra & Bellman-Ford comparisons',
      snippet: 'Shortest path complexities, negative edge cycles handling with Bellman-Ford.',
      date: 'Aug 14',
      subject: 'Algorithms',
      color: '#F59E0B',
      extractedTasks: 0
    }
  ];

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 80px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0 }}>
            Lecture Notes
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
            Automatic task extraction & study summaries
          </p>
        </div>
        <button
          className="btn-press"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            background: 'var(--sarah-primary)',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '10px',
            padding: '7px 12px',
            fontSize: '12px',
            fontWeight: 600,
            cursor: 'pointer'
          }}
        >
          <Plus size={14} />
          <span>Capture</span>
        </button>
      </div>

      {/* Search */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: 'var(--sarah-surface-container-lowest)',
          border: '1px solid var(--sarah-outline-variant)',
          borderRadius: '12px',
          padding: '8px 12px'
        }}
      >
        <Search size={16} color="var(--sarah-secondary)" />
        <input
          type="text"
          placeholder="Search lecture notes or extracted tasks..."
          style={{
            border: 'none',
            outline: 'none',
            background: 'none',
            fontSize: '13px',
            width: '100%',
            color: 'var(--sarah-on-background)'
          }}
        />
      </div>

      {/* Quick AI extraction note card */}
      <div
        className="glass-card"
        style={{
          padding: '14px 16px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          background: 'rgba(255, 255, 255, 0.9)'
        }}
      >
        <div
          style={{
            width: '36px',
            height: '36px',
            borderRadius: '10px',
            backgroundColor: 'rgba(68, 80, 183, 0.1)',
            color: 'var(--sarah-primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}
        >
          <Sparkles size={18} />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
            Instant AI Extraction
          </div>
          <div style={{ fontSize: '11px', color: 'var(--sarah-on-surface-variant)' }}>
            Paste lecture notes, syllabi or transcripts to auto-generate tasks.
          </div>
        </div>
      </div>

      {/* Notes List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {notes.map((n) => (
          <div
            key={n.id}
            className="surface-card btn-press"
            style={{
              padding: '14px 16px',
              display: 'flex',
              flexDirection: 'column',
              gap: '8px',
              cursor: 'pointer'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: n.color }} />
                <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--sarah-secondary)' }}>
                  {n.subject}
                </span>
              </div>
              <span style={{ fontSize: '11px', color: 'var(--sarah-secondary)' }}>
                {n.date}
              </span>
            </div>

            <div style={{ fontSize: '14px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {n.title}
            </div>

            <div style={{ fontSize: '12px', color: 'var(--sarah-on-surface-variant)', lineHeight: 1.4 }}>
              {n.snippet}
            </div>

            {n.extractedTasks > 0 && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: 'var(--sarah-primary)', fontWeight: 600, marginTop: '2px' }}>
                <FileText size={12} />
                <span>{n.extractedTasks} task{n.extractedTasks > 1 ? 's' : ''} extracted</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
