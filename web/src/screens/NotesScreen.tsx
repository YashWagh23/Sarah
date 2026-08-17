import React from 'react';
import { 
  Plus, 
  Search, 
  Pin, 
  FileText, 
  X 
} from 'lucide-react';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { NoteCard } from '../components/NoteCard';

export const NotesScreen: React.FC = () => {
  const { 
    notes, 
    searchQuery, 
    setSearchQuery, 
    selectedSubject, 
    setSelectedSubject, 
    pinnedNotes, 
    unpinnedNotes, 
    openCreateNoteModal, 
    openEditNoteModal, 
    togglePin, 
    removeNote 
  } = useNotes();

  const { subjects, getSubjectColor } = useSubjects();

  const totalFilteredCount = pinnedNotes.length + unpinnedNotes.length;

  return (
    <div 
      className="animate-fade-in"
      style={{
        padding: '16px 18px 90px 18px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px'
      }}
    >
      {/* Top Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--sarah-on-background)', margin: 0, letterSpacing: '-0.02em' }}>
            Academic Notes
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--sarah-secondary)', margin: 0 }}>
            Keep important classroom instructions close.
          </p>
        </div>
        <button
          type="button"
          onClick={() => openCreateNoteModal(selectedSubject !== 'all' ? selectedSubject : undefined)}
          className="btn-press"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            background: 'var(--sarah-primary)',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '12px',
            padding: '8px 14px',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            boxShadow: '0 3px 10px rgba(68, 80, 183, 0.28)'
          }}
        >
          <Plus size={15} strokeWidth={2.5} />
          <span>New Note</span>
        </button>
      </div>

      {/* Real-time Search Bar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: '#FFFFFF',
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
          placeholder="Search by title, subject, or lecture content..."
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
              width: '20px',
              height: '20px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--sarah-secondary)'
            }}
          >
            <X size={12} />
          </button>
        )}
      </div>

      {/* Subject Filter Chips */}
      <div 
        style={{ 
          display: 'flex', 
          gap: '8px', 
          overflowX: 'auto', 
          paddingBottom: '2px' 
        }}
        className="scroll-container"
      >
        <button
          type="button"
          onClick={() => setSelectedSubject('all')}
          className="btn-press"
          style={{
            border: 'none',
            borderRadius: '16px',
            padding: '7px 14px',
            fontSize: '12px',
            fontWeight: selectedSubject === 'all' ? 700 : 500,
            cursor: 'pointer',
            whiteSpace: 'nowrap',
            backgroundColor: selectedSubject === 'all' ? 'var(--sarah-primary)' : '#FFFFFF',
            color: selectedSubject === 'all' ? '#FFFFFF' : 'var(--sarah-on-surface-variant)',
            boxShadow: selectedSubject === 'all' ? '0 2px 8px rgba(68, 80, 183, 0.25)' : '0 1px 3px rgba(0, 0, 0, 0.04)',
            transition: 'all 0.15s ease'
          }}
        >
          All ({notes.length})
        </button>

        {subjects.map((sub) => {
          const isSelected = selectedSubject.toLowerCase() === sub.name.toLowerCase();
          const count = notes.filter(n => n.subject.toLowerCase() === sub.name.toLowerCase()).length;
          const dotColor = sub.color || getSubjectColor(sub.name);
          return (
            <button
              key={sub.id}
              type="button"
              onClick={() => setSelectedSubject(sub.name)}
              className="btn-press"
              style={{
                border: 'none',
                borderRadius: '16px',
                padding: '7px 14px',
                fontSize: '12px',
                fontWeight: isSelected ? 700 : 500,
                cursor: 'pointer',
                whiteSpace: 'nowrap',
                backgroundColor: isSelected ? 'var(--sarah-primary)' : '#FFFFFF',
                color: isSelected ? '#FFFFFF' : 'var(--sarah-on-surface-variant)',
                boxShadow: isSelected ? '0 2px 8px rgba(68, 80, 183, 0.25)' : '0 1px 3px rgba(0, 0, 0, 0.04)',
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                transition: 'all 0.15s ease'
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: isSelected ? '#FFFFFF' : dotColor }} />
              <span>{sub.name} {count > 0 ? `(${count})` : ''}</span>
            </button>
          );
        })}
      </div>

      {/* 1. Pinned Notes Section */}
      {pinnedNotes.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '0 2px' }}>
            <Pin size={13} fill="var(--sarah-tertiary)" color="var(--sarah-tertiary)" />
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                letterSpacing: '0.06em',
                textTransform: 'uppercase',
                color: 'var(--sarah-tertiary)'
              }}
            >
              Pinned Notes ({pinnedNotes.length})
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {pinnedNotes.map((note) => (
              <NoteCard
                key={note.id}
                note={note}
                onEdit={openEditNoteModal}
                onTogglePin={togglePin}
                onDelete={removeNote}
              />
            ))}
          </div>
        </section>
      )}

      {/* 2. All / Other Notes Section */}
      {unpinnedNotes.length > 0 && (
        <section style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: pinnedNotes.length > 0 ? '6px' : '0' }}>
          {pinnedNotes.length > 0 && (
            <div style={{ padding: '0 2px' }}>
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: 700,
                  letterSpacing: '0.06em',
                  textTransform: 'uppercase',
                  color: 'var(--sarah-secondary)'
                }}
              >
                All Notes ({unpinnedNotes.length})
              </span>
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {unpinnedNotes.map((note) => (
              <NoteCard
                key={note.id}
                note={note}
                onEdit={openEditNoteModal}
                onTogglePin={togglePin}
                onDelete={removeNote}
              />
            ))}
          </div>
        </section>
      )}

      {/* Empty State when no matching notes */}
      {totalFilteredCount === 0 && (
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
              color: 'var(--sarah-secondary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <FileText size={24} />
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--sarah-on-background)' }}>
              {searchQuery || selectedSubject !== 'all' ? 'No matching notes found' : 'No notes yet.'}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--sarah-secondary)', marginTop: '2px' }}>
              {searchQuery || selectedSubject !== 'all' 
                ? 'Try adjusting your search query or subject filter.' 
                : 'Capture classroom lecture notes, formulas, or reminders.'}
            </div>
          </div>
          {searchQuery || selectedSubject !== 'all' ? (
            <button
              type="button"
              onClick={() => {
                setSearchQuery('');
                setSelectedSubject('all');
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
              onClick={() => openCreateNoteModal()}
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
                cursor: 'pointer'
              }}
            >
              <Plus size={15} />
              <span>New Note</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
};
