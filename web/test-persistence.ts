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
  initializeAndTrackPersistence
} from './src/lib/db';

async function runTest() {
  console.log('=== Starting Sarah IndexedDB Comprehensive Persistence Verification ===\n');

  // ──────────────────────────────────────────────────────────────────────────
  // A. TASK VERIFICATION
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
  // B. ACADEMIC NOTES VERIFICATION
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [2] ACADEMIC NOTES STORAGE & CRUD ---');
  const initialNotes = await getNotes();
  console.log(`✓ Fetched ${initialNotes.length} initial academic notes.`);
  if (initialNotes.length === 0) throw new Error('Expected initial seed notes');

  // 1. Add Note
  const newNote = await addNote({
    title: 'Computer Networks Lab — Socket Programming in C',
    content: 'Server flow: socket() -> bind() -> listen() -> accept() -> read()/write() -> close(). Client flow: socket() -> connect() -> write()/read() -> close().',
    subject: 'Computer Networks',
    pinned: false
  });
  console.log(`✓ Added Note: "${newNote.title}" (Subject: ${newNote.subject})`);

  // 2. Retrieve Note
  const fetchedNote = await getNote(newNote.id);
  if (!fetchedNote || fetchedNote.title !== 'Computer Networks Lab — Socket Programming in C') {
    throw new Error('Failed to retrieve note from IndexedDB');
  }
  console.log('✓ Successfully retrieved note by ID');

  // 3. Update Title & Content & Subject
  const updatedNote = await updateNote({
    ...fetchedNote,
    title: 'Computer Networks Lab — TCP Socket Programming in C',
    content: 'Updated with multi-client threading instructions using pthread_create.',
    subject: 'Computer Networks'
  });
  if (
    updatedNote.title !== 'Computer Networks Lab — TCP Socket Programming in C' ||
    !updatedNote.content.includes('pthread_create')
  ) {
    throw new Error('Note update did not persist');
  }
  console.log(`✓ Updated Note content and title successfully.`);

  // 4. Pin Note
  const pinnedNote = await toggleNotePinned(newNote.id, true);
  if (!pinnedNote || !pinnedNote.pinned) {
    throw new Error('toggleNotePinned failed to pin note');
  }
  console.log(`✓ Pinned Note status: pinned=${pinnedNote.pinned}`);

  // 5. Unpin Note
  const unpinnedNote = await toggleNotePinned(newNote.id, false);
  if (!unpinnedNote || unpinnedNote.pinned) {
    throw new Error('toggleNotePinned failed to unpin note');
  }
  console.log(`✓ Unpinned Note status: pinned=${unpinnedNote.pinned}`);

  // 6. Delete Note
  const noteToDelete = initialNotes[initialNotes.length - 1];
  console.log(`✓ Deleting note: "${noteToDelete.title}" (ID: ${noteToDelete.id})`);
  await deleteNote(noteToDelete.id);
  const checkDeletedNote = await getNote(noteToDelete.id);
  if (checkDeletedNote) {
    throw new Error('Deleted note was still present in IndexedDB');
  }
  console.log('✓ Note successfully deleted.');

  // ──────────────────────────────────────────────────────────────────────────
  // C. SIMULATED RELOAD PERSISTENCE
  // ──────────────────────────────────────────────────────────────────────────
  console.log('\n--- [3] SIMULATED RELOAD & INTEGRITY ---');
  const reloadTasks = await getTasks();
  const reloadNotes = await getNotes();
  
  const verifiedTask = reloadTasks.find(t => t.id === newTask.id);
  if (!verifiedTask || !verifiedTask.completed) {
    throw new Error('Task did not survive reload with completed status');
  }

  const verifiedNote = reloadNotes.find(n => n.id === newNote.id);
  if (!verifiedNote || verifiedNote.title !== 'Computer Networks Lab — TCP Socket Programming in C') {
    throw new Error('Note did not survive reload with updated content');
  }

  const session = await initializeAndTrackPersistence();
  console.log(`✓ Tasks after reload: ${reloadTasks.length}`);
  console.log(`✓ Notes after reload: ${reloadNotes.length}`);
  console.log(`✓ Session status: Ready (Reload count: ${session.reloadCount})`);

  console.log('\n================================================================');
  console.log('✅ ALL INDEXEDDB TASKS & ACADEMIC NOTES TESTS PASSED PERFECTLY!');
  console.log('================================================================\n');
}

runTest().catch((err) => {
  console.error('\n❌ Persistence verification test failed:', err);
  process.exit(1);
});
