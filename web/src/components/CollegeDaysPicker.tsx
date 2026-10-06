import React from 'react';

// Monday-first, as a college timetable reads; values are JS weekday numbers.
const DAYS: Array<{ value: number; short: string; long: string }> = [
  { value: 1, short: 'Mon', long: 'Monday' },
  { value: 2, short: 'Tue', long: 'Tuesday' },
  { value: 3, short: 'Wed', long: 'Wednesday' },
  { value: 4, short: 'Thu', long: 'Thursday' },
  { value: 5, short: 'Fri', long: 'Friday' },
  { value: 6, short: 'Sat', long: 'Saturday' },
  { value: 0, short: 'Sun', long: 'Sunday' }
];

interface CollegeDaysPickerProps {
  value: number[];
  onChange: (days: number[]) => void;
}

export const CollegeDaysPicker: React.FC<CollegeDaysPickerProps> = ({ value, onChange }) => (
  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6 }}>
    {DAYS.map(day => {
      const isOn = value.includes(day.value);
      return (
        <button
          key={day.value}
          type="button"
          className="chip"
          aria-pressed={isOn}
          aria-label={day.long}
          onClick={() => onChange(isOn ? value.filter(d => d !== day.value) : [...value, day.value].sort((a, b) => a - b))}
          style={{ justifyContent: 'center', padding: 0, fontSize: 12.5 }}
        >
          {day.short.slice(0, 2)}
        </button>
      );
    })}
  </div>
);

export function describeCollegeDays(days: number[] | undefined): string {
  const set = new Set(days ?? []);
  if (set.size === 0) return 'None';
  const key = [1, 2, 3, 4, 5, 6, 0].map(d => (set.has(d) ? '1' : '0')).join('');
  if (key === '1111100') return 'Mon-Fri';
  if (key === '1111110') return 'Mon-Sat';
  if (key === '1111111') return 'Every day';
  return DAYS.filter(d => set.has(d.value)).map(d => d.short).join(', ');
}
