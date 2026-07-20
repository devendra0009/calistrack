import { useId, useState, type ChangeEvent, type FormEvent } from 'react'
import { toast } from 'sonner'
import {
  useAdminExercises,
  useCreateExercise,
  useDeprecateExercise,
  useUpdateExercise,
} from '@/features/admin/api'
import {
  CATALOG_STATUS,
  DIFFICULTY,
  EXERCISE_CATEGORY,
  METRIC_TYPE,
} from '@/features/admin/constants'
import {
  uploadExerciseDemo,
  uploadExerciseThumbnail,
} from '@/features/media/api'
import type { AdminExerciseRequest, AdminExerciseResponse } from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

const emptyForm = (): AdminExerciseRequest => ({
  name: '',
  description: '',
  category: 'STATIC',
  metricType: 'TIME',
  difficulty: 'BEGINNER',
  thumbnailUrl: null,
  demoVideoUrl: null,
  status: 'ACTIVE',
})

function isVideoUrl(url: string): boolean {
  return /\.(mp4|webm|mov|m4v)(\?|$)/i.test(url) || url.includes('/video/')
}

export function AdminExercisesPage() {
  const list = useAdminExercises()
  const create = useCreateExercise()
  const update = useUpdateExercise()
  const deprecate = useDeprecateExercise()

  const [form, setForm] = useState<AdminExerciseRequest>(emptyForm)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [uploadingThumb, setUploadingThumb] = useState(false)
  const [uploadingDemo, setUploadingDemo] = useState(false)

  const thumbInputId = useId()
  const demoInputId = useId()

  function startCreate() {
    setEditingId(null)
    setForm(emptyForm())
    setShowForm(true)
  }

  function startEdit(ex: AdminExerciseResponse) {
    setEditingId(ex.id)
    setForm({
      name: ex.name,
      description: ex.description ?? '',
      category: ex.category,
      metricType: ex.metricType,
      difficulty: ex.difficulty,
      thumbnailUrl: ex.thumbnailUrl,
      demoVideoUrl: ex.demoVideoUrl,
      status: ex.status,
    })
    setShowForm(true)
  }

  async function onThumbnailChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setUploadingThumb(true)
    try {
      const url = await uploadExerciseThumbnail(file)
      setForm((prev) => ({ ...prev, thumbnailUrl: url }))
      toast.success('Thumbnail uploaded')
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Thumbnail upload failed')
    } finally {
      setUploadingThumb(false)
    }
  }

  async function onDemoChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setUploadingDemo(true)
    try {
      const url = await uploadExerciseDemo(file)
      setForm((prev) => ({ ...prev, demoVideoUrl: url }))
      toast.success('Demo media uploaded')
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Demo upload failed')
    } finally {
      setUploadingDemo(false)
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    try {
      if (editingId) {
        await update.mutateAsync({ id: editingId, body: form })
        toast.success('Exercise updated')
      } else {
        await create.mutateAsync(form)
        toast.success('Exercise created')
      }
      setShowForm(false)
      setEditingId(null)
      setForm(emptyForm())
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Save failed')
    }
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-emerald-800">
            Step 1
          </p>
          <h1 className="text-2xl font-bold text-stone-900">Exercises</h1>
          <p className="mt-1 text-sm text-stone-600">
            Reusable moves. Goals and workout lines both reference these.
          </p>
        </div>
        <Button type="button" onClick={startCreate}>
          New exercise
        </Button>
      </header>

      {showForm ? (
        <form
          onSubmit={onSubmit}
          className="space-y-3 rounded-2xl border border-stone-200 bg-white/90 p-5 shadow-sm"
        >
          <h2 className="font-semibold text-stone-900">
            {editingId ? 'Edit exercise' : 'Create exercise'}
          </h2>
          <Input
            label="Name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
            placeholder="e.g. 3s Planche Hold"
          />
          <label className="flex flex-col gap-1.5 text-sm">
            <span className="font-medium text-stone-800">Description</span>
            <textarea
              className="min-h-20 rounded-lg border border-stone-300 bg-white px-3 py-2 text-stone-900 outline-none ring-emerald-600/30 focus:ring-2"
              value={form.description ?? ''}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </label>
          <div className="grid gap-3 sm:grid-cols-2">
            <SelectField
              label="Category"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              options={[...EXERCISE_CATEGORY]}
            />
            <SelectField
              label="Metric type"
              value={form.metricType}
              onChange={(e) => setForm({ ...form, metricType: e.target.value })}
              options={[...METRIC_TYPE]}
            />
            <SelectField
              label="Difficulty"
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
              options={[...DIFFICULTY]}
            />
            <SelectField
              label="Status"
              value={form.status ?? 'ACTIVE'}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              options={[...CATALOG_STATUS]}
            />
          </div>

          <div className="grid gap-4 border-t border-stone-100 pt-4 sm:grid-cols-2">
            <div className="space-y-2">
              <p className="text-sm font-medium text-stone-800">Thumbnail image</p>
              <p className="text-xs text-stone-500">Shown while the athlete performs this move.</p>
              {form.thumbnailUrl ? (
                <div className="overflow-hidden rounded-xl border border-stone-200 bg-stone-50">
                  <img
                    src={form.thumbnailUrl}
                    alt="Exercise thumbnail"
                    className="aspect-video w-full object-cover"
                  />
                </div>
              ) : (
                <div className="flex aspect-video items-center justify-center rounded-xl border border-dashed border-stone-300 bg-stone-50 text-xs text-stone-500">
                  No thumbnail
                </div>
              )}
              <div className="flex flex-wrap gap-2">
                <label
                  htmlFor={thumbInputId}
                  className="inline-flex cursor-pointer items-center justify-center rounded-lg border border-stone-300 bg-white px-4 py-2.5 text-sm font-semibold text-stone-900 hover:bg-stone-50"
                >
                  {uploadingThumb
                    ? 'Uploading…'
                    : form.thumbnailUrl
                      ? 'Replace image'
                      : 'Upload image'}
                </label>
                <input
                  id={thumbInputId}
                  type="file"
                  accept="image/jpeg,image/png,image/webp,image/gif"
                  className="sr-only"
                  disabled={uploadingThumb}
                  onChange={onThumbnailChange}
                />
                {form.thumbnailUrl ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => setForm({ ...form, thumbnailUrl: null })}
                  >
                    Remove
                  </Button>
                ) : null}
              </div>
            </div>

            <div className="space-y-2">
              <p className="text-sm font-medium text-stone-800">Demo video / image</p>
              <p className="text-xs text-stone-500">Preferred media shown during the session.</p>
              {form.demoVideoUrl ? (
                <div className="overflow-hidden rounded-xl border border-stone-200 bg-stone-50">
                  {isVideoUrl(form.demoVideoUrl) ? (
                    <video
                      src={form.demoVideoUrl}
                      controls
                      playsInline
                      className="aspect-video w-full bg-black object-contain"
                    />
                  ) : (
                    <img
                      src={form.demoVideoUrl}
                      alt="Exercise demo"
                      className="aspect-video w-full object-cover"
                    />
                  )}
                </div>
              ) : (
                <div className="flex aspect-video items-center justify-center rounded-xl border border-dashed border-stone-300 bg-stone-50 text-xs text-stone-500">
                  No demo media
                </div>
              )}
              <div className="flex flex-wrap gap-2">
                <label
                  htmlFor={demoInputId}
                  className="inline-flex cursor-pointer items-center justify-center rounded-lg border border-stone-300 bg-white px-4 py-2.5 text-sm font-semibold text-stone-900 hover:bg-stone-50"
                >
                  {uploadingDemo
                    ? 'Uploading…'
                    : form.demoVideoUrl
                      ? 'Replace media'
                      : 'Upload media'}
                </label>
                <input
                  id={demoInputId}
                  type="file"
                  accept="video/mp4,video/webm,video/quicktime,image/jpeg,image/png,image/webp,image/gif"
                  className="sr-only"
                  disabled={uploadingDemo}
                  onChange={onDemoChange}
                />
                {form.demoVideoUrl ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => setForm({ ...form, demoVideoUrl: null })}
                  >
                    Remove
                  </Button>
                ) : null}
              </div>
            </div>
          </div>

          <div className="flex gap-2 pt-2">
            <Button
              type="submit"
              loading={create.isPending || update.isPending}
              disabled={uploadingThumb || uploadingDemo}
            >
              {editingId ? 'Save' : 'Create'}
            </Button>
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setShowForm(false)
                setEditingId(null)
              }}
            >
              Cancel
            </Button>
          </div>
        </form>
      ) : null}

      {list.isLoading ? (
        <Spinner />
      ) : (
        <ul className="space-y-2">
          {(list.data ?? []).map((ex) => (
            <li
              key={ex.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-stone-200 bg-white/90 px-4 py-3 shadow-sm"
            >
              <div className="flex min-w-0 items-center gap-3">
                {ex.thumbnailUrl ? (
                  <img
                    src={ex.thumbnailUrl}
                    alt=""
                    className="h-12 w-12 shrink-0 rounded-lg object-cover"
                  />
                ) : (
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-stone-100 text-[10px] font-medium text-stone-400">
                    {ex.demoVideoUrl ? 'Demo' : 'No media'}
                  </div>
                )}
                <div className="min-w-0">
                  <p className="font-semibold text-stone-900">{ex.name}</p>
                  <p className="text-xs text-stone-500">
                    {ex.category} · {ex.metricType} · {ex.difficulty} · {ex.status}
                    {ex.demoVideoUrl ? ' · demo' : ''}
                  </p>
                </div>
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="secondary" onClick={() => startEdit(ex)}>
                  Edit
                </Button>
                {ex.status !== 'DEPRECATED' ? (
                  <Button
                    type="button"
                    variant="ghost"
                    loading={deprecate.isPending}
                    onClick={async () => {
                      try {
                        await deprecate.mutateAsync(ex.id)
                        toast.success('Exercise deprecated')
                      } catch (err) {
                        toast.error(
                          err instanceof ApiError ? err.message : 'Deprecate failed',
                        )
                      }
                    }}
                  >
                    Deprecate
                  </Button>
                ) : null}
              </div>
            </li>
          ))}
          {!list.data?.length ? (
            <p className="text-sm text-stone-500">No exercises yet — create one above.</p>
          ) : null}
        </ul>
      )}
    </div>
  )
}
