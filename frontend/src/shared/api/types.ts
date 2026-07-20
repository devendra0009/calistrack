export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'UNSPECIFIED'
export type ExperienceLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
export type QuestionType = 'REPS' | 'YES_NO'
export type UserNodeStatus = 'LOCKED' | 'AVAILABLE' | 'IN_PROGRESS' | 'COMPLETED'
export type WorkoutSessionStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'

export interface AuthResponse {
  idToken: string
  refreshToken: string
  expiresIn: string
  userId: string
  email: string | null
  displayName: string | null
}

export interface GoalNodeSummary {
  id: string
  name: string
  description: string | null
  nodeType: string | null
  difficulty: string | null
  targetValue: number | null
  operator: string | null
  unitLabel: string | null
  xpReward: number | null
  estimatedMinutes: number | null
  status: string | null
}

export interface MeResponse {
  id: string
  displayName: string
  heightCm: number | null
  weightKg: number | null
  age: number | null
  gender: Gender | null
  experience: ExperienceLevel | null
  avatarUrl: string | null
  role: string
  goal: GoalNodeSummary | null
  createdAt: string
  updatedAt: string
}

export interface PatchMeRequest {
  displayName?: string
  heightCm?: number
  weightKg?: number
  age?: number
  gender?: Gender
  experience?: ExperienceLevel
  avatarUrl?: string
}

export interface OnboardingQuestionDto {
  nodeId: string
  prompt: string
  type: QuestionType
}

export interface OnboardingQuestionsResponse {
  goalNodeId: string
  questions: OnboardingQuestionDto[]
}

export interface OnboardingAnswerDto {
  nodeId: string
  type: QuestionType
  value: number | boolean | string
}

export interface OnboardingAnswersRequest {
  goalNodeId: string
  answers: OnboardingAnswerDto[]
}

export interface PlacedUserNodeDto {
  nodeId: string
  status: UserNodeStatus
}

export interface OnboardingAnswersResponse {
  goalNodeId: string
  focusNodeId: string
  sessionId: string
  workoutId: string
  workoutTitle: string
  sessionStatus: WorkoutSessionStatus
  placedNodes: PlacedUserNodeDto[]
}

export interface OnboardingStatusResponse {
  completed: boolean
}

export interface CurrentWorkoutSessionResponse {
  sessionId: string
  workoutId: string
  workoutTitle: string
  workoutDescription: string | null
  focusNodeId: string
  focusNodeName: string
  status: WorkoutSessionStatus
  verified: boolean
  createdAt: string
  updatedAt: string
}

export type ExerciseAttemptStatus = 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED'

export interface AttemptSummaryDto {
  id: string
  status: ExerciseAttemptStatus
  actualSets: number | null
  actualReps: number | null
  actualHoldSeconds: number | null
  actualRestSeconds: number | null
  notes: string | null
}

export interface SessionExerciseLineDto {
  workoutExerciseId: string
  sequence: number
  exerciseId: string
  exerciseName: string
  exerciseMetricType: string
  thumbnailUrl: string | null
  demoVideoUrl: string | null
  targetSets: number | null
  targetReps: number | null
  targetHoldSeconds: number | null
  targetRestSeconds: number | null
  notes: string | null
  attempt: AttemptSummaryDto | null
}

export interface WorkoutSessionDetailResponse {
  sessionId: string
  workoutId: string
  workoutTitle: string
  workoutDescription: string | null
  focusNodeId: string
  focusNodeName: string
  status: WorkoutSessionStatus
  verified: boolean
  startedAt: string | null
  completedAt: string | null
  exercises: SessionExerciseLineDto[]
}

export interface ExerciseAttemptResponse {
  id: string
  workoutSessionId: string
  workoutExerciseId: string
  status: ExerciseAttemptStatus
  actualSets: number | null
  actualReps: number | null
  actualHoldSeconds: number | null
  actualRestSeconds: number | null
  notes: string | null
}

export interface PatchExerciseAttemptRequest {
  actualSets?: number
  actualReps?: number
  actualHoldSeconds?: number
  actualRestSeconds?: number
  notes?: string
  status?: ExerciseAttemptStatus
}

export interface CatalogGoal {
  id: string
  name: string
  description: string | null
  nodeType?: string | null
  difficulty: string | null
}

/** Nested id + display name from admin catalog APIs. */
export interface NamedRef {
  id: string
  name: string
}

export interface AdminExerciseResponse {
  id: string
  name: string
  description: string | null
  category: string
  metricType: string
  difficulty: string
  thumbnailUrl: string | null
  demoVideoUrl: string | null
  status: string
  createdAt: string
  updatedAt: string
}

export interface AdminExerciseRequest {
  name: string
  description?: string | null
  category: string
  metricType: string
  difficulty: string
  thumbnailUrl?: string | null
  demoVideoUrl?: string | null
  status?: string
}

export interface AdminNodeResponse {
  id: string
  name: string
  description: string | null
  nodeType: string
  exercise: NamedRef
  targetValue: number
  operator: string
  unitLabel: string
  difficulty: string
  xpReward: number | null
  estimatedMinutes: number | null
  status: string
  createdAt: string
  updatedAt: string
}

export interface AdminNodeRequest {
  name: string
  description?: string | null
  nodeType: string
  exerciseId: string
  targetValue: number
  operator: string
  unitLabel: string
  difficulty: string
  xpReward?: number | null
  estimatedMinutes?: number | null
  status?: string
}

export interface AdminNodeEdgeResponse {
  id: string
  fromNode: NamedRef
  toNode: NamedRef
  relationType: string
  createdAt: string
}

export interface AdminNodeEdgeRequest {
  fromNodeId: string
  toNodeId: string
  relationType?: string
}

export interface AdminWorkoutExerciseResponse {
  id: string
  exercise: NamedRef
  sequence: number
  targetSets: number | null
  targetReps: number | null
  targetHoldSeconds: number | null
  targetRestSeconds: number | null
  notes: string | null
  demoVideoUrl: string | null
}

export interface AdminWorkoutExerciseRequest {
  exerciseId: string
  sequence: number
  targetSets?: number | null
  targetReps?: number | null
  targetHoldSeconds?: number | null
  targetRestSeconds?: number | null
  notes?: string | null
  demoVideoUrl?: string | null
}

export interface AdminWorkoutSummaryResponse {
  id: string
  title: string
  description: string | null
  goalNode: NamedRef
  difficulty: string
  createdByUserId: string | null
  status: string
  exerciseCount: number
  createdAt: string
  updatedAt: string
}

export interface AdminWorkoutResponse {
  id: string
  title: string
  description: string | null
  goalNode: NamedRef
  difficulty: string
  createdByUserId: string | null
  status: string
  createdAt: string
  updatedAt: string
  exercises: AdminWorkoutExerciseResponse[]
}

export interface AdminWorkoutRequest {
  title: string
  description?: string | null
  goalNodeId: string
  difficulty: string
  status?: string
  exercises?: AdminWorkoutExerciseRequest[] | null
}

export type PlacementAnswerType = 'REPS' | 'YES_NO'

export interface AdminPathQuestionResponse {
  id: string
  goalNode: NamedRef
  node: NamedRef
  prompt: string
  answerType: PlacementAnswerType
  sortOrder: number
  createdAt: string
}

export interface AdminPathQuestionRequest {
  goalNodeId: string
  nodeId: string
  prompt: string
  answerType: PlacementAnswerType
  sortOrder: number
}
