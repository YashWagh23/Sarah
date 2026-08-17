import 'fake-indexeddb/auto';
import { 
  getTasks, 
  getTask, 
  addTask, 
  updateTask, 
  deleteTask, 
  completeTask, 
  initializeAndTrackPersistence, 
  type Task 
} from './src/lib/db';

async function runTest() {
  console.log('=== Starting Sarah IndexedDB Task Storage Verification ===\n');

  // 1. Initial Load & Seeding
  console.log('1. Testing Task Store Initialization & Seeding...');
  const initialTasks = await getTasks();
  console.log(`   Fetched ${initialTasks.length} initial tasks.`);
  if (initialTasks.length === 0) {
    throw new Error('Expected initial seed tasks to be populated');
  }
  console.log(`   Sample seed task: "${initialTasks[0].title}" [${initialTasks[0].priority}] (${initialTasks[0].subject})`);

  // 2. Adding a new task
  console.log('\n2. Testing Task Creation (addTask)...');
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
  console.log(`   Created Task ID: ${newTask.id} - "${newTask.title}"`);
  
  const fetchedTask = await getTask(newTask.id);
  if (!fetchedTask || fetchedTask.title !== 'Study Distributed Consensus & Paxos') {
    throw new Error('Failed to retrieve newly added task from IndexedDB');
  }
  console.log('   Verification: Task successfully stored and retrieved.');

  // 3. Updating the task
  console.log('\n3. Testing Task Modification (updateTask)...');
  const updated = await updateTask({
    ...fetchedTask,
    title: 'Study Distributed Consensus (Raft & Paxos)',
    estimatedMinutes: 60,
    priority: 'must'
  });
  console.log(`   Updated Task Title: "${updated.title}", Est: ${updated.estimatedMinutes}m`);
  
  const verifyUpdated = await getTask(newTask.id);
  if (!verifyUpdated || verifyUpdated.estimatedMinutes !== 60 || verifyUpdated.title !== 'Study Distributed Consensus (Raft & Paxos)') {
    throw new Error('Task update did not persist in IndexedDB');
  }
  console.log('   Verification: Task updates persisted properly.');

  // 4. Completing the task
  console.log('\n4. Testing Task Completion (completeTask)...');
  const completedTask = await completeTask(newTask.id, true);
  if (!completedTask || !completedTask.completed) {
    throw new Error('completeTask failed to set completed flag');
  }
  console.log(`   Task completion status: ${completedTask.completed}`);

  // 5. Deleting another task
  console.log('\n5. Testing Task Deletion (deleteTask)...');
  const taskToDelete = initialTasks[initialTasks.length - 1];
  console.log(`   Deleting task: "${taskToDelete.title}" (ID: ${taskToDelete.id})`);
  await deleteTask(taskToDelete.id);
  
  const checkDeleted = await getTask(taskToDelete.id);
  if (checkDeleted) {
    throw new Error('Deleted task was still found in IndexedDB');
  }
  console.log('   Verification: Task was deleted successfully.');

  // 6. Persistence across simulated reload
  console.log('\n6. Testing Persistence across Simulated Reload...');
  const reloadTasks = await getTasks();
  const ourTask = reloadTasks.find(t => t.id === newTask.id);
  if (!ourTask) {
    throw new Error('Created task not found after simulated reload');
  }
  if (!ourTask.completed) {
    throw new Error('Completed status did not persist after reload');
  }
  if (ourTask.title !== 'Study Distributed Consensus (Raft & Paxos)') {
    throw new Error('Updated title did not persist after reload');
  }
  console.log(`   Reloaded task list has ${reloadTasks.length} tasks.`);
  console.log(`   Verified our task survived reload intact with completed=${ourTask.completed}`);

  // 7. General Persistence status check
  const status = await initializeAndTrackPersistence();
  console.log('\n7. General DB Session Status:', status);

  console.log('\n✅ SUCCESS: All IndexedDB Task persistence and CRUD tests passed perfectly!');
}

runTest().catch((err) => {
  console.error('\n❌ Task persistence test failed:', err);
  process.exit(1);
});
