import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { 
  getUserProfile, 
  saveUserProfile, 
  DEFAULT_USER_PROFILE, 
  type UserProfile, 
  type EnergyLevel,
  type ThemePreference
} from '../lib/db';
import { useTasks } from './TasksContext';
import { formatMinutes, parseTimeToMinutes, startOfTodayMs, useNow } from '../lib/datetime';
import { applyTheme, resolveTheme, watchSystemTheme, type ResolvedTheme } from '../lib/theme';

export interface FeasibilityAssessment {
  rawMinutesAvailable: number;
  energyMultiplier: number;
  realisticCapacityMinutes: number;
  totalPendingMinutes: number;
  mustDoMinutes: number;
  status: 'optimal' | 'tight' | 'overloaded' | 'rest_recommended';
  headline: string;
  subtext: string;
}

export interface StudyGoalProgress {
  goalMinutes: number;
  completedMinutes: number;
  percent: number;
  isMet: boolean;
}

interface UserProfileContextType {
  profile: UserProfile;
  isLoading: boolean;
  updateProfile: (updates: Partial<UserProfile>) => Promise<UserProfile>;
  setEnergyLevel: (energy: EnergyLevel) => Promise<void>;
  setTheme: (theme: ThemePreference) => Promise<void>;
  resolvedTheme: ResolvedTheme;
  feasibility: FeasibilityAssessment;
  studyGoal: StudyGoalProgress;
  refreshProfile: () => Promise<void>;
}

const UserProfileContext = createContext<UserProfileContextType | undefined>(undefined);

