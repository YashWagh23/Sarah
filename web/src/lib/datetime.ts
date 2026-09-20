import { useEffect, useState } from 'react';

/**
 * Local-time date helpers.
 *
 * Deadlines are stored as plain 'YYYY-MM-DD' strings produced by `<input type="date">`,
 * which is always the user's local calendar day. `toISOString()` converts to UTC first,
 * so it reports the wrong day for part of every day outside UTC — the whole app must
 * therefore go through these helpers instead.
 */
export function toLocalDateStr(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function todayStr(): string {
  return toLocalDateStr(new Date());
}

export function addDaysStr(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return toLocalDateStr(d);
}

export function tomorrowStr(): string {
  return addDaysStr(1);
}

export function toLocalTimeStr(date: Date): string {
  const h = String(date.getHours()).padStart(2, '0');
  const m = String(date.getMinutes()).padStart(2, '0');
  return `${h}:${m}`;
}

/** Start of the local calendar day, in epoch milliseconds. */
export function startOfTodayMs(): number {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

/** Parses 'HH:mm' into minutes past local midnight, falling back when malformed. */
export function parseTimeToMinutes(value: string | undefined, fallbackMinutes: number): number {
  if (!value) return fallbackMinutes;
  const [h, m] = value.split(':').map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return fallbackMinutes;
  return h * 60 + m;
}

/** '1h 30m' / '45m' — used across the feasibility copy. */
export function formatMinutes(mins: number): string {
  const safe = Math.max(0, Math.round(mins));
  const h = Math.floor(safe / 60);
  const m = safe % 60;
  if (h > 0 && m > 0) return `${h}h ${m}m`;
  if (h > 0) return `${h}h`;
  return `${m}m`;
}

/**
 * A clock that re-renders on an interval, so anything derived from "now"
 * (the current calendar day, remaining study capacity, overdue badges) keeps up
 * with a PWA session that stays open for hours or crosses midnight.
 */
export function useNow(intervalMs = 30000): number {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const tick = () => setNow(Date.now());
    const timer = setInterval(tick, intervalMs);

    // A backgrounded tab throttles timers, so re-sync the moment it returns.
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') tick();
    };
    document.addEventListener('visibilitychange', handleVisibility);
    window.addEventListener('focus', tick);

    return () => {
      clearInterval(timer);
      document.removeEventListener('visibilitychange', handleVisibility);
      window.removeEventListener('focus', tick);
    };
  }, [intervalMs]);

  return now;
}
