-- Morning stretch daily routine (additive). Safe to re-run: ON CONFLICT DO NOTHING.
-- Personal cycle: user stays on Day N until they complete that stretch session.
-- Descriptions: form steps + plain-language TARGETS: line (parsed by the stretch UI).
-- thumbnail_url: placeholder paths — replace with your real image links when ready.

-- Stretch exercises (MOBILITY / TIME)
INSERT INTO exercise (id, name, description, category, metric_type, difficulty, status, thumbnail_url) VALUES
('71111111-1111-1111-1111-111111110001', 'Neck Circles',
 $desc$Sit or stand tall with shoulders relaxed.
Drop your chin slightly and roll your head in slow, wide circles.
Move only through a pain-free range — never force or crack the neck.
Switch directions halfway; keep breathing steady.

TARGETS: Neck and upper shoulders — eases desk stiffness and makes it easier to turn your head$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/neck_circles_howto.png'),
('71111111-1111-1111-1111-111111110002', 'Shoulder Rolls',
 $desc$Stand or sit upright with arms hanging naturally.
Lift shoulders toward ears, then roll them back and down in a smooth circle.
Feel the shoulder blades glide; keep the neck long and jaw soft.
Reverse direction for the second half of the hold.

TARGETS: Shoulders and upper back — loosens tight shoulders and helps you sit/stand taller$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/shoulder_rolls_howto.png'),
('71111111-1111-1111-1111-111111110003', 'Cat-Cow',
 $desc$Start on all fours (or stand with hands on thighs).
Inhale as you drop the belly and lift the chest (cow); gaze softens forward.
Exhale as you round the spine, tuck the pelvis, and look toward the navel (cat).
Move with the breath — feel each vertebra articulate, not just the lower back.

TARGETS: Mid and lower back — wakes up a stiff spine and feels good first thing in the morning$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/cat_cow_howto.png'),
('71111111-1111-1111-1111-111111110004', 'Standing Forward Fold',
 $desc$Stand with feet hip-width; soft bend in the knees.
Hinge at the hips and fold forward, letting the torso hang heavy.
Hold elbows or let arms dangle; keep weight mid-foot, not in the heels.
Gently nod the head yes/no to release the neck — never lock the knees.

TARGETS: Back of the legs and lower back — lengthens tight legs and helps you unwind$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/standing_forward_fold_howto.png'),
('71111111-1111-1111-1111-111111110005', 'Hip Flexor Stretch',
 $desc$Kneel on one knee with the front foot planted (or step into a standing lunge).
Tuck the pelvis under so the low back stays long — avoid dumping into a swayback.
Gently shift hips forward until you feel the front of the rear hip open.
Keep the ribs stacked over the pelvis; switch sides when noted.

TARGETS: Front of the hips — opens hips after sitting and makes lunges feel freer$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/hip_flexor_stretch_howto.png'),
('71111111-1111-1111-1111-111111110006', 'World''s Greatest Stretch',
 $desc$Step into a deep lunge with the back knee down or hovering.
Place the same-side hand inside the front foot and plant the other hand on the floor.
Rotate the free arm toward the ceiling, following it with your eyes.
Keep the front knee tracking over the ankle; breathe into the upper back twist.

TARGETS: Hips, mid-back, and shoulders — opens the whole body and helps you twist more easily$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/worlds_greatest_stretch_howto.png'),
('71111111-1111-1111-1111-111111110007', 'Hamstring Stretch',
 $desc$Sit with one leg extended (or stand with a soft knee and hinge at the hips).
Lengthen the spine first, then tip forward from the hips — not by rounding the back.
Feel the stretch along the back of the thigh; keep the foot relaxed, not pointed hard.
Hold steadily; ease off if you feel sharp pain behind the knee.

TARGETS: Back of the thighs — eases tight legs so bending forward feels easier$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/hamstring_stretch_howto.png'),
('71111111-1111-1111-1111-111111110008', 'Figure-Four Glute Stretch',
 $desc$Lie on your back (or sit tall) and cross one ankle over the opposite knee.
Draw the uncrossed thigh toward your chest, keeping the crossed knee gently open.
Feel the stretch in the outer hip/glute of the crossed leg — keep the low back grounded.
Switch sides; avoid yanking the knee toward the chest.

TARGETS: Outer hips and glutes — loosens tight hips and can ease low-back tension$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/figure_four_glute_stretch_howto.png'),
('71111111-1111-1111-1111-111111110009', 'Chest Opener',
 $desc$Stand in a doorway with forearms on the frame, elbows near shoulder height — or clasp hands behind the back.
Step through gently (or lift arms) until you feel the chest and front of shoulders open.
Keep ribs down and neck long; do not shrug.
Breathe into the sternum; ease intensity if shoulders pinch.

TARGETS: Chest and front of shoulders — opens a rounded chest from phone and desk posture$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/chest_opener_howto.png'),
('71111111-1111-1111-1111-111111110010', 'Wrist Circles',
 $desc$Extend one arm forward with a soft elbow, or rest the forearm on a surface.
Slowly circle the wrist through its full comfortable range.
Keep fingers relaxed; reverse direction halfway.
Switch wrists; stop if you feel sharp joint pain.

TARGETS: Wrists and forearms — warms wrists for planks, push-ups, and handstands$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/wrist_circles_howto.png'),
('71111111-1111-1111-1111-111111110011', 'Side Body Stretch',
 $desc$Stand tall and reach one arm overhead.
Lean gently to the opposite side, keeping both feet grounded and hips square.
Feel the stretch along the outer ribs and waist — do not collapse forward.
Breathe into the long side; switch sides when noted.

TARGETS: Sides of the body — lengthens the waist and helps you reach overhead more freely$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/side_body_stretch_howto.png'),
('71111111-1111-1111-1111-111111110012', 'Ankle Circles',
 $desc$Sit or stand with one foot lifted slightly (hold a wall for balance if needed).
Draw slow circles with the toes, moving from the ankle — not the whole leg.
Circle both directions; keep the motion smooth and controlled.
Switch ankles; stay in a pain-free range.

TARGETS: Ankles — improves ankle mobility for squats, lunges, and balance$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/ankle_circles_howto.png'),
('71111111-1111-1111-1111-111111110013', 'Child''s Pose',
 $desc$Kneel and sit the hips back toward the heels; fold the torso over the thighs.
Reach arms forward or rest them beside the body — whichever feels more restful.
Let the forehead rest on the mat or a stacked fist; soften the jaw and shoulders.
Breathe slowly into the back ribs; ease knees apart if hips feel cramped.

TARGETS: Hips and lower back — a resting stretch that helps you settle and relax$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/childs_pose_howto.png'),
('71111111-1111-1111-1111-111111110014', 'Spinal Twist',
 $desc$Sit tall or lie on your back with knees bent.
Gently rotate the knees/torso to one side while keeping both shoulders grounded (supine) or the spine long (seated).
Look opposite the twist if comfortable; never force the range.
Hold and breathe; switch sides when noted.

TARGETS: Mid and lower back — a gentle twist that eases stiffness after sitting$desc$,
 'MOBILITY', 'TIME', 'BEGINNER', 'ACTIVE',
 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/spinal_twist_howto.png')
ON CONFLICT (id) DO NOTHING;

-- Existing DBs: form guides + placeholder images are refreshed by Flyway V5__stretch_exercise_guides.sql

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
