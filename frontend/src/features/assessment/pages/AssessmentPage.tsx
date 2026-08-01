import { useEffect, useId, useRef, useState, type ChangeEvent } from 'react'
import { Check, Circle, Flag, Play, Upload, X } from 'lucide-react'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import type {
  GoalPathAssessmentResponse,
  PathAssessmentNodeResponse,
  UserNodeStatus,
} from '@/shared/api/types'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/Button'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { useAssessmentPath, useVerifySkillMutation } from '@/features/assessment/api'

const VIDEO_ACCEPT = 'video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov'
const VIDEO_MIME = new Set(['video/mp4', 'video/webm', 'video/quicktime'])
const MAX_VIDEO_BYTES = 50 * 1024 * 1024

function statusLabel(status: UserNodeStatus): string {
  switch (status) {
    case 'COMPLETED':
      return 'Completed'
    case 'AVAILABLE':
      return 'Current focus'
    case 'IN_PROGRESS':
      return 'In progress'
    case 'LOCKED':
      return 'Ahead'
  }
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function assertVideoFile(file: File): string | null {
  if (!VIDEO_MIME.has(file.type)) {
    return 'Use an MP4, WebM, or MOV video'
  }
  if (file.size > MAX_VIDEO_BYTES) {
    return 'Video must be 50 MB or smaller'
  }
  return null
}

function VideoPlayer({
  src,
  label,
}: {
  src: string
  label: string
}) {
  return (
    <div className="overflow-hidden rounded-xl border border-stone-200 bg-stone-950">
      <video
        src={src}
        controls
        playsInline
        preload="metadata"
        className="max-h-72 w-full bg-black"
        aria-label={label}
      >
        Your browser does not support video playback.
      </video>
    </div>
  )
}

type Draft = { file: File; previewUrl: string }

function PathNodeRow({
  node,
  isLast,
  uploadingNodeId,
  onConfirmUpload,
}: {
  node: PathAssessmentNodeResponse
  isLast: boolean
  uploadingNodeId: string | null
  onConfirmUpload: (nodeId: string, file: File) => Promise<void>
}) {
  const inputId = useId()
  const inputRef = useRef<HTMLInputElement>(null)
  const [draft, setDraft] = useState<Draft | null>(null)
  const [showUploaded, setShowUploaded] = useState(false)
  const busy = uploadingNodeId === node.nodeId

  useEffect(() => {
    return () => {
      if (draft?.previewUrl) URL.revokeObjectURL(draft.previewUrl)
    }
  }, [draft])

  const clearDraft = () => {
    if (draft?.previewUrl) URL.revokeObjectURL(draft.previewUrl)
    setDraft(null)
  }

  const onChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return

    const error = assertVideoFile(file)
    if (error) {
      toast.error(error)
      return
    }

    if (draft?.previewUrl) URL.revokeObjectURL(draft.previewUrl)
    setDraft({ file, previewUrl: URL.createObjectURL(file) })
    setShowUploaded(false)
  }

  const openPicker = () => inputRef.current?.click()

  const submitDraft = async () => {
    if (!draft) return
    try {
      await onConfirmUpload(node.nodeId, draft.file)
      clearDraft()
      setShowUploaded(true)
    } catch {
      // keep draft so the user can retry
    }
  }

  return (
    <li className="relative flex gap-4">
      <div className="flex w-10 shrink-0 flex-col items-center">
        <span
          className={cn(
            'relative z-10 flex size-10 items-center justify-center rounded-full border-2',
            node.verified &&
              'border-emerald-600 bg-emerald-600 text-white shadow-sm',
            !node.verified &&
              node.awaitingVerify &&
              'border-amber-500 bg-amber-50 text-amber-900',
            !node.verified &&
              !node.awaitingVerify &&
              node.status === 'AVAILABLE' &&
              'border-emerald-500 bg-emerald-50 text-emerald-900',
            !node.verified &&
              !node.awaitingVerify &&
              node.status === 'COMPLETED' &&
              'border-amber-400 bg-amber-50 text-amber-800',
            !node.verified &&
              !node.awaitingVerify &&
              (node.status === 'LOCKED' || node.status === 'IN_PROGRESS') &&
              'border-stone-300 bg-stone-50 text-stone-500',
          )}
          aria-hidden
        >
          {node.verified ? (
            <Check className="size-5 stroke-[2.5]" />
          ) : node.goal ? (
            <Flag className="size-4" />
          ) : (
            <Circle className="size-3.5 fill-current opacity-40" />
          )}
        </span>
        {!isLast ? (
          <span
            className={cn(
              'mt-1 min-h-8 w-0.5 flex-1 rounded-full',
              node.verified ? 'bg-emerald-300' : 'bg-stone-200',
            )}
            aria-hidden
          />
        ) : null}
      </div>

      <div
        className={cn(
          'mb-4 min-w-0 flex-1 rounded-2xl border px-4 py-4',
          node.verified && 'border-emerald-200 bg-emerald-50/60',
          !node.verified && node.awaitingVerify && 'border-amber-300 bg-amber-50/80 shadow-sm',
          !node.verified && !node.awaitingVerify && node.goal && 'border-emerald-300 bg-stone-50 shadow-sm',
          !node.verified && !node.awaitingVerify && !node.goal && 'border-stone-200 bg-stone-50/90',
        )}
      >
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <p className="text-base font-semibold text-stone-900">
                {node.name}
              </p>
              {node.goal ? (
                <span className="rounded-md bg-emerald-100 px-1.5 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-emerald-900">
                  Goal
                </span>
              ) : null}
              {node.awaitingVerify && !node.verified ? (
                <span className="rounded-md bg-amber-100 px-1.5 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-amber-900">
                  Verify to unlock next
                </span>
              ) : null}
            </div>
            <p className="mt-1 text-xs font-medium text-stone-500">
              Step {node.stepIndex}
              {' · '}
              {node.verified ? 'Verified' : statusLabel(node.status)}
              {node.difficulty ? ` · ${node.difficulty}` : null}
            </p>
            {node.description ? (
              <p className="mt-1.5 line-clamp-2 text-sm text-stone-600">
                {node.description}
              </p>
            ) : null}
            {!node.verified && node.awaitingVerify ? (
              <p className="mt-1.5 text-xs text-amber-900">
                Plan finished — upload a video to unlock the next node&apos;s Day 1.
              </p>
            ) : null}
            {!node.verified && !node.awaitingVerify && node.status === 'COMPLETED' ? (
              <p className="mt-1.5 text-xs text-amber-800">
                Assumed from placement — upload a video to confirm.
              </p>
            ) : null}
          </div>

          <div className="flex shrink-0 flex-col items-stretch gap-2 sm:items-end">
            <input
              ref={inputRef}
              id={inputId}
              type="file"
              accept={VIDEO_ACCEPT}
              className="sr-only"
              disabled={busy}
              onChange={onChange}
            />

            {node.verified && !draft ? (
              <span className="inline-flex items-center justify-center gap-1.5 rounded-lg bg-emerald-100 px-3 py-2 text-sm font-semibold text-emerald-900">
                <Check className="size-4 stroke-[2.5]" />
                Verified
              </span>
            ) : null}

            {!draft && node.verified && node.videoUrl ? (
              <Button
                type="button"
                variant="secondary"
                className="w-full sm:w-auto"
                onClick={() => setShowUploaded((v) => !v)}
              >
                <Play className="size-4" />
                {showUploaded ? 'Hide video' : 'Watch video'}
              </Button>
            ) : null}

            {!draft && !node.verified ? (
              <Button
                type="button"
                variant="primary"
                disabled={uploadingNodeId !== null}
                className="w-full sm:w-auto"
                onClick={openPicker}
              >
                <Upload className="size-4" />
                Verify skill
              </Button>
            ) : null}

            {!draft && node.verified ? (
              <button
                type="button"
                disabled={busy || uploadingNodeId !== null}
                className="text-center text-xs font-medium text-stone-500 underline-offset-2 hover:text-stone-800 hover:underline disabled:opacity-50"
                onClick={openPicker}
              >
                Replace video
              </button>
            ) : null}
          </div>
        </div>

        {showUploaded && node.videoUrl && !draft ? (
          <div className="mt-4 space-y-2">
            <p className="text-xs font-medium uppercase tracking-wide text-stone-500">
              Your uploaded proof
            </p>
            <VideoPlayer src={node.videoUrl} label={`Proof for ${node.name}`} />
          </div>
        ) : null}

        {draft ? (
          <div className="mt-4 space-y-3 rounded-xl border border-stone-200 bg-stone-50/80 p-3 sm:p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-sm font-semibold text-stone-900">
                  Preview before upload
                </p>
                <p className="mt-0.5 truncate text-xs text-stone-500">
                  {draft.file.name} · {formatFileSize(draft.file.size)}
                </p>
              </div>
              <button
                type="button"
                className="rounded-lg p-1.5 text-stone-500 hover:bg-stone-200 hover:text-stone-800"
                aria-label="Discard preview"
                disabled={busy}
                onClick={clearDraft}
              >
                <X className="size-4" />
              </button>
            </div>

            <VideoPlayer
              src={draft.previewUrl}
              label={`Preview for ${node.name}`}
            />

            <div className="flex flex-col gap-2 sm:flex-row sm:justify-end">
              <Button
                type="button"
                variant="secondary"
                disabled={busy}
                className="w-full sm:w-auto"
                onClick={openPicker}
              >
                Choose different
              </Button>
              <Button
                type="button"
                variant="ghost"
                disabled={busy}
                className="w-full sm:w-auto"
                onClick={clearDraft}
              >
                Cancel
              </Button>
              <Button
                type="button"
                variant="primary"
                loading={busy}
                disabled={uploadingNodeId !== null && !busy}
                className="w-full sm:w-auto"
                onClick={() => void submitDraft()}
              >
                <Upload className="size-4" />
                Upload & verify
              </Button>
            </div>
          </div>
        ) : null}
      </div>
    </li>
  )
}

