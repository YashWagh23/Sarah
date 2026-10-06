import React from 'react';
import { Plus } from 'lucide-react';
import { useSubjects } from '../context/SubjectsContext';
import { FALLBACK_SUBJECT } from '../lib/db';
import { FieldGroup } from './ui';

interface SubjectPickerProps {
  value: string;
  isCustom: boolean;
  customValue: string;
  onPick: (name: string) => void;
  onCustom: () => void;
  onCustomChange: (value: string) => void;
}

/** Subject chips plus a "new subject" option with an inline name field. */
export const SubjectPicker: React.FC<SubjectPickerProps> = ({ value, isCustom, customValue, onPick, onCustom, onCustomChange }) => {
  const { subjects, getSubjectColor } = useSubjects();
  const options = subjects.length > 0
    ? subjects.map(s => ({ name: s.name, color: s.color }))
    : [{ name: FALLBACK_SUBJECT, color: getSubjectColor(FALLBACK_SUBJECT) }];
  // A record filed under a subject with no Subject entry still shows it selected.
  if (value && !options.some(o => o.name.toLowerCase() === value.toLowerCase())) {
    options.unshift({ name: value, color: getSubjectColor(value) });
  }

  return (
    <FieldGroup label="Subject">
      <div className="row" style={{ flexWrap: 'wrap', gap: 8 }}>
        {options.map(opt => (
          <button
            key={opt.name}
            type="button"
            className="chip"
            aria-pressed={!isCustom && value.toLowerCase() === opt.name.toLowerCase()}
            onClick={() => onPick(opt.name)}
          >
            <span className="swatch" style={{ background: opt.color }} />
            {opt.name}
          </button>
        ))}
        <button type="button" className="chip" aria-pressed={isCustom} onClick={onCustom} style={{ borderStyle: isCustom ? 'solid' : 'dashed' }}>
          <Plus size={14} />
          New subject
        </button>
      </div>
      {isCustom && (
        <input
          className="input"
          aria-label="New subject name"
          value={customValue}
          onChange={(e) => onCustomChange(e.target.value)}
          placeholder="Subject name, e.g. Operating Systems"
          autoFocus
        />
      )}
    </FieldGroup>
  );
};
