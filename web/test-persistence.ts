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
  initializeAndTrackPersistence
} from './src/lib/db';

async function runTest() {
  console.log('=== Starting Sarah IndexedDB Comprehensive Persistence Verification ===\n');

  // ──────────────────────────────────────────────────────────────────────────
  // A. TASK VERIFICATION (REGRESSION CHECK)
  // ──────────────────────────────────────────────────────────────────────────
  console.log('--- [1] TASK STORAGE & CRUD ---');
  const initialTasks = await getTasks();
  console.log(`✓ Fetched ${initialTasks.length} initial tasks.`);
  if (initialTasks.length === 0) throw new Error('Expected initial seed tasks');

  const newTask = await addTask({
    title: 'Study Distributed Consensus & Paxos',
    description: 'Prepare notes on Raft vs Paxos leader election protocols.',
    subject: 'Computer Networks',
    deadline: '2026-08-20',
    deadlineTime: '22:00',
    priority: 'must',
    estimatedMinutes: 50,
    completed: false
  });
  console.log(`✓ Created Task: "${newTask.title}" (ID: ${newTask.id})`);

  const fetchedTask = await getTask(newTask.id);
  if (!fetchedTask || fetchedTask.title !== 'Study Distributed Consensus & Paxos') {
    throw new Error('Failed to retrieve task');
  }

  const updatedTask = await updateTask({
    ...fetchedTask,
    title: 'Study Distributed Consensus (Raft & Paxos)',
    estimatedMinutes: 60
  });
  if (updatedTask.estimatedMinutes !== 60) throw new Error('Task update failed');
  console.log(`✓ Updated Task: "${updatedTask.title}" (${updatedTask.estimatedMinutes}m)`);

  const completedTask = await completeTask(newTask.id, true);
  if (!completedTask || !completedTask.completed) throw new Error('completeTask failed');
  console.log(`✓ Completed Task: completed=${completedTask.completed}`);

  // ──────────────────────────────────────────────────────────────────────────
  // B. ACADEMIC NOTES VERIFICATION (REGRESSION CHECK)
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [2] ACADEMIC NOTES STORAGE & CRUD ---');
  const initialNotes = await getNotes();
  console.log(`✓ Fetched ${initialNotes.length} initial academic notes.`);
  if (initialNotes.length === 0) throw new Error('Expected initial seed notes');

  const newNote = await addNote({
    title: 'Computer Networks Lab — Socket Programming in C',
    content: 'Server flow: socket() -> bind() -> listen() -> accept() -> read()/write() -> close().',
    subject: 'Computer Networks',
    pinned: false
  });
  console.log(`✓ Added Note: "${newNote.title}" (Subject: ${newNote.subject})`);

  const fetchedNote = await getNote(newNote.id);
  if (!fetchedNote || fetchedNote.title !== 'Computer Networks Lab — Socket Programming in C') {
    throw new Error('Failed to retrieve note');
  }

  const updatedNote = await updateNote({
    ...fetchedNote,
    title: 'Computer Networks Lab — TCP Socket Programming in C',
    pinned: true
  });
  if (!updatedNote.pinned) throw new Error('Note update failed');
  console.log(`✓ Updated & Pinned Note: "${updatedNote.title}" (pinned=${updatedNote.pinned})`);

  await toggleNotePinned(newNote.id, false);
  console.log(`✓ Unpinned Note successfully.`);

  // ──────────────────────────────────────────────────────────────────────────
  // C. REMINDERS VERIFICATION (MILESTONE 4)
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [3] REMINDERS STORAGE, SNOOZE & DISMISS ---');
  const initialReminders = await getReminders();
  console.log(`✓ Fetched ${initialReminders.length} initial reminders.`);
  if (initialReminders.length === 0) throw new Error('Expected initial seed reminders');

  const now = Date.now();
  const testReminderTime = now + 1800000; // in 30 mins

  // 1. Add Reminder with linked Task
  const newReminder = await addReminder({
    title: 'Review Operating Systems Assignment Feedback',
    message: 'Check professor comments on memory management submission.',
    reminderAt: testReminderTime,
    taskId: newTask.id,
    completed: false,
    dismissed: false
  });
  console.log(`✓ Added Reminder: "${newReminder.title}" (Linked Task ID: ${newReminder.taskId})`);

  // 2. Retrieve Reminder
  const fetchedReminder = await getReminder(newReminder.id);
  if (!fetchedReminder || fetchedReminder.title !== 'Review Operating Systems Assignment Feedback') {
    throw new Error('Failed to retrieve reminder from IndexedDB');
  }
  if (fetchedReminder.taskId !== newTask.id) {
    throw new Error('Linked task ID was not preserved');
  }
  console.log('✓ Successfully retrieved reminder with intact linked task ID');

  // 3. Update Title & Message
  const updatedReminder = await updateReminder({
    ...fetchedReminder,
    title: 'Review OS Assignment Feedback & Grades',
    message: 'Check professor comments and grade rubric.'
  });
  if (updatedReminder.title !== 'Review OS Assignment Feedback & Grades') {
    throw new Error('Reminder title update failed');
  }
  console.log(`✓ Updated Reminder: "${updatedReminder.title}"`);

  // 4. Snooze Reminder (+1 hour)
  const snoozedTime = testReminderTime + 3600000;
  const snoozedReminder = await snoozeReminder(newReminder.id, snoozedTime);
  if (!snoozedReminder || snoozedReminder.reminderAt !== snoozedTime) {
    throw new Error('snoozeReminder failed to update reminderAt');
  }
  console.log(`✓ Snoozed Reminder: new reminderAt=${new Date(snoozedReminder.reminderAt).toLocaleTimeString()}`);

  // 5. Dismiss Reminder
  const dismissedReminder = await dismissReminder(newReminder.id);
  if (!dismissedReminder || !dismissedReminder.dismissed) {
    throw new Error('dismissReminder failed to set dismissed=true');
  }
  console.log(`✓ Dismissed Reminder status: dismissed=${dismissedReminder.dismissed}`);

  // 6. Delete a seed reminder
  const reminderToDelete = initialReminders[0];
  console.log(`✓ Deleting reminder: "${reminderToDelete.title}" (ID: ${reminderToDelete.id})`);
  await deleteReminder(reminderToDelete.id);
  const checkDeleted = await getReminder(reminderToDelete.id);
  if (checkDeleted) {
    throw new Error('Deleted reminder was still found in IndexedDB');
  }
  console.log('✓ Reminder successfully deleted.');

  // ──────────────────────────────────────────────────────────────────────────
  // D. SIMULATED RELOAD & INTEGRITY
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [4] SIMULATED RELOAD & COMPREHENSIVE INTEGRITY ---');
  const reloadTasks = await getTasks();
  const reloadNotes = await getNotes();
  const reloadReminders = await getReminders();

  const verifiedTask = reloadTasks.find(t => t.id === newTask.id);
  if (!verifiedTask || !verifiedTask.completed) {
    throw new Error('Task did not survive reload with completed=true');
  }

  const verifiedNote = reloadNotes.find(n => n.id === newNote.id);
  if (!verifiedNote) {
    throw new Error('Note did not survive reload');
  }

  const verifiedReminder = reloadReminders.find(r => r.id === newReminder.id);
  if (!verifiedReminder || !verifiedReminder.dismissed || verifiedReminder.reminderAt !== snoozedTime) {
    throw new Error('Reminder did not survive reload with dismissed and snoozed time intact');
  }

  // Verify chronological sorting of reminders
  for (let i = 0; i < reloadReminders.length - 1; i++) {
    if (reloadReminders[i].reminderAt > reloadReminders[i + 1].reminderAt) {
      throw new Error('Reminders are not sorted chronologically');
    }
  }

  const session = await initializeAndTrackPersistence();
  console.log(`✓ Tasks in store: ${reloadTasks.length}`);
  console.log(`✓ Notes in store: ${reloadNotes.length}`);
  console.log(`✓ Reminders in store: ${reloadReminders.length} (chronologically ordered)`);
  console.log(`✓ Session status: Ready (Reload count: ${session.reloadCount})`);

  console.log('\n================================================================');
  console.log('✅ ALL INDEXEDDB TASKS, NOTES & REMINDERS TESTS PASSED!');
  console.log('================================================================\n');
}

runTest().catch((err) => {
  console.error('\n❌ Persistence verification test failed:', err);
  process.exit(1);
});
