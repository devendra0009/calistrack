# Roadmap

| Version | Name | Focus |
| --- | --- | --- |
| V1 | Skill Tracker (~1 week) | Auth, graph, goal Q&A, PENDING sessions, train loop, post-workout video verify, progress, history |
| V2 | Intelligent Coach (2–3 weeks) | AI video verify, auto unlock, personalized workouts, form score, readiness |
| V3 | Social Platform | Leaderboards, friends, feed, challenges, clubs, verified ranks |
| V4 | AI Coach Platform | Adaptive plans, form correction, recovery, injury insights, paid coaching |

## V1 checklist

- [ ] Authentication (email + password)
- [ ] Skill graph (seeded Muscle-Up path)
- [ ] Goal + path questions → first PENDING session
- [ ] Train loop (attempts per exercise)
- [ ] Assessments after COMPLETED session (video + manual verify)
- [ ] Next PENDING session only when verified
- [ ] Progress dashboard
- [ ] Workout history

## V2+ notes

- Recommendation engine replaces simple “workout for current node”.
- Assessment status `PENDING_AI` becomes active.
- Leaderboard trust uses verified video assessments.
