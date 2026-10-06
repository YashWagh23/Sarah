import { buildTonightPlan, rankTasks, type PlanSession } from './src/lib/planner';
import { DEFAULT_USER_PROFILE, type Task, type UserProfile } from './src/lib/db';
import { toLocalDateStr } from './src/lib/datetime';

// Tuesday 6 Oct 2026 — every scenario pins the clock so results never depend on
// when or where the suite runs.
const at = (day: number, hh: number, mm = 0) => new Date(2026, 9, day, hh, mm, 0, 0).getTime();
const TUE = 6;
const SAT = 10;

const profile = (overrides: Partial<UserProfile> = {}): UserProfile => ({
  ...DEFAULT_USER_PROFILE,
  targetBedtime: '23:30',
  wakeUpTime: '07:00',
  collegeEndTime: '17:00',
  commuteMinutes: 30,
  collegeDays: [1, 2, 3, 4, 5],
  energyLevel: 'normal',
  ...overrides
});

let seq = 0;
const task = (nowMs: number, dayOffset: number, overrides: Partial<Task> = {}): Task => {
  const d = new Date(nowMs);
  d.setDate(d.getDate() + dayOffset);
  seq++;
  return {
    id: `t${seq}`,
    title: `Task ${seq}`,
    subject: 'General',
    deadline: toLocalDateStr(d),
    deadlineTime: '23:59',
    priority: 'should',
    estimatedMinutes: 45,
    completed: false,
    createdAt: seq,
    updatedAt: seq,
    ...overrides
  };
};

let failures = 0;
function check(label: string, condition: boolean, detail = '') {
  if (condition) {
    console.log(`✓ ${label}`);
  } else {
    failures++;
    console.log(`✗ ${label}${detail ? ` — ${detail}` : ''}`);
  }
}

console.log('=== Sarah Tonight Planner Verification ===\n');

// 1. A normal weeknight: tonight's deadlines first, far-off work later.
{
  const now = at(TUE, 18);
  const dueToday = task(now, 0, { title: 'Lab report', priority: 'must', estimatedMinutes: 60 });
  const dueTomorrowMorning = task(now, 1, { title: 'Quiz prep', deadlineTime: '09:00', estimatedMinutes: 45 });
  const farOff = task(now, 12, { title: 'Term project', priority: 'must', estimatedMinutes: 90 });
  const done = task(now, 0, { title: 'Already done', completed: true });
  const plan = buildTonightPlan([farOff, dueTomorrowMorning, dueToday, done], profile(), now);

  console.log('--- [1] Weeknight plan ---');
  check('Completed tasks are not planned', !plan.ranked.some(s => s.task.id === done.id));
  check('Next move is the task due tonight', plan.next?.task.id === dueToday.id, plan.next?.task.title);
  check('Due-tomorrow-morning work counts as tonight-critical',
    plan.ranked.find(s => s.task.id === dueTomorrowMorning.id)?.needsTonight === true);
  check('A must-do due in 12 days does not outrank tonight’s deadlines',
    plan.ranked[plan.ranked.length - 1].task.id === farOff.id);
  check('Free time excludes the dinner block (5h30m − 30m)', plan.freeMinutes === 300, String(plan.freeMinutes));
  check('Nothing at risk', plan.atRisk.length === 0);
  check('Status is a fit, not overloaded', plan.status === 'optimal' || plan.status === 'tight', plan.status);
  const dinner = plan.items.find(i => i.kind === 'dinner');
  check('Dinner is kept on the timeline', Boolean(dinner));
  const sessions = plan.items.filter((i): i is PlanSession => i.kind === 'task');
  check('No session overlaps dinner', sessions.every(s => !dinner || s.end <= dinner.start || s.start >= dinner.end));
  check('Planned focus never exceeds realistic capacity', plan.plannedMinutes <= plan.focusCapacityMinutes);
}

// 2. Too much due tonight → overloaded with a shortfall, not a cheerful "achievable".
{
  const now = at(TUE, 22, 30);
  const a = task(now, 0, { title: 'Assignment', priority: 'must', estimatedMinutes: 120 });
  const b = task(now, 0, { title: 'Problem set', priority: 'must', estimatedMinutes: 90 });
  const plan = buildTonightPlan([a, b], profile(), now);

  console.log('\n--- [2] Overloaded night ---');
  check('Status is overloaded', plan.status === 'overloaded', plan.status);
  check('Shortfall is reported', plan.shortfallMinutes > 0, String(plan.shortfallMinutes));
  check('At-risk list names the deadlines', plan.atRisk.length >= 1);
}

