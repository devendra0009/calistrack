-- Plain-language stretch guides + placeholder how-to images.
-- Replace thumbnail_url values with your real Cloudinary (or other) links when ready.

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/neck_circles_howto.png',
  description = $neck$
Sit or stand tall with shoulders relaxed.
Drop your chin slightly and roll your head in slow, wide circles.
Move only through a pain-free range — never force or crack the neck.
Switch directions halfway; keep breathing steady.

TARGETS: Neck and upper shoulders — eases desk stiffness and makes it easier to turn your head
$neck$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110001';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/shoulder_rolls_howto.png',
  description = $shoulders$
Stand or sit upright with arms hanging naturally.
Lift shoulders toward ears, then roll them back and down in a smooth circle.
Feel the shoulder blades glide; keep the neck long and jaw soft.
Reverse direction for the second half of the hold.

TARGETS: Shoulders and upper back — loosens tight shoulders and helps you sit/stand taller
$shoulders$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110002';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/cat_cow_howto.png',
  description = $catcow$
Start on all fours (or stand with hands on thighs).
Inhale as you drop the belly and lift the chest (cow); gaze softens forward.
Exhale as you round the spine, tuck the pelvis, and look toward the navel (cat).
Move with the breath — feel each vertebra articulate, not just the lower back.

TARGETS: Mid and lower back — wakes up a stiff spine and feels good first thing in the morning
$catcow$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110003';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/standing_forward_fold_howto.png',
  description = $fold$
Stand with feet hip-width; soft bend in the knees.
Hinge at the hips and fold forward, letting the torso hang heavy.
Hold elbows or let arms dangle; keep weight mid-foot, not in the heels.
Gently nod the head yes/no to release the neck — never lock the knees.

TARGETS: Back of the legs and lower back — lengthens tight legs and helps you unwind
$fold$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110004';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/hip_flexor_stretch_howto.png',
  description = $hipflex$
Kneel on one knee with the front foot planted (or step into a standing lunge).
Tuck the pelvis under so the low back stays long — avoid dumping into a swayback.
Gently shift hips forward until you feel the front of the rear hip open.
Keep the ribs stacked over the pelvis; switch sides when noted.

TARGETS: Front of the hips — opens hips after sitting and makes lunges feel freer
$hipflex$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110005';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/worlds_greatest_stretch_howto.png',
  description = $wgs$
Step into a deep lunge with the back knee down or hovering.
Place the same-side hand inside the front foot and plant the other hand on the floor.
Rotate the free arm toward the ceiling, following it with your eyes.
Keep the front knee tracking over the ankle; breathe into the upper back twist.

TARGETS: Hips, mid-back, and shoulders — opens the whole body and helps you twist more easily
$wgs$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110006';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/hamstring_stretch_howto.png',
  description = $ham$
Sit with one leg extended (or stand with a soft knee and hinge at the hips).
Lengthen the spine first, then tip forward from the hips — not by rounding the back.
Feel the stretch along the back of the thigh; keep the foot relaxed, not pointed hard.
Hold steadily; ease off if you feel sharp pain behind the knee.

TARGETS: Back of the thighs — eases tight legs so bending forward feels easier
$ham$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110007';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/figure_four_glute_stretch_howto.png',
  description = $fig4$
Lie on your back (or sit tall) and cross one ankle over the opposite knee.
Draw the uncrossed thigh toward your chest, keeping the crossed knee gently open.
Feel the stretch in the outer hip/glute of the crossed leg — keep the low back grounded.
Switch sides; avoid yanking the knee toward the chest.

TARGETS: Outer hips and glutes — loosens tight hips and can ease low-back tension
$fig4$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110008';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/chest_opener_howto.png',
  description = $chest$
Stand in a doorway with forearms on the frame, elbows near shoulder height — or clasp hands behind the back.
Step through gently (or lift arms) until you feel the chest and front of shoulders open.
Keep ribs down and neck long; do not shrug.
Breathe into the sternum; ease intensity if shoulders pinch.

TARGETS: Chest and front of shoulders — opens a rounded chest from phone and desk posture
$chest$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110009';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/wrist_circles_howto.png',
  description = $wrist$
Extend one arm forward with a soft elbow, or rest the forearm on a surface.
Slowly circle the wrist through its full comfortable range.
Keep fingers relaxed; reverse direction halfway.
Switch wrists; stop if you feel sharp joint pain.

TARGETS: Wrists and forearms — warms wrists for planks, push-ups, and handstands
$wrist$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110010';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/side_body_stretch_howto.png',
  description = $side$
Stand tall and reach one arm overhead.
Lean gently to the opposite side, keeping both feet grounded and hips square.
Feel the stretch along the outer ribs and waist — do not collapse forward.
Breathe into the long side; switch sides when noted.

TARGETS: Sides of the body — lengthens the waist and helps you reach overhead more freely
$side$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110011';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/ankle_circles_howto.png',
  description = $ankle$
Sit or stand with one foot lifted slightly (hold a wall for balance if needed).
Draw slow circles with the toes, moving from the ankle — not the whole leg.
Circle both directions; keep the motion smooth and controlled.
Switch ankles; stay in a pain-free range.

TARGETS: Ankles — improves ankle mobility for squats, lunges, and balance
$ankle$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110012';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/childs_pose_howto.png',
  description = $child$
Kneel and sit the hips back toward the heels; fold the torso over the thighs.
Reach arms forward or rest them beside the body — whichever feels more restful.
Let the forehead rest on the mat or a stacked fist; soften the jaw and shoulders.
Breathe slowly into the back ribs; ease knees apart if hips feel cramped.

TARGETS: Hips and lower back — a resting stretch that helps you settle and relax
$child$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110013';

UPDATE exercise SET
  thumbnail_url = 'https://res.cloudinary.com/dap8lkkgr/image/upload/v0000000000/calistrack/exercise_thumbnail/spinal_twist_howto.png',
  description = $twist$
Sit tall or lie on your back with knees bent.
Gently rotate the knees/torso to one side while keeping both shoulders grounded (supine) or the spine long (seated).
Look opposite the twist if comfortable; never force the range.
Hold and breathe; switch sides when noted.

TARGETS: Mid and lower back — a gentle twist that eases stiffness after sitting
$twist$,
  updated_at = now()
WHERE id = '71111111-1111-1111-1111-111111110014';
