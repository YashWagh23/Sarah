import React, { useMemo } from 'react';
import { Plus, Search, X } from 'lucide-react';
import { useNotes } from '../context/NotesContext';
import { useSubjects } from '../context/SubjectsContext';
import { NoteCard } from '../components/NoteCard';
import { EmptyState, SectionHead } from '../components/ui';

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
    togglePin
  } = useNotes();
  const { subjects, getSubjectColor } = useSubjects();

  const totalShown = pinnedNotes.length + unpinnedNotes.length;
  const isFiltered = Boolean(searchQuery.trim()) || selectedSubject !== 'all';

  // Every subject notes are filed under, including free-typed ones and "General".
  const subjectChips = useMemo(() => {
    const byKey = new Map<string, { key: string; name: string; color: string; count: number }>();
    for (const sub of subjects) {
      byKey.set(sub.name.toLowerCase(), { key: sub.name.toLowerCase(), name: sub.name, color: sub.color, count: 0 });
    }
    for (const note of notes) {
      const name = note.subject?.trim();
      if (!name) continue;
      const key = name.toLowerCase();
      const entry = byKey.get(key);
      if (entry) entry.count++;
      else byKey.set(key, { key, name, color: getSubjectColor(name), count: 1 });
    }
    return Array.from(byKey.values())
      .filter(e => e.count > 0 || selectedSubject.toLowerCase() === e.key)
      .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name));
  }, [subjects, notes, getSubjectColor, selectedSubject]);

  return (
    <div className="screen">
      <header className="screen-head">
        <div>
          <h2 className="screen-title">Notes</h2>
          <p className="screen-sub">{notes.length} {notes.length === 1 ? 'note' : 'notes'} from class</p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => openCreateNoteModal(selectedSubject !== 'all' ? selectedSubject : undefined)}
        >
          <Plus size={16} />
          New note
        </button>
      </header>

      {notes.length > 0 && (
        <div className="stack-12">
          <label className="search">
            <Search size={17} />
            <span className="visually-hidden">Search notes</span>
            <input
              type="search"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search titles and content"
            />
            {searchQuery && (
              <button type="button" className="icon-btn" style={{ width: 28, height: 28 }} aria-label="Clear search" onClick={() => setSearchQuery('')}>
                <X size={15} />
              </button>
            )}
          </label>

          {subjectChips.length > 1 && (
            <div className="chip-row" role="group" aria-label="Filter by subject">
              <button type="button" className="chip" aria-pressed={selectedSubject === 'all'} onClick={() => setSelectedSubject('all')}>
                All
              </button>
              {subjectChips.map(chip => {
                const isOn = selectedSubject.toLowerCase() === chip.key;
                return (
                  <button
                    key={chip.key}
                    type="button"
                    className="chip"
                    aria-pressed={isOn}
                    onClick={() => setSelectedSubject(isOn ? 'all' : chip.name)}
                  >
                    <span className="swatch" style={{ background: chip.color }} />
                    {chip.name}
                    <span className="chip-count">{chip.count}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      )}

      {pinnedNotes.length > 0 && (
        <section aria-labelledby="pinned-notes" className="stack-8">
          <SectionHead id="pinned-notes" title="Pinned" count={pinnedNotes.length} />
          <div className="stack-8">
            {pinnedNotes.map(note => (
              <NoteCard key={note.id} note={note} onEdit={openEditNoteModal} onTogglePin={togglePin} />
            ))}
          </div>
        </section>
      )}

      {unpinnedNotes.length > 0 && (
        <section aria-labelledby="all-notes" className="stack-8">
          {pinnedNotes.length > 0 && <SectionHead id="all-notes" title="Everything else" count={unpinnedNotes.length} />}
          <div className="stack-8">
            {unpinnedNotes.map(note => (
              <NoteCard key={note.id} note={note} onEdit={openEditNoteModal} onTogglePin={togglePin} />
            ))}
          </div>
        </section>
      )}

      {totalShown === 0 && (
        isFiltered ? (
          <EmptyState
            title="Nothing matches"
            body="Try another search or subject."
            action={(
              <button type="button" className="btn btn-secondary" onClick={() => { setSearchQuery(''); setSelectedSubject('all'); }}>
                Clear filters
              </button>
            )}
          />
        ) : (
          <EmptyState
            title="No notes yet"
            body="Capture what the professor said, a formula, or what is on the exam. Pin the ones you need on Today."
            action={(
              <button type="button" className="btn btn-primary" onClick={() => openCreateNoteModal()}>
                <Plus size={16} />
                New note
              </button>
            )}
          />
        )
      )}
    </div>
  );
};
