import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { 
  getUserProfile, 
  saveUserProfile, 
  DEFAULT_USER_PROFILE, 
  type UserProfile, 
  type EnergyLevel 
} from '../lib/db';
import { useTasks } from './TasksContext';

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

interface UserProfileContextType {
  profile: UserProfile;
  isLoading: boolean;
  updateProfile: (updates: Partial<UserProfile>) => Promise<UserProfile>;
  setEnergyLevel: (energy: EnergyLevel) => Promise<void>;
  feasibility: FeasibilityAssessment;
  refreshProfile: () => Promise<void>;
}

const UserProfileContext = createContext<UserProfileContextType | undefined>(undefined);

export const UserProfileProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<UserProfile>(DEFAULT_USER_PROFILE);
  const [isLoading, setIsLoading] = useState(true);
  const { tasks, showToast } = useTasks();

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
    await saveUserProfile(updated);
    setProfile(updated);
    const labels: Record<EnergyLevel, string> = {
      high: '⚡ High Focus (Pace accelerated)',
      normal: '🎯 Steady Pace (Standard capacity)',
      low: '🔋 Low Battery (Light study mode)',
      exhausted: '🛋️ Exhausted (Prioritizing rest)'
    };
    showToast(labels[energy]);
  }, [profile, showToast]);

  // Real-Time Feasibility Calculation Engine
  const feasibility = useMemo((): FeasibilityAssessment => {
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    // Parse target bedtime
    const [bedHour, bedMin] = (profile.targetBedtime || '23:30').split(':').map(Number);
    const bedtimeMinutes = (isNaN(bedHour) ? 23 : bedHour) * 60 + (isNaN(bedMin) ? 30 : bedMin);

    // Compute raw available minutes before sleep
    let minutesUntilSleep = bedtimeMinutes - currentMinutes;
    if (minutesUntilSleep < 0) {
      // Past bedtime
      minutesUntilSleep = 0;
    }

    // Commute and college end buffer if student is still at college
    const [colEndHour, colEndMin] = (profile.collegeEndTime || '17:00').split(':').map(Number);
    const collegeEndTotal = (isNaN(colEndHour) ? 17 : colEndHour) * 60 + (isNaN(colEndMin) ? 0 : colEndMin) + (profile.commuteMinutes || 0);

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

    const formatMins = (mins: number) => {
      const h = Math.floor(mins / 60);
      const m = mins % 60;
      if (h > 0 && m > 0) return `${h}h ${m}m`;
      if (h > 0) return `${h}h`;
      return `${m}m`;
    };

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
  }, [profile, tasks]);

  const value = useMemo(() => ({
    profile,
    isLoading,
    updateProfile,
    setEnergyLevel,
    feasibility,
    refreshProfile
  }), [profile, isLoading, updateProfile, setEnergyLevel, feasibility, refreshProfile]);

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