export const UserProfileProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<UserProfile>(DEFAULT_USER_PROFILE);
  const [isLoading, setIsLoading] = useState(true);
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(() => resolveTheme(DEFAULT_USER_PROFILE.theme));
  const { tasks, showToast } = useTasks();
  const now = useNow(30000);

  const refreshProfile = useCallback(async () => {
    try {
      const data = await getUserProfile();
      setProfile(data);
    } catch (err) {
      console.error('Failed to load user profile from IndexedDB:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshProfile();
  }, [refreshProfile]);

  const updateProfile = useCallback(async (updates: Partial<UserProfile>) => {
    const updated = {
      ...profile,
      ...updates
    };
    await saveUserProfile(updated);
    setProfile(updated);
    showToast('Profile preferences updated');
    return updated;
  }, [profile, showToast]);

  const setEnergyLevel = useCallback(async (energy: EnergyLevel) => {
    const updated = {
      ...profile,
      energyLevel: energy
    };
    // Instant zero-lag optimistic UI update
    setProfile(updated);
    const labels: Record<EnergyLevel, string> = {
      high: '⚡ High Focus (Pace accelerated)',
      normal: '🎯 Steady Pace (Standard capacity)',
      low: '🔋 Low Battery (Light study mode)',
      exhausted: '🛋️ Exhausted (Prioritizing rest)'
    };
    showToast(labels[energy]);
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

  // Real-Time Feasibility Calculation Engine
  const feasibility = useMemo((): FeasibilityAssessment => {
    const currentDate = new Date(now);
    const currentMinutes = currentDate.getHours() * 60 + currentDate.getMinutes();

    const bedtimeMinutes = parseTimeToMinutes(profile.targetBedtime, 23 * 60 + 30);

    // Raw minutes left before sleep. A bedtime past midnight (e.g. 00:30) reads as
    // a small number, so it is rolled into the next day instead of collapsing to 0.
    let minutesUntilSleep = bedtimeMinutes - currentMinutes;
    if (bedtimeMinutes < 6 * 60 && currentMinutes > 12 * 60) {
      minutesUntilSleep = bedtimeMinutes + 24 * 60 - currentMinutes;
    }
    if (minutesUntilSleep < 0) {
      minutesUntilSleep = 0;
    }

    // Commute and college end buffer if student is still at college
    const collegeEndTotal = parseTimeToMinutes(profile.collegeEndTime, 17 * 60) + (profile.commuteMinutes || 0);

    const remainingCollegeCommute = currentMinutes < collegeEndTotal ? Math.max(0, collegeEndTotal - currentMinutes) : 0;
    const dinnerBuffer = (currentMinutes < 20 * 60 && minutesUntilSleep > 120) ? 30 : 0;

    const rawMinutesAvailable = Math.max(0, minutesUntilSleep - remainingCollegeCommute - dinnerBuffer);

    // Energy multipliers
    const multipliers: Record<EnergyLevel, number> = {
      high: 1.25,
      normal: 1.0,
      low: 0.7,
      exhausted: 0.4
    };
    const energyMultiplier = multipliers[profile.energyLevel] || 1.0;
    const realisticCapacityMinutes = Math.round(rawMinutesAvailable * energyMultiplier);

    // Pending tasks workload
    const activeTasks = tasks.filter(t => !t.completed);
    const mustTasks = activeTasks.filter(t => t.priority === 'must');
    const totalPendingMinutes = activeTasks.reduce((acc, t) => acc + (t.estimatedMinutes || 30), 0);
    const mustDoMinutes = mustTasks.reduce((acc, t) => acc + (t.estimatedMinutes || 30), 0);

    // Status evaluation
    let status: FeasibilityAssessment['status'] = 'optimal';
    let headline = '';
    let subtext = '';

    const formatMins = formatMinutes;

    if (profile.energyLevel === 'exhausted') {
      status = 'rest_recommended';
      headline = 'Protect your energy tonight 🛋️';
      subtext = `You have ~${formatMins(realisticCapacityMinutes)} focus capacity before ${profile.targetBedtime}. Defer non-urgent tasks.`;
    } else if (minutesUntilSleep <= 30 && activeTasks.length > 0) {
      status = 'tight';
      headline = 'Bedtime approaching 🌙';
      subtext = `Wrap up your notes and wind down for ${profile.targetBedtime}.`;
    } else if (realisticCapacityMinutes >= totalPendingMinutes) {
      status = 'optimal';
      headline = `Workload is fully achievable tonight ✨`;
      subtext = `~${formatMins(realisticCapacityMinutes)} study capacity vs ${formatMins(totalPendingMinutes)} pending workload.`;
    } else if (realisticCapacityMinutes >= mustDoMinutes) {
      status = 'tight';
      headline = `Must-Do tasks fit within bedtime 🎯`;
      subtext = `~${formatMins(realisticCapacityMinutes)} capacity covers all Must-Do work (${formatMins(mustDoMinutes)}).`;
    } else {
      status = 'overloaded';
      const deficit = mustDoMinutes - realisticCapacityMinutes;
      headline = `Schedule is tight for tonight ⚠️`;
      subtext = `Must-Do tasks exceed bedtime by ~${formatMins(deficit)}. Consider scaling scope or tackling top priority first.`;
    }

    return {
      rawMinutesAvailable,
      energyMultiplier,
      realisticCapacityMinutes,
      totalPendingMinutes,
      mustDoMinutes,
      status,
      headline,
      subtext
    };
  }, [profile, tasks, now]);

  // Daily study goal — how much of today's target has actually been completed.
  const studyGoal = useMemo((): StudyGoalProgress => {
    const goalMinutes = Math.round((profile.dailyStudyGoalHours ?? 3) * 60);
    const dayStart = startOfTodayMs();
    const completedMinutes = tasks
      .filter(t => t.completed && t.updatedAt >= dayStart)
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
    updateProfile,
    setEnergyLevel,
    setTheme,
    resolvedTheme,
    feasibility,
    studyGoal,
    refreshProfile
  }), [profile, isLoading, updateProfile, setEnergyLevel, setTheme, resolvedTheme, feasibility, studyGoal, refreshProfile]);

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