// 3. Bedtime arithmetic around midnight.
{
  console.log('\n--- [3] Midnight edge cases ---');
  const lateNow = at(TUE + 1, 0, 10); // 00:10, bedtime 23:30 — already past it
  const lateTask = task(lateNow, 1);
  const late = buildTonightPlan([lateTask], profile(), lateNow);
  check('00:10 with a 23:30 bedtime is past bedtime (old engine reported ~23h free)',
    late.status === 'past_bedtime' && late.minutesUntilBed === 0, `${late.status} / ${late.minutesUntilBed}`);

  const owlNow = at(TUE, 23, 50);
  const owl = buildTonightPlan([task(owlNow, 1)], profile({ targetBedtime: '00:30' }), owlNow);
  check('A 00:30 bedtime at 23:50 leaves 40 minutes', owl.minutesUntilBed === 40, String(owl.minutesUntilBed));

  const owlAfterMidnight = at(TUE + 1, 0, 10);
  const owl2 = buildTonightPlan([], profile({ targetBedtime: '00:30' }), owlAfterMidnight);
  check('A 00:30 bedtime at 00:10 still leaves 20 minutes', owl2.minutesUntilBed === 20, String(owl2.minutesUntilBed));
}

// 4. College days vs. weekends.
{
  console.log('\n--- [4] College days ---');
  const weekday = at(TUE, 14);
  const weekdayPlan = buildTonightPlan([task(weekday, 0, { estimatedMinutes: 30 })], profile(), weekday);
  const firstSession = weekdayPlan.items.find((i): i is PlanSession => i.kind === 'task');
  check('Weekday plan blocks classes first', weekdayPlan.items[0]?.kind === 'college');
  check('First session starts after college + commute (17:30)',
    Boolean(firstSession) && firstSession!.start >= 17 * 60 + 30, String(firstSession?.start));

  const saturday = at(SAT, 14);
  const satPlan = buildTonightPlan([task(saturday, 0, { estimatedMinutes: 30 })], profile(), saturday);
  const satSession = satPlan.items.find((i): i is PlanSession => i.kind === 'task');
  check('Saturday is not a college day by default', !satPlan.isCollegeDay);
  check('Saturday work can start right away', satSession?.start === 14 * 60, String(satSession?.start));

  const satWithClasses = buildTonightPlan([], profile({ collegeDays: [1, 2, 3, 4, 5, 6] }), saturday);
  check('Saturday classes are honoured when enabled', satWithClasses.isCollegeDay);
}

// 5. Rest mode only keeps what cannot wait.
{
  console.log('\n--- [5] Rest mode ---');
  const now = at(TUE, 19);
  const urgent = task(now, 0, { title: 'Due tonight', estimatedMinutes: 25 });
  const later = task(now, 5, { title: 'Due in 5 days', priority: 'must' });
  const plan = buildTonightPlan([urgent, later], profile({ energyLevel: 'exhausted' }), now);
  check('Status is rest_recommended', plan.status === 'rest_recommended', plan.status);
  check('Urgent task is still planned', plan.planned.some(s => s.task.id === urgent.id));
  check('Non-urgent task is deferred', plan.deferred.some(s => s.task.id === later.id));
}

// 6. Ranking and splitting.
{
  console.log('\n--- [6] Ranking & long tasks ---');
  const now = at(TUE, 18);
  const overdue = task(now, -2, { title: 'Overdue', priority: 'later' });
  const mustNextWeek = task(now, 7, { title: 'Must, next week', priority: 'must' });
  const ranked = rankTasks([mustNextWeek, overdue], now);
  check('Overdue work outranks a must-do due next week', ranked[0].task.id === overdue.id);
  check('Overdue reason is human-readable', ranked[0].reason === 'Overdue by 2 days', ranked[0].reason);

  const big = task(now, 0, { title: 'Big essay', estimatedMinutes: 120 });
  const plan = buildTonightPlan([big], profile(), now);
  const parts = plan.items.filter((i): i is PlanSession => i.kind === 'task');
  check('A 2h task is split into 45m focus blocks', parts.length === 3 && parts.every(p => p.end - p.start <= 45),
    parts.map(p => p.end - p.start).join(','));
  check('Breaks separate the blocks', plan.items.some(i => i.kind === 'break'));
  check('Split parts are numbered', parts[parts.length - 1]?.part === parts.length && parts[0]?.parts === parts.length);
}

// 7. Empty state.
{
  console.log('\n--- [7] Nothing to do ---');
  const now = at(TUE, 18);
  const plan = buildTonightPlan([], profile(), now);
  check('No tasks → clear status', plan.status === 'clear');
  check('No next move', plan.next === null);
}

console.log('\n================================================================');
if (failures > 0) {
  console.log(`❌ ${failures} planner check(s) failed`);
  process.exit(1);
}
console.log('✅ ALL TONIGHT PLANNER CHECKS PASSED!');
console.log('================================================================');
