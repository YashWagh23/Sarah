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
  initializeAndTrackPersistence
} from './src/lib/db';

async function runTest() {
  console.log('=== Starting Sarah IndexedDB Comprehensive Persistence Verification (Milestone 5) ===\n');

  // ──────────────────────────────────────────────────────────────────────────
  // A. SUBJECTS STORAGE & CRUD (MILESTONE 5)
  // ──────────────────────────────────────────────────────────────────────────
  console.log('--- [1] SUBJECTS STORAGE & CRUD ---');
  const initialSubjects = await getSubjects();
  console.log(`✓ Fetched ${initialSubjects.length} initial subjects.`);
  if (initialSubjects.length === 0) throw new Error('Expected initial seed subjects');

  // 1. Add Subject
  const newSubject = await addSubject({
    name: 'Distributed Systems & Cloud',
    code: 'CS 405',
    color: '#06B6D4'
  });
  console.log(`✓ Added Subject: "${newSubject.name}" (${newSubject.code}, Color: ${newSubject.color})`);

  // 2. Retrieve Subject
  const fetchedSubject = await getSubject(newSubject.id);
  if (!fetchedSubject || fetchedSubject.name !== 'Distributed Systems & Cloud') {
    throw new Error('Failed to retrieve subject from IndexedDB');
  }
  console.log('✓ Successfully retrieved subject by ID');

  // 3. Update Subject
  const updatedSubject = await updateSubject({
    ...fetchedSubject,
    name: 'Distributed Systems & Cloud Computing',
    code: 'CS 405A',
    color: '#3B82F6'
  });
  if (updatedSubject.code !== 'CS 405A') {
    throw new Error('Subject update failed');
  }
  console.log(`✓ Updated Subject: "${updatedSubject.name}" (${updatedSubject.code})`);

  // ──────────────────────────────────────────────────────────────────────────
  // B. CROSS-ENTITY SUBJECT LINKING
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [2] CROSS-ENTITY SUBJECT LINKING ---');
  
  // Link Task to new Subject
  const linkedTask = await addTask({
    title: 'Implement Raft Leader Election in Go',
    description: 'Implement heartbeat RPCs and election timeouts.',
    subject: updatedSubject.name,
    deadline: '2026-08-25',
    deadlineTime: '23:59',
    priority: 'must',
    estimatedMinutes: 60,
    completed: false
  });
  console.log(`✓ Created Task linked to subject "${linkedTask.subject}" (ID: ${linkedTask.id})`);

  // Link Note to new Subject
  const linkedNote = await addNote({
    title: 'Raft Consensus Protocol Summary',
    content: 'Leader election, log replication, and safety guarantees.',
    subject: updatedSubject.name,
    pinned: true
  });
  console.log(`✓ Created Note linked to subject "${linkedNote.subject}" (ID: ${linkedNote.id})`);

  // Link Reminder to new Subject
  const linkedReminder = await addReminder({
    title: 'Submit Raft Design Document',
    message: 'Upload architectural flowcharts.',
    reminderAt: Date.now() + 3600000 * 3,
    subject: updatedSubject.name,
    completed: false,
    dismissed: false
  });
  console.log(`✓ Created Reminder linked to subject "${linkedReminder.subject}" (ID: ${linkedReminder.id})`);

  // ──────────────────────────────────────────────────────────────────────────
  // C. SAFE SUBJECT DELETION & FALLBACK
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [3] SAFE SUBJECT DELETION & FALLBACK INTEGRITY ---');
  console.log(`✓ Deleting subject "${updatedSubject.name}" (ID: ${updatedSubject.id})`);
  await deleteSubject(updatedSubject.id);

  const checkDeletedSubject = await getSubject(updatedSubject.id);
  if (checkDeletedSubject) {
    throw new Error('Deleted subject still present in store');
  }
  console.log('✓ Subject removed from store.');

  // Verify that linked task survived and gracefully fell back to 'General'
  const postDeleteTask = await getTask(linkedTask.id);
  if (!postDeleteTask || postDeleteTask.subject !== 'General') {
    throw new Error(`Linked task did not survive with 'General' fallback. Got: ${postDeleteTask?.subject}`);
  }
  console.log(`✓ Linked Task survived deletion: subject="${postDeleteTask.subject}" (ID: ${postDeleteTask.id})`);

  // Verify that linked note survived and gracefully fell back to 'General'
  const postDeleteNote = await getNote(linkedNote.id);
  if (!postDeleteNote || postDeleteNote.subject !== 'General') {
    throw new Error(`Linked note did not survive with 'General' fallback. Got: ${postDeleteNote?.subject}`);
  }
  console.log(`✓ Linked Note survived deletion: subject="${postDeleteNote.subject}" (ID: ${postDeleteNote.id})`);

  // Verify that linked reminder survived and gracefully fell back to 'General'
  const postDeleteReminder = await getReminder(linkedReminder.id);
  if (!postDeleteReminder || postDeleteReminder.subject !== 'General') {
    throw new Error(`Linked reminder did not survive with 'General' fallback. Got: ${postDeleteReminder?.subject}`);
  }
  console.log(`✓ Linked Reminder survived deletion: subject="${postDeleteReminder.subject}" (ID: ${postDeleteReminder.id})`);

  // ──────────────────────────────────────────────────────────────────────────
  // D. TASKS, NOTES & REMINDERS REGRESSION CRUD CHECKS
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [4] TASKS, NOTES & REMINDERS REGRESSION CHECKS ---');
  
  // Task completion
  await completeTask(postDeleteTask.id, true);
  const completedTask = await getTask(postDeleteTask.id);
  if (!completedTask?.completed) throw new Error('Task completion failed');
  console.log('✓ Task completion toggling verified.');

  // Note pin toggle
  await toggleNotePinned(postDeleteNote.id, false);
  const unpinnedNote = await getNote(postDeleteNote.id);
  if (unpinnedNote?.pinned) throw new Error('Note unpin failed');
  console.log('✓ Note pin toggling verified.');

  // Reminder snooze & dismiss
  const newReminderTime = Date.now() + 7200000;
  await snoozeReminder(postDeleteReminder.id, newReminderTime);
  await dismissReminder(postDeleteReminder.id);
  const snoozedDismissedReminder = await getReminder(postDeleteReminder.id);
  if (!snoozedDismissedReminder?.dismissed || snoozedDismissedReminder.reminderAt !== newReminderTime) {
    throw new Error('Reminder snooze & dismiss failed');
  }
  console.log('✓ Reminder snooze and dismissal verified.');

  // ──────────────────────────────────────────────────────────────────────────
  // E. SIMULATED RELOAD PERSISTENCE ACROSS ALL STORES
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [5] SIMULATED RELOAD & COMPREHENSIVE INTEGRITY ---');
  const reloadTasks = await getTasks();
  const reloadNotes = await getNotes();
  const reloadReminders = await getReminders();
  const reloadSubjects = await getSubjects();

  const session = await initializeAndTrackPersistence();
  console.log(`✓ Subjects in store: ${reloadSubjects.length}`);
  console.log(`✓ Tasks in store: ${reloadTasks.length}`);
  console.log(`✓ Notes in store: ${reloadNotes.length}`);
  console.log(`✓ Reminders in store: ${reloadReminders.length}`);
  console.log(`✓ Session status: Ready (Reload count: ${session.reloadCount})`);

  console.log('\n================================================================');
  console.log('✅ ALL INDEXEDDB SUBJECTS, TASKS, NOTES & REMINDERS TESTS PASSED!');
  console.log('================================================================\n');
}

runTest().catch((err) => {
  console.error('\n❌ Persistence verification test failed:', err);
  process.exit(1);
});
