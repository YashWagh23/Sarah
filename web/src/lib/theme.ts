import type { ThemePreference } from './db';

export type ResolvedTheme = 'light' | 'dark';

/**
 * The preference lives in IndexedDB with the rest of the profile, but that read is
 * async — so it is mirrored to localStorage and replayed by a tiny inline script in
 * index.html to paint the right theme before React mounts (no white flash on open).
 */
export const THEME_STORAGE_KEY = 'sarah_theme_preference';

const THEME_COLORS: Record<ResolvedTheme, string> = {
  light: '#F9F9FB',
  dark: '#0B0C0E'
};

export function prefersDark(): boolean {
  if (typeof window === 'undefined' || !window.matchMedia) return false;
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

export function resolveTheme(preference: ThemePreference): ResolvedTheme {
  if (preference === 'light' || preference === 'dark') return preference;
  return prefersDark() ? 'dark' : 'light';
}

export function applyTheme(preference: ThemePreference): ResolvedTheme {
  const resolved = resolveTheme(preference);

  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-theme', resolved);

    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute('content', THEME_COLORS[resolved]);
  }

  try {
    localStorage.setItem(THEME_STORAGE_KEY, preference);
  } catch {
    // Private browsing or blocked storage: the theme still applies for this session.
  }

  return resolved;
}

/** Calls back whenever the OS theme flips; only meaningful while following the system. */
export function watchSystemTheme(onChange: () => void): () => void {
  if (typeof window === 'undefined' || !window.matchMedia) return () => {};
  const query = window.matchMedia('(prefers-color-scheme: dark)');
  query.addEventListener('change', onChange);
  return () => query.removeEventListener('change', onChange);
}
