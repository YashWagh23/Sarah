import React from 'react';

// Monday-first, as a college timetable reads; values are JS weekday numbers.
const DAYS: Array<{ value: number; short: string; long: string }> = [
  { value: 1, short: 'M', long: 'Monday' },
  { value: 2, short: 'T', long: 'Tuesday' },
  { value: 3, short: 'W', long: 'Wednesday' },
  { value: 4, short: 'T', long: 'Thursday' },
  { value: 5, short: 'F', long: 'Friday' },
  { value: 6, short: 'S', long: 'Saturday' },
  { value: 0, short: 'S', long: 'Sunday' }
];

interface CollegeDaysPickerProps {
  value: number[];
  onChange: (days: number[]) => void;
}

export const CollegeDaysPicker: React.FC<CollegeDaysPickerProps> = ({ value, onChange }) => (
  <div role="group" aria-label="Days with classes" style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '6px' }}>
    {DAYS.map(day => {
      const isOn = value.includes(day.value);
      return (
        <button
          key={day.value}
          type="button"
          aria-pressed={isOn}
          aria-label={day.long}
          title={day.long}
          onClick={() => onChange(isOn ? value.filter(d => d !== day.value) : [...value, day.value].sort())}
          className="btn-press"
          style={{
            height: '36px',
            borderRadius: '10px',
            border: isOn ? '1.5px solid var(--sarah-primary)' : '1px solid var(--sarah-outline-variant)',
            backgroundColor: isOn ? 'rgba(var(--sarah-primary-rgb), 0.12)' : 'transparent',
            color: isOn ? 'var(--sarah-primary)' : 'var(--sarah-secondary)',
            fontSize: '12.5px',
            fontWeight: isOn ? 700 : 500,
            cursor: 'pointer'
          }}
        >
          {day.short}
        </button>
      );
    })}
  </div>
);

export function describeCollegeDays(days: number[] | undefined): string {
  const set = new Set(days ?? []);
  if (set.size === 0) return 'None';
  const key = [1, 2, 3, 4, 5, 6, 0].map(d => (set.has(d) ? '1' : '0')).join('');
  if (key === '1111100') return 'Mon–Fri';
  if (key === '1111110') return 'Mon–Sat';
  if (key === '1111111') return 'Every day';
  return DAYS.filter(d => set.has(d.value)).map(d => d.long.slice(0, 3)).join(', ');
}
