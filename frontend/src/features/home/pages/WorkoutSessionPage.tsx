import { Link, useNavigate, useParams } from "react-router";
import { useEffect, useRef, useState } from "react";
import { toast } from '@/shared/ui/notify'
import { ApiError } from "@/shared/api/errors";
import type { SessionExerciseLineDto } from "@/shared/api/types";
import { Button } from "@/shared/ui/Button";
import { PageError } from "@/shared/ui/PageError";
import { PageShell } from "@/shared/ui/PageShell";
import { Spinner } from "@/shared/ui/Spinner";
import { parseStretchGuide } from "@/features/stretching/lib/stretchGuide";
import {
  useSessionTrainMutations,
  useWorkoutSessionDetail,
} from "@/features/home/api";
import { useWorkoutMusic } from "@/features/workout-music/WorkoutMusicProvider";

/** Compact prescription chips: how much to do. */
function DoseChips({ line }: { line: SessionExerciseLineDto }) {
  const chips: string[] = [];
  if (line.targetSets != null && line.targetHoldSeconds != null) {
    chips.push(`${line.targetSets} × ${line.targetHoldSeconds}s`);
  } else if (line.targetSets != null && line.targetReps != null) {
    chips.push(`${line.targetSets} × ${line.targetReps}`);
  } else if (line.targetSets != null) {
    chips.push(`${line.targetSets} sets`);
  } else if (line.targetReps != null) {
    chips.push(`${line.targetReps} reps`);
  } else if (line.targetHoldSeconds != null) {
    chips.push(`${line.targetHoldSeconds}s hold`);
  }
  if (line.targetRestSeconds != null) {
    chips.push(`${line.targetRestSeconds}s rest`);
  }

  if (chips.length === 0) {
    return (
      <p className="mt-2 text-sm font-medium text-stone-700">
        Complete as prescribed
      </p>
    );
  }

  return (
    <div className="mt-2 flex flex-wrap gap-1.5">
      {chips.map((chip) => (
        <span
          key={chip}
          className="rounded-md bg-stone-900 px-2 py-1 text-xs font-semibold tabular-nums text-stone-50"
        >
          {chip}
        </span>
      ))}
    </div>
  );
}

/** Role badge from notes like [WARMUP] / [SKILL] — hide the rest of notes. */
function roleFromNotes(notes: string | null): string | null {
  if (!notes) return null;
  const match = notes.match(/^\s*\[([A-Z_]+)\]/);
  return match ? match[1].replaceAll("_", " ") : null;
}

function howToSteps(description: string | null): string[] {
  const guide = parseStretchGuide(description);
  if (guide.steps.length > 0) return guide.steps;
  if (!description?.trim()) return [];
  return description
    .split(/\n+/)
    .map((line) => line.replace(/^\s*\d+[.)]\s*/, "").trim())
    .filter(Boolean)
    .slice(0, 4);
}

function isVideoUrl(url: string): boolean {
  return /\.(mp4|webm|mov|m4v)(\?|$)/i.test(url) || url.includes("/video/");
}

/** Inline form guide — image/video always visible, no extra click. */
function FormGuide({ line }: { line: SessionExerciseLineDto }) {
  const demo = line.demoVideoUrl?.trim() || null;
  const thumb = line.thumbnailUrl?.trim() || null;
  const demoIsVideo = Boolean(demo && isVideoUrl(demo));
  const stillImage = thumb || (demo && !demoIsVideo ? demo : null);

  if (!stillImage && !demoIsVideo) return null;

  return (
    <div className="mt-3 overflow-hidden rounded-xl border border-stone-200 bg-stone-50">
      {demoIsVideo && demo ? (
        <video
          src={demo}
          controls
          playsInline
          preload="metadata"
          poster={thumb ?? undefined}
          className="aspect-4/3 max-h-48 w-full object-contain"
        />
      ) : stillImage ? (
        <img
          src={stillImage}
          alt=""
          className="max-h-48 w-full object-contain"
          loading="lazy"
        />
      ) : null}
    </div>
  );
}

