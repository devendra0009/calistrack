-- Morning stretch daily routine (additive). Safe to re-run: ON CONFLICT DO NOTHING.
-- Personal cycle: user stays on Day N until they complete that stretch session.

-- Stretch exercises (MOBILITY / TIME)
INSERT INTO exercise (id, name, description, category, metric_type, difficulty, status) VALUES
('71111111-1111-1111-1111-111111110001', 'Neck Circles', 'Slow gentle neck circles each direction', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110002', 'Shoulder Rolls', 'Roll shoulders forward and back', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110003', 'Cat-Cow', 'Spinal flexion/extension on all fours or standing', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110004', 'Standing Forward Fold', 'Hinge at hips, soft knees, hang', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110005', 'Hip Flexor Stretch', 'Kneeling or standing hip flexor opener', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110006', 'World''s Greatest Stretch', 'Lunge with thoracic rotation', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110007', 'Hamstring Stretch', 'Seated or standing posterior chain stretch', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110008', 'Figure-Four Glute Stretch', 'Supine or seated figure-four', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110009', 'Chest Opener', 'Doorway or clasped-hands chest stretch', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110010', 'Wrist Circles', 'Gentle wrist mobility both directions', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110011', 'Side Body Stretch', 'Overhead reach and lateral bend', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110012', 'Ankle Circles', 'Ankle mobility each direction', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110013', 'Child''s Pose', 'Resting hip and back stretch', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE'),
('71111111-1111-1111-1111-111111110014', 'Spinal Twist', 'Seated or supine gentle twist', 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Anchor node (not on any skill path; satisfies workout.goal_node_id FK)
INSERT INTO node (id, name, description, node_type, exercise_id, target_value, operator, unit_label, difficulty, estimated_minutes, status) VALUES
('72222222-2222-2222-2222-222222220001', 'Morning Stretch',
 'Daily guided stretching routine (not a skill path node)', 'MOBILITY',
 '71111111-1111-1111-1111-111111110003', 1, '>=', 'SESSION', 'BEGINNER', 10, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 7 stretch workouts
INSERT INTO workout (id, title, description, goal_node_id, kind, difficulty, status) VALUES
('73333333-3333-3333-3333-333333330001', 'Morning Stretch — Day 1',
 'Wake-up neck, shoulders, and spine', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330002', 'Morning Stretch — Day 2',
 'Hip openers and forward fold', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330003', 'Morning Stretch — Day 3',
 'Full-body flow with world''s greatest stretch', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330004', 'Morning Stretch — Day 4',
 'Posterior chain and glutes', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330005', 'Morning Stretch — Day 5',
 'Chest, wrists, and side body', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330006', 'Morning Stretch — Day 6',
 'Ankles, hips, and gentle twist', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE'),
('73333333-3333-3333-3333-333333330007', 'Morning Stretch — Day 7',
 'Recovery full-body stretch', '72222222-2222-2222-2222-222222220001', 'STRETCH', 'BEGINNER', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Day 1
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330001', '71111111-1111-1111-1111-111111110001', 1, 1, NULL, 45, 10, 'Slow circles'),
('73333333-3333-3333-3333-333333330001', '71111111-1111-1111-1111-111111110002', 2, 1, NULL, 45, 10, 'Both directions'),
('73333333-3333-3333-3333-333333330001', '71111111-1111-1111-1111-111111110003', 3, 1, NULL, 60, 15, 'Match breath'),
('73333333-3333-3333-3333-333333330001', '71111111-1111-1111-1111-111111110009', 4, 1, NULL, 45, 10, 'Open the chest')
ON CONFLICT DO NOTHING;

-- Day 2
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330002', '71111111-1111-1111-1111-111111110002', 1, 1, NULL, 30, 10, 'Warm shoulders'),
('73333333-3333-3333-3333-333333330002', '71111111-1111-1111-1111-111111110005', 2, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330002', '71111111-1111-1111-1111-111111110004', 3, 1, NULL, 60, 15, 'Soft knees'),
('73333333-3333-3333-3333-333333330002', '71111111-1111-1111-1111-111111110013', 4, 1, NULL, 60, 10, 'Relax')
ON CONFLICT DO NOTHING;

-- Day 3
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330003', '71111111-1111-1111-1111-111111110003', 1, 1, NULL, 45, 10, 'Warm the spine'),
('73333333-3333-3333-3333-333333330003', '71111111-1111-1111-1111-111111110006', 2, 1, NULL, 60, 20, 'Each side'),
('73333333-3333-3333-3333-333333330003', '71111111-1111-1111-1111-111111110005', 3, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330003', '71111111-1111-1111-1111-111111110011', 4, 1, NULL, 40, 10, 'Each side')
ON CONFLICT DO NOTHING;

-- Day 4
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330004', '71111111-1111-1111-1111-111111110007', 1, 1, NULL, 60, 15, 'Each side if needed'),
('73333333-3333-3333-3333-333333330004', '71111111-1111-1111-1111-111111110008', 2, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330004', '71111111-1111-1111-1111-111111110004', 3, 1, NULL, 45, 10, 'Hang soft'),
('73333333-3333-3333-3333-333333330004', '71111111-1111-1111-1111-111111110013', 4, 1, NULL, 60, 10, 'Breathe')
ON CONFLICT DO NOTHING;

-- Day 5
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330005', '71111111-1111-1111-1111-111111110009', 1, 1, NULL, 45, 10, 'Open chest'),
('73333333-3333-3333-3333-333333330005', '71111111-1111-1111-1111-111111110010', 2, 1, NULL, 30, 10, 'Both wrists'),
('73333333-3333-3333-3333-333333330005', '71111111-1111-1111-1111-111111110011', 3, 1, NULL, 40, 10, 'Each side'),
('73333333-3333-3333-3333-333333330005', '71111111-1111-1111-1111-111111110002', 4, 1, NULL, 30, 10, 'Finish with rolls')
ON CONFLICT DO NOTHING;

-- Day 6
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330006', '71111111-1111-1111-1111-111111110012', 1, 1, NULL, 30, 10, 'Each ankle'),
('73333333-3333-3333-3333-333333330006', '71111111-1111-1111-1111-111111110005', 2, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330006', '71111111-1111-1111-1111-111111110014', 3, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330006', '71111111-1111-1111-1111-111111110013', 4, 1, NULL, 60, 10, 'Settle')
ON CONFLICT DO NOTHING;

-- Day 7
INSERT INTO workout_exercise (workout_id, exercise_id, sequence, target_sets, target_reps, target_hold_seconds, target_rest_seconds, notes) VALUES
('73333333-3333-3333-3333-333333330007', '71111111-1111-1111-1111-111111110001', 1, 1, NULL, 30, 5, 'Easy'),
('73333333-3333-3333-3333-333333330007', '71111111-1111-1111-1111-111111110003', 2, 1, NULL, 45, 10, 'Flow'),
('73333333-3333-3333-3333-333333330007', '71111111-1111-1111-1111-111111110006', 3, 1, NULL, 45, 15, 'Each side'),
('73333333-3333-3333-3333-333333330007', '71111111-1111-1111-1111-111111110008', 4, 1, NULL, 40, 10, 'Each side'),
('73333333-3333-3333-3333-333333330007', '71111111-1111-1111-1111-111111110013', 5, 1, NULL, 60, 10, 'Close the week')
ON CONFLICT DO NOTHING;

INSERT INTO workout_plan (id, node_id, title, description, kind, code, duration_days, status, created_at, updated_at) VALUES
('75555555-5555-5555-5555-555555550001', '72222222-2222-2222-2222-222222220001',
 'Morning Stretch — 7 Day Cycle',
 'Guided daily stretch. Advances only when you finish the current day; then cycles Day 7 → Day 1.',
 'DAILY_ROUTINE', 'morning_stretch', 7, 'ACTIVE', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO workout_plan_day (id, plan_id, day_number, workout_id) VALUES
('76666666-6666-6666-6666-666666660001', '75555555-5555-5555-5555-555555550001', 1, '73333333-3333-3333-3333-333333330001'),
('76666666-6666-6666-6666-666666660002', '75555555-5555-5555-5555-555555550001', 2, '73333333-3333-3333-3333-333333330002'),
('76666666-6666-6666-6666-666666660003', '75555555-5555-5555-5555-555555550001', 3, '73333333-3333-3333-3333-333333330003'),
('76666666-6666-6666-6666-666666660004', '75555555-5555-5555-5555-555555550001', 4, '73333333-3333-3333-3333-333333330004'),
('76666666-6666-6666-6666-666666660005', '75555555-5555-5555-5555-555555550001', 5, '73333333-3333-3333-3333-333333330005'),
('76666666-6666-6666-6666-666666660006', '75555555-5555-5555-5555-555555550001', 6, '73333333-3333-3333-3333-333333330006'),
('76666666-6666-6666-6666-666666660007', '75555555-5555-5555-5555-555555550001', 7, '73333333-3333-3333-3333-333333330007')
ON CONFLICT (id) DO NOTHING;
