# 25july done till now

| MVP-01 | Email + password auth + basic profile |
| MVP-02 | Admin/seed: nodes, skill graph, workouts, assign workouts to nodes, create questions |
| MVP-03 | One seeded path: Australian Pull-up → … → Muscle-Up |
| MVP-04 | Goal-path onboarding questions (no video); place UserNodes; create first `workout_session` PENDING |
| MVP-05 | Train loop: PENDING → IN_PROGRESS + exercise_attempts → COMPLETED unverified |
| MVP-06 | Home shows current session (PENDING / IN_PROGRESS / needs verify) |

# todo in above

| MVP-06 | Home shows current session (PENDING / IN_PROGRESS / needs verify) | -> show all workouts user had completed -> if it needs verification show them as maybe selecting dd(PENDING, COMPLETED BUT TO VERIFY, VERIFIED)
| MVP-07 | Post-workout video assessment on `workout.goal_node_id`; manual verify |
-> So whenever a user finishes a workout, assign them their new workout and let them finish their workout -> so user will be finishing all the workouts related to that goalNode (maybe 7sessions are there for 7days till user reaches the goalNode)
-> now comes the assesment page -> in this page we have all the goalNodes listed for a user -> a user can directly perform it, record it and upload it to tell the system user can do this workout!! -> this will help me clear up my assumption too when placing -> as when user was answering the questions we placed user at a certain node before his actualGoalNode assuming user can perform all these easier skills -> this page will help us verify that assumption!! ## Later -> I don't want to build a workout session manually each time -> Maybe ai can help here by creating a next workout session + attached exercises for each user specific to a goalNode till maybe 7days

| MVP-08 | On PASS after plan complete: mark node verified, unlock next node’s Day 1 PENDING |
-> **Implemented:** curated `workout_plan` per node; session COMPLETE → next plan day; last day → `AWAITING_VERIFY`; assessment PASS → next node Day 1.
-> Verify each session? **No** — verify the **node** after the plan. Optional per-session verify deferred.
-> AI per-user workouts? **No for MVP** — curated shared plans only; AI may later fill a missing shared plan template per node.


# 26july done till now

all the above todo points are achieved means 
    -> video assessment manual verify done as soon as it uploads
    -> assement page created where each goal node can be verified by uploading video
    -> one node multiple workouts daywise separated and done the changes on user + admin side both

## todos
-> upload videos compression
-> video upload fallback strategy to local
-> now i want to play with data (Basically niche learning about muscles, body composition, workouts, calesthenics and anatomy maybe)
    -> i need to study about calesthenics, all the goal skills we could have and how we can achieve the skills
    -> how to build strong foundations
    -> warmp exercises to avoid injury 
    -> on the basis of weight we need exercises or plans
    -> how a skill is achieved whats the science behind it and how to learn a skill faster & clear
    -> why form matters instead of reps
    -> if i could start over what i will focus more on -> does abraham lincoln quote fits here? -> sharping the axe?
    -> how to make calesthenics more fun
    -> benefits of it and disadvantage of gym -> how to motivate, influence and manipulate people to start calesthenics -> how to show my physique and skills impactfully so that people pay for my services and training 
    -> how to social media influencing and get brand deals by reaching that level of natural body
        -> should i show my calesthenics journey + building the app?
        -> should i show my diet + workout sessions 
    -> after i brainstorm on this, i will create my own workout plan -> feed this data to my database -> start using my own app to workout

## deep-dive notes
-> Master roadmap (science, foundations, skill tree, branding, Calistrack schema mapping): [calisthenics-master-roadmap.md](./calisthenics-master-roadmap.md)
-> Module 1 rewritten as kid-friendly chapters (terms, visualize-while-moving, nutrition before/after): start at "MODULE 1: Your Body Explained Like You're 10" 

# 30july
    -> read above docs and build the foundation to calesthenics, take in knowledge, to learn and teach later 
    -> build a whole workout plan and start practicing using it and upload videos
        -> created workout plan for front lever top-down 
        -> will plan a script and record a intro video today -> what we are gonna do -> how we plan to do it!!
            -> for now i have workout plan for fl only, comment down if you want me to add a skill and its roadmap for you !!
        -> live the app for users too 
## todos
    -> after this, we'll learn ai fundamentals and take google ai certification 
    -> then we'll build ai coach and diff functionalites for our calistrack!! 
        -> and start phase2 development!!
    -> make apis respond faster -> cuz slower api response can lead to audience just fuck off from your page, cuz they don't care about the journey or hard-work, they just need a working material that is efficient, faster and free

# 31 july 
    -> planning to build ai coach 
        -> let see what all it'll require and maybe work acc to it 
        