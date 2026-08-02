-- Indexes for stretch /today session lookups (user-scoped order by created/completed).

CREATE INDEX IF NOT EXISTS idx_workout_session_user_created
	ON workout_session (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_workout_session_user_completed
	ON workout_session (user_id, completed_at DESC);