function PathProgress({ data }: { data: GoalPathAssessmentResponse }) {
  const pct =
    data.totalCount === 0
      ? 0
      : Math.round((data.verifiedCount / data.totalCount) * 100)

  return (
    <section className="rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm">
      <div className="flex items-end justify-between gap-3">
        <div>
          <h2 className="text-sm font-semibold uppercase tracking-wide text-stone-500">
            Path to
          </h2>
          <p className="mt-1 text-xl font-bold text-stone-900">
            {data.goalNodeName}
          </p>
        </div>
        <p className="text-sm font-semibold tabular-nums text-emerald-900">
          {data.verifiedCount}/{data.totalCount} verified
        </p>
      </div>
      <div className="mt-4 h-2 overflow-hidden rounded-full bg-stone-100">
        <div
          className="h-full rounded-full bg-emerald-600 transition-[width] duration-500 ease-out"
          style={{ width: `${pct}%` }}
        />
      </div>
      <p className="mt-3 text-sm text-stone-600">
        Film yourself performing each skill, preview the clip, then upload.
        Verifying clears placement assumptions for earlier steps.
      </p>
    </section>
  )
}

export function AssessmentPage() {
  const pathQuery = useAssessmentPath()
  const verify = useVerifySkillMutation()
  const [uploadingNodeId, setUploadingNodeId] = useState<string | null>(null)

  const onConfirmUpload = async (nodeId: string, file: File) => {
    setUploadingNodeId(nodeId)
    try {
      const result = await verify.mutateAsync({ nodeId, file })
      toast.success(`${result.nodeName} verified`)
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : 'Could not verify skill',
      )
      throw err
    } finally {
      setUploadingNodeId(null)
    }
  }

  if (pathQuery.isLoading) {
    return (
      <PageShell embedded title="Skill assessment">
        <Spinner />
      </PageShell>
    )
  }

  if (pathQuery.isError || !pathQuery.data) {
    return (
      <PageShell
        embedded
        title="Skill assessment"
        subtitle="Prove the skills on your path with a short video."
      >
        <p className="rounded-xl border border-dashed border-stone-200 bg-stone-50/80 px-4 py-6 text-center text-sm text-stone-600">
          Could not load your skill path. Make sure you have a goal set, then try
          again.
        </p>
      </PageShell>
    )
  }

  const data = pathQuery.data

  return (
    <PageShell
      embedded
      title="Skill assessment"
      subtitle="Walk your path and verify each skill when you can perform it."
    >
      <div className="space-y-6">
        <PathProgress data={data} />

        <section>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-stone-500">
            Your path
          </h2>
          {data.nodes.length === 0 ? (
            <p className="rounded-xl border border-dashed border-stone-200 bg-stone-50/80 px-4 py-6 text-center text-sm text-stone-600">
              No path nodes found for this goal.
            </p>
          ) : (
            <ol className="list-none p-0">
              {data.nodes.map((node, index) => (
                <PathNodeRow
                  key={node.nodeId}
                  node={node}
                  isLast={index === data.nodes.length - 1}
                  uploadingNodeId={uploadingNodeId}
                  onConfirmUpload={onConfirmUpload}
                />
              ))}
            </ol>
          )}
        </section>
      </div>
    </PageShell>
  )
}
