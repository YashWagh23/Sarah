import React from 'react';
import { 
  BookOpen, 
  Plus, 
  Clock 
} from 'lucide-react';

export const SubjectsScreen: React.FC = () => {
  const subjects = [
    {
      id: 1,
      name: 'Machine Learning',
      code: 'CS 401',
      color: '#5E6AD2',
      tasksCount: 3,
      credits: 4,
      notesCount: 8
    },
    {
      id: 2,
      name: 'Operating Systems',
      code: 'CS 302',
      color: '#10B981',
      tasksCount: 2,
      credits: 4,
      notesCount: 12
    },
    {
      id: 3,
      name: 'Algorithms & Data Structures',
      code: 'CS 204',
      color: '#F59E0B',
      tasksCount: 1,
      credits: 3,
      notesCount: 6
    },
    {
      id: 4,
      name: 'Computer Networks',
      code: 'CS 350',
      color: '#EC4899',
      tasksCount: 1,
      credits: 3,
      notesCount: 5
    },
    {
      id: 5,
      name: 'Database Management Systems',
      code: 'CS 310',
      color: '#8B5CF6',
      tasksCount: 0,
      credits: 3,
      notesCount: 9
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
            Academic Subjects
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
            Course load & subject color system
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
          <span>Add</span>
        </button>
      </div>

      {/* Grid of Subject Cards */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {subjects.map((sub) => (
          <div
            key={sub.id}
            className="surface-card btn-press"
            style={{
              padding: '16px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              cursor: 'pointer',
              borderLeft: `4px solid ${sub.color}`
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  color: sub.color,
                  backgroundColor: `${sub.color}15`,
                  padding: '2px 8px',
                  borderRadius: '6px'
                }}
              >
                {sub.code}
              </span>
              <span style={{ fontSize: '11px', color: 'var(--sarah-secondary)', fontWeight: 500 }}>
                {sub.credits} Credits
              </span>
            </div>

            <div style={{ fontSize: '16px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {sub.name}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', fontSize: '12px', color: 'var(--sarah-secondary)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Clock size={13} color="var(--sarah-secondary)" />
                <span>{sub.tasksCount} active tasks</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <BookOpen size={13} color="var(--sarah-secondary)" />
                <span>{sub.notesCount} notes</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
