import { useMemo, useState } from 'react'
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

const CM_PER_INCH = 2.54
const LB_PER_KG = 2.2046226218

type HeightUnit = 'cm' | 'ft'
type WeightUnit = 'kg' | 'lb'

const schema = z.object({
  displayName: z.string().min(1).max(100),
  heightCm: z.string().optional(),
  heightFt: z.string().optional(),
  heightIn: z.string().optional(),
  weightValue: z.string().optional(),
  dateOfBirth: z.string().optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER', 'UNSPECIFIED']).optional(),
  experience: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']).optional(),
})

type FormValues = z.infer<typeof schema>

function cmToFtIn(cm: number): { ft: number; inches: number } {
  const totalInches = cm / CM_PER_INCH
  let ft = Math.floor(totalInches / 12)
  let inches = Math.round(totalInches - ft * 12)
  if (inches === 12) {
    ft += 1
    inches = 0
  }
  return { ft, inches }
}

function ftInToCm(ft: number, inches: number): number {
  return Math.round((ft * 12 + inches) * CM_PER_INCH * 100) / 100
}

function parseOptionalNumber(value: string | undefined): number | undefined {
  if (!value?.trim()) return undefined
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}

function ageFromDob(isoDate: string): number | null {
  if (!isoDate) return null
  const dob = new Date(`${isoDate}T00:00:00`)
  if (Number.isNaN(dob.getTime())) return null
  const today = new Date()
  let age = today.getFullYear() - dob.getFullYear()
  const m = today.getMonth() - dob.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age -= 1
  return age >= 0 ? age : null
}

function bmiFromMetric(heightCm: number, weightKg: number): number {
  const m = heightCm / 100
  return weightKg / (m * m)
}

function bmiLabel(bmi: number): string {
  if (bmi < 18.5) return 'Underweight'
  if (bmi < 25) return 'Normal'
  if (bmi < 30) return 'Overweight'
  return 'Obese'
}

function UnitToggle<T extends string>({
  value,
  options,
  onChange,
}: {
  value: T
  options: { value: T; label: string }[]
  onChange: (v: T) => void
}) {
  return (
    <div className="inline-flex rounded-lg border border-stone-300 bg-stone-50 p-0.5 text-xs font-semibold">
      {options.map((opt) => (
        <button
          key={opt.value}
          type="button"
          className={
            value === opt.value
              ? 'rounded-md bg-stone-900 px-2.5 py-1 text-stone-50'
              : 'rounded-md px-2.5 py-1 text-stone-600'
          }
          onClick={() => onChange(opt.value)}
        >
          {opt.label}
        </button>
      ))}
    </div>
  )
}

