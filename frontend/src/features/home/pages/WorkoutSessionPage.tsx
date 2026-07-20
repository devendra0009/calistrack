import { Link, useNavigate, useParams } from "react-router";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import { ApiError } from "@/shared/api/errors";
import type { SessionExerciseLineDto } from "@/shared/api/types";
import { Button } from "@/shared/ui/Button";
import { PageShell } from "@/shared/ui/PageShell";
import { Spinner } from "@/shared/ui/Spinner";
import {
  useSessionTrainMutations,
  useWorkoutSessionDetail,
} from "@/features/home/api";

function targetInstruction(line: SessionExerciseLineDto): string {
  const parts: string[] = [];
  if (line.targetSets != null && line.targetReps != null) {
    parts.push(`Do ${line.targetSets} sets of ${line.targetReps} reps`);
  } else if (line.targetSets != null) {
    parts.push(`Do ${line.targetSets} sets`);
  } else if (line.targetReps != null) {
    parts.push(`Do ${line.targetReps} reps`);
  }
  if (line.targetHoldSeconds != null) {
    parts.push(`Hold ${line.targetHoldSeconds}s`);
  }
  if (line.targetRestSeconds != null) {
    parts.push(`Rest ${line.targetRestSeconds}s between sets`);
  }
  return parts.length
    ? parts.join(". ") + "."
    : "Complete this movement as prescribed.";
}

function isVideoUrl(url: string): boolean {
  return /\.(mp4|webm|mov|m4v)(\?|$)/i.test(url) || url.includes("/video/");
}

function ExerciseMedia({ line }: { line: SessionExerciseLineDto }) {
  const demo = line.demoVideoUrl;
  const thumb = line.thumbnailUrl;
  if (!demo && !thumb) return null;

  if (demo && isVideoUrl(demo)) {
    return (
      <div className="mt-3 overflow-hidden rounded-xl border border-stone-200 bg-black">
        <video
          src={demo}
          controls
          playsInline
          poster={thumb ?? undefined}
          className="aspect-video w-full object-contain"
        />
      </div>
    );
  }

  const imageUrl = demo ?? thumb;
  if (!imageUrl) return null;

  return (
    <div className="mt-3 overflow-hidden rounded-xl border border-stone-200 bg-stone-100">
      <img
        src={imageUrl}
        alt={`${line.exerciseName} demo`}
        className="aspect-video w-full object-cover"
      />
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
}: {
  startedAt: string | null;
  completedAt: string | null;
  running: boolean;
}) {
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    if (!running || !startedAt) return;
    const id = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, [running, startedAt]);

  if (!startedAt) {
    return (
      <div className="rounded-2xl border border-stone-200 bg-white/90 px-5 py-4 text-center">
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
    <div className="rounded-2xl border border-emerald-200 bg-emerald-50/80 px-5 py-4 text-center">
      <p className="text-xs font-semibold uppercase tracking-wide text-emerald-800">
        {running ? "Training" : "Final time"}
      </p>
      <p className="mt-1 font-mono text-3xl font-bold tabular-nums text-emerald-950">
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
  const beginRequested = useRef(false);

  // Auto-begin when opening a PENDING session (timer starts with Start Training).
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
      <PageShell title="Session">
        <p className="text-stone-600">Missing session id.</p>
      </PageShell>
    );
  }

  if (
    detail.isLoading ||
    (detail.data?.status === "PENDING" && begin.isPending)
  ) {
    return (
      <PageShell title="Session">
        <Spinner label="Starting workout…" />
      </PageShell>
    );
  }

  if (detail.isError || !detail.data) {
    return (
      <PageShell title="Session">
        <p className="text-red-600">
          {detail.error instanceof ApiError
            ? detail.error.message
            : "Could not load session."}
        </p>
        <Link
          to="/home"
          className="mt-4 inline-block text-sm font-medium text-emerald-800"
        >
          Back to home
        </Link>
      </PageShell>
    );
  }

  const session = detail.data;
  const readOnly =
    session.status === "COMPLETED" || session.status === "ABANDONED";
  const doneCount = session.exercises.filter(
    (e) => e.attempt?.status === "COMPLETED",
  ).length;
  const totalCount = session.exercises.length;
  const allDone = totalCount > 0 && doneCount === totalCount;

  return (
    <PageShell
      title={session.workoutTitle}
      subtitle={`Focus: ${session.focusNodeName}`}
      actions={
        <Link
          to="/home"
          className="rounded-lg px-3 py-2 text-sm font-medium text-stone-700 hover:bg-stone-100"
        >
          Home
        </Link>
      }
    >
      <div className="space-y-4">
        <SessionTimer
          startedAt={session.startedAt}
          completedAt={session.completedAt}
          running={!readOnly && Boolean(session.startedAt)}
        />

        {session.workoutDescription ? (
          <p className="text-sm text-stone-600">{session.workoutDescription}</p>
        ) : null}

        <p className="text-sm font-medium text-stone-700">
          Progress: {doneCount}/{totalCount} exercises done
        </p>

        <ul className="space-y-3">
          {session.exercises.map((line) => {
            const done = line.attempt?.status === "COMPLETED";
            return (
              <li
                key={line.workoutExerciseId}
                className={
                  done
                    ? "rounded-2xl border border-emerald-200 bg-emerald-50/70 p-4"
                    : "rounded-2xl border border-stone-200 bg-white/90 p-4"
                }
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
                      Exercise {line.sequence}
                    </p>
                    <h3 className="mt-1 text-base font-bold text-stone-900">
                      {line.exerciseName}
                    </h3>
                    <p className="mt-1 text-sm text-stone-600">
                      {targetInstruction(line)}
                    </p>
                    {line.notes ? (
                      <p className="mt-1 text-sm text-stone-500">
                        {line.notes}
                      </p>
                    ) : null}
                  </div>

                  {done ? (
                    <span className="shrink-0 rounded-full bg-emerald-700 px-3 py-1 text-xs font-semibold text-white">
                      Done
                    </span>
                  ) : readOnly ? (
                    <span className="shrink-0 rounded-full bg-stone-200 px-3 py-1 text-xs font-semibold text-stone-700">
                      Missed
                    </span>
                  ) : (
                    <Button
                      className="shrink-0"
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
                      Mark completed
                    </Button>
                  )}
                </div>

                <ExerciseMedia line={line} />
              </li>
            );
          })}
        </ul>

        {!readOnly ? (
          <Button
            className="w-full"
            loading={complete.isPending}
            disabled={!allDone}
            onClick={async () => {
              try {
                const next = await complete.mutateAsync();
                if (next.status === "PENDING") {
                  toast.success(`Next up: ${next.workoutTitle}`);
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
            Finish session
          </Button>
        ) : (
          <section className="rounded-2xl border border-amber-200 bg-amber-50/80 p-5">
            <p className="text-sm font-medium text-amber-950">
              Session completed
              {session.verified ? " and verified" : ", not verified yet"}. Video
              verification comes next.
            </p>
          </section>
        )}

        {!readOnly && !allDone ? (
          <p className="text-center text-xs text-stone-500">
            Mark every exercise completed to unlock Finish session.
          </p>
        ) : null}
      </div>
    </PageShell>
  );
}
