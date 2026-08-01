export const CATALOG_STATUS = ['ACTIVE', 'COMING_SOON', 'DEPRECATED'] as const
export const DIFFICULTY = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ELITE'] as const
export const EXERCISE_CATEGORY = [
  'PULL',
  'PUSH',
  'CORE',
  'BALANCE',
  'STATIC',
  'MOBILITY',
  'LEGS',
] as const
export const METRIC_TYPE = ['TIME', 'REPS', 'DISTANCE', 'ANGLE', 'WEIGHT'] as const
export const NODE_TYPE = ['MILESTONE', 'SKILL', 'HOLD', 'MOBILITY'] as const
export const OPERATOR = ['>=', '<=', '==', '<', '>'] as const

export const ADMIN_STEPS = [
  {
    step: 1,
    to: '/admin/exercises',
    title: 'Exercises',
    blurb: 'Create reusable moves (e.g. 3s Planche Hold).',
  },
  {
    step: 2,
    to: '/admin/nodes',
    title: 'Goals / Nodes',
    blurb: 'Create a goal node and link it to an exercise.',
  },
  {
    step: 3,
    to: '/admin/workouts',
    title: 'Workouts',
    blurb: 'Attach workouts to a skill node, then add exercise lines.',
  },
  {
    step: '3b',
    to: '/admin/workout-plans',
    title: 'Workout plans',
    blurb: 'Pick a node + day count, then build each day’s exercises (copy prior day to progress volume).',
  },
  {
    step: 4,
    to: '/admin/path',
    title: 'Path sequence',
    blurb: 'Connect goals with edges so learners progress step by step.',
  },
  {
    step: 5,
    to: '/admin/path-questions',
    title: 'Placement questions',
    blurb: 'Per goal: ask ordered questions; answers place the user on the path.',
  },
] as const
