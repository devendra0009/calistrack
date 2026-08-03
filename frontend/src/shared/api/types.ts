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
  /** Computed from dateOfBirth when present; otherwise legacy stored age. */
  age: number | null
  dateOfBirth: string | null
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
  dateOfBirth?: string
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

export interface OnboardingNextQuestionResponse {
  goalNodeId: string
  index: number
  total: number
  question: OnboardingQuestionDto
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

export interface OnboardingStepRequest {
  goalNodeId: string
  answers: OnboardingAnswerDto[]
}

export type OnboardingStepOutcome = 'NEXT' | 'PLACED'

export interface OnboardingStepResponse {
  outcome: OnboardingStepOutcome
  index: number
  total: number
  nextQuestion?: OnboardingQuestionDto | null
  goalNodeId?: string | null
  focusNodeId?: string | null
  sessionId?: string | null
  workoutId?: string | null
  workoutTitle?: string | null
  sessionStatus?: WorkoutSessionStatus | null
  placedNodes?: PlacedUserNodeDto[] | null
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

export type AssessmentStatus =
  | 'NOT_ATTEMPTED'
  | 'PENDING_REVIEW'
  | 'PENDING_AI'
  | 'PASSED'
  | 'FAILED'

export interface PathAssessmentNodeResponse {
  nodeId: string
  name: string
  description: string | null
  difficulty: string | null
  stepIndex: number
  goal: boolean
  status: UserNodeStatus
  verified: boolean
  awaitingVerify: boolean
  videoUrl: string | null
}

export interface GoalPathAssessmentResponse {
  goalNodeId: string
  goalNodeName: string
  verifiedCount: number
  totalCount: number
  nodes: PathAssessmentNodeResponse[]
}

export interface SubmitAssessmentRequest {
  nodeId: string
  videoUrl: string
  workoutSessionId?: string
}

export interface AssessmentResponse {
  id: string
  nodeId: string
  nodeName: string
  status: AssessmentStatus
  verified: boolean
  videoUrl: string | null
  performedAt: string
}

export type WorkoutKind = 'SKILL' | 'STRETCH'
export type UserPlanEnrollmentStatus = 'ACTIVE' | 'AWAITING_VERIFY' | 'COMPLETED'

export interface CurrentWorkoutSessionResponse {
  sessionId: string
  workoutId: string
  workoutTitle: string
  workoutDescription: string | null
  workoutKind: WorkoutKind
  focusNodeId: string
  focusNodeName: string
  status: WorkoutSessionStatus
  verified: boolean
  planEnrollmentId: string | null
  planDayNumber: number | null
  planDurationDays: number | null
  enrollmentStatus: UserPlanEnrollmentStatus | null
  awaitingVerify: boolean
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
  exerciseDescription: string | null
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
  workoutKind: WorkoutKind
  focusNodeId: string
  focusNodeName: string
  status: WorkoutSessionStatus
  verified: boolean
  planEnrollmentId: string | null
  planDayNumber: number | null
  planDurationDays: number | null
  enrollmentStatus: UserPlanEnrollmentStatus | null
  awaitingVerify: boolean
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

export interface StretchExerciseLineDto {
  workoutExerciseId: string
  sequence: number
  exerciseId: string
  exerciseName: string
  exerciseDescription: string | null
  exerciseMetricType: string
  thumbnailUrl: string | null
  demoVideoUrl: string | null
  targetSets: number | null
  targetReps: number | null
  targetHoldSeconds: number | null
  targetRestSeconds: number | null
  notes: string | null
}

export interface StretchingTodayResponse {
  planCode: string
  planTitle: string
  dayNumber: number
  durationDays: number
  workoutId: string
  workoutTitle: string
  workoutDescription: string | null
  sessionId: string | null
  sessionStatus: WorkoutSessionStatus | null
  exercises: StretchExerciseLineDto[]
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
  kind: WorkoutKind
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
  kind: WorkoutKind
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
  kind?: WorkoutKind
  difficulty: string
  status?: string
  exercises?: AdminWorkoutExerciseRequest[] | null
}

export interface AdminWorkoutPlanDayResponse {
  id: string
  dayNumber: number
  workout: NamedRef
}

export interface AdminWorkoutPlanDayRequest {
  dayNumber: number
  workoutId: string
}

export type WorkoutPlanKind = 'SKILL' | 'DAILY_ROUTINE'

export interface AdminWorkoutPlanSummaryResponse {
  id: string
  title: string
  node: NamedRef
  kind: WorkoutPlanKind
  code: string | null
  durationDays: number
  dayCount: number
  status: string
  createdAt: string
  updatedAt: string
}

export interface AdminWorkoutPlanResponse {
  id: string
  title: string
  description: string | null
  node: NamedRef
  kind: WorkoutPlanKind
  code: string | null
  durationDays: number
  status: string
  days: AdminWorkoutPlanDayResponse[]
  createdAt: string
  updatedAt: string
}

export interface AdminWorkoutPlanRequest {
  title: string
  description?: string | null
  nodeId: string
  kind?: WorkoutPlanKind
  code?: string | null
  durationDays: number
  status?: string
  days?: AdminWorkoutPlanDayRequest[] | null
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

export interface ChatRequest {
  message: string
}

export interface ChatResponse {
  response: string
}
