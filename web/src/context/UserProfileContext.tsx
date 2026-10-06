import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { 
  getUserProfile, 
  hasStoredUserProfile,
  saveUserProfile, 
  DEFAULT_USER_PROFILE, 
  type UserProfile, 
  type EnergyLevel,
  type ThemePreference
} from '../lib/db';
import { useTasks } from './TasksContext';
import { startOfTodayMs, useNow } from '../lib/datetime';
import { applyTheme, resolveTheme, watchSystemTheme, type ResolvedTheme } from '../lib/theme';
import { buildTonightPlan, ENERGY_PROFILES, type TonightPlan } from '../lib/planner';

export interface StudyGoalProgress {
  goalMinutes: number;
  completedMinutes: number;
  percent: number;
  isMet: boolean;
}

interface UserProfileContextType {
  profile: UserProfile;
  isLoading: boolean;
  /** True on a fresh install until the welcome sheet saves a profile. */
  isFirstRun: boolean;
  updateProfile: (updates: Partial<UserProfile>, toast?: string | null) => Promise<UserProfile>;
  setEnergyLevel: (energy: EnergyLevel) => Promise<void>;
  setTheme: (theme: ThemePreference) => Promise<void>;
  resolvedTheme: ResolvedTheme;
  plan: TonightPlan;
  studyGoal: StudyGoalProgress;
  refreshProfile: () => Promise<void>;
}

const UserProfileContext = createContext<UserProfileContextType | undefined>(undefined);

export const UserProfileProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<UserProfile>(DEFAULT_USER_PROFILE);
  const [isLoading, setIsLoading] = useState(true);
  const [isFirstRun, setIsFirstRun] = useState(false);
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(() => resolveTheme(DEFAULT_USER_PROFILE.theme));
  const { tasks, showToast } = useTasks();
  const now = useNow(30000);

  const refreshProfile = useCallback(async () => {
    try {
      const [data, stored] = await Promise.all([getUserProfile(), hasStoredUserProfile()]);
      setProfile(data);
      setIsFirstRun(!stored);
    } catch (err) {
      console.error('Failed to load user profile from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  const updateProfile = useCallback(async (
    updates: Partial<UserProfile>,
    toast: string | null = 'Profile preferences updated'
  ) => {
    const updated = {
      ...profile,
      ...updates
    };
    await saveUserProfile(updated);
    setProfile(updated);
    setIsFirstRun(false);
    if (toast) showToast(toast);
    return updated;
  }, [profile, showToast]);

  const setEnergyLevel = useCallback(async (energy: EnergyLevel) => {
    const updated = {
      ...profile,
      energyLevel: energy
    };
    // Instant zero-lag optimistic UI update
    setProfile(updated);
    showToast(`Energy set to ${ENERGY_PROFILES[energy].label}. Plan updated.`);
    try {
      await saveUserProfile(updated);
    } catch (err) {
      console.error('Failed to save energy level:', err);
    }
  }, [profile, showToast]);

  const setTheme = useCallback(async (theme: ThemePreference) => {
    const updated = { ...profile, theme };
    // Paint first, persist after — the swap should feel instant.
    setResolvedTheme(applyTheme(theme));
    setProfile(updated);
    try {
      await saveUserProfile(updated);
    } catch (err) {
      console.error('Failed to save theme preference:', err);
    }
  }, [profile]);

  // Apply the stored preference once the profile has loaded from IndexedDB.
  useEffect(() => {
    setResolvedTheme(applyTheme(profile.theme || 'system'));
  }, [profile.theme]);

  // Follow the OS while the preference is 'system'.
  useEffect(() => {
    if (profile.theme !== 'system') return;
    return watchSystemTheme(() => setResolvedTheme(applyTheme('system')));
  }, [profile.theme]);

  // Tonight's plan — recomputed from the ticking clock, so an app left open
  // keeps re-planning as time passes, tasks change, or energy shifts.
  const plan = useMemo(() => buildTonightPlan(tasks, profile, now), [tasks, profile, now]);

  // Daily study goal — how much of today's target has actually been completed.
  const studyGoal = useMemo((): StudyGoalProgress => {
    const goalMinutes = Math.round((profile.dailyStudyGoalHours ?? 3) * 60);
    const dayStart = startOfTodayMs();
    const completedMinutes = tasks
      // completedAt, not updatedAt: editing a task finished yesterday must not
      // credit it to today. Older records without the field fall back.
      .filter(t => t.completed && (t.completedAt ?? t.updatedAt) >= dayStart)
      .reduce((acc, t) => acc + (t.estimatedMinutes || 0), 0);
    const percent = goalMinutes > 0
      ? Math.min(100, Math.round((completedMinutes / goalMinutes) * 100))
      : 0;
    return {
      goalMinutes,
      completedMinutes,
      percent,
      isMet: goalMinutes > 0 && completedMinutes >= goalMinutes
    };
  }, [profile.dailyStudyGoalHours, tasks, now]);

  const value = useMemo(() => ({
    profile,
    isLoading,
    isFirstRun,
    updateProfile,
    setEnergyLevel,
    setTheme,
    resolvedTheme,
    plan,
    studyGoal,
    refreshProfile
  }), [profile, isLoading, isFirstRun, updateProfile, setEnergyLevel, setTheme, resolvedTheme, plan, studyGoal, refreshProfile]);

  return (
    <UserProfileContext.Provider value={value}>
      {children}
    </UserProfileContext.Provider>
  );
};

export function useUserProfile(): UserProfileContextType {
  const context = useContext(UserProfileContext);
  if (!context) {
    throw new Error('useUserProfile must be used within a UserProfileProvider');
  }
  return context;
}