function formatElapsed(totalSeconds: number): string {
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  if (h > 0) {
    return `${h}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  }
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function SessionTimer({
  startedAt,
  completedAt,
  running,
  accent = "emerald",
}: {
  startedAt: string | null;
  completedAt: string | null;
  running: boolean;
  accent?: "emerald" | "sky";
}) {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    if (!running || !startedAt) return;
    const id = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, [running, startedAt]);

  const idleBox =
    accent === "sky"
      ? "rounded-2xl border border-sky-200 bg-sky-50/90 px-5 py-4 text-center"
      : "rounded-2xl border border-stone-200 bg-stone-50/90 px-5 py-4 text-center";
  const activeBox =
    accent === "sky"
      ? "rounded-2xl border border-sky-200 bg-sky-50/80 px-5 py-4 text-center"
      : "rounded-2xl border border-emerald-200 bg-emerald-50/80 px-5 py-4 text-center";
  const labelColor =
    accent === "sky" ? "text-sky-900" : "text-emerald-900";
  const valueColor =
    accent === "sky" ? "text-sky-950" : "text-emerald-950";

  if (!startedAt) {
    return (
      <div className={idleBox}>
        <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
          Session timer
        </p>
        <p className="mt-1 font-mono text-3xl font-bold text-stone-400">
          00:00
        </p>
      </div>
    );
  }

  const startMs = new Date(startedAt).getTime();
  const endMs = completedAt ? new Date(completedAt).getTime() : now;
  const elapsed = Math.max(0, Math.floor((endMs - startMs) / 1000));

  return (
    <div className={activeBox}>
      <p className={`text-xs font-semibold uppercase tracking-wide ${labelColor}`}>
        {running ? (accent === "sky" ? "Stretching" : "Training") : "Final time"}
      </p>
      <p className={`mt-1 font-mono text-3xl font-bold tabular-nums ${valueColor}`}>
        {formatElapsed(elapsed)}
      </p>
    </div>
  );
}

export function WorkoutSessionPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const detail = useWorkoutSessionDetail(sessionId);
  const { begin, markDone, complete } = useSessionTrainMutations(
    sessionId ?? "",
  );
  const { leaveWorkout } = useWorkoutMusic();
  const beginRequested = useRef(false);

  useEffect(() => {
    if (!sessionId || !detail.data) return;
    if (detail.data.status !== "PENDING") return;
    if (beginRequested.current || begin.isPending) return;
    beginRequested.current = true;
    void begin.mutateAsync().catch((err) => {
      beginRequested.current = false;
      toast.error(
        err instanceof ApiError ? err.message : "Could not start training",
      );
    });
  }, [sessionId, detail.data, begin]);

  if (!sessionId) {
    return (
      <PageShell embedded title="Session">
        <p className="text-stone-600">Missing session id.</p>
      </PageShell>
    );
  }

  if (
    detail.isLoading ||
    (detail.data?.status === "PENDING" && begin.isPending)
  ) {
    return (
      <PageShell embedded title="Session">
        <Spinner label="Starting session…" />
      </PageShell>
    );
  }

  if (detail.isError || !detail.data) {
    return (
      <PageShell embedded title="Session">
        <PageError
          title="Session didn’t load"
          message={
            detail.error instanceof ApiError
              ? detail.error.message
              : "Could not load session."
          }
          onRetry={() => void detail.refetch()}
        >
          <Link
            to="/home"
            className="mt-3 inline-block text-sm font-medium text-emerald-900 hover:underline"
          >
            Back to home
          </Link>
        </PageError>
      </PageShell>
    );
  }

  const session = detail.data;
  const isStretch = session.workoutKind === "STRETCH";
  const readOnly =
    session.status === "COMPLETED" || session.status === "ABANDONED";
  const doneCount = session.exercises.filter(
    (e) => e.attempt?.status === "COMPLETED",
  ).length;
  const totalCount = session.exercises.length;
  const allDone = totalCount > 0 && doneCount === totalCount;
  const dayLabel =
    session.planDayNumber != null && session.planDurationDays != null
      ? ` · Day ${session.planDayNumber} of ${session.planDurationDays}`
      : "";

  return (
    <PageShell
      embedded
      title={session.workoutTitle}
      subtitle={
        isStretch
          ? `Morning stretch${dayLabel}`
          : `Focus: ${session.focusNodeName}${dayLabel}`
      }
    >
      <div className="space-y-4">
        {isStretch ? (
          <div className="rounded-2xl border border-sky-200 bg-sky-50/90 px-4 py-3 text-sm text-sky-950">
            Mobility routine — separate from skill workouts.{" "}
            <Link to="/stretch" className="font-semibold underline">
              Open Stretch hub
            </Link>
          </div>
        ) : null}

        {!isStretch && session.awaitingVerify ? (
          <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-950">
            Plan complete for this skill.{" "}
            <Link to="/assessment" className="font-semibold underline">
              Verify on Assessment
            </Link>{" "}
            to unlock the next node.
          </div>
        ) : null}

        <SessionTimer
          startedAt={session.startedAt}
          completedAt={session.completedAt}
          running={!readOnly && Boolean(session.startedAt)}
          accent={isStretch ? "sky" : "emerald"}
        />

        <p className="text-sm font-medium text-stone-700">
          {doneCount}/{totalCount} done
        </p>

        <ul className="space-y-3">
          {session.exercises.map((line) => {
            const done = line.attempt?.status === "COMPLETED";
            const role = roleFromNotes(line.notes);
            const guide = parseStretchGuide(line.exerciseDescription);
            const steps = guide.steps.length > 0
              ? guide.steps
              : howToSteps(line.exerciseDescription);

            return (
              <li
                key={line.workoutExerciseId}
                className={
                  done
                    ? isStretch
                      ? "rounded-2xl border border-sky-300 bg-sky-100/70 p-4"
                      : "rounded-2xl border border-emerald-200 bg-emerald-50/70 p-4"
                    : "rounded-2xl border border-stone-200 bg-stone-50/90 p-4"
                }
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p
                        className={
                          isStretch
                            ? "text-xs font-semibold uppercase tracking-wide text-sky-800"
                            : "text-xs font-semibold uppercase tracking-wide text-stone-500"
                        }
                      >
                        {line.sequence}. {line.exerciseName}
                      </p>
                      {role ? (
                        <span className="rounded bg-stone-100 px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-stone-600">
                          {role}
                        </span>
                      ) : null}
                    </div>

                    <DoseChips line={line} />

                    {steps.length > 0 ? (
                      <ol className="mt-2 space-y-0.5 text-sm text-stone-600">
                        {steps.map((step, i) => (
                          <li key={i} className="flex gap-2">
                            <span className="w-4 shrink-0 tabular-nums text-stone-400">
                              {i + 1}.
                            </span>
                            <span>{step}</span>
                          </li>
                        ))}
                      </ol>
                    ) : null}

                    {isStretch && guide.targets ? (
                      <p className="mt-2 text-xs text-sky-900/80">
                        <span className="font-semibold">Helps: </span>
                        {guide.targets}
                      </p>
                    ) : null}
                  </div>

                  {done ? (
                    <span
                      className={
                        isStretch
                          ? "shrink-0 rounded-full bg-sky-700 px-3 py-1 text-xs font-semibold text-white"
                          : "shrink-0 rounded-full bg-emerald-700 px-3 py-1 text-xs font-semibold text-white"
                      }
                    >
                      Done
                    </span>
                  ) : readOnly ? (
                    <span className="shrink-0 rounded-full bg-stone-200 px-3 py-1 text-xs font-semibold text-stone-700">
                      Missed
                    </span>
                  ) : (
                    <Button
                      className={
                        isStretch
                          ? "shrink-0 bg-sky-700! hover:bg-sky-800!"
                          : "shrink-0"
                      }
                      loading={
                        markDone.isPending &&
                        markDone.variables === line.workoutExerciseId
                      }
                      onClick={async () => {
                        try {
                          await markDone.mutateAsync(line.workoutExerciseId);
                          toast.success(`${line.exerciseName} marked done`);
                        } catch (err) {
                          toast.error(
                            err instanceof ApiError
                              ? err.message
                              : "Could not mark exercise",
                          );
                        }
                      }}
                    >
                      Done
                    </Button>
                  )}
                </div>

                <FormGuide line={line} />
              </li>
            );
          })}
        </ul>

        {!readOnly ? (
          <Button
            className={
              isStretch ? "w-full bg-sky-700! hover:bg-sky-800!" : "w-full"
            }
            loading={complete.isPending}
            disabled={!allDone}
            onClick={async () => {
              try {
                const next = await complete.mutateAsync();
                leaveWorkout();
                if (isStretch) {
                  toast.success("Stretching complete — nice start to the day");
                  navigate("/stretch", { replace: true });
                  return;
                }
                if (next.status === "PENDING") {
                  const day =
                    next.planDayNumber != null && next.planDurationDays != null
                      ? ` (Day ${next.planDayNumber} of ${next.planDurationDays})`
                      : "";
                  toast.success(`Next up: ${next.workoutTitle}${day}`);
                } else if (next.awaitingVerify) {
                  toast.success(
                    "Plan complete — verify this skill to unlock the next node",
                  );
                } else {
                  toast.success(
                    "Path complete — no more workouts for this goal",
                  );
                }
                navigate("/home", { replace: true });
              } catch (err) {
                toast.error(
                  err instanceof ApiError
                    ? err.message
                    : "Could not finish session",
                );
              }
            }}
          >
            {isStretch ? "Finish stretching" : "Finish session"}
          </Button>
        ) : isStretch ? (
          <section className="rounded-2xl border border-sky-200 bg-sky-50/80 p-5">
            <p className="text-sm font-medium text-sky-950">
              Stretch day completed. No skill verification needed.
            </p>
            <Button
              variant="secondary"
              className="mt-4 w-full sm:w-auto"
              onClick={() => navigate("/stretch")}
            >
              Back to Stretch
            </Button>
          </section>
        ) : (
          <section className="rounded-2xl border border-amber-200 bg-amber-50/80 p-5">
            <p className="text-sm font-medium text-amber-950">
              Session completed
              {session.verified ? " and verified" : ", not verified yet"}.
            </p>
            <p className="mt-2 text-sm text-amber-900/80">
              Prove the skill on your path with a short video — not every session
              needs a separate check.
            </p>
            <Button
              variant="primary"
              className="mt-4 w-full sm:w-auto"
              onClick={() => navigate("/assessment")}
            >
              Verify skills
            </Button>
          </section>
        )}

        {!readOnly && !allDone ? (
          <p className="text-center text-xs text-stone-500">
            Mark every {isStretch ? "stretch" : "exercise"} done to finish.
          </p>
        ) : null}
      </div>
    </PageShell>
  );
}