export function ProfilePage() {
  const me = useMe()
  const patchMe = usePatchMe()
  const [heightUnit, setHeightUnit] = useState<HeightUnit>('cm')
  const [weightUnit, setWeightUnit] = useState<WeightUnit>('kg')

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: me.data
      ? {
          displayName: me.data.displayName,
          heightCm: me.data.heightCm?.toString() ?? '',
          heightFt: me.data.heightCm
            ? cmToFtIn(me.data.heightCm).ft.toString()
            : '',
          heightIn: me.data.heightCm
            ? cmToFtIn(me.data.heightCm).inches.toString()
            : '',
          weightValue: me.data.weightKg?.toString() ?? '',
          dateOfBirth: me.data.dateOfBirth ?? '',
          gender: me.data.gender ?? undefined,
          experience: me.data.experience ?? undefined,
        }
      : undefined,
  })

  const watched = form.watch()

  const liveHeightCm = useMemo(() => {
    if (heightUnit === 'cm') {
      return parseOptionalNumber(watched.heightCm)
    }
    const ft = parseOptionalNumber(watched.heightFt) ?? 0
    const inches = parseOptionalNumber(watched.heightIn) ?? 0
    if (!watched.heightFt?.trim() && !watched.heightIn?.trim()) return undefined
    return ftInToCm(ft, inches)
  }, [heightUnit, watched.heightCm, watched.heightFt, watched.heightIn])

  const liveWeightKg = useMemo(() => {
    const raw = parseOptionalNumber(watched.weightValue)
    if (raw == null) return undefined
    return weightUnit === 'lb'
      ? Math.round((raw / LB_PER_KG) * 100) / 100
      : raw
  }, [watched.weightValue, weightUnit])

  const liveAge = useMemo(
    () => ageFromDob(watched.dateOfBirth ?? '') ?? me.data?.age ?? null,
    [watched.dateOfBirth, me.data?.age],
  )

  const liveBmi = useMemo(() => {
    if (liveHeightCm == null || liveWeightKg == null) return null
    if (liveHeightCm <= 0 || liveWeightKg <= 0) return null
    return Math.round(bmiFromMetric(liveHeightCm, liveWeightKg) * 10) / 10
  }, [liveHeightCm, liveWeightKg])

  function switchHeightUnit(next: HeightUnit) {
    if (next === heightUnit) return
    if (next === 'ft') {
      const cm = parseOptionalNumber(form.getValues('heightCm'))
      if (cm != null) {
        const { ft, inches } = cmToFtIn(cm)
        form.setValue('heightFt', String(ft))
        form.setValue('heightIn', String(inches))
      }
    } else {
      const ft = parseOptionalNumber(form.getValues('heightFt')) ?? 0
      const inches = parseOptionalNumber(form.getValues('heightIn')) ?? 0
      if (form.getValues('heightFt') || form.getValues('heightIn')) {
        form.setValue('heightCm', String(ftInToCm(ft, inches)))
      }
    }
    setHeightUnit(next)
  }

  function switchWeightUnit(next: WeightUnit) {
    if (next === weightUnit) return
    const raw = parseOptionalNumber(form.getValues('weightValue'))
    if (raw != null) {
      if (next === 'lb') {
        form.setValue(
          'weightValue',
          String(Math.round(raw * LB_PER_KG * 10) / 10),
        )
      } else {
        form.setValue(
          'weightValue',
          String(Math.round((raw / LB_PER_KG) * 100) / 100),
        )
      }
    }
    setWeightUnit(next)
  }

  if (me.isLoading) {
    return (
      <PageShell embedded title="Your profile">
        <Spinner />
      </PageShell>
    )
  }

  if (!me.data) {
    return (
      <PageShell embedded title="Your profile">
        <p className="text-red-600">Could not load profile.</p>
      </PageShell>
    )
  }

  const watchedName = watched.displayName || me.data.displayName

  return (
    <PageShell
      embedded
      title="Your profile"
      subtitle="Optional stats — you can update these anytime."
    >
      <form
        className="max-w-md space-y-4 rounded-2xl border border-stone-200 bg-stone-50/90 p-6 shadow-sm"
        onSubmit={form.handleSubmit(async (values) => {
          try {
            let heightCm: number | undefined
            if (heightUnit === 'cm') {
              heightCm = parseOptionalNumber(values.heightCm)
            } else if (values.heightFt?.trim() || values.heightIn?.trim()) {
              heightCm = ftInToCm(
                parseOptionalNumber(values.heightFt) ?? 0,
                parseOptionalNumber(values.heightIn) ?? 0,
              )
            }

            let weightKg: number | undefined
            const weightRaw = parseOptionalNumber(values.weightValue)
            if (weightRaw != null) {
              weightKg =
                weightUnit === 'lb'
                  ? Math.round((weightRaw / LB_PER_KG) * 100) / 100
                  : weightRaw
            }

            await patchMe.mutateAsync({
              displayName: values.displayName,
              heightCm,
              weightKg,
              dateOfBirth: values.dateOfBirth?.trim()
                ? values.dateOfBirth
                : undefined,
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

        <div className="space-y-2">
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm font-medium text-stone-800">Height</span>
            <UnitToggle
              value={heightUnit}
              onChange={switchHeightUnit}
              options={[
                { value: 'cm', label: 'cm' },
                { value: 'ft', label: 'ft / in' },
              ]}
            />
          </div>
          {heightUnit === 'cm' ? (
            <Input
              label="Centimetres"
              type="number"
              step="0.1"
              min="0"
              {...form.register('heightCm')}
            />
          ) : (
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="Feet"
                type="number"
                min="0"
                step="1"
                {...form.register('heightFt')}
              />
              <Input
                label="Inches"
                type="number"
                min="0"
                max="11"
                step="1"
                {...form.register('heightIn')}
              />
            </div>
          )}
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm font-medium text-stone-800">Weight</span>
            <UnitToggle
              value={weightUnit}
              onChange={switchWeightUnit}
              options={[
                { value: 'kg', label: 'kg' },
                { value: 'lb', label: 'lb' },
              ]}
            />
          </div>
          <Input
            label={weightUnit === 'kg' ? 'Kilograms' : 'Pounds'}
            type="number"
            step="0.1"
            min="0"
            {...form.register('weightValue')}
          />
        </div>

        <Input
          label="Date of birth"
          type="date"
          max={new Date().toISOString().slice(0, 10)}
          {...form.register('dateOfBirth')}
        />

        <div className="grid grid-cols-2 gap-3 rounded-xl border border-stone-200 bg-stone-50 px-3 py-3 text-sm">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
              Age
            </p>
            <p className="mt-0.5 font-semibold tabular-nums text-stone-900">
              {liveAge != null ? `${liveAge} yrs` : '—'}
            </p>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
              BMI
            </p>
            <p className="mt-0.5 font-semibold tabular-nums text-stone-900">
              {liveBmi != null ? (
                <>
                  {liveBmi}{' '}
                  <span className="font-medium text-stone-500">
                    · {bmiLabel(liveBmi)}
                  </span>
                </>
              ) : (
                '—'
              )}
            </p>
          </div>
        </div>

        <label className="flex flex-col gap-1.5 text-sm">
          <span className="font-medium text-stone-800">Gender</span>
          <select
            className="rounded-lg border border-stone-300 bg-stone-50 px-3 py-2"
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
            className="rounded-lg border border-stone-300 bg-stone-50 px-3 py-2"
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
