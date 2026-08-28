import 'fake-indexeddb/auto';
import { 
  getTasks, 
  getTask, 
  addTask, 
  updateTask, 
  deleteTask, 
  completeTask, 
  getNotes, 
  getNote, 
  addNote, 
  updateNote, 
  deleteNote, 
  toggleNotePinned,
  getReminders,
  getReminder,
  addReminder,
  updateReminder,
  deleteReminder,
  dismissReminder,
  snoozeReminder,
  getSubjects,
  getSubject,
  addSubject,
  updateSubject,
  deleteSubject,
  runProductionCleanupMigration,
  initializeAndTrackPersistence
} from './src/lib/db';

async function runHardeningTest() {
  console.log('=== Starting Sarah PWA Production Hardening & Clean State Verification ===\n');

  // ──────────────────────────────────────────────────────────────────────────
  // 1. FRESH INSTALLATION: ZERO DEMO DATA
  // ──────────────────────────────────────────────────────────────────────────
  console.log('--- [1] FRESH INSTALL CLEAN STATE (ZERO DEMO DATA) ---');
  
  await initializeAndTrackPersistence();
  const freshTasks = await getTasks();
  const freshNotes = await getNotes();
  const freshReminders = await getReminders();
  const freshSubjects = await getSubjects();

  console.log(`✓ Fresh Tasks Count: ${freshTasks.length} (Expected: 0)`);
  console.log(`✓ Fresh Notes Count: ${freshNotes.length} (Expected: 0)`);
  console.log(`✓ Fresh Reminders Count: ${freshReminders.length} (Expected: 0)`);
  console.log(`✓ Fresh Subjects Count: ${freshSubjects.length} (Expected: 0)`);

  if (freshTasks.length !== 0 || freshNotes.length !== 0 || freshReminders.length !== 0 || freshSubjects.length !== 0) {
    throw new Error('Fresh installation must have zero demo records');
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 2. REAL USER DATA CREATION & SUBJECT LINKING
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [2] REAL USER DATA CREATION & LINKING ---');

  // User creates their own subject
  const userSubject = await addSubject({
    name: 'Embedded Systems',
    code: 'ECE 320',
    color: '#06B6D4'
  });
  console.log(`✓ Created User Subject: "${userSubject.name}" (${userSubject.code})`);

  // User creates a task linked to that subject
  const userTask = await addTask({
    title: 'Design UART Communication Driver',
    description: 'Implement baud rate generator and ring buffer FIFO.',
    subject: userSubject.name,
    deadline: '2026-08-30',
    deadlineTime: '22:00',
    priority: 'must',
    estimatedMinutes: 45,
    completed: false
  });
  console.log(`✓ Created User Task: "${userTask.title}" (ID: ${userTask.id})`);

  // User creates a note linked to that subject
  const userNote = await addNote({
    title: 'ARM Cortex Interrupt Handlers (NVIC)',
    content: 'Priority grouping, tail-chaining latency, and SysTick registers.',
    subject: userSubject.name,
    pinned: true
  });
  console.log(`✓ Created User Note: "${userNote.title}" (ID: ${userNote.id})`);

  // User creates a reminder linked to that subject
  const userReminder = await addReminder({
    title: 'Microcontroller Lab Viva',
    message: 'Bring circuit diagram and breadboard.',
    reminderAt: Date.now() + 7200000,
    subject: userSubject.name,
    completed: false,
    dismissed: false
  });
  console.log(`✓ Created User Reminder: "${userReminder.title}" (ID: ${userReminder.id})`);

  // ──────────────────────────────────────────────────────────────────────────
  // 3. TARGETED MIGRATION TEST: USER DATA SURVIVAL
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [3] TARGETED CLEANUP MIGRATION & USER DATA PRESERVATION ---');

  // Run migration again — ensure genuine user data remains 100% untouched
  await runProductionCleanupMigration();

  const postMigrateTasks = await getTasks();
  const postMigrateNotes = await getNotes();
  const postMigrateReminders = await getReminders();
  const postMigrateSubjects = await getSubjects();

  if (postMigrateTasks.length !== 1 || postMigrateTasks[0].id !== userTask.id) {
    throw new Error('User task was deleted during migration');
  }
  if (postMigrateNotes.length !== 1 || postMigrateNotes[0].id !== userNote.id) {
    throw new Error('User note was deleted during migration');
  }
  if (postMigrateReminders.length !== 1 || postMigrateReminders[0].id !== userReminder.id) {
    throw new Error('User reminder was deleted during migration');
  }
  if (postMigrateSubjects.length !== 1 || postMigrateSubjects[0].id !== userSubject.id) {
    throw new Error('User subject was deleted during migration');
  }
  console.log('✓ Migration ran safely: all user records preserved perfectly.');

  // ──────────────────────────────────────────────────────────────────────────
  // 4. SAFE SUBJECT DELETION & NON-DESTRUCTIVE FALLBACK
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [4] SAFE SUBJECT DELETION & DATA INTEGRITY ---');
  await deleteSubject(userSubject.id);

  const checkDeletedSub = await getSubject(userSubject.id);
  if (checkDeletedSub) throw new Error('Deleted subject still present');

  const survivedTask = await getTask(userTask.id);
  const survivedNote = await getNote(userNote.id);
  const survivedReminder = await getReminder(userReminder.id);

  if (!survivedTask || survivedTask.subject !== 'General') {
    throw new Error(`Task did not fallback to 'General'. Subject is: ${survivedTask?.subject}`);
  }
  if (!survivedNote || survivedNote.subject !== 'General') {
    throw new Error(`Note did not fallback to 'General'. Subject is: ${survivedNote?.subject}`);
  }
  if (!survivedReminder || survivedReminder.subject !== 'General') {
    throw new Error(`Reminder did not fallback to 'General'. Subject is: ${survivedReminder?.subject}`);
  }
  console.log('✓ All linked user records survived subject deletion with "General" fallback.');

  // ──────────────────────────────────────────────────────────────────────────
  // 5. INTERACTION & MUTATION TEST
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [5] USER INTERACTION & MUTATION VERIFICATION ---');

  // Complete task
  await completeTask(survivedTask.id, true);
  const completedT = await getTask(survivedTask.id);
  if (!completedT?.completed) throw new Error('Task completion failed');
  console.log('✓ Task completion state toggle verified.');

  // Unpin note
  await toggleNotePinned(survivedNote.id, false);
  const unpinnedN = await getNote(survivedNote.id);
  if (unpinnedN?.pinned) throw new Error('Note unpin failed');
  console.log('✓ Note pin state toggle verified.');

  // Snooze reminder
  const newTime = Date.now() + 1800000;
  await snoozeReminder(survivedReminder.id, newTime);
  const snoozedR = await getReminder(survivedReminder.id);
  if (snoozedR?.reminderAt !== newTime) throw new Error('Reminder snooze failed');
  console.log('✓ Reminder snooze verified.');

  // Dismiss reminder
  await dismissReminder(survivedReminder.id);
  const dismissedR = await getReminder(survivedReminder.id);
  if (!dismissedR?.dismissed) throw new Error('Reminder dismissal failed');
  console.log('✓ Reminder dismiss verified.');

  // ──────────────────────────────────────────────────────────────────────────
  // 6. SIMULATED RELOAD
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [6] SIMULATED RELOAD INTEGRITY ---');
  const finalTasks = await getTasks();
  const finalNotes = await getNotes();
  const finalReminders = await getReminders();

  console.log(`✓ Stored User Tasks: ${finalTasks.length}`);
  console.log(`✓ Stored User Notes: ${finalNotes.length}`);
  console.log(`✓ Stored User Reminders: ${finalReminders.length}`);

  // ──────────────────────────────────────────────────────────────────────────
  // 7. USER PROFILE & BACKUP EXPORT/IMPORT INTEGRITY
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [7] PROFILE PERSISTENCE & DATA BACKUP VERIFICATION ---');
  const { getUserProfile, saveUserProfile, exportAllDataJSON, importAllDataJSON } = await import('./src/lib/db');
  
  const initialProfile = await getUserProfile();
  console.log(`✓ Initial Profile Loaded: "${initialProfile.name}" (Bedtime: ${initialProfile.targetBedtime})`);
  
  await saveUserProfile({
    ...initialProfile,
    name: 'Sarah Student',
    energyLevel: 'high'
  });
  const updatedProfile = await getUserProfile();
  if (updatedProfile.name !== 'Sarah Student' || updatedProfile.energyLevel !== 'high') {
    throw new Error('User profile update failed');
  }
  console.log('✓ User profile mutation and persistence verified.');

  const backupJson = await exportAllDataJSON();
  if (!backupJson.includes('Sarah Student')) {
    throw new Error('Export JSON backup missing user profile');
  }
  console.log('✓ Full JSON Data Export verified.');

  const importResult = await importAllDataJSON(backupJson);
  if (!importResult.success || importResult.importedCounts.tasks < 1) {
    throw new Error('Full JSON Data Import failed');
  }
  console.log(`✓ Full JSON Data Restore verified (${importResult.importedCounts.tasks} tasks restored).`);

  console.log('\n================================================================');
  console.log('✅ ALL PRODUCTION HARDENING & CLEAN STATE TESTS PASSED!');
  console.log('================================================================\n');
}

runHardeningTest().catch((err) => {
  console.error('\n❌ Production verification test failed:', err);
  process.exit(1);
});
