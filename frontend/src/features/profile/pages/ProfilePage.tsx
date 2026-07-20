import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import type { ExperienceLevel, Gender } from '@/shared/api/types'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { UserAvatar } from '@/shared/ui/UserAvatar'
import { useMe, usePatchMe } from '@/features/profile/api'

const schema = z.object({
  displayName: z.string().min(1).max(100),
  heightCm: z.string().optional(),
  weightKg: z.string().optional(),
  age: z.string().optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER', 'UNSPECIFIED']).optional(),
  experience: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']).optional(),
})

type FormValues = z.infer<typeof schema>

export function ProfilePage() {
  const me = useMe()
  const patchMe = usePatchMe()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: me.data
      ? {
          displayName: me.data.displayName,
          heightCm: me.data.heightCm?.toString() ?? '',
          weightKg: me.data.weightKg?.toString() ?? '',
          age: me.data.age?.toString() ?? '',
          gender: me.data.gender ?? undefined,
          experience: me.data.experience ?? undefined,
        }
      : undefined,
  })

  if (me.isLoading) {
    return (
      <PageShell title="Your profile">
        <Spinner />
      </PageShell>
    )
  }

  if (!me.data) {
    return (
      <PageShell title="Your profile">
        <p className="text-red-600">Could not load profile.</p>
      </PageShell>
    )
  }

  const watchedName = form.watch('displayName') || me.data.displayName

  return (
    <PageShell
      title="Your profile"
      subtitle="Optional stats — you can update these anytime."
    >
      <form
        className="max-w-md space-y-4 rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm"
        onSubmit={form.handleSubmit(async (values) => {
          try {
            await patchMe.mutateAsync({
              displayName: values.displayName,
              heightCm: values.heightCm ? Number(values.heightCm) : undefined,
              weightKg: values.weightKg ? Number(values.weightKg) : undefined,
              age: values.age ? Number(values.age) : undefined,
              gender: values.gender as Gender | undefined,
              experience: values.experience as ExperienceLevel | undefined,
            })
            toast.success('Profile updated')
          } catch (err) {
            toast.error(
              err instanceof ApiError ? err.message : 'Update failed',
            )
          }
        })}
      >
        <div className="flex flex-col items-center gap-2 pb-2">
          <UserAvatar
            displayName={watchedName}
            avatarUrl={me.data.avatarUrl}
            size="lg"
          />
          <p className="text-sm text-stone-500">
            {me.data.avatarUrl
              ? 'Profile photo'
              : 'No photo yet — showing initials'}
          </p>
        </div>

        <Input label="Display name" {...form.register('displayName')} />
        <Input label="Height (cm)" type="number" step="0.1" {...form.register('heightCm')} />
        <Input label="Weight (kg)" type="number" step="0.1" {...form.register('weightKg')} />
        <Input label="Age" type="number" {...form.register('age')} />

        <label className="flex flex-col gap-1.5 text-sm">
          <span className="font-medium text-stone-800">Gender</span>
          <select
            className="rounded-lg border border-stone-300 bg-white px-3 py-2"
            {...form.register('gender')}
          >
            <option value="">Prefer not to say</option>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
            <option value="UNSPECIFIED">Unspecified</option>
          </select>
        </label>

        <label className="flex flex-col gap-1.5 text-sm">
          <span className="font-medium text-stone-800">Experience</span>
          <select
            className="rounded-lg border border-stone-300 bg-white px-3 py-2"
            {...form.register('experience')}
          >
            <option value="">Select…</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
          </select>
        </label>

        <Button type="submit" loading={patchMe.isPending}>
          Save profile
        </Button>
      </form>
    </PageShell>
  )
}
