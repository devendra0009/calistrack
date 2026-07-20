import { useActionState, useEffect, useId, useRef, useState, startTransition, type ChangeEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { register } from '@/features/auth/api'
import { registerSchema } from '@/features/auth/schemas'
import { uploadAvatarAndSetProfile } from '@/features/media/api'

type FormState = { error?: string; fieldErrors?: Record<string, string> }

const ACCEPT = 'image/jpeg,image/png,image/webp,image/gif'

export function RegisterPage() {
  const navigate = useNavigate()
  const fileInputId = useId()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [avatarFile, setAvatarFile] = useState<File | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)

  useEffect(() => {
    if (!avatarFile) {
      setPreviewUrl(null)
      return
    }
    const url = URL.createObjectURL(avatarFile)
    setPreviewUrl(url)
    return () => URL.revokeObjectURL(url)
  }, [avatarFile])

  const [state, formAction, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const parsed = registerSchema.safeParse({
        displayName: formData.get('displayName'),
        email: formData.get('email'),
        password: formData.get('password'),
      })

      if (!parsed.success) {
        const fieldErrors: Record<string, string> = {}
        for (const issue of parsed.error.issues) {
          const key = String(issue.path[0] ?? 'form')
          if (!fieldErrors[key]) fieldErrors[key] = issue.message
        }
        return { fieldErrors }
      }

      try {
        await register(parsed.data)

        if (avatarFile) {
          await uploadAvatarAndSetProfile(avatarFile)
        }

        startTransition(() => navigate('/setup', { replace: true }))
        return {}
      } catch (err) {
        const message =
          err instanceof ApiError ? err.message : 'Unable to create account'
        return { error: message }
      }
    },
    {},
  )

  function onAvatarChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null
    setAvatarFile(file)
  }

  function clearAvatar() {
    setAvatarFile(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  return (
    <PageShell
      title="Start your path"
      subtitle="Create an account to set a goal and get your first workout session."
    >
      <form action={formAction} className="max-w-md space-y-4 rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm">
        <div className="flex flex-col items-center gap-3 pb-2">
          <label
            htmlFor={fileInputId}
            className="group relative flex h-28 w-28 cursor-pointer items-center justify-center overflow-hidden rounded-full border-2 border-dashed border-stone-300 bg-stone-50 transition hover:border-emerald-600 hover:bg-emerald-50/40"
          >
            {previewUrl ? (
              <img
                src={previewUrl}
                alt="Profile preview"
                className="h-full w-full object-cover"
              />
            ) : (
              <span className="px-3 text-center text-xs font-medium text-stone-500 group-hover:text-emerald-800">
                Add photo
              </span>
            )}
            <input
              id={fileInputId}
              ref={fileInputRef}
              type="file"
              accept={ACCEPT}
              className="sr-only"
              onChange={onAvatarChange}
            />
          </label>
          <div className="flex items-center gap-3 text-sm">
            <button
              type="button"
              className="font-semibold text-emerald-800 hover:underline"
              onClick={() => fileInputRef.current?.click()}
            >
              {avatarFile ? 'Change photo' : 'Upload profile photo'}
            </button>
            {avatarFile ? (
              <button
                type="button"
                className="text-stone-500 hover:text-stone-800 hover:underline"
                onClick={clearAvatar}
              >
                Remove
              </button>
            ) : null}
          </div>
          <p className="text-center text-xs text-stone-500">
            Optional · JPEG, PNG, WebP, or GIF · max 5 MB
          </p>
        </div>

        <Input
          label="Display name"
          name="displayName"
          autoComplete="nickname"
          required
          error={state.fieldErrors?.displayName}
        />
        <Input
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          required
          error={state.fieldErrors?.email}
        />
        <Input
          label="Password"
          name="password"
          type="password"
          autoComplete="new-password"
          required
          minLength={6}
          error={state.fieldErrors?.password}
        />
        {state.error ? (
          <p className="text-sm text-red-600" role="alert">
            {state.error}
          </p>
        ) : null}
        <Button type="submit" loading={pending} className="w-full">
          Create account
        </Button>
        <p className="text-center text-sm text-stone-600">
          Already have an account?{' '}
          <Link className="font-semibold text-emerald-800 hover:underline" to="/login">
            Sign in
          </Link>
        </p>
      </form>
    </PageShell>
  )
}
