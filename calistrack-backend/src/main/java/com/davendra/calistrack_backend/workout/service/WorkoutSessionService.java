package com.davendra.calistrack_backend.workout.service;

import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import com.davendra.calistrack_backend.catalog.repo.WorkoutExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.dto.WorkoutAssignment;
import com.davendra.calistrack_backend.path.facade.NextWorkoutFacade;
import com.davendra.calistrack_backend.path.facade.NextWorkoutFacade.NextPathStep;
import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;
import com.davendra.calistrack_backend.progress.repo.UserNodeRepository;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.dto.CurrentWorkoutSessionResponse;
import com.davendra.calistrack_backend.workout.dto.ExerciseAttemptResponse;
import com.davendra.calistrack_backend.workout.dto.PatchExerciseAttemptRequest;
import com.davendra.calistrack_backend.workout.dto.WorkoutSessionDetailResponse;
import com.davendra.calistrack_backend.workout.dto.WorkoutSessionDetailResponse.AttemptSummaryDto;
import com.davendra.calistrack_backend.workout.dto.WorkoutSessionDetailResponse.SessionExerciseLineDto;
import com.davendra.calistrack_backend.workout.entity.ExerciseAttempt;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.ExerciseAttemptStatus;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import com.davendra.calistrack_backend.workout.repo.ExerciseAttemptRepository;
import com.davendra.calistrack_backend.workout.repo.WorkoutSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseAttemptRepository exerciseAttemptRepository;
    private final WorkoutRepository workoutRepository;
    private final UserNodeRepository userNodeRepository;
    private final NextWorkoutFacade nextWorkoutFacade;
    private final CurrentUserService currentUserService;

    public WorkoutSessionService(
            WorkoutSessionRepository workoutSessionRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            ExerciseAttemptRepository exerciseAttemptRepository,
            WorkoutRepository workoutRepository,
            UserNodeRepository userNodeRepository,
            NextWorkoutFacade nextWorkoutFacade,
            CurrentUserService currentUserService
    ) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseAttemptRepository = exerciseAttemptRepository;
        this.workoutRepository = workoutRepository;
        this.userNodeRepository = userNodeRepository;
        this.nextWorkoutFacade = nextWorkoutFacade;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public boolean hasAnySession(AppUser user) {
        return workoutSessionRepository.existsByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public Optional<CurrentWorkoutSessionResponse> findCurrentForUser(AppUser user) {
        Optional<WorkoutSession> open = workoutSessionRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
                user.getId(),
                EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
        );
        WorkoutSession session = open
                .or(() -> workoutSessionRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()))
                .orElse(null);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(toCurrentResponse(session));
    }

    @Transactional(readOnly = true)
    public CurrentWorkoutSessionResponse requireCurrent() {
        AppUser user = currentUserService.requireActiveUser();
        return findCurrentForUser(user)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "No workout session found for current user"
                ));
    }

    @Transactional(readOnly = true)
    public WorkoutSessionDetailResponse getDetail(UUID sessionId) {
        AppUser user = currentUserService.requireActiveUser();
        WorkoutSession session = requireOwnedSession(sessionId, user);
        return toDetailResponse(session);
    }

    @Transactional(readOnly = true)
    public void assertNoOpenSession(AppUser user) {
        boolean open = workoutSessionRepository.existsByUserIdAndStatusIn(
                user.getId(),
                EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
        );
        if (open) {
            throw new ApiException(HttpStatus.CONFLICT, "User already has an open workout session");
        }
    }

    @Transactional
    public WorkoutSession createPending(AppUser user, Workout workout) {
        assertNoOpenSession(user);

        WorkoutSession session = new WorkoutSession();
        session.setUser(user);
        session.setWorkout(workout);
        session.setStatus(WorkoutSessionStatus.PENDING);
        session.setVerified(false);
        return workoutSessionRepository.save(session);
    }

    @Transactional
    public CurrentWorkoutSessionResponse beginSession(UUID sessionId) {
        AppUser user = currentUserService.requireActiveUser();
        WorkoutSession session = requireOwnedSession(sessionId, user);

        if (session.getStatus() == WorkoutSessionStatus.COMPLETED
                || session.getStatus() == WorkoutSessionStatus.ABANDONED) {
            throw new ApiException(HttpStatus.CONFLICT, "Session is already " + session.getStatus());
        }

        if (session.getStatus() == WorkoutSessionStatus.PENDING) {
            session.setStatus(WorkoutSessionStatus.IN_PROGRESS);
            session.setStartedAt(Instant.now());
            workoutSessionRepository.save(session);
        }

        return toCurrentResponse(session);
    }

    /**
     * One-tap "I did this exercise" — creates a COMPLETED {@code exercise_attempt}
     * (no sets/reps logging). First mark also begins the session if still PENDING.
     */
    @Transactional
    public ExerciseAttemptResponse markExerciseCompleted(UUID sessionId, UUID workoutExerciseId) {
        AppUser user = currentUserService.requireActiveUser();
        WorkoutSession session = requireOwnedSession(sessionId, user);

        if (session.getStatus() == WorkoutSessionStatus.COMPLETED
                || session.getStatus() == WorkoutSessionStatus.ABANDONED) {
            throw new ApiException(HttpStatus.CONFLICT, "Session is already " + session.getStatus());
        }

        WorkoutExercise line = workoutExerciseRepository
                .findByIdAndWorkout_Id(workoutExerciseId, session.getWorkout().getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Workout exercise not found on this session's workout"
                ));

        Optional<ExerciseAttempt> existing = exerciseAttemptRepository
                .findByWorkoutSession_IdAndWorkoutExercise_Id(sessionId, workoutExerciseId);
        if (existing.isPresent()) {
            ExerciseAttempt attempt = existing.get();
            if (attempt.getStatus() != ExerciseAttemptStatus.COMPLETED) {
                attempt.setStatus(ExerciseAttemptStatus.COMPLETED);
                return toAttemptResponse(exerciseAttemptRepository.save(attempt));
            }
            return toAttemptResponse(attempt);
        }

        if (session.getStatus() == WorkoutSessionStatus.PENDING) {
            session.setStatus(WorkoutSessionStatus.IN_PROGRESS);
            session.setStartedAt(Instant.now());
            workoutSessionRepository.save(session);
        }

        ExerciseAttempt attempt = new ExerciseAttempt();
        attempt.setWorkoutSession(session);
        attempt.setWorkoutExercise(line);
        attempt.setStatus(ExerciseAttemptStatus.COMPLETED);
        if (line.getTargetSets() != null) {
            attempt.setActualSets(line.getTargetSets());
        }
        if (line.getTargetReps() != null) {
            attempt.setActualReps(line.getTargetReps());
        }
        if (line.getTargetHoldSeconds() != null) {
            attempt.setActualHoldSeconds(line.getTargetHoldSeconds());
        }
        return toAttemptResponse(exerciseAttemptRepository.save(attempt));
    }

    @Transactional
    public ExerciseAttemptResponse startExercise(UUID sessionId, UUID workoutExerciseId) {
        // Backward-compatible alias: simplified UX marks the line done in one step.
        return markExerciseCompleted(sessionId, workoutExerciseId);
    }

    @Transactional
    public ExerciseAttemptResponse patchAttempt(UUID attemptId, PatchExerciseAttemptRequest request) {
        AppUser user = currentUserService.requireActiveUser();
        ExerciseAttempt attempt = exerciseAttemptRepository.findDetailedById(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exercise attempt not found"));

        WorkoutSession session = attempt.getWorkoutSession();
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Exercise attempt does not belong to current user");
        }
        if (session.getStatus() == WorkoutSessionStatus.COMPLETED
                || session.getStatus() == WorkoutSessionStatus.ABANDONED) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot update attempt on a finished session");
        }

        if (request.actualSets() != null) {
            attempt.setActualSets(request.actualSets());
        }
        if (request.actualReps() != null) {
            attempt.setActualReps(request.actualReps());
        }
        if (request.actualHoldSeconds() != null) {
            attempt.setActualHoldSeconds(request.actualHoldSeconds());
        }
        if (request.actualRestSeconds() != null) {
            attempt.setActualRestSeconds(request.actualRestSeconds());
        }
        if (request.notes() != null) {
            attempt.setNotes(request.notes().isBlank() ? null : request.notes().trim());
        }
        if (request.status() != null) {
            if (request.status() == ExerciseAttemptStatus.IN_PROGRESS
                    && attempt.getStatus() != ExerciseAttemptStatus.IN_PROGRESS) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot reopen a finished attempt");
            }
            attempt.setStatus(request.status());
        }

        return toAttemptResponse(exerciseAttemptRepository.save(attempt));
    }

    @Transactional
    public CurrentWorkoutSessionResponse completeSession(UUID sessionId) {
        AppUser user = currentUserService.requireActiveUserWithGoal();
        WorkoutSession session = requireOwnedSession(sessionId, user);

        if (session.getStatus() == WorkoutSessionStatus.COMPLETED) {
            // Prefer the newer PENDING session created after a previous finish.
            return findCurrentForUser(user).orElseGet(() -> toCurrentResponse(session));
        }
        if (session.getStatus() == WorkoutSessionStatus.ABANDONED) {
            throw new ApiException(HttpStatus.CONFLICT, "Abandoned session cannot be completed");
        }

        long lineCount = workoutExerciseRepository.countByWorkout_Id(session.getWorkout().getId());
        if (lineCount == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Workout has no exercises configured");
        }

        List<ExerciseAttempt> attempts = exerciseAttemptRepository.findByWorkoutSession_Id(sessionId);
        long completedCount = attempts.stream()
                .filter(a -> a.getStatus() == ExerciseAttemptStatus.COMPLETED)
                .count();

        if (completedCount != lineCount) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Mark every exercise completed before finishing ("
                            + completedCount + "/" + lineCount + " done)"
            );
        }

        Instant now = Instant.now();
        if (session.getStartedAt() == null) {
            session.setStartedAt(now);
        }
        session.setStatus(WorkoutSessionStatus.COMPLETED);
        session.setCompletedAt(now);
        session.setVerified(false);
        session.setDurationSeconds((int) Duration.between(session.getStartedAt(), now).getSeconds());
        workoutSessionRepository.save(session);

        Node completedFocus = session.getWorkout().getGoalNode();
        markUserNodeCompleted(user, completedFocus);

        WorkoutSession nextSession = createNextPendingSession(user, completedFocus);
        if (nextSession != null) {
            return toCurrentResponse(nextSession);
        }
        return toCurrentResponse(session);
    }

    /**
     * Advance path: unlock next node that has a workout and create a PENDING session for it.
     */
    private WorkoutSession createNextPendingSession(AppUser user, Node completedFocus) {
        Node goal = user.getCurrentGoalNode();
        if (goal == null) {
            return null;
        }

        Optional<NextPathStep> step = nextWorkoutFacade.nextWorkoutAfter(goal.getId(), completedFocus.getId());
        if (step.isEmpty()) {
            return null;
        }

        NextPathStep next = step.get();
        WorkoutAssignment assignment = next.assignment();

        for (UUID skippedId : next.skippedNodeIds()) {
            markUserNodeCompleted(user, skippedId);
        }
        markUserNodeAvailable(user, assignment.focusNodeId());

        Workout workout = workoutRepository.findByIdWithGoalNode(assignment.workoutId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Workout not found: " + assignment.workoutId()
                ));

        return createPending(user, workout);
    }

    private void markUserNodeCompleted(AppUser user, Node node) {
        markUserNodeCompleted(user, node.getId());
    }

    private void markUserNodeCompleted(AppUser user, UUID nodeId) {
        userNodeRepository.findByUser_IdAndNode_Id(user.getId(), nodeId).ifPresent(userNode -> {
            userNode.setStatus(UserNodeStatus.COMPLETED);
            userNode.setProgressPercentage(BigDecimal.valueOf(100));
            userNode.setLastAttemptAt(Instant.now());
            if (userNode.getUnlockedAt() == null) {
                userNode.setUnlockedAt(Instant.now());
            }
            userNodeRepository.save(userNode);
        });
    }

    private void markUserNodeAvailable(AppUser user, UUID nodeId) {
        userNodeRepository.findByUser_IdAndNode_Id(user.getId(), nodeId).ifPresent(userNode -> {
            if (userNode.getStatus() == UserNodeStatus.COMPLETED) {
                return;
            }
            userNode.setStatus(UserNodeStatus.AVAILABLE);
            if (userNode.getUnlockedAt() == null) {
                userNode.setUnlockedAt(Instant.now());
            }
            userNodeRepository.save(userNode);
        });
    }

    private WorkoutSession requireOwnedSession(UUID sessionId, AppUser user) {
        WorkoutSession session = workoutSessionRepository.findWithDetailsById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Workout session not found"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Workout session does not belong to current user");
        }
        return session;
    }

    private WorkoutSessionDetailResponse toDetailResponse(WorkoutSession session) {
        Workout workout = session.getWorkout();
        Node focus = workout.getGoalNode();
        List<WorkoutExercise> lines = workoutExerciseRepository
                .findByWorkout_IdOrderBySequenceAsc(workout.getId());
        Map<UUID, ExerciseAttempt> attemptsByLine = exerciseAttemptRepository
                .findByWorkoutSession_Id(session.getId())
                .stream()
                .collect(Collectors.toMap(a -> a.getWorkoutExercise().getId(), Function.identity()));

        List<SessionExerciseLineDto> exercises = lines.stream()
                .map(line -> {
                    Exercise exercise = line.getExercise();
                    ExerciseAttempt attempt = attemptsByLine.get(line.getId());
                    AttemptSummaryDto attemptDto = attempt == null ? null : new AttemptSummaryDto(
                            attempt.getId(),
                            attempt.getStatus(),
                            attempt.getActualSets(),
                            attempt.getActualReps(),
                            attempt.getActualHoldSeconds(),
                            attempt.getActualRestSeconds(),
                            attempt.getNotes()
                    );
                    String demoVideoUrl = line.getDemoVideoUrl() != null && !line.getDemoVideoUrl().isBlank()
                            ? line.getDemoVideoUrl()
                            : exercise.getDemoVideoUrl();
                    return new SessionExerciseLineDto(
                            line.getId(),
                            line.getSequence(),
                            exercise.getId(),
                            exercise.getName(),
                            exercise.getMetricType(),
                            exercise.getThumbnailUrl(),
                            demoVideoUrl,
                            line.getTargetSets(),
                            line.getTargetReps(),
                            line.getTargetHoldSeconds(),
                            line.getTargetRestSeconds(),
                            line.getNotes(),
                            attemptDto
                    );
                })
                .toList();

        return new WorkoutSessionDetailResponse(
                session.getId(),
                workout.getId(),
                workout.getTitle(),
                workout.getDescription(),
                focus.getId(),
                focus.getName(),
                session.getStatus(),
                session.isVerified(),
                session.getStartedAt(),
                session.getCompletedAt(),
                exercises
        );
    }

    private CurrentWorkoutSessionResponse toCurrentResponse(WorkoutSession session) {
        Workout workout = session.getWorkout();
        Node focus = workout.getGoalNode();
        return new CurrentWorkoutSessionResponse(
                session.getId(),
                workout.getId(),
                workout.getTitle(),
                workout.getDescription(),
                focus.getId(),
                focus.getName(),
                session.getStatus(),
                session.isVerified(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private ExerciseAttemptResponse toAttemptResponse(ExerciseAttempt attempt) {
        return new ExerciseAttemptResponse(
                attempt.getId(),
                attempt.getWorkoutSession().getId(),
                attempt.getWorkoutExercise().getId(),
                attempt.getStatus(),
                attempt.getActualSets(),
                attempt.getActualReps(),
                attempt.getActualHoldSeconds(),
                attempt.getActualRestSeconds(),
                attempt.getNotes()
        );
    }
}
